package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.constants.ManagerStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Manager;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.system.SystemInfo;

public class ManagerDAO {



	public static int changePassword(Connection conn, Manager manager, WebSiteType webSiteType) throws SQLException {
		String sql = "UPDATE manager SET password = ?, updater = ?, update_time = SYSTIMESTAMP WHERE user_id = ? AND website_type = ? ";
		return DBQueryRunner.update(conn, sql, manager.getPassword(), manager.getUpdater(), manager.getUserId(), webSiteType.unique());
	}


	public static Manager findManagerByUserID(Connection conn, WebSiteType webSiteType, String userID) throws SQLException {
		String sql = "SELECT * FROM manager WHERE user_id = ? AND website_type = ? ";
		return DBQueryRunner.getBean(conn, Manager.class, sql, userID, webSiteType.unique());
	}
	
	public static int updateLastLogin(Connection conn, Manager manager) throws SQLException {
		return DBQueryRunner.update(conn, "UPDATE manager SET session_id = ?, server_id = ?, login_time = ? WHERE user_id = ? AND website_type = ? ", 
			manager.getSessionID(), manager.getServerID(), manager.getLoginTime(), manager.getUserId(), manager.getWebsiteType());
	}
	

	public static List<Manager> findAllManagerByServerID(Connection conn, String serverID) throws SQLException {
		String sql = "SELECT * FROM manager WHERE server_id = ? AND status = ? ";
		
		return DBQueryRunner.getBeanList(conn, Manager.class, sql, serverID, ManagerStatusType.ACTIVE.unique());
	}
	
	public static int logout(Connection conn, Manager pManager, String sessionId, String serverId) throws SQLException {
		String sql = "UPDATE manager SET session_id = null, server_id = null, update_time = SYSTIMESTAMP, updater = ? WHERE user_id = ? and session_id = ? AND server_id = ? AND website_type = ? ";
		return DBQueryRunner.update(conn, sql, pManager.getUserId(), pManager.getUserId(), sessionId, serverId, pManager.getWebsiteType());
	}


	public static int updateServerID(final Connection conn, int webSiteType, String userId, String serverId) throws SQLException {
		String sql = "UPDATE manager SET update_time = SYSTIMESTAMP, SERVER_ID = ? WHERE  website_type = ? AND user_id = ?";
		return DBQueryRunner.update(conn, sql, serverId, webSiteType, userId);
	}


	public static int clearSessionIdAndServerId(Connection conn) throws SQLException {
		String sql = "UPDATE manager SET update_time = SYSTIMESTAMP, session_id = null, server_id = null WHERE server_id = ?";
		return DBQueryRunner.update(conn, sql, SystemInfo.getInstance().getServerID());
	}

}
