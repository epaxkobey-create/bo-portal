package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.dto.WebsiteCountrySetting;
import com.nv.commons.model.database.DBQueryRunner;

public class WebsiteCountrySettingDAO {

	public static List<WebsiteCountrySetting> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM WebsiteCountrySetting ";

		return DBQueryRunner.getBeanList(conn, WebsiteCountrySetting.class, sql);
	}




}
