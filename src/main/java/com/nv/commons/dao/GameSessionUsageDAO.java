package com.nv.commons.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.model.database.DBQueryRunner;

public class GameSessionUsageDAO {

	public static GameSessionUsage getGameSessionUsage(Connection conn, String userKey) throws SQLException {
		String sql = "Select * from gameSessionUsage where user_key=? ";
		return DBQueryRunner.getBean(conn, GameSessionUsage.class, sql, userKey);
	}

	public static int insertNewGameSessionUsage(Connection conn, String userKey, BigDecimal limitSnapShot,
		int sessionStatus, Timestamp periodStartTime)
		throws SQLException {

		String sql;

		if (periodStartTime != null) {
			sql = "insert into gameSessionUsage (user_key, limit_snapshot, last_active_time, create_date, "
				+ "version, usage, session_status, period_start_time) values (?, ?, SYSTIMESTAMP, SYSTIMESTAMP, 1, 0, ?, ?)";
			return DBQueryRunner.update(conn, sql, userKey, limitSnapShot, sessionStatus, periodStartTime);
		} else {
			sql = "insert into gameSessionUsage (user_key, limit_snapshot, last_active_time, create_date, "
				+ "version, usage, session_status, period_start_time) values (?, ?, SYSTIMESTAMP, SYSTIMESTAMP, 1, 0, ?, SYSTIMESTAMP)";
			return DBQueryRunner.update(conn, sql, userKey, limitSnapShot, sessionStatus);
		}
	}

	public static int updateGameSessionUsage(Connection conn, String userKey, BigDecimal currentSessionUsedUsage,
		BigDecimal originalVersion) throws SQLException {
		String sql = "update gameSessionUsage set usage = ?, version = version + 1, last_active_time = SYSTIMESTAMP ,update_date = SYSTIMESTAMP, session_status = ? where user_key=? and version = ?";
		return DBQueryRunner.update(conn, sql, currentSessionUsedUsage, BinaryStatusType.ACTIVE.unique(), userKey,
			originalVersion);
	}

	public static int updateLimitSnapShotAndUsage(Connection conn, String userKey, BigDecimal limitSnapShot,
		BigDecimal usage, BigDecimal version, Timestamp periodStartTime)
		throws SQLException {

		String sql;
		if (periodStartTime != null) {
			sql = "update gameSessionUsage set limit_snapshot = ?, usage = ?, version = version +1, update_date = SYSTIMESTAMP, period_start_time = ? where user_key=? AND version = ?";
			return DBQueryRunner.update(conn, sql, limitSnapShot, usage, periodStartTime, userKey, version);
		}else{
			 sql = "update gameSessionUsage set limit_snapshot = ?, usage = ?, version = version +1, update_date = SYSTIMESTAMP where user_key=? AND version = ?";
			return DBQueryRunner.update(conn, sql, limitSnapShot, usage, userKey, version);
		}
	}

	public static int updateActiveTimeAndStatus(Connection conn, String userKey, BigDecimal version)
		throws SQLException {
		String sql = "Update gameSessionUsage set last_active_time  = SYSTIMESTAMP, update_date = SYSTIMESTAMP, version = version +1, session_status = ? where user_key = ? and version = ? ";
		return DBQueryRunner.update(conn, sql, BinaryStatusType.ACTIVE.unique(), userKey, version);
	}

	public static int endGameSession(Connection conn, String userKey, BigDecimal totalUsage, BigDecimal version)
		throws SQLException {
		String sql = "update gameSessionUsage SET usage = ?, session_status = ?, version = version + 1, last_active_time = SYSTIMESTAMP, update_date = SYSTIMESTAMP where user_key=? and version = ? and session_status = ?";

		return DBQueryRunner.update(conn, sql, totalUsage, BinaryStatusType.INACTIVE.unique(), userKey, version,
			BinaryStatusType.ACTIVE.unique());

	}

	public static List<GameSessionUsage> getAllActiveGameSessionUsage(Connection conn) throws SQLException {
		String sql = "Select * from gameSessionUsage where session_status = ?";
		return DBQueryRunner.getBeanList(conn, GameSessionUsage.class, sql, BinaryStatusType.ACTIVE.unique());
	}

	public static int resetUsageAndUpdateLimit(Connection conn, String userKey, BigDecimal newLimitSnapShot,
		BigDecimal version) throws SQLException {
		String sql = "Update gameSessionUsage set usage = 0, limit_snapshot = ?,  version =  version + 1, update_date = SYSTIMESTAMP, period_start_time = SYSTIMESTAMP where user_key = ? and version = ? ";
		return DBQueryRunner.update(conn, sql, newLimitSnapShot, userKey, version);
	}

	public static List<GameSessionUsage> getAllGameSessionUsage(Connection conn) throws SQLException {
		String sql = "Select * from gameSessionUsage";
		return DBQueryRunner.getBeanList(conn, GameSessionUsage.class, sql);
	}

	public static List<GameSessionUsage> getExpiredGameSessionUsage(Connection conn, Timestamp cutoff)
		throws SQLException {
		String sql = "Select * from gameSessionUsage where period_start_time is not null and period_start_time < ?";
		return DBQueryRunner.getBeanList(conn, GameSessionUsage.class, sql, cutoff);
	}

	public static int batchEndGameSessions(Connection conn,
		Map<String, BigDecimal> userUsageMap) throws SQLException {

		String sql = "UPDATE gameSessionUsage " +
			"SET usage = ?, " +
			"    session_status = ?, " +
			"    last_active_time = SYSTIMESTAMP, " +
			"    update_date = SYSTIMESTAMP, " +
			"    version = version + 1 " +
			"WHERE user_key = ? " +
			"AND session_status = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (Map.Entry<String, BigDecimal> entry : userUsageMap.entrySet()) {
				ps.setBigDecimal(1, entry.getValue());                          // usage
				ps.setInt(2, BinaryStatusType.INACTIVE.unique());              // session_status = INACTIVE
				ps.setString(3, entry.getKey());                               // user_key
				ps.setInt(4, BinaryStatusType.ACTIVE.unique());                // WHERE session_status = ACTIVE
				ps.addBatch();
			}
			int[] results = ps.executeBatch();
			return Arrays.stream(results).sum();
		}
	}

	public static List<GameSessionUsage> getGameSessionUsageByUserKeys(Connection conn,
		List<String> userKeys) throws SQLException {
		String placeholders = userKeys.stream().map(k -> "?").collect(Collectors.joining(", "));
		String sql = "SELECT * FROM gameSessionUsage WHERE user_key IN (" + placeholders + ") AND session_status = ?";
		Object[] params = new Object[userKeys.size() + 1];
		for (int i = 0; i < userKeys.size(); i++) {
			params[i] = userKeys.get(i);
		}
		params[userKeys.size()] = BinaryStatusType.ACTIVE.unique();
		return DBQueryRunner.getBeanList(conn, GameSessionUsage.class, sql, params);
	}

}
