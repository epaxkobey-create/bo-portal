package com.nv.commons.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dto.MoneyTransaction;

public class PendingMoneyTransactionCache {

	private static final PendingMoneyTransactionCache instance = new PendingMoneyTransactionCache();

	// TODO 這邊僅有寫入，但沒有查詢，檢查是否還需要
	private final Map<String, List<MoneyTransaction>> depositPendingCache = new ConcurrentHashMap<>();

	// TODO 這邊僅有寫入，但沒有查詢，檢查是否還需要
	private final Map<String, List<MoneyTransaction>> withdrawalPendingCache = new ConcurrentHashMap<>();

	public PendingMoneyTransactionCache() {
	}

	public static PendingMoneyTransactionCache getInstance() {
		return instance;
	}

	public void setDepositPendingList(String userKey, List<MoneyTransaction> pendingDeposit) {

		if (pendingDeposit == null) {
			return;
		}
		depositPendingCache.put(userKey, pendingDeposit);

	}

	public void setWithdrawalPendingList(String userKey, List<MoneyTransaction> pendingWithdrawal) {

		if (pendingWithdrawal == null) {
			return;
		}

		withdrawalPendingCache.put(userKey, pendingWithdrawal);
	}

}
