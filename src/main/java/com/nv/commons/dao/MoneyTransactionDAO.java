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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.LocalBankType;
import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.TimeIntervalType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PageResult;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.BeanConverter;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.model.database.ResultSetProcessor;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.MoneyTransactionUtils;
import com.nv.commons.utils.OracleUtils;
import com.nv.commons.utils.PageUtils;
import com.nv.commons.utils.PaymentGatewayUtils;
import org.apache.commons.lang3.StringUtils;

public class MoneyTransactionDAO {

	public static List<MoneyTransaction> findAll(Connection conn)
		throws SQLException {
		String sql = "SELECT * FROM MoneyTransaction ";
		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql);
	}

	public static List<MoneyTransaction> findAllByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		String sql = "SELECT * FROM MoneyTransaction WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql, updateTime);
	}

	public static long getMoneyTransactionIdSeq(Connection conn) throws SQLException {
		String sql = "SELECT moneytransaction_id_seq.NEXTVAL FROM DUAL";
		return DBQueryRunner.getNumber(conn, sql).longValue();
	}

	public static int addDeposit(Connection conn, MoneyTransaction moneyTransaction) throws SQLException {
		StringBuilder sql = new StringBuilder("INSERT INTO moneytransaction (id, "
			+ "user_id, website_type, vip_level, reference_no, amount, "
			+ "to_bank_id, to_bank_name, to_bank_account, to_bank_number, to_bank_branch, to_finance_code, to_payment_type, "
			+ "from_bank_id, from_bank_name, from_bank_account, from_bank_number, from_bank_branch, from_finance_code, remark, "
			+ "transaction_type, status, creator, bonus_id, bonus, currency, "
			+ "create_time, update_time, verified_time, amount_fee, real_amount, exchange_amount, "
			+ "exchange_rate, real_exchange_amount, transfer_type, bonus_title) "
			+ "SELECT ?, "
			+ "?, ?, ?, ?, ?, "
			+ "?, ?, ?, ?, ?, ?, ?, "
			+ "?, ?, ?, ?, ?, ?, ?, "
			+ "?, ?, ?, ?, ?, ?,"
			+ "SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP, ?, ?, ?, "
			+ "?, ?, ?, ? FROM dual ");

		List<Object> params = new ArrayList<>();
		moneyTransaction.setId(getMoneyTransactionIdSeq(conn));
		params.add(moneyTransaction.getId());
		params.add(moneyTransaction.getUserId());
		params.add(moneyTransaction.getWebsiteType());
		params.add(moneyTransaction.getVipLevel());
		params.add(moneyTransaction.getReferenceNo());
		params.add(moneyTransaction.getAmount());
		params.add(moneyTransaction.getToBankId());
		params.add(moneyTransaction.getToBankName());
		params.add(moneyTransaction.getToBankAccount());
		params.add(moneyTransaction.getToBankNumber());
		params.add(moneyTransaction.getToBankBranch());
		params.add(moneyTransaction.getToFinanceCode());
		params.add(moneyTransaction.getToPaymentType());
		params.add(moneyTransaction.getFromBankId());
		params.add(moneyTransaction.getFromBankName());
		params.add(moneyTransaction.getFromBankAccount());
		params.add(moneyTransaction.getFromBankNumber());
		params.add(moneyTransaction.getFromBankBranch());
		params.add(moneyTransaction.getFromFinanceCode());
		params.add(moneyTransaction.getRemark());
		params.add(moneyTransaction.getTransactionType());
		params.add(moneyTransaction.getStatus());
		params.add(moneyTransaction.getCreator());
		params.add(moneyTransaction.getBonusId());
		params.add(moneyTransaction.getBonus());
		params.add(moneyTransaction.getCurrency());
		params.add(moneyTransaction.getAmountFee());
		params.add(moneyTransaction.getAmount());
		params.add(moneyTransaction.getExchangeAmount());
		params.add(moneyTransaction.getExchangeRate());
		params.add(moneyTransaction.getRealExchangeAmount());
		params.add(moneyTransaction.getTransferType());
		params.add(moneyTransaction.getBonusTitle());

		return DBQueryRunner.update(conn, sql.toString(), params);
	}

	public static int approveDeposit(Connection conn, MoneyTransaction moneyTransaction) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
				"SELECT id, executor, transaction_type, status, reference_no, " +
					"to_bank_id, to_bank_name, to_bank_account, to_bank_number, to_bank_branch, " +
					"to_finance_code, to_payment_type, from_bank_number, " +
					"approved_userid, approved_time, approved_note, external_message " +
					"FROM moneytransaction WHERE id = ? FOR UPDATE",
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), moneyTransaction.getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				String executor = rs.getString("executor");
				if (null == moneyTransaction.getExecutor()) {
					//auto case
					if (null != executor) {
						throw new Deviation("this transaction is processing by another user " + executor);
					}
				} else if (!moneyTransaction.getExecutor().equals(executor)) {
					//normal case
					throw new Deviation("this transaction is processing by another user " + executor);
				}
				int type = rs.getInt("transaction_type");
				if (!(MoneyTransactionType.DEPOSIT.unique() == type
					|| MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == type)) {
					throw new Deviation("Transaction Type is invalid");
				}

				if (MoneyTransactionType.DEPOSIT.unique() == type
					&& MoneyTransactionStatusType.NEW.unique() != rs.getInt("status")) {
					throw new Deviation("Status is invalid");
				} else if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == type
					&& (MoneyTransactionStatusType.NEW.unique() != rs.getInt("status"))
					&& MoneyTransactionStatusType.CLOSE.unique() != rs.getInt("status")) {
					throw new Deviation("Status is invalid");
				}

				if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == type) {
					rs.updateString("reference_no", moneyTransaction.getReferenceNo());//pg
				}
				rs.updateObject("to_bank_id", moneyTransaction.getToBankId());
				rs.updateString("to_bank_name", moneyTransaction.getToBankName());
				rs.updateString("to_bank_account", moneyTransaction.getToBankAccount());
				rs.updateString("to_bank_number", moneyTransaction.getToBankNumber());
				rs.updateString("to_bank_branch", moneyTransaction.getToBankBranch());
				rs.updateString("to_finance_code", moneyTransaction.getToFinanceCode());
				rs.updateObject("to_payment_type", moneyTransaction.getToPaymentType());
				if (moneyTransaction.getFromBankNumber() != null) {
					rs.updateString("from_bank_number", moneyTransaction.getFromBankNumber());
				}
				rs.updateInt("status", moneyTransaction.getStatus());
				rs.updateString("approved_userid", moneyTransaction.getApprovedUserid());
				rs.updateTimestamp("approved_time", moneyTransaction.getApprovedTime());
				rs.updateString("approved_note", moneyTransaction.getApprovedNote());
				if (moneyTransaction.getExternalMessage() != null) {
					rs.updateString("external_message", moneyTransaction.getExternalMessage());
				}
				rs.updateNull("executor");
				rs.updateRow();
				return 1;
			}
			return 0;
		} finally {
			DbUtils.closeAll(ps, rs);
		}
	}

	public static boolean rejectDeposit(Connection conn, MoneyTransaction moneyTransaction, LangMessage lang)
		throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			// normal case
			ps = conn.prepareStatement(
				"SELECT id, user_id, website_type, status, from_bank_number, approved_userid, approved_time, approved_note, executor, amount, external_message, transaction_type, reference_no FROM moneytransaction "
					+ "WHERE id = ? AND transaction_type IN (?, ?) AND website_type = ? FOR UPDATE",
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), moneyTransaction.getId(),
				MoneyTransactionType.DEPOSIT.unique(), MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique(),
				moneyTransaction.getWebsiteType());
			//			}
			rs = ps.executeQuery();
			if (rs.next()) {
				String executor = rs.getString("executor");

				if (lang != null) {
					if (moneyTransaction.getExecutor() != null && !moneyTransaction.getExecutor()
						.equals(executor)) {
						throw new Deviation(lang.get("msg.error.info.distinctExecutor",
							new String[] {(executor != null) ? executor : ""}));
					}
				}

				if (moneyTransaction.getReferenceNo() != null) {
					rs.updateString("reference_no", moneyTransaction.getReferenceNo());
				}

				rs.updateInt("status", moneyTransaction.getStatus());
				if (moneyTransaction.getFromBankNumber() != null) {
					rs.updateString("from_bank_number", moneyTransaction.getFromBankNumber());
				}
				rs.updateString("approved_userid", moneyTransaction.getApprovedUserid());
				rs.updateString("approved_note", moneyTransaction.getApprovedNote());
				if (moneyTransaction.getExternalMessage() != null) {
					rs.updateString("external_message", moneyTransaction.getExternalMessage());
				}
				rs.updateTimestamp("approved_time", new Timestamp(System.currentTimeMillis()));
				rs.updateNull("executor");
				rs.updateRow();

				moneyTransaction.setUserId(rs.getString("user_id"));
				moneyTransaction.setWebsiteType(rs.getInt("website_type"));
				moneyTransaction.setAmount(rs.getBigDecimal("amount"));
			} else {
				throw new Deviation("fail id=" + moneyTransaction.getId());
			}
		} catch (Exception e) {
			if (DbUtils.isLockedException(e)) {
				LogUtils.SYS.error("moneytransaction locked id={}", moneyTransaction.getId());
			}
			throw e;
		} finally {
			DbUtils.closeAll(ps, rs);
		}
		return true;
	}

	public static int rejectWithdrawalByPaymentGateway(Connection conn, MoneyTransaction moneyTransaction)
		throws SQLException {
		String sql = "UPDATE moneytransaction SET "
			+ "executor = null, "
			+ "expire_time = null, "
			//			+ "from_bank_id = null, "
			+ "status = ?, "
			+ "approved_userid = ?, "
			+ "approved_note = ?, "
			+ "approved_time = ?, "
			+ "reference_no = ?, "
			+ "update_time = SYSTIMESTAMP "
			+ "WHERE executor = ? AND status IN ( ?, ? ) AND id = ? AND website_type = ? AND transaction_type = ? ";
		return DBQueryRunner.update(conn, sql, moneyTransaction.getStatus(), moneyTransaction.getApprovedUserid(),
			moneyTransaction.getApprovedNote(), moneyTransaction.getApprovedTime(),
			moneyTransaction.getReferenceNo(), moneyTransaction.getExecutor(),
			MoneyTransactionStatusType.PENDING_APPROVAL.unique(), MoneyTransactionStatusType.PROCESSING.unique(),
			moneyTransaction.getId(),
			moneyTransaction.getWebsiteType(), MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique());
	}

	public static int approveWithdrawal(Connection conn, MoneyTransaction moneyTransaction,
		MoneyTransactionStatusType nowStatusType, MoneyTransactionType
			originalTransactionType) throws SQLException {

		StringBuilder sql = new StringBuilder("UPDATE moneytransaction set "
			+ "to_payment_type = ?, "
			+ "from_bank_id = ?, "
			+ "from_bank_name = ?, "
			+ "from_bank_account = ?, "
			+ "from_bank_number = ?, "
			+ "from_bank_branch = ?, "
			+ "from_finance_code = ?, "
			+ "status = ?, "
			+ "approved_userid = ?, "
			+ "approved_time = ?, "
			+ "approved_note = ?, ");

		List<Object> params = new ArrayList<>(
			Arrays.asList(moneyTransaction.getToPaymentType(), moneyTransaction.getFromBankId(),
				moneyTransaction.getFromBankName(), moneyTransaction.getFromBankAccount(),
				moneyTransaction.getFromBankNumber(), moneyTransaction.getFromBankBranch(),
				moneyTransaction.getFromFinanceCode(), moneyTransaction.getStatus(),
				moneyTransaction.getApprovedUserid(), moneyTransaction.getApprovedTime(),
				moneyTransaction.getApprovedNote()));

		if (moneyTransaction.getExternalMessage() != null) {
			sql.append("external_message = ?, ");
			params.add(moneyTransaction.getExternalMessage());
		}

		sql.append("reference_no = ?, "
			+ "expire_time = null, "
			+ "executor = null, "
			+ "update_time = SYSTIMESTAMP "
			+ "WHERE id = ? AND status = ? AND transaction_type = ? ");

		params.addAll(Arrays.asList(moneyTransaction.getReferenceNo(),
			moneyTransaction.getId(),
			nowStatusType.unique(), originalTransactionType.unique()));

		return DBQueryRunner.update(conn, sql.toString(), params);
	}

	public static String getTotalMoneyTxn(Connection conn, int[] status, int[] type, Timestamp startDate,
		Timestamp endDate, CurrencyType currencyType, WebSiteType webSiteType,
		long pageNumber, long showCount, String column, DBOrderType orderType, LangMessage lang) throws Exception {
		StringBuilder sql = new StringBuilder(
			"SELECT id, user_id, amount, bonus, create_time, approved_time, transaction_type, to_payment_type, to_bank_branch, to_bank_name, currency FROM moneytransaction WHERE ");

		List<Object> paramsList = new ArrayList<>();
		if (startDate != null && endDate != null) {
			sql.append(" approved_time >= ? AND approved_time <= ? ");
			paramsList.add(startDate);
			paramsList.add(endDate);
		} else {
			sql.append(" create_time >= TRUNC(ADD_MONTHS(SYSDATE, -3),'mm') AND create_time <= SYSTIMESTAMP ");
		}
		sql.append(" AND website_type = ? ");
		paramsList.add(webSiteType.unique());

		sql.append(" AND transaction_type IN (").append(StringUtils.repeat("?", ",", type.length)).append(")");
		Arrays.stream(type).forEach(paramsList::add);

		sql.append(" AND status IN (").append(StringUtils.repeat("?", ",", status.length)).append(")");
		Arrays.stream(status).forEach(paramsList::add);

		sql.append(" AND currency = ? ");
		paramsList.add(currencyType.name());

		column = BeanConverter.getDBColumnName(MoneyTransaction.class, column);
		if (column != null) {
			sql.append(" ORDER BY ").append(column);
			if (orderType != null) {
				sql.append(orderType.getSqlString());
			}
		}

		PageResult<MoneyTransaction> moneyTxns = DBQueryRunner
			.getPageResult(conn, MoneyTransaction.class, sql.toString(), pageNumber, showCount, paramsList);

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("status", 200);
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, moneyTxns.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, moneyTxns.getTotalCount());
			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

			List<MoneyTransaction> txns = moneyTxns.getResultList();

			for (MoneyTransaction txn : txns) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("userId", txn.getUserId());
				jGenerator
					.writeStringField("id", MoneyTransactionUtils.formatId(txn.getId(), txn.getTransactionType()));
				//				jGenerator.writeStringField("type", MoneyTransactionType.getInstance(txn.getType()).name());
				jGenerator.writeNumberField("amount", txn.getAmount());
				jGenerator.writeNumberField("bonus", txn.getBonus());
				jGenerator.writeNumberField("total", txn.getBonus().add(txn.getAmount()));
				jGenerator.writeStringField("approvedTime", FormatUtils.dateFormat(txn.getApprovedTime()));
				jGenerator.writeStringField("createTime", FormatUtils.dateFormat(txn.getCreateTime()));
				jGenerator.writeNumberField("currencyType", CurrencyType.valueOf(txn.getCurrency()).unique());

				int transactionType = txn.getTransactionType();
				jGenerator.writeNumberField("transactionType", transactionType);
				if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == transactionType) {
					String paymentAccount = PaymentGatewayUtils.getPaymentAccountByToBankBranch(txn.getToBankBranch())
						.orElse("");
					jGenerator.writeStringField("toBankName", paymentAccount);
				} else {
					jGenerator.writeStringField("toBankName", txn.getToBankName());
				}

				String toPaymentType = null;

				Integer paymentType = txn.getToPaymentType();
				if (null != paymentType) {
					if (MoneyTransactionType.DEPOSIT.unique() == transactionType) {
						toPaymentType = LocalBankType.getInstance(paymentType).getFullName(lang);
					} else if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == transactionType) {
						toPaymentType = PaymentType.getInstanceOf(paymentType).getFullName(lang);
					}
				}
				jGenerator.writeStringField("toPaymentType", toPaymentType);

				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
			jGenerator.writeEndObject();

		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static MoneyTransaction getDepositInfoForPG(Connection conn, long id, boolean isNotification)
		throws SQLException {

		String sql = "SELECT * FROM moneytransaction WHERE id = ? AND ";

		List<Object> params = new ArrayList<>();
		params.add(id);

		if (isNotification) {
			sql = sql + " (status = ? OR status = ? )";
			params.add(MoneyTransactionStatusType.NEW.unique());
			params.add(MoneyTransactionStatusType.CONFIRMED.unique());
		} else {
			sql = sql + " status = ? ";
			params.add(MoneyTransactionStatusType.NEW.unique());
		}

		return DBQueryRunner.getBean(conn, MoneyTransaction.class, sql, params);
	}

	public static MoneyTransaction getDepositInfoByReferenceNoForPG(Connection conn, String referenceNo,
		boolean isNotification)
		throws SQLException {

		String sql = "SELECT * FROM moneytransaction WHERE reference_no = ? AND ";

		List<Object> params = new ArrayList<>();
		params.add(referenceNo);

		if (isNotification) {
			sql = sql + " (status = ? OR status = ? )";
			params.add(MoneyTransactionStatusType.NEW.unique());
			params.add(MoneyTransactionStatusType.CONFIRMED.unique());
		} else {
			sql = sql + " status = ? ";
			params.add(MoneyTransactionStatusType.NEW.unique());
		}

		return DBQueryRunner.getBean(conn, MoneyTransaction.class, sql, params);
	}

	public static int updateDepositForPG(Connection conn, MoneyTransaction moneyTransaction) throws SQLException {

		String sql = "UPDATE moneytransaction set "
			+ "reference_no = ?, "
			+ "from_bank_number = ?, "
			+ "to_bank_id = ?, "
			+ "to_bank_name = ?, "
			+ "to_bank_account = ?, "
			+ "to_bank_number = ?, "
			+ "to_bank_branch = ?, "
			+ "to_payment_type = ?, "
			+ "status = ?, "
			+ "approved_userid = ?, "
			+ "approved_time = ?, "
			+ "approved_note = ? ,"
			+ "remark = ?, "
			+ "update_time = SYSTIMESTAMP "
			+ "WHERE id = ? AND status = ? "
			+ " AND transaction_type = ? ";

		return DBQueryRunner.update(conn, sql,
			moneyTransaction.getReferenceNo(), moneyTransaction.getFromBankNumber(),
			moneyTransaction.getToBankId(), moneyTransaction.getToBankName(),
			moneyTransaction.getToBankAccount(), moneyTransaction.getToBankNumber(),
			moneyTransaction.getToBankBranch(), moneyTransaction.getToPaymentType(),
			moneyTransaction.getStatus(),
			moneyTransaction.getApprovedUserid(), moneyTransaction.getApprovedTime(),
			moneyTransaction.getApprovedNote(), moneyTransaction.getRemark(),
			moneyTransaction.getId(),
			MoneyTransactionStatusType.NEW.unique(),
			MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique());
	}

	public static int updateDepositForPGWithProof(Connection conn, MoneyTransaction moneyTransaction)
		throws SQLException {

		String sql = "UPDATE moneytransaction set "
			+ "reference_no = ?, "
			+ "from_bank_number = ?, "
			+ "to_bank_id = ?, "
			+ "to_bank_name = ?, "
			+ "to_bank_account = ?, "
			+ "to_bank_number = ?, "
			+ "to_bank_branch = ?, "
			+ "to_payment_type = ?, "
			+ "status = ?, "
			+ "proof = ?, "
			+ "approved_userid = ?, "
			+ "approved_time = ?, "
			+ "approved_note = ? ,"
			+ "remark = ?, "
			+ "update_time = SYSTIMESTAMP "
			+ "WHERE id = ? AND status = ? "
			+ " AND transaction_type = ? ";

		return DBQueryRunner.update(conn, sql,
			moneyTransaction.getReferenceNo(), moneyTransaction.getFromBankNumber(),
			moneyTransaction.getToBankId(), moneyTransaction.getToBankName(),
			moneyTransaction.getToBankAccount(), moneyTransaction.getToBankNumber(),
			moneyTransaction.getToBankBranch(), moneyTransaction.getToPaymentType(),
			moneyTransaction.getStatus(), moneyTransaction.getProof(),
			moneyTransaction.getApprovedUserid(), moneyTransaction.getApprovedTime(),
			moneyTransaction.getApprovedNote(), moneyTransaction.getRemark(),
			moneyTransaction.getId(),
			MoneyTransactionStatusType.NEW.unique(),
			MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique());
	}

	public static MoneyTransaction getPaymentGatewayWithdrawalDetail(Connection conn, long id)
		throws SQLException {

		String sql = "SELECT * FROM moneytransaction WHERE id = ? AND status = ?";

		Object[] params = {
			id,
			MoneyTransactionStatusType.PROCESSING.unique()
		};

		return DBQueryRunner.getBean(conn, MoneyTransaction.class, sql, params);
	}

	public static MoneyTransaction getPaymentGatewayWithdrawalDetailByReferenceNo(Connection conn, String referenceNo)
		throws SQLException {

		String sql = "SELECT * FROM moneytransaction WHERE reference_no = ? AND status = ?";

		Object[] params = {
			referenceNo,
			MoneyTransactionStatusType.PROCESSING.unique()
		};

		return DBQueryRunner.getBean(conn, MoneyTransaction.class, sql, params);
	}

	public static Map<String, Integer> getDepositPendingCountByUserKeys(Connection conn,
		String[][] userKeys, Function<String, Account> getAccountFunc) throws SQLException {

		Map<String, Integer> accountDepositPendingCountMap = new HashMap<>();

		int[] moneyTransactionTypes = {MoneyTransactionType.DEPOSIT.unique(),
			MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()};

		String sql = "SELECT website_type, user_id, (SELECT COUNT(*) AS count FROM moneytransaction "
			+ "WHERE status = ? AND transaction_type IN (" + StringUtils.repeat("?", ",", moneyTransactionTypes.length)
			+ ") "
			+ "AND user_id = account.user_id "
			+ "AND website_type = account.website_type "
			+ "GROUP BY website_type, user_id) AS count "
			+ "FROM account WHERE (user_id, website_type) IN (" + OracleUtils.getGroupCondition(userKeys.length) + ") ";

		List<Object> params = new ArrayList<>();

		params.add(MoneyTransactionStatusType.NEW.unique());
		Arrays.stream(moneyTransactionTypes).forEach(params::add);
		params.add(OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));

		WebSiteType webSiteType;
		String userKey;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn.prepareStatement(sql);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), params);
			rs = ps.executeQuery();

			while (rs.next()) {
				webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = getAccountFunc.apply(userKey);

				if (userInCache == null) {
					// LogUtils.SYS.error("Not find player when update pending deposit count, {}" + userKey);
					continue;
				}

				accountDepositPendingCountMap.put(userKey, rs.getInt("count"));
			}

		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}

		return accountDepositPendingCountMap;
	}

	public static Map<String, Integer> getWithdrawalPendingCountByUserKeys(Connection conn,
		String[][] userKeys, Function<String, Account> getAccountFunc) throws SQLException {

		Map<String, Integer> accountWithdrawalPendingCountMap = new HashMap<>();

		int[] moneyTransactionTypes = {
			MoneyTransactionType.WITHDRAWALS.unique(), MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()};

		String sql = "SELECT website_type, user_id, (SELECT COUNT(*) FROM moneytransaction "
			+ "WHERE status IN (" + StringUtils.repeat("?", ",",
			MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE.length)
			+ ") AND transaction_type IN (" + StringUtils.repeat("?", ",", moneyTransactionTypes.length) + ") "
			+ "AND user_id = account.user_id "
			+ "AND website_type = account.website_type "
			+ "GROUP BY website_type, user_id) AS count "
			+ "FROM account WHERE (user_id, website_type) IN (" + OracleUtils.getGroupCondition(userKeys.length) + ") ";

		List<Object> params = new ArrayList<>();

		Arrays.stream(MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE).forEach(params::add);
		Arrays.stream(moneyTransactionTypes).forEach(params::add);
		params.add(OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));

		WebSiteType webSiteType;
		String userKey;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn.prepareStatement(sql);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), params);
			rs = ps.executeQuery();

			while (rs.next()) {
				webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = getAccountFunc.apply(userKey);

				if (userInCache == null) {
					// LogUtils.SYS.error("Not find player when update pending withdrawal count, {}" + userKey);
					continue;
				}

				accountWithdrawalPendingCountMap.put(userKey, rs.getInt("count"));
			}

		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}

		return accountWithdrawalPendingCountMap;
	}

	public static int findDepositPendingCountByUserId(Connection conn, String userId, WebSiteType webSiteType)
		throws SQLException {

		int[] moneyTransactionTypes = {MoneyTransactionType.DEPOSIT.unique(),
			MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()};

		String sql = "SELECT COUNT(*) FROM moneytransaction WHERE status = ? AND transaction_type IN ("
			+ StringUtils.repeat("?", ",", moneyTransactionTypes.length) + ") "
			+ "AND user_id = ? AND website_type = ? ";

		List<Object> params = new ArrayList<>();

		params.add(MoneyTransactionStatusType.NEW.unique());
		Arrays.stream(moneyTransactionTypes).forEach(params::add);
		params.add(userId);
		params.add(webSiteType.unique());

		return DBQueryRunner.getNumberWithDefault(conn, sql, 0, params).intValue();
	}

	//	public static int findWithdrawalPendingCountByUserId(Connection conn, String userId, WebSiteType webSiteType)
	//		throws SQLException {
	//
	//		int[] moneyTransactionTypes = {
	//			MoneyTransactionType.WITHDRAWALS.unique(), MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()};
	//
	//		String sql = "SELECT COUNT(*) FROM moneytransaction WHERE status IN ("
	//			+ StringUtils.repeat("?", ",", MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE.length)
	//			+ ") AND transaction_type IN (" + StringUtils.repeat("?", ",", moneyTransactionTypes.length) + ") "
	//			+ "AND user_id = ? AND website_type = ? ";
	//
	//		List<Object> params = new ArrayList<>();
	//
	//		Arrays.stream(MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE).forEach(params::add);
	//		Arrays.stream(moneyTransactionTypes).forEach(params::add);
	//		params.add(userId);
	//		params.add(webSiteType.unique());
	//
	//		return DBQueryRunner.getNumberWithDefault(conn, sql, 0, params).intValue();
	//	}

	public static Map<String, List<MoneyTransaction>> getTransactionPendingByUserKeys(Connection conn,
		String[][] userKeys, Function<String, Account> getAccountFunc) throws SQLException {

		Map<String, List<MoneyTransaction>> accountWithdrawalPendingMap = new HashMap<>();

		int[] moneyTransactionTypes = {
			MoneyTransactionType.DEPOSIT.unique(), MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique(),
			MoneyTransactionType.WITHDRAWALS.unique(), MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()};

		String sql = "SELECT * "
			+ "FROM moneytransaction "
			+ "WHERE status IN (" + StringUtils.repeat("?", ",",
			MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE.length) + ") "
			+ "AND transaction_type IN (" + StringUtils.repeat("?", ",", moneyTransactionTypes.length) + ") "
			+ "AND (user_id, website_type) IN (" + OracleUtils.getGroupCondition(userKeys.length) + ") "
			+ "ORDER BY create_time DESC";

		List<Object> params = new ArrayList<>();
		Arrays.stream(MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE).forEach(params::add);
		Arrays.stream(moneyTransactionTypes).forEach(params::add);
		params.add(OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));

		WebSiteType webSiteType;
		String userKey;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), params);
			rs = ps.executeQuery();

			while (rs.next()) {
				webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = getAccountFunc.apply(userKey);

				if (userInCache == null) {
					// LogUtils.SYS.error("Not find player when update pending transaction, {}" + userKey);
					continue;
				}

				List<MoneyTransaction> moneyTransactionList = accountWithdrawalPendingMap.computeIfAbsent(userKey,
					k -> new ArrayList<>());

				MoneyTransaction moneyTransaction = new MoneyTransaction();
				moneyTransaction.setTransactionType(rs.getInt("transaction_type"));
				moneyTransaction.setWebsiteType(rs.getInt("website_type"));
				moneyTransaction.setId(rs.getLong("id"));
				moneyTransaction.setAmount(rs.getBigDecimal("amount"));
				moneyTransaction.setExchangeAmount(rs.getBigDecimal("exchange_amount"));
				moneyTransaction.setCreateTime(rs.getTimestamp("create_time"));
				moneyTransaction.setUpdateTime(rs.getTimestamp("update_time"));
				moneyTransaction.setVerifiedTime(rs.getTimestamp("verified_time"));
				moneyTransaction.setApprovedTime(rs.getTimestamp("approved_time"));
				moneyTransaction.setStatus(rs.getInt("status"));
				moneyTransaction.setToPaymentType(rs.getInt("to_payment_type"));
				moneyTransaction.setCurrency(rs.getString("currency"));
				moneyTransaction.setToBankName(rs.getString("to_bank_name"));
				moneyTransaction.setToBankBranch(rs.getString("to_bank_branch"));
				moneyTransaction.setReferenceNo(rs.getString("reference_no"));
				moneyTransaction.setFromBankNumber(rs.getString("from_bank_number"));
				moneyTransaction.setExternalMessage(rs.getString("external_message"));
				moneyTransaction.setVerifiedNote(rs.getString("verified_note"));

				moneyTransactionList.add(moneyTransaction);
			}
		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}

		return accountWithdrawalPendingMap;
	}

	public static boolean checkMoneyTransactionStatusById(Connection conn, long id,
		List<MoneyTransactionStatusType> statusList,
		List<MoneyTransactionType> transactionTypeList)
		throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM moneytransaction WHERE id = ? ");
		List<Object> params = new ArrayList<>();
		params.add(id);

		if (statusList != null && !statusList.isEmpty()) {
			sql.append(" AND STATUS in (").append(StringUtils.repeat("?", ",", statusList.size())).append(") ");
			statusList.forEach(status -> params.add(status.unique()));
		}

		if (transactionTypeList != null && !transactionTypeList.isEmpty()) {
			sql.append(" AND transaction_type in (").append(StringUtils.repeat("?", ",", transactionTypeList.size()))
				.append(") ");
			transactionTypeList.forEach(type -> params.add(type.unique()));
		}

		return DBQueryRunner.getNumber(conn, sql.toString(), params).intValue() > 0;
	}

	public static MoneyTransaction getMoneyTransactionById(Connection conn, long id)
		throws SQLException {

		String sql = "SELECT * FROM moneytransaction WHERE id = ?";

		return DBQueryRunner.getBean(conn, MoneyTransaction.class, sql, id);
	}

	public static Map<TimeIntervalType, Integer> getPendingWithdrawal(Connection conn, WebSiteType webSiteType)
		throws SQLException {

		String sql = "SELECT (EXTRACT (DAY FROM (expire_time-SYSTIMESTAMP))*24*60"
			+ "+EXTRACT (HOUR FROM (expire_time-SYSTIMESTAMP))*60"
			+ "+EXTRACT (MINUTE FROM (expire_time-SYSTIMESTAMP))) AS expire_interval "
			+ "FROM moneyTransaction "
			+ "WHERE website_type = ? "
			+ "AND transaction_type = ? "
			+ "AND status IN ("
			+ StringUtils.repeat("?", ",", MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE.length) + ") "
			+ "AND expire_time IS NOT NULL ORDER BY expire_interval";

		Map<TimeIntervalType, Integer> resultMap = new LinkedHashMap<>();
		ResultSetProcessor processor = (index, rs) -> {

			int expireInterval = rs.getInt("expire_interval");

			if (expireInterval > 0) {
				for (TimeIntervalType halfHourType : TimeIntervalType.VALUES) {
					int[] interval = halfHourType.getInterval();

					boolean isMatchInterval = false;
					if (interval.length == 1 && expireInterval >= interval[0]) {
						isMatchInterval = true;
					} else if (expireInterval >= interval[0] && expireInterval <= interval[1]) {
						isMatchInterval = true;
					}

					if (isMatchInterval) {
						int count = 0;
						if (resultMap.containsKey(halfHourType)) {
							count = resultMap.get(halfHourType);
						}
						count++;

						resultMap.put(halfHourType, count);
					}
				}
			} else {
				TimeIntervalType intervalType = TimeIntervalType.getInstance(-1);

				int count = 0;
				if (resultMap.containsKey(intervalType)) {
					count = resultMap.get(intervalType);
				}
				count++;

				resultMap.put(intervalType, count);
			}

		};

		List<Object> params = new ArrayList<>();
		params.add(webSiteType.unique());
		params.add(MoneyTransactionType.WITHDRAWALS.unique());
		Arrays.stream(MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE).forEach(params::add);

		DBQueryRunner.processResultSet(conn, processor, sql, params);

		return resultMap;
	}

	public static int awaitingWithdrawal(Connection conn, MoneyTransaction moneyTransaction, int delayTime)
		throws SQLException {
		String sql =
			"UPDATE moneyTransaction SET awaiting_verified_time = create_time + numToDSInterval(?, 'minute'), status = ?, "
				+ "executor = ?, update_time = SYSTIMESTAMP WHERE website_type = ? AND id = ? AND status = ? ";

		return DBQueryRunner.update(conn, sql, delayTime, MoneyTransactionStatusType.AWAITED.unique(),
			moneyTransaction.getExecutor(),
			moneyTransaction.getWebsiteType(), moneyTransaction.getId(), MoneyTransactionStatusType.NEW.unique());
	}

	public static int refreshUpdateTime(Connection conn, Long... ids) throws SQLException {

		StringBuilder sql = new StringBuilder(" UPDATE moneytransaction SET update_time = SYSTIMESTAMP WHERE id ");

		List<Object> param = new ArrayList<>();

		if (ids.length > 1) {
			sql.append(" IN (")
				.append(StringUtils.repeat("?", ",", ids.length))
				.append(")");
			Arrays.stream(ids).forEach(param::add);
		} else {
			sql.append(" = ? ");
			param.add(ids[0]);
		}

		return DBQueryRunner.update(conn, sql.toString(), param);
	}

	public static MoneyTransaction getMoneyTransactionByStatus(Connection conn, long id,
		List<MoneyTransactionStatusType> statusTypeList)
		throws SQLException {

		StringBuilder sql = new StringBuilder(
			"SELECT * FROM moneytransaction WHERE id = ? ");

		sql.append("AND status in (");
		for (int i = 0; i < statusTypeList.size(); i++) {
			sql.append(statusTypeList.get(i).unique());
			if (i != statusTypeList.size() - 1) {
				sql.append(",");
			}
		}
		sql.append(")");

		return DBQueryRunner
			.getBean(conn, MoneyTransaction.class, sql.toString(), id);
	}

	/// 根據user, 交易時間取得所有交易, 除了已取消的
	public static List<MoneyTransaction> getMoneyTransactionByUserAndTime(Connection conn, String userId,
		WebSiteType webSiteType, Timestamp startTime, Timestamp endTime) throws SQLException {
		String sql = "SELECT * FROM moneytransaction WHERE user_id = ? AND website_type = ? AND create_time >= ? AND create_time <= ? AND status <> ? ORDER BY create_time DESC";
		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql, userId, webSiteType.unique(), startTime,
			endTime, MoneyTransactionStatusType.REVERTED.unique());
	}

	/// 根據user取得所有交易, 除了已取消的
	public static List<MoneyTransaction> getMoneyTransactionByUser(Connection conn, String userId,
		WebSiteType webSiteType) throws SQLException {
		String sql = "SELECT * FROM moneytransaction WHERE user_id = ? AND website_type = ? AND status <> ? ORDER BY create_time DESC";
		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql, userId, webSiteType.unique(),
			MoneyTransactionStatusType.REVERTED.unique());
	}

	/// 根據user取得所有交易, 除了已取消的
	public static List<MoneyTransaction> getPagedMoneyTransactionRecord(Connection conn, String userId, int webSiteType,
		int pageNumber, int pageSize, int[] types, Timestamp startTime, Timestamp endTime)
		throws SQLException {

		List<Object> paramsList = new ArrayList<>();

		StringBuilder sqlCondition = new StringBuilder();
		sqlCondition.append("WHERE mt.user_id = ? AND mt.website_type = ? AND mt.status <> ? ");
		paramsList.add(userId);
		paramsList.add(webSiteType);
		paramsList.add(MoneyTransactionStatusType.REVERTED.unique());

		if (types.length > 0) {
			sqlCondition.append("AND mt.transaction_type IN (")
				.append(StringUtils.repeat("?", ",", types.length))
				.append(") ");
			Arrays.stream(types).forEach(paramsList::add);
		}

		if (startTime != null && endTime != null) {
			sqlCondition.append("AND mt.create_time >= ? AND mt.create_time <= ? ");
			paramsList.add(startTime);
			paramsList.add(endTime);
		}

		String sql;

		if (pageNumber > 0 && pageSize > 0) {

			pageNumber = (pageNumber - 1) * pageSize + 1;
			int rowStart = pageNumber;
			int rowEnd = rowStart + pageSize - 1;

			sql = """
				SELECT * FROM
				(
					SELECT mt.*, ROW_NUMBER() OVER (ORDER BY mt.create_time DESC) as rnum
					FROM MoneyTransaction mt
				""" + sqlCondition + """
				) WHERE rnum BETWEEN ? AND ?
				""";
			paramsList.add(rowStart);
			paramsList.add(rowEnd);
		} else {

			sql = """
				SELECT mt.* FROM MoneyTransaction mt
				""" + sqlCondition + """
				ORDER BY mt.create_time DESC
				""";
		}

		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql, paramsList);
	}

	/// 根據user取得所有交易, 除了已取消的
	public static Map<String, BigDecimal> getPagedMoneyTransactionRecordSummary(Connection conn, String userId,
		WebSiteType webSiteType)
		throws SQLException {
		Map<String, BigDecimal> result = new HashMap<>();
		String sql = "SELECT COUNT(*) AS \"totalRecords\", "
			+ "  SUM(CASE WHEN status = 0 AND transaction_type in (0,2) THEN amount ELSE 0 END) AS \"pendingDepositAmount\", "
			+ "  SUM(CASE WHEN status = 0 AND transaction_type in (1,3) THEN amount ELSE 0 END) AS \"pendingWithdrawalAmount\" "
			+ " FROM moneytransaction "
			+ " WHERE user_id = ?  AND website_type = ? ";

		var set = DBQueryRunner.queryForMap(conn, sql, userId, webSiteType.unique());

		set.forEach((k, v) -> {
			if (v instanceof BigDecimal) {
				result.put(k, (BigDecimal) v);
			} else if (v instanceof Number) {
				result.put(k, BigDecimal.valueOf(((Number) v).longValue()));
			} else {
				result.put(k, BigDecimal.ZERO);
			}
		});

		return result;
	}

	//	public static int addWithdrawal(Connection conn, MoneyTransaction moneyTransaction) throws SQLException {
	//		StringBuilder sql = new StringBuilder("INSERT INTO moneytransaction (id, "
	//			+ "user_id, website_type, vip_level, amount, real_amount, to_bank_id, to_bank_name, "
	//			+ "to_bank_account, to_bank_number, to_bank_branch, to_finance_code, to_payment_type, remark, transaction_type, "
	//			+ "status, creator, currency, bank_extra_data, "
	//			+ "bonus, create_time, update_time, amount_fee, "
	//			+ "fee_id, exchange_amount, exchange_rate, real_exchange_amount) "
	//			+ "SELECT ?, "
	//			+ "?, ?,?, ?, ?, ?, ?, "
	//			+ "?, ?, ?, ?, ?, ?, ?, "
	//			+ "?, ?, ?, ?, ?, ?, ?, "
	//			+ "?, ?, ?, ?, "
	//			+ "0, SYSTIMESTAMP, SYSTIMESTAMP, ?, "
	//			+ "?, ?, ?, ? FROM dual ");
	//
	//		List<Object> params = new ArrayList<>();
	//		params.add(moneyTransaction.getId());
	//		params.add(moneyTransaction.getUserId());
	//		params.add(moneyTransaction.getWebsiteType());
	//

	/// /		params.add(moneyTransaction.getUserId());
	/// /		params.add(moneyTransaction.getWebsiteType());
	//
	//		params.add(moneyTransaction.getVipLevel());
	//		params.add(moneyTransaction.getAmount());
	//		params.add(moneyTransaction.getRealAmount());
	//		params.add(moneyTransaction.getToBankId());
	//		params.add(moneyTransaction.getToBankName());
	//		params.add(moneyTransaction.getToBankAccount());
	//		params.add(moneyTransaction.getToBankNumber());
	//		params.add(moneyTransaction.getToBankBranch());
	//		params.add(moneyTransaction.getToFinanceCode());
	//		params.add(moneyTransaction.getToPaymentType());
	//		params.add(moneyTransaction.getRemark());
	//		params.add(moneyTransaction.getTransactionType());
	//		params.add(moneyTransaction.getStatus());
	//		params.add(moneyTransaction.getCreator());
	//		params.add(moneyTransaction.getCurrency());
	//		params.add(moneyTransaction.getBankExtraData());
	//		params.add(moneyTransaction.getAmountFee());
	//		params.add(moneyTransaction.getFeeId());
	//		params.add(moneyTransaction.getAmount());
	//		params.add(BigDecimal.ONE);
	//		params.add(moneyTransaction.getAmount());
	//
	//
	//		return DBQueryRunner.update(conn, sql.toString(), params);
	//	}
	public static int addWithdrawal(Connection conn, MoneyTransaction moneyTransaction) throws SQLException {
		StringBuilder sql = new StringBuilder("INSERT INTO moneytransaction (id, "
			+ "user_id, website_type, vip_level, amount, real_amount, to_bank_id, to_bank_name, "
			+ "to_bank_account, to_bank_number, to_bank_branch, to_finance_code, to_payment_type, remark, transaction_type, "
			+ "status, creator, currency, bank_extra_data, "
			+ "bonus, create_time, update_time, amount_fee, "
			+ "fee_id, exchange_amount, exchange_rate, real_exchange_amount) "
			+ "SELECT ?, "
			+ "?, ?, (select vip_level from account where user_id = ? and website_type = ?), ?, ?, ?, ?, "
			+ "?, ?, ?, ?, ?, ?, ?, "
			+ "?, ?, ?, ?, "
			+ "0, SYSTIMESTAMP, SYSTIMESTAMP, ?, "
			+ "?, ?, ?, ? FROM dual ");

		List<Object> params = new ArrayList<>();
		params.add(moneyTransaction.getId());
		params.add(moneyTransaction.getUserId());
		params.add(moneyTransaction.getWebsiteType());
		params.add(moneyTransaction.getUserId());
		params.add(moneyTransaction.getWebsiteType());
		params.add(moneyTransaction.getAmount());
		params.add(moneyTransaction.getRealAmount());
		params.add(moneyTransaction.getToBankId());
		params.add(moneyTransaction.getToBankName());
		params.add(moneyTransaction.getToBankAccount());
		params.add(moneyTransaction.getToBankNumber());
		params.add(moneyTransaction.getToBankBranch());
		params.add(moneyTransaction.getToFinanceCode());
		params.add(moneyTransaction.getToPaymentType());
		params.add(moneyTransaction.getRemark());
		params.add(moneyTransaction.getTransactionType());
		params.add(moneyTransaction.getStatus());
		params.add(moneyTransaction.getCreator());
		params.add(moneyTransaction.getCurrency());
		params.add(moneyTransaction.getBankExtraData());
		params.add(moneyTransaction.getAmountFee());
		params.add(moneyTransaction.getFeeId());
		params.add(moneyTransaction.getAmount());
		params.add(BigDecimal.ONE);
		params.add(moneyTransaction.getAmount());

		return DBQueryRunner.update(conn, sql.toString(), params);
	}

	public static PageResult<MoneyTransaction> findMoneyTransactionByMultiCondition(
		Connection conn, String userId, Long transactionId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		String createdBy, Timestamp createdSince, String updatedBy, Timestamp updatedSince, int websiteType,
		String sortField, DBOrderType dbOrderType, MoneyTransactionType moneyTransactionType,
		long pageNumber, long showCount
	) throws Exception {

		StringBuilder sql = new StringBuilder();
		List<Object> param = new ArrayList<>();

		sql.append("SELECT ");
		sql.append("mt.ID, mt.user_id, mt.website_type, mt.transaction_type, mt.amount, mt.status, ");
		sql.append("mt.create_time, mt.update_time, mt.approved_time, mt.approved_userid, ");
		sql.append("(CASE ");
		sql.append("  WHEN kyc.FIRST_NAME IS NULL AND kyc.LAST_NAME IS NULL THEN mt.creator ");
		sql.append("  WHEN kyc.FIRST_NAME IS NULL THEN kyc.LAST_NAME ");
		sql.append("  WHEN kyc.LAST_NAME IS NULL THEN kyc.FIRST_NAME ");
		sql.append("  ELSE kyc.FIRST_NAME || ' ' || kyc.LAST_NAME ");
		sql.append("END) AS creator ");
		sql.append("FROM MoneyTransaction mt ");
		sql.append("LEFT JOIN KYCPERSONALINFO kyc ON mt.creator = kyc.USER_ID AND mt.website_type = kyc.WEBSITE_TYPE ");

		sql.append("WHERE ");
		sql.append(" mt.website_type = ? AND mt.transaction_type = ? ");
		param.add(websiteType);
		param.add(moneyTransactionType.unique());

		if (userId != null) {
			sql.append(" AND LOWER(mt.user_id) like ?");
			param.add("%" + userId.trim().toLowerCase() + "%");
		}

		if (transactionId != null) {
			sql.append(" AND mt.ID = ?");
			param.add(transactionId);
		}

		if (status > -999) {
			sql.append(" AND mt.status = ? ");
			param.add(status);
		}

		if (minAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND mt.amount >= ?");
			param.add(minAmount);
		}

		if (maxAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND mt.amount <= ?");
			param.add(maxAmount);
		}

		if (createdSince != null) {
			sql.append(" AND mt.create_time >= ?");
			param.add(createdSince);
		}

		if (updatedBy != null) {
			sql.append(" AND LOWER(mt.approved_userid) like ?");
			param.add("%" + updatedBy.trim().toLowerCase() + "%");
		}

		//		TODO: 調整參數命名
		//		確定後台管控以 approve_time 為主的話,傳入參數要改叫 approvedSince

		if (updatedSince != null) {
			sql.append(" AND mt.approved_time >=?");
			param.add(updatedSince);

		}

		if (createdBy != null && !createdBy.trim().isEmpty()) {
			sql.append(" AND (");
			sql.append("   LOWER(kyc.FIRST_NAME || ' ' || kyc.LAST_NAME) like ? ");
			sql.append("   OR LOWER(kyc.LAST_NAME || ' ' || kyc.FIRST_NAME) like ? ");
			sql.append("   OR LOWER(kyc.FIRST_NAME) like ? ");
			sql.append("   OR LOWER(kyc.LAST_NAME) like ? ");
			sql.append("   OR (kyc.FIRST_NAME IS NULL AND kyc.LAST_NAME IS NULL AND LOWER(mt.creator) like ?) ");
			sql.append(" )");

			String searchTerm = "%" + createdBy.trim().toLowerCase() + "%";
			param.add(searchTerm);
			param.add(searchTerm);
			param.add(searchTerm);
			param.add(searchTerm);
			param.add(searchTerm);
		}

		if (sortField != null) {
			String finalSortField = switch (sortField) {
				case "creator" -> "LOWER((CASE "
					+ "  WHEN kyc.FIRST_NAME IS NULL AND kyc.LAST_NAME IS NULL THEN mt.creator "
					+ "  WHEN kyc.FIRST_NAME IS NULL THEN kyc.LAST_NAME "
					+ "  WHEN kyc.LAST_NAME IS NULL THEN kyc.FIRST_NAME "
					+ "  ELSE kyc.FIRST_NAME || ' ' || kyc.LAST_NAME "
					+ "END))";
				case "status" -> " CASE mt.status"
					+ " WHEN 0 THEN 'Pending'"
					+ " WHEN 2 THEN 'Approved'"
					+ " WHEN -2 THEN 'Disapproved'"
					+ " END ";
				case "approved_userid" -> "LOWER(mt.approved_userid)";
				case "id" -> "mt.id";
				case "user_id" -> "LOWER(mt.user_id)";
				case "amount" -> "mt.amount";
				case "create_time" -> "mt.create_time";
				case "update_time" -> "mt.update_time";
				case "approved_time" -> "mt.approved_time";
				default -> "mt.create_time";
			};
			sql.append(" ORDER BY ").append(finalSortField);

			if (dbOrderType != null) {
				sql.append(dbOrderType.getSqlString()).append(" NULLS LAST");
			}
		}

		return DBQueryRunner.getPageResult(conn, MoneyTransaction.class, sql.toString(), pageNumber, showCount, param);
	}

	public static List<MoneyTransaction> getBatchMoneyTransactionsByIds(Connection conn, List<Long> ids,
		String sortField, DBOrderType dbOrderType) throws SQLException {

		StringBuilder sql = new StringBuilder("Select * from MoneyTransaction where id IN (");

		sql.append(StringUtils.repeat("?", ",", ids.size()) + ")");

		if (sortField != null) {
			sql.append(" ORDER BY ").append(sortField).append(" ").append(dbOrderType.getSqlString());
		}

		List<Object> param = new ArrayList<>(ids);

		return DBQueryRunner.getBeanList(conn, MoneyTransaction.class, sql.toString(), param);

	}

	public static PageResult<MoneyTransaction> findMoneyTransactionRecord(
		Connection conn, String userId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		int websiteType, Timestamp createdStart, Timestamp createdEnd, MoneyTransactionType moneyTransactionType,
		String sortField, DBOrderType dbOrderType,
		long pageNumber, long showCount
	) throws Exception {

		StringBuilder sql = new StringBuilder();
		List<Object> param = new ArrayList<>();

		sql.append("SELECT DISTINCT ");
		sql.append("mt.ID, mt.user_id, mt.website_type, mt.transaction_type, mt.amount, mt.status, ");
		sql.append("mt.create_time, mt.update_time, mt.approved_userid, mt.approved_time,");
		sql.append("(CASE ");
		sql.append("  WHEN kyc.FIRST_NAME IS NULL AND kyc.LAST_NAME IS NULL THEN mt.creator ");
		sql.append("  WHEN kyc.FIRST_NAME IS NULL THEN kyc.LAST_NAME ");
		sql.append("  WHEN kyc.LAST_NAME IS NULL THEN kyc.FIRST_NAME ");
		sql.append("  ELSE kyc.FIRST_NAME || ' ' || kyc.LAST_NAME ");
		sql.append("END) AS creator ");
		sql.append("FROM MoneyTransaction mt ");
		sql.append("LEFT JOIN KYCPERSONALINFO kyc ON mt.creator = kyc.USER_ID AND mt.website_type = kyc.WEBSITE_TYPE ");

		sql.append("WHERE ");
		sql.append(" mt.website_type = ? AND mt.transaction_type = ? ");
		param.add(websiteType);
		param.add(moneyTransactionType.unique());

		if (userId != null) {
			sql.append(" AND mt.user_id =  ?");
			param.add(userId);
		}

		if (status > -999) {
			sql.append(" AND mt.status = ? ");
			param.add(status);
		}
		if (minAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND mt.amount >= ?");
			param.add(minAmount);
		}
		if (maxAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND mt.amount <= ?");
			param.add(maxAmount);
		}

		if (createdStart != null && createdEnd != null) {
			sql.append(" AND mt.create_time >= ? AND mt.create_time <= ?");
			param.add(createdStart);
			param.add(createdEnd);
		}

		if (sortField != null) {
			String finalSortField;
			if ("creator".equals(sortField) || "mt.creator".equals(sortField)) {
				// 直接使用 SELECT 中的 creator 别名
				finalSortField = "LOWER(creator)";
			} else if ("status".equals(sortField) || "mt.status".equals(sortField)) {
				// 直接使用 mt.status，让前端处理显示逻
				finalSortField = " CASE mt.status"
					+ " WHEN 0 THEN 'Pending'"
					+ " WHEN 2 THEN 'Approved'"
					+ " WHEN -2 THEN 'Disapproved'"
					+ " END ";
			} else if ("approved_userid".equals(sortField)) {
				// 添加表别名
				finalSortField = "LOWER(mt.approved_userid)";
			} else if ("id".equalsIgnoreCase(sortField)) {
				finalSortField = "mt.ID";
			} else if ("user_id".equals(sortField)) {
				finalSortField = "LOWER(mt.user_id)";
			} else if ("amount".equals(sortField)) {
				finalSortField = "mt.amount";
			} else if ("create_time".equals(sortField)) {
				finalSortField = "mt.create_time";
			} else if ("update_time".equals(sortField)) {
				finalSortField = "mt.update_time";
			} else if ("approved_time".equals(sortField)) {
				finalSortField = "mt.approved_time";
			} else {
				// 默认处理，确保有表别名
				finalSortField = sortField.startsWith("mt.") ? sortField : "mt." + sortField;
			}
			sql.append(" ORDER BY ").append(finalSortField);

			if (dbOrderType != null) {
				sql.append(dbOrderType.getSqlString()).append(" NULLS LAST");

			}
		}

		return DBQueryRunner.getPageResult(conn, MoneyTransaction.class, sql.toString(), pageNumber, showCount, param);
	}

	public static BigDecimal getGrandTotalOfMoneyTransaction(Connection conn, String userId, int status,
		BigDecimal minAmount,
		BigDecimal maxAmount,
		int websiteType, Timestamp createdStart, Timestamp createdEnd, List<MoneyTransactionType> moneyTransactionTypes)
		throws SQLException {
		StringBuilder sql = new StringBuilder(
			"select SUM(amount) from MoneyTransaction where website_type = ? ");
		List<Object> param = new ArrayList<>();
		param.add(websiteType);
		sql.append(" AND transaction_type IN (");
		sql.append(String.join(",", Collections.nCopies(moneyTransactionTypes.size(), "?")));
		sql.append(")");
		moneyTransactionTypes.forEach(type -> param.add(type.unique()));

		if (userId != null) {
			sql.append(" AND user_id =  ?");
			param.add(userId);
		}

		sql.append(" AND status = ? ");
		param.add(status);

		if (minAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND amount >= ?");
			param.add(minAmount);
		}
		if (maxAmount.compareTo(BigDecimal.ZERO) > 0) {
			sql.append(" AND amount <= ?");
			param.add(maxAmount);
		}

		if (createdStart != null && createdEnd != null) {
			sql.append(" AND create_time >= ? AND create_time <= ?");
			param.add(createdStart);
			param.add(createdEnd);
		}

		Number total = DBQueryRunner.getNumberWithDefault(conn, sql.toString(), BigDecimal.ZERO, param);

		if (total == null) {
			return BigDecimal.ZERO;
		}

		return new BigDecimal(total.toString());
	}

	public static int approveRejectWithdrawalBO(Connection conn, List<Long> ids, int status, String approvedUserId)
		throws SQLException {
		String sql = "update MoneyTransaction set update_time = SYSTIMESTAMP, status = ?, "
			+ "approved_time = SYSTIMESTAMP, approved_userid = ? "
			+ "where id in (" + StringUtils.repeat("?", ",", ids.size()) + ") ";

		List<Object> param = new ArrayList<>();
		param.add(status);
		param.add(approvedUserId);
		param.addAll(ids);
		return DBQueryRunner.update(conn, sql, param);

	}

	public static String findAdjustmentByMultiCondition(Connection conn, Long transactionIdCondition,
		String userId, Date createDateStart, String creator,
		int currencyType, String sortCondition, DBOrderType orderType, WebSiteType webSiteType,
		PageInfo pageInfo, Set<CurrencyType> managerCurrencySet, int transactionType)
		throws Exception {

		String selectSql =
			"SELECT moneytransaction.website_type, moneytransaction.user_id, moneytransaction.create_time, "
				+ "creator, moneytransaction.id, amount, currency ";
		StringBuilder sharedSql = new StringBuilder("FROM moneytransaction ");

		List<Object> params = new ArrayList<>();
		prepareShareSqlForAdjustment(sharedSql, params,
			transactionIdCondition, userId,
			createDateStart, creator,
			currencyType, webSiteType, managerCurrencySet, transactionType);

		String countSql = " SELECT COUNT(*) " + sharedSql;
		int totalCount = DBQueryRunner.getNumber(conn, countSql, params).intValue();
		pageInfo.setTotalCount(totalCount);

		params.add(pageInfo.getLastRowNumber());
		params.add(pageInfo.getFirstRowNumber());

		String pageSQL = OracleUtils.getCalculatedPageSQL(
			selectSql + sharedSql + " ORDER BY " + sortCondition + orderType.getSqlString());

		AtomicInteger pageCount = new AtomicInteger();
		JsonValueProcessor processor = (index, rs, jGenerator) -> {
			pageCount.getAndIncrement();

			String transactionUserId = rs.getString("user_id");
			final Account account = AccountDAO.getAccountByUserId(conn, transactionUserId, webSiteType);
			final int currencyTypeId = account.getCurrencyTypeId();

			jGenerator.writeStartObject();
			jGenerator.writeStringField("userId", rs.getString("user_id"));
			jGenerator.writeNumberField("createTime", rs.getTimestamp("create_time").getTime());
			jGenerator.writeStringField("createTimeStr", FormatUtils.dateFormat(rs.getTimestamp("create_time")));
			jGenerator.writeStringField("creator", rs.getString("creator"));
			jGenerator.writeStringField("id",
				MoneyTransactionUtils.formatId(rs.getLong("id"), MoneyTransactionType.ADJUSTMENT));
			jGenerator.writeNumberField("amount", rs.getDouble("amount"));
			jGenerator.writeStringField("currencyType", rs.getString("currency"));
			jGenerator.writeNumberField("currencyTypeId", currencyTypeId);
			jGenerator.writeEndObject();
		};

		if (pageInfo.getPageSize() <= 0) {
			pageInfo.setTotalCount(pageCount.get());
		}

		return DBQueryRunner.processJsonArrayValue(conn, processor, pageSQL, params);
	}

	private static void prepareShareSqlForAdjustment(StringBuilder sharedSql, List<Object> params,
		Long transactionIdCondition, String userId, Date createDateStart, String creator, int currencyType,
		WebSiteType webSiteType,
		Set<CurrencyType> managerCurrencySet, int transactionType) {

		sharedSql.append("LEFT JOIN account "
			+ "ON account.user_id = moneytransaction.user_id AND account.website_type = moneytransaction.website_type ");

		sharedSql.append("WHERE moneytransaction.website_type = ? ");
		params.add(webSiteType.unique());

		if (null != createDateStart) {
			sharedSql.append("AND moneytransaction.create_time >= ? ");
			params.add(new Timestamp(createDateStart.getTime()));
		}

		if (transactionType == -1) {
			sharedSql.append("AND transaction_type in (?,?) ");
			params.add(MoneyTransactionType.ADJUSTMENT.unique());
			params.add(MoneyTransactionType.REVENUE_ADJUSTMENT.unique());
		} else {
			sharedSql.append("AND transaction_type = ? ");
			params.add(transactionType);
		}

		if (null != transactionIdCondition) {
			sharedSql.append("AND moneytransaction.id = ? ");
			params.add(transactionIdCondition);
		}
		if (null != userId) {
			sharedSql.append("AND LOWER(moneytransaction.user_id) LIKE ? ");
			params.add("%" + userId.trim().toLowerCase() + "%");
		}
		if (null != creator) {
			sharedSql.append("AND LOWER(creator) LIKE ? ");
			params.add("%" + creator.trim().toLowerCase() + "%");
		}
		if (-1 != currencyType) {
			sharedSql.append("AND currency = ? ");
			params.add(CurrencyType.getInstance(currencyType).name());
		} else {
			sharedSql.append(" AND currency IN (").append(StringUtils.repeat("?", ",", managerCurrencySet.size()))
				.append(")");
			managerCurrencySet.stream().map(Enum::name).forEach(params::add);
		}
	}

	public static int addAdjustment(Connection conn, MoneyTransaction moneyTransaction, Timestamp now)
		throws SQLException {

		long moneyTransactionId = getMoneyTransactionIdSeq(conn);
		moneyTransaction.setId(moneyTransactionId);

		String sql = "INSERT INTO moneytransaction (id, "
			+ "user_id, website_type, vip_level, amount, transaction_type, status, "
			+ "creator, approved_userid, approved_note, remark, currency, "
			+ "bonus, create_time, update_time, approved_time, amount_fee, real_amount, "
			+ "exchange_amount, exchange_rate, real_exchange_amount) "
			+ "VALUES (?, "
			+ "?, ?, (select vip_level from account where user_id = ? and website_type = ?), ?, ?, ?, "
			+ "?, ?, ?, ?, ?, "
			+ "0, ?, ?, ?, 0, ?, "
			+ "?, ?, ?)";

		return DBQueryRunner.update(conn, sql,
			moneyTransaction.getId(),
			moneyTransaction.getUserId(), moneyTransaction.getWebsiteType(), moneyTransaction.getUserId(),
			moneyTransaction.getWebsiteType(), moneyTransaction.getAmount(), moneyTransaction.getTransactionType(),
			moneyTransaction.getStatus(),
			moneyTransaction.getCreator(), moneyTransaction.getApprovedUserid(), moneyTransaction.getApprovedNote(),
			moneyTransaction.getRemark(), moneyTransaction.getCurrency(),
			now, now, now, moneyTransaction.getAmount(),
			moneyTransaction.getAmount(), BigDecimal.ONE, moneyTransaction.getAmount());
	}
}
