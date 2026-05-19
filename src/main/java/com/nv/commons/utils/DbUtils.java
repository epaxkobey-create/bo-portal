package com.nv.commons.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.nv.commons.model.database.ConnProcessor;
import com.nv.commons.model.database.DBPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PooledConnection;

public class DbUtils {

	private DbUtils() {
		throw new AssertionError();
	}

	public static void closeAll(Connection conn, Statement stmt, Statement ps) {
		close(ps);
		close(stmt);
		close(conn);
	}

	public static void closeAll(Connection conn, Statement ps) {
		close(ps);
		close(conn);
	}

	/**
	 * Utility method to close up all the stuff. It eases the syntax
	 *
	 * @param conn
	 * @param stmt
	 * @param rs
	 */
	public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
		close(rs);
		close(stmt);
		close(conn);
	}

	public static void closeAll(Statement stmt, Statement ps) {
		close(ps);
		close(stmt);
	}

	public static void closeAll(Statement stmt, ResultSet rs) {
		close(rs);
		close(stmt);
	}

	/**
	 * Close connection
	 *
	 * @param conn
	 */
	public static void close(Connection conn) {
		if (conn == null) {
			return;
		}
		try {
			conn.clearWarnings();
			conn.close();
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	/**
	 * Close connection
	 *
	 * @param pConn
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static void close(Connection... pConn) {
		if (null == pConn || pConn.length == 0) {
			return;
		}
		for (Connection conn : pConn) {
			if (conn == null) {
				continue;
			}
			try {
				conn.clearWarnings();
				conn.close();
			} catch (SQLException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Close statement
	 *
	 * @param stmt
	 */
	public static void close(Statement stmt) {
		if (stmt == null) {
			return;
		}
		try {
			stmt.close();
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	/**
	 * Close statement
	 *
	 * @param pStmt
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static void close(Statement... pStmt) {
		if (null == pStmt || pStmt.length == 0) {
			return;
		}
		for (Statement stmt : pStmt) {
			if (stmt == null) {
				continue;
			}
			try {
				stmt.close();
			} catch (SQLException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Close resultset
	 *
	 * @param rs
	 */
	public static void close(ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	/**
	 * Close resultset
	 *
	 * @param pRs
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static void close(ResultSet... pRs) {
		if (null == pRs || pRs.length == 0) {
			return;
		}
		for (ResultSet rs : pRs) {
			if (rs == null) {
				continue;
			}
			try {
				rs.close();
			} catch (SQLException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Rollback connection
	 *
	 * @param conn
	 */
	public static void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				LogUtils.SYS.error(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Rollback connection
	 *
	 * @param pConn
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static void rollback(Connection... pConn) {
		if (null == pConn || pConn.length == 0) {
			return;
		}
		for (Connection conn : pConn) {
			if (conn == null) {
				continue;
			}
			try {
				conn.rollback();
			} catch (SQLException ex) {
				LogUtils.SYS.error(ex.getMessage(), ex);
			}
		}
	}

	public static boolean isLockedException(Exception e) {
		return StringUtils.isNotEmpty(e.getMessage()) && e.getMessage().contains("ORA-00054:");
	}

	public static boolean isUniqueConstraintException(Exception e) {
		return StringUtils.isNotEmpty(e.getMessage()) && e.getMessage().contains("ORA-00001:");
	}

	//Tomcat JDBC Connection Pool的ResetAbandonedTimer是透過setTimestamp修改
	//這裡使用setRemoveAbandonedTimeout，而實作setTimestamp的定義
	public static void setRemoveAbandonedTimeout(Connection conn, long maxTimeout) throws SQLException {
		if (!(conn instanceof PooledConnection)) {
			return;
		}
		PooledConnection pcon = conn.unwrap(PooledConnection.class);
		pcon.setTimestamp(System.currentTimeMillis() + maxTimeout * 1000 - pcon.getAbandonTimeout());
	}

}
