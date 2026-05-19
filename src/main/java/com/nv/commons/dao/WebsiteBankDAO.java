package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.WebsiteBank;
import com.nv.commons.model.database.DBQueryRunner;

public class WebsiteBankDAO {
	
	public static List<WebsiteBank> findAll(Connection conn) throws SQLException {
		String sql = "SELECT * FROM websiteBank order by update_time";

		return DBQueryRunner.getBeanList(conn, WebsiteBank.class, sql);
	}


	public static List<WebsiteBank> findByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, WebsiteBank.class,
			"SELECT * FROM websiteBank WHERE update_time > ? order by update_time", updateTime);
	}

}
