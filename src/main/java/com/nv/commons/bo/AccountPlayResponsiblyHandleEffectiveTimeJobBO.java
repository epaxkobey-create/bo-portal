package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.nv.commons.cache.AccountCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.RealityCheckType;
import com.nv.commons.constants.SelfExclusionType;
import com.nv.commons.constants.SessionExpiryType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeSpentLimitType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dao.GameSessionUsageDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * HandleEffectiveTimeJob 處理的，一定是 new value > current value 或是 new value == no limit
 */
public class AccountPlayResponsiblyHandleEffectiveTimeJobBO {

	public static final Logger logger = LogUtils.SYS;

	private enum EndTimeUpdateMode {
		NO_UPDATE,
		SET_END_TIME,
		CLEAR_END_TIME
	}

	private static final Map<AccountPlayResponsiblyType, Function<AccountPlayResponsiblySetting, UpdateRecord>> UPDATE_RECORD_GENERATORS;

	static {
		UPDATE_RECORD_GENERATORS = new EnumMap<>(AccountPlayResponsiblyType.class);
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.WAGER_LIMITS,
			s -> generateLimitsUpdateRecord(s, "Wager Limit"));
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.LOSS_LIMITS,
			s -> generateLimitsUpdateRecord(s, "Loss Limit"));
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.DEPOSIT_LIMITS,
			s -> generateLimitsUpdateRecord(s, "Deposit Limit"));
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.SESSION_EXPIRY,
			AccountPlayResponsiblyHandleEffectiveTimeJobBO::generateSessionExpiryUpdateRecord);
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.SELF_EXCLUSION,
			AccountPlayResponsiblyHandleEffectiveTimeJobBO::generateSelfExclusionUpdateRecord);
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.REALITY_CHECK,
			AccountPlayResponsiblyHandleEffectiveTimeJobBO::generateRealityCheckUpdateRecord);
		UPDATE_RECORD_GENERATORS.put(AccountPlayResponsiblyType.TIME_SPENT_LIMIT,
			AccountPlayResponsiblyHandleEffectiveTimeJobBO::generateTimeSpentLimitUpdateRecord);
	}

	public static void checkAndUpdate() {
		checkEffective();
	}

	private static void checkEffective() {

		Timestamp now = new Timestamp(System.currentTimeMillis());

		List<AccountPlayResponsiblySetting> playResponsiblyList = new ArrayList<>();
		LinkedList<AccountUpdateLog> accountUpdateLogList = new LinkedList<>();

		List<String> needUpdateAnnualReminderUserKeyList = new ArrayList<>();

		List<AccountPlayResponsiblySetting> allSettings = Collections.emptyList();
		try {
			allSettings = DbExecutor.query(AccountPlayResponsiblySettingDAO::findAll);
		} catch (Exception e) {
			LogUtils.SYS.error(
				"Failed to query play responsibly settings: " + e.getMessage(),
				e);
			return;
		}

		for (AccountPlayResponsiblySetting playResponsibly : allSettings) {

			AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(playResponsibly.getType());

			if (type == null) {
				LogUtils.SYS.error("type is null\n{}", JSONUtils.toJsonString(playResponsibly));
				continue;
			}

			switch (type) {
				case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS, TIME_SPENT_LIMIT -> {

					if (isTimeToTurnActive(playResponsibly, now)) {

						generateUpdateLog(playResponsibly, accountUpdateLogList);

						prepareActivatePlayResponsibly(playResponsibly, playResponsibly.getNewValue(),
							playResponsibly.getNewValue(), EndTimeUpdateMode.NO_UPDATE,
							playResponsiblyList);

//						needUpdateAnnualReminderUserKeyList.add(
//							AccountUtils.getUserKey(playResponsibly.getWebsiteType(), playResponsibly.getUserId()));
						if (type == AccountPlayResponsiblyType.TIME_SPENT_LIMIT) {
							String userKey = AccountUtils.getUserKey(
								playResponsibly.getWebsiteType(), playResponsibly.getUserId()
							);
							BigDecimal newLimitSnapshot = BigDecimal.valueOf(
									Integer.parseInt(playResponsibly.getNewValue()))
								.multiply(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER));

							resetGameSessionUsageAndUpdateLimit(userKey, newLimitSnapshot);
						}
					}

				}

				case SESSION_EXPIRY, REALITY_CHECK -> {

					if (isTimeToTurnActive(playResponsibly, now)) {

						generateUpdateLog(playResponsibly, accountUpdateLogList);

						prepareActivatePlayResponsibly(playResponsibly, playResponsibly.getNewValue(),
							playResponsibly.getNewValue(), EndTimeUpdateMode.NO_UPDATE,
							playResponsiblyList);
					}
				}

				case SELF_EXCLUSION -> {
					if (playResponsibly.getEffectiveTime() != null
						&& isTimeReached(playResponsibly.getEffectiveTime(), now)) {

						generateUpdateLog(playResponsibly, accountUpdateLogList);

						prepareActivatePlayResponsibly(playResponsibly, playResponsibly.getNewValue(),
							playResponsibly.getNewValue(), EndTimeUpdateMode.SET_END_TIME,
							playResponsiblyList);

					} else if (playResponsibly.getEffectiveEndTime() != null
						&& isTimeReached(playResponsibly.getEffectiveEndTime(), now)) {

						generateUpdateLog(playResponsibly, accountUpdateLogList);

						String newValue = String.valueOf(SelfExclusionType.NO_EXCLUSION.unique());

						prepareActivatePlayResponsibly(playResponsibly, newValue,
							newValue, EndTimeUpdateMode.CLEAR_END_TIME,
							playResponsiblyList);
					}
				}

				default -> {
				}
			}
		}

		if (!playResponsiblyList.isEmpty()) {
			batchUpdatePlayResponsibly(playResponsiblyList, accountUpdateLogList, needUpdateAnnualReminderUserKeyList);
			logger.info("--- Done Update PlayResponsibly ---");
		}
	}

	private static boolean isTimeToTurnActive(AccountPlayResponsiblySetting playResponsibly, Timestamp now) {
		// not yet active
		return !playResponsibly.isActive()
			&& isTimeReached(
			playResponsibly.getEffectiveTime(), now);
	}

	private static void prepareActivatePlayResponsibly(AccountPlayResponsiblySetting playResponsibly,
		String currentValue, String newValue, EndTimeUpdateMode endTimeUpdateMode,
		List<AccountPlayResponsiblySetting> playResponsiblyList) {

		playResponsibly.setCurrentValue(currentValue);
		playResponsibly.setNewValue(newValue);
		playResponsibly.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		playResponsibly.setEffectiveTime(null);
		switch (endTimeUpdateMode) {
			case SET_END_TIME -> playResponsibly.setEffectiveEndTime();
			case CLEAR_END_TIME -> playResponsibly.setEffectiveEndTime(null);
			case NO_UPDATE -> {
			}
		}
		playResponsibly.setUpdater("SYS");

		playResponsiblyList.add(playResponsibly);
	}

	private static void batchUpdatePlayResponsibly(List<AccountPlayResponsiblySetting> playResponsiblyList,
		LinkedList<AccountUpdateLog> accountUpdateLogList, List<String> needUpdateAnnualReminderUserKeyList) {

		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			for (AccountPlayResponsiblySetting playResponsibly : playResponsiblyList) {
				AccountPlayResponsiblySettingDAO.update(conn, playResponsibly);
			}

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		// Update Annual Reminder (to not remind user this year)
		for (String userKey : needUpdateAnnualReminderUserKeyList) {
			Account account = AccountCache.getInstance().getAccount(userKey);

			WebSiteType accountWebSiteType = WebSiteType.getInstance(account.getWebsiteType());
			String accountUserId = account.getUserId();
			CurrencyType accountCurrencyType = CurrencyType.getInstance(account.getCurrencyTypeId());

			// fix for update
			ThreadLocalUtils.set(accountWebSiteType);
			ThreadLocalUtils.set(accountCurrencyType);

			AccountPlayResponsiblyLimitsBO.updateAnnualReminderToTrue(accountWebSiteType, accountUserId,
				"SYS", "0.0.0.0", true);
		}

		if (!accountUpdateLogList.isEmpty()) {

			AccountUpdateLogBO.batchInsert(accountUpdateLogList);

			Stream.of(
				AccountUpdateType.WAGER_LIMITS_DAILY, AccountUpdateType.WAGER_LIMITS_WEEKLY,
				AccountUpdateType.WAGER_LIMITS_MONTHLY,
				AccountUpdateType.LOSS_LIMITS_DAILY, AccountUpdateType.LOSS_LIMITS_WEEKLY,
				AccountUpdateType.LOSS_LIMITS_MONTHLY,
				AccountUpdateType.DEPOSIT_LIMITS_DAILY, AccountUpdateType.DEPOSIT_LIMITS_WEEKLY,
				AccountUpdateType.DEPOSIT_LIMITS_MONTHLY,
				AccountUpdateType.SESSION_EXPIRY, AccountUpdateType.SELF_EXCLUSION, AccountUpdateType.REALITY_CHECK
			).forEach(accountUpdateType -> {
				logger.info("Total {}: {} records is updated.",
					accountUpdateType.getDisplayName(),
					accountUpdateLogList.stream()
						.filter(log -> log.getLogType() == accountUpdateType.unique())
						.count());
			});
		}
	}

	private static void generateUpdateLog(AccountPlayResponsiblySetting playResponsibly,
		LinkedList<AccountUpdateLog> accountUpdateLogList) {

		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(playResponsibly.getType());
		Function<AccountPlayResponsiblySetting, UpdateRecord> generator = UPDATE_RECORD_GENERATORS.get(type);

		if (generator == null) {
			LogUtils.SYS.error("No update record generator registered for type: {}", type);
			return;
		}

		UpdateRecord updateRecord = generator.apply(playResponsibly);

		if (updateRecord != null) {
			var periodType = AccountPlayResponsiblyPeriodType.getInstanceOf(playResponsibly.getPeriodType());

			accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(playResponsibly.getUserId(),
				playResponsibly.getWebsiteType(),
				type.getAccountUpdateType(periodType),
				updateRecord, "SYS", "0.0.0.0", CurrencyType.EUR.unique()));
		}
	}

	private static UpdateRecord generateLimitsUpdateRecord(AccountPlayResponsiblySetting setting, String
		limitTypeName) {
		int currentValue = Integer.parseInt(setting.getCurrentValue());
		int newValue = Integer.parseInt(setting.getNewValue());
		AccountPlayResponsiblyPeriodType periodType = AccountPlayResponsiblyPeriodType.getInstanceOf(
			setting.getPeriodType());

		String newOrRevoke = (newValue == SystemConstants.NO_LIMIT_SETTING)
			? "Revoke" : "New";

		String fullName = formatLimitsValueWithCurrency(CurrencyType.EUR, newValue);

		String label = limitTypeName + " (" + periodType.getFullName(null) + ")";

		return getRecord(setting, newOrRevoke, "Limit",
			label,
			fullName);
	}

	private static String buildPendingString(String newOrRevoke, String itemType, String valueDisplay, Timestamp
		effectiveTime) {
		return "Pending " + newOrRevoke + " " + itemType +
			(valueDisplay != null ? ": " + valueDisplay : "") +
			"\n(Effective " + DateUtils.toString(effectiveTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma)
			+ ")";

	}

	private static UpdateRecord generateSessionExpiryUpdateRecord(AccountPlayResponsiblySetting setting) {
		SessionExpiryType currentSessionExpiryType = SessionExpiryType.getInstanceOf(setting.getCurrentValue());
		SessionExpiryType newSessionExpiryType = SessionExpiryType.getInstanceOf(setting.getNewValue());

		String newOrRevoke = (newSessionExpiryType == SessionExpiryType.NONE)
			? "Revoke" : "New";

		String label = SessionExpiryType.class.getSimpleName();
		String fullName = newSessionExpiryType.getFullName();

		return getRecord(setting, newOrRevoke, "Session",
			label,
			fullName);
	}

	private static UpdateRecord generateSelfExclusionUpdateRecord(AccountPlayResponsiblySetting setting) {
		String before, after, message;

		SelfExclusionType currentSelfExclusionType = SelfExclusionType.getInstanceOf(setting.getCurrentValue());
		SelfExclusionType newSelfExclusionType = SelfExclusionType.getInstanceOf(setting.getNewValue());

		if (!setting.isActive()) {
			String newOrRevoke =
				(newSelfExclusionType == SelfExclusionType.NO_EXCLUSION)
					? "Revoke" : "New";

			String label = SelfExclusionType.class.getSimpleName();
			String fullName = newSelfExclusionType.getFullName();

			return getRecord(setting, newOrRevoke, "Exclusion",
				label,
				fullName);

		} else {
			// MEMO: full name is different
			before = "Current Exclusion: " + currentSelfExclusionType.getFullName();
			after = "Current Exclusion: " + SelfExclusionType.NO_EXCLUSION.getFullName();
			message = "SYS update Self Exclusion to No Exclusion";

			return new UpdateRecord(before, after, message);
		}
	}

	private static UpdateRecord generateTimeSpentLimitUpdateRecord(AccountPlayResponsiblySetting setting) {
		int newValue = Integer.parseInt(setting.getNewValue());
		TimeSpentLimitType newTimeSpentLimitType = TimeSpentLimitType.getInstanceOf(newValue);

		String newOrRevoke = (newValue == SystemConstants.NO_LIMIT_SETTING)
			? "Revoke" : "New";

		String label = "Time Spent Limit (Daily)";
		String fullName = newTimeSpentLimitType != null ? newTimeSpentLimitType.getName() : String.valueOf(newValue);

		return getRecord(setting, newOrRevoke, "Limit",
			label,
			fullName);
	}

	private static UpdateRecord generateRealityCheckUpdateRecord(AccountPlayResponsiblySetting setting) {
		RealityCheckType currentRealityCheckType = RealityCheckType.getInstanceOf(setting.getCurrentValue());
		RealityCheckType newRealityCheckType = RealityCheckType.getInstanceOf(setting.getNewValue());

		// Reality_Check不会有Revoke , Revoke只会出现在能选"None" (无) 的Type
		String newOrRevoke = "New";
		String label = RealityCheckType.class.getSimpleName();
		String fullName = newRealityCheckType.getFullName();

		return getRecord(setting, newOrRevoke, "Reminder",
			label,
			fullName);
	}

	@NotNull
	private static UpdateRecord getRecord(AccountPlayResponsiblySetting setting,
		String newOrRevoke, String itemType, String label, String fullName) {

		String before = buildPendingString(newOrRevoke, itemType,
			newOrRevoke.equals("New") ? fullName : null,
			setting.getEffectiveTime());

		String after = "Current " + itemType + ": " + fullName;

		String message = "SYS update " + label;

		return new UpdateRecord(before, after, message);
	}

	private static boolean isTimeReached(Timestamp time, Timestamp currentTime) {
		return time.before(currentTime)
			|| Math.abs(DateUtils.secondsBetween(time.getTime(), currentTime.getTime())) <= 60;
	}

	private static String formatLimitsValueWithCurrency(CurrencyType currencyType, Number limitsValue) {
		return currencyType.getCurrencySymbol() + " " +
			FormatUtils.numberFormat(limitsValue, FormatUtils.NUMBER_PATTERN_THOUSAND_SEPARATOR_NO_DECIMAL);
	}

	private static void resetGameSessionUsageAndUpdateLimit(String userKey, BigDecimal newLimitSnapshot) {
		try {
			DbExecutor.update(conn -> {
				GameSessionUsage existing = GameSessionUsageDAO.getGameSessionUsage(conn, userKey);
				if (existing == null) {
					logger.debug("resetGameSessionUsage: no record found for user {}, skip", userKey);
					return 0;
				}
				return GameSessionUsageDAO.resetUsageAndUpdateLimit(conn, userKey, newLimitSnapshot, existing.getVersion());
			});
			logger.info("resetGameSessionUsage success for user {}, newLimitSnapshot={}s", userKey, newLimitSnapshot);
		} catch (Exception e) {
			logger.error("resetGameSessionUsage failed for user {}: {}", userKey, e.getMessage(), e);
		}
	}
}
