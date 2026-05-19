package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameTxnSummaryType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameTransactionDAO;
import com.nv.commons.dao.GameTransactionHistoryDAO;
import com.nv.commons.dao.GameTransactionSummaryHourlyDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.dto.GameTransactionSummaryHourly;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.CollectionUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.GameTransactionUtils;
import com.nv.commons.utils.LogUtils;

public class GameTransactionBO {

	public static String getNotUnsettledBetDetailsByMultiCondition(String userId, WebSiteType webSiteType,
		int vendorId, int gameType, String date, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang, TimeZone timeZone) {

		try {

			if (timeZone == null) {
				Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);
				timeZone = WebsiteCurrencySettingCache.getInstance()
					.getDefaultTimeZone(webSiteType, CurrencyType.getInstance(account.getCurrencyTypeId()));
			}

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

			TimeZone finalTimeZone = timeZone;

			return DbExecutor.query(conn ->
				GameTransactionDAO.findNotUnsettledBetDetailsByMultiCondition(conn, condition,
					startDate, endDate, sortCondition, orderType, pageInfo, lang, finalTimeZone));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	public static String getUnsettledBetDetailsByMultiCondition(String userId, WebSiteType webSiteType,
		Date fromDate, Date toDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		LangMessage lang, TimeZone timeZone) {

		try {

			if (timeZone == null) {
				Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);
				timeZone = WebsiteCurrencySettingCache.getInstance()
					.getDefaultTimeZone(webSiteType, CurrencyType.getInstance(account.getCurrencyTypeId()));
			}

			Timestamp startDate = DateTimeBuilder.localDateTime(fromDate).withMinTime().toTimestamp();
			Timestamp endDate = DateTimeBuilder.localDateTime(toDate).withMaxTime().toTimestamp();

			TimeZone finalTimeZone = timeZone;

			return DbExecutor.query(conn ->
				GameTransactionDAO.findUnsettledBetDetailsByMultiCondition(conn, userId, webSiteType, null, null,
					startDate, endDate, sortCondition, orderType, pageInfo, lang, finalTimeZone));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	public static String getGameTxnByUserId(long id, String userId, WebSiteType webSiteType,
		Timestamp txnTime, Timestamp settleTime, LangMessage lang) throws Exception {

		String gameTxnJson;

		try {

			Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);
			CurrencyType currencyType = CurrencyType.getInstance(account.getCurrencyTypeId());

			TimeZone timeZone = WebsiteCurrencySettingCache.getInstance().getDefaultTimeZone(webSiteType, currencyType);

			gameTxnJson = DbExecutor.query(conn ->
				GameTransactionDAO.findTxnById(conn, id, webSiteType, txnTime, settleTime, lang, timeZone,
					currencyType.unique()));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}

		return gameTxnJson;
	}

	public static void insertGameTxnHistory() {
		Timestamp endDate = DateTimeBuilder.localDateTime(GameTransactionUtils.getReservedTime()).minusDays(1)
			.toTimestamp();
		Connection readConn = null;
		Map<WebSiteType, List<Timestamp>> websiteTimeMap = new HashMap<>();
		try {
			readConn = DBPool.getReadConnection();
			websiteTimeMap = GameTransactionDAO.getBeforeDate(readConn, endDate);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(readConn);
		}

		for (WebsiteInfo websiteInfo : WebsiteInfoCache.getInstance().getAll()) {
			WebSiteType webSiteType = WebSiteType.getInstance(websiteInfo.getId());
			List<Timestamp> timeList = websiteTimeMap.computeIfAbsent(webSiteType, w -> new ArrayList<>());
			timeList.add(endDate);
		}

		for (Map.Entry<WebSiteType, List<Timestamp>> websiteEntry : websiteTimeMap.entrySet()) {
			WebSiteType webSiteType = websiteEntry.getKey();

			for (Timestamp time : websiteEntry.getValue()) {
				int hour = 0;
				while (hour < 24) {
					Timestamp startTime = DateTimeBuilder.localDateTime(time).plusHours(hour).toTimestamp();
					Timestamp endTime = DateTimeBuilder.localDateTime(time).plusHours(hour += 1).toTimestamp();

					List<Long> ids = Collections.emptyList();
					Connection readStandByConn = null;
					try {
						readStandByConn = DBPool.getStandbyReadConnection();
						ids = GameTransactionDAO.getIdByDate(readStandByConn, startTime, endTime);
					} catch (Exception e) {
						LogUtils.SYS.error(e.getMessage(), e);
					} finally {
						DbUtils.close(readStandByConn);
					}

					if (ids.isEmpty()) {
						continue;
					}

					List<List<Long>> idGroup = ids.stream().collect(CollectionUtils.groupingBy(1000));
					Connection writeConn = null;
					try {
						writeConn = DBPool.getWriteConnection();
						writeConn.setAutoCommit(false);

						for (List<Long> idIndividual : idGroup) {

							long insertHistoryDuration = System.currentTimeMillis();

							GameTransactionHistoryDAO.insertToHistory(writeConn, idIndividual);

							LogUtils.backOfficeMonitor.info("Insert to game history, website: {}, spend {} secs ",
								webSiteType,
								DateUtils.secondsBetween(insertHistoryDuration, System.currentTimeMillis()));

							long deleteMainDuration = System.currentTimeMillis();

							GameTransactionDAO.delete(writeConn, idIndividual);

							LogUtils.backOfficeMonitor.info("Delete game bet record, website: {}, spend {} secs ",
								webSiteType,
								DateUtils.secondsBetween(deleteMainDuration, System.currentTimeMillis()));

							writeConn.commit();
						}

					} catch (Exception e) {
						DbUtils.rollback(writeConn);
						LogUtils.SYS.error(e.getMessage(), e);
					} finally {
						DbUtils.close(writeConn);
					}
				}
			}
		}
	}

	public static void insertGameTxnSummaryHourly() {

		try {

			Timestamp endTime = DateTimeBuilder.localDateTime()
				//			.truncatedTo(ChronoUnit.HOURS)
				.truncatedTo(ChronoUnit.MINUTES) // TODO: confirm the interval
				.toTimestamp();

			Map<GameTransactionSummaryHourly, List<Long>> summarizeHourly =
				DbExecutor.query(conn -> GameTransactionDAO.summarizeHourly(conn, endTime));

			for (Map.Entry<GameTransactionSummaryHourly, List<Long>> summarizeHourlyEntry : summarizeHourly.entrySet()) {
				GameTransactionSummaryHourly summaryHourly = summarizeHourlyEntry.getKey();
				List<Long> ids = summarizeHourlyEntry.getValue();

				if (ids.isEmpty()) {
					continue;
				}

				GameTransactionSummaryHourly existingSummaryHourly =
					DbExecutor.query(conn -> GameTransactionSummaryHourlyDAO.find(conn, summaryHourly));

				DbExecutor.update(conn -> {

					long insertDuration = System.currentTimeMillis();

					if (existingSummaryHourly != null) {

						summaryHourly.setBetCount(
							summaryHourly.getBetCount() + existingSummaryHourly.getBetCount());
						summaryHourly.setSumBetAmount(
							summaryHourly.getSumBetAmount().add(existingSummaryHourly.getSumBetAmount()));
						summaryHourly.setProfit(
							summaryHourly.getProfit().add(existingSummaryHourly.getProfit()));
						summaryHourly.setTurnover(
							summaryHourly.getTurnover().add(existingSummaryHourly.getTurnover()));
						summaryHourly.setProgressBetAmount(
							summaryHourly.getProgressBetAmount().add(existingSummaryHourly.getProgressBetAmount()));
						summaryHourly.setProgressProfitLoss(
							summaryHourly.getProgressProfitLoss().add(existingSummaryHourly.getProgressProfitLoss()));

						GameTransactionSummaryHourlyDAO.update(conn, summaryHourly);

						LogUtils.backOfficeMonitor.info("Update game trans summary hourly, spend {} secs",
							DateUtils.secondsBetween(insertDuration, System.currentTimeMillis()));
					} else {

						GameTransactionSummaryHourlyDAO.insert(conn, summaryHourly);

						LogUtils.backOfficeMonitor.info("Insert into game trans summary hourly, spend {} secs",
							DateUtils.secondsBetween(insertDuration, System.currentTimeMillis()));
					}

					long updateDuration = System.currentTimeMillis();

					GameTransactionDAO.updateIsSummarized(conn, ids);

					LogUtils.backOfficeMonitor.info("Update game trans summarized, spend {} secs ",
						DateUtils.secondsBetween(updateDuration, System.currentTimeMillis()));

					return ids.size();
				});
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public static List<GameTransaction> getOddsAndOddsTypeIsNULLRecords(int webSiteType, int vendorId) {
		try {
			return DbExecutor.query(conn ->
				GameTransactionDAO.findOddsAndOddsTypeIsNULLRecords(conn, webSiteType, vendorId));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	public static void updateOddsAndOddsType(List<GameTransaction> list) {
		try {
			for (GameTransaction gameTxn : list) {
				DbExecutor.update(conn ->
					GameTransactionDAO.updateOddsAndOddsType(conn, gameTxn));
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	/**
	 * 從 GameTransaction 即時聚合每日 / 每廠商 / 每遊戲類型的投注摘要。
	 * 行為對齊 {@code MemberServlet#searchBetDetails}：以 {@code settle_time} 為時間軸，
	 * 排除 UNSETTLED 並僅取已彙總過的紀錄。回傳結構與
	 * {@link GameTransactionSummaryBO#getBetSummaryDetails} 完全一致，可作為替代資料源。
	 */
	public static String getBetSummaryDetailsFromTransaction(String userId, WebSiteType webSiteType,
		String[] gameTypes, String[] vendorIds, String fromDate, String toDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo, LangMessage lang) {

		try {
			return DbExecutor.query(conn ->
				GameTransactionDAO.findBetSummaryDetailsByMultiCondition(conn, userId, webSiteType,
					gameTypes, vendorIds, fromDate, toDate, sortCondition, orderType, pageInfo, lang));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	/**
	 * 從 GameTransaction 即時聚合，僅以日期分組（不依 vendor / game_type）。
	 * 回傳格式對齊 {@link GameTransactionSummaryBO#getBetSummaryByDateRange}：每列含
	 * transactionTime / transactionTimeStr / paymentType (固定為 BET) / amount / profit / turnover。
	 */
	public static String getBetSummaryByDateRange(String userId, WebSiteType webSiteType,
		Date startDate, Date endDate, String sortCondition, DBOrderType orderType, PageInfo pageInfo) {

		try {
			final int paymentType = com.nv.commons.constants.AccountSummaryReportType.BET.unique();

			com.nv.commons.model.database.JsonValueProcessor processor = (index, rs, jGenerator) -> {
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
				GameTransactionDAO.findBetSummaryByDateRangeGroupByDate(conn, userId, webSiteType,
					startDate, endDate, paymentType, sortCondition, orderType, pageInfo, processor));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		}
	}
}
