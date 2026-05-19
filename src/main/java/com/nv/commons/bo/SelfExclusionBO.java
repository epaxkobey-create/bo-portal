package com.nv.commons.bo;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.SelfExclusionType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;
import com.nv.module.backendapi.cache.PlayerLocalCache;

/**
 * @author Ken
 */
public class SelfExclusionBO {

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateSelfExclusion(String userId,
		AccountPlayResponsiblyType type, List<AccountPlayResponsiblySetting> playResponsiblyList, String updater,
		LinkedList<AccountUpdateLog> accountUpdateLogList, boolean isBOAdmin)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());
		LogUtils.accountPlayResponsibly.info("currencyType: {}", currencyType.unique());

		AccountPlayResponsiblySetting newSelfExclusion = playResponsiblyList.getFirst();

		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newSelfExclusion.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldSelfExclusion = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		if (oldSelfExclusion == null) {

			processInsertSelfExclusion(
				userId,
				webSiteType,
				type,
				periodType,
				newSelfExclusion.getNewValue(),
				accountUpdateLogList);
		} else {

			String takeEffectMinutesSettingValue =
				WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(), currencyType.unique(),
					SelfExclusionType.getInstanceOf(oldSelfExclusion.getCurrentValue()).unique()
					== SelfExclusionType.INDEFINITE.unique() ?
						WebsiteSystemSettingType.SELF_EXCLUSION_MODIFY_TAKE_EFFECT_MINUTES_FROM_INDEFINITE.unique() :
						WebsiteSystemSettingType.SELF_EXCLUSION_MODIFY_TAKE_EFFECT_MINUTES_FROM_DEFINITE.unique()
				);
			long takeEffectMinutes = Long.parseLong(takeEffectMinutesSettingValue);

			oldSelfExclusion.setUpdater(updater);

			processUpdateSelfExclusion(
				userId,
				webSiteType,
				currencyType,
				type,
				periodType,
				newSelfExclusion.getNewValue(),
				oldSelfExclusion,
				takeEffectMinutes,
				accountUpdateLogList,
				isBOAdmin);
		}
	}

	private static void processInsertSelfExclusion(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newValue,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		SelfExclusionType newSelfExclusionType = SelfExclusionType.getInstanceOf(newValue);

		if (newSelfExclusionType.unique() == SelfExclusionType.NO_EXCLUSION.unique()) {
			return;
		}

		AccountPlayResponsiblySetting newSelfExclusion =
			AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId, webSiteType,
				type, periodType, String.valueOf(newSelfExclusionType.unique()), userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newSelfExclusion));

		String before = "Current Exclusion: " + SelfExclusionType.NO_EXCLUSION.getFullName();
		String after = "Current Exclusion: " +
					   SelfExclusionType.getInstanceOf(newSelfExclusion.getNewValue()).getFullName();

		accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType),
			before, after, "New Self Exclusion"));

		// logout player
		logoutPlayer(userId, webSiteType);
	}

	private static void processUpdateSelfExclusion(
		String userId,
		WebSiteType webSiteType,
		CurrencyType currencyType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newValue,
		AccountPlayResponsiblySetting oldSelfExclusion,
		long takeEffectMinutes,
		LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean isBOAdmin)
		throws Exception {

		SelfExclusionType newSelfExclusionType = SelfExclusionType.getInstanceOf(newValue);
		SelfExclusionType currentSelfExclusionType = SelfExclusionType.getInstanceOf(oldSelfExclusion.getCurrentValue());

		// same newValue
		if (newSelfExclusionType.unique() == SelfExclusionType.getInstanceOf(oldSelfExclusion.getNewValue()).unique()) {
			return;
		}

		boolean hasCancel = AccountPlayResponsiblySettingBO.checkStatusAndGetCancelUpdateLog(userId, currencyType, type, periodType, oldSelfExclusion,
			accountUpdateLogList);

		boolean needLogoutPlayer;

		if (isBOAdmin) {
			needLogoutPlayer = updateAndGetUpdateLogForBOAdmin(userId, type, periodType, takeEffectMinutes,
				currentSelfExclusionType, newSelfExclusionType, oldSelfExclusion, accountUpdateLogList, hasCancel);
		} else {
			needLogoutPlayer = updateAndGetUpdateLogForPlayer(userId, type, periodType,
				currentSelfExclusionType, newSelfExclusionType, oldSelfExclusion, accountUpdateLogList, hasCancel);
		}

		// logout player
		if (needLogoutPlayer) {
			logoutPlayer(userId, webSiteType);
		}

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldSelfExclusion));
	}

	/**
	 * Player-side self-exclusion: ALL changes take effect immediately (no cooling period).
	 * Per requirement: "Self-Exclusion on the player side has no cooling period -- it takes effect immediately"
	 */
	private static boolean updateAndGetUpdateLogForPlayer(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType,
		SelfExclusionType currentSelfExclusionType, SelfExclusionType newSelfExclusionType,
		AccountPlayResponsiblySetting oldSelfExclusion, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel) {

		String newOrRevoke = (newSelfExclusionType == SelfExclusionType.NO_EXCLUSION) ? "Revoke" : "New";
		String before = "Current Exclusion: " + currentSelfExclusionType.getFullName();
		String after = "Current Exclusion: " + newSelfExclusionType.getFullName();

		if (!hasCancel) {
			accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, type.getAccountUpdateType(periodType),
				before, after, newOrRevoke + " Self Exclusion"));
		}

		oldSelfExclusion.setCurrentValue(String.valueOf(newSelfExclusionType.unique()));
		oldSelfExclusion.setNewValue(String.valueOf(newSelfExclusionType.unique()));
		oldSelfExclusion.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		oldSelfExclusion.setEffectiveTime(null);
		oldSelfExclusion.setEffectiveEndTime();

		return newSelfExclusionType != SelfExclusionType.NO_EXCLUSION;
	}

	private static boolean updateAndGetUpdateLogForBOAdmin(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType, long takeEffectMinutes,
		SelfExclusionType currentSelfExclusionType, SelfExclusionType newSelfExclusionType,
		AccountPlayResponsiblySetting oldSelfExclusion, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel) {

		boolean needLogoutPlayer;

		AccountPlayResponsiblyStatusType statusType;
		Timestamp effectiveTime;
		UpdateRecord updateRecord = null;

		boolean isTakeEffectImmediately = isTakeEffectImmediately(currentSelfExclusionType, newSelfExclusionType);

		if (isTakeEffectImmediately) {

			statusType = AccountPlayResponsiblyStatusType.ACTIVE;
			effectiveTime = null;
			if (!hasCancel) {
				updateRecord = new UpdateRecord("Current Exclusion: " + currentSelfExclusionType.getFullName(),
					"Current Exclusion: " + newSelfExclusionType.getFullName(), "New Self Exclusion");
			}

			currentSelfExclusionType = newSelfExclusionType; // change current to new

			needLogoutPlayer = true;
		} else {

			statusType = AccountPlayResponsiblyStatusType.PENDING;

			effectiveTime = Timestamp.from(
				new Timestamp(System.currentTimeMillis()).toInstant().plus(Duration.ofMinutes(takeEffectMinutes)));

			String newOrRevoke =
				(newSelfExclusionType == SelfExclusionType.NO_EXCLUSION)
					? "Revoke" : "New";

			String before = "Current Exclusion: " + currentSelfExclusionType.getFullName();
			String after = "Pending " + newOrRevoke + " Exclusion" +
						   (newOrRevoke.equals("New") ? ": " + newSelfExclusionType.getFullName() : "") + "\n(Effective " +
						   DateUtils.toString(effectiveTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma) + ")";

			updateRecord = new UpdateRecord(before, after, newOrRevoke + " Self Exclusion");

			needLogoutPlayer = false;
		}

		if (updateRecord != null) {
			accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, type.getAccountUpdateType(periodType), updateRecord));
		}

		oldSelfExclusion.setCurrentValue(String.valueOf(currentSelfExclusionType.unique()));
		oldSelfExclusion.setNewValue(String.valueOf(newSelfExclusionType.unique()));
		oldSelfExclusion.setStatus(statusType.unique());
		oldSelfExclusion.setEffectiveTime(effectiveTime);
		oldSelfExclusion.setEffectiveEndTime();

		return needLogoutPlayer;
	}

	private static boolean isTakeEffectImmediately(SelfExclusionType currentSelfExclusionType,
		SelfExclusionType newSelfExclusionType) {

		// current != -1, modify from definite
		if (currentSelfExclusionType.unique() != SelfExclusionType.INDEFINITE.unique()) {
			// current == 0 || new == -1 || new >= current, become ACTIVE and take effect immediately
			// current == (7-365), become PENDING and take effect after 1 day
			return currentSelfExclusionType.unique() == SelfExclusionType.NO_EXCLUSION.unique()
				   || newSelfExclusionType.unique() == SelfExclusionType.INDEFINITE.unique()
				   || newSelfExclusionType.unique() >= currentSelfExclusionType.unique();
		}
		// current == -1, modify from indefinite
		else {
			// new == -1, become ACTIVE and take effect immediately
			// new != -1, become PENDING and take effect after cooling period (24h per requirement)
			return newSelfExclusionType.unique() == SelfExclusionType.INDEFINITE.unique();
		}
	}

	private static void logoutPlayer(String userId, WebSiteType webSiteType) {

		final String userKey = AccountUtils.getUserKey(webSiteType, userId);
		Account playerInCache = PlayerLocalCache.getInstance().get(userKey);
		if (playerInCache != null) {
			PlayerBO.logoutBea(playerInCache);
		}
	}
}
