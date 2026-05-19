package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dao.AccountContactInfoVerificationDAO;
import com.nv.commons.dto.AccountContactInfoVerification;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Cache for AccountContactInfoVerification with DB table AccountContactInfoVerification
 */
public class AccountContactInfoVerificationCache extends AbstractCache {

	// <key (webSiteType_userId_contactType_content), <verifyCode, accountContactInfoVerification>>
	private final ConcurrentHashMap<String, Map<String, AccountContactInfoVerification>> cache = new ConcurrentHashMap<>();
	// <verifyCode, accountContactInfoVerification>
	private final ConcurrentHashMap<String, AccountContactInfoVerification> cacheByVerifyCode = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final AccountContactInfoVerificationCache instance = new AccountContactInfoVerificationCache();

	public AccountContactInfoVerificationCache() {
	}

	public static AccountContactInfoVerificationCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		cache.clear();

		try (Connection conn = DBPool.getReadConnection()) {

			List<AccountContactInfoVerification> list = AccountContactInfoVerificationDAO.findAll(conn);

			for (AccountContactInfoVerification verification : list) {

				cache.computeIfAbsent(getCacheKey(verification), k -> new ConcurrentHashMap<>())
					.put(verification.getVerifyCode(), verification);

				cacheByVerifyCode.put(verification.getVerifyCode(), verification);
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

			List<AccountContactInfoVerification> list = AccountContactInfoVerificationDAO.findAllByUpdateTime(conn, queryTime);

			for (AccountContactInfoVerification verificationInDB : list) {

				cache.computeIfAbsent(getCacheKey(verificationInDB), k -> new ConcurrentHashMap<>())
					.put(verificationInDB.getVerifyCode(), verificationInDB);

				cacheByVerifyCode.put(verificationInDB.getVerifyCode(), verificationInDB);

				isUpdated = true;
				maxUpdateTime = Math.max(maxUpdateTime, verificationInDB.getUpdateTime().getTime());
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

	private String getCacheKey(int webSiteType, String userId, int contactType, String content) {
		return webSiteType + "_" + userId + "_" + contactType + "_" + content;
	}

	private String getCacheKey(AccountContactInfoVerification verification) {
		return getCacheKey(verification.getWebsiteType(), verification.getUserId(),
			verification.getContactType(), verification.getContent());
	}

	public List<AccountContactInfoVerification> getContactInfoVerificationList(int webSiteType, String userId,
		int contactType, String content) {
		String cacheKey = getCacheKey(webSiteType, userId, contactType, content);
		Map<String, AccountContactInfoVerification> verificationMap = cache.get(cacheKey);
		return verificationMap != null ? new ArrayList<>(verificationMap.values()) : new ArrayList<>();
	}

	public AccountContactInfoVerification getContactInfoVerification(String verifyCode) {
		return cacheByVerifyCode.get(verifyCode);
	}
}
