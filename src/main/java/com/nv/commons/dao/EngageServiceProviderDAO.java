package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.module.engagement.dto.EngageServiceProvider;

public class EngageServiceProviderDAO {

	/**
	 *
	 */
	public static List<EngageServiceProvider> getAll(Connection conn) throws SQLException {

		final String sql = "SELECT * FROM EngageServiceProvider";

		return DBQueryRunner.getBeanList(conn, EngageServiceProvider.class, sql);
	}
}
