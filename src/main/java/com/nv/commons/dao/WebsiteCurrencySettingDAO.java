package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.dto.WebsiteCurrencySetting;
import com.nv.commons.model.database.DBQueryRunner;

public class WebsiteCurrencySettingDAO {

	public static List<WebsiteCurrencySetting> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM WebsiteCurrencySetting ";

		return DBQueryRunner.getBeanList(conn, WebsiteCurrencySetting.class, sql);
	}







}
