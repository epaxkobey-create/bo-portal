package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import com.nv.commons.dao.ServerInfoDAO;
import com.nv.commons.dto.ServerInfoEntity;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Title: com.neutec.nepal.common.bo.ServerInfoBO<br>
 * Description: AP server資料存取BO
 *
 * @author Marc.Chen
 * @version 1.0
 */
public class ServerInfoBO {

	public static List<ServerInfoEntity> queryAllServerInfo() throws Exception {
		try (Connection conn = DBPool.getReadConnection()) {
			return ServerInfoDAO.findAll(conn);
		}
	}

	public static boolean isExist(Set<String> ipSet) {
		try (Connection conn = DBPool.getReadConnection()) {
			for (String ip : ipSet) {
				if (ServerInfoDAO.isExist(conn, ip)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 更新資料庫內這個IP的server狀態
	 *
	 */
	public static void updateStatusByIP(String ip, boolean isActive) throws Exception {
		updateStatusByIP(ip, -1, isActive);
	}

	/**
	 * 更新資料庫內這個IP的server狀態
	 *
	 */
	public static void updateStatusByIP(final String ip, int serverType, final boolean isActive) throws Exception {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			ServerInfoDAO.updateStatusByIP(conn, ip, serverType, isActive);
			conn.commit();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static List<ServerInfoEntity> queryServerInfoListByUpdateDate(Timestamp updateTime) throws Exception {
		try (Connection conn = DBPool.getReadConnection()) {
			return ServerInfoDAO.findByUpdateTime(conn, updateTime);
		}
	}

}
