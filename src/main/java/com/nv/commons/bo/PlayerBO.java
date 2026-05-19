package com.nv.commons.bo;

import com.nv.commons.cache.AccountCache;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.module.backendapi.cache.PlayerLocalCache;

import java.sql.Connection;
import java.sql.Timestamp;

public class PlayerBO {

	/*
	 *
	 */
//	@Deprecated
//	public static void setupAccountFieldsAfterLogin(Account account, String loginIp, HttpServletRequest request)
//		throws Deviation {
//		// MEMO: for proxy test
//		if (request == null) {
//			return;
//		}
//
//		/*
//		 * for backend api (server to server), request session is null.
//		 * so just use empty sessionId.
//		 */
//		final String sessionId = (request.getSession(false) == null)
//			? ""
//			: request.getSession(false).getId();
//
//		/*
//		 */
//		account.setServerId(SystemInfo.getInstance().getServerID());
//		account.setSessionId(sessionId);
//		account.setLoginIp(loginIp);
//
//		// MEMO: need deviceType for updateLastLogin
//		// "0=PC, 1=Mobile"
//		account.setDeviceType(DeviceType.getInstance(request).unique());
//		account.setPlatformType(PlatformType.getInstance(request).unique());
//
//		String userId = account.getUserId();
//		WebSiteType webSiteType = WebSiteType.getInstance(account.getWebsiteType());
//
//		/*
//		 */
//		Connection conn = null;
//		try {
//			conn = DBPool.getReadConnection();
//			//TODO 替換舊機制
//			account.setAccountContactInfoList(
//				AccountContactInfoDAO.findAccountContactDataByUserId(conn, userId, webSiteType));
//
//			// pending deposit count
//			account
//				.setDepositPendingCount(
//					new AtomicInteger(MoneyTransactionDAO.findDepositPendingCountByUserId(conn, userId, webSiteType)));
//			// pending withdrawal count
//			account.setWithdrawalPendingCount(
//				new AtomicInteger(MoneyTransactionDAO.findWithdrawalPendingCountByUserId(conn, userId, webSiteType)));
//			// pending withdrawal
//			//			account
//			//				.setWithdrawalPendingList(MoneyTransactionDAO.getPendingWithdrawalByUserId(conn, userId, webSiteType));
//
//		} catch (Deviation e) {
//			throw e;
//		} catch (Exception e) {
//			LogUtils.SYS.error(e.getMessage(), e);
//			throw new Deviation("msg.error.account.loginFailed");
//		} finally {
//			DbUtils.close(conn);
//		}
//	}

	public static String loginFailureThenLock(Account account) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			//超過錯誤次數,lock
			AccountDAO.updateLoginFailureAndLock(conn, account.getUserId(), account.getWebsiteType());
			conn.commit();

			AccountCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			DbUtils.close(conn);
		}
		return "msg.error.account.loginTimesOver";
	}

	public static String addLoginFailure(Account account) {

		Connection conn = null;
		//未超過錯誤次數,僅加錯誤次數
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			AccountDAO.updateLoginFailure(conn, account.getUserId(), account.getWebsiteType());
			conn.commit();

			AccountCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback();
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		//密碼錯誤
		return "msg.error.account.loginFail";
	}

	// TODO: select SYSTIMESTAMP from dual > lastLoginTime
	public static void updateLastLogin(final Account account) throws Exception {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountDAO.updateLastLogin(conn, account.getUserId(), account.getWebsiteType(),
				account.getLoginIp(), account.getSessionId(), account.getServerId(), account.getDeviceType(),
				account.getPlatformType());

			conn.commit();

			// TODO: no need select table
			Timestamp loginTime = AccountDAO.getLastLoginTime(conn, account.getWebsiteType(), account.getUserId());
			account.setLoginTime(loginTime);
			account.setUpdateTime(loginTime);

			AccountCache.getInstance().update();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static void logoutByBO(final String userKey) {
		try {
			String[] strings = AccountUtils.parseUserKeyEnhancement(userKey);

			WebSiteType webSiteType = WebSiteType.getInstance(Integer.parseInt(strings[0]));

			clearSessionId(webSiteType.unique(), strings[1], null);

		} catch (Exception e) {
			LogUtils.backendApi.error(e.getMessage(), e);
		}
	}

	public static void logoutBea(final Account accountInCache) {
		try {
			if (accountInCache == null) {
				return;
			}

			final String userKey = accountInCache.getUserKey();
			String[] strings = AccountUtils.parseUserKeyEnhancement(userKey);

			WebSiteType webSiteType = WebSiteType.getInstance(Integer.parseInt(strings[0]));

			clearSessionId(webSiteType.unique(), strings[1], accountInCache.getSessionId());

			PlayerLocalCache.getInstance().remove(userKey, accountInCache.getSessionId());

		} catch (Exception e) {
			LogUtils.backendApi.error(e.getMessage(), e);
		}
	}

	private static boolean clearSessionId(int websiteType, String userId, String sessionId) {

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int updateCount;

			if (sessionId == null) {
				updateCount = AccountDAO.clearSessionAndServerId(conn, userId, websiteType);

			} else {
				String serverId = SystemInfo.getInstance().getServerID();

				updateCount = AccountDAO.clearSessionAndServerId(conn, userId, websiteType,
					sessionId, serverId);
			}

			conn.commit();

			AccountCache.getInstance().update();

			return updateCount > 0;

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		return false;
	}


}
