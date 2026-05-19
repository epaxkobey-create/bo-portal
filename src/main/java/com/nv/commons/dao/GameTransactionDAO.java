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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.bo.ReferenceDataBO;
import com.nv.commons.cache.GameCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.BetType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameTxnSummaryType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.OddsType;
import com.nv.commons.constants.SystemTxnStatusType;
import com.nv.commons.constants.TimePeriodType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.dto.GameTransactionSummaryHourly;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.model.database.ResultSetProcessor;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.OracleUtils;
import com.nv.commons.utils.PageUtils;
import com.nv.commons.utils.Validator;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class GameTransactionDAO {

	public static Map<WebSiteType, Integer> getGameTxnNotSummarizedCount(Connection conn) throws SQLException {
		String sql = " SELECT website_type, COUNT(*) AS count FROM gametransaction "
			+ " WHERE settle_time >= ? "
			+ " AND system_txn_status = 1 "
			+ " AND is_game_txn_summarized = 0 "
			+ " GROUP BY website_type ";

		Timestamp today = TimePeriodType.TODAY.getDuration()[0];

		Map<WebSiteType, Integer> resultMap = new EnumMap<>(WebSiteType.class);

		ResultSetProcessor processor = (index, rs) -> {
			WebSiteType webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

			resultMap.put(webSiteType, rs.getInt("count"));
		};

		DBQueryRunner.processResultSet(conn, processor, sql, today);

		return resultMap;
	}

	public static String findNotUnsettledBetDetailsByMultiCondition(Connection conn, GameTransaction condition,
		Timestamp startDate, Timestamp endDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo, LangMessage lang, TimeZone timeZone) throws Exception {

		String selectSql =
			"SELECT gt.id, gt.txn_time, gt.settle_time, gt.create_time, "
				+ "gt.vendor_id, v.name AS vendor_name, "
				+ "gt.game_id, g.name AS game_name, g.name_en AS game_name_en, gt.game_type, "
				+ "gt.bet_amount, gt.profit_loss, gt.turnover, gt.system_txn_status, gt.vendor_txn_id, "
				+ "gt.odds, gt.odds_type, gt.bonus_turnover_id, "
				+ "gt.user_id, gt.website_type, "
				+ "CASE WHEN gt.bonus_turnover_id < 0 THEN 0 ELSE 1 END AS is_bonus_wallet ";

		List<Object> paramsList = new ArrayList<>();

		Function<String, String> sharedSql = (tableName) -> {
			StringBuilder str = new StringBuilder("FROM " + tableName);
			str.append("LEFT JOIN game g ON g.id = gt.game_id ");
			str.append("LEFT JOIN vendor v ON v.id = gt.vendor_id ");
			str.append("WHERE gt.user_id = ? AND gt.website_type = ? ");
			paramsList.add(condition.getUserId());
			paramsList.add(condition.getWebsiteType());

			if (condition.getIsGameTxnSummarized() == GameTxnSummaryType.SUMMARIZED.unique()) {
				str.append("AND gt.is_game_txn_summarized = ? ");
				paramsList.add(condition.getIsGameTxnSummarized());
			}

			str.append("AND gt.settle_time >= ? ");
			paramsList.add(startDate);

			if (null != endDate) {
				str.append("AND gt.settle_time <= ? ");
				paramsList.add(endDate);
			} else {
				str.append("AND gt.settle_time <= SYSTIMESTAMP ");
			}

			str.append("AND gt.system_txn_status <> ? ");
			paramsList.add(SystemTxnStatusType.UNSETTLED.unique());

			if (condition.getVendorId() != 0) {
				str.append("AND gt.vendor_id = ? ");
				paramsList.add(condition.getVendorId());
			}

			if (condition.getGameType() != 0) {
				str.append("AND gt.game_type = ? ");
				paramsList.add(condition.getGameType());
			}

			return str.toString();
		};
		String sharedSqlTxn = sharedSql.apply(" gameTransaction gt ");
		String unionSql = selectSql + sharedSqlTxn;

		String countSql = "SELECT COUNT(*) " + sharedSqlTxn;
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String subSummarySql = "SELECT SUM(bet_amount) bet_amount, "
			+ " SUM(CASE WHEN system_txn_status = ? THEN profit_loss ELSE 0 END) profit_loss, "
			+ " SUM(CASE WHEN system_txn_status = ? THEN turnover ELSE 0 END) turnover "
			+ " FROM ( " + unionSql + ")";

		List<Object> subSummaryParamsList = new ArrayList<>();
		subSummaryParamsList.add(SystemTxnStatusType.SETTLED.unique());
		subSummaryParamsList.add(SystemTxnStatusType.SETTLED.unique());
		subSummaryParamsList.addAll(paramsList);

		GameTransaction totalGameTransaction = DBQueryRunner
			.getBean(conn, GameTransaction.class, subSummarySql, subSummaryParamsList);

		if (sortCondition.equalsIgnoreCase("game_id")) {
			sortCondition = "game_name_en";
		} else if (sortCondition.equalsIgnoreCase("vendor_id")) {
			sortCondition = "vendor_name";
		}

		String joinWallet = "SELECT g.*, w.BALANCE_BEFORE, w.BALANCE_AFTER FROM ( " + unionSql + ")  g "
			+ " LEFT JOIN "
			+ " ( SELECT wt.reference_id, "
			+ " MAX(CASE WHEN UPPER(wt.description) = 'BET PLACED' THEN wt.balance_before END) AS BALANCE_BEFORE, "
			+ " MAX(CASE WHEN UPPER(wt.description) IN ('BET LOSS','BET WIN') THEN wt.balance_after END) AS BALANCE_AFTER "
			+ " FROM WalletTransaction wt "
			+ " WHERE UPPER(wt.description) IN ('BET PLACED','BET LOSS','BET WIN') "
			+ " GROUP BY wt.reference_id "
			+ " ) w "
			+ " ON g.vendor_txn_id = w.reference_id "
			+ " ORDER BY " + sortCondition + " " + orderType.getSqlString();

		String sql = OracleUtils.getCalculatedPageSQL(joinWallet);
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
				processResultSetForBetDetails(jGenerator, rs, lang, timeZone);
			}
			jGenerator.writeEndArray();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.closeAll(ps, rs);
			JSONUtils.close(jGenerator);
		}

		String dataTableJson = pageInfo.getDataTableJson(out.toString());
		JSONObject mainObject = new JSONObject(dataTableJson);

		Map<String, BigDecimal> hashMap = new HashMap<>();
		hashMap.put("totalBetAmount", totalGameTransaction.getBetAmount());
		hashMap.put("totalProfitLoss", totalGameTransaction.getProfitLoss());
		hashMap.put("totalTurnover", totalGameTransaction.getTurnover());
		mainObject.accumulate(PageUtils.TOTAL_AMOUNT, new JSONObject(hashMap));

		return mainObject.toString();
	}

	public static String findUnsettledBetDetailsByMultiCondition(Connection conn, String userId, WebSiteType webSiteType,
		String[] gameTypes, String[] vendorIds, Timestamp startDate, Timestamp endDate, String sortCondition,
		DBOrderType orderType, PageInfo pageInfo, LangMessage lang, TimeZone timeZone) throws Exception {

		String selectSql =
			"SELECT gt.id, gt.txn_time, NULL settle_time, gt.create_time, "
				+ "gt.vendor_id, v.name AS vendor_name, "
				+ "gt.game_id, g.name AS game_name, g.name_en AS game_name_en, gt.game_type, "
				+ "gt.bet_amount, gt.profit_loss, gt.turnover, gt.system_txn_status, gt.vendor_txn_id, "
				+ "gt.odds, gt.odds_type, gt.bonus_turnover_id, "
				+ "gt.user_id, gt.website_type, "
				+ "CASE WHEN gt.bonus_turnover_id < 0 THEN 0 ELSE 1 END AS is_bonus_wallet ";

		List<Object> paramsList = new ArrayList<>();

		Function<String, String> sharedSql = (tableName) -> {
			StringBuilder str = new StringBuilder("FROM " + tableName);
			str.append("LEFT JOIN game g ON g.id = gt.game_id ");
			str.append("LEFT JOIN vendor v ON v.id = gt.vendor_id ");
			str.append("WHERE ");

			if (!StringUtils.isEmpty(userId)) {
				str.append("gt.user_id = ? AND ");
				paramsList.add(userId);
			}

			if (webSiteType != null) {
				str.append("gt.website_type = ? AND ");
				paramsList.add(webSiteType.unique());
			}

			str.append("gt.is_game_txn_summarized = ? ");
			paramsList.add(GameTxnSummaryType.NOT_SUMMARIZED.unique());

			str.append("AND gt.txn_time >= ? ");
			paramsList.add(startDate);

			if (null != endDate) {
				str.append("AND gt.txn_time <= ? ");
				paramsList.add(endDate);
			} else {
				str.append("AND gt.txn_time <= SYSTIMESTAMP ");
			}

			str.append("AND gt.system_txn_status = ? ");
			paramsList.add(SystemTxnStatusType.UNSETTLED.unique());

			if (gameTypes != null && gameTypes.length != 0) {
				str.append("AND gt.game_type in (").append(StringUtils.repeat("?", ",", gameTypes.length)).append(") ");
				paramsList.addAll(Arrays.asList(gameTypes));
			}

			if (vendorIds != null && vendorIds.length != 0) {
				str.append("AND gt.vendor_id in (").append(StringUtils.repeat("?", ",", vendorIds.length)).append(") ");
				paramsList.addAll(Arrays.asList(vendorIds));
			}

			return str.toString();
		};
		String sharedSqlTxn = sharedSql.apply(" gameTransaction gt ");

		String unionSql = selectSql + sharedSqlTxn;

		String countSql = "SELECT COUNT(*) " + sharedSqlTxn;
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String subSummarySql = "SELECT SUM(bet_amount) bet_amount FROM ( " + unionSql + ")";
		Number totalBetAmount = DBQueryRunner.getNumberWithDefault(conn, subSummarySql, BigDecimal.ZERO, paramsList);

		String joinWallet = "SELECT g.*, w.* FROM ( " + unionSql + ") g "
			+ " LEFT JOIN "
			+ " ( SELECT wt.reference_id, wt.transaction_type, wt.status "
			+ " FROM WalletTransaction wt "
			+ " WHERE UPPER(wt.description) IN ('BET PLACED') "
//			+ " GROUP BY wt.reference_id "
			+ " ) w "
			+ " ON g.vendor_txn_id = w.reference_id "
			+ " ORDER BY " + sortCondition + " " + orderType.getSqlString();

		String sql = OracleUtils.getCalculatedPageSQL(joinWallet);
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
				processResultSetForBetDetails(jGenerator, rs, lang, timeZone);
			}
			jGenerator.writeEndArray();

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.closeAll(ps, rs);
			JSONUtils.close(jGenerator);
		}

		String dataTableJson = pageInfo.getDataTableJson(out.toString());
		JSONObject mainObject = new JSONObject(dataTableJson);

		Map<String, BigDecimal> hashMap = new HashMap<>();
		hashMap.put("totalBetAmount", BigDecimal.valueOf(totalBetAmount.doubleValue()));
		mainObject.accumulate(PageUtils.TOTAL_AMOUNT, new JSONObject(hashMap));

		return mainObject.toString();
	}

	public static String findTxnById(Connection conn, long id, WebSiteType webSiteType, Timestamp txnTime,
		Timestamp settleTime, LangMessage lang, TimeZone timeZone, int currencyType) throws Exception {

		StringBuilder sql = new StringBuilder("SELECT * FROM gametransaction ");

		List<Object> params = new ArrayList<>();

		String condition = " WHERE id = ? AND website_type = ? ";
		params.add(id);
		params.add(webSiteType.unique());

		if (settleTime != null) {
			condition += " AND settle_time >= ? AND settle_time <= ? ";
			params.add(DateTimeBuilder.localDateTime(settleTime).withMinTime().toTimestamp());
			params.add(DateTimeBuilder.localDateTime(settleTime).withMaxTime().toTimestamp());
		} else {
			condition += " AND txn_time >= ? AND txn_time <= ? ";
			params.add(DateTimeBuilder.localDateTime(txnTime).withMinTime().toTimestamp());
			params.add(DateTimeBuilder.localDateTime(txnTime).withMaxTime().toTimestamp());
		}

		sql.append(condition);

		GameTransaction gameTxn = DBQueryRunner.getBean(conn, GameTransaction.class, sql.toString(), params);

		if (gameTxn == null) {
			return null;
		}

		String getWalletInfoSql = " SELECT "
			+ " MAX(CASE WHEN UPPER(wt.description) = 'BET PLACED' THEN wt.balance_before END) AS BALANCE_BEFORE, "
			+ " MAX(CASE WHEN UPPER(wt.description) IN ('BET LOSS','BET WIN') THEN wt.balance_after END) AS BALANCE_AFTER "
			+ " FROM WalletTransaction wt "
			+ " WHERE REFERENCE_ID = ? "
			+ " AND UPPER(DESCRIPTION) IN ('BET PLACED','BET LOSS','BET WIN') "
			+ " GROUP BY REFERENCE_ID ";

		BigDecimal balanceBefore = BigDecimal.ZERO;
		BigDecimal balanceAfter = BigDecimal.ZERO;
		Map<String, Object> walletInfo = DBQueryRunner.queryForMap(conn, getWalletInfoSql, gameTxn.getVendorTxnId());
		if (walletInfo != null && !walletInfo.isEmpty()) {
			balanceBefore = (BigDecimal) walletInfo.get("BALANCE_BEFORE");
			balanceAfter = (BigDecimal) walletInfo.get("BALANCE_AFTER");
		}

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try {
			String oddsType = Optional.ofNullable(OddsType.getInstance(gameTxn.getOddsType()))
				.map(OddsType::name)
				.orElse("");

			WebsiteVendor websiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, gameTxn.getVendorId());

			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("gameTxn");
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("txnID", gameTxn.getId());
			jGenerator.writeStringField("userID", gameTxn.getUserId());
			jGenerator.writeStringField("websiteType", WebSiteType.getInstance(gameTxn.getWebsiteType()).name());
			jGenerator.writeStringField("vendorName", websiteVendor.getDisplayName());
			jGenerator.writeNumberField("vendorID", websiteVendor.getVendorId());
			jGenerator.writeNumberField("txnTime", gameTxn.getTxnTime().getTime());
			jGenerator.writeStringField("txnTimeStr",
				FormatUtils.dateFormatWithTimeZone(gameTxn.getTxnTime(), timeZone));
			jGenerator.writeNumberField("balanceBefore", balanceBefore);
			jGenerator.writeNumberField("betAmount", gameTxn.getBetAmount());
			jGenerator.writeNumberField("balanceAfter", balanceAfter);
			jGenerator.writeNumberField("winAmount", gameTxn.getWinAmount());
			jGenerator.writeStringField("currency", CurrencyType.valueOf(gameTxn.getCurrency()).getFullName(lang));
			jGenerator.writeStringField("vendorTxnID", gameTxn.getVendorTxnId());
			jGenerator.writeNumberField("vendorTxnLastModifyTime",
				gameTxn.getSettleTime() == null ? 0 : gameTxn.getSettleTime().getTime());
			jGenerator.writeStringField("vendorTxnLastModifyTimeStr",
				FormatUtils.dateFormatWithTimeZone(gameTxn.getSettleTime(), timeZone));
			Timestamp startTime = DateTimeBuilder.localDateTime(gameTxn.getSettleTime()).withMinTime().toTimestamp();
			Timestamp endTime = DateTimeBuilder.localDateTime(gameTxn.getSettleTime()).withMaxTime().toTimestamp();
			String text = ReferenceDataBO.getText(gameTxn.getReferenceKey(), startTime, endTime);
			jGenerator.writeStringField("gameInfoJson", Validator.stripJson(text));
			jGenerator.writeStringField("gameInfoJson", text);
			jGenerator.writeNumberField("createTime", gameTxn.getCreateTime().getTime());
			jGenerator.writeStringField("createTimeStr",
				FormatUtils.dateFormatWithTimeZone(gameTxn.getCreateTime(), timeZone));
			jGenerator.writeNumberField("updateTime", gameTxn.getUpdateTime().getTime());
			jGenerator.writeStringField("updateTimeStr",
				FormatUtils.dateFormatWithTimeZone(gameTxn.getUpdateTime(), timeZone));
			Integer gameType = gameTxn.getGameType();
			jGenerator.writeNumberField("gameTypeUnique", gameType == null ? 0 : gameType);
			jGenerator.writeStringField("gameType",
				gameType != null ? GameType.getInstance(gameType).getFullName(lang) : "");
			jGenerator.writeNumberField("gameTypeId",  gameType);
			jGenerator.writeNumberField("turnover", gameTxn.getTurnover());
			jGenerator.writeStringField("txnStatus", gameTxn.getTxnStatus());
			jGenerator.writeNumberField("realBetAmount", gameTxn.getRealBetAmount());
			jGenerator.writeNumberField("adjustAmount", gameTxn.getAdjustAmount());
			jGenerator.writeNumberField("progressBetAmount", gameTxn.getProgressBetAmount());
			jGenerator.writeNumberField("progressProfitLoss", gameTxn.getProgressProfitLoss());

			Integer gameID = gameTxn.getGameId();
			jGenerator.writeStringField("gameID", gameID != null ? String.valueOf(gameID) : "");
			Game game = GameCache.getInstance().getGameById(gameTxn.getGameId());
			String gameName = game.getName();
			String gameNameEN = Validator.isEmpty(game.getNameEn()) ? gameName : game.getNameEn();
			jGenerator.writeStringField("gameName", gameName != null ? gameName : "");
			jGenerator.writeStringField("gameNameEn", gameNameEN != null ? gameNameEN : "");
			jGenerator.writeNumberField("vipLevel", gameTxn.getVipLevel());
			jGenerator.writeStringField("isTurnoverSummarized", gameTxn.getIsTurnoverSummarized() == 1 ? "Yes" : "No");
			jGenerator.writeNumberField("profitLoss", gameTxn.getProfitLoss());
			jGenerator.writeNumberField("systemTxnStatus", gameTxn.getSystemTxnStatus());
			jGenerator.writeStringField("systemTxnStatusName",
				SystemTxnStatusType.getInstance(gameTxn.getSystemTxnStatus()).getSimpleName());

			// Hyper link
			ProviderProxy proxy = ProviderProxyCache.getInstance()
				.getProviderProxy(webSiteType, websiteVendor.getWebsiteProviderId(), CurrencyType.getInstance(currencyType));
			jGenerator.writeStringField("resultUrl", proxy.getGameTxnResultUrl(gameTxn));

			jGenerator.writeNumberField("odds", gameTxn.getOdds());
			jGenerator.writeStringField("oddsType", oddsType);
			jGenerator.writeStringField("recordId", gameTxn.getFcRecordId());
			jGenerator.writeEndObject();
			jGenerator.writeEndArray();
			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}

		return out.toString();
	}

	public static Map<WebSiteType, List<Timestamp>> getBeforeDate(Connection conn, Timestamp endDate)
		throws SQLException {

		String sql = "SELECT website_type, TRUNC(settle_time, 'dd') AS settle_time "
			+ "FROM gameTransaction WHERE system_txn_status <> ? AND settle_time < ? "
			+ "GROUP BY website_type, TRUNC(settle_time, 'dd')";

		Map<WebSiteType, List<Timestamp>> result = new HashMap<>();

		ResultSetProcessor processor = (index, rs) -> result.computeIfAbsent(
				WebSiteType.getInstance(rs.getInt("website_type")), website -> new ArrayList<>())
			.add(rs.getTimestamp("settle_time"));

		DBQueryRunner
			.processResultSet(conn, processor, sql, SystemTxnStatusType.UNSETTLED.unique(), endDate);

		return result;
	}

	public static Map<GameTransactionSummaryHourly, List<Long>> summarizeHourly(Connection conn, Timestamp endDateTime)
		throws SQLException {

		String sql = "SELECT website_type, user_id, vendor_id, game_type, currency, "
			+ "COUNT(*) as bet_count, ? AS summary_date, "
			+ "SUM(bet_amount) AS sum_bet_amount, "
			+ "SUM(CASE WHEN system_txn_status = ? THEN profit_loss ELSE 0 END) AS profit, "
			+ "SUM(CASE WHEN system_txn_status = ? THEN turnover ELSE 0 END) AS turnover, "
			+ "SUM(bet_amount) AS progress_bet_amount, SUM(profit_loss) AS progress_profit_loss, "
			+ "LISTAGG(id, ',') WITHIN GROUP (ORDER BY id) AS ids "
			+ "FROM gameTransaction WHERE system_txn_status <> ? AND settle_time < ? AND IS_GAME_TXN_SUMMARIZED = 0 "
			+ "GROUP BY website_type, user_id, vendor_id, game_type, currency ";

		Map<GameTransactionSummaryHourly, List<Long>> result = new HashMap<>();

		ResultSetProcessor processor = (index, rs) -> {
			GameTransactionSummaryHourly summaryHourly = new GameTransactionSummaryHourly();
			summaryHourly.setWebsiteType(rs.getInt("website_type"));
			summaryHourly.setUserId(rs.getString("user_id"));
			summaryHourly.setVendorId(rs.getInt("vendor_id"));
			summaryHourly.setGameType(rs.getInt("game_type"));
			summaryHourly.setCurrency(rs.getString("currency"));
			summaryHourly.setBetCount(rs.getInt("bet_count"));
			summaryHourly.setSummaryDate(rs.getTimestamp("summary_date"));
			summaryHourly.setSumBetAmount(rs.getBigDecimal("sum_bet_amount"));
			summaryHourly.setTurnover(rs.getBigDecimal("turnover"));
			summaryHourly.setProfit(rs.getBigDecimal("profit"));
			summaryHourly.setProgressBetAmount(rs.getBigDecimal("progress_bet_amount"));
			summaryHourly.setProgressProfitLoss(rs.getBigDecimal("progress_profit_loss"));

			String idsStr = rs.getString("ids");
			List<Long> ids = Arrays.stream(idsStr.split(","))
				.map(Long::valueOf)
				.collect(Collectors.toList());

			result.put(summaryHourly, ids);
		};

		DBQueryRunner
			.processResultSet(conn, processor, sql, endDateTime, SystemTxnStatusType.SETTLED.unique(),
				SystemTxnStatusType.SETTLED.unique(), SystemTxnStatusType.UNSETTLED.unique(), endDateTime);

		return result;
	}

	public static void delete(Connection conn, List<Long> ids)
		throws SQLException {

		String sql = "DELETE FROM gametransaction WHERE id IN (" + OracleUtils.getGroupCondition(ids.size()) + ") ";
		DBQueryRunner.update(conn, sql, OracleUtils.getOracleARRAY(conn, "INTEGER_ARRAY", ids.toArray()));
	}

	public static List<Long> getIdByDate(Connection conn, Timestamp startTime, Timestamp endTime)
		throws SQLException {

		String sql = "SELECT id FROM gameTransaction WHERE settle_time >= ? AND settle_time < ? AND system_txn_status <> ? ";

		List<Long> result = new ArrayList<>();
		ResultSetProcessor processor = (index, rs) -> result.add(rs.getLong("id"));

		DBQueryRunner
			.processResultSet(conn, processor, sql, startTime, endTime, SystemTxnStatusType.UNSETTLED.unique());

		return result;
	}

	public static int[] updateIsSummarized(Connection conn, List<Long> ids) throws SQLException {
		String sql = " UPDATE gameTransaction SET IS_GAME_TXN_SUMMARIZED = ?, UPDATE_TIME = SYSTIMESTAMP "
			+ "WHERE id = ? ";

		List<Object[]> values = ids.stream().map(id -> new Object[] {
			GameTxnSummaryType.SUMMARIZED.unique(), id
		}).collect(Collectors.toList());

		return DBQueryRunner.batch(conn, sql, values);
	}

	public static String findBetReport(Connection conn, String userKey, int websiteTypeId,
		Timestamp transactionStartDate, Timestamp transactionEndDate,
		BigDecimal minAmount, BigDecimal maxAmount, String sortField, DBOrderType sortOder
	) throws Exception {
		ArrayList<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append(" WITH g AS ( ");
		sql.append(" SELECT /*+ MATERIALIZE */ ID, USER_ID, TXN_TIME, SETTLE_TIME, CREATE_TIME, ");
		sql.append(" VENDOR_ID, VENDOR_NAME, GAME_TYPE, GAME_ID, VENDOR_TXN_ID, ");
		sql.append(" BET_AMOUNT, PROFIT_LOSS, TURNOVER, TXN_STATUS ");
		sql.append(" FROM GAMETRANSACTION");
		sql.append(" WHERE WEBSITE_TYPE = ? ");
		params.add(websiteTypeId);

		if (userKey != null && !userKey.trim().isEmpty()) {
			sql.append(" AND LOWER(user_id) like ? ");
			params.add("%" + userKey.trim().toLowerCase() + "%");
		}

		if (transactionStartDate != null) {
			sql.append(" AND txn_time >= ? ");
			params.add(transactionStartDate);
		}

		if (transactionEndDate != null) {
			sql.append(" AND txn_time <= ? ");
			params.add(transactionEndDate);
		}

		if (minAmount != null) {
			sql.append(" AND profit_loss >= ? ");
			params.add(minAmount);
		}

		if (maxAmount != null) {
			sql.append(" AND profit_loss <= ? ");
			params.add(maxAmount);
		}

		sql.append(" ), ");
		sql.append(" w AS (");
		sql.append(" SELECT /*+ MATERIALIZE */ wt.REFERENCE_ID, ");
		sql.append(
			" MAX(CASE WHEN UPPER(wt.description) = 'BET PLACED' THEN wt.balance_before END) AS BALANCE_BEFORE, ");
		sql.append(
			" MAX(CASE WHEN UPPER(wt.description) IN ('BET LOSS','BET WIN') THEN wt.balance_after END) AS BALANCE_AFTER ");
		sql.append(" FROM WalletTransaction wt ");
		sql.append(" JOIN (SELECT DISTINCT VENDOR_TXN_ID FROM g) t ON t.VENDOR_TXN_ID = wt.REFERENCE_ID ");
		sql.append(" WHERE UPPER(wt.description) IN ('BET PLACED','BET LOSS','BET WIN') ");
		sql.append(" GROUP BY wt.REFERENCE_ID ");
		sql.append(" ) ");
		sql.append(" SELECT ");
		sql.append(" g.ID, g.USER_ID, g.TXN_TIME, g.SETTLE_TIME, g.CREATE_TIME, ");
		sql.append(" g.VENDOR_ID, g.VENDOR_NAME, g.GAME_TYPE, g.GAME_ID, g.VENDOR_TXN_ID, ");
		sql.append(" w.BALANCE_BEFORE, g.BET_AMOUNT, g.PROFIT_LOSS, w.BALANCE_AFTER, g.TURNOVER, ");
		sql.append(" g.TXN_STATUS ");
		sql.append(" FROM g ");
		sql.append(" LEFT JOIN w ON w.REFERENCE_ID = g.VENDOR_TXN_ID ");

		if (sortField != null && !sortField.trim().isEmpty()) {
			sql.append(" ORDER BY ").append(sortField).append(" ").append(sortOder.getSqlString())
				.append(" NULLS LAST ");
		} else {
			sql.append(" ORDER BY g.CREATE_TIME DESC NULLS LAST ");
		}

		JsonValueProcessor memberProcessor = generateProcessor(websiteTypeId);
		return DBQueryRunner.processJsonArrayValue(conn, memberProcessor, sql.toString(), params);
	}

	private static JsonValueProcessor generateProcessor(int webSiteType) {
		JsonValueProcessor processor = (index, rs, jGenerator) -> {

			String userId = rs.getString("user_id");
			String gameId = rs.getString("game_id");
			int vendorId = rs.getInt("vendor_id");
			int gameType = rs.getInt("game_type");
			String oddsType = Optional.ofNullable(OddsType.getInstance(rs.getInt("odds_type")))
				.map(OddsType::name)
				.orElse("");

			jGenerator.writeStartObject();

			if (VendorCache.getInstance()
				.getWebSiteVendor(WebSiteType.getInstance(webSiteType), vendorId) != null) {
				String vendorName = VendorCache.getInstance()
					.getWebSiteVendor(WebSiteType.getInstance(webSiteType), vendorId).getDisplayName();
				jGenerator.writeStringField("vendorName", vendorName);
			}

			if (GameCache.getInstance().getGameById(Integer.parseInt(gameId)) != null) {
				String gameName = GameCache.getInstance().getGameById(Integer.parseInt(gameId)).getNameEn();
				jGenerator.writeStringField("gameName", gameName);
			}

			jGenerator.writeStringField("userId", userId);
			jGenerator.writeStringField("vendorTransactionId", rs.getString("vendor_txn_id"));
			jGenerator.writeStringField("id", rs.getString("id"));
			jGenerator.writeNumberField("profitLoss", rs.getBigDecimal("profit_loss"));
			jGenerator.writeNumberField("betAmount", rs.getBigDecimal("bet_amount"));
			jGenerator.writeNumberField("turnover", rs.getBigDecimal("turnover"));
			jGenerator.writeStringField("gameId", gameId);

			GameType gameTypeEnum = GameType.getInstance(gameType);

			jGenerator.writeStringField("gameType", gameTypeEnum.getName());

			jGenerator.writeNumberField("vendorId", vendorId);


			Timestamp transactionTime = rs.getTimestamp("txn_time");
			if(transactionTime != null) {
				jGenerator.writeNumberField("transactionTime", transactionTime.getTime());
			}

			Timestamp settleTime = rs.getTimestamp("settle_time");
			if(settleTime != null) {
				jGenerator.writeNumberField("settleTime",settleTime.getTime());
			}

			Timestamp createTime = rs.getTimestamp("create_time");
			if(createTime != null) {
				jGenerator.writeNumberField("createTime", createTime.getTime());
			}

			jGenerator.writeNumberField("balanceBefore", rs.getBigDecimal("balance_before"));
			jGenerator.writeNumberField("balanceAfter", rs.getBigDecimal("balance_after"));
			jGenerator.writeStringField("transactionStatus", rs.getString("txn_status"));
			jGenerator.writeStringField("txnStatus", rs.getString("system_txn_status"));

			jGenerator.writeNumberField("odds", rs.getBigDecimal("odds"));
			jGenerator.writeStringField("oddsType", oddsType);

			jGenerator.writeEndObject();
		};
		return processor;
	}

	/**
	 * Find bet report with pagination following the pattern of getMemberJsonByMultiCondition
	 * Returns JSON array string with paginated results
	 */
	/**
	 * Find bet report grand totals (sum of all records matching search criteria)
	 * Returns map with TOTAL_BET_AMOUNT, TOTAL_PROFIT_LOSS, TOTAL_TURNOVER
	 */
	public static Map<String, Object> findBetReportGrandTotals(
		Connection conn,
		String userKey,
		int websiteTypeId,
		int systemTxnStatus,
		Timestamp transactionStartDate,
		Timestamp transactionEndDate,
		BigDecimal minAmount,
		BigDecimal maxAmount
	) throws Exception {
		ArrayList<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT ");
		sql.append(" COALESCE(SUM(BET_AMOUNT), 0) AS TOTAL_BET_AMOUNT, ");
		sql.append(" COALESCE(SUM(CASE WHEN SYSTEM_TXN_STATUS = ? THEN PROFIT_LOSS ELSE 0 END), 0) AS TOTAL_PROFIT_LOSS, ");
		sql.append(" COALESCE(SUM(CASE WHEN SYSTEM_TXN_STATUS = ? THEN TURNOVER ELSE 0 END), 0) AS TOTAL_TURNOVER ");
		sql.append(" FROM GAMETRANSACTION ");
		params.add(SystemTxnStatusType.SETTLED.unique());
		params.add(SystemTxnStatusType.SETTLED.unique());

		conditionsForFindBetReport(sql, params, websiteTypeId, userKey, systemTxnStatus,
			transactionStartDate, transactionEndDate, minAmount, maxAmount);

		return DBQueryRunner.queryForMap(conn, sql.toString(), params.toArray());
	}

	public static String findBetReportWithPagination(
		Connection conn,
		String userKey,
		int websiteTypeId,
		int systemTxnStatus,
		Timestamp transactionStartDate,
		Timestamp transactionEndDate,
		BigDecimal minAmount,
		BigDecimal maxAmount,
		String sortField,
		DBOrderType sortOrder,
		PageInfo pageInfo
	) throws Exception {
		ArrayList<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append(" WITH g AS ( ");
		sql.append(" SELECT /*+ MATERIALIZE */ ID, USER_ID, TXN_TIME, SETTLE_TIME, CREATE_TIME, ");
		sql.append(" VENDOR_ID, VENDOR_NAME, GAME_TYPE, GAME_ID, VENDOR_TXN_ID, ");
		sql.append(" BET_AMOUNT, PROFIT_LOSS, TURNOVER, SYSTEM_TXN_STATUS, TXN_STATUS, ODDS, ODDS_TYPE ");
		sql.append(" FROM GAMETRANSACTION ");

		conditionsForFindBetReport(sql, params, websiteTypeId, userKey, systemTxnStatus,
			transactionStartDate, transactionEndDate, minAmount, maxAmount);

		sql.append(" ), ");
		sql.append(" w AS (");
		sql.append(" SELECT /*+ MATERIALIZE */ wt.REFERENCE_ID, ");
		sql.append(
			" MAX(CASE WHEN UPPER(wt.description) = 'BET PLACED' THEN wt.balance_before END) AS BALANCE_BEFORE, ");
		sql.append(
			" MAX(CASE WHEN UPPER(wt.description) IN ('BET LOSS','BET WIN') THEN wt.balance_after END) AS BALANCE_AFTER ");
		sql.append(" FROM WalletTransaction wt ");
		sql.append(" JOIN (SELECT DISTINCT VENDOR_TXN_ID FROM g) t ON t.VENDOR_TXN_ID = wt.REFERENCE_ID ");
		sql.append(" WHERE UPPER(wt.description) IN ('BET PLACED','BET LOSS','BET WIN') ");
		sql.append(" GROUP BY wt.REFERENCE_ID ");
		sql.append(" ) ");
		sql.append(" SELECT ");
		sql.append(" g.ID, g.USER_ID, g.TXN_TIME, g.SETTLE_TIME, g.CREATE_TIME, ");
		sql.append(" g.VENDOR_ID, g.VENDOR_NAME, g.GAME_TYPE, g.GAME_ID, g.VENDOR_TXN_ID, ");
		sql.append(" w.BALANCE_BEFORE, w.BALANCE_AFTER, ");
		sql.append(" g.SYSTEM_TXN_STATUS, g.TXN_STATUS, g.ODDS, g.ODDS_TYPE, gm.NAME AS GAME_NAME, ");
		sql.append(" g.BET_AMOUNT, ");
		sql.append(" (CASE WHEN g.SYSTEM_TXN_STATUS = ? THEN g.PROFIT_LOSS ELSE 0 END) AS PROFIT_LOSS, ");
		sql.append(" (CASE WHEN g.SYSTEM_TXN_STATUS = ? THEN g.TURNOVER ELSE 0 END) AS TURNOVER ");
		sql.append(" FROM g ");
		sql.append(" LEFT JOIN w ON w.REFERENCE_ID = g.VENDOR_TXN_ID ");
		sql.append(" LEFT JOIN GAME gm ON gm.ID = g.GAME_ID ");
		// TODO: do we really need this filter?
		params.add(SystemTxnStatusType.SETTLED.unique());
		params.add(SystemTxnStatusType.SETTLED.unique());

		// Execute count query
		String countSql = " SELECT COUNT(*) FROM ( " + sql + ")";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, params).intValue();
		pageInfo.setTotalCount(totalCount);

		// Add sorting
		if (sortField != null && !sortField.trim().isEmpty()) {
			sql.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder.getSqlString())
				.append(" NULLS LAST ");
		} else {
			sql.append(" ORDER BY g.CREATE_TIME DESC NULLS LAST ");
		}

		// Add pagination parameters
		params.add(pageInfo.getLastRowNumber());
		params.add(pageInfo.getFirstRowNumber());

		// Wrap with Oracle pagination
		String pageSQL = OracleUtils.getCalculatedPageSQL(sql.toString());

		// Generate JSON using processor
		JsonValueProcessor processor = generateProcessor(websiteTypeId);
		return DBQueryRunner.processJsonArrayValue(conn, processor, pageSQL, params);
	}

	private static void conditionsForFindBetReport(StringBuilder sql, List<Object> params,
		int websiteTypeId, String userKey, int systemTxnStatus,
		Timestamp transactionStartDate, Timestamp transactionEndDate, BigDecimal minAmount, BigDecimal maxAmount) {

		sql.append(" WHERE WEBSITE_TYPE = ? ");
		params.add(websiteTypeId);

		if (userKey != null && !userKey.trim().isEmpty()) {
			sql.append(" AND LOWER(user_id) like ? ");
			params.add("%" + userKey.trim().toLowerCase() + "%");
		}

		if (systemTxnStatus == SystemTxnStatusType.UNSETTLED.unique()) {
			sql.append(" AND system_txn_status = ? ");
			params.add(systemTxnStatus);

			if (transactionStartDate != null) {
				sql.append(" AND txn_time >= ? ");
				params.add(transactionStartDate);
			}
			if (transactionEndDate != null) {
				sql.append(" AND txn_time <= ? ");
				params.add(transactionEndDate);
			}
		} else {
			sql.append(" AND system_txn_status <> ? ");
			params.add(SystemTxnStatusType.UNSETTLED.unique());

			if (transactionStartDate != null) {
				sql.append(" AND settle_time >= ? ");
				params.add(transactionStartDate);
			}
			if (transactionEndDate != null) {
				sql.append(" AND settle_time <= ? ");
				params.add(transactionEndDate);
			}
		}

		if (minAmount != null) {
			sql.append(" AND profit_loss >= ? ");
			params.add(minAmount);
		}

		if (maxAmount != null) {
			sql.append(" AND profit_loss <= ? ");
			params.add(maxAmount);
		}
	}

	private static void processResultSetForBetDetails(JsonGenerator jGenerator, ResultSet rs,
		LangMessage lang, TimeZone timeZone) throws Exception {

		WebSiteType webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));
		int vendorId = rs.getInt("vendor_id");
		String gameName =
			Validator.isEmpty(rs.getString("game_name")) ? "-" : rs.getString("game_name");
		String gameNameEn =
			Validator.isEmpty(rs.getString("game_name_en")) ? "-" : rs.getString("game_name_en");
		Number dbGameType = (Number) rs.getObject("game_type");
		String oddsType = Optional.ofNullable(OddsType.getInstance(rs.getInt("odds_type")))
			.map(OddsType::name)
			.orElse("");
		BetType betType = BetType.getInstance(rs.getInt("bonus_turnover_id"));
		SystemTxnStatusType systemTxnStatusType = SystemTxnStatusType.getInstance(rs.getInt("system_txn_status"));
		long settleTimestampTime =
			systemTxnStatusType == SystemTxnStatusType.UNSETTLED ? 0 : rs.getTimestamp("settle_time").getTime();
		BigDecimal balanceBefore =
			systemTxnStatusType == SystemTxnStatusType.UNSETTLED ? BigDecimal.ZERO : rs.getBigDecimal("BALANCE_BEFORE");
		BigDecimal balanceAfter =
			systemTxnStatusType == SystemTxnStatusType.UNSETTLED ? BigDecimal.ZERO : rs.getBigDecimal("BALANCE_AFTER");

		WebsiteVendor websiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, vendorId);

		jGenerator.writeStartObject();

		jGenerator.writeStringField("txnTimeZone",
			FormatUtils.dateFormatWithTimeZone(rs.getTimestamp("txn_time"), timeZone));
		jGenerator.writeStringField("settleTimeZone",
			FormatUtils.dateFormatWithTimeZone(rs.getTimestamp("settle_time"), timeZone));
		jGenerator.writeStringField("createTimeZone",
			FormatUtils.dateFormatWithTimeZone(rs.getTimestamp("create_time"), timeZone));
		jGenerator.writeStringField("txnTime",
			FormatUtils.dateFormat(rs.getTimestamp("txn_time"), FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd));
		jGenerator.writeStringField("settleTime",
			FormatUtils.dateFormat(rs.getTimestamp("settle_time"), FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd));
		jGenerator.writeNumberField("txnTimestamp", rs.getTimestamp("txn_time").getTime());
		jGenerator.writeNumberField("settleTimestamp", settleTimestampTime);
		jGenerator.writeNumberField("createTimestamp", rs.getTimestamp("create_time").getTime());

		jGenerator.writeNumberField("vendorId", vendorId);
		jGenerator.writeStringField("vendorName", websiteVendor.getDisplayName());
		jGenerator.writeStringField("gameName", gameName);
		jGenerator.writeStringField("gameNameEn", gameNameEn);
		jGenerator.writeStringField("gameType", GameType.getInstance(dbGameType.intValue()).getFullName(lang));
		jGenerator.writeNumberField("gameTypeId", dbGameType.intValue());

		jGenerator.writeNumberField("balanceBefore", balanceBefore);
		jGenerator.writeNumberField("betAmount", rs.getBigDecimal("bet_amount"));
		jGenerator.writeNumberField("profitLoss", rs.getBigDecimal("profit_loss"));
		jGenerator.writeNumberField("balanceAfter", balanceAfter);
		jGenerator.writeNumberField("turnover", rs.getBigDecimal("turnover"));
		jGenerator.writeNumberField("id", rs.getLong("id"));
		jGenerator.writeStringField("txnStatus", String.valueOf(systemTxnStatusType.unique()));
		jGenerator.writeStringField("vendorTxnId", rs.getString("vendor_txn_id"));
		jGenerator.writeNumberField("odds", rs.getBigDecimal("odds"));
		jGenerator.writeStringField("oddsType", oddsType);
		jGenerator.writeNumberField("isBonusWallet", rs.getLong("is_bonus_wallet"));

		jGenerator.writeStringField("betType", betType.getName());
		jGenerator.writeNumberField("paymentType", AccountSummaryReportType.BET.unique());

		jGenerator.writeEndObject();
	}

	public static List<GameTransaction> findOddsAndOddsTypeIsNULLRecords(Connection conn, int webSiteType, int vendorId)
		throws SQLException {
		String sql = "SELECT * FROM GameTransaction "
			+ "WHERE website_type = ? AND vendor_id = ? AND odds IS NULL AND odds_type IS NULL ";
		return DBQueryRunner.getBeanList(conn, GameTransaction.class, sql, webSiteType, vendorId);
	}

	public static int updateOddsAndOddsType(Connection conn, GameTransaction bean) throws SQLException {
		return DBQueryRunner.update(conn,
			"""
				UPDATE GameTransaction SET
				UPDATE_TIME = SYSTIMESTAMP, ODDS = ?, ODDS_TYPE = ?
				WHERE ID = ?
				""",
			bean.getOdds(), bean.getOddsType(), bean.getId()
		);
	}

	/**
	 * 從 GameTransaction 即時聚合每日 / 每廠商 / 每遊戲類型的投注摘要。
	 * <p>
	 * 行為對齊 {@code MemberServlet#searchBetDetails} 的查詢條件：
	 * <ul>
	 *   <li>時間欄位使用 {@code settle_time}（與既有 settled bet 相關報表一致）。</li>
	 *   <li>過濾 {@code system_txn_status <> UNSETTLED} 與 {@code is_game_txn_summarized = SUMMARIZED}。</li>
	 *   <li>{@code profit / turnover} 僅累計 SETTLED 狀態的紀錄；{@code bet_amount} 累計所有非 UNSETTLED 紀錄。</li>
	 * </ul>
	 * 回傳 JSON 結構與 {@link GameTransactionSummaryDAO#findBetSummaryDetailsByMultiCondition} 完全一致，
	 * 便於替換資料源。
	 */
	public static String findBetSummaryDetailsByMultiCondition(Connection conn, String userId,
		WebSiteType webSiteType, String[] gameTypes, String[] vendorIds, String fromDate, String toDate,
		String sortCondition, DBOrderType orderType, PageInfo pageInfo, LangMessage lang) throws Exception {

		String selectSql = "SELECT TRUNC(gt.settle_time) summary_date, gt.vendor_id, gt.game_type, "
			+ "SUM(gt.bet_amount) sum_bet_amount, "
			+ "SUM(CASE WHEN gt.system_txn_status = ? THEN gt.profit_loss ELSE 0 END) profit, "
			+ "SUM(CASE WHEN gt.system_txn_status = ? THEN gt.turnover ELSE 0 END) turnover ";

		StringBuilder sharedSql = new StringBuilder(
			"FROM gameTransaction gt WHERE gt.user_id = ? AND gt.website_type = ? "
				+ "AND gt.settle_time BETWEEN ? AND ? "
				+ "AND gt.system_txn_status <> ? "
				+ "AND gt.is_game_txn_summarized = ? ");

		String pattern = FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy;
		Timestamp startDate = DateTimeBuilder.localDateTime(fromDate, pattern).withMinTime().toTimestamp();
		Timestamp endDate = DateTimeBuilder.localDateTime(toDate, pattern).withMaxTime().toTimestamp();

		List<Object> paramsList = new ArrayList<>();
		// SELECT 的兩個 CASE WHEN 參數
		paramsList.add(SystemTxnStatusType.SETTLED.unique());
		paramsList.add(SystemTxnStatusType.SETTLED.unique());
		// WHERE
		paramsList.add(userId);
		paramsList.add(webSiteType.unique());
		paramsList.add(startDate);
		paramsList.add(endDate);
		paramsList.add(SystemTxnStatusType.UNSETTLED.unique());
		paramsList.add(GameTxnSummaryType.SUMMARIZED.unique());

		if (gameTypes != null) {
			sharedSql.append("AND gt.game_type IN (")
				.append(StringUtils.repeat("?", ",", gameTypes.length))
				.append(") ");
			paramsList.addAll(Arrays.asList(gameTypes));
		}
		if (vendorIds != null) {
			sharedSql.append("AND gt.vendor_id IN (")
				.append(StringUtils.repeat("?", ",", vendorIds.length))
				.append(") ");
			paramsList.addAll(Arrays.asList(vendorIds));
		}

		sharedSql.append("GROUP BY TRUNC(gt.settle_time), gt.vendor_id, gt.game_type ");

		String sortCol = !StringUtils.isEmpty(sortCondition) && sortCondition.equalsIgnoreCase("SUMMARY_DATE")
			? "TRUNC(gt.SETTLE_TIME)"
			: sortCondition;
		// 對應 summary 表欄位 → GameTransaction 欄位的差異
		if (null != sortCol && sortCol.equalsIgnoreCase("sum_bet_amount")) {
			sortCol = "SUM(gt.bet_amount)";
		}

		// Grand total（不分頁、不排序）
		PreparedStatement totalPs = null;
		ResultSet totalRs = null;
		BigDecimal totalBetAmount = BigDecimal.ZERO;
		BigDecimal totalProfitLoss = BigDecimal.ZERO;
		BigDecimal totalTurnover = BigDecimal.ZERO;
		try {
			totalPs = conn.prepareStatement(selectSql + sharedSql);
			DBQueryRunner.fillStatement(totalPs, totalPs.getParameterMetaData(), paramsList);
			totalRs = totalPs.executeQuery();
			while (totalRs.next()) {
				totalBetAmount = totalBetAmount.add(Optional.ofNullable(totalRs.getBigDecimal("sum_bet_amount")).orElse(BigDecimal.ZERO));
				totalProfitLoss = totalProfitLoss.add(Optional.ofNullable(totalRs.getBigDecimal("profit")).orElse(BigDecimal.ZERO));
				totalTurnover = totalTurnover.add(Optional.ofNullable(totalRs.getBigDecimal("turnover")).orElse(BigDecimal.ZERO));
			}
		} finally {
			DbUtils.closeAll(totalPs, totalRs);
		}

		String countSql = "SELECT COUNT(*) FROM (" + selectSql + sharedSql + " )";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String sql = OracleUtils.getCalculatedPageSQL(
			selectSql + sharedSql + " ORDER BY " + sortCol + " " + orderType.getSqlString());
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
					jGenerator.writeStringField("gameTypeName",
						GameType.getInstance(gameType.intValue()).getFullName(lang));
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
		hashMap.put("totalBetAmount", totalBetAmount);
		hashMap.put("totalProfitLoss", totalProfitLoss);
		hashMap.put("totalTurnover", totalTurnover);
		mainObject.accumulate(PageUtils.TOTAL_AMOUNT, new JSONObject(hashMap));

		return mainObject.toString();
	}

	/**
	 * 從 GameTransaction 即時聚合，僅以 TRUNC(settle_time) 分組（不依 vendor / game_type 分組）。
	 * <p>
	 * 行為與欄位對齊 {@link GameTransactionSummaryDAO#findBetSummaryByDateRangeGroupByDate}，
	 * 差異僅在資料源與額外過濾：
	 * <ul>
	 *   <li>排除 UNSETTLED（system_txn_status &lt;&gt; 0）</li>
	 *   <li>profit / turnover 僅累計 SETTLED 的紀錄；amount = SUM(bet_amount) 含所有非 UNSETTLED 紀錄</li>
	 *   <li>is_game_txn_summarized = 1（SUMMARIZED）</li>
	 * </ul>
	 */
	public static String findBetSummaryByDateRangeGroupByDate(Connection conn, String userId,
		WebSiteType webSiteType, java.util.Date startDate, java.util.Date endDate, int paymentType,
		String sortCondition, DBOrderType orderType, PageInfo pageInfo,
		JsonValueProcessor processor) throws Exception {

		Timestamp start = DateTimeBuilder.localDateTime(startDate).withMinTime().toTimestamp();
		Timestamp end = DateTimeBuilder.localDateTime(endDate).withMaxTime().toTimestamp();

		List<Object> paramsList = new ArrayList<>();
		// SELECT 內兩個 CASE WHEN 用的 SETTLED 參數
		paramsList.add(SystemTxnStatusType.SETTLED.unique());
		paramsList.add(SystemTxnStatusType.SETTLED.unique());
		// WHERE
		paramsList.add(userId);
		paramsList.add(webSiteType.unique());
		paramsList.add(start);
		paramsList.add(end);
		paramsList.add(SystemTxnStatusType.UNSETTLED.unique());
		paramsList.add(GameTxnSummaryType.SUMMARIZED.unique());

		String selectSql = "SELECT TRUNC(gt.settle_time) summary_date, "
			+ "SUM(gt.bet_amount) amount, "
			+ "SUM(CASE WHEN gt.system_txn_status = ? THEN gt.profit_loss ELSE 0 END) profit, "
			+ "SUM(CASE WHEN gt.system_txn_status = ? THEN gt.turnover ELSE 0 END) turnover "
			+ "FROM gameTransaction gt "
			+ "WHERE gt.user_id = ? AND gt.website_type = ? "
			+ "AND gt.settle_time BETWEEN ? AND ? "
			+ "AND gt.system_txn_status <> ? "
			+ "AND gt.is_game_txn_summarized = ? "
			+ "GROUP BY TRUNC(gt.settle_time)";

		String countSql = "SELECT COUNT(*) FROM (" + selectSql + ")";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
		pageInfo.setTotalCount(totalCount);

		String safeSort = !StringUtils.isEmpty(sortCondition) && sortCondition.equalsIgnoreCase("SUMMARY_DATE")
			? "TRUNC(gt.SETTLE_TIME)"
			: sortCondition;

		String sql = OracleUtils.getCalculatedPageSQL(
			selectSql + " ORDER BY " + safeSort + " " + orderType.getSqlString());
		paramsList.add(pageInfo.getLastRowNumber());
		paramsList.add(pageInfo.getFirstRowNumber());

		return DBQueryRunner.processJsonArrayValue(conn, processor, sql, paramsList);
	}
}
