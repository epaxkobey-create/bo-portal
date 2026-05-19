package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dao.AccountContactInfoDAO;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Cache for AccountContactInfo with DB table AccountContactInfo
 */
public class AccountContactInfoCache extends AbstractCache {

	// <userKey, <contactType, accountContactInfo>>
	private final ConcurrentHashMap<String, Map<Integer, AccountContactInfo>> cache = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final AccountContactInfoCache instance = new AccountContactInfoCache();

	public AccountContactInfoCache() {
	}

	public static AccountContactInfoCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		cache.clear();

		try (Connection conn = DBPool.getReadConnection()) {

			List<AccountContactInfo> list = AccountContactInfoDAO.findAll(conn);

			for (AccountContactInfo contactInfo : list) {

				String userKey = AccountUtils.getUserKey(contactInfo.getWebsiteType(),
					contactInfo.getUserId());

				cache.computeIfAbsent(userKey, o -> new ConcurrentHashMap<>())
					.put(contactInfo.getContactType(), contactInfo);
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

			List<AccountContactInfo> list = AccountContactInfoDAO.findAllByUpdateTime(conn, queryTime);

			for (AccountContactInfo contactInfoInDB : list) {

				String userKey = AccountUtils.getUserKey(contactInfoInDB.getWebsiteType(),
					contactInfoInDB.getUserId());

				cache.computeIfAbsent(userKey, o -> new ConcurrentHashMap<>())
					.put(contactInfoInDB.getContactType(), contactInfoInDB);

				isUpdated = true;
				maxUpdateTime = Math.max(maxUpdateTime, contactInfoInDB.getUpdateTime().getTime());
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

	public AccountContactInfo getContactInfo(int webSiteType, String userId, int contactType) {
		return getContactInfo(AccountUtils.getUserKey(webSiteType, userId), contactType);
	}

	public AccountContactInfo getContactInfo(String userKey, int contactType) {
		Map<Integer, AccountContactInfo> contactInfoMap = cache.get(userKey);
		if (contactInfoMap == null) {
			return null;
		}
		return contactInfoMap.get(contactType);
	}
}
