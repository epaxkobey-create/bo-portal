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
import com.nv.commons.constants.RealityCheckType;
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
public class RealityCheckBO {

	private static final AccountPlayResponsiblyType type = AccountPlayResponsiblyType.REALITY_CHECK;

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateRealityCheck(String userId,
		List<AccountPlayResponsiblySetting> playResponsiblyList, String updater,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());
		LogUtils.accountPlayResponsibly.info("currencyType: {}", currencyType.unique());

		AccountPlayResponsiblySetting newRealityCheck = playResponsiblyList.getFirst();

		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newRealityCheck.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldRealityCheck = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		if (oldRealityCheck == null) {

			RealityCheckType newRealityCheckType = RealityCheckType.getInstanceOf(newRealityCheck.getNewValue());

			processInsertRealityCheck(
				userId,
				webSiteType,
				periodType,
				newRealityCheckType,
				accountUpdateLogList);
		} else {

			String takeEffectMinutesSettingValue =
				WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(), currencyType.unique(),
					WebsiteSystemSettingType.REALITY_CHECK_MODIFY_TAKE_EFFECT_MINUTES.unique()
				);
			long takeEffectMinutes = Long.parseLong(takeEffectMinutesSettingValue);

			oldRealityCheck.setUpdater(updater);

			RealityCheckType newRealityCheckType = RealityCheckType.getInstanceOf(newRealityCheck.getNewValue());

			processUpdateRealityCheck(
				userId,
				periodType,
				newRealityCheckType,
				oldRealityCheck,
				takeEffectMinutes,
				accountUpdateLogList);
		}
	}

	private static void processInsertRealityCheck(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyPeriodType periodType,
		RealityCheckType newRealityCheckType,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		AccountPlayResponsiblySetting newRealityCheck =
			AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId, webSiteType,
				type, periodType, String.valueOf(newRealityCheckType.unique()), userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newRealityCheck));

		String before = "Current Reminder: " +
						RealityCheckType.getInstanceOf(type.getDefaultValue()).getFullName();
		String after = "Current Reminder: " +
					   newRealityCheckType.getFullName();

		AccountUpdateLog accountUpdateLog = AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType),
			before, after, "New Reality Check");

		accountUpdateLogList.add(accountUpdateLog);
	}

	private static void processUpdateRealityCheck(
		String userId,
		AccountPlayResponsiblyPeriodType periodType,
		RealityCheckType newRealityCheckType,
		AccountPlayResponsiblySetting oldRealityCheck,
		long takeEffectMinutes,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		RealityCheckType currentRealityCheckType = RealityCheckType.getInstanceOf(oldRealityCheck.getCurrentValue());

		// same newValue
		if (newRealityCheckType == RealityCheckType.getInstanceOf(oldRealityCheck.getNewValue())) {
			return;
		}

		boolean hasCancel = AccountPlayResponsiblySettingBO.checkStatusAndGetCancelUpdateLog(userId, null,
			type, periodType, oldRealityCheck, accountUpdateLogList);

		updateAndGetUpdateLog(userId, periodType, takeEffectMinutes,
			currentRealityCheckType, newRealityCheckType, oldRealityCheck, accountUpdateLogList, hasCancel);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldRealityCheck));
	}

	private static void updateAndGetUpdateLog(String userId,
		AccountPlayResponsiblyPeriodType periodType, long takeEffectMinutes,
		RealityCheckType currentRealityCheckType, RealityCheckType newRealityCheckType,
		AccountPlayResponsiblySetting oldRealityCheck, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel) {

		AccountPlayResponsiblyStatusType statusType;
		Timestamp effectiveTime;
		UpdateRecord updateRecord = null;

		// new == current, become ACTIVE and take effect immediately
		if (newRealityCheckType.unique() == currentRealityCheckType.unique()) {

			statusType = AccountPlayResponsiblyStatusType.ACTIVE;
			effectiveTime = null;
			if (!hasCancel) {
				updateRecord = new UpdateRecord("Current Reminder: " + currentRealityCheckType.getFullName(),
					"Current Reminder: " + newRealityCheckType.getFullName(), "New Reality Check");
			}

			currentRealityCheckType = newRealityCheckType; // change current to new
		}
		// new > current, become PENDING and active after 1 day
		else if (newRealityCheckType.unique() > currentRealityCheckType.unique()) {

			statusType = AccountPlayResponsiblyStatusType.PENDING;

			effectiveTime = Timestamp.from(
				new Timestamp(System.currentTimeMillis()).toInstant().plus(Duration.ofMinutes(takeEffectMinutes)));

			String before = "Current Reminder: " + currentRealityCheckType.getFullName();
			String after = "Pending New Reminder: " + newRealityCheckType.getFullName() + "\n(Effective " +
						   DateUtils.toString(effectiveTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma) + ")";

			updateRecord = new UpdateRecord(before, after, "New Reality Check");
		}
		// new < current, become ACTIVE and take effect immediately
		else {

			statusType = AccountPlayResponsiblyStatusType.ACTIVE;
			effectiveTime = null;
			updateRecord = new UpdateRecord("Current Reminder: " + currentRealityCheckType.getFullName(),
				"Current Reminder: " + newRealityCheckType.getFullName(), "New Reality Check");

			currentRealityCheckType = newRealityCheckType; // change current to new
		}

		if (updateRecord != null) {
			accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, type.getAccountUpdateType(periodType), updateRecord));
		}

		oldRealityCheck.setCurrentValue(String.valueOf(currentRealityCheckType.unique()));
		oldRealityCheck.setNewValue(String.valueOf(newRealityCheckType.unique()));
		oldRealityCheck.setStatus(statusType.unique());
		oldRealityCheck.setEffectiveTime(effectiveTime);
	}
}
