package com.nv.commons.model.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import com.nv.commons.constants.EnvironmentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.system.Setting;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JUnitUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Title: com.nv.nepal.common.utils.db.DBPool<br>
 * Description: DB connection pool工具
 *
 */
public enum DBPool {

	PRIMARY("db.json") {};
	//	STANDBY("dbStandby.properties") {};

	private PoolManager poolManager;

	DBPool(String filename) {
		try {
			// for test case
			if ("junit".equalsIgnoreCase(System.getProperty(SystemConstants.RUNTIME_ENV))) {
				poolManager = JUnitUtils.getMockPoolManager();
				return;
			}
			if ("uat".equalsIgnoreCase(System.getProperty(SystemConstants.RUNTIME_ENV))) {
				filename = "db-uat.json";
			}

			EnvironmentType environmentType = SystemInfo.getInstance().getEnvironmentType();
			String filePath = String.join(File.separator, environmentType.getPropertiesFolder(), filename);

			LogUtils.SYS.debug("DBPool-" + this.name() + ", filePath = " + filePath+", environmentType=" + environmentType.name()+ ", RUNTIME_ENV="+System.getProperty(SystemConstants.RUNTIME_ENV));

			PoolManager poolManager = new OraclePoolManager(this.name(), filePath);

			int i = 0;
			// 等10分鐘
			for (; i < 600; i++) {
				// 至少有一個存在
				if (poolManager.getAvailableNodesSize() > 0) {
					break;
				}
				if (i != 0 && i % 60 == 0) {
					LogUtils.SYS.error("DBPool wait available nodes ");
				}
				Thread.sleep(1000L);
			}
			if (i == 600) {
				throw new Exception("Exception : no available nodes ");
			}
			poolManager.checkDBTime();
			this.poolManager = poolManager;

		} catch (Exception ex) {
			LogUtils.SYS.error("DBPool-" + this.name() + "," + ex.getMessage(), ex);
		}
	}

	public static void closeAllDataSource() {
		for (DBPool dbPool : DBPool.values()) {
			dbPool.poolManager.closeDataSource();
		}
	}

	public static Connection getReadConnection() throws SQLException {
		Connection conn = getPoolManager().get(-1);
		DbUtils.setRemoveAbandonedTimeout(conn, Setting.REMOVE_ABANDONED_TIMEOUT_READ);
		return conn;
	}

	public static Connection getReadConnection(int timeout) throws SQLException {
		Connection conn = getPoolManager().get(-1);
		// 最低的秒數，不可低於Setting.REMOVE_ABANDONED_TIMEOUT_READ指定的秒數
		DbUtils.setRemoveAbandonedTimeout(conn, Long.max(Setting.REMOVE_ABANDONED_TIMEOUT_READ, timeout));
		return conn;
	}

	public static Connection getWriteConnection() throws SQLException {
		Connection conn = getPoolManager().get(0);
		DbUtils.setRemoveAbandonedTimeout(conn, Setting.REMOVE_ABANDONED_TIMEOUT_WRITE);
		return conn;
	}

	public static Connection getWriteConnection(int timeout) throws SQLException {
		Connection conn = getPoolManager().get(0);
		// 最低的秒數，不可低於Setting.REMOVE_ABANDONED_TIMEOUT_WRITE指定的秒數
		DbUtils.setRemoveAbandonedTimeout(conn, Long.max(Setting.REMOVE_ABANDONED_TIMEOUT_WRITE, timeout));
		return conn;
	}

	public static Connection getStandbyReadConnection() throws SQLException {
		// 目前沒有stand by
		return getReadConnection();

		//		Connection conn = STANDBY.poolManager.get();
		//		DbUtils.setRemoveAbandonedTimeout(conn, Setting.REMOVE_ABANDONED_TIMEOUT_READ);
		//		return conn;
	}


	private static PoolManager getPoolManager() {
		//		return Setting.SWITCHOVER_TO_STANDBY ? STANDBY.poolManager : PRIMARY.poolManager;
		return PRIMARY.poolManager;
	}

}
