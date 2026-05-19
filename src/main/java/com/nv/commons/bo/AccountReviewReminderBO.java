package com.nv.commons.bo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountReviewReminderType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;

/**
 * @author Ken
 */
public class AccountReviewReminderBO {

	private static final AccountPlayResponsiblyType type = AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER;

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateAccountReviewReminder(String userId,
		List<AccountPlayResponsiblySetting> playResponsiblyList, String updater,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());

		AccountPlayResponsiblySetting newReminder = playResponsiblyList.getFirst();

		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newReminder.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldReminder = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		if (oldReminder == null) {

			AccountReviewReminderType newReminderType = AccountReviewReminderType.getInstanceOf(newReminder.getNewValue());

			processInsertAccountReviewReminder(
				userId,
				webSiteType,
				periodType,
				newReminderType,
				accountUpdateLogList);
		} else {

			oldReminder.setUpdater(updater);

			AccountReviewReminderType newReminderType = AccountReviewReminderType.getInstanceOf(newReminder.getNewValue());

			processUpdateAccountReviewReminder(
				userId,
				periodType,
				newReminderType,
				oldReminder,
				accountUpdateLogList);
		}
	}

	private static void processInsertAccountReviewReminder(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyPeriodType periodType,
		AccountReviewReminderType newReminderType,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		AccountPlayResponsiblySetting newReminder =
			AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId, webSiteType,
				type, periodType, String.valueOf(newReminderType.unique()), userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newReminder));

		UpdateRecord updateRecord = new UpdateRecord(
			"Current Reminder: " + AccountReviewReminderType.getInstanceOf(type.getDefaultValue()).getFullName(),
			"Current Reminder: " + newReminderType.getFullName(),
			"New Account Review Reminder");

		accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType), updateRecord));
	}

	private static void processUpdateAccountReviewReminder(
		String userId,
		AccountPlayResponsiblyPeriodType periodType,
		AccountReviewReminderType newReminderType,
		AccountPlayResponsiblySetting oldReminder,
		LinkedList<AccountUpdateLog> accountUpdateLogList)
		throws Exception {

		AccountReviewReminderType currentReminderType = AccountReviewReminderType.getInstanceOf(oldReminder.getCurrentValue());

		// same newValue
		if (newReminderType == AccountReviewReminderType.getInstanceOf(oldReminder.getNewValue())) {
			return;
		}

		updateAndGetUpdateLog(userId, periodType,
			currentReminderType, newReminderType, oldReminder, accountUpdateLogList);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldReminder));
	}

	private static void updateAndGetUpdateLog(String userId,
		AccountPlayResponsiblyPeriodType periodType,
		AccountReviewReminderType currentReminderType, AccountReviewReminderType newReminderType,
		AccountPlayResponsiblySetting oldReminder, LinkedList<AccountUpdateLog> accountUpdateLogList) {

		UpdateRecord updateRecord = new UpdateRecord(
			"Current Reminder: " + currentReminderType.getFullName(),
			"Current Reminder: " + newReminderType.getFullName(),
			"New Account Review Reminder");

		accountUpdateLogList.addLast(AccountUpdateLogUtils.getAccountUpdateLog(
			userId, type.getAccountUpdateType(periodType), updateRecord));

		oldReminder.setCurrentValue(String.valueOf(newReminderType.unique()));
		oldReminder.setNewValue(String.valueOf(newReminderType.unique()));
		oldReminder.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		oldReminder.setEffectiveTime(getUpdatedEffectiveTime(newReminderType.unique()));
	}

	public static void extendEffectiveTime(String userId, WebSiteType webSiteType) {
		try {
			DbExecutor.update(conn ->
				AccountPlayResponsiblySettingDAO.extendAccountReviewReminderEffectiveTime(conn,
					userId, webSiteType.unique(), userId));

			AccountPlayResponsiblySettingCache.getInstance().update();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	private static Timestamp getUpdatedEffectiveTime(int months) {
		// this will auto handle month-end 29/30/31
		return Timestamp.valueOf(LocalDateTime.now().plusMonths(months));
	}
}
