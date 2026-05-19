package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.model.database.DBQueryRunner;

public class WebsiteInfoDAO {

	public static List<WebsiteInfo> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM websiteInfo ";
		return DBQueryRunner.getBeanList(conn, WebsiteInfo.class, sql);
	}

	public static List<WebsiteInfo> get(Connection conn, Timestamp updateTime) throws SQLException {
		String sql = " SELECT * FROM websiteInfo WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, WebsiteInfo.class, sql, updateTime);
	}
}
