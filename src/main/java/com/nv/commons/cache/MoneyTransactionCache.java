package com.nv.commons.cache;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.dao.MoneyTransactionDAO;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * Cache for MoneyTransaction with DB table MoneyTransaction
 */
public class MoneyTransactionCache extends AbstractCache {

	// <moneyTransactionId, moneyTransaction>
	private final ConcurrentHashMap<Long, MoneyTransaction> cache = new ConcurrentHashMap<>();
	// <userKey, <moneyTransactionId, moneyTransaction>>
	private final ConcurrentHashMap<String, Map<Long, MoneyTransaction>> cacheByUserKey = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final MoneyTransactionCache instance = new MoneyTransactionCache();

	public MoneyTransactionCache() {
	}

	public static MoneyTransactionCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {

			List<MoneyTransaction> list = MoneyTransactionDAO.findAll(conn);

			for (MoneyTransaction moneyTransaction : list) {

				cache.put(moneyTransaction.getId(), moneyTransaction);

				String userKey = AccountUtils.getUserKey(moneyTransaction.getWebsiteType(),
					moneyTransaction.getUserId());

				cacheByUserKey.computeIfAbsent(userKey, o -> new ConcurrentHashMap<>())
					.put(moneyTransaction.getId(), moneyTransaction);
			}

			lastUpdateTime = System.currentTimeMillis();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {

		try (Connection conn = DBPool.getReadConnection()) {

			Timestamp queryTime = new Timestamp(lastUpdateTime);

			long maxUpdateTime = queryTime.getTime();

			boolean isUpdated = false;

			List<MoneyTransaction> list = MoneyTransactionDAO.findAllByUpdateTime(conn, queryTime);

			for (MoneyTransaction moneyTransactionInDB : list) {

				cache.put(moneyTransactionInDB.getId(), moneyTransactionInDB);

				String userKey = AccountUtils.getUserKey(moneyTransactionInDB.getWebsiteType(),
					moneyTransactionInDB.getUserId());

				cacheByUserKey.computeIfAbsent(userKey, o -> new ConcurrentHashMap<>())
					.put(moneyTransactionInDB.getId(), moneyTransactionInDB);

				isUpdated = true;
				maxUpdateTime = Math.max(maxUpdateTime, moneyTransactionInDB.getUpdateTime().getTime());
			}

			if (isUpdated) {
				lastUpdateTime = maxUpdateTime - ERROR_VALUE;
			} else {
				lastUpdateTime = maxUpdateTime;
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		update();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public List<MoneyTransaction> getMoneyTransactionList(int webSiteType, String userId) {
		return getMoneyTransactionList(AccountUtils.getUserKey(webSiteType, userId));
	}

	public List<MoneyTransaction> getMoneyTransactionList(String userKey) {
		Map<Long, MoneyTransaction> userTransactions = cacheByUserKey.get(userKey);
		if (userTransactions == null) {
			return List.of();
		}
		return List.copyOf(userTransactions.values());
	}

	public List<MoneyTransaction> getMoneyTransactionList(String userKey, int[] types,
		Timestamp startDate, Timestamp endDate) {
		Map<Long, MoneyTransaction> userTransactions = cacheByUserKey.get(userKey);
		if (userTransactions == null) {
			return List.of();
		}
		return userTransactions.values().stream()
			.filter(mt -> types == null || types.length == 0
				|| Arrays.stream(types).anyMatch(t -> t == mt.getTransactionType()))
			.filter(mt -> (startDate == null || !mt.getCreateTime().before(startDate))
				&& (endDate == null || !mt.getCreateTime().after(endDate)))
			.collect(Collectors.toList());
	}

	public MoneyTransaction getMoneyTransaction(Long moneyTransactionId) {
		return cache.get(moneyTransactionId);
	}

	public Map<String, BigDecimal> getMoneyTransactionRecordSummary(int webSiteType, String userId) {
		return getMoneyTransactionRecordSummary(AccountUtils.getUserKey(webSiteType, userId));
	}

	public Map<String, BigDecimal> getMoneyTransactionRecordSummary(String userKey) {

		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(userKey);

		long totalRecords = moneyTransactionList.size();

		BigDecimal pendingDepositAmount = calculatePendingTransactionAmount(moneyTransactionList,
			MoneyTransactionType.DEPOSIT_TYPE_UNIQUE);

		BigDecimal pendingWithdrawalAmount = calculatePendingTransactionAmount(moneyTransactionList,
			MoneyTransactionType.WITHDRAWAL_TYPE_UNIQUE);

		Map<String, BigDecimal> result = new HashMap<>();
		result.put("totalRecords", BigDecimal.valueOf(totalRecords));
		result.put("pendingDepositAmount", pendingDepositAmount);
		result.put("pendingWithdrawalAmount", pendingWithdrawalAmount);

		return result;
	}

	public Map<String, BigDecimal> getMoneyTransactionRecordSummary(String userKey, long[] moneyTransactionIds,
		int[] types, Timestamp startDate, Timestamp endDate) {

		List<MoneyTransaction> moneyTransactionList = getMoneyTransactionList(userKey, types, startDate, endDate);

		long totalRecords = moneyTransactionList.size();

		moneyTransactionList = moneyTransactionList.stream()
			.filter(mt ->
				Arrays.stream(moneyTransactionIds).anyMatch(id -> id == mt.getId()))
			.collect(Collectors.toList());

		BigDecimal pendingDepositAmount = calculatePendingTransactionAmount(moneyTransactionList,
			MoneyTransactionType.DEPOSIT_TYPE_UNIQUE);

		BigDecimal pendingWithdrawalAmount = calculatePendingTransactionAmount(moneyTransactionList,
			MoneyTransactionType.WITHDRAWAL_TYPE_UNIQUE);

		Map<String, BigDecimal> result = new HashMap<>();
		result.put("totalRecords", BigDecimal.valueOf(totalRecords));
		result.put("pendingDepositAmount", pendingDepositAmount);
		result.put("pendingWithdrawalAmount", pendingWithdrawalAmount);

		return result;
	}

	private BigDecimal calculatePendingTransactionAmount(List<MoneyTransaction> moneyTransactionList, int[] types) {
		return moneyTransactionList.stream()
			.filter(txn ->
				Arrays.stream(types).anyMatch(t -> t == txn.getTransactionType())
					&& Arrays.stream(MoneyTransactionStatusType.PENDING_UNIQUE).anyMatch(t -> t == txn.getStatus()))
			.map(MoneyTransaction::getAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
