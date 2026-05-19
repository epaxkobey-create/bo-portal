package com.nv.commons.model.database;

import java.sql.SQLException;

import com.nv.commons.utils.LogUtils;
import oracle.jdbc.OracleConnection;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

// 這個的使用設定在db.json
public class RollbackInterceptor extends JdbcInterceptor {

	@Override
	public void reset(ConnectionPool parent, PooledConnection con) {
		return;
	}

	@Override
	public void disconnected(ConnectionPool parent, PooledConnection con, boolean finalizing) {
		// if its oracle make sure we rollback here before disconnect just in
		// case a running TX is open

		final String message =
			"Connection {" + con + "} with Auto-Commit false is going to be closed. Doing an explicit Rollback here!";

		autoRollback(con, message);

		super.disconnected(parent, con, finalizing);
	}

	//	private void autoRollback(PooledConnection con) {
	//
	//		autoRollback(con, "");
	//	}

	private void autoRollback(PooledConnection con, String message) {
		try {
			if (con.getConnection().isWrapperFor(OracleConnection.class) &&
				!con.getConnection().getAutoCommit()) {

				if (message != null && message.length() > 0) {
					LogUtils.SYS.warn(message);
				}

				try {
					con.getConnection().rollback();

				} catch (SQLException e) {
					LogUtils.SYS.error(
						"Failed to rollback connection {" + con + "} before closing it." + e.getMessage());
				}
			}
		} catch (SQLException e) {
			LogUtils.SYS.error("Failed to check auto commit of connection {" + con + "}" + e.getMessage());
		}
	}
}
