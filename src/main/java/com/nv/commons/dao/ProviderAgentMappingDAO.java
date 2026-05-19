package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.ProviderAgentMapping;
import com.nv.commons.model.database.DBQueryRunner;

public class ProviderAgentMappingDAO {


	public static List<ProviderAgentMapping> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM providerAgentMapping ";

		return DBQueryRunner.getBeanList(conn, ProviderAgentMapping.class, sql);
	}

	public static List<ProviderAgentMapping> get(Connection conn, WebSiteType webSiteType) throws SQLException {
		String sql = "SELECT * FROM providerAgentMapping WHERE website_type = ?";

		return DBQueryRunner.getBeanList(conn, ProviderAgentMapping.class, sql, webSiteType.unique());
	}

}
