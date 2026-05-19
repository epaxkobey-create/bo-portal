package com.nv.commons.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.SystemTxnStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountStats;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.ResultSetProcessor;
import com.nv.commons.utils.DateTimeBuilder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AccountStatsDAO {

	public static void lock(Connection conn, WebSiteType webSiteType, String userId, Timestamp summaryDate,
		DBQueryType queryType) throws SQLException {

		String sql = "SELECT user_id FROM accountStats WHERE website_type = ? AND user_id = ? AND summary_date = ?";
		if (queryType != null) {
			sql += queryType.getSqlString();
		}
		DBQueryRunner.getString(conn, sql, webSiteType.unique(), userId, summaryDate);
	}

	public static AccountStats get(Connection conn, WebSiteType webSiteType, String userId, Timestamp summaryDate,
		DBQueryType queryType)
		throws SQLException {
		String sql = "SELECT * FROM accountStats WHERE website_type = ? AND user_id = ? AND summary_date = ?";
		if (queryType != null) {
			sql += queryType.getSqlString();
		}
		return DBQueryRunner.getBean(conn, AccountStats.class, sql, webSiteType.unique(), userId, summaryDate);
	}

	private static final LoadingCache<String, AccountStats> accountStatsCache = Caffeine.newBuilder()
		.expireAfterAccess(1, TimeUnit.MINUTES)
		.build(key -> null);

	public static void invalidateCache() {
		accountStatsCache.invalidateAll();
	}

	public static AccountStats getStatsByUserIdFromTransaction(Connection conn, String userId, WebSiteType webSiteType)
		throws SQLException {
		String key = userId + "-" + webSiteType.unique();
		AccountStats cachedStats = accountStatsCache.get(key);
		if (cachedStats != null) {
			return cachedStats;
		}

		String sql = """
			SELECT
			COALESCE((SELECT SUM(PROFIT_LOSS) FROM GAMETRANSACTION WHERE USER_ID = ? AND SYSTEM_TXN_STATUS = ?), 0) AS profit_loss,
			COALESCE((SELECT SUM(TURNOVER) FROM GAMETRANSACTION WHERE USER_ID = ? AND SYSTEM_TXN_STATUS = ?), 0) AS turnover,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (0,2) THEN 1 ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS deposit_count,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (0,2) THEN AMOUNT ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS deposit_amount,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (1,4) THEN 1 ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS withdrawal_count,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (1,4) THEN AMOUNT ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS withdrawal_amount,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (3) THEN 1 ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS adjustment_count,
			COALESCE((SELECT SUM(CASE WHEN TRANSACTION_TYPE IN (3) THEN AMOUNT ELSE 0 END) FROM MONEYTRANSACTION WHERE USER_ID = ? AND STATUS = 2), 0) AS adjustment_amount,
			CAST(0 AS DECIMAL(19,2)) AS transfer_in,
			CAST(0 AS DECIMAL(19,2)) AS transfer_out,
			CAST(0 AS DECIMAL(19,2)) AS recycle_balance,
			CAST(0 AS DECIMAL(19,2)) AS POINT_TO_BALANCE,
			0 AS bonus_count,
			CAST(0 AS DECIMAL(19,2)) AS bonus_amount
			FROM DUAL
			""";
		var stats = DBQueryRunner.getBean(conn, AccountStats.class, sql,
			userId, SystemTxnStatusType.SETTLED.unique(), // profit_loss
			userId, SystemTxnStatusType.SETTLED.unique(), // turnover
			userId, userId, userId, userId, userId, userId);
		if (stats == null) {
			stats = new AccountStats();
			stats.setProfitLoss(BigDecimal.ZERO);
			stats.setTurnover(BigDecimal.ZERO);
			stats.setDepositAmount(BigDecimal.ZERO);
			stats.setDepositCount(0);
			stats.setAdjustmentAmount(BigDecimal.ZERO);
			stats.setAdjustmentCount(0);
			stats.setWithdrawalAmount(BigDecimal.ZERO);
			stats.setWithdrawalCount(0);
			stats.setTransferIn(BigDecimal.ZERO);
			stats.setTransferOut(BigDecimal.ZERO);
			stats.setRecycleBalance(BigDecimal.ZERO);
			stats.setPointToBalance(BigDecimal.ZERO);
			stats.setBonusAmount(BigDecimal.ZERO);
			stats.setBonusCount(0);
			stats.setUserId(userId);
		}
		accountStatsCache.put(key, stats);
		return stats;
	}

	public static AccountStats getStatsByUserId(Connection conn, String userId, WebSiteType webSiteType)
		throws SQLException {
		String sql =
			"SELECT NVL(SUM(deposit_count), 0) AS deposit_count, NVL(SUM(deposit_amount), 0) AS deposit_amount,"
				+ "NVL(SUM(withdrawal_count), 0) AS withdrawal_count, NVL(SUM(withdrawal_amount), 0) AS withdrawal_amount, "
				+ "NVL(SUM(adjustment_count), 0) AS adjustment_count, NVL(SUM(adjustment_amount), 0) AS adjustment_amount, "
				+ "NVL(SUM(bonus_count), 0) AS bonus_count, NVL(SUM(bonus_amount), 0) AS bonus_amount, "
				+ "NVL(SUM(profit_loss), 0) AS profit_loss, NVL(SUM(turnover), 0) AS turnover, "
				+ "NVL(SUM(transfer_in), 0) AS transfer_in, NVL(SUM(transfer_out), 0) AS transfer_out,"
				+ "NVL(SUM(recycle_balance), 0) AS recycle_balance,"
				+ "NVL(SUM(POINT_TO_BALANCE), 0) AS POINT_TO_BALANCE "
				+ "FROM accountStats WHERE user_id = ? AND website_type = ? ";
		return DBQueryRunner.getBean(conn, AccountStats.class, sql, userId, webSiteType.unique());
	}

	public static int updateDeposit(Connection conn, Account account,
		BigDecimal depositAmount, Timestamp approveTime)
		throws SQLException {

		String sql =
			"MERGE INTO accountStats s USING(SELECT ? AS user_id, ? AS website_type, TRUNC(SYSDATE, 'mm') AS summary_date FROM dual) d "
				+ "ON (s.website_type = d.website_type AND s.user_id = d.user_id AND s.summary_date = d.summary_date) "
				+ "WHEN MATCHED THEN UPDATE SET s.deposit_amount = s.deposit_amount + ?, s.deposit_count = s.deposit_count + 1, s.update_time = ? "
				+ "WHEN NOT MATCHED THEN INSERT(user_id, website_type, currency, affiliate_id, summary_date, deposit_amount, deposit_count, update_time, recycle_balance)"
				+ "VALUES(?, ?, ?, ?, TRUNC(SYSDATE, 'mm'), ?, 1, ?, 0)";

		Object[] params = {
			account.getUserId(),
			account.getWebsiteType(),
			depositAmount,
			approveTime,
			account.getUserId(),
			account.getWebsiteType(),
			account.getCurrencyTypeName(),
			account.getAffiliateId(),
			depositAmount,
			approveTime
		};

		return DBQueryRunner.update(conn, sql, params);
	}

	public static int updateWithdrawal(Connection conn, Account account, BigDecimal withdrawalAmount,
		Timestamp approveTime) throws SQLException {

		String sql =
			"MERGE INTO accountStats s USING(SELECT ? AS user_id, ? AS website_type, TRUNC(SYSDATE, 'mm') AS summary_date FROM dual) d "
				+ "ON (s.website_type = d.website_type AND s.user_id = d.user_id AND s.summary_date = d.summary_date) "
				+ "WHEN MATCHED THEN UPDATE SET s.withdrawal_amount = s.withdrawal_amount + ?, s.withdrawal_count = s.withdrawal_count + 1, s.update_time = ? "
				+ "WHEN NOT MATCHED THEN INSERT(user_id, website_type, currency, affiliate_id, summary_date, withdrawal_amount, withdrawal_count, update_time, recycle_balance)"
				+ "VALUES(?, ?, ?, ?, TRUNC(SYSDATE, 'mm'), ?, 1, ?, 0)";

		Object[] params = {
			account.getUserId(),
			account.getWebsiteType(),
			withdrawalAmount,
			approveTime,
			account.getUserId(),
			account.getWebsiteType(),
			account.getCurrencyTypeName(),
			account.getAffiliateId(),
			withdrawalAmount,
			approveTime
		};

		return DBQueryRunner.update(conn, sql, params);
	}

	public static int updateAdjustment(Connection conn, Account account,
		BigDecimal adjustmentAmount, Timestamp createTime) throws SQLException {

		String sql =
			"MERGE INTO accountStats s USING(SELECT ? AS user_id, ? AS website_type, TRUNC(SYSDATE, 'mm') AS summary_date FROM dual) d "
				+ "ON (s.website_type = d.website_type AND s.user_id = d.user_id AND s.summary_date = d.summary_date) "
				+ "WHEN MATCHED THEN UPDATE SET s.adjustment_amount = s.adjustment_amount + ?, s.adjustment_count = s.adjustment_count + 1, s.update_time = ? "
				+ "WHEN NOT MATCHED THEN INSERT(user_id, website_type, currency, affiliate_id, summary_date, adjustment_amount, adjustment_count, update_time, recycle_balance) "
				+ "VALUES(?, ?, ?, ?, TRUNC(SYSDATE, 'mm'), ?, 1, ?, 0)";

		Object[] params = {
			account.getUserId(),
			account.getWebsiteType(),
			adjustmentAmount,
			createTime,
			account.getUserId(),
			account.getWebsiteType(),
			account.getCurrencyTypeName(),
			account.getAffiliateId(),
			adjustmentAmount,
			createTime
		};

		return DBQueryRunner.update(conn, sql, params);
	}

	public static void summarize(Connection readConn, Connection writeConn, WebSiteType webSiteType, String userId,
		Timestamp startTime, Timestamp endTime) throws SQLException {

		String summaryDate = DateTimeBuilder.localDateTime(startTime).toString("yyyy-MM-dd");

		String mergeSql = " MERGE INTO accountStats s USING( "
			+ " SELECT "
			+ " ? AS website_type, "
			+ " ? AS user_Id, "
			+ " ? AS currency, "
			+ " ? AS affiliate_id, "
			+ " TO_TIMESTAMP(?,'YYYY-MM-DD') AS summary_date, "
			+ " ? AS bet_amount, "
			+ " ? AS profit_loss, "
			+ " ? AS turnover, "
			+ " ? AS deposit, "
			+ " ? AS withdrawal, "
			+ " ? AS adjustment, "
			+ " 0 AS bonus, "
			+ " ? AS bet_count, "
			+ " ? AS deposit_count, "
			+ " ? AS withdrawal_count, "
			+ " ? AS adjustment_count, "
			+ " ? AS bonus_count, "
			+ " 0 AS transfer_in, "
			+ " 0 AS transfer_out, "
			+ " ? AS recycle_balance "
			+ " FROM DUAL "
			+ " ) r ON (s.website_type = r.website_type AND s.user_id = r.user_id AND s.summary_date = r.summary_date ) "
			+ " WHEN MATCHED THEN UPDATE SET "
			+ " 	deposit_count = r.deposit_count, deposit_amount = r.deposit, "
			+ " 	withdrawal_count = r.withdrawal_count, withdrawal_amount = r.withdrawal, "
			+ " 	adjustment_count = r.adjustment_count, adjustment_amount = r.adjustment, "
			+ " 	bonus_count = r.bonus_count, bonus_amount = r.bonus, "
			+ " 	turnover = r.turnover, profit_loss = r.profit_loss, bet_count = r.bet_count, update_time = SYSTIMESTAMP, "
			+ "    transfer_in = r.transfer_in, transfer_out = r.transfer_out, recycle_balance = r.recycle_balance "
			+ " WHEN NOT MATCHED THEN INSERT(website_type, user_id, currency, affiliate_id, summary_date, "
			+ " 	deposit_count, deposit_amount, withdrawal_count, withdrawal_amount, adjustment_count, adjustment_amount,"
			+ " 	bonus_count, bonus_amount, turnover, profit_loss, bet_count, update_time, transfer_in, transfer_out, recycle_balance)"
			+ " VALUES(r.website_type, r.user_id, r.currency, r.affiliate_id, r.summary_date, "
			+ " 	r.deposit_count, r.deposit, r.withdrawal_count, r.withdrawal, r.adjustment_count, r.adjustment, "
			+ "  	r.bonus_count, r.bonus, r.turnover, r.profit_loss, r.bet_count, SYSTIMESTAMP, r.transfer_in, r.transfer_out, r.recycle_balance)";

		ResultSetProcessor processor = (index, rs) -> {
			Object[] mergeParams = {
				rs.getInt("website_type"),
				rs.getString("user_id"),
				rs.getString("currency"),
				rs.getLong("affiliate_id"),
				summaryDate,
				rs.getBigDecimal("bet_amount"),
				rs.getBigDecimal("profit"),
				rs.getBigDecimal("turnover"),
				rs.getBigDecimal("deposit"),
				rs.getBigDecimal("withdrawal"),
				rs.getBigDecimal("adjustment"),
				rs.getBigDecimal("bet_count"),
				rs.getBigDecimal("deposit_count"),
				rs.getBigDecimal("withdrawal_count"),
				rs.getBigDecimal("adjustment_count"),
				rs.getBigDecimal("bonus_count"),
				rs.getBigDecimal("recycle_balance")
			};
			DBQueryRunner.update(writeConn, mergeSql, mergeParams);
		};

		List<Object> values = new ArrayList<>();
		StringBuilder selectSql = new StringBuilder();
		selectSql.append("	SELECT website_type, user_Id, currency, affiliate_id, to_timestamp('").append(summaryDate)
			.append("','YYYY-MM-DD') as summary_date,");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type = ").append(AccountSummaryReportType.BET.unique())
			.append(" THEN amount END, 0)) AS bet_amount, ");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type = ").append(AccountSummaryReportType.BET.unique())
			.append(" THEN profit END, 0)) AS profit, ");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type = ").append(AccountSummaryReportType.TURNOVER.unique())
			.append(" THEN amount END, 0)) AS turnover, ");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type = ").append(AccountSummaryReportType.DEPOSIT.unique())
			.append(" THEN amount END, 0)) AS deposit, ");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type = ").append(AccountSummaryReportType.WITHDRAWALS.unique())
			.append(" THEN amount END, 0)) AS withdrawal, ");
		selectSql.append(" 	SUM(NVL(CASE WHEN payment_type in ( ")
			.append(AccountSummaryReportType.ADJUSTMENT.unique()).append(",")
			.append(AccountSummaryReportType.REVENUE_ADJUSTMENT.unique())
			.append(" ) THEN amount END, 0)) AS adjustment, ");


		//BetCount
		selectSql.append(
				" NVL((SELECT SUM(bet_count) FROM gameTransactionSummary WHERE website_type =? AND user_id =? ")
			.append("  AND summary_date >= ? AND summary_date <= ?),0) AS bet_count")
			.append(",");
		values.add(webSiteType.unique());
		values.add(userId);
		values.add(startTime);
		values.add(endTime);

		//DepositCount
		selectSql.append(" NVL((SELECT count(id) FROM moneyTransaction WHERE website_type =? AND user_id =? ")
			.append("  AND transaction_type IN( ")
			.append(MoneyTransactionType.DEPOSIT.unique()).append(",")
			.append(MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique())
			.append(" ) AND approved_time >= ? AND approved_time <= ?), 0) AS deposit_count")
			.append(",");
		values.add(webSiteType.unique());
		values.add(userId);
		values.add(startTime);
		values.add(endTime);

		//WithdrawalCount
		selectSql.append(" NVL((SELECT count(id) FROM moneyTransaction WHERE website_type =? AND user_id =? ")
			.append("  AND transaction_type IN( ")
			.append(MoneyTransactionType.WITHDRAWALS.unique()).append(",")
			.append(MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique())
			.append(" ) AND approved_time >= ? AND approved_time <= ?), 0) AS withdrawal_count")
			.append(",");
		values.add(webSiteType.unique());
		values.add(userId);
		values.add(startTime);
		values.add(endTime);

		//AdjustmentCount
		selectSql.append(" NVL((SELECT count(id) FROM moneyTransaction WHERE website_type =? AND user_id =? ")
			.append("  AND transaction_type IN( ")
			.append(MoneyTransactionType.ADJUSTMENT.unique()).append(",")
			.append(MoneyTransactionType.REVENUE_ADJUSTMENT.unique())
			.append(" ) AND approved_time >= ? AND approved_time <= ?), 0) AS adjustment_count")
			.append(",");
		values.add(webSiteType.unique());
		values.add(userId);
		values.add(startTime);
		values.add(endTime);


		selectSql.append(" 	FROM accountSummaryReport a ");
		selectSql.append(" 	WHERE a.website_type = ? ");
		selectSql.append("  AND a.user_id = ? ");
		selectSql.append("  AND a.transaction_time >= ? ");
		selectSql.append("  AND a.transaction_time <= ? ");
		selectSql.append(" GROUP BY ").append(" website_type, user_Id, currency, affiliate_id, to_timestamp('")
			.append(summaryDate).append("','YYYY-MM-DD') ");
		values.add(webSiteType.unique());
		values.add(userId);
		values.add(startTime);
		values.add(endTime);

		DBQueryRunner.processResultSet(readConn, processor, selectSql.toString(), values);
	}
}
