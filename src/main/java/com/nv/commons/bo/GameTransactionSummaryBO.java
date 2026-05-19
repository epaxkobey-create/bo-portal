package com.nv.commons.bo;

import java.util.Date;

import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameTransactionSummaryDAO;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.LogUtils;

public class GameTransactionSummaryBO {

	public static String getBetSummaryDetails(String userId, WebSiteType webSiteType,
		String[] gameTypes, String[] vendorIds, String fromDate, String toDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo, LangMessage lang) {

		try {
			return DbExecutor.query(conn ->
				GameTransactionSummaryDAO.findBetSummaryDetailsByMultiCondition(conn, userId, webSiteType,
					gameTypes, vendorIds, fromDate, toDate, sortCondition, orderType, pageInfo, lang));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	/**
	 * 依日期區間撈取 GameTransactionSummary，僅以日期聚合（不依 vendor / game_type 分組）。
	 * 回傳格式對齊 {@link AccountSummaryReportBO#getAccountSummaryReport}：每列含
	 * transactionTime / transactionTimeStr / paymentType (固定為 BET) / amount / profit / turnover。
	 */
	public static String getBetSummaryByDateRange(String userId, WebSiteType webSiteType,
		Date startDate, Date endDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo) {

		try {
			final int paymentType = AccountSummaryReportType.BET.unique();

			JsonValueProcessor processor = (index, rs, jGenerator) -> {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("transactionTime", rs.getTimestamp("summary_date").getTime());
				jGenerator.writeStringField("transactionTimeStr",
					FormatUtils.dateFormat(rs.getTimestamp("summary_date"),
						FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd));
				jGenerator.writeNumberField("paymentType", paymentType);
				jGenerator.writeNumberField("amount", rs.getBigDecimal("amount"));
				jGenerator.writeNumberField("profit", rs.getBigDecimal("profit"));
				jGenerator.writeNumberField("turnover", rs.getBigDecimal("turnover"));
				jGenerator.writeEndObject();
			};

			return DbExecutor.query(conn ->
				GameTransactionSummaryDAO.findBetSummaryByDateRangeGroupByDate(conn, userId, webSiteType,
					startDate, endDate, paymentType, sortCondition, orderType, pageInfo, processor));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}
}
