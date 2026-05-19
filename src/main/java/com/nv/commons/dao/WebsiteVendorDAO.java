package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.model.database.DBQueryRunner;

public class WebsiteVendorDAO {

	public static List<WebsiteVendor> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM websitevendor ";
		return DBQueryRunner.getBeanList(conn, WebsiteVendor.class, sql);
	}

	public static List<WebsiteVendor> getVendorByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, WebsiteVendor.class,
			"SELECT * FROM websitevendor WHERE update_time > ? ORDER BY UPDATE_TIME", updateTime);
	}

	public static int updateWebsiteVendor(Connection conn, WebsiteVendor websiteVendor) throws SQLException {
		String sql =
			"UPDATE websitevendor SET status = ?, maintenance_start = ? , maintenance_end = ?, updater = ?, update_time = SYSTIMESTAMP "
				+ " WHERE website_provider_id = ? and website_type = ? ";

		Object[] params = {
			websiteVendor.getStatus(),
			websiteVendor.getMaintenanceStart(),
			websiteVendor.getMaintenanceEnd(),
			websiteVendor.getUpdater(),
			websiteVendor.getWebsiteProviderId(),
			websiteVendor.getWebsiteType(),

		};

		return DBQueryRunner.update(conn, sql, params);

	}

}
