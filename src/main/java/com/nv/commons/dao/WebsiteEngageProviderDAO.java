package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.module.engagement.dto.WebsiteEngageProvider;

public class WebsiteEngageProviderDAO {

	/**
	 *
	 */
	public static List<WebsiteEngageProvider> getAll(Connection conn) throws SQLException {

		final String sql = "SELECT * FROM WebsiteEngageProvider";

		return DBQueryRunner.getBeanList(conn, WebsiteEngageProvider.class, sql);
	}

}
