package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AffiliateDomain;
import com.nv.commons.model.database.DBQueryRunner;

public class AffiliateDomainDAO {

	public static AffiliateDomain getDefault(Connection conn, WebSiteType webSiteType) throws SQLException {

		String sql = " SELECT * FROM affiliateDomain WHERE website_type = ? and is_marketing_default = 1";

		return DBQueryRunner.getBean(conn, AffiliateDomain.class, sql, webSiteType.unique());
	}
}
