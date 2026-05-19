package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;

/**
 * @author Ken
 */
public class AccountPlayResponsiblyLimitsBO {

	/**
	 * Decision class for limit updates using state machine pattern
	 */
	static class PlayResponsiblyLimitUpdateDecision {

		enum UpdateType {
			IMMEDIATE_ACTIVE,    // Takes effect immediately (decrease, new limit equal to current)
			PENDING_INCREASE,    // Takes effect after waiting period (increase limit)
			PENDING_REVOKE,      // Takes effect after waiting period (revoke limit)
			NO_CHANGE            // No change needed
		}

		private final UpdateType updateType;
		private final int newCurrentValue;
		private final AccountPlayResponsiblyStatusType statusType;
		private final Timestamp effectiveTime;

		public static PlayResponsiblyLimitUpdateDecision decide(
			int currentValue,
			int newValue,
			long takeEffectMinutes) {

			final boolean currentIsNoLimit = (currentValue == SystemConstants.NO_LIMIT_SETTING);
			final boolean limitDoesNotChange = (newValue == currentValue);
			final boolean limitRevoked = (newValue == SystemConstants.NO_LIMIT_SETTING);
			final boolean limitIsIncreased = newValue > currentValue;
			final boolean limitIsDecreased = (newValue < currentValue) && !limitRevoked;

			boolean shouldImmediateActive = currentIsNoLimit || limitDoesNotChange || limitIsDecreased;

			if (shouldImmediateActive) {
				return new PlayResponsiblyLimitUpdateDecision(
					UpdateType.IMMEDIATE_ACTIVE,
					newValue,
					AccountPlayResponsiblyStatusType.ACTIVE,
					null
				);
			} else {
				// 2. New is no limit OR new > current -> pending with waiting period
				Timestamp effectiveTime = calculateEffectiveTime(takeEffectMinutes);
				UpdateType type = limitRevoked
					? UpdateType.PENDING_REVOKE
					: UpdateType.PENDING_INCREASE;

				return new PlayResponsiblyLimitUpdateDecision(
					type,
					currentValue,  // Keep current value unchanged
					AccountPlayResponsiblyStatusType.PENDING,
					effectiveTime
				);
			}
		}

		private static Timestamp calculateEffectiveTime(long takeEffectMinutes) {
			return Timestamp.from(
				new Timestamp(System.currentTimeMillis())
					.toInstant()
					.plus(Duration.ofMinutes(takeEffectMinutes))
			);
		}

		public UpdateType getUpdateType() {
			return updateType;
		}

		public int getNewCurrentValue() {
			return newCurrentValue;
		}

		public AccountPlayResponsiblyStatusType getStatusType() {
			return statusType;
		}

		public Timestamp getEffectiveTime() {
			return effectiveTime;
		}

		private PlayResponsiblyLimitUpdateDecision(UpdateType updateType, int newCurrentValue,
			AccountPlayResponsiblyStatusType statusType, Timestamp effectiveTime) {
			this.updateType = updateType;
			this.newCurrentValue = newCurrentValue;
			this.statusType = statusType;
			this.effectiveTime = effectiveTime;
		}
	}

	/**
	 * Generator for UpdateRecord based on update type
	 */
	static class PlayResponsiblyLimitUpdateRecordGenerator {

		public static UpdateRecord generate(
			CurrencyType currencyType,
			PlayResponsiblyLimitUpdateDecision decision,
			int currentValue,
			int newValue,
			String updateDisplayName,
			boolean hasCancel, boolean isCurrencyType) {

			String currentValueStr = isCurrencyType ?
				formatValue(currencyType, currentValue) : formatValue(currentValue);
			String newValueStr = isCurrencyType ? formatValue(currencyType, newValue) : formatValue(newValue);

			return switch (decision.getUpdateType()) {
				case IMMEDIATE_ACTIVE -> {
					if (hasCancel) {
						// Determine if decrease after cancel
						if (newValue < currentValue) {
							yield new UpdateRecord(
								"Current Limit: " + currentValueStr,
								"Current Limit: " + newValueStr,
								"Decrease " + updateDisplayName
							);
						} else {
							yield null;
						}
					}

					// Determine if new or decrease
					if (currentValue == SystemConstants.NO_LIMIT_SETTING
						|| newValue == currentValue) {
						yield new UpdateRecord(
							"Current Limit: " + currentValueStr,
							"Current Limit: " + newValueStr,
							"New " + updateDisplayName
						);
					} else {
						yield new UpdateRecord(
							"Current Limit: " + currentValueStr,
							"Current Limit: " + newValueStr,
							"Decrease " + updateDisplayName
						);
					}
				}

				case PENDING_INCREASE -> new UpdateRecord(
					"Current Limit: " + currentValueStr,
					"Pending New Limit: " + newValueStr + "\n(Effective " +
						formatEffectiveTime(decision.getEffectiveTime()) + ")",
					"New " + updateDisplayName
				);

				case PENDING_REVOKE -> new UpdateRecord(
					"Current Limit: " + currentValueStr,
					"Pending Revoke Limit\n(Effective " +
						formatEffectiveTime(decision.getEffectiveTime()) + ")",
					"Revoke " + updateDisplayName
				);

				case NO_CHANGE -> null;
			};
		}

		private static String formatValue(CurrencyType currencyType, int value) {
			return AccountPlayResponsiblySettingBO.formatLimitsValueWithCurrency(
				currencyType, value);
		}

		private static String formatEffectiveTime(Timestamp effectiveTime) {
			return DateUtils.toString(effectiveTime,
				FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma);
		}

		private static String formatValue(int value){
			return AccountPlayResponsiblySettingBO.formatLimitsValueWithHour(value);
		}
	}

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateLimits(String userId,
		WebsiteSystemSettingType settingType,
		AccountPlayResponsiblyType type, List<AccountPlayResponsiblySetting> playResponsiblyList,
		String updater, String updaterIp, LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());
		LogUtils.accountPlayResponsibly.info("currencyType: {}", currencyType.unique());

		for (var newLimits : playResponsiblyList) {

			AccountPlayResponsiblyPeriodType periodType = AccountPlayResponsiblyPeriodType.getInstanceOf(
				newLimits.getPeriodType());

			AccountPlayResponsiblySetting oldLimits = AccountPlayResponsiblySettingBO.getPlayResponsiblyFromCacheOrDB(
				userId, webSiteType, type, periodType
			);

			AccountPlayResponsiblySetting updatedLimits;

			if (oldLimits == null) {

				updatedLimits = processInsertLimits(
					userId,
					webSiteType,
					currencyType,
					type,
					periodType,
					newLimits.getNewValue(),
					accountUpdateLogList);
			} else {

				String takeEffectMinutesSettingValue =
					WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(), currencyType.unique(),
						settingType.unique()
					);
				long takeEffectMinutes = Long.parseLong(takeEffectMinutesSettingValue);

				oldLimits.setUpdater(updater);

				AccountUpdateType accountUpdateType = type.getAccountUpdateType(periodType);

				updatedLimits = processUpdateLimits(
					userId,
					currencyType,
					type,
					accountUpdateType,
					newLimits.getNewValue(),
					oldLimits,
					takeEffectMinutes,
					accountUpdateLogList);
			}

			if (updatedLimits != null) {
				try {
					AccountPlayResponsiblySettingBO.setLimitRuleToSeamlessWalletServer(userId, type, periodType,
						new BigDecimal(updatedLimits.getNewValue()).doubleValue(),
						updatedLimits.getEffectiveTime());
				} catch (Exception e) {
					LogUtils.accountPlayResponsibly.error("{} Limit set rule error: {}", type, e.getMessage());
				}
			}
		}
	}

	private static AccountPlayResponsiblySetting processInsertLimits(
		String userId,
		WebSiteType webSiteType,
		CurrencyType currencyType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newValue,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		int newLimitsValue = Integer.parseInt(newValue);

		if (type != AccountPlayResponsiblyType.DEPOSIT_LIMITS && newLimitsValue == SystemConstants.NO_LIMIT_SETTING) {
			return null;
		}

		AccountPlayResponsiblySetting newLimits = AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId,
			webSiteType, type, periodType, newValue, userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newLimits));

		String before = "Current Limit: " + AccountPlayResponsiblySettingBO.formatLimitsValueWithCurrency(currencyType,
			SystemConstants.NO_LIMIT_SETTING);
		String after = "Current Limit: " + AccountPlayResponsiblySettingBO.formatLimitsValueWithCurrency(currencyType,
			Integer.parseInt(newLimits.getNewValue()));

		accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType),
			before, after, "New " + type.getAccountUpdateType(periodType).getDisplayName()));

		return newLimits;
	}

	private static AccountPlayResponsiblySetting processUpdateLimits(
		String userId,
		CurrencyType currencyType,
		AccountPlayResponsiblyType type,
		AccountUpdateType accountUpdateType,
		String newValue,
		AccountPlayResponsiblySetting oldLimits,
		long takeEffectMinutes,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		int newLimitsValue = Integer.parseInt(newValue);
		int currentLimitsValue = Integer.parseInt(oldLimits.getCurrentValue());

		// same newValue
		if (newLimitsValue == Integer.parseInt(oldLimits.getNewValue())) {
			return null;
		}

		boolean hasCancel = checkStatusAndGetCancelUpdateLog(userId, currencyType, type, accountUpdateType, oldLimits,
			accountUpdateLogList);

		updateAndGetUpdateLog(userId, currencyType, accountUpdateType, takeEffectMinutes,
			currentLimitsValue, newLimitsValue, oldLimits, accountUpdateLogList, hasCancel);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldLimits));

		return oldLimits;
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

	private static void updateAndGetUpdateLog(String userId, CurrencyType currencyType,
		AccountUpdateType accountUpdateType,
		long takeEffectMinutes,
		int currentLimitsValue, int newLimitsValue,
		AccountPlayResponsiblySetting oldLimits, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel) {

		// 1. Decide state transition using decision class
		PlayResponsiblyLimitUpdateDecision decision = PlayResponsiblyLimitUpdateDecision.decide(
			currentLimitsValue,
			newLimitsValue,
			takeEffectMinutes
		);

		// 2. Generate UpdateRecord using generator
		String displayName = accountUpdateType.getDisplayName();
		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(oldLimits.getType());
		boolean isCurrencyType = (type != AccountPlayResponsiblyType.TIME_SPENT_LIMIT);

		UpdateRecord updateRecord = PlayResponsiblyLimitUpdateRecordGenerator.generate(
			currencyType,
			decision,
			currentLimitsValue,
			newLimitsValue,
			displayName,
			hasCancel, isCurrencyType
		);

		// 3. Add to log list
		if (updateRecord != null) {
			accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, accountUpdateType, updateRecord));
		}

		// 4. Update DTO
		oldLimits.setCurrentValue(String.valueOf(decision.getNewCurrentValue()));
		oldLimits.setNewValue(String.valueOf(newLimitsValue));
		oldLimits.setStatus(decision.getStatusType().unique());
		oldLimits.setEffectiveTime(decision.getEffectiveTime());
	}

	public static void updateAnnualReminderToTrue(WebSiteType webSiteType, String userId,
		String updater, String updaterIp, boolean isFromJobChecking) {
		try {

			boolean isWagerLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.WAGER_LIMITS, isFromJobChecking);
			boolean isLossLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.LOSS_LIMITS, isFromJobChecking);
			boolean isDepositLimitSet = AccountPlayResponsiblySettingBO.isLimitsSet(
				userId, webSiteType, AccountPlayResponsiblyType.DEPOSIT_LIMITS, isFromJobChecking);

			// if any limit not set correctly, no need update annual reminder
			if (!isWagerLimitSet || !isLossLimitSet || !isDepositLimitSet) {
				return;
			}

			var prList = new ArrayList<AccountPlayResponsiblySetting>();
			var annualReminder =
				AccountPlayResponsiblySettingCache.getInstance().getPlayResponsiblyOrDefault(webSiteType, userId,
					AccountPlayResponsiblyType.ANNUAL_REMINDER, AccountPlayResponsiblyPeriodType.DAILY);
			annualReminder.setCurrentValue(String.valueOf(BinaryStatusType.ACTIVE.unique()));
			annualReminder.setNewValue(String.valueOf(BinaryStatusType.ACTIVE.unique()));
			prList.add(annualReminder);

			AnnualPlayResponsiblyReminderBO.updateAnnualReminder(userId, AccountPlayResponsiblyType.ANNUAL_REMINDER,
				prList, updater);

			AccountPlayResponsiblySettingCache.getInstance().update();
		} catch (Exception e) {
			LogUtils.accountPlayResponsibly.error("Error updating Annual Reminder after Limits updated: {}",
				e.getMessage());
		}
	}
}
