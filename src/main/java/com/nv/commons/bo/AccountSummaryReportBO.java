package com.nv.commons.bo;

import java.util.Date;

import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountSummaryReportDAO;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.LogUtils;

public class AccountSummaryReportBO {

	public static String getAccountSummaryReport(String userId, WebSiteType webSiteType,
		AccountSummaryReportType accountSummaryReportType, Date startDate, Date endDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo) {

		try {

			JsonValueProcessor process = (index, rs, jGenerator) -> {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("transactionTime", rs.getTimestamp("transaction_time").getTime());
				jGenerator.writeStringField("transactionTimeStr",
					FormatUtils.dateFormat(rs.getTimestamp("transaction_time"),
						FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd));
				jGenerator.writeNumberField("paymentType", rs.getInt("payment_type"));
				jGenerator.writeNumberField("amount", rs.getBigDecimal("amount"));
				jGenerator.writeNumberField("profit", rs.getBigDecimal("profit"));
				jGenerator.writeNumberField("turnover", rs.getBigDecimal("turnover"));
				jGenerator.writeEndObject();
			};

			return DbExecutor.query(conn ->
				AccountSummaryReportDAO.findAccountSummaryReportByMultiCondition(conn, userId, webSiteType,
					startDate, endDate, accountSummaryReportType, sortCondition, orderType, pageInfo, process));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}
}
