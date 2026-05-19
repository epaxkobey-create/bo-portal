package com.nv.commons.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ProviderAgentDAO;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.ProviderAgent;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 紀錄遊戲商所提供的agent資料
 */
public class ProviderAgentCache extends AbstractCache {

	private final Map<Integer, ProviderAgent> cache = new ConcurrentHashMap<>();

	private static final ProviderAgentCache instance = new ProviderAgentCache();

	private Timestamp lastUpdateTime = new Timestamp(0);
	// 誤差值
	private static final long ERROR_VALUE = 1000;

	public static ProviderAgentCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {
			ProviderAgentDAO.getAll(conn)
				.forEach(providerAgent -> cache.put(providerAgent.getId(), providerAgent));

		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public ProviderAgent get(int providerAgentId) {
		return cache.get(providerAgentId);
	}


	private Stream<ProviderAgent> getAsStream(WebSiteType websiteType, CurrencyType currencyType) {

		return ProviderAgentMappingCache.getInstance().getAsStream(websiteType)
			.map(providerAgentMapping -> get(providerAgentMapping.getProviderAgentId()))
			.filter(providerAgent -> Arrays.stream(providerAgent.getCurrencyTypes()).anyMatch(c -> c == currencyType));
	}

	// 找出這個網站在這個貨幣下，有設定哪些遊戲商agent
	public List<ProviderAgent> getList(WebSiteType websiteType, CurrencyType currencyType) {

		return getAsStream(websiteType, currencyType).toList();
	}

	public Optional<ProviderAgent> findFirst(WebSiteType websiteType, Provider provider, CurrencyType currencyType) {

		return getAsStream(websiteType, currencyType)
			.filter(providerAgent -> providerAgent.getProviderId() == provider.getId())
			.findFirst();
	}

	// key = websiteType_unique + "_" + currencyType_unique + "_" + providerId
	// 快取三秒，如果超過時間會呼叫 load 方法重新查詢
	private final LoadingCache<String, Boolean> hasMatchLoadingCache = CacheBuilder.newBuilder()
		.expireAfterWrite(3000, TimeUnit.MILLISECONDS)
		.build(new CacheLoader<>() {

			@NotNull
			@Override
			public Boolean load(
				@NotNull
				String keyStr) {
				try {
					String[] keys = keyStr.split("_");

					WebSiteType _websiteType = WebSiteType.getInstance(Integer.parseInt(keys[0]));
					CurrencyType _currencyType = CurrencyType.getInstance(Integer.parseInt(keys[1]));
					int _providerId = Integer.parseInt(keys[2]);

					return ProviderAgentCache.getInstance().getAsStream(_websiteType, _currencyType)
						.anyMatch(providerAgent -> providerAgent.getProviderId() == _providerId);

				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
				return false;
			}
		});

	public boolean has(WebSiteType websiteType, Provider provider, CurrencyType currencyType) {
		try {
			String key = websiteType.unique() + "_" + currencyType.unique() + "_" + provider.getId();

			return hasMatchLoadingCache.get(key);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return false;
	}


	@Override
	public void update() {
		try (Connection conn = DBPool.getReadConnection()) {
			boolean isUpdated = false;
			boolean isConnectionInfoClear = false;
			Timestamp maxUpdatedTime = Timestamp.from(lastUpdateTime.toInstant());

			List<ProviderAgent> providerAgentList = ProviderAgentDAO.get(conn, maxUpdatedTime);

			for (ProviderAgent providerAgentInDB : providerAgentList) {
				maxUpdatedTime = DateUtils.max(maxUpdatedTime, providerAgentInDB.getUpdateTime());

				ProviderAgent providerAgentInCache = cache.get(providerAgentInDB.getId());

				if (providerAgentInCache == null) {
					cache.put(providerAgentInDB.getId(), providerAgentInDB);
					isUpdated = true;
					isConnectionInfoClear = true;
					continue;
				}

				if (providerAgentInDB.getUpdateTime().compareTo(providerAgentInCache.getUpdateTime()) > 0) {
					cache.put(providerAgentInCache.getId(), providerAgentInDB);
					isUpdated = true;

					isConnectionInfoClear = isConnectionInfoClear ||
						!providerAgentInDB.getAgentInfo().equals(providerAgentInCache.getAgentInfo());
				}
			}

			if (isUpdated) {
				if (isConnectionInfoClear) {
					ProviderProxyCache.getInstance().clearConnectionInfo();
				}

				lastUpdateTime = new Timestamp(maxUpdatedTime.getTime() - ERROR_VALUE);
				update();
			} else {
				lastUpdateTime = maxUpdatedTime;
			}

		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		this.init();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache);
	}
}
