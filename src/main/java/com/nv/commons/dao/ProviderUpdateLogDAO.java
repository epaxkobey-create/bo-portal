package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.nv.commons.constants.DBOrderType;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.ProviderUpdateLog;
import com.nv.commons.model.database.BeanConverter;
import com.nv.commons.model.database.DBQueryRunner;

public class ProviderUpdateLogDAO {


	public static int[] insertAll(Connection conn, List<ProviderUpdateLog> providerUpdateLogList) throws SQLException {
		String sql = "INSERT INTO PROVIDERUPDATELOG"
			+ "(id, PROVIDER_ID, website_type, log_type, records, updater, update_time, updater_ip, log_type_str, currency_type_id) "
			+ "VALUES(PROVIDERUPDATELOG_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		List<Object[]> params = providerUpdateLogList.stream().map(providerUpdateLog -> new Object[] {
			providerUpdateLog.getProviderId(),
			providerUpdateLog.getWebsiteType(),
			providerUpdateLog.getLogType(), providerUpdateLog.getRecords(), providerUpdateLog.getUpdater(),
			new Timestamp(System.currentTimeMillis()), providerUpdateLog.getUpdaterIp(),
			providerUpdateLog.getLogTypeStr(), providerUpdateLog.getCurrencyTypeId(),
		}).toList();

		return DBQueryRunner.batch(conn, sql, params);
	}

	public static PageResult<ProviderUpdateLog> findProviderUpdateLogPageResult(
		Connection conn,
		Timestamp startDate,
		Timestamp endDate, int providerId, int type,
		int currency,
		String column, int pageNumber, int showCount, DBOrderType orderType, int websiteType) throws Exception {

		StringBuilder sql = new StringBuilder("SELECT * FROM PROVIDERUPDATELOG WHERE ");

		List<Object> params = new ArrayList<>();

		sql.append("WEBSITE_TYPE = ? ");
		params.add(websiteType);

		sql.append(" AND CURRENCY_TYPE_ID = ? ");
		params.add(currency);

		if(type > -99) {
			sql.append("AND LOG_TYPE = ? ");
			params.add(type);
		}

		if(providerId != -1) {
			sql.append(" AND PROVIDER_ID = ? ");
			params.add(providerId);
		}

		if (startDate != null) {
			sql.append(" AND update_time >= ? ");
			params.add(startDate);
		}

		if (endDate != null) {
			sql.append(" AND update_time <= ? ");
			params.add(endDate);
		}
		column = BeanConverter.getDBColumnName(AccountUpdateLog.class, column);
		if (column != null) {
			if(column.equals("updater")){
				sql.append(" ORDER BY LOWER(").append(column).append(") ");
			}else{
				sql.append(" ORDER BY ").append(column);
			}
			if (orderType != null) {
				sql.append(orderType.getSqlString());
			}
		}
		return DBQueryRunner
			.getPageResult(conn, ProviderUpdateLog.class, sql.toString(), pageNumber, showCount, params);

	}
}
