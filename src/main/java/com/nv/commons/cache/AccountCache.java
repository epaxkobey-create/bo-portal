package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Cache for Account with DB table Account
 */
public class AccountCache extends AbstractCache {

	// userKey, account
	private final ConcurrentHashMap<String, Account> cache = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final AccountCache instance = new AccountCache();

	public AccountCache() {
	}

	public static AccountCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		cache.clear();

		try (Connection conn = DBPool.getReadConnection()) {

			List<Account> list = AccountDAO.findAll(conn);

			for (Account account : list) {

				String userKey = AccountUtils.getUserKey(account.getWebsiteType(),
					account.getUserId());

				cache.put(userKey, account);
			}

			lastUpdateTime = System.currentTimeMillis();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {

		try (Connection conn = DBPool.getReadConnection()) {

			Timestamp queryTime = new Timestamp(lastUpdateTime);

			long maxUpdateTime = queryTime.getTime();

			boolean isUpdated = false;

			List<Account> list = AccountDAO.findAllByUpdateTime(conn, queryTime);

			for (Account accountInDB : list) {

				String userKey = AccountUtils.getUserKey(accountInDB.getWebsiteType(),
					accountInDB.getUserId());

				Account accountInCache = cache.get(userKey);

				if (accountInCache == null
					|| !accountInDB.getUpdateTime().equals(accountInCache.getUpdateTime())) {

					cache.put(userKey, accountInDB);

					isUpdated = true;
				}

				maxUpdateTime = Math.max(maxUpdateTime, accountInDB.getUpdateTime().getTime());
			}

			if (isUpdated) {
				lastUpdateTime = maxUpdateTime - ERROR_VALUE;
			} else {
				lastUpdateTime = maxUpdateTime;
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public Account getAccount(int webSiteType, String userId) {
		return getAccount(AccountUtils.getUserKey(webSiteType, userId));
	}

	public Account getAccount(String userKey) {
		return cache.get(userKey);
	}
}
