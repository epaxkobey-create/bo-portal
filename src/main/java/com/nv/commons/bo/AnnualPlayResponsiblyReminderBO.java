package com.nv.commons.bo;

import java.util.List;
import java.util.Optional;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ThreadLocalUtils;

/**
 * @author Ken
 */
public class AnnualPlayResponsiblyReminderBO {

	/**
	 * 1. insert new
	 * or
	 * 2. update old
	 */
	public static void updateAnnualReminder(String userId,
		AccountPlayResponsiblyType type, List<AccountPlayResponsiblySetting> playResponsiblyList, String updater)
		throws Exception {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		LogUtils.accountPlayResponsibly.info("webSiteType: {}", webSiteType.unique());
		LogUtils.accountPlayResponsibly.info("currencyType: {}", currencyType.unique());

		AccountPlayResponsiblySetting newAnnualReminder = playResponsiblyList.getFirst();

		AccountPlayResponsiblyPeriodType periodType = Optional.ofNullable(
				AccountPlayResponsiblyPeriodType.getInstanceOf(newAnnualReminder.getPeriodType()))
			// 默认为Daily
			.orElse(AccountPlayResponsiblyPeriodType.DAILY);

		AccountPlayResponsiblySetting oldAnnualReminder = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrNull(webSiteType, userId, type, periodType);

		if (oldAnnualReminder == null) {

			String newAnnualReminderValue = newAnnualReminder.getNewValue();

			processInsertAnnualReminder(
				userId,
				webSiteType,
				type,
				periodType,
				newAnnualReminderValue);
		} else {

			oldAnnualReminder.setUpdater(updater);

			String newAnnualReminderValue = newAnnualReminder.getNewValue();

			processUpdateAnnualReminder(
				newAnnualReminderValue,
				oldAnnualReminder);
		}
	}

	private static void processInsertAnnualReminder(
		String userId,
		WebSiteType webSiteType,
		AccountPlayResponsiblyType type,
		AccountPlayResponsiblyPeriodType periodType,
		String newAnnualReminderValue)
		throws Exception {

		AccountPlayResponsiblySetting newAnnualReminder =
			AccountPlayResponsiblySettingBO.generateNewPlayResponsibly(userId, webSiteType,
				type, periodType, newAnnualReminderValue, userId);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.insert(conn, newAnnualReminder));
	}

	private static void processUpdateAnnualReminder(
		String newAnnualReminderValue,
		AccountPlayResponsiblySetting oldAnnualReminder)
		throws Exception {

		oldAnnualReminder.setCurrentValue(newAnnualReminderValue);
		oldAnnualReminder.setNewValue(newAnnualReminderValue);
		oldAnnualReminder.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		oldAnnualReminder.setEffectiveTime(null);

		DbExecutor.update(conn ->
			AccountPlayResponsiblySettingDAO.update(conn, oldAnnualReminder));
	}

	public static void resetAnnualReminderToFalse() {
		try {
			DbExecutor.update(AccountPlayResponsiblySettingDAO::resetAnnualReminderToFalse);

			AccountPlayResponsiblySettingCache.getInstance().update();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}
}
