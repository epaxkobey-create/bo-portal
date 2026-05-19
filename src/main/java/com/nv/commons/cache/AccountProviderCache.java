package com.nv.commons.cache;

import com.nv.commons.bo.AccountProviderBO;
import com.nv.commons.cache.key.AccountProviderCacheKey;
import com.nv.commons.cache.key.AccountProviderKey;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountProviderDAO;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 紀錄玩家在遊戲商那邊建立的帳號
 */
public class AccountProviderCache extends AbstractCache {

	private ConcurrentHashMap<AccountProviderCacheKey, Set<AccountProvider>> cache = new ConcurrentHashMap<>();

	private static final AccountProviderCache instance = new AccountProviderCache();

	private Timestamp latestUpdateTime = new Timestamp(System.currentTimeMillis());

	// 只cache過去四小時的資料，找不到會再去DB抓取
	private static final int CACHE_HOURS = 4;

	private AccountProviderCache() {

	}

	public static AccountProviderCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		ConcurrentHashMap<AccountProviderCacheKey, Set<AccountProvider>> temp = new ConcurrentHashMap<>();

		WebsiteInfoCache.getInstance().getAll().parallelStream().forEach(websiteInfo -> {

			WebSiteType webSiteType = WebSiteType.getInstance(websiteInfo.getId());

			if (webSiteType == null) {
				return;
			}

			try (Connection conn = DBPool.getReadConnection()) {
				List<AccountProvider> accountProviderListInDb = AccountProviderDAO.findAliveByWebsiteAndTime(conn,
					webSiteType.unique(), CACHE_HOURS);

				for (AccountProvider accountProviderInDb : accountProviderListInDb) {

					AccountProviderCacheKey cacheKey = new AccountProviderCacheKey(
						accountProviderInDb.getWebsiteType(),
						accountProviderInDb.getUserId());

					temp.computeIfAbsent(cacheKey, list -> new CopyOnWriteArraySet<>())
						.add(accountProviderInDb);
				}

			} catch (Exception ex) {
				LogUtils.providerMonitor.error(ex.getMessage(), ex);
			}
		});
		this.cache = temp;

	}

	public AccountProvider getAccountProvider(AccountProviderKey key) {

		Set<AccountProvider> set = getModifyAccountProviderSet(
			new AccountProviderCacheKey(key.getWebsiteType(), key.getUserId()));

		for (AccountProvider ap : set) {
			if (key.getProviderId() == ap.getProviderId()) {
				return ap;
			}
		}

		try {
			AccountProvider accountProvider = AccountProviderBO.getAccountProvider(
				key.getUserId(),
				key.getWebsiteType(),
				key.getProviderId()
			);
			return updateAccountProviderCache(accountProvider);
		} catch (Throwable e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

	public Set<AccountProvider> getAccountProviderSet(String userId, int webSiteType) {
		return Collections.unmodifiableSet(
			getModifyAccountProviderSet(new AccountProviderCacheKey(webSiteType, userId)));
	}

	private Set<AccountProvider> getModifyAccountProviderSet(AccountProviderCacheKey accountProviderCacheKey) {

		return cache.computeIfAbsent(accountProviderCacheKey, key -> {
			List<AccountProvider> accountProviderList = AccountProviderBO.getAccountProviderList(
				accountProviderCacheKey.websiteType(), accountProviderCacheKey.userId());

			if (accountProviderList.isEmpty()) {
				return new CopyOnWriteArraySet<>();
			}
			return new CopyOnWriteArraySet<>(accountProviderList);
		});
	}

	public BigDecimal getAllSumBalance(int websiteType, String userId) {
		Set<AccountProvider> accountProviderList = getAccountProviderSet(userId, websiteType);

		if (accountProviderList.isEmpty()) {
			return BigDecimal.ZERO;
		}

		return accountProviderList
			.stream()
			.map(AccountProvider::getProviderBalance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public void update() {

		long twoSecondsAgo = System.currentTimeMillis() - (2 * 1000);

		try {
			Timestamp temp = latestUpdateTime;

			final List<AccountProvider> userAccountProviderListInDb = AccountProviderBO.findLatestUpdate(
				latestUpdateTime);

			Timestamp lastUpdateTime = batchUpdateAccountProviderCache(userAccountProviderListInDb);

			if (lastUpdateTime.after(temp)) {
				temp = lastUpdateTime;
			}

			//MEMO 若無資料更新 則將下次查詢的時間推進到這次開始做Update時間的前兩秒
			if (latestUpdateTime.compareTo(temp) == 0) {
				latestUpdateTime = new Timestamp(twoSecondsAgo);
			} else {
				latestUpdateTime = temp;
			}

		} catch (Exception ex) {
			LogUtils.providerMonitor.error("error while update AccountProvider list", ex);
		}
	}

	private Timestamp batchUpdateAccountProviderCache(List<AccountProvider> updatedAccountProviderListInDb) {
		if (updatedAccountProviderListInDb == null || updatedAccountProviderListInDb.isEmpty()) {
			return new Timestamp(System.currentTimeMillis());
		}

		Map<AccountProviderCacheKey, List<AccountProvider>> updatedDataInDbByCacheKey = updatedAccountProviderListInDb.stream()
			.collect(Collectors.groupingBy(accountProvider -> new AccountProviderCacheKey(
				accountProvider.getWebsiteType(), accountProvider.getUserId())));

		// web只會將有登入的玩家 account provider放到cache，所以只需更新有異動且存在cache中的資料，不需放其他未登入玩家的account provider
		if (ServerInfoUtils.isPlayerServer()) {
			for (Map.Entry<AccountProviderCacheKey, List<AccountProvider>> entry : updatedDataInDbByCacheKey.entrySet()) {
				AccountProviderCacheKey key = entry.getKey();
				//update exists data in cache
				List<AccountProvider> updatedAccountProviderListInDbByCacheKey = entry.getValue();
				if (cache.containsKey(key)) {
					updateAccountProviderInCache(key, updatedAccountProviderListInDbByCacheKey);
				}
			}
		} else {
			// 在mg因為bonus結算需求，如果只放有異動的accountProvider到cache中，會漏掉該玩家其他未異動的accountProvider，
			// 所以需該玩家將所有accountProvider放到cache中
			String[][] userKeysNotInCache = updatedDataInDbByCacheKey.keySet().stream()
				.filter(key -> !cache.containsKey(key))
				.map(key -> new String[] {key.userId(), String.valueOf(key.websiteType())})
				.toArray(String[][]::new);

			// 一次查出所有未在cache中的玩家所有的accountProvider，減少db查詢次數
			List<AccountProvider> list = AccountProviderBO.findByWebsitesAndUserIds(userKeysNotInCache);

			Map<AccountProviderCacheKey, List<AccountProvider>> allAccountProviderMapByUserKey =
				list.stream()
					.collect(Collectors.groupingBy(accountProvider -> new AccountProviderCacheKey(
						accountProvider.getWebsiteType(), accountProvider.getUserId())));

			for (Map.Entry<AccountProviderCacheKey, List<AccountProvider>> entry : updatedDataInDbByCacheKey.entrySet()) {
				AccountProviderCacheKey key = entry.getKey();
				//update exists data in cache
				List<AccountProvider> updatedAccountProviderListInDbByCacheKey = entry.getValue();
				if (cache.containsKey(key)) {
					updateAccountProviderInCache(key, updatedAccountProviderListInDbByCacheKey);
				} else {
					List<AccountProvider> newAccountProviders = allAccountProviderMapByUserKey.get(key);
					if (CollectionUtils.isNotEmpty(newAccountProviders)) {
						cache.put(key, new CopyOnWriteArraySet<>(newAccountProviders));
					}
				}
			}
		}

		return updatedAccountProviderListInDb.stream()
			.map(AccountProvider::getProviderUpdateTime)
			.max(Comparator.naturalOrder())
			.orElse(new Timestamp(System.currentTimeMillis()));
	}

	private void updateAccountProviderInCache(AccountProviderCacheKey key,
		List<AccountProvider> updatedAccountProviderListInDbByCacheKey) {

		Set<AccountProvider> accountProviderSetInCache = cache.get(key);

		Map<Integer, AccountProvider> accountProviderInCacheByProviderId = accountProviderSetInCache.stream()
			.collect(Collectors.toMap(AccountProvider::getProviderId, Function.identity()));

		for (AccountProvider updatedAccountProviderInDb : updatedAccountProviderListInDbByCacheKey) {

			AccountProvider accountProviderInCache = accountProviderInCacheByProviderId.get(
				updatedAccountProviderInDb.getProviderId());

			if (accountProviderInCache != null) {
				accountProviderInCache.setProviderBalance(updatedAccountProviderInDb.getProviderBalance());
				accountProviderInCache.setProviderPassword(updatedAccountProviderInDb.getProviderPassword());
				accountProviderInCache.setProviderUpdateTime(updatedAccountProviderInDb.getProviderUpdateTime());
				accountProviderInCache.setExposure(updatedAccountProviderInDb.getExposure());
			} else {
				accountProviderSetInCache.add(updatedAccountProviderInDb);
			}
		}
	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh account provider cache.");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}


	private AccountProvider updateAccountProviderCache(AccountProvider accountProvider) {

		if (accountProvider == null) {
			return null;
		}
		AccountProviderCacheKey cacheKey = new AccountProviderCacheKey(accountProvider.getWebsiteType(),
			accountProvider.getUserId());

		Set<AccountProvider> accountProviders = getModifyAccountProviderSet(cacheKey);

		// TODO: 假設 provider 帳號 accountProvider 都不能重複，所以 providerId 也不會重複，也就是每個 providerId 只會有一個 accountProvider ?
		Optional<AccountProvider> optional = accountProviders.stream()
			.filter(ap -> accountProvider.getProviderId() == ap.getProviderId())
			.findFirst();

		// TODO: 為什麼沒有比較 update time ?
		if (optional.isPresent()) {
			AccountProvider accountProviderInCache = optional.get();
			accountProviderInCache.setProviderBalance(accountProvider.getProviderBalance());
			accountProviderInCache.setProviderPassword(accountProvider.getProviderPassword());
			accountProviderInCache.setProviderUpdateTime(accountProvider.getProviderUpdateTime());
			accountProviderInCache.setExposure(accountProvider.getExposure());

			return accountProviderInCache;
		} else {
			accountProviders.add(accountProvider);
			return accountProvider;
		}

	}

	public void cleanCacheFromCacheUpdateTime() {

		try {
			long startTime = System.currentTimeMillis();

			ConcurrentHashMap<AccountProviderCacheKey, Set<AccountProvider>> temp = new ConcurrentHashMap<>();

			Date now = DateTimeBuilder.localDateTime().toDate();

			for (Map.Entry<AccountProviderCacheKey, Set<AccountProvider>> entry : cache.entrySet()) {

				for (AccountProvider ap : entry.getValue()) {
					if (DateUtils.hoursBetween(ap.getProviderUpdateTime(), now) < CACHE_HOURS) {
						temp.put(entry.getKey(), entry.getValue());
						break;
					}
				}
			}

			LogUtils.providerMonitor.info(
				"cleanCacheFromCacheUpdateTime AccountProviderCache for loop takes {} sec, oldSize:{}, newSize:{} ",
				DateUtils.secondsElapsedSince(startTime), cache.mappingCount(), temp.mappingCount());

			cache = temp;

			LogUtils.providerMonitor.info(
				"cleanCache3 AccountProviderCache done takes {} sec.", DateUtils.secondsElapsedSince(startTime));

		} catch (Exception e) {
			LogUtils.providerMonitor.error(e.getMessage(), e);
		}
	}

	public Map<String, String> getUserKeysByProviderAccounts(int providerId, List<String> providerAccounts) {
		if (providerAccounts == null || providerAccounts.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<String> providerAccountSet = new HashSet<>(providerAccounts);
		Map<String, String> result = new HashMap<>();

		for (Map.Entry<AccountProviderCacheKey, Set<AccountProvider>> entry : cache.entrySet()) {
			for (AccountProvider ap : entry.getValue()) {
				if (ap.getProviderId() == providerId
					&& providerAccountSet.contains(ap.getProviderAccount())) {
					String userKey = AccountUtils.getUserKey(ap.getWebsiteType(), ap.getUserId());
					result.put(ap.getProviderAccount(), userKey);

					if (result.size() == providerAccountSet.size()) {
						return result;
					}
				}
			}
		}

		return result;
	}
}
