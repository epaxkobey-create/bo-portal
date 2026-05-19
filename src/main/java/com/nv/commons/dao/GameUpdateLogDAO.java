package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.nv.commons.constants.DBOrderType;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.GameUpdateLog;
import com.nv.commons.dto.PageResult;
import com.nv.commons.model.database.BeanConverter;
import com.nv.commons.model.database.DBQueryRunner;

public class GameUpdateLogDAO {

	public static int insert(Connection conn, GameUpdateLog gameUpdateLog) throws SQLException {
		String sql = "INSERT INTO GAMEUPDATELOG"
			+ "(id, GAME_ID, website_type, log_type, records, updater, update_time, updater_ip, log_type_str, currency_type_id) "
			+ "VALUES(GAMEUPDATELOG_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		return DBQueryRunner.update(conn, sql, gameUpdateLog.getgameId(),
			gameUpdateLog.getWebsiteType(),
			gameUpdateLog.getLogType(), gameUpdateLog.getRecords(), gameUpdateLog.getUpdater(),
			new Timestamp(System.currentTimeMillis()), gameUpdateLog.getUpdaterIp(),
			gameUpdateLog.getLogTypeStr(), gameUpdateLog.getCurrencyTypeId()
		);
	}


	public static PageResult<GameUpdateLog> findGameUpdateLogPageResult(
		Connection conn,
		Timestamp startDate,
		Timestamp endDate, int gameId, int type,
		int currency,
		String column, int pageNumber, int showCount, DBOrderType orderType, int websiteType) throws Exception {

		StringBuilder sql = new StringBuilder("SELECT * FROM GAMEUPDATELOG WHERE ");

		List<Object> params = new ArrayList<>();

		sql.append("WEBSITE_TYPE = ? ");
		params.add(websiteType);

		sql.append(" AND CURRENCY_TYPE_ID = ? ");
		params.add(currency);

		if(type > -99) {
			sql.append("AND LOG_TYPE = ? ");
			params.add(type);
		}

		if(gameId != -1) {
			sql.append(" AND GAME_ID = ? ");
			params.add(gameId);
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
			.getPageResult(conn, GameUpdateLog.class, sql.toString(), pageNumber, showCount, params);

	}
}
