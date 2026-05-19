package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.module.engagement.dto.EngageServiceAccount;

public class EngageServiceAccountDAO {

	/**
	 * TODO: implement get by update time
	 */
	public static List<EngageServiceAccount> getAll(Connection conn) throws SQLException {

		final String sql = "SELECT * FROM EngageServiceAccount";

		return DBQueryRunner.getBeanList(conn, EngageServiceAccount.class, sql);
	}
}
