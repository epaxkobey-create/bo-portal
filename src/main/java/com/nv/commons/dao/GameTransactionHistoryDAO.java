package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.utils.OracleUtils;

public class GameTransactionHistoryDAO {

	public static void insertToHistory(Connection conn, List<Long> ids) throws SQLException {

		String sql =
			"INSERT /*+ APPEND */ INTO gametransactionhistory " +
				"SELECT * FROM gametransaction WHERE id IN (" + OracleUtils.getGroupCondition(ids.size()) + ") ";
		DBQueryRunner.update(conn, sql, OracleUtils.getOracleARRAY(conn, "INTEGER_ARRAY", ids.toArray()));
	}
}
