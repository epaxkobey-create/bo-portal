package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AccountSummaryReport;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.model.database.ResultSetProcessor;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.OracleUtils;

public class AccountSummaryReportDAO {

	public static int insert(Connection conn, AccountSummaryReport bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				INSERT INTO AccountSummaryReport
				(WEBSITE_TYPE, USER_ID, PAYMENT_TYPE, TRANSACTION_TIME, AMOUNT, BONUS, PROFIT, CURRENCY)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getPaymentType(), bean.getTransactionTime(),
			bean.getAmount(), bean.getBonus(), bean.getProfit(), bean.getCurrency()
		);
	}

	public static int update(Connection conn, AccountSummaryReport bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				UPDATE AccountSummaryReport SET AMOUNT = ?, BONUS = ?, PROFIT = ?
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND PAYMENT_TYPE = ? AND TRANSACTION_TIME = ?
				""",
			bean.getAmount(), bean.getBonus(), bean.getProfit(),
			bean.getWebsiteType(), bean.getUserId(), bean.getPaymentType(), bean.getTransactionTime()
		);
	}

	public static AccountSummaryReport find(Connection conn, AccountSummaryReport bean)
		throws SQLException {

		return DBQueryRunner.getBean(conn, AccountSummaryReport.class,
			"""
				SELECT * FROM AccountSummaryReport
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND PAYMENT_TYPE = ? AND TRANSACTION_TIME = ?
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getPaymentType(), bean.getTransactionTime());
	}

	public static String findAccountSummaryReportByMultiCondition(Connection conn,
		String userId, WebSiteType webSiteType, Date start, Date end, AccountSummaryReportType paymentType,
		String sortCondition, DBOrderType orderType, PageInfo pageInfo, JsonValueProcessor processor) throws Exception {

		List<Object> paramsList = new ArrayList<>();
		StringBuilder selectSql;

		if (paymentType == AccountSummaryReportType.BET) {
			selectSql = buildBetWithTurnoverSql(webSiteType, userId, start, end, paramsList);
		} else {
			selectSql = buildGeneralSql(webSiteType, userId, paymentType, start, end, paramsList);
		}

		String countSql = "SELECT COUNT(*) FROM (" + selectSql + ")";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String sql = OracleUtils.getCalculatedPageSQL(selectSql + " ORDER BY " + sortCondition + " " + orderType.getSqlString());
		paramsList.add(pageInfo.getLastRowNumber());
		paramsList.add(pageInfo.getFirstRowNumber());

		return DBQueryRunner.processJsonArrayValue(conn, processor, sql, paramsList);
	}

	private static StringBuilder buildBetWithTurnoverSql(WebSiteType webSiteType, String userId,
		Date start, Date end, List<Object> paramsList) {

		String dateCond = buildDateCondition(start, end);

		// Inner BET query params
		paramsList.add(webSiteType.unique());
		paramsList.add(userId);
		paramsList.add(AccountSummaryReportType.BET.unique());
		addDateParams(start, end, paramsList);

		// Turnover subquery params
		paramsList.add(webSiteType.unique());
		paramsList.add(userId);
		paramsList.add(AccountSummaryReportType.TURNOVER.unique());
		addDateParams(start, end, paramsList);

		return new StringBuilder(
			"SELECT t.transaction_time, t.payment_type, t.amount, t.bonus, t.profit, COALESCE(v.turnover, 0) AS turnover ")
			.append("FROM (SELECT TRUNC(transaction_time) transaction_time, payment_type, ")
			.append("SUM(amount) amount, SUM(bonus) bonus, SUM(profit) profit ")
			.append("FROM v_accountsummaryreport WHERE website_type = ? AND user_id = ? AND payment_type = ? ")
			.append(dateCond)
			.append("GROUP BY TRUNC(transaction_time), payment_type) t ")
			.append("LEFT JOIN (SELECT TRUNC(transaction_time) transaction_time, SUM(amount) turnover ")
			.append("FROM v_accountsummaryreport WHERE website_type = ? AND user_id = ? AND payment_type = ? ")
			.append(dateCond)
			.append("GROUP BY TRUNC(transaction_time)) v ON t.transaction_time = v.transaction_time");
	}

	private static StringBuilder buildGeneralSql(WebSiteType webSiteType, String userId,
		AccountSummaryReportType paymentType, Date start, Date end, List<Object> paramsList) {

		String dateCond = buildDateCondition(start, end);

		paramsList.add(webSiteType.unique());
		paramsList.add(userId);
		if (null != paymentType) paramsList.add(paymentType.unique());
		addDateParams(start, end, paramsList);

		StringBuilder innerSql = new StringBuilder(
			"SELECT TRUNC(transaction_time) transaction_time, payment_type, ")
			.append("SUM(amount) amount, SUM(bonus) bonus, SUM(profit) profit ")
			.append("FROM v_accountsummaryreport WHERE website_type = ? AND user_id = ? ");
		if (null != paymentType) innerSql.append("AND payment_type = ? ");
		innerSql.append(dateCond).append("GROUP BY TRUNC(transaction_time), payment_type");

		StringBuilder selectSql = new StringBuilder(
			"SELECT transaction_time, payment_type, amount, bonus, profit, 0 AS turnover FROM (")
			.append(innerSql).append(") ");

		if (null != paymentType) {
			selectSql.append("WHERE payment_type = ? ");
			paramsList.add(paymentType.unique());
		}

		return selectSql;
	}

	private static String buildDateCondition(Date start, Date end) {
		StringBuilder sb = new StringBuilder();
		if (null != start) sb.append("AND transaction_time >= ? ");
		if (null != end) sb.append("AND transaction_time <= ? ");
		return sb.toString();
	}

	private static void addDateParams(Date start, Date end, List<Object> paramsList) {
		if (null != start) paramsList.add(DateTimeBuilder.localDateTime(start).withMinTime().toTimestamp());
		if (null != end) paramsList.add(DateTimeBuilder.localDateTime(end).withMaxTime().toTimestamp());
	}

	public static List<String> getUserIdForSummary(Connection conn, WebSiteType webSiteType, Timestamp startTime,
		Timestamp endTime) throws SQLException {
		String sql = "SELECT user_id FROM v_accountsummaryreport "
			+ "WHERE website_type = ? AND transaction_time >= ? AND transaction_time <= ? "
			+ "GROUP BY user_id ";

		List<String> userIdList = new ArrayList<>();

		ResultSetProcessor processor = (index, rs) -> userIdList.add(rs.getString("user_id"));

		DBQueryRunner.processResultSet(conn, processor, sql, webSiteType.unique(), startTime, endTime);

		return userIdList;
	}
}
