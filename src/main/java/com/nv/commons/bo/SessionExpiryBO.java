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
import com.nv.commons.constants.SessionExpiryType;
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
public class SessionExpiryBO {

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateSessionExpiry(String userId,
		AccountPlayResponsiblyType type, List<AccountPlayResponsiblySetting> playResponsiblyList, String updater,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());
		LogUtils.accountPlayResponsibly.info("currencyType: {}", currencyType.unique());

		AccountPlayResponsiblySetting newSessionExpiry = playResponsiblyList.getFirst();

		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newSessionExpiry.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldSessionExpiry = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		if (oldSessionExpiry == null) {

			processInsertSessionExpiry(
				userId,
				webSiteType,
				type,
				periodType,
				newSessionExpiry.getNewValue(),
				accountUpdateLogList);
		} else {

			String takeEffectMinutesSettingValue =
				WebsiteSystemSettingCache.getInstance().getValueByKey(webSiteType.unique(), currencyType.unique(),
					WebsiteSystemSettingType.SESSION_EXPIRY_MODIFY_TAKE_EFFECT_MINUTES.unique()
				);
			long takeEffectMinutes = Long.parseLong(takeEffectMinutesSettingValue);

			oldSessionExpiry.setUpdater(updater);

			processUpdateSessionExpiry(
				userId,
				currencyType,
				type,
				periodType,
				newSessionExpiry.getNewValue(),
				oldSessionExpiry,
				takeEffectMinutes,
				accountUpdateLogList);
		}
	}

	private static void processInsertSessionExpiry(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newValue,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		SessionExpiryType newSessionExpiryType = SessionExpiryType.getInstanceOf(newValue);

		if (newSessionExpiryType.unique() == SessionExpiryType.NONE.unique()) {
			return;
		}

		AccountPlayResponsiblySetting newSessionExpiry = AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId,
			webSiteType, type, periodType, String.valueOf(newSessionExpiryType.unique()), userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newSessionExpiry));

		String before = "Current Session: " + SessionExpiryType.NONE.getFullName();
		String after = "Current Session: " +
					   SessionExpiryType.getInstanceOf(newSessionExpiry.getNewValue()).getFullName();

		accountUpdateLogList.add(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType),
			before, after, "New Session Expiry"));
	}

	// 方法太長
	private static void processUpdateSessionExpiry(
		String userId,
		CurrencyType currencyType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newValue,
		AccountPlayResponsiblySetting oldSessionExpiry,
		long takeEffectMinutes,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		SessionExpiryType newSessionExpiryType = SessionExpiryType.getInstanceOf(newValue);
		SessionExpiryType currentSessionExpiryType = SessionExpiryType.getInstanceOf(oldSessionExpiry.getCurrentValue());

		// same newValue
		if (newSessionExpiryType.unique() == SessionExpiryType.getInstanceOf(oldSessionExpiry.getNewValue()).unique()) {
			return;
		}

		boolean hasCancel = AccountPlayResponsiblySettingBO.checkStatusAndGetCancelUpdateLog(userId, currencyType, type, periodType, oldSessionExpiry,
			accountUpdateLogList);

		updateAndGetUpdateLog(userId, type, periodType, takeEffectMinutes,
			currentSessionExpiryType, newSessionExpiryType, oldSessionExpiry, accountUpdateLogList, hasCancel);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldSessionExpiry));
	}

	private static void updateAndGetUpdateLog(String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType, long takeEffectMinutes,
		SessionExpiryType currentSessionExpiryType, SessionExpiryType newSessionExpiryType,
		AccountPlayResponsiblySetting oldSessionExpiry, LinkedList<AccountUpdateLog> accountUpdateLogList,
		boolean hasCancel) {

		AccountPlayResponsiblyStatusType statusType;
		Timestamp effectiveTime;
		UpdateRecord updateRecord = null;

		// current == 0 || new == current, become ACTIVE and take effect immediately
		if (currentSessionExpiryType.unique() == SessionExpiryType.NONE.unique()
			|| newSessionExpiryType.unique() == currentSessionExpiryType.unique()) {

			statusType = AccountPlayResponsiblyStatusType.ACTIVE;
			effectiveTime = null;
			if (!hasCancel) {
				updateRecord = new UpdateRecord("Current Session: " + currentSessionExpiryType.getFullName(),
					"Current Session: " + newSessionExpiryType.getFullName(), "New Session Expiry");
			}

			currentSessionExpiryType = newSessionExpiryType; // change current to new
		}
		// new == 0 || new > current, become PENDING and active after 7 days
		else if (newSessionExpiryType.unique() == SessionExpiryType.NONE.unique()
				 || newSessionExpiryType.unique() > currentSessionExpiryType.unique()) {

			statusType = AccountPlayResponsiblyStatusType.PENDING;

			effectiveTime = Timestamp.from(
				new Timestamp(System.currentTimeMillis()).toInstant().plus(Duration.ofMinutes(takeEffectMinutes)));

			String newOrRevoke = newSessionExpiryType.unique() > currentSessionExpiryType.unique() ? "New" : "Revoke";

			String before = "Current Session: " + currentSessionExpiryType.getFullName();

			String after = "Pending " + newOrRevoke + " Session" +
						   (newOrRevoke.equals("New") ? ": " + newSessionExpiryType.getFullName() : "") + "\n(Effective " +
						   DateUtils.toString(effectiveTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma) + ")";

			updateRecord = new UpdateRecord(before, after, newOrRevoke + " Session Expiry");
		}
		// new < current, become ACTIVE and take effect immediately
		else {

			statusType = AccountPlayResponsiblyStatusType.ACTIVE;
			effectiveTime = null;
			updateRecord = new UpdateRecord("Current Session: " + currentSessionExpiryType.getFullName(),
				"Current Session: " + newSessionExpiryType.getFullName(), "Decrease Session Expiry");

			currentSessionExpiryType = newSessionExpiryType; // change current to new
		}

		if (updateRecord != null) {
			accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
				userId, type.getAccountUpdateType(periodType), updateRecord));
		}

		oldSessionExpiry.setCurrentValue(String.valueOf(currentSessionExpiryType.unique()));
		oldSessionExpiry.setNewValue(String.valueOf(newSessionExpiryType.unique()));
		oldSessionExpiry.setStatus(statusType.unique());
		oldSessionExpiry.setEffectiveTime(effectiveTime);
	}
}
