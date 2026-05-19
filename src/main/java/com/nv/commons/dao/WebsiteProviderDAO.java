package com.nv.commons.dao;

import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.model.database.BeanConverter;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.DataBeanProcessor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsiteProviderDAO {

	public static List<WebsiteProvider> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM websiteprovider ";

		return DBQueryRunner.getBeanList(conn, WebsiteProvider.class, sql);
	}

	public static Map<Integer, WebsiteProvider> getMap(Connection conn, WebSiteType webSiteType) throws SQLException {
		String sql = "SELECT * FROM websiteProvider WHERE website_type = ?";

		Map<Integer, WebsiteProvider> resultMap = new HashMap<>();

		DataBeanProcessor<WebsiteProvider> processor = (rs, bean) -> resultMap.put(bean.getProviderId(), bean);

		DBQueryRunner.processBeanResult(conn, processor, WebsiteProvider.class, sql, webSiteType.unique());

		return resultMap;
	}

	public static List<WebsiteProvider> getProviderByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		return DBQueryRunner.getBeanList(conn, WebsiteProvider.class,
			"SELECT * FROM websiteprovider WHERE update_time > ? ORDER BY UPDATE_TIME", updateTime);
	}


	public static PageResult<WebsiteProvider> getAllProviders(Connection conn, long pageNumber, long showCount,
		String column, DBOrderType orderType, int websiteType) throws Exception {
		StringBuilder sql = new StringBuilder(
			"SELECT provider_id, display_name, status, display_order, maintenance_start, maintenance_end "
				+ "FROM websiteprovider WHERE website_type = ?");

		column = BeanConverter.getDBColumnName(WebsiteProvider.class, column);
		if (column != null) {
			if (column.equalsIgnoreCase("status")) {
				sql.append(" ORDER BY CASE status ")
					.append("WHEN 1 THEN 1 ")
					.append("WHEN 0 THEN 3 ")
					.append("WHEN -1 THEN 2 ")
					.append("END");
			} else {
				sql.append(" ORDER BY ").append(column);
			}

			if (orderType != null) {
				sql.append(orderType.getSqlString());
			}
		}

		return DBQueryRunner.getPageResult(conn, WebsiteProvider.class, sql.toString(), pageNumber, showCount,
			websiteType);
	}

	public static int updateProvider(Connection conn, WebsiteProvider provider) throws SQLException {
		String sql =
			"UPDATE websiteprovider SET status = ?, display_name = ?, display_order = ?, updater = ?, maintenance_start = ?, maintenance_end = ?, update_time = SYSTIMESTAMP "
				+ " WHERE website_type = ? AND provider_id = ?  ";

		Object[] params = {
			provider.getStatus(),
			provider.getDisplayName(),
			provider.getDisplayOrder(),
			provider.getUpdater(),
			provider.getMaintenanceStart(),
			provider.getMaintenanceEnd(),
			provider.getWebsiteType(),
			provider.getProviderId()
		};

		return DBQueryRunner.update(conn, sql, params);
	}

}
