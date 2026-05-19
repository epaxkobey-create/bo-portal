package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nv.commons.constants.GameTxnSummaryType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.GameTransactionSummaryHourly;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.DataBeanProcessor;
import com.nv.commons.model.database.ResultSetProcessor;

public class GameTransactionSummaryHourlyDAO {

	public static int insert(Connection conn, GameTransactionSummaryHourly bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				INSERT INTO GameTransactionSummaryHourly
				(WEBSITE_TYPE, USER_ID, VENDOR_ID, GAME_TYPE, SUMMARY_DATE,
				BET_COUNT, SUM_BET_AMOUNT, PROFIT, TURNOVER, PROGRESS_BET_AMOUNT, PROGRESS_PROFIT_LOSS,
				CURRENCY, VIP_LEVEL, AFFILIATE_ID, IS_SUMMARIZED, CREATE_TIME, UPDATE_TIME)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate(),
			bean.getBetCount(), bean.getSumBetAmount(), bean.getProfit(), bean.getTurnover(),
			bean.getProgressBetAmount(), bean.getProgressProfitLoss(),
			bean.getCurrency(), bean.getVipLevel(), bean.getAffiliateId(), bean.getIsSummarized()
		);
	}

	public static int update(Connection conn, GameTransactionSummaryHourly bean)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				UPDATE GameTransactionSummaryHourly SET
				BET_COUNT = ?, SUM_BET_AMOUNT = ?, PROFIT = ?, TURNOVER = ?,
				PROGRESS_BET_AMOUNT = ?, PROGRESS_PROFIT_LOSS = ?,
				CURRENCY = ?, VIP_LEVEL = ?, AFFILIATE_ID = ?, IS_SUMMARIZED = ?, UPDATE_TIME = SYSTIMESTAMP
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND VENDOR_ID = ? AND GAME_TYPE = ? AND SUMMARY_DATE = ?
				""",
			bean.getBetCount(), bean.getSumBetAmount(), bean.getProfit(), bean.getTurnover(),
			bean.getProgressBetAmount(), bean.getProgressProfitLoss(),
			bean.getCurrency(), bean.getVipLevel(), bean.getAffiliateId(), bean.getIsSummarized(),
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate()
		);
	}

	public static Map<Timestamp, List<String>> findNotSummarized(Connection conn,
		WebSiteType webSiteType) throws SQLException {

		String sql = "SELECT user_id, summary_date "
			+ " FROM gameTransactionSummaryHourly "
			+ " WHERE website_type = ? "
			// is_game_txn_summarized不包含GameTxnSummaryType.SUMMARIZED => 需要做by user結算
			+ " AND is_summarized = ? "
			+ " GROUP BY user_id, summary_date";

		Map<Timestamp, List<String>> resultMap = new HashMap<>();

		ResultSetProcessor processor = (index, rs) ->
			resultMap.computeIfAbsent(rs.getTimestamp("summary_date"), o -> new ArrayList<>())
				.add(rs.getString("user_id"));

		DBQueryRunner.processResultSet(conn, processor, sql,
			webSiteType.unique(), GameTxnSummaryType.NOT_SUMMARIZED.unique());

		return resultMap;
	}

	public static Map<String, List<GameTransactionSummaryHourly>> find(Connection conn, WebSiteType websiteType, String userId,
		Timestamp startTime, Timestamp endTime) throws SQLException {

		String sql = " SELECT * FROM gameTransactionSummaryHourly "
			+ " WHERE website_type = ? "
			+ " AND user_id = ? "
			+ " AND summary_date >= ? "
			+ " AND summary_date <= ? ";

		Object[] values = {
			websiteType.unique(),
			userId,
			startTime,
			endTime
		};

		Map<String, List<GameTransactionSummaryHourly>> resultMap = new HashMap<>();

		DataBeanProcessor<GameTransactionSummaryHourly> processor = (rs, bean) -> {
			String key = bean.getVendorId() + "-" + bean.getGameType();
			resultMap.computeIfAbsent(key, o -> new ArrayList<>()).add(bean);
		};

		DBQueryRunner.processBeanResult(conn, processor, GameTransactionSummaryHourly.class, sql, values);

		return resultMap;
	}

	public static GameTransactionSummaryHourly find(Connection conn, GameTransactionSummaryHourly bean) throws SQLException {

		return DBQueryRunner.getBean(conn, GameTransactionSummaryHourly.class,
			"""
				SELECT * FROM GameTransactionSummaryHourly
				WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND VENDOR_ID = ? AND GAME_TYPE = ? AND SUMMARY_DATE = ?
				AND IS_SUMMARIZED = ?
				""",
			bean.getWebsiteType(), bean.getUserId(), bean.getVendorId(), bean.getGameType(), bean.getSummaryDate(),
			GameTxnSummaryType.NOT_SUMMARIZED.unique()
		);
	}

	public static int[] updateIsSummarized(Connection conn, List<GameTransactionSummaryHourly> txnSummaryList) throws SQLException {
		String sql = " UPDATE gameTransactionSummaryHourly SET is_summarized = ?, update_time = SYSTIMESTAMP "
			+ " WHERE website_type = ? "
			+ " AND user_id = ? "
			+ " AND vendor_id = ? "
			+ " AND game_type = ? "
			+ " AND summary_date = ? "
			+ " AND update_time <= ? ";

		List<Object[]> values = txnSummaryList.stream().map(txnSummary -> new Object[]{
			GameTxnSummaryType.SUMMARIZED.unique(),
			txnSummary.getWebsiteType(),
			txnSummary.getUserId(),
			txnSummary.getVendorId(),
			txnSummary.getGameType(),
			txnSummary.getSummaryDate(),
			txnSummary.getUpdateTime()
		}).collect(Collectors.toList());

		return DBQueryRunner.batch(conn, sql, values);
	}
}
