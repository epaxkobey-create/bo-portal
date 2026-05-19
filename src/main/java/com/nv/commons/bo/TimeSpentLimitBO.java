package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeSpentLimitType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dao.GameSessionUsageDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;

public class TimeSpentLimitBO {

	public static final AccountPlayResponsiblyType type = AccountPlayResponsiblyType.TIME_SPENT_LIMIT;

	public static void updateTimeSpentLimit(String userId,
		List<AccountPlayResponsiblySetting> playResponsiblyList, String updater,
		LinkedList<AccountUpdateLog> accountUpdateLogList) throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();
		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());

		AccountPlayResponsiblySetting newTimeSpentLimit = playResponsiblyList.getFirst();
		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newTimeSpentLimit.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldTimeSpentLimit = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		TimeSpentLimitType newTimeSpentLimitType = TimeSpentLimitType.getInstanceOf(newTimeSpentLimit.getNewValue());

		if (oldTimeSpentLimit == null) {
			processInsertTimeSpentLimit(
				userId,
				webSiteType,
				periodType,
				newTimeSpentLimitType,
				accountUpdateLogList
			);
		} else {
			String takeEffectMinutesSettingValue =
				WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(), currencyType.unique(),
					WebsiteSystemSettingType.TIME_SPENT_LIMIT_MODIFY_TAKE_EFFECT_MINUTES.unique()
				);
			long takeEffectMinutes = Long.parseLong(takeEffectMinutesSettingValue);
			oldTimeSpentLimit.setUpdater(updater);

			processUpdateTimeSpentLimit(
				userId,
				currencyType,
				newTimeSpentLimit,
				newTimeSpentLimitType,
				oldTimeSpentLimit,
				accountUpdateLogList,
				takeEffectMinutes,
				webSiteType.unique()
			);
		}
	}

	private static void processInsertTimeSpentLimit(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyPeriodType periodType,
		TimeSpentLimitType newTimeSpentLimitType,
		LinkedList<AccountUpdateLog> accountUpdateLogList) throws Exception {

		AccountPlayResponsiblySetting newTimeSpentLimit =
			AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId, webSiteType,
				type, periodType, String.valueOf(newTimeSpentLimitType.unique()), userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newTimeSpentLimit));

		String displayName = type.getAccountUpdateType(periodType).getDisplayName();
		UpdateRecord updateRecord = new UpdateRecord(
			"Current Limit: " + TimeSpentLimitType.getInstanceOf(type.getDefaultValue()).getName(),
			"Current Limit: " + newTimeSpentLimitType.getName(),
			"New " + displayName);

		accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType), updateRecord));

		String userKey = AccountUtils.getUserKey(webSiteType, userId);
		BigDecimal newLimitSnapShot = BigDecimal.valueOf(Integer.parseInt(newTimeSpentLimit.getNewValue()))
			.multiply(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER));
		try {
			DbExecutor.update(conn -> {
				GameSessionUsage existing = GameSessionUsageDAO.getGameSessionUsage(conn, userKey);
				if (existing == null) {
					Timestamp now = new Timestamp(System.currentTimeMillis());
					return GameSessionUsageDAO.insertNewGameSessionUsage(conn,userKey,newLimitSnapShot,
						BinaryStatusType.INACTIVE.unique(), now);
				}
				return GameSessionUsageDAO.resetUsageAndUpdateLimit(conn, userKey, newLimitSnapShot, existing.getVersion());
			});
		} catch (Exception e) {
			LogUtils.SYS.error("Error resetting usage of gameSessionUsage userId: {}", userId);
		}

	}

	private static void processUpdateTimeSpentLimit(
		String userId,
		CurrencyType currencyType,
		AccountPlayResponsiblySetting newTimeSpentLimit,
		TimeSpentLimitType newTimeSpentLimitType,
		AccountPlayResponsiblySetting oldTimeSpentLimit,
		LinkedList<AccountUpdateLog> accountUpdateLogList,
		long takeEffectMinutes, int websiteType
	) throws Exception {

		int newLimitsValue = Integer.parseInt(newTimeSpentLimit.getNewValue());
		int currentLimitsValue = Integer.parseInt(oldTimeSpentLimit.getCurrentValue());

		// same newValue
		if (newTimeSpentLimitType == TimeSpentLimitType.getInstanceOf(oldTimeSpentLimit.getNewValue())) {
			return;
		}
		boolean hasCancel = checkStatusAndGetCancelUpdateLog(userId, currencyType, type,
			AccountUpdateType.TIME_SPENT_LIMIT, oldTimeSpentLimit,
			accountUpdateLogList);

		updateAndGetUpdateLog(userId, currencyType, AccountUpdateType.TIME_SPENT_LIMIT, takeEffectMinutes,
			currentLimitsValue, newLimitsValue, oldTimeSpentLimit, accountUpdateLogList, hasCancel, websiteType);
		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldTimeSpentLimit));
	}

	private static void updateAndGetUpdateLog(String userId, CurrencyType currencyType,
		AccountUpdateType accountUpdateType,
		long takeEffectMinutes,
		int currentLimitsValue, int newLimitsValue,
		AccountPlayResponsiblySetting oldLimits, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel, int websiteType) {

		// 1. Decide state transition using decision class
		AccountPlayResponsiblyLimitsBO.PlayResponsiblyLimitUpdateDecision decision = AccountPlayResponsiblyLimitsBO.PlayResponsiblyLimitUpdateDecision.decide(
			currentLimitsValue,
			newLimitsValue,
			takeEffectMinutes
		);

		// check if current limit is <= new limit
		if (decision.getUpdateType()
			.equals(AccountPlayResponsiblyLimitsBO.PlayResponsiblyLimitUpdateDecision.UpdateType.IMMEDIATE_ACTIVE)) {
			String userKey = AccountUtils.getUserKey(websiteType, userId);

			try {
				//TODO: IF current limit 0 -> new Value any value (PERIOD START TIME SHOULD TAKE latest time)

				Timestamp periodStartTime =null;

				if(currentLimitsValue ==  TimeSpentLimitType.NO_TIME_SPENT_LIMIT.unique() && currentLimitsValue < newLimitsValue){
					periodStartTime = new Timestamp(System.currentTimeMillis());
				}
				GameSessionUsageBO.updateGameSessionUsageIfLimitUpdateImmediate(userKey, BigDecimal.valueOf(newLimitsValue),periodStartTime);
			} catch (Exception e) {
				LogUtils.SYS.error(
					"Error updating usage of gameSessionUsage during limits affect immediately userId: {}", userId);
			}
		}

		String displayName = accountUpdateType.getDisplayName();
		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(oldLimits.getType());
		boolean isCurrencyType = (type != AccountPlayResponsiblyType.TIME_SPENT_LIMIT);

		UpdateRecord updateRecord = AccountPlayResponsiblyLimitsBO.PlayResponsiblyLimitUpdateRecordGenerator.generate(
			currencyType,
			decision,
			currentLimitsValue,
			newLimitsValue,
			displayName,
			hasCancel,
			isCurrencyType
		);

		if (updateRecord != null) {
			accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, accountUpdateType, updateRecord));
		}

		oldLimits.setCurrentValue(String.valueOf(decision.getNewCurrentValue()));
		oldLimits.setNewValue(String.valueOf(newLimitsValue));
		oldLimits.setStatus(decision.getStatusType().unique());
		oldLimits.setEffectiveTime(decision.getEffectiveTime());
	}

	private static Timestamp getUpdatedEffectiveTime(int months) {
		// this will auto handle month-end 29/30/31
		return Timestamp.valueOf(LocalDateTime.now().plusMonths(months));
	}

	private static boolean checkStatusAndGetCancelUpdateLog(String userId, CurrencyType currencyType,
		AccountPlayResponsiblyType type, AccountUpdateType accountUpdateType,
		AccountPlayResponsiblySetting oldLimits, LinkedList<AccountUpdateLog> accountUpdateLogList) {

		// already in PENDING, add CANCEL UpdateLog
		if (!oldLimits.isActive()) {

			UpdateRecord cancelUpdateRecord = new UpdateRecord();
			AccountPlayResponsiblySettingBO.generateCancelUpdateRecord(currencyType, type, oldLimits,
				cancelUpdateRecord);

			accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, accountUpdateType,
				cancelUpdateRecord));

			return true;
		}

		return false;
	}
}
