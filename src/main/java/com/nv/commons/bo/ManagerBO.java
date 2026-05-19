package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.nv.commons.cache.ManagerCache;
import com.nv.commons.constants.ManagerStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ManagerDAO;
import com.nv.commons.dto.Manager;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class ManagerBO {

	public static Manager queryManagerByManagerID(WebSiteType webSiteType, String userID) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return ManagerDAO.findManagerByUserID(conn, webSiteType, userID);
		} catch (Exception e) {
			LogUtils.SYS.error("can't find manager:{}", userID, e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static Manager managerAuthenticate(WebSiteType webSiteType, String userName, String password,
		String sessionId, boolean fromInformalDomain) throws Exception {

		Connection conn = null;
		Manager manager;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			manager = ManagerDAO.findManagerByUserID(conn, webSiteType, userName);
			if (null == manager) {
				throw new Deviation();
			}
			if (ManagerStatusType.INACTIVE == ManagerStatusType.getInstanceOf(manager.getStatus())) {
				throw new Deviation();
			}
			if (!password.equals(manager.getPassword())) {
				throw new Deviation();
			}
			if ((fromInformalDomain && !manager.isVirtual()) || (!fromInformalDomain && manager.isVirtual())) {
				throw new Deviation();
			}

			manager.setServerID(SystemInfo.getInstance().getServerID());
			manager.setSessionID(sessionId);
			manager.setLoginTime(new Timestamp(System.currentTimeMillis())); // for notification

			ManagerDAO.updateLastLogin(conn, manager);
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}

		return manager;
	}


	public static int logout(Manager pManager) throws SQLException {

		WebSiteType webSiteType = pManager.getWebsiteTypeObj();
		Manager manager = ManagerCache.getInstance().getManager(webSiteType, pManager.getUserId());

		if (manager != null && manager.getSessionID().equals(pManager.getSessionID())) {
			ManagerCache.getInstance().remove(webSiteType, pManager.getUserId());
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);

				int result = ManagerDAO.logout(conn, pManager, pManager.getSessionID(), pManager.getServerID());
				conn.commit();
				return result;
			} catch (Exception e) {
				DbUtils.rollback(conn);
				throw e;
			} finally {
				DbUtils.close(conn);
			}
		}
		return 0;
	}


	public static void changeManagerPassword(Manager updateManager, Manager originalManager, String loginIP,
		WebSiteType webSiteType) throws SQLException {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			ManagerDAO.changePassword(conn, updateManager, webSiteType);
			conn.commit();

			LogUtils.SYS.info(
				"[ update manager password ] operate webSiteType: {} userID:{} from {}, password old {} new {}",
				webSiteType, originalManager.getUserId(), loginIP, originalManager.getPassword(),
				updateManager.getPassword());

		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static void updateServerID(String systemServerID, int websiteType, String userID)
		throws SQLException {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int updateCount = ManagerDAO.updateServerID(conn, websiteType, userID, systemServerID);
			conn.commit();

			LogUtils.SYS.debug("manager {} serverId: {}, updateCount: {}", userID, systemServerID, updateCount);

		} catch (SQLException e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}


}
