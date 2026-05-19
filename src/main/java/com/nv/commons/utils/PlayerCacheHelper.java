package com.nv.commons.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.nv.commons.bo.AccountBO;
import com.nv.commons.cache.PendingMoneyTransactionCache;
import com.nv.commons.cache.PlayerAccountDocumentCache;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.dao.AccountContactInfoDAO;
import com.nv.commons.dao.AccountDocumentDAO;
import com.nv.commons.dao.MoneyTransactionDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.model.database.DBPool;
import com.nv.module.backendapi.cache.PlayerLocalCache;

public class PlayerCacheHelper {

	private final PlayerLocalCache cache;

	public PlayerCacheHelper(PlayerLocalCache cache) {
		this.cache = cache;
	}

	/**
	 *
	 */
	public static class UpdatedAttributeRecord {

		private final String[] userIdWebSiteId;

		private List<Integer> sumOfUpdatedAttributes = new ArrayList<>();

		public UpdatedAttributeRecord(String[] userIdWebSiteId) {
			this.userIdWebSiteId = userIdWebSiteId;
		}

		public String[] getUserIdWebSiteId() {
			return userIdWebSiteId;
		}

		public List<Integer> getSumOfUpdatedAttributes() {
			return sumOfUpdatedAttributes;
		}


		public void updateSumOfUpdatedAttributes(UpdatedAttributeType updatedAttributeType) {
			if (!sumOfUpdatedAttributes.contains(updatedAttributeType.unique())) {
				sumOfUpdatedAttributes.add(updatedAttributeType.unique());
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			UpdatedAttributeRecord that = (UpdatedAttributeRecord) o;
			return Arrays.equals(userIdWebSiteId, that.userIdWebSiteId);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(userIdWebSiteId);
		}
	}

	/**
	 *
	 */
	public void syncAccountRelatedTable(Map<String, UpdatedAttributeRecord> updatedAttributeRecordMap,
		//		List<String[]> groupUpdateList,
//		List<String[]> inboxUpdateList,
//		List<String[]> accountBankUpdateList,
		List<String[]> accountContactUpdateList,
		List<String[]> multipleTransactionUpdateList,
		//	List<String[]> accountBonusTurnoverUpdateList,
		List<String[]> accountDocumentUpdateList
	) {

		List<Runnable> tasks = new ArrayList<>();

		// 5. AccountContact
		if (!accountContactUpdateList.isEmpty()) {
			tasks.add(() -> syncAccountContactWithDB(accountContactUpdateList));
		}

		// 6. Multiple Transaction
		if (!multipleTransactionUpdateList.isEmpty()) {
			tasks.add(() -> syncMultipleTransactionWithDB(multipleTransactionUpdateList));
		}

		// 6. AccountBonus
		//		if (!accountBonusTurnoverUpdateList.isEmpty()) {
		//			tasks.add(() -> syncAccountBonusTurnoverWithDB(accountBonusTurnoverUpdateList));
		//		}

		// 7.AccountDocument
		if (!accountDocumentUpdateList.isEmpty()) {
			tasks.add(() -> syncAccountDocumentWithDB(accountDocumentUpdateList));
		}

		// 8. reset
		if (!updatedAttributeRecordMap.isEmpty()) {
			tasks.add(() -> AccountBO.resetUpdateAttribute(updatedAttributeRecordMap));
		}

		/*
		 * await
		 */
		GlobalThreadPool.await(tasks);
	}

	/*
	 *
	 */
	public Account put(String userKey, Account account) {

		if (userKey == null) {
			return null;
		}
		return this.cache.put(userKey, account);
	}

	private void syncAccountContactWithDB(List<String[]> accountContactUpdateList) {

		try (Connection conn = DBPool.getReadConnection()) {

			Map<String, List<AccountContactInfo>> accountContactMap = AccountContactInfoDAO
				.getAccountContactByUserKeys(conn,
					accountContactUpdateList.toArray(new String[accountContactUpdateList.size()][]),
					this.cache::get);

			for (Map.Entry<String, List<AccountContactInfo>> entry : accountContactMap.entrySet()) {

				final String userKey = entry.getKey();

				Account userInCache = this.cache.get(userKey);

				if (userInCache != null) {

					List<AccountContactInfo> accountContactList = entry.getValue();

					userInCache.setAccountContactInfoList(accountContactList);

					// MEMO: 如果 userInCache 有異動, 要 put back 到 redis cache
					this.put(userKey, userInCache);
				}
			}

		} catch (Exception e) {
			LogUtils.SYS.error("[failure update AccountContact in player cache]", e);
		}
	}

	private void syncMultipleTransactionWithDB(List<String[]> multipleTransactionUpdateList) {

		try (Connection conn = DBPool.getReadConnection()) {

			Map<String, Integer> depositPendingCountMap = MoneyTransactionDAO.getDepositPendingCountByUserKeys(conn,
				multipleTransactionUpdateList.toArray(new String[multipleTransactionUpdateList.size()][]),
				this.cache::get);

			Map<String, Integer> withdrawalPendingCountMap = MoneyTransactionDAO.getWithdrawalPendingCountByUserKeys(
				conn, multipleTransactionUpdateList.toArray(new String[multipleTransactionUpdateList.size()][]),
				this.cache::get);

			Map<String, List<MoneyTransaction>> transactionPendingMap = MoneyTransactionDAO
				.getTransactionPendingByUserKeys(conn,
					multipleTransactionUpdateList.toArray(new String[multipleTransactionUpdateList.size()][]),
					this.cache::get);

			/*
			 */

			for (Map.Entry<String, Integer> entry : depositPendingCountMap.entrySet()) {

				final String userKey = entry.getKey();
				PendingMoneyTransactionCache pendingTransactionCache = PendingMoneyTransactionCache.getInstance();

				Account userInCache = this.cache.get(userKey);

				if (userInCache != null) {

					userInCache.setDepositPendingCount(new AtomicInteger(entry.getValue()));

					if (entry.getValue() > 0) {

						List<MoneyTransaction> transactionList = transactionPendingMap.get(userKey);

						if (transactionList != null) {
							List<MoneyTransaction> pendingDeposit = transactionList.stream()
								.filter(
									t -> t.getTransactionType() == MoneyTransactionType.DEPOSIT.unique()
										|| t.getTransactionType()
										== MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique())
								.collect(
									Collectors.toList());

							pendingTransactionCache.setDepositPendingList(userKey, pendingDeposit);
						} else {
							pendingTransactionCache.setDepositPendingList(userKey, Collections.emptyList());
						}
					} else {

						pendingTransactionCache.setDepositPendingList(userKey, Collections.emptyList());
					}

					// MEMO: 如果 userInCache 有異動, 要 put back 到 redis cache
					this.put(userKey, userInCache);
				}
			}

			for (Map.Entry<String, Integer> entry : withdrawalPendingCountMap.entrySet()) {

				final String userKey = entry.getKey();
				PendingMoneyTransactionCache pendingTransactionCache = PendingMoneyTransactionCache.getInstance();

				Account userInCache = this.cache.get(userKey);

				if (userInCache != null) {

					userInCache.setWithdrawalPendingCount(new AtomicInteger(entry.getValue()));

					if (entry.getValue() > 0) {

						List<MoneyTransaction> transactionList = transactionPendingMap.get(userKey);

						if (transactionList != null) {
							List<MoneyTransaction> pendingWithdrawal = transactionList.stream()
								.filter(
									t -> t.getTransactionType() == MoneyTransactionType.WITHDRAWALS.unique()
										|| t.getTransactionType()
										== MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique())
								.collect(
									Collectors.toList());

							pendingTransactionCache.setWithdrawalPendingList(userKey, pendingWithdrawal);
						} else {
							pendingTransactionCache.setWithdrawalPendingList(userKey, Collections.emptyList());
						}

					} else {

						pendingTransactionCache.setWithdrawalPendingList(userKey, Collections.emptyList());
					}

					// MEMO: 如果 userInCache 有異動, 要 put back 到 redis cache
					this.put(userKey, userInCache);
				}

			}

		} catch (Exception e) {
			LogUtils.SYS.error("[failure update multiple Moneytransaction in player cache]", e);
		}
	}

	/*
	 *
	 */
	private void syncAccountDocumentWithDB(List<String[]> accountDocumentUpdateList) {

		try (Connection conn = DBPool.getReadConnection()) {

			Map<String, List<AccountDocument>> accountDocumentMap = AccountDocumentDAO
				.getAccountDocumentByUserKeys(conn,
					accountDocumentUpdateList.toArray(new String[accountDocumentUpdateList.size()][]),
					this.cache::get);

			for (Map.Entry<String, List<AccountDocument>> entry : accountDocumentMap.entrySet()) {

				final String userKey = entry.getKey();

				Account userInCache = this.cache.get(userKey);

				if (userInCache != null) {

					List<AccountDocument> accountDocumentList = entry.getValue();

					PlayerAccountDocumentCache.getInstance()
						.setAccountDocumentList(userKey, accountDocumentList);
				}
			}

			Map<String, EnumMap<DocumentType, List<AccountDocument>>> accountOtherDocumentMap = AccountDocumentDAO
				.getOtherGroupDocumentByUserKeys(conn,
					accountDocumentUpdateList.toArray(new String[accountDocumentUpdateList.size()][]));

			for (Map.Entry<String, EnumMap<DocumentType, List<AccountDocument>>> entry : accountOtherDocumentMap.entrySet()) {

				final String userKey = entry.getKey();

				Account userInCache = this.cache.get(userKey);

				if (userInCache != null) {

					EnumMap<DocumentType, List<AccountDocument>> otherDocumentList = entry.getValue();

					PlayerAccountDocumentCache.getInstance()
						.setOtherDocumentList(userKey, otherDocumentList);
				}
			}

		} catch (Exception e) {
			LogUtils.SYS.error("[failure update AccountDocument in player cache]", e);
		}
	}

}
