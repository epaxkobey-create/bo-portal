package com.nv.commons.bo;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameTxnSummaryType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.SystemTxnStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameTransactionDAO;
import com.nv.commons.dao.GameTransactionSummaryDAO;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;
import com.nv.commons.utils.Validator;

public class BTReportBO {

	public static String getSettledBetsSummary(String userId, WebSiteType webSiteType,
		String fromDate, String toDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang) throws Exception {

		try {

			String json = DbExecutor.query(conn ->
				GameTransactionSummaryDAO.findBetSummaryDetailsByMultiCondition(conn, userId, webSiteType,
					null, null, fromDate, toDate, sortCondition, orderType, pageInfo, lang));

			JsonNode rootNode = JSONUtils.getObjectMapper().readTree(json);
			if (!isValidRootNode(rootNode)) {
				return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
			}

			JsonNode totalAmount = rootNode.get("iTotalAmount");
			String totalAmountJson = totalAmount.toString();

			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {

				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartArray();

				for (JsonNode data : rootNode.get("aaData")) {

					long summaryDate = data.get("summaryDate").asLong();
					int vendorId = data.get("vendorId").asInt();
					String vendorName = data.get("vendorName").asText();
					String gameType = data.get("gameType").asText(null);
					double profit = data.get("profit").asDouble();
					double turnover = data.get("turnover").asDouble();

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("summaryDate", summaryDate);
					jGenerator.writeNumberField("vendorId", vendorId);
					jGenerator.writeStringField("vendorName", vendorName);
					if (null == gameType) {
						jGenerator.writeNullField("gameTypeId");
					} else {
						jGenerator.writeNumberField("gameTypeId", Integer.parseInt(gameType));
					}
					jGenerator.writeNumberField("profit", profit);
					jGenerator.writeNumberField("turnover", turnover);
					jGenerator.writeEndObject();
				}

				jGenerator.writeEndArray();
			} finally {
				JSONUtils.close(jGenerator);
			}

			return pageInfo.getPageInfoJson("records", out.toString(), "totalAmount", totalAmountJson);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
	}

	/**
	 * 與 {@link #getSettledBetsSummary} 同 shape，但資料源改為從 GAMETRANSACTION 即時聚合
	 * （不依賴 GAMETRANSACTIONSUMMARY 預先彙總的結果）。
	 */
	public static String getSettledBetsSummaryFromTransaction(String userId, WebSiteType webSiteType,
		String fromDate, String toDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang) throws Exception {

		try {

			String json = DbExecutor.query(conn ->
				GameTransactionDAO.findBetSummaryDetailsByMultiCondition(conn, userId, webSiteType,
					null, null, fromDate, toDate, sortCondition, orderType, pageInfo, lang));

			JsonNode rootNode = JSONUtils.getObjectMapper().readTree(json);
			if (!isValidRootNode(rootNode)) {
				return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
			}

			JsonNode totalAmount = rootNode.get("iTotalAmount");
			String totalAmountJson = totalAmount.toString();

			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {

				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartArray();

				for (JsonNode data : rootNode.get("aaData")) {

					long summaryDate = data.get("summaryDate").asLong();
					int vendorId = data.get("vendorId").asInt();
					String vendorName = data.get("vendorName").asText();
					String gameType = data.get("gameType").asText(null);
					double profit = data.get("profit").asDouble();
					double turnover = data.get("turnover").asDouble();

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("summaryDate", summaryDate);
					jGenerator.writeNumberField("vendorId", vendorId);
					jGenerator.writeStringField("vendorName", vendorName);
					if (null == gameType) {
						jGenerator.writeNullField("gameTypeId");
					} else {
						jGenerator.writeNumberField("gameTypeId", Integer.parseInt(gameType));
					}
					jGenerator.writeNumberField("profit", profit);
					jGenerator.writeNumberField("turnover", turnover);
					jGenerator.writeEndObject();
				}

				jGenerator.writeEndArray();
			} finally {
				JSONUtils.close(jGenerator);
			}

			return pageInfo.getPageInfoJson("records", out.toString(), "totalAmount", totalAmountJson);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
	}

	public static String getAccountSummaryBetDetailReport(String userId, WebSiteType webSiteType,
		int vendorId, int gameType, String date, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang, TimeZone timeZone) throws Exception {

		try {

			Timestamp startDate = DateTimeBuilder.localDateTime(date, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy)
				.withMinTime().toTimestamp();

			Timestamp endDate = DateTimeBuilder.localDateTime(date, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy)
				.withMaxTime().toTimestamp();

			GameTransaction condition = new GameTransaction();
			condition.setUserId(userId);
			condition.setWebsiteType(webSiteType.unique());
			condition.setVendorId(vendorId);
			condition.setGameType(gameType);
			condition.setIsGameTxnSummarized(GameTxnSummaryType.SUMMARIZED.unique());

			String json = DbExecutor.query(conn ->
				GameTransactionDAO.findNotUnsettledBetDetailsByMultiCondition(conn, condition,
					startDate, endDate, sortCondition, orderType, pageInfo, lang, timeZone));

			JsonNode rootNode = JSONUtils.getObjectMapper().readTree(json);
			if (!isValidRootNode(rootNode)) {
				return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
			}

			JsonNode totalAmount = rootNode.get("iTotalAmount");
			String totalAmountJson = totalAmount.toString();

			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {

				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartArray();

				for (JsonNode data : rootNode.get("aaData")) {
					String txnTimeZone = data.get("txnTimeZone").asText(null);
					long txnTimeStamp = DateUtils.toDate(txnTimeZone, FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd_HHmmss)
						.getTime();

					String settleTimeZone = data.get("settleTimeZone").asText(null);
					long settleTimeStamp = DateUtils.toDate(settleTimeZone,
							FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd_HHmmss)
						.getTime();

					String createTimeZone = data.get("createTimeZone").asText(null);
					long createTimeStamp = DateUtils.toDate(createTimeZone,
							FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd_HHmmss)
						.getTime();

					String vendorName = data.get("vendorName").asText();
					String vendorTxnId = data.get("vendorTxnId").asText();
					String betType = data.get("betType").asText();

					int gameTypeId = data.get("gameTypeId").asInt();
					double betAmount = data.get("betAmount").asDouble();
					double profit = data.get("profitLoss").asDouble();
					double turnover = data.get("turnover").asDouble();
					int id = data.get("id").asInt();
					String txnStatus = data.get("txnStatus").asText();
					double odds = data.get("odds").asDouble();

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("txnTimestamp", txnTimeStamp);
					jGenerator.writeNumberField("settleTimestamp", settleTimeStamp);
					jGenerator.writeNumberField("createTimestamp", createTimeStamp);
					jGenerator.writeNumberField("settleDate", settleTimeStamp);
					jGenerator.writeStringField("vendorName", vendorName);
					String gameName = data.get("gameName").asText();
					String gameNameEn = data.get("gameNameEn").asText();
					jGenerator.writeStringField("gameName", gameName);
					jGenerator.writeStringField("gameNameEn", Validator.isEmpty(gameNameEn) ? gameName : gameNameEn);
					jGenerator.writeNumberField("gameTypeId", gameTypeId);
					jGenerator.writeNumberField("betAmount", betAmount);
					jGenerator.writeNumberField("profit", profit);
					jGenerator.writeNumberField("turnover", turnover);
					jGenerator.writeNumberField("transactionId", id);
					jGenerator.writeNumberField("txnStatusTypeId", Integer.parseInt(txnStatus));
					jGenerator.writeNumberField("vendorId", vendorId);
					jGenerator.writeStringField("vendorTxnId", vendorTxnId);
					jGenerator.writeNumberField("odds", odds);
					jGenerator.writeStringField("betType", betType);

					SystemTxnStatusType systemTxnStatusType = SystemTxnStatusType.getInstance(
						Integer.parseInt(txnStatus));

					if (systemTxnStatusType == null) {
						LogUtils.SYS.error(
							"getAccountSummaryBetDetailReport error, systemTxnStatusType is null, txnStatus: {}",
							txnStatus);
						throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
					}

					String betResult = switch (systemTxnStatusType) {
						case VOID -> "voided";
						case REVOACTION -> "cancelled";
						default -> BigDecimal.valueOf(profit).compareTo(BigDecimal.ZERO) > 0 ? "win" : "lose";
					};

					jGenerator.writeStringField("betResult", betResult);
					jGenerator.writeEndObject();
				}

				jGenerator.writeEndArray();
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			} finally {
				JSONUtils.close(jGenerator);
			}

			return pageInfo.getPageInfoJson("records", out.toString(), "totalAmount", totalAmountJson);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return pageInfo.getPageInfoJson("records", "[]", "totalAmount", "{}");
	}

	public static String getUnsettledBetsSummary(String userId, WebSiteType webSiteType,
		Timestamp fromDate, Timestamp toDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang, TimeZone timeZone) throws Exception {

		try {

			String json = DbExecutor.query(
				conn -> GameTransactionDAO.findUnsettledBetDetailsByMultiCondition(conn, userId, webSiteType,
					null, null, fromDate, toDate, sortCondition, orderType, pageInfo, lang,
					timeZone));

			JsonNode rootNode = JSONUtils.getObjectMapper().readTree(json);
			if (!isValidRootNode(rootNode)) {
				return pageInfo.getPageInfoJson("records", "[]");
			}

			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {

				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartArray();

				for (JsonNode data : rootNode.get("aaData")) {

					long summaryDate = data.get("txnTimestamp").asLong();
					int vendorId = data.get("vendorId").asInt();
					String vendorName = data.get("vendorName").asText();
					int gameTypeId = data.get("gameTypeId").asInt();
					String gameType = data.get("gameType").asText(null);
					double betAmount = data.get("betAmount").asDouble();

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("summaryDate", summaryDate);
					jGenerator.writeNumberField("vendorId", vendorId);
					jGenerator.writeStringField("vendorName", vendorName);
					if (null == gameType) {
						jGenerator.writeNullField("gameTypeId");
					} else {
						jGenerator.writeNumberField("gameTypeId", gameTypeId);
					}
					jGenerator.writeNumberField("betAmount", betAmount);
					jGenerator.writeEndObject();
				}

				jGenerator.writeEndArray();
			} finally {
				JSONUtils.close(jGenerator);
			}

			return pageInfo.getPageInfoJson("records", out.toString());
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return pageInfo.getPageInfoJson("records", "[]");
	}

	// BO bet report

	public static String getBetReport(WebSiteType webSiteType, String userId, Timestamp transactionStartDate,
		Timestamp transactionEndDate, BigDecimal minAmount, BigDecimal maxAmount, String sortField,
		DBOrderType sortOder)
		throws Exception {
		return DbExecutor.query(conn ->
			{
				try {
					return GameTransactionDAO.findBetReport(conn, userId, webSiteType.unique(), transactionStartDate,
						transactionEndDate, minAmount, maxAmount, sortField, sortOder);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		);

	}

	/**
	 * Get bet report with pagination following the pattern of searchMember
	 * Returns JSON with paginated results, subTotal (current page), and grandTotal (all pages)
	 */
	public static String getBetReportWithPagination(
		WebSiteType webSiteType,
		String userId,
		SystemTxnStatusType systemTxnStatusType,
		Timestamp transactionStartDate,
		Timestamp transactionEndDate,
		BigDecimal minAmount,
		BigDecimal maxAmount,
		String sortField,
		DBOrderType sortOrder,
		PageInfo pageInfo
	) throws Exception {
		return DbExecutor.query(conn -> {
			try {
				// 1. Get paginated data
				String paginatedJson = GameTransactionDAO.findBetReportWithPagination(
					conn, userId, webSiteType.unique(), systemTxnStatusType.unique(),
					transactionStartDate, transactionEndDate, minAmount, maxAmount,
					sortField, sortOrder, pageInfo);

				// 2. Get grand totals (sum of ALL records across all pages)
				Map<String, Object> grandTotals = GameTransactionDAO.findBetReportGrandTotals(
						conn, userId, webSiteType.unique(), systemTxnStatusType.unique(),
						transactionStartDate, transactionEndDate, minAmount, maxAmount);

				// 3. Parse paginated JSON and calculate subTotal (current page only)
				JsonNode rootNode = JSONUtils.getObjectMapper().readTree(paginatedJson);

				BigDecimal subTotalBetAmount = BigDecimal.ZERO;
				BigDecimal subTotalProfitLoss = BigDecimal.ZERO;
				BigDecimal subTotalTurnover = BigDecimal.ZERO;

				// Calculate subtotals from current page data
				for (JsonNode record : rootNode) {
					subTotalBetAmount = subTotalBetAmount.add(
						record.has("betAmount") && !record.get("betAmount").isNull()
							? new BigDecimal(record.get("betAmount").asText())
							: BigDecimal.ZERO
					);
					subTotalProfitLoss = subTotalProfitLoss.add(
						record.has("profitLoss") && !record.get("profitLoss").isNull()
							? new BigDecimal(record.get("profitLoss").asText())
							: BigDecimal.ZERO
					);
					subTotalTurnover = subTotalTurnover.add(
						record.has("turnover") && !record.get("turnover").isNull()
							? new BigDecimal(record.get("turnover").asText())
							: BigDecimal.ZERO
					);
				}

				// 4. Build complete response with aaData, subtotals, and grandtotals
				BigDecimal grandTotalBetAmount = toBigDecimal(grandTotals.get("TOTAL_BET_AMOUNT"));
				BigDecimal grandTotalProfitLoss = toBigDecimal(grandTotals.get("TOTAL_PROFIT_LOSS"));
				BigDecimal grandTotalTurnover = toBigDecimal(grandTotals.get("TOTAL_TURNOVER"));

				StringWriter responseWriter = new StringWriter();
				JsonGenerator jGenerator = null;
				try {
					jGenerator = JSONUtils.getFactory().createGenerator(responseWriter);
					jGenerator.writeStartObject();
					jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pageInfo.getTotalCount());
					jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pageInfo.getTotalCount());

					jGenerator.writeFieldName(PageUtils.SHOW_DATA);
					jGenerator.writeRawValue(paginatedJson);

					jGenerator.writeObjectFieldStart("subTotals");
					jGenerator.writeNumberField("betAmount", subTotalBetAmount);
					jGenerator.writeNumberField("profitLoss", subTotalProfitLoss);
					jGenerator.writeNumberField("turnover", subTotalTurnover);
					jGenerator.writeEndObject();

					jGenerator.writeObjectFieldStart("grandTotals");
					jGenerator.writeNumberField("betAmount", grandTotalBetAmount);
					jGenerator.writeNumberField("profitLoss", grandTotalProfitLoss);
					jGenerator.writeNumberField("turnover", grandTotalTurnover);
					jGenerator.writeEndObject();

					jGenerator.writeEndObject();
				} finally {
					JSONUtils.close(jGenerator);
				}

				// 5. Return combined result
				return responseWriter.toString();

			} catch (Exception e) {
				LogUtils.SYS.error("Error in getBetReportWithPagination: {}", e.getMessage(), e);
				throw new RuntimeException(e);
			}
		});
	}

	private static BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		if (value instanceof Number n) {
			return BigDecimal.valueOf(n.doubleValue());
		}
		return BigDecimal.ZERO;
	}

	private static boolean isValidRootNode(JsonNode rootNode) {
		JsonNode aaDataNode = rootNode.get("aaData");
		if (aaDataNode == null || !aaDataNode.isArray()) {
			LogUtils.SYS.debug("Invalid JSON structure: missing or invalid 'aaData' field");
			return false;
		}
		JsonNode totalAmountNode = rootNode.get("iTotalAmount");
		if (totalAmountNode == null || !totalAmountNode.isObject()) {
			LogUtils.SYS.debug("Invalid JSON structure: missing or invalid 'iTotalAmount' field");
			return false;
		}
		return true;
	}
}
