package com.nv.commons.dao;

import com.nv.commons.constants.DBQueryType;
import com.nv.commons.dto.SystemSetting;
import com.nv.commons.model.database.DBQueryRunner;

import java.sql.Connection;
import java.util.List;

public class SystemSettingDAO {

	public static List<SystemSetting> findAll(final Connection conn) throws Exception {
		String sql = " SELECT key, value, remark, image, update_time FROM systemsetting  ";
		return DBQueryRunner.getBeanList(conn, SystemSetting.class, sql);
	}

	public static SystemSetting findByKey(final Connection conn, String key, DBQueryType dbQueryType) throws Exception {
		String sql = "SELECT key, value, remark, image, update_time FROM systemsetting WHERE key = ? ";
		if (dbQueryType != null) {
			sql += dbQueryType.getSqlString();
		}
		return DBQueryRunner.getBean(conn, SystemSetting.class, sql, key);
	}

}
