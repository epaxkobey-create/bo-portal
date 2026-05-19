package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.PageResult;
import com.nv.commons.model.database.BeanConverter;
import com.nv.commons.model.database.DBQueryRunner;
import org.apache.commons.lang3.StringUtils;

public class AccountUpdateLogDAO {

	// MEMO: 每種 update type 都只會取回一筆，所以不用分頁
	public static List<AccountUpdateLog> getAccountUpdateLog(Connection conn, String userId, int webSiteType)
		throws SQLException {

		// TODO: 有更好的寫法嗎？ accountUpdateLog 的資料量很大
		String sql = "SELECT t.updater, t.log_type, t.update_time "
			+ "FROM (SELECT log_type, user_id, website_type, MAX(update_time) as MaxTime "
			+ "FROM accountUpdateLog WHERE user_id = ? AND website_type = ? GROUP BY log_type, user_id, website_type) r "
			+ "INNER JOIN accountUpdateLog t "
			+ "ON t.log_type = r.log_type AND t.update_time = r.MaxTime AND t.user_id = r.user_id AND t.website_type = r.website_type";

		return DBQueryRunner.getBeanList(conn, AccountUpdateLog.class, sql, userId, webSiteType);
	}


	public static int insert(Connection conn, AccountUpdateLog accountUpdateLog) throws SQLException {

		String sql = "INSERT INTO accountUpdateLog"
			+ "(id, user_id, website_type, log_type, records, updater, update_time, updater_ip, currency_type_id, log_type_str) "
			+ "VALUES(ACCOUNTUPDATELOG_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, ?, ?)";

		return DBQueryRunner.update(conn, sql, accountUpdateLog.getUserId(),
			accountUpdateLog.getWebsiteType(),
			accountUpdateLog.getLogType(), accountUpdateLog.getRecords(), accountUpdateLog.getUpdater(),
//			accountUpdateLog.getUpdateTime(),
			accountUpdateLog.getUpdaterIp(), accountUpdateLog.getCurrencyTypeId(), accountUpdateLog.getLogTypeStr());
	}

	public static int insert(Connection conn, AccountUpdateLog accountUpdateLog, Timestamp updateTime)
		throws SQLException {

		if (updateTime == null) {
			return insert(conn, accountUpdateLog);
		}

		String sql = "INSERT INTO accountUpdateLog"
			+ "(id, user_id, website_type, log_type, records, updater, update_time, updater_ip, currency_type_id, log_type_str) "
			+ "VALUES(ACCOUNTUPDATELOG_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		return DBQueryRunner.update(conn, sql, accountUpdateLog.getUserId(),
			accountUpdateLog.getWebsiteType(),
			accountUpdateLog.getLogType(), accountUpdateLog.getRecords(), accountUpdateLog.getUpdater(),
			updateTime,
			accountUpdateLog.getUpdaterIp(), accountUpdateLog.getCurrencyTypeId(), accountUpdateLog.getLogTypeStr());
	}

//	@Deprecated
//	public static int update(Connection conn, AccountUpdateLog accountUpdateLog) throws SQLException {
//
//		return DBQueryRunner.update(conn,
//			"UPDATE accountUpdateLog SET records = ? WHERE user_id = ? AND website_type = ?",
//			accountUpdateLog.getRecords(), accountUpdateLog.getUserId(), accountUpdateLog.getWebsiteType());
//	}

	public static void batchInsert(Connection conn, List<AccountUpdateLog> accountUpdateLogList) throws SQLException {
		String sql = "INSERT INTO accountUpdateLog"
			+ "(id, user_id, website_type, log_type, records, updater, update_time, updater_ip, currency_type_id, log_type_str) "
			+ "VALUES (ACCOUNTUPDATELOG_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		List<Object[]> paramList = accountUpdateLogList.stream().map(
			log -> new Object[] {
				log.getUserId(),
				log.getWebsiteType(),
				log.getLogType(),
				log.getRecords(),
				log.getUpdater(),
				log.getUpdateTime(),
				log.getUpdaterIp(),
				log.getCurrencyTypeId(),
				log.getLogTypeStr()
			}).collect(Collectors.toList());

		DBQueryRunner.batch(conn, sql, paramList);
	}

	public static PageResult<AccountUpdateLog> findAccountUpdateLogRpt(Connection conn, Timestamp startDate,
		Timestamp endDate, String searchName, AccountUpdateType type,
		List<AccountUpdateType> accessUpdateTypeOption, int currency, List<Integer> managerCurrencyList,
		String column, int pageNumber, int showCount, DBOrderType orderType, WebSiteType websitetype)
		throws Exception {

		StringBuilder sql = new StringBuilder(
			"SELECT t.id, t.log_type, t.user_id, t.records, t.update_time, t.updater_ip, t.currency_type_id, t.log_type_str, ");
		sql.append("CASE ");
			sql.append("WHEN k.first_name IS NULL AND k.last_name IS NULL THEN t.updater ");
			sql.append("WHEN k.first_name IS NULL THEN k.last_name ");
			sql.append("WHEN k.last_name IS NULL THEN k.first_name ");
			sql.append("ELSE k.first_name || ' ' || k.last_name ");
		sql.append("END AS updater ");
		sql.append("FROM accountUpdateLog t ");
		sql.append("LEFT JOIN accountDocument d ON ");
			sql.append("d.user_id = t.updater AND d.website_type = t.website_type AND d.status = ");
			sql.append(DocumentStatusType.APPROVED.unique()).append(" ");
		sql.append("LEFT JOIN kycPersonalInfo k ON ");
			sql.append("k.account_document_id = d.id ");
		sql.append("WHERE t.website_type = ? ");
		List<Object> paramsList = new ArrayList<>();

		paramsList.add(websitetype.unique());

		if (type != null) {
			sql.append(" AND t.log_type = ? ");
			paramsList.add(type.unique());
		} else {
			sql.append(" AND t.log_type IN (")
				.append(StringUtils.repeat("?", ",", accessUpdateTypeOption.size()))
				.append(")");
			accessUpdateTypeOption.stream().map(AccountUpdateType::unique).forEach(paramsList::add);
		}

		if (searchName != null) {
			sql.append(" AND t.user_id = ? ");
			paramsList.add(searchName);
		}

		if (startDate != null) {
			sql.append(" AND t.update_time >= ? ");
			paramsList.add(startDate);
		}

		if (endDate != null) {
			sql.append(" AND t.update_time <= ? ");
			paramsList.add(endDate);
		}

		if (currency != -1) {
			sql.append(" AND t.currency_type_id = ? ");
			paramsList.add(currency);
		} else {
			sql.append(" AND t.currency_type_id IN (")
				.append(StringUtils.repeat("?", ",", managerCurrencyList.size()))
				.append(")");
			paramsList.addAll(managerCurrencyList);
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
			.getPageResult(conn, AccountUpdateLog.class, sql.toString(), pageNumber, showCount, paramsList);
	}

}
