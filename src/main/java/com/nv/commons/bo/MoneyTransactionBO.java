package com.nv.commons.bo;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountCardCache;
import com.nv.commons.cache.MoneyTransactionCache;
import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.constants.APIResponseType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeIntervalType;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.constants.WalletTransactionStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountBankDao;
import com.nv.commons.dao.AccountCardDAO;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dao.MoneyTransactionDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountBank;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.MoneyTransactionUtils;
import com.nv.commons.utils.PageUtils;
import com.nv.commons.utils.PaymentGatewayUtils;
import com.nv.commons.utils.ThreadLocalUtils;
import com.nv.commons.utils.Validator;
import com.nv.module.openapi.dto.moneytransaction.MoneyTransactionDetailResult;
import com.nv.module.openapi.dto.moneytransaction.MoneyTransactionRecordPagedResult;
import com.nv.module.openapi.dto.moneytransaction.MoneyTransactionRecordResult;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;

public class MoneyTransactionBO {

	public static boolean rejectDeposit(MoneyTransaction... moneyTransactions) {
		//		return rejectDeposit(null, null, moneyTransactions);
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			for (MoneyTransaction moneyTransaction : moneyTransactions) {

				WebSiteType webSiteType = WebSiteType.getInstance(moneyTransaction.getWebsiteType());
				String userId = moneyTransaction.getUserId();
				String userKey = AccountUtils.getUserKey(webSiteType.unique(), userId);

				String walletReferenceNo = moneyTransaction.getWalletReferenceNo();

				SeamlessWalletApiService.getInstance()
					.completeDepositAndGetNewBalance(userKey, WalletTransactionStatusType.FAILED, walletReferenceNo,
						moneyTransaction);

				moneyTransaction.setStatus(MoneyTransactionStatusType.CLOSE.unique());
				moneyTransaction.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				MoneyTransaction moneyTransactionDB =
					MoneyTransactionDAO.getMoneyTransactionById(conn, moneyTransaction.getId());
				if (null != moneyTransactionDB) {
					// for DI Event
					moneyTransaction.setCurrency(moneyTransactionDB.getCurrency());
					MoneyTransactionDAO.rejectDeposit(conn, moneyTransaction, null);
					AccountDAO.updateUpdatedAttribute(
						conn,
						moneyTransaction.getUserId(),
						WebSiteType.getInstance(moneyTransaction.getWebsiteType()),
						UpdatedAttributeType.MULTIPLE_TRANSACTION);
				}
			}
			MoneyTransactionDAO.refreshUpdateTime(
				conn, Arrays.stream(moneyTransactions).map(MoneyTransaction::getId).toArray(Long[]::new));

			conn.commit();

		} catch (Deviation e) {
			DbUtils.rollback(conn);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}
		return true;

	}

	public static boolean rejectDeposit(LangMessage lang, String rejectUserId, MoneyTransaction... moneyTransactions) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			for (MoneyTransaction moneyTransaction : moneyTransactions) {
				if (moneyTransaction.getStatus() != MoneyTransactionStatusType.NEW.unique()) {
					continue;
				}

				WebSiteType webSiteType = WebSiteType.getInstance(moneyTransaction.getWebsiteType());
				String userId = moneyTransaction.getUserId();
				String userKey = AccountUtils.getUserKey(webSiteType.unique(), userId);

				String walletReferenceNo = moneyTransaction.getWalletReferenceNo();
				SeamlessWalletApiService.getInstance()
					.completeDepositAndGetNewBalance(userKey, WalletTransactionStatusType.FAILED, walletReferenceNo,
						moneyTransaction);

				moneyTransaction.setStatus(MoneyTransactionStatusType.CLOSE.unique());
				moneyTransaction.setApprovedUserid(rejectUserId);
				moneyTransaction.setUpdateTime(new Timestamp(System.currentTimeMillis()));
				MoneyTransaction moneyTransactionDB =
					MoneyTransactionDAO.getMoneyTransactionById(conn, moneyTransaction.getId());
				if (null != moneyTransactionDB) {
					// for DI Event
					moneyTransaction.setCurrency(moneyTransactionDB.getCurrency());
					MoneyTransactionDAO.rejectDeposit(conn, moneyTransaction, lang);
					AccountDAO.updateUpdatedAttribute(
						conn,
						moneyTransaction.getUserId(),
						WebSiteType.getInstance(moneyTransaction.getWebsiteType()),
						UpdatedAttributeType.MULTIPLE_TRANSACTION);
				}

				WebSocketBO.sendDepositLimitUsageUpdate(AccountCache.getInstance()
					.getAccount(moneyTransaction.getWebsiteType(), moneyTransaction.getUserId()));
			}

			MoneyTransactionDAO.refreshUpdateTime(
				conn, Arrays.stream(moneyTransactions).map(MoneyTransaction::getId).toArray(Long[]::new));
			conn.commit();

		} catch (Deviation e) {
			DbUtils.rollback(conn);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}
		return true;
	}

	public static boolean approveDepositByPG(MoneyTransaction moneyTransaction) {
		Connection conn = null;
		MoneyTransaction deposit;
		try {
			conn = DBPool.getReadConnection();
			deposit = MoneyTransactionDAO.getDepositInfoForPG(conn, moneyTransaction.getId(), false);
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		if (null == deposit) {
			return false;
		}
		moneyTransaction.setStatus(MoneyTransactionStatusType.CONFIRMED.unique());

		return approveDeposit(deposit, moneyTransaction);
	}

	public static boolean approvePaymentDepositByBoCheckOrder(MoneyTransaction moneyTransaction) {
		Connection conn = null;
		MoneyTransaction deposit;
		try {
			conn = DBPool.getReadConnection();
			deposit =
				MoneyTransactionDAO.getMoneyTransactionByStatus(
					conn,
					moneyTransaction.getId(),
					Arrays.asList(MoneyTransactionStatusType.NEW, MoneyTransactionStatusType.CLOSE));
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		if (null == deposit) {
			return false;
		}
		moneyTransaction.setStatus(MoneyTransactionStatusType.CONFIRMED.unique());

		return approveDeposit(deposit, moneyTransaction);
	}

	// 只有account不是suspend才可做
	// 目前這支是共用的，approve local bank、manual/ auto approve PaymentGateway
	private static boolean approveDeposit(
		MoneyTransaction createTxInfo, MoneyTransaction approveTxInfo) {

		int updateCount;

		Timestamp approveTime = new Timestamp(System.currentTimeMillis());

		final int webSiteType = createTxInfo.getWebsiteType();

		WebsiteInfo websiteInfo = WebsiteInfoCache.getInstance().getByWebType(webSiteType);

		if (websiteInfo == null) {
			return false;
		}

		final WebSiteType webSiteTypeObj = WebSiteType.getInstance(webSiteType);
		final String userId = createTxInfo.getUserId();
		// manager who approves this deposit
		final String managerId = approveTxInfo.getApprovedUserid();

		// not null
		final BigDecimal depositAmount = createTxInfo.getAmount();

		Account account;
		Connection connLock = null;
		try {
			connLock = DBPool.getWriteConnection();
			connLock.setAutoCommit(false);

			account = AccountDAO.selectForUpdateForApprove(connLock, userId, webSiteType);

			if (AccountStatusType.SUSPEND.unique() == account.getStatus()) {
				throw new Deviation(APIResponseType.ACCOUNT_IS_SUSPENDED.getI18nKey());
			}
			String autoAppendMsg = "";

			/*
			 * check remark before update Account
			 */
			approveTxInfo.setApprovedNote(approveTxInfo.getApprovedNote() + autoAppendMsg);

			String userKey = AccountUtils.getUserKey(
				webSiteType,
				userId
			);

			BigDecimal oldBalance = SeamlessWalletApiService.getInstance().getBalance(userKey);

			String walletReferenceNo = createTxInfo.getWalletReferenceNo();
			BigDecimal newBalance = SeamlessWalletApiService.getInstance()
				.completeDepositAndGetNewBalance(userKey, WalletTransactionStatusType.COMPLETED, walletReferenceNo,
					createTxInfo);

			// 紀錄 玩家 金額異動
			if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {

				LogUtils.balance.debug(
					JSONUtils.getJSONString(
						"Action", "approve deposit",
						"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
						"bonusId", String.valueOf(createTxInfo.getId()),
						"Player", userId,
						"WebsiteType", String.valueOf(account.getWebsiteType()),
						"BeforeBalance", oldBalance.toPlainString(),
						"AfterBalance", newBalance.add(depositAmount).toPlainString()));
			}

			approveTxInfo.setApprovedTime(approveTime);
			// for DI Event
			approveTxInfo.setUserId(createTxInfo.getUserId());
			approveTxInfo.setAmount(createTxInfo.getAmount());
			approveTxInfo.setCurrency(createTxInfo.getCurrency());

			updateCount = MoneyTransactionDAO.approveDeposit(connLock, approveTxInfo);

			LogUtils.moneyTransaction.info(
				JSONUtils.getJSONString(
					"Action",
					"approve deposit",
					"Time",
					String.valueOf(new Timestamp(System.currentTimeMillis())),
					"ApproveUser",
					managerId,
					"Player",
					userId,
					"ApproveTxnInfo",
					JSONUtils.toJsonString(approveTxInfo),
					"CreateTxnInfo",
					JSONUtils.toJsonString(createTxInfo)));

			// why refreshUpdateTime of txn?
			MoneyTransactionDAO.refreshUpdateTime(connLock, createTxInfo.getId());
			// unlock account
			connLock.commit();

		} catch (Deviation e) {
			DbUtils.rollback(connLock);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(connLock);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(connLock);
		}

		if (updateCount > 0) {
			Connection conn2 = null;
			try {
				conn2 = DBPool.getWriteConnection();
				conn2.setAutoCommit(false);

				boolean isFirstDeposit = (null == account.getFirstDepositTime());
				AccountDAO.updateForApproveDeposit(conn2, account, isFirstDeposit, depositAmount, approveTime);

				conn2.commit();

			} catch (Exception e) {
				DbUtils.rollback(conn2);
				LogUtils.SYS.error(e.getMessage(), e);
			} finally {
				DbUtils.close(conn2);
			}

		}

		return updateCount > 0;
	}

	@Deprecated
	public static void awaitingVerifyWithdrawal(
		MoneyTransaction targetMoneyTransaction, int delayMinutes) {

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			MoneyTransaction moneyTransaction = new MoneyTransaction();
			moneyTransaction.setExecutor("SYS");
			moneyTransaction.setId(targetMoneyTransaction.getId());
			moneyTransaction.setWebsiteType(targetMoneyTransaction.getWebsiteType());

			MoneyTransactionDAO.awaitingWithdrawal(conn, moneyTransaction, delayMinutes);

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

	}

	public static void settledWithdrawalByPaymentGateway(
		MoneyTransaction moneyTransaction, boolean callBackSuccess, String note) throws Exception {

		Connection conn = null;
		int result;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			StringBuilder sb = new StringBuilder();
			if (note != null) {
				String oldApproveNote = moneyTransaction.getApprovedNote();
				if (oldApproveNote != null && !oldApproveNote.isEmpty()) {
					sb.append(oldApproveNote);
					sb.append(";");
				}
				sb.append(note);
				if (callBackSuccess) {
					sb.append("(System)");
				}
			}
			moneyTransaction.setApprovedNote(sb.toString());

			if (callBackSuccess) {
				moneyTransaction.setStatus(MoneyTransactionStatusType.CONFIRMED.unique());
				result =
					MoneyTransactionDAO.approveWithdrawal(
						conn,
						moneyTransaction,
						MoneyTransactionStatusType.PROCESSING,
						MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY);

				LogUtils.moneyTransaction.info(
					JSONUtils.getJSONString(
						"Action", "approve withdrawal",
						"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
						"ApproveUser", moneyTransaction.getApprovedUserid(),
						"Player", moneyTransaction.getUserId(),
						"TxnInfo", JSONUtils.toJsonString(moneyTransaction)));

			} else {

				moneyTransaction.setStatus(MoneyTransactionStatusType.CLOSE.unique());

				result = MoneyTransactionDAO.rejectWithdrawalByPaymentGateway(conn, moneyTransaction);

				if (result > 0) {
					long moneyTransactionId = moneyTransaction.getId();

					boolean isExist = MoneyTransactionDAO.checkMoneyTransactionStatusById(conn, moneyTransactionId,
						Arrays.asList(MoneyTransactionStatusType.CLOSE, MoneyTransactionStatusType.REVERTED
							, MoneyTransactionStatusType.REJECTED),
						Arrays.asList(MoneyTransactionType.WITHDRAWALS,
							MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY));

					if (!isExist) {
						LogUtils.moneyTransaction.error("MoneyTransaction({}) status is not reject or withdrawal.",
							moneyTransactionId);
					} else {

						AccountDAO.updateForRejectWithdrawal(
							conn,
							moneyTransaction.getAmount(),
							moneyTransaction.getUserId(),
							moneyTransaction.getWebsiteType(),
							moneyTransactionId);
					}
				}

				LogUtils.moneyTransaction.info(
					JSONUtils.getJSONString(
						"Action", "reject withdrawal",
						"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
						"ApproveUser", moneyTransaction.getApprovedUserid(),
						"Player", moneyTransaction.getUserId(),
						"TxnInfo", JSONUtils.toJsonString(moneyTransaction)));
			}

			MoneyTransactionDAO.refreshUpdateTime(conn, moneyTransaction.getId());
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.close(conn);
		}

		// approve 成功才需要處理dashboard跟account身上的withdrawal
		if (result > 0) {
			if (callBackSuccess) {
				confirmWithdrawal(moneyTransaction.getUserId(),
					WebSiteType.getInstance(moneyTransaction.getWebsiteType()), moneyTransaction.getApprovedTime());
			} else {
				LogUtils.moneyTransaction.info(JSONUtils.getJSONString("Action", "PG reject withdrawal", "Time",
					String.valueOf(new Timestamp(System.currentTimeMillis())), "Rejector",
					moneyTransaction.getApprovedUserid(), "Player", moneyTransaction.getUserId(), "RejectTxInfo",
					JSONUtils.toJsonString(moneyTransaction)));
			}
		}
	}

	public static void confirmWithdrawal(
		String userId,
		WebSiteType webSiteType,
		Timestamp now) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			Account account = AccountDAO.getAccountByUserId(conn, userId, webSiteType);

			boolean isFirstWithdrawal = account.getFirstWithdrawalTime() == null;

			AccountDAO.updateForApproveWithdrawal(conn, userId, webSiteType, isFirstWithdrawal, now);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

	}

	public static boolean boApproveWithdrawal(List<Long> ids, String managerUserId) throws Exception {
		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(ids, null, null).stream()
			.filter(x -> x.getStatus() == MoneyTransactionStatusType.NEW.unique()).toList();
		Connection conn = null;
		int result = 0;

		List<Long> validIds = moneyTransactionList.stream().map(MoneyTransaction::getId).toList();
		if (validIds.isEmpty()) {
			return true;
		}
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			result = MoneyTransactionDAO.approveRejectWithdrawalBO(conn, validIds,
				MoneyTransactionStatusType.CONFIRMED.unique(),
				managerUserId);

			if (result <= 0) {
				throw new Exception("Failed to approve withdrawal transactions");
			}

			for (MoneyTransaction moneyTransaction : moneyTransactionList) {
				confirmWithdrawalInTransaction(conn, moneyTransaction.getUserId(),
					WebSiteType.getInstance(moneyTransaction.getWebsiteType()),
					new Timestamp(System.currentTimeMillis()));
			}

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		return result >= 0;

	}

	private static void confirmWithdrawalInTransaction(
		Connection conn,
		String userId,
		WebSiteType webSiteType,
		Timestamp now) throws Exception {

		Account account = AccountDAO.getAccountByUserId(conn, userId, webSiteType);
		boolean isFirstWithdrawal = account.getFirstWithdrawalTime() == null;

		AccountDAO.updateForApproveWithdrawal(conn, userId, webSiteType, isFirstWithdrawal, now);
	}

	public static String getDashboardDeposit(
		Timestamp startDate,
		Timestamp endDate,
		CurrencyType currencyType,
		WebSiteType webSiteType,
		long pageNumber,
		long showCount,
		String column,
		DBOrderType orderType,
		LangMessage lang) {
		Connection conn = null;
		String result;
		try {
			conn = DBPool.getReadConnection();

			result =
				MoneyTransactionDAO.getTotalMoneyTxn(
					conn,
					new int[] {MoneyTransactionStatusType.CONFIRMED.unique()},
					new int[] {
						MoneyTransactionType.DEPOSIT.unique(),
						MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()
					},
					startDate,
					endDate,
					currencyType,
					webSiteType,
					pageNumber,
					showCount,
					column,
					orderType,
					lang);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
		return result;
	}

	public static String getPendingDashboardDeposit(
		CurrencyType currencyType,
		WebSiteType webSiteType,
		long pageNumber,
		long showCount,
		String column,
		DBOrderType orderType,
		LangMessage lang) {
		Connection conn = null;
		String result;
		try {
			conn = DBPool.getReadConnection();

			result =
				MoneyTransactionDAO.getTotalMoneyTxn(
					conn,
					new int[] {MoneyTransactionStatusType.NEW.unique()},
					new int[] {
						MoneyTransactionType.DEPOSIT.unique(),
						MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()
					},
					null,
					null,
					currencyType,
					webSiteType,
					pageNumber,
					showCount,
					column,
					orderType,
					lang);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
		return result;
	}

	public static String getDashboardWithdrawal(
		Timestamp startDate,
		Timestamp endDate,
		CurrencyType currencyType,
		WebSiteType webSiteType,
		long pageNumber,
		long showCount,
		String column,
		DBOrderType orderType,
		LangMessage lang) {
		Connection conn = null;
		String result;
		try {
			conn = DBPool.getReadConnection();

			int[] type =
				new int[] {
					MoneyTransactionType.WITHDRAWALS.unique(),
					MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()
				};

			result =
				MoneyTransactionDAO.getTotalMoneyTxn(
					conn,
					new int[] {MoneyTransactionStatusType.CONFIRMED.unique()},
					type,
					startDate,
					endDate,
					currencyType,
					webSiteType,
					pageNumber,
					showCount,
					column,
					orderType,
					lang);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
		return result;
	}

	public static String getPendingDashboardWithdrawal(
		CurrencyType currencyType,
		WebSiteType webSiteType,
		long pageNumber,
		long showCount,
		String column,
		DBOrderType orderType,
		LangMessage lang) {
		Connection conn = null;
		String result;
		try {
			conn = DBPool.getReadConnection();

			int[] type =
				new int[] {
					MoneyTransactionType.WITHDRAWALS.unique(),
					MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()
				};

			result =
				MoneyTransactionDAO.getTotalMoneyTxn(
					conn,
					MoneyTransactionStatusType.WITHDRAWAL_PENDING_UNIQUE,
					type,
					null,
					null,
					currencyType,
					webSiteType,
					pageNumber,
					showCount,
					column,
					orderType,
					lang);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		} finally {
			DbUtils.close(conn);
		}
		return result;
	}

	public static MoneyTransaction getPaymentGatewayDepositDetail(String id) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return MoneyTransactionDAO.getDepositInfoForPG(conn, Long.parseLong(id), false);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return null;
	}

	public static MoneyTransaction getPaymentGatewayDepositDetailByReferenceNo(String referenceNo) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return MoneyTransactionDAO.getDepositInfoByReferenceNoForPG(conn, referenceNo, false);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return new MoneyTransaction();
	}

	public static boolean updatePaymentGatewayDepositDetail(MoneyTransaction moneyTransaction) {
		Connection conn = null;
		boolean result = false;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			result = MoneyTransactionDAO.updateDepositForPG(conn, moneyTransaction) > 0;
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		return result;
	}

	public static boolean updatePaymentGatewayDepositDetailWithProof(
		MoneyTransaction moneyTransaction) {
		Connection conn = null;
		boolean result = false;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			result = MoneyTransactionDAO.updateDepositForPGWithProof(conn, moneyTransaction) > 0;
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		return result;
	}

	public static MoneyTransaction getPaymentGatewayWithdrawalDetail(String id) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return MoneyTransactionDAO.getPaymentGatewayWithdrawalDetail(conn, Long.parseLong(id));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return null;
	}

	public static MoneyTransaction getPaymentGatewayWithdrawalDetailByReferenceNo(
		String referenceNo) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return MoneyTransactionDAO.getPaymentGatewayWithdrawalDetailByReferenceNo(conn, referenceNo);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		return new MoneyTransaction();
	}

	public static Map<TimeIntervalType, Integer> getPendingWithdrawal(WebSiteType webSiteType)
		throws Exception {
		try (Connection conn = DBPool.getReadConnection()) {
			return MoneyTransactionDAO.getPendingWithdrawal(conn, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}
	}

	public static boolean createDeposit(MoneyTransaction moneyTransaction) {

		final BigDecimal depositAmount = moneyTransaction.getAmount();
		final int webSiteType = moneyTransaction.getWebsiteType();
		final String userId = moneyTransaction.getUserId();

		final BigDecimal amountFee = moneyTransaction.getAmountFee();
		if (null == amountFee) {
			moneyTransaction.setAmountFee(BigDecimal.ZERO);
		}

		moneyTransaction.setStatus(MoneyTransactionStatusType.NEW.unique());
		moneyTransaction.setBonusTitle("NO Bonus");
		moneyTransaction.setExchangeAmount(depositAmount);
		moneyTransaction.setExchangeRate(BigDecimal.ONE);
		moneyTransaction.setRealExchangeAmount(depositAmount);

		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			Account account = AccountDAO.getAccountByUserId(conn, userId, WebSiteType.getInstance(webSiteType));
			if (null == account) {
				LogUtils.paymentGateway.error("createDeposit can't find account - websiteType:{} userId:{}",
					webSiteType, userId);
				return false;
			}

			moneyTransaction.setBonus(BigDecimal.ZERO);
			moneyTransaction.setVipLevel(account.getVipLevel());

		} catch (Deviation e) {
			throw e;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		int result;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			result = MoneyTransactionDAO.addDeposit(conn, moneyTransaction);

			conn.commit();

			LogUtils.moneyTransaction.info(JSONUtils.getJSONString(
				"Action", "create deposit",
				"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
				"Creator", moneyTransaction.getCreator(),
				"Player", moneyTransaction.getUserId(),
				"TxnInfo", JSONUtils.toJsonString(moneyTransaction)));

			MoneyTransactionCache.getInstance().update();
		} catch (Deviation e) {
			DbUtils.rollback(conn);
			throw e;
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}
		return result > 0;
	}

	//從dao呼叫getList, 然後包裝成MoneyTransactionRecord格式
	public static List<MoneyTransactionRecordResult> getMoneyTransactionRecord(String userId, WebSiteType webSiteType,
		Timestamp startDate, Timestamp endDate) throws SQLException {
		List<MoneyTransactionRecordResult> result = new ArrayList<>();
		Connection conn;
		try {
			conn = DBPool.getReadConnection();
			List<MoneyTransaction> moneyTransactionList;

			if (startDate != null && endDate != null)
				moneyTransactionList = MoneyTransactionDAO.getMoneyTransactionByUserAndTime(conn, userId, webSiteType,
					startDate, endDate);
			else
				moneyTransactionList = MoneyTransactionDAO.getMoneyTransactionByUser(conn, userId, webSiteType);

			//轉成所需格式
			for (MoneyTransaction moneyTransaction : moneyTransactionList) {
				MoneyTransactionRecordResult record = new MoneyTransactionRecordResult(
					MoneyTransactionUtils.formatId(moneyTransaction.getId(), moneyTransaction.getTransactionType()),
					MoneyTransactionType.getInstance(moneyTransaction.getTransactionType()).getName(),
					moneyTransaction.getCreateTime() != null ? moneyTransaction.getCreateTime().getTime() : null,
					moneyTransaction.getAmount(),
					moneyTransaction.getStatus()
				);
				result.add(record);
			}

			return result;

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}
	}

	public static String getMoneyTransactionRecordPaged(Account account, WebSiteType webSiteType,
		int pageNumber, int pageSize, int[] types, Timestamp startDate, Timestamp endDate)
		throws Exception {

		var userId = account.getUserId();
		BigDecimal mainWallet = SeamlessWalletApiService.getInstance().getBalance(account.getUserKey());
		var list = getMoneyTransactionRecord(userId, webSiteType, pageNumber, pageSize, types, startDate, endDate);

		try {

			var moneyTransactionIds = list.stream()
				.mapToLong(mt -> MoneyTransactionUtils.deformatFrontendId(mt.transactionId()))
				.toArray();

			var summary = MoneyTransactionCache.getInstance().getMoneyTransactionRecordSummary(account.getUserKey(),
				moneyTransactionIds, types, startDate, endDate);

			/// total pending amount calculate from DB
			BigDecimal totalPendingDeposit = DbExecutor.query(conn ->
				MoneyTransactionDAO.getGrandTotalOfMoneyTransaction(
					conn, userId, MoneyTransactionStatusType.NEW.unique(), BigDecimal.ZERO, BigDecimal.ZERO,
					webSiteType.unique(), startDate, endDate,
					List.of(MoneyTransactionType.DEPOSIT, MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY)
				)
			);

			BigDecimal totalPendingWithdrawal = DbExecutor.query(conn ->
				MoneyTransactionDAO.getGrandTotalOfMoneyTransaction(
					conn, userId, MoneyTransactionStatusType.NEW.unique(), BigDecimal.ZERO, BigDecimal.ZERO,
					webSiteType.unique(), startDate, endDate, List.of(MoneyTransactionType.WITHDRAWALS)
				)
			);

			if (totalPendingDeposit == null) {
				totalPendingDeposit = BigDecimal.ZERO;
			}
			if (totalPendingWithdrawal == null) {
				totalPendingWithdrawal = BigDecimal.ZERO;
			}

			PageInfo pageInfo = new PageInfo();
			pageInfo.setPageSize(pageSize);
			pageInfo.setPageNumber(pageNumber);
			pageInfo.setTotalCount(summary.get("totalRecords").intValue());

			return pageInfo.getPageInfoJson(
				"mainWalletAmount", String.valueOf(mainWallet),
				"pendingDepositAmount", String.valueOf(totalPendingDeposit),
				"pendingWithdrawalAmount", String.valueOf(totalPendingWithdrawal),
				"records", JSONUtils.toJsonString(list));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}
	}

	//從dao呼叫getList, 然後包裝成MoneyTransactionRecord格式
	public static List<MoneyTransactionRecordResult> getMoneyTransactionRecord(String userId, WebSiteType webSiteType,
		int pageNumber, int pageSize, int[] types, Timestamp startDate, Timestamp endDate) throws Exception {

		List<MoneyTransactionRecordResult> result = new ArrayList<>();

		List<MoneyTransaction> moneyTransactionList = DbExecutor.query(conn ->
			MoneyTransactionDAO.getPagedMoneyTransactionRecord(conn, userId, webSiteType.unique(),
				pageNumber, pageSize, types, startDate, endDate));

		//轉成所需格式
		for (MoneyTransaction moneyTransaction : moneyTransactionList) {
			MoneyTransactionRecordResult record = new MoneyTransactionRecordResult(
				MoneyTransactionUtils.formatId(moneyTransaction.getId(), moneyTransaction.getTransactionType()),
				MoneyTransactionType.getInstance(moneyTransaction.getTransactionType()).getName(),
				moneyTransaction.getCreateTime() != null ? moneyTransaction.getCreateTime().getTime() : null,
				moneyTransaction.getAmount(),
				moneyTransaction.getStatus()
			);
			result.add(record);
		}

		return result;
	}

	private static PageResult<MoneyTransaction> searchDeposit(
		String userId, Long transactionId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		String createdBy, Timestamp createdSince, String updatedBy, Timestamp updatedSince, int websiteType,
		String sortField, DBOrderType dbOrderType,
		long pageNumber, long showCount) {

		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			return MoneyTransactionDAO.findMoneyTransactionByMultiCondition(
				conn,
				userId,
				transactionId,
				status,
				minAmount,
				maxAmount,
				createdBy,
				createdSince,
				updatedBy,
				updatedSince,
				websiteType,
				sortField,
				dbOrderType,
				MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY,
				pageNumber,
				showCount

			);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		} finally {
			DbUtils.close(conn);
		}
	}

	//從dao呼叫getList, 然後包裝成GameTransactionHistory格式
	public static MoneyTransactionDetailResult getMoneyTransactionDetail(Long moneyTransactionId) throws Exception {
		/// 以ID取得交易
		MoneyTransaction moneyTransaction = MoneyTransactionCache.getInstance()
			.getMoneyTransaction(moneyTransactionId);
		/// 轉為所需格式

		MoneyTransactionType transactionType = MoneyTransactionType.getInstance(moneyTransaction.getTransactionType());
		PaymentType paymentType = null;
		if (moneyTransaction.getToPaymentType() != null) {
			paymentType = PaymentType.getInstanceOf(moneyTransaction.getToPaymentType());
		}

		var bankName = "";
		var cardNumber = "";

		if (transactionType == MoneyTransactionType.DEPOSIT
			|| transactionType == MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY) {

			bankName = moneyTransaction.getFromBankName();
			cardNumber = moneyTransaction.getFromBankNumber();

		} else {
			bankName = moneyTransaction.getToBankName();
			cardNumber = moneyTransaction.getToBankNumber();
		}

		return new MoneyTransactionDetailResult(
			MoneyTransactionUtils.formatId(moneyTransaction.getId(), transactionType.unique()),
			moneyTransaction.getCreateTime() != null ? moneyTransaction.getCreateTime().getTime() : 0L,
			moneyTransaction.getApprovedTime() != null ? moneyTransaction.getApprovedTime().getTime() : 0L,
			transactionType.getName(),
			paymentType != null ? paymentType.getFullName(null) : "",
			bankName,
			cardNumber,
			moneyTransaction.getAmount().doubleValue(),
			moneyTransaction.getStatus()
		);
	}

	public static String withdrawByAccountCard(Account playerInCache, BigDecimal withdrawAmount, int accountCardId)
		throws Exception {

		AccountCard accountCard = AccountCardBO.findById(accountCardId);
		if (accountCard == null) {
			throw new Deviation("Please add a card before making a withdraw.");
		}
		if (!accountCard.getUserId().equalsIgnoreCase(playerInCache.getUserId())) {
			throw new Deviation().setI18N("msg.error.account.paymentMethod.userId.incorrect");
		}

		return withdraw(playerInCache, withdrawAmount, (int) accountCard.getId(), accountCard.getBankName(),
			accountCard.getBankName(), accountCard.getCardholderName(), accountCard.getCardNo());
	}

	public static String withdrawByAccountBank(Account playerInCache, BigDecimal withdrawAmount, int accountBankId)
		throws Exception {

		AccountBank accountBank = AccountBankBO.getAccountBankById(accountBankId);
		if (accountBank == null) {
			throw new Deviation("Please add a bank before making a withdraw.");
		}

		return withdraw(playerInCache, withdrawAmount, accountBank.getId(), accountBank.getBankName(),
			accountBank.getBankBranch(), accountBank.getBankAccName(), accountBank.getBankAccNumber());
	}

	public static String withdraw(Account playerInCache, BigDecimal withdrawAmount,
		int toBankId, String toBankName, String toBankBranch, String toBankAccountName, String toBankAccountNo)
		throws Exception {

		try {

			withdrawAmount = withdrawAmount.setScale(4, RoundingMode.DOWN);

			if (Validator.verifyLength(withdrawAmount, 20)) {
				throw new Deviation("msg.error.validation.moneyAmountInvalid");
			}

			if (withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new Deviation("msg.error.validation.moneyAmountInvalid");
			}

			MoneyTransaction moneyTransaction = new MoneyTransaction();
			setMoneyTransactionObj(moneyTransaction,
				playerInCache.getUserId(), WebSiteType.getInstance(playerInCache.getWebsiteType()),
				MoneyTransactionType.WITHDRAWALS, PaymentType.CREDIT_CARD, MoneyTransactionStatusType.NEW,
				toBankId, toBankName, toBankBranch, toBankAccountName, toBankAccountNo, withdrawAmount, withdrawAmount,
				playerInCache.getVipLevel(), playerInCache.getCurrencyTypeName(), playerInCache.getUserId());

			if (MoneyTransactionBO.createWithdrawal(moneyTransaction, playerInCache)) {

				JsonGenerator jGenerator = null;
				StringWriter writer = new StringWriter();
				try {
					jGenerator = JSONUtils.getFactory().createGenerator(writer);
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("withdrawAmount", withdrawAmount);
					jGenerator.writeNumberField("realAmount", moneyTransaction.getRealAmount());
					jGenerator.writeStringField("toBankName", toBankName);
					jGenerator.writeStringField("toBankBranch", toBankBranch);
					jGenerator.writeStringField("toBankAccountName", toBankAccountName);
					jGenerator.writeStringField("toBankAccountNo", toBankAccountNo);
					jGenerator.writeStringField("transactionId",
						MoneyTransactionUtils.formatId(moneyTransaction.getId(),
							moneyTransaction.getTransactionType()));
					jGenerator.writeNumberField("transactionTypeId", moneyTransaction.getTransferType());
					jGenerator.writeEndObject();
				} finally {
					JSONUtils.close(jGenerator);
				}

				return writer.toString();
			}
			throw new Deviation("msg.withdrawal.fail");
		} catch (Exception e) {
			if (e instanceof Deviation) {
				throw e;
			}
			throw new Deviation("global.text.pleaseContactCustomerService");
		}
	}

	/**
	 * amount只接受正數
	 * 扣完錢為負，擋
	 * account為suspend擋
	 *
	 */
	public static boolean createWithdrawal(MoneyTransaction moneyTransaction, Account playerInCache)
		throws Exception {

		int result = DbExecutor.update(conn -> {

			long moneyTransactionId = MoneyTransactionDAO.getMoneyTransactionIdSeq(conn);
			moneyTransaction.setId(moneyTransactionId);

			int addResult = MoneyTransactionDAO.addWithdrawal(conn, moneyTransaction);

			if (addResult > 0) {

				String userKey = AccountUtils.getUserKey(playerInCache.getWebsiteType(), moneyTransaction.getUserId());

				SeamlessWalletApiService service = SeamlessWalletApiService.getInstance();

				BigDecimal currentBalance = service.getBalance(playerInCache.getUserKey());

				if (currentBalance.compareTo(moneyTransaction.getAmount()) < 0) {
					LogUtils.moneyTransaction.info(
						"withdraw fail, balance not enough, userKey: {}, balance: {}, withdrawAmount: {}",
						userKey, currentBalance, moneyTransaction.getAmount());
					throw new Deviation(
						"Withdrawal failed, Balance: "
							+ CurrencyType.EUR.getCurrencySymbol() +
							FormatUtils.numberFormat(currentBalance) + ", Withdrawal Amount: "
							+ CurrencyType.EUR.getCurrencySymbol() +
							FormatUtils.numberFormat(moneyTransaction.getAmount()));
				}

				String withdrawResult = service.withdrawFromWallet(userKey, "wd-" + System.currentTimeMillis(),
					moneyTransaction.getAmount().doubleValue());

				JsonNode jsonNodeDeposit = JSONUtils.toJsonNode(withdrawResult);

				if (200 != jsonNodeDeposit.get("status").asInt()) {
					LogUtils.moneyTransaction.info("withdraw fail, result: {}", withdrawResult);
					throw new Deviation("withdraw failed!");
				}
			}

			LogUtils.moneyTransaction.info(JSONUtils.getJSONString(
				"Action", "create withdrawal",
				"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
				"Creator", moneyTransaction.getCreator(),
				"Player", moneyTransaction.getUserId(),
				"TxnInfo", JSONUtils.toJsonString(moneyTransaction)));

			return addResult;
		});

		MoneyTransactionCache.getInstance().update();

		return result > 0;
	}

	public static String getDepositReport(
		String userId, Long transactionId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		String createdBy, Timestamp createdSince, String updatedBy, Timestamp updatedSince, int websiteType,
		String sortField, DBOrderType dbOrderType, int currencyType,
		long pageNumber, long showCount
	) {
		PageResult<MoneyTransaction> pageResult = searchDeposit(
			userId,
			transactionId,
			status,
			minAmount,
			maxAmount,
			createdBy,
			createdSince,
			updatedBy,
			updatedSince,
			websiteType,
			sortField,
			dbOrderType,
			pageNumber,
			showCount
		);

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pageResult.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pageResult.getTotalCount());
			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

			for (MoneyTransaction mt : pageResult.getResultList()) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("transactionId",
					MoneyTransactionUtils.formatId(mt.getId(), MoneyTransactionType.DEPOSIT));
				jGenerator.writeStringField("email", mt.getUserId());
				jGenerator.writeNumberField("amount", mt.getAmount());
				jGenerator.writeNumberField("status", mt.getStatus());
				jGenerator.writeStringField("createdBy", mt.getCreator());
				jGenerator.writeNumberField("createdTime", mt.getCreateTime().getTime());
				if (mt.getApprovedTime() != null)
					jGenerator.writeNumberField("updatedTime", mt.getApprovedTime().getTime());
				else
					jGenerator.writeNullField("updatedTime");
				jGenerator.writeStringField("updatedBy", mt.getApprovedUserid());
				jGenerator.writeNumberField("currencyTypeId", currencyType);
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();

		};
		return JSONUtils.getJSONString(processor);

	}

	public static String getPaymentMoneyTransactionDetail(long id) {
		Connection conn = null;
		MoneyTransaction moneyTransaction;

		try {
			conn = DBPool.getReadConnection();

			moneyTransaction = MoneyTransactionDAO.getMoneyTransactionById(conn, id);
			if (null == moneyTransaction) {
				throw new Deviation("moneyTransaction id not found");
			}

		} catch (Deviation e) {
			throw e;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return JSONUtils.EMPTY_JSON_ARRAY_STRING;
		} finally {
			DbUtils.close(conn);
		}

		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			int status = moneyTransaction.getStatus();
			int transaction_type = moneyTransaction.getTransactionType();
			MoneyTransactionType transactionType = MoneyTransactionType.getInstance(transaction_type);

			jGenerator.writeStringField("transactionId",
				MoneyTransactionUtils.formatId(moneyTransaction.getId(), transactionType));
			jGenerator.writeStringField("email", moneyTransaction.getUserId());
			jGenerator.writeNumberField("createTime", moneyTransaction.getCreateTime().getTime());
			jGenerator.writeNumberField(
				"amount", moneyTransaction.getAmount());
			jGenerator.writeStringField("fromBankName", moneyTransaction.getFromBankName());
			jGenerator.writeStringField("toBankName", moneyTransaction.getToBankName());
			jGenerator.writeStringField("fromBankAccount", moneyTransaction.getFromBankAccount());

			if (moneyTransaction.getApprovedTime() != null) {
				jGenerator.writeNumberField("updateTime", moneyTransaction.getApprovedTime().getTime());
			}
			String toBankAccountDisplayName = moneyTransaction.getToBankAccount();
			String toBankBranch = moneyTransaction.getToBankBranch();
			if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == transaction_type) {
				try {
					int pgAccountId = Integer.parseInt(toBankAccountDisplayName);
					PGAccount pgAccount = PaymentGatewayCache.getInstance().getPGAccount(pgAccountId);

					if (pgAccount != null) {
						toBankAccountDisplayName = pgAccount.getDisplayName();
					}
				} catch (Exception e) {
					LogUtils.SYS.error("ID:{}-{}", id, e.getMessage(), e);
				}

				jGenerator.writeStringField("toBankAccount", toBankAccountDisplayName);
				jGenerator.writeStringField(
					"toBankBranch",
					PaymentGatewayUtils.getPaymentAccountByToBankBranch(toBankBranch).orElse(null));
			} else {
				jGenerator.writeStringField("toBankAccount", toBankAccountDisplayName);
				jGenerator.writeStringField("toBankBranch", toBankBranch);

			}
			jGenerator.writeStringField("fromBankName", moneyTransaction.getFromBankName());
			jGenerator.writeStringField("fromBankNumber", moneyTransaction.getFromBankNumber());

			jGenerator.writeStringField("fromBankBranch", moneyTransaction.getFromBankBranch());
			jGenerator.writeStringField("fromFinanceCode", moneyTransaction.getFromFinanceCode());
			jGenerator.writeStringField("toFinanceCode", moneyTransaction.getToFinanceCode());

			jGenerator.writeStringField("toBankNumber", moneyTransaction.getToBankNumber());

			if (moneyTransaction.getToPaymentType() != null) {
				PaymentType paymentType = PaymentType.getInstanceOf(moneyTransaction.getToPaymentType());

				if ((moneyTransaction.getTransactionType() == MoneyTransactionType.WITHDRAWALS.unique()
					|| moneyTransaction.getTransactionType()
					== MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique())
					&& (moneyTransaction.getToBankId() == null || moneyTransaction.getToBankId() <= 0)) {
					jGenerator.writeNullField("paymentType");
				} else
					jGenerator.writeNumberField("paymentType", paymentType.unique());
			}

			jGenerator.writeStringField("creator", moneyTransaction.getCreator());

			jGenerator.writeNumberField("status", status);
			jGenerator.writeEndObject();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return JSONUtils.EMPTY_JSON_ARRAY_STRING;
		} finally {
			JSONUtils.close(jGenerator);
		}

		return out.toString();
	}

	public static String getBatchMoneyTransactionDetails(List<Long> ids, String sortField, DBOrderType dbOrderType)
		throws Exception {
		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(ids, sortField, dbOrderType);

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeArrayFieldStart("aaData");
			for (MoneyTransaction mt : moneyTransactionList) {

				MoneyTransactionType transactionType = MoneyTransactionType.getInstance(mt.getTransactionType());

				jGenerator.writeStartObject();

				if (MoneyTransactionType.DEPOSIT == transactionType
					|| MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY == transactionType) {
					jGenerator.writeStringField("fromBankName", mt.getFromBankName());
					jGenerator.writeStringField("fromBankNumber", mt.getFromBankNumber());

				} else if (MoneyTransactionType.WITHDRAWALS == transactionType
					|| MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY == transactionType) {

					jGenerator.writeStringField("toBankName", mt.getToBankName());
					jGenerator.writeStringField("toBankNumber", mt.getToBankNumber());
				}

				if (mt.getToPaymentType() != null) {
					PaymentType paymentType = PaymentType.getInstanceOf(mt.getToPaymentType());
					jGenerator.writeNumberField("paymentMethod", paymentType.unique());
				}

				jGenerator.writeStringField("transactionId",
					MoneyTransactionUtils.formatId(mt.getId(), transactionType));
				jGenerator.writeStringField("email", mt.getUserId());
				jGenerator.writeNumberField("amount", mt.getAmount());
				jGenerator.writeNumberField("status", mt.getStatus());
				jGenerator.writeStringField("createdBy", mt.getCreator());
				jGenerator.writeNumberField("createdTime", mt.getCreateTime().getTime());
				if (mt.getApprovedTime() != null) {
					jGenerator.writeNumberField("updatedTime", mt.getApprovedTime().getTime());
				} else {
					jGenerator.writeNullField("updatedTime");
				}

				jGenerator.writeStringField("updatedBy", mt.getApprovedUserid());
				if (mt.getCurrency() != null) {
					jGenerator.writeNumberField("currencyTypeId", CurrencyType.getInstance(mt.getCurrency()).unique());
				} else {
					jGenerator.writeNumberField("currencyTypeId", CurrencyType.EUR.unique());
				}

				jGenerator.writeStringField("bank", mt.getFromBankName());
				jGenerator.writeStringField("cardNumber", mt.getFromBankNumber());

				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();

		};
		return JSONUtils.getJSONString(processor);

	}

	public static List<MoneyTransaction> getMoneyTransactionList(List<Long> ids, String sortField,
		DBOrderType dbOrderType) throws Exception {
		return DbExecutor.query(conn ->
			MoneyTransactionDAO.getBatchMoneyTransactionsByIds(conn, ids, sortField, dbOrderType)
		);

	}

	private static PageResult<MoneyTransaction> getMoneyTransactionRecordByUserId(
		String userId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		int websiteType, Timestamp createdStart, Timestamp createdEnd, MoneyTransactionType moneyTransactionType,
		String sortField, DBOrderType dbOrderType,
		long pageNumber, long showCount) {

		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			return MoneyTransactionDAO.findMoneyTransactionRecord(
				conn,
				userId,
				status,
				minAmount,
				maxAmount,
				websiteType,
				createdStart,
				createdEnd,
				moneyTransactionType,
				sortField,
				dbOrderType,
				pageNumber,
				showCount
			);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		} finally {
			DbUtils.close(conn);
		}
	}

	private static BigDecimal getGrandTotalOfMoneyTransaction(
		String userId, BigDecimal minAmount, BigDecimal maxAmount,
		int websiteType, Timestamp createdStart, Timestamp createdEnd, MoneyTransactionType moneyTransactionType) {

		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			return MoneyTransactionDAO.getGrandTotalOfMoneyTransaction(
				conn,
				userId,
				MoneyTransactionStatusType.CONFIRMED.unique(),
				minAmount,
				maxAmount,
				websiteType,
				createdStart,
				createdEnd,
				List.of(moneyTransactionType)

			);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		} finally {
			DbUtils.close(conn);
		}
	}

	public static String getUserMoneyTransactionReport(
		String userId, int status, BigDecimal minAmount, BigDecimal maxAmount, int websiteType,
		String sortField, DBOrderType dbOrderType, Timestamp createdStart, Timestamp createdEnd,
		MoneyTransactionType moneyTransactionType,
		long pageNumber, long showCount
	) {
		PageResult<MoneyTransaction> moneyTransactionPageResult = getMoneyTransactionRecordByUserId(
			userId, status, minAmount, maxAmount, websiteType, createdStart,
			createdEnd, moneyTransactionType,
			sortField, dbOrderType, pageNumber, showCount
		);

		boolean showAll = (status == -999);

		BigDecimal grandTotal = (status == MoneyTransactionStatusType.CONFIRMED.unique() || showAll) ?
			getGrandTotalOfMoneyTransaction(userId, minAmount, maxAmount, websiteType, createdStart,
				createdEnd, moneyTransactionType) :
			BigDecimal.ZERO;

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			BigDecimal currentPageTotalAmount = BigDecimal.ZERO;
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, moneyTransactionPageResult.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, moneyTransactionPageResult.getTotalCount());

			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

			for (MoneyTransaction mt : moneyTransactionPageResult.getResultList()) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("transactionId",
					MoneyTransactionUtils.formatId(mt.getId(), moneyTransactionType));
				jGenerator.writeNumberField("amount", mt.getAmount());
				jGenerator.writeNumberField("status", mt.getStatus());
				jGenerator.writeStringField("createdBy", mt.getCreator());
				jGenerator.writeNumberField("createdTime", mt.getCreateTime().getTime());
				if (mt.getApprovedTime() != null)
					jGenerator.writeNumberField("updatedTime", mt.getApprovedTime().getTime());
				else
					jGenerator.writeNullField("updatedTime");
				jGenerator.writeStringField("updatedBy", mt.getApprovedUserid());

				if (mt.getStatus() == MoneyTransactionStatusType.CONFIRMED.unique()) {
					currentPageTotalAmount = currentPageTotalAmount.add(mt.getAmount());
				}
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
			jGenerator.writeObjectFieldStart(PageUtils.TOTAL_AMOUNT);
			jGenerator.writeNumberField("grandTotal", grandTotal);
			jGenerator.writeNumberField("totalAmount", currentPageTotalAmount);
			jGenerator.writeEndObject();

		};
		return JSONUtils.getJSONString(processor);
	}

	public static int batchApproveDeposit(
		List<Long> transactionIdList,
		String managerUserId) throws Exception {

		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(transactionIdList, null, null);
		int successCount = 0;
		for (MoneyTransaction moneyTransaction : moneyTransactionList) {

			if (moneyTransaction.getStatus() != MoneyTransactionStatusType.NEW.unique()) {
				continue;
			}

			moneyTransaction.setApprovedUserid(managerUserId);
			moneyTransaction.setApprovedTime(DateTimeBuilder.localDateTime().toTimestamp());

			try {
				approvePaymentDepositByBoCheckOrder(moneyTransaction);
				successCount++;
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}

			WebSocketBO.sendDepositLimitUsageUpdate(AccountCache.getInstance()
				.getAccount(moneyTransaction.getWebsiteType(), moneyTransaction.getUserId()));
		}

		if (successCount == 0) {
			return 0;
		}

		if (transactionIdList.size() == successCount) {
			return 1;
		}

		return 2;
	}

	public static String searchAdjustment(
		Long transactionIdCondition,
		String userId,
		Date createDateStart,
		String creator,
		String sortCondition,
		DBOrderType orderType,
		PageInfo pageInfo,
		Set<CurrencyType> managerCurrencySet,
		int transactionType) {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();

		Connection conn = null;

		try {

			conn = DBPool.getReadConnection();

			return MoneyTransactionDAO.findAdjustmentByMultiCondition(
				conn,
				transactionIdCondition,
				userId,
				createDateStart,
				creator,
				-1,
				sortCondition,
				orderType,
				webSiteType,
				pageInfo,
				managerCurrencySet,
				transactionType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return "";
		} finally {
			DbUtils.close(conn);
		}
	}

	/**
	 * amount only accept positive or negative, 0 is rejected
	 *
	 */
	public static boolean createAdjustment(Account account, MoneyTransaction moneyTransaction)
		throws Exception {

		moneyTransaction.setTransactionType(MoneyTransactionType.ADJUSTMENT.unique());
		moneyTransaction.setStatus(MoneyTransactionStatusType.CONFIRMED.unique());

		Timestamp now = new Timestamp(System.currentTimeMillis());

		Connection conn = null;

		try {

			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			int count = MoneyTransactionDAO.addAdjustment(conn, moneyTransaction, now);

			if (count <= 0) {
				throw new Deviation("Insert to adjustment error");
			}

			LogUtils.moneyTransaction.info(
				JSONUtils.getJSONString(
					"Action", "create adjustment",
					"Time", String.valueOf(new Timestamp(System.currentTimeMillis())),
					"Creator", moneyTransaction.getCreator(),
					"Player", moneyTransaction.getUserId(),
					"TxnInfo", JSONUtils.toJsonString(moneyTransaction)));

			BigDecimal oldBalance = getBalanceFromWallet(moneyTransaction.getUserId());

			BigDecimal newBalance = adjustmentAndGetNewBalance(moneyTransaction.getUserId(),
				moneyTransaction.getAmount());

			boolean isFirstAdjustment = account.getFirstAdjustmentTime() == null;
			AccountDAO.updateForAdjustment(conn, account.getUserId(), WebSiteType.getInstance(account.getWebsiteType()),
				isFirstAdjustment, now);

			MoneyTransactionDAO.refreshUpdateTime(conn, moneyTransaction.getId());

			LogUtils.SYS.info("[Adjustment] websiteType: {} player: {} balance from {} to {}",
				moneyTransaction.getWebsiteType(), moneyTransaction.getUserId(), oldBalance, newBalance);

			conn.commit();

			MoneyTransactionCache.getInstance().update();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		return true;
	}

	public static PageResult<MoneyTransaction> searchWithdrawal(String userId, Long transactionId, int status,
		BigDecimal minAmount, BigDecimal maxAmount,
		String createdBy, Timestamp createdSince, String updatedBy, Timestamp updatedSince, int websiteType,
		String sortField, DBOrderType dbOrderType,
		long pageNumber, long showCount) {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			return MoneyTransactionDAO.findMoneyTransactionByMultiCondition(
				conn,
				userId,
				transactionId,
				status,
				minAmount,
				maxAmount,
				createdBy,
				createdSince,
				updatedBy,
				updatedSince,
				websiteType,
				sortField,
				dbOrderType,
				MoneyTransactionType.WITHDRAWALS,
				pageNumber,
				showCount

			);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		} finally {
			DbUtils.close(conn);
		}

	}

	public static String getWithdrawalReport(
		String userId, Long transactionId, int status, BigDecimal minAmount, BigDecimal maxAmount,
		String createdBy, Timestamp createdSince, String updatedBy, Timestamp updatedSince, int websiteType,
		String sortField, DBOrderType dbOrderType, int currencyType,
		long pageNumber, long showCount
	) {
		PageResult<MoneyTransaction> pageResult = searchWithdrawal(
			userId,
			transactionId,
			status,
			minAmount,
			maxAmount,
			createdBy,
			createdSince,
			updatedBy,
			updatedSince,
			websiteType,
			sortField,
			dbOrderType,
			pageNumber,
			showCount
		);

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pageResult.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pageResult.getTotalCount());
			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

			for (MoneyTransaction mt : pageResult.getResultList()) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("transactionId",
					MoneyTransactionUtils.formatId(mt.getId(), MoneyTransactionType.WITHDRAWALS));
				jGenerator.writeStringField("email", mt.getUserId());
				jGenerator.writeNumberField("amount", mt.getAmount());
				jGenerator.writeNumberField("status", mt.getStatus());
				jGenerator.writeStringField("createdBy", mt.getCreator());
				jGenerator.writeNumberField("createdTime", mt.getCreateTime().getTime());
				if (mt.getApprovedTime() != null)
					jGenerator.writeNumberField("updatedTime", mt.getApprovedTime().getTime());
				else
					jGenerator.writeNullField("updatedTime");
				jGenerator.writeStringField("updatedBy", mt.getApprovedUserid());
				jGenerator.writeNumberField("currencyTypeId", currencyType);
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();

		};
		return JSONUtils.getJSONString(processor);

	}

	public static int disapproveWithdrawal(List<Long> ids, String managerUserId) throws Exception {
		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(ids, null, null);
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			List<Long> successfulRefunds = new ArrayList<>();

			for (MoneyTransaction moneyTransaction : moneyTransactionList) {
				if (moneyTransaction.getStatus() != MoneyTransactionStatusType.NEW.unique()) {
					continue;
				}
				try {
					String userKey = AccountUtils.getUserKey(moneyTransaction.getWebsiteType(),
						moneyTransaction.getUserId());
					BigDecimal newBalance = SeamlessWalletApiService.getInstance()
						.refundAndGetNewBalance(userKey, moneyTransaction.getAmount());

					successfulRefunds.add(moneyTransaction.getId());
					LogUtils.SYS.info("Refunded {} to user {}, new balance: {}",
						moneyTransaction.getAmount(), userKey, newBalance);
				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
			}

			if (!successfulRefunds.isEmpty()) {
				int dbResult = MoneyTransactionDAO.approveRejectWithdrawalBO(conn, successfulRefunds,
					MoneyTransactionStatusType.CLOSE.unique(),
					managerUserId);
				if (dbResult > 0) {
					conn.commit();
					return 1;
				}
			}
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		return 0;
	}

	public static String getAdjustmentInfoByStatusForCreate(String userId)
		throws Exception {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = JSONUtils.getFactory().createGenerator(out);

		try {
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("balance", getBalanceFromWallet(userId));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			jGenerator.writeNumberField("balance", 0.00);
		} finally {
			jGenerator.writeEndObject();
			JSONUtils.close(jGenerator);
		}

		return out.toString();
	}

	private static BigDecimal getBalanceFromWallet(String userId)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		String userKey = AccountUtils.getUserKey(webSiteType, userId);

		return SeamlessWalletApiService.getInstance().getBalance(userKey);
	}

	private static BigDecimal adjustmentAndGetNewBalance(String userId, BigDecimal amount)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		String userKey = AccountUtils.getUserKey(webSiteType, userId);

		return SeamlessWalletApiService.getInstance().adjustmentAndGetNewBalance(userKey, amount);
	}

	// job every 00:00 of the day, check the user if it is no transactions (no betting, no deposit, no withdrawal)  include for 30months
	public static void autoGenerateWithdrawalRequest(int websiteType) throws Exception {

		List<Account> accList = DbExecutor.query(conn -> {
			return AccountDAO.findUsersWithoutSuccessfulTransactions(conn, websiteType, true);
		});

		for (Account account : accList) {
			try {
				// if there is no balance, return
				if (null == account.getBalance() ||
					account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				AccountCard accountCard = AccountCardBO.findFirstActiveCardByUserId(account);
				MoneyTransaction moneyTransaction = new MoneyTransaction();
				moneyTransaction.setTransactionType(MoneyTransactionType.WITHDRAWALS.unique());
				moneyTransaction.setStatus(MoneyTransactionStatusType.NEW.unique());
				moneyTransaction.setWebsiteType(account.getWebsiteType());
				moneyTransaction.setUserId(account.getUserId());
				moneyTransaction.setVipLevel(account.getVipLevel());

				moneyTransaction.setAmount(account.getBalance());
				moneyTransaction.setFeeId(-1);
				moneyTransaction.setRealAmount(account.getBalance());
				moneyTransaction.setAmountFee(BigDecimal.ZERO);
				moneyTransaction.setExpireInterval(0);

				if (accountCard != null) {
					long cardId = accountCard.getId();
					if (cardId <= Integer.MAX_VALUE) {
						moneyTransaction.setToBankId((int) cardId);
					} else {
						moneyTransaction.setToBankId(-1);
					}
					moneyTransaction.setToBankName(accountCard.getBankName());
					moneyTransaction.setToBankBranch(accountCard.getBankName());
					moneyTransaction.setToBankAccount(accountCard.getCardholderName());
					moneyTransaction.setToBankNumber(accountCard.getCardNo());
					moneyTransaction.setToPaymentType(PaymentType.CREDIT_CARD.unique());
				}

				moneyTransaction.setBankExtraData("{}");
				moneyTransaction.setCurrency(CurrencyType.getInstance(account.getCurrencyTypeId()).getName());
				moneyTransaction.setCreator("SYS");

				createWithdrawal(moneyTransaction, account);

			} catch (Exception e) {
				LogUtils.SYS.error("Error processing user {}: {}", account.getUserId(), e.getMessage(), e);
			}
		}

	}

	public static void setMoneyTransactionObj(MoneyTransaction moneyTransaction,
		String userId, WebSiteType webSiteType,
		MoneyTransactionType transactionType, PaymentType paymentType, MoneyTransactionStatusType statusType,
		int toBankId, String toBankName, String toBankBranch, String toBankAccount, String toBankNumber,
		BigDecimal amount, BigDecimal realAmount, int vipLevel, String currencyTypeName, String creator) {

		moneyTransaction.setUserId(userId);
		moneyTransaction.setWebsiteType(webSiteType.unique());
		moneyTransaction.setTransactionType(transactionType.unique());
		moneyTransaction.setToPaymentType(paymentType.unique());
		moneyTransaction.setStatus(statusType.unique());
		moneyTransaction.setAmount(amount);
		moneyTransaction.setRealAmount(realAmount);
		moneyTransaction.setFeeId(-1);
		moneyTransaction.setAmountFee(BigDecimal.ZERO);
		moneyTransaction.setExpireInterval(0);
		moneyTransaction.setToBankId(toBankId);
		moneyTransaction.setToBankName(toBankName);
		moneyTransaction.setToBankBranch(toBankBranch);
		moneyTransaction.setToBankAccount(toBankAccount);
		moneyTransaction.setToBankNumber(toBankNumber);
		moneyTransaction.setBankExtraData("{}");
		moneyTransaction.setVipLevel(vipLevel);
		moneyTransaction.setCurrency(currencyTypeName);
		moneyTransaction.setCreator(creator);
	}
}
