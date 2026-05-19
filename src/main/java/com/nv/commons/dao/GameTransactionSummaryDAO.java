package com.nv.commons.dao;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.GameTransactionSummary;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.OracleUtils;
import com.nv.commons.utils.PageUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import java.util.Date;

public class GameTransactionSummaryDAO {

	public static int insert(Connection conn, GameTransactionSummary bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				INSERT INTO GameTransactionSummary
				(WEBSITE_TYPE, USER_ID, VENDOR_ID, GAME_TYPE, SUMMARY_DATE,
				BET_COUNT, SUM_BET_AMOUNT, PROFIT, TURNOVER,
				PROGRESS_BET_AMOUNT, PROGRESS_PROFIT_LOSS,
				CURRENCY, VIP_LEVEL, AFFILIATE_ID, CREATE_TIME, UPDATE_TIME)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate(),
			bean.getBetCount(), bean.getSumBetAmount(), bean.getProfit(), bean.getTurnover(),
			bean.getProgressBetAmount(), bean.getProgressProfitLoss(),
			bean.getCurrency(), bean.getVipLevel(), bean.getAffiliateId()
		);
	}

	public static int update(Connection conn, GameTransactionSummary bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				UPDATE GameTransactionSummary SET
				BET_COUNT = ?, SUM_BET_AMOUNT = ?, PROFIT = ?, TURNOVER = ?,
				PROGRESS_BET_AMOUNT = ?, PROGRESS_PROFIT_LOSS = ?,
				CURRENCY = ?, VIP_LEVEL = ?, AFFILIATE_ID = ?, UPDATE_TIME = SYSTIMESTAMP
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND VENDOR_ID = ? AND GAME_TYPE = ? AND SUMMARY_DATE = ?
				""",
			bean.getBetCount(), bean.getSumBetAmount(), bean.getProfit(), bean.getTurnover(),
			bean.getProgressBetAmount(), bean.getProgressProfitLoss(),
			bean.getCurrency(), bean.getVipLevel(), bean.getAffiliateId(),
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate()
		);
	}

	public static GameTransactionSummary find(Connection conn, GameTransactionSummary bean) throws SQLException {

		return DBQueryRunner.getBean(conn, GameTransactionSummary.class,
			"""
				SELECT * FROM GameTransactionSummary
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND VENDOR_ID = ? AND GAME_TYPE = ? AND SUMMARY_DATE = ?
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate()
		);
	}

	public static String findBetSummaryDetailsByMultiCondition(Connection conn, String userId, WebSiteType webSiteType,
		String[] gameTypes, String[] vendorIds, String fromDate, String toDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo, LangMessage lang) throws Exception {

		String selectSql = "SELECT TRUNC(summary_date) summary_date, vendor_id, game_type, "
			+ "SUM(sum_bet_amount) sum_bet_amount, SUM(profit) profit, SUM(turnover) turnover ";
		StringBuilder sharedSql = new StringBuilder(
			"FROM GameTransactionSummary WHERE user_id = ? AND website_type = ? "
				+ "AND summary_date BETWEEN ? AND ? ");

		List<Object> paramsList = new ArrayList<>();
		paramsList.add(userId);
		paramsList.add(webSiteType.unique());

		String pattern = FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy;
		Timestamp startDate = DateTimeBuilder.localDateTime(fromDate, pattern).withMinTime().toTimestamp();
		Timestamp endDate = DateTimeBuilder.localDateTime(toDate, pattern).withMaxTime().toTimestamp();

		paramsList.add(startDate);
		paramsList.add(endDate);

		if (gameTypes != null) {
			sharedSql.append(" AND game_type in (").append(StringUtils.repeat("?", ",", gameTypes.length)).append(") ");
			paramsList.addAll(Arrays.asList(gameTypes));
		}
		if (vendorIds != null) {
			sharedSql.append(" AND vendor_id in (").append(StringUtils.repeat("?", ",", vendorIds.length)).append(") ");
			paramsList.addAll(Arrays.asList(vendorIds));
		}

		sharedSql.append("GROUP BY TRUNC(summary_date), vendor_id, game_type ");

		sortCondition = !StringUtils.isEmpty(sortCondition) && sortCondition.equalsIgnoreCase("SUMMARY_DATE") ?
			"TRUNC(SUMMARY_DATE)" :
			sortCondition;

		// Grand Total
		List<GameTransactionSummary> totalGameTransactionSummary = DBQueryRunner.getBeanList(conn,
			GameTransactionSummary.class, selectSql + sharedSql, paramsList);

		String countSql = "SELECT COUNT(*) FROM (" + selectSql + sharedSql + " )";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String sql = OracleUtils.getCalculatedPageSQL(
			selectSql + sharedSql + " ORDER BY " + sortCondition + " " + orderType.getSqlString());
		paramsList.add(pageInfo.getLastRowNumber());
		paramsList.add(pageInfo.getFirstRowNumber());

		PreparedStatement ps = null;
		ResultSet rs = null;
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {

			ps = conn.prepareStatement(sql);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), paramsList);
			rs = ps.executeQuery();
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartArray();

			while (rs.next()) {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("summaryDate", rs.getTimestamp("summary_date").getTime());
				jGenerator.writeStringField("summaryDateStr",
					FormatUtils.dateFormat(rs.getTimestamp("summary_date"),
						FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd));
				int vendorId = rs.getInt("vendor_id");
				jGenerator.writeNumberField("vendorId", vendorId);
				jGenerator.writeStringField("vendorName", VendorCache.getInstance().getVendor(vendorId).getName());
				Number gameType = (Number) rs.getObject("game_type");
				if (null == gameType) {
					jGenerator.writeNullField("gameType");
					jGenerator.writeNullField("gameTypeName");
				} else {
					jGenerator.writeNumberField("gameType", gameType.intValue());
					jGenerator
						.writeStringField("gameTypeName", GameType.getInstance(gameType.intValue()).getFullName(lang));
				}
				jGenerator.writeNumberField("amount", rs.getBigDecimal("sum_bet_amount"));
				jGenerator.writeNumberField("profit", rs.getBigDecimal("profit"));
				jGenerator.writeNumberField("turnover", rs.getBigDecimal("turnover"));
				jGenerator.writeEndObject();
			}

			jGenerator.writeEndArray();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeAll(ps, rs);
			JSONUtils.close(jGenerator);
		}

		String dataTableJson = pageInfo.getDataTableJson(out.toString());

		JSONObject mainObject = new JSONObject(dataTableJson);

		Map<String, BigDecimal> hashMap = new HashMap<>();
		hashMap.put("totalBetAmount", totalGameTransactionSummary.stream().map(GameTransactionSummary::getSumBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
		hashMap.put("totalProfitLoss", totalGameTransactionSummary.stream().map(GameTransactionSummary::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add));
		hashMap.put("totalTurnover", totalGameTransactionSummary.stream().map(GameTransactionSummary::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add));

		mainObject.accumulate(PageUtils.TOTAL_AMOUNT, new JSONObject(hashMap));

		return mainObject.toString();
	}

	/**
	 * 依日期區間撈取 GameTransactionSummary，僅以 TRUNC(summary_date) 聚合（不依 vendor / game_type 分組）。
	 * 回傳每日一列的彙總 JSON，欄位對齊 AccountSummaryReportBO 的格式：
	 * transactionTime / transactionTimeStr / paymentType / amount / profit / turnover。
	 */
	public static String findBetSummaryByDateRangeGroupByDate(Connection conn, String userId,
		WebSiteType webSiteType, Date startDate, Date endDate, int paymentType,
		String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		JsonValueProcessor processor) throws Exception {

		Timestamp start = DateTimeBuilder.localDateTime(startDate).withMinTime().toTimestamp();
		Timestamp end = DateTimeBuilder.localDateTime(endDate).withMaxTime().toTimestamp();

		List<Object> paramsList = new ArrayList<>();
		paramsList.add(userId);
		paramsList.add(webSiteType.unique());
		paramsList.add(start);
		paramsList.add(end);

		String selectSql = "SELECT TRUNC(summary_date) summary_date, "
			+ "SUM(sum_bet_amount) amount, SUM(profit) profit, SUM(turnover) turnover "
			+ "FROM GameTransactionSummary "
			+ "WHERE user_id = ? AND website_type = ? "
			+ "AND summary_date BETWEEN ? AND ? "
			+ "GROUP BY TRUNC(summary_date)";

		String countSql = "SELECT COUNT(*) FROM (" + selectSql + ")";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String safeSort = !StringUtils.isEmpty(sortCondition) && sortCondition.equalsIgnoreCase("SUMMARY_DATE")
			? "TRUNC(SUMMARY_DATE)"
			: sortCondition;

		String sql = OracleUtils.getCalculatedPageSQL(
			selectSql + " ORDER BY " + safeSort + " " + orderType.getSqlString());
		paramsList.add(pageInfo.getLastRowNumber());
		paramsList.add(pageInfo.getFirstRowNumber());

		// processor 取出每筆所需欄位並寫入 paymentType 固定值
		return DBQueryRunner.processJsonArrayValue(conn, processor, sql, paramsList);
	}
}
