package com.nv.commons.bo;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.WebsiteSystemSettingDAO;
import com.nv.commons.dto.WebsiteSystemSetting;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;

public class WebsiteSystemSettingBO {

	public static WebsiteSystemSetting getEntity(WebSiteType websiteType, int key, String currency) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return WebsiteSystemSettingDAO.getEntity(conn, websiteType, key, currency);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return null;
	}
}
