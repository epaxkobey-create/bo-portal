package com.nv.commons.bo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountReviewReminderType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.PeriodType;
import com.nv.commons.constants.RealityCheckType;
import com.nv.commons.constants.SelfExclusionType;
import com.nv.commons.constants.SessionExpiryType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeSpentLimitType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.PlayResponsiblyAllResponse;
import com.nv.commons.dto.PlayResponsiblyDetailedResponse;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ken
 */
public class AccountPlayResponsiblySettingBO {

	public static List<AccountPlayResponsiblySetting> findAll(String userId, WebSiteType webSiteType)
		throws Exception {

		List<AccountPlayResponsiblySetting> playResponsiblyList = new ArrayList<>();

		Arrays.stream(AccountPlayResponsiblyType.values())
			.forEach(type -> {
				try {
					Optional.ofNullable(findAllWithDefault(userId, webSiteType, type))
						.ifPresent(playResponsiblyList::addAll);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

		return playResponsiblyList;
	}

	public static List<AccountPlayResponsiblySetting> findAllWithDefault(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type)
		throws Exception {
		return findAll(userId, webSiteType, type, true);
	}

	public static List<AccountPlayResponsiblySetting> findAll(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type)
		throws Exception {
		return findAll(userId, webSiteType, type, false);
	}

	private static List<AccountPlayResponsiblySetting> findAll(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type, boolean defaultIfNotExist)
		throws Exception {

		try {

			return DbExecutor.query(conn -> {

				if (!AccountPlayResponsiblySettingDAO.isExist(conn, userId, webSiteType.unique(), type.unique())
					&& defaultIfNotExist) {

					List<AccountPlayResponsiblySetting> playResponsiblyList = new ArrayList<>();

					// daily
					playResponsiblyList.add(generateNewPlayResponsibly(userId, webSiteType,
						type, AccountPlayResponsiblyPeriodType.DAILY, null, userId));

					// weekly and monthly
					if (type == AccountPlayResponsiblyType.WAGER_LIMITS
						|| type == AccountPlayResponsiblyType.LOSS_LIMITS
						|| type == AccountPlayResponsiblyType.DEPOSIT_LIMITS) {
						playResponsiblyList.add(generateNewPlayResponsibly(userId, webSiteType,
							type, AccountPlayResponsiblyPeriodType.WEEKLY, null, userId));
						playResponsiblyList.add(generateNewPlayResponsibly(userId, webSiteType,
							type, AccountPlayResponsiblyPeriodType.MONTHLY, null, userId));
					}

					return playResponsiblyList;
				}

				return AccountPlayResponsiblySettingDAO.findAll(conn, userId, webSiteType.unique(), type.unique());
			});
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			if (e instanceof Deviation deviation) {
				throw deviation;
			}
		}

		return Collections.emptyList();
	}

	public static boolean update(String userId,
		AccountPlayResponsiblyType type,
		List<AccountPlayResponsiblySetting> playResponsiblyList,
		String updater, String updaterIp,
		boolean isBOAdmin, boolean isFromRegister)
		throws Exception {

		AccountUpdateLogUtils.setInfo(updater, updaterIp);

		LinkedList<AccountUpdateLog> accountUpdateLogList = new LinkedList<>();

		try {

			switch (type) {

				case WAGER_LIMITS -> {
					WebsiteSystemSettingType settingType = WebsiteSystemSettingType.WAGER_LIMITS_MODIFY_TAKE_EFFECT_MINUTES;

					AccountPlayResponsiblyLimitsBO.updateLimits(userId,
						settingType,
						type, playResponsiblyList, updater, updaterIp, accountUpdateLogList);
				}

				case LOSS_LIMITS -> {
					WebsiteSystemSettingType settingType = WebsiteSystemSettingType.LOSS_LIMITS_MODIFY_TAKE_EFFECT_MINUTES;

					AccountPlayResponsiblyLimitsBO.updateLimits(userId,
						settingType,
						type, playResponsiblyList, updater, updaterIp, accountUpdateLogList);
				}

				case SESSION_EXPIRY -> SessionExpiryBO.updateSessionExpiry(userId,
					type, playResponsiblyList, updater, accountUpdateLogList);

				case SELF_EXCLUSION -> SelfExclusionBO.updateSelfExclusion(userId,
					type, playResponsiblyList, updater, accountUpdateLogList, isBOAdmin);

				case REALITY_CHECK -> RealityCheckBO.updateRealityCheck(userId,
					playResponsiblyList, updater, accountUpdateLogList);

				case DEPOSIT_LIMITS -> {
					WebsiteSystemSettingType settingType = WebsiteSystemSettingType.DEPOSIT_LIMITS_MODIFY_TAKE_EFFECT_MINUTES;

					AccountPlayResponsiblyLimitsBO.updateLimits(userId,
						settingType,
						type, playResponsiblyList, updater, updaterIp, accountUpdateLogList);

					WebSocketBO.sendDepositLimitUsageUpdate(AccountCache.getInstance()
						.getAccount(ThreadLocalUtils.getWebSiteType().unique(), userId));
				}

				case ANNUAL_REMINDER -> AnnualPlayResponsiblyReminderBO.updateAnnualReminder(userId,
					type, playResponsiblyList, updater);

				case ACCOUNT_REVIEW_REMINDER -> AccountReviewReminderBO.updateAccountReviewReminder(userId,
					playResponsiblyList, updater, accountUpdateLogList);

				case TIME_SPENT_LIMIT -> TimeSpentLimitBO.updateTimeSpentLimit(
					userId,
					playResponsiblyList, updater, accountUpdateLogList
				);
			}

			AccountPlayResponsiblySettingCache.getInstance().update();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		}

		// Update Annual Reminder (to not remind user this year)
		AccountPlayResponsiblyLimitsBO.updateAnnualReminderToTrue(ThreadLocalUtils.getWebSiteType(), userId,
			userId, updaterIp, false);

		if (!isFromRegister) {

			Optional.of(accountUpdateLogList)
				.filter(list -> !list.isEmpty())
				.ifPresent(list -> {
					// MEMO: increase 1ms start from 2nd item to prevent incorrect order of update log
					for (int i = 1; i < list.size(); i++) {
						Timestamp prev = list.get(i - 1).getUpdateTime();
						Timestamp curr = list.get(i).getUpdateTime();
						if (curr.getTime() <= prev.getTime()) {
							list.get(i).setUpdateTime(new Timestamp(prev.getTime() + 1));
						}
					}

					AccountUpdateLogBO.batchInsert(list);
				});
		}

		return true;
	}

	public static boolean cancel(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType,
		String updater, String updaterIp)
		throws Exception {

		AccountUpdateLogUtils.setInfo(updater, updaterIp);

		switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> {
				if (periodType == null) {
					throw new Deviation().setI18N("fs.parameter.validation", "periodType");
				}
			}
		}

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LinkedList<AccountUpdateLog> accountUpdateLogList = new LinkedList<>();

		try {

			DbExecutor.update(conn -> {

				AccountPlayResponsiblySetting playResponsibly = AccountPlayResponsiblySettingCache.getInstance()
					.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

				if (playResponsibly != null && !playResponsibly.isActive()) {

					UpdateRecord cancelUpdateRecord = new UpdateRecord();
					generateCancelUpdateRecord(currencyType, type, playResponsibly, cancelUpdateRecord);

					accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
						userId, type.getAccountUpdateType(periodType),
						cancelUpdateRecord));

					playResponsibly.setNewValue(playResponsibly.getCurrentValue());
					playResponsibly.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
					playResponsibly.setEffectiveTime(null);
					// playResponsibly.setEffectiveEndTime(); // MEMO: no need set effectiveEndTime
					playResponsibly.setUpdater(updater);

					AccountPlayResponsiblySettingDAO.update(conn, playResponsibly);
				}

				return 1;
			});

			AccountPlayResponsiblySettingCache.getInstance().update();

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		}

		Optional.of(accountUpdateLogList)
			.filter(list -> !list.isEmpty())
			.ifPresent(AccountUpdateLogBO::batchInsert);

		try {
			cancelLimitRuleToSeamlessWalletServer(userId, type, periodType);
		} catch (Exception e) {
			LogUtils.accountPlayResponsibly.error("cancelBetRule error: {}", e.getMessage());
		}

		return true;
	}

	public static AccountPlayResponsiblySetting generateNewPlayResponsibly(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType, String newValue, String creator) {

		newValue = StringUtils.isEmpty(newValue) ? String.valueOf(type.getDefaultValue()) : newValue;

		Timestamp effectiveTime = null;

		if (type == AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER) {
			Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);
			if (account != null) {
				effectiveTime = Timestamp.valueOf(
					account.getSignUpTime().toLocalDateTime().plusMonths(Integer.parseInt(newValue)));
			}
		}

		AccountPlayResponsiblySetting playResponsibly = new AccountPlayResponsiblySetting();
		playResponsibly.setUserId(userId);
		playResponsibly.setWebsiteType(webSiteType.unique());
		playResponsibly.setType(type.unique());
		playResponsibly.setPeriodType(periodType.unique());
		playResponsibly.setCurrentValue(newValue);
		playResponsibly.setNewValue(newValue);
		playResponsibly.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		playResponsibly.setEffectiveTime(effectiveTime);
		playResponsibly.setEffectiveEndTime();
		playResponsibly.setCreator(creator);
		playResponsibly.setUpdater(creator);

		return playResponsibly;
	}

	public static void generateCancelUpdateRecord(CurrencyType currencyType, AccountPlayResponsiblyType type,
		AccountPlayResponsiblySetting playResponsibly, UpdateRecord cancelUpdateRecord) {

		String newOrRevoke =
			Integer.parseInt(playResponsibly.getNewValue()) == type.getDefaultValue() ? "Revoke" : "New";
		String effectiveTime =
			DateUtils.toString(playResponsibly.getEffectiveTime(),
				FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma);

		String cancelBefore = "";
		String cancelAfter = "";
		String cancelMessage = "";

		switch (type) {

			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> {

				String label = type.getAccountUpdateType(
					AccountPlayResponsiblyPeriodType.getInstanceOf(playResponsibly.getPeriodType())).getFullName(null);
				String newValue = formatLimitsValueWithCurrency(currencyType,
					Integer.parseInt(playResponsibly.getNewValue()));

				cancelBefore = "Pending " + newOrRevoke + " Limit" +
					(newOrRevoke.equals("New") ? ": " + newValue : "") + "\n(Effective " + effectiveTime + ")";
				cancelAfter = "Cancel " + cancelBefore;
				cancelMessage = "Cancel " + label;
			}
			case SESSION_EXPIRY -> {

				String newValue = SessionExpiryType.getInstanceOf(playResponsibly.getNewValue()).getFullName();

				cancelBefore = "Pending " + newOrRevoke + " Session" +
					(newOrRevoke.equals("New") ? ": " + newValue : "") + "\n(Effective " + effectiveTime + ")";
				cancelAfter = "Cancel " + cancelBefore;
				cancelMessage = "Cancel Session Expiry";
			}
			case SELF_EXCLUSION -> {

				String newValue = SelfExclusionType.getInstanceOf(playResponsibly.getNewValue()).getFullName();

				cancelBefore = "Pending " + newOrRevoke + " Exclusion" +
					(newOrRevoke.equals("New") ? ": " + newValue : "") + "\n(Effective " + effectiveTime + ")";
				cancelAfter = "Cancel " + cancelBefore;
				cancelMessage = "Cancel Self Exclusion";
			}
			case REALITY_CHECK -> {

				String newValue = RealityCheckType.getInstanceOf(playResponsibly.getNewValue()).getFullName();

				cancelBefore = "Pending New Reminder: " + newValue + "\n(Effective " + effectiveTime + ")";
				cancelAfter = "Cancel " + cancelBefore;
				cancelMessage = "Cancel Reality Check";
			}
			case TIME_SPENT_LIMIT ->{
				String label = type.getAccountUpdateType(
					AccountPlayResponsiblyPeriodType.getInstanceOf(playResponsibly.getPeriodType())).getFullName(null);
				String newValue = formatLimitsValueWithHour(
					Integer.parseInt(playResponsibly.getNewValue()));

				cancelBefore = "Pending " + newOrRevoke + " Limit" +
					(newOrRevoke.equals("New") ? ": " + newValue : "") + "\n(Effective " + effectiveTime + ")";
				cancelAfter = "Cancel " + cancelBefore;
				cancelMessage = "Cancel " + label;
			}

		}

		if (cancelUpdateRecord != null) {
			cancelUpdateRecord.setBeforeUpdate(cancelBefore);
			cancelUpdateRecord.setAfterUpdate(cancelAfter);
			cancelUpdateRecord.setMessage(cancelMessage);
		}
	}

	public static boolean checkStatusAndGetCancelUpdateLog(String userId, CurrencyType currencyType,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType,
		AccountPlayResponsiblySetting oldSetting, LinkedList<AccountUpdateLog> accountUpdateLogList) {

		// already in PENDING, add CANCEL UpdateLog
		if (!oldSetting.isActive()) {

			UpdateRecord cancelUpdateRecord = new UpdateRecord();
			generateCancelUpdateRecord(currencyType, type, oldSetting, cancelUpdateRecord);

			accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, type.getAccountUpdateType(periodType),
				cancelUpdateRecord));

			return true;
		}

		return false;
	}

	public static String getAllPlayResponsibly(String userId, WebSiteType webSiteType) throws Exception {

		List<AccountPlayResponsiblySetting> selfExclusion = getPlayResponsiblyFromCacheOrDB(
			userId, webSiteType, AccountPlayResponsiblyType.SELF_EXCLUSION
		);

		PlayResponsiblyAllResponse.SelfExclusionData selfExclusionData = new PlayResponsiblyAllResponse.SelfExclusionData(
			!selfExclusion.isEmpty() ? Integer.parseInt(selfExclusion.getFirst().getCurrentValue()) :
				AccountPlayResponsiblyType.SELF_EXCLUSION.getDefaultValue()
		);

		List<AccountPlayResponsiblySetting> timeSpentLimit = getPlayResponsiblyFromCacheOrDB(
			userId, webSiteType, AccountPlayResponsiblyType.TIME_SPENT_LIMIT
		);
		PlayResponsiblyAllResponse.TimeSpentLimitData timeSpentLimitData = new PlayResponsiblyAllResponse.TimeSpentLimitData(
			!timeSpentLimit.isEmpty() ? Integer.parseInt(timeSpentLimit.getFirst().getCurrentValue()) :
				AccountPlayResponsiblyType.TIME_SPENT_LIMIT.getDefaultValue()
		);

		PlayResponsiblyAllResponse response = new PlayResponsiblyAllResponse(
			getLimitsData(userId, webSiteType, AccountPlayResponsiblyType.DEPOSIT_LIMITS),
			getLimitsData(userId, webSiteType, AccountPlayResponsiblyType.WAGER_LIMITS),
			getLimitsData(userId, webSiteType, AccountPlayResponsiblyType.LOSS_LIMITS),
			null, null, selfExclusionData, timeSpentLimitData
		);

		Account account = AccountCache.getInstance().getAccount(webSiteType.unique(), userId);
		if (!account.hasViewedPlayResponsibly()) {
			AccountBO.updateViewedPlayResponsiblyToTrue(webSiteType.unique(), userId);
		}

		return JSONUtils.toJsonString(response);
	}

	private static PlayResponsiblyAllResponse.LimitsData getLimitsData(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type) {
		return buildLimitsData(
			getPlayResponsiblyFromCacheOrDB(userId, webSiteType, type),
			type);
	}

	public static String getPlayResponsibly(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type) {

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> jGenerator.writeObjectField(
			"playResponsiblyList", getPlayResponsiblyFromCacheOrDB(userId, webSiteType, type));

		return JSONUtils.getJSONString(processor);
	}

	public static String getPlayerAllResponsibilitiesWithJGenerator(String userId, WebSiteType webSiteType)
		throws Exception {

		List<AccountPlayResponsiblySetting> accountPlayResponsibiltyList =
			AccountPlayResponsiblySettingBO.findAll(userId, webSiteType);

		if (accountPlayResponsibiltyList.isEmpty()) {
			return JSONUtils.toJsonString(new PlayResponsiblyDetailedResponse());
		}

		Map<Integer, List<AccountPlayResponsiblySetting>> groupedResponsibly = accountPlayResponsibiltyList.stream()
			.collect(Collectors.groupingBy(AccountPlayResponsiblySetting::getType));

		PlayResponsiblyDetailedResponse response = new PlayResponsiblyDetailedResponse();

		// wagerLimits
		List<AccountPlayResponsiblySetting> wagerLimits = groupedResponsibly.getOrDefault(
			AccountPlayResponsiblyType.WAGER_LIMITS.unique(), List.of());
		if (!wagerLimits.isEmpty()) {
			response.setWagerLimits(buildLimitsDetailData(wagerLimits));
		}

		// lossLimits
		List<AccountPlayResponsiblySetting> lossLimits = groupedResponsibly.getOrDefault(
			AccountPlayResponsiblyType.LOSS_LIMITS.unique(), List.of());
		if (!lossLimits.isEmpty()) {
			response.setLossLimits(buildLimitsDetailData(lossLimits));
		}

		// depositLimits
		List<AccountPlayResponsiblySetting> depositLimits = groupedResponsibly.getOrDefault(
			AccountPlayResponsiblyType.DEPOSIT_LIMITS.unique(), List.of());
		if (!depositLimits.isEmpty()) {
			response.setDepositLimits(buildLimitsDetailData(depositLimits));
		}

		// realityCheck
		groupedResponsibly.getOrDefault(AccountPlayResponsiblyType.REALITY_CHECK.unique(), List.of())
			.stream().findFirst()
			.ifPresent(setting -> response.setRealityCheck(buildDetailData(setting, false)));

		// sessionExpiry
		groupedResponsibly.getOrDefault(AccountPlayResponsiblyType.SESSION_EXPIRY.unique(), List.of())
			.stream().findFirst()
			.ifPresent(setting -> response.setSessionExpiry(buildDetailData(setting, false)));

		// selfExclusion
		groupedResponsibly.getOrDefault(AccountPlayResponsiblyType.SELF_EXCLUSION.unique(), List.of())
			.stream().findFirst()
			.ifPresent(setting -> response.setSelfExclusion(buildDetailData(setting, true)));

		groupedResponsibly.getOrDefault(AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER.unique(), List.of())
			.stream().findFirst()
			.ifPresent(setting -> response.setAccountReviewReminder(buildDetailData(setting, false)));

		groupedResponsibly.getOrDefault(AccountPlayResponsiblyType.TIME_SPENT_LIMIT.unique(), List.of()).stream()
			.findFirst()
			.ifPresent(setting -> response.setTimeSpentLimit(buildDetailData(setting, false)));

		return JSONUtils.toJsonString(response);
	}

	public static void updatePlayerRealityCheckTime(Account playerInCache) {

		// reality check time not updated before
		if (!playerInCache.getRealityCheckReminderData().isRealityCheckTimeUpdated()) {

			// set reality check time updated to true
			playerInCache.getRealityCheckReminderData().setRealityCheckTimeUpdated(true);
		}
	}

	public static void checkAndSendRealityCheckReminderToPlayer() {

		// reality check time reached
		var playerInCacheList = PlayerLocalCache.getInstance().getAll().stream()
			.filter(playerInCache -> playerInCache.getRealityCheckReminderData().isRealityCheckTimeReached())
			.collect(Collectors.toList());

		playerInCacheList.forEach(playerInCache -> {
			WebSocketBO.sendRealityCheckReminder(playerInCache);
		});
	}

	public static void updateAccountReviewReminderTime(String userId, WebSiteType webSiteType) {
		AccountReviewReminderBO.extendEffectiveTime(userId, webSiteType);
		AccountPlayResponsiblySettingCache.getInstance().update();
	}

	public static void validateRequest(AccountPlayResponsiblyType type,
		List<AccountPlayResponsiblySetting> playResponsiblyList) {

		int[] periodTypes = playResponsiblyList != null && !playResponsiblyList.isEmpty()
			? playResponsiblyList.stream().mapToInt(AccountPlayResponsiblySetting::getPeriodType).toArray()
			: null;

		String[] newValues = playResponsiblyList != null && !playResponsiblyList.isEmpty()
			? playResponsiblyList.stream().map(AccountPlayResponsiblySetting::getNewValue).toArray(String[]::new)
			: null;

		if (!validateRequestPeriodType(type, periodTypes)) {
			throw new Deviation().setI18N("fs.parameter.validation", "periodType");
		} else if (!validateRequestNewValue(type, newValues)) {
			throw new Deviation().setI18N("fs.parameter.validation", "newValue");
		}

		validateRequestNewValueLimit(type, playResponsiblyList);
	}

	public static boolean validateRequestPeriodType(AccountPlayResponsiblyType type, int... periodTypes) {

		if (periodTypes == null || periodTypes.length == 0
			|| Arrays.stream(periodTypes).distinct().count() != periodTypes.length) {
			return false;
		}

		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> Arrays.stream(periodTypes)
				.allMatch(periodType -> AccountPlayResponsiblyPeriodType.getInstanceOf(periodType) != null);

			case SESSION_EXPIRY, SELF_EXCLUSION, REALITY_CHECK, ANNUAL_REMINDER, ACCOUNT_REVIEW_REMINDER,
				 TIME_SPENT_LIMIT -> Arrays.stream(periodTypes)
				.allMatch(periodType -> periodType == AccountPlayResponsiblyPeriodType.DAILY.unique());
		};
	}

	public static boolean validateRequestNewValue(AccountPlayResponsiblyType type, String... newValues) {

		if (newValues == null || newValues.length == 0
			|| Arrays.stream(newValues).anyMatch(StringUtils::isEmpty)) {
			return false;
		}

		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> Arrays.stream(newValues)
				.allMatch(newValue -> {
					try {
						int newValueInt = Integer.parseInt(newValue);
						return newValueInt == SystemConstants.NO_LIMIT_SETTING
							|| (newValueInt >= SystemConstants.MIN_LIMIT_SETTING
							&& newValueInt <= SystemConstants.MAX_LIMIT_SETTING);
					} catch (NumberFormatException e) {
						LogUtils.SYS.error(e.getMessage(), e);
						return false;
					}
				});

			case SESSION_EXPIRY -> Arrays.stream(newValues)
				.allMatch(newValue -> SessionExpiryType.getInstanceOf(newValue) != null);

			case SELF_EXCLUSION -> Arrays.stream(newValues)
				.allMatch(newValue -> {
					try {
						return SelfExclusionType.getInstanceOf(newValue) != null;
					} catch (NumberFormatException e) {
						return false;
					}
				});

			case REALITY_CHECK -> Arrays.stream(newValues)
				.allMatch(newValue -> RealityCheckType.getInstanceOf(newValue) != null);

			case ANNUAL_REMINDER -> Arrays.stream(newValues)
				.allMatch(newValue -> Integer.parseInt(newValue) == BinaryStatusType.ACTIVE.unique());

			case ACCOUNT_REVIEW_REMINDER -> Arrays.stream(newValues)
				.allMatch(newValue -> AccountReviewReminderType.getInstanceOf(newValue) != null);

			case TIME_SPENT_LIMIT -> Arrays.stream(newValues)
				.allMatch(newValue -> {
					try {
						return TimeSpentLimitType.getInstanceOf(newValue) != null;
					} catch (NumberFormatException e) {
						return false;
					}
				});
		};
	}

	public static void validateRequestNewValueLimit(AccountPlayResponsiblyType type,
		List<AccountPlayResponsiblySetting> playResponsiblyList) {

		String limitLabel;

		switch (type) {
			case WAGER_LIMITS -> limitLabel = "wager";
			case LOSS_LIMITS -> limitLabel = "loss";
			case DEPOSIT_LIMITS -> limitLabel = "deposit";
			default -> {
				return;
			}
		}

		Map<Integer, Integer> limits = playResponsiblyList.stream()
			.collect(Collectors.toMap(
				AccountPlayResponsiblySetting::getPeriodType,
				playResponsiblySetting -> Integer.parseInt(playResponsiblySetting.getNewValue())
			));

		int dailyLimit = limits.getOrDefault(AccountPlayResponsiblyPeriodType.DAILY.unique(),
			SystemConstants.NO_LIMIT_SETTING);
		int weeklyLimit = limits.getOrDefault(AccountPlayResponsiblyPeriodType.WEEKLY.unique(),
			SystemConstants.NO_LIMIT_SETTING);
		int monthlyLimit = limits.getOrDefault(AccountPlayResponsiblyPeriodType.MONTHLY.unique(),
			SystemConstants.NO_LIMIT_SETTING);

		// check infinite
		if (dailyLimit == SystemConstants.NO_LIMIT_SETTING) {
			if (weeklyLimit != SystemConstants.NO_LIMIT_SETTING) {
				throw new Deviation().setI18N(
					"msg.error.account.playResponsibly.limit.weekly." + limitLabel + ".lesser");
			} else if (monthlyLimit != SystemConstants.NO_LIMIT_SETTING) {
				throw new Deviation().setI18N(
					"msg.error.account.playResponsibly.limit.monthly." + limitLabel + ".lesser");
			}
		} else if (weeklyLimit == SystemConstants.NO_LIMIT_SETTING
			&& monthlyLimit != SystemConstants.NO_LIMIT_SETTING) {
			throw new Deviation().setI18N("msg.error.account.playResponsibly.limit.monthly." + limitLabel + ".lesser");
		}

		// check dailyLimit < weeklyLimit < monthlyLimit
		if (weeklyLimit != SystemConstants.NO_LIMIT_SETTING && weeklyLimit < dailyLimit) {
			throw new Deviation().setI18N("msg.error.account.playResponsibly.limit.weekly." + limitLabel + ".lesser");
		} else if (monthlyLimit != SystemConstants.NO_LIMIT_SETTING && monthlyLimit < weeklyLimit) {
			throw new Deviation().setI18N("msg.error.account.playResponsibly.limit.monthly." + limitLabel + ".lesser");
		}
	}

	public static String formatLimitsValueWithCurrency(CurrencyType currencyType, Number limitsValue) {
		return currencyType.getCurrencySymbol() + " " +
			FormatUtils.numberFormat(limitsValue, FormatUtils.NUMBER_PATTERN_THOUSAND_SEPARATOR_NO_DECIMAL);
	}

	public static String formatLimitsValueWithHour(Number limitsValue){
		if(limitsValue.intValue() > 0 ){
			return limitsValue+ "h";
		}
		return "-";
	}

	public static boolean setLimitRuleToSeamlessWalletServer(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType,
		double limitsValue, Timestamp effectiveTime)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		String userKey = AccountUtils.getUserKey(webSiteType, userId);

		String result = switch (type) {

			case WAGER_LIMITS, LOSS_LIMITS -> SeamlessWalletApiService.getInstance().setBetRule(userKey,
				type.getBetRuleType(periodType), limitsValue, effectiveTime);

			case DEPOSIT_LIMITS -> SeamlessWalletApiService.getInstance().setPaymentRule(userKey,
				type.getPaymentRuleType(periodType), limitsValue, effectiveTime);

			default -> "";
		};

		if (result.isEmpty()) {
			return false;
		}

		JsonNode data = JSONUtils.toJsonNode(result);
		return data.get("status").asInt() == 200;
	}

	public static boolean cancelLimitRuleToSeamlessWalletServer(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		String userKey = AccountUtils.getUserKey(webSiteType, userId);

		String result = switch (type) {

			case WAGER_LIMITS, LOSS_LIMITS -> SeamlessWalletApiService.getInstance().cancelBetRule(userKey,
				type.getBetRuleType(periodType));

			case DEPOSIT_LIMITS -> SeamlessWalletApiService.getInstance().cancelPaymentRule(userKey,
				type.getPaymentRuleType(periodType));

			default -> "";
		};

		if (result.isEmpty()) {
			return false;
		}

		JsonNode data = JSONUtils.toJsonNode(result);
		return data.get("status").asInt() == 200;
	}

	public static List<AccountPlayResponsiblySetting> getPlayResponsiblyFromCacheOrDB(
		String userId, WebSiteType webSiteType, AccountPlayResponsiblyType type) {

		try {
			List<AccountPlayResponsiblySetting> list = AccountPlayResponsiblySettingCache.getInstance()
				.getPlayResponsiblyByType(webSiteType, userId, type);

			return !list.isEmpty() ?
				list :
				AccountPlayResponsiblySettingBO.findAllWithDefault(userId, webSiteType, type);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public static AccountPlayResponsiblySetting getPlayResponsiblyFromCacheOrDB(
		String userId, WebSiteType webSiteType, AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType
	) {
		try {
			AccountPlayResponsiblySetting fromCacheSetting = AccountPlayResponsiblySettingCache.getInstance()
				.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

			if (fromCacheSetting != null) {
				return fromCacheSetting;
			}

			return DbExecutor.query(conn ->
				AccountPlayResponsiblySettingDAO.findOneByUserIdAndTypeAndPeriodType(
					conn, userId, webSiteType.unique(), type.unique(), periodType.unique()
				)
			);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

	public static boolean isLimitsSet(String userId, WebSiteType webSiteType,
		AccountPlayResponsiblyType type, boolean isFromJobChecking) {

		List<AccountPlayResponsiblySetting> list = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyByType(webSiteType, userId, type);

		return isFromJobChecking ? isLimitsSetCheckFromJob(list, type) : isLimitsSetCheckFromUserOrBO(list, type);
	}

	private static boolean isLimitsSetCheckFromJob(List<AccountPlayResponsiblySetting> list,
		AccountPlayResponsiblyType type) {

		if (list.isEmpty()) {
			return false;
		}

		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> {
				yield list.stream().allMatch(playResponsibly -> {

					try {

						int currentValue = Integer.parseInt(playResponsibly.getCurrentValue());

						if (currentValue == SystemConstants.NO_LIMIT_SETTING) {

							int updateYear = playResponsibly.getUpdateTime().toLocalDateTime().getYear();
							int currentYear = DateTimeBuilder.localDateTime().toLocalDateTime().getYear();

							return updateYear == currentYear;
						}

						return currentValue >= SystemConstants.NO_LIMIT_SETTING;
					} catch (NumberFormatException e) {
						LogUtils.SYS.error(e.getMessage(), e);
						return false;
					}
				});
			}
			default -> true;
		};
	}

	private static boolean isLimitsSetCheckFromUserOrBO(List<AccountPlayResponsiblySetting> list,
		AccountPlayResponsiblyType type) {

		if (list.isEmpty()) {
			return false;
		}

		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> {
				yield list.stream().anyMatch(playResponsibly -> {

					try {

						int currentValue = Integer.parseInt(playResponsibly.getCurrentValue());

						if (currentValue == SystemConstants.NO_LIMIT_SETTING) {

							int updateYear = playResponsibly.getUpdateTime().toLocalDateTime().getYear();
							int currentYear = DateTimeBuilder.localDateTime().toLocalDateTime().getYear();

							return updateYear == currentYear;
						}

						return true;
					} catch (NumberFormatException e) {
						LogUtils.SYS.error(e.getMessage(), e);
						return false;
					}
				});
			}
			default -> true;
		};
	}

	private static PlayResponsiblyAllResponse.LimitsData buildLimitsData(
		List<AccountPlayResponsiblySetting> limits, AccountPlayResponsiblyType type) {

		Integer daily = limits.stream()
			.filter(prs -> prs.getPeriodType() == AccountPlayResponsiblyPeriodType.DAILY.unique())
			.findFirst()
			.map(prs -> Integer.parseInt(prs.getCurrentValue()))
			.orElse(type.getDefaultValue());

		Integer weekly = limits.stream()
			.filter(prs -> prs.getPeriodType() == AccountPlayResponsiblyPeriodType.WEEKLY.unique())
			.findFirst()
			.map(prs -> Integer.parseInt(prs.getCurrentValue()))
			.orElse(type.getDefaultValue());

		Integer monthly = limits.stream()
			.filter(prs -> prs.getPeriodType() == AccountPlayResponsiblyPeriodType.MONTHLY.unique())
			.findFirst()
			.map(prs -> Integer.parseInt(prs.getCurrentValue()))
			.orElse(type.getDefaultValue());

		return new PlayResponsiblyAllResponse.LimitsData(daily, weekly, monthly);
	}

	private static PlayResponsiblyDetailedResponse.PlayResponsiblyDetailData buildDetailData(
		AccountPlayResponsiblySetting setting, boolean checkMessageCondition) {

		PlayResponsiblyDetailedResponse.PlayResponsiblyDetailData data =
			new PlayResponsiblyDetailedResponse.PlayResponsiblyDetailData(
				setting.getCurrentValue(),
				setting.getStatus(),
				setting.getNewValue()
			);

		if (setting.getEffectiveTime() != null) {
			data.setEffectiveTime(setting.getEffectiveTime().getTime());
		}

		if (setting.getEffectiveEndTime() != null) {
			data.setEffectiveEndTime(setting.getEffectiveEndTime().getTime());
		}

		if (setting.getMessage() != null) {
			if (checkMessageCondition) {
				if (!Objects.equals(setting.getCurrentValue(), setting.getNewValue())) {
					data.setMessage(setting.getMessage());
				}
			} else {
				data.setMessage(setting.getMessage());
			}
		}

		return data;
	}

	private static PlayResponsiblyDetailedResponse.LimitsDetailData buildLimitsDetailData(
		List<AccountPlayResponsiblySetting> limits) {

		PlayResponsiblyDetailedResponse.LimitsDetailData limitsData =
			new PlayResponsiblyDetailedResponse.LimitsDetailData();

		for (AccountPlayResponsiblySetting setting : limits) {
			String periodName = PeriodType.getInstanceOf(setting.getPeriodType()).getName().toLowerCase();
			PlayResponsiblyDetailedResponse.PlayResponsiblyDetailData detailData = buildDetailData(setting, false);

			switch (periodName) {
				case "daily" -> limitsData.setDaily(detailData);
				case "weekly" -> limitsData.setWeekly(detailData);
				case "monthly" -> limitsData.setMonthly(detailData);
			}
		}

		return limitsData;
	}
}
