package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.WebsiteSystemSetting;
import com.nv.commons.model.database.DBQueryRunner;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Neutec
 */
public class WebsiteSystemSettingDAO {


	public static WebsiteSystemSetting getEntity(final Connection conn, WebSiteType websiteType, int key, String currency)
		throws Exception {
		String sql = "SELECT * FROM websitesystemsetting WHERE website_type = ? AND key = ? ";

		List<Object> params = new ArrayList<>();
		params.add(websiteType.unique());
		params.add(key);

		if (currency != null) {
			sql += " AND currency = ? ";
			params.add(currency);
		}

		return DBQueryRunner.getBean(conn, WebsiteSystemSetting.class, sql, params);
	}

	public static List<WebsiteSystemSetting> getEntities(Connection conn) throws SQLException {
		String sql = " SELECT * FROM websitesystemsetting ";
		return DBQueryRunner.getBeanList(conn, WebsiteSystemSetting.class, sql);
	}

	public static List<WebsiteSystemSetting> getEntities(Connection conn, Timestamp updateTime) throws SQLException {
		String sql = " SELECT * FROM websitesystemsetting WHERE update_time > ?";
		return DBQueryRunner.getBeanList(conn, WebsiteSystemSetting.class, sql, updateTime);
	}

}
