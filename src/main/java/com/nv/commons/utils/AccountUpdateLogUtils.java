package com.nv.commons.utils;

import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;

public class AccountUpdateLogUtils {

	private static ThreadLocal<String> updaterForUpdateLog = new ThreadLocal<>();
	private static ThreadLocal<String> ipForUpdateLog = new ThreadLocal<>();

	public static void setInfo(String updater, String updaterIp) {

		updaterForUpdateLog.set(updater);
		ipForUpdateLog.set(updaterIp);
	}

	public static AccountUpdateLog getAccountUpdateLog(String userId, AccountUpdateType updateType, String before, String after, String message) {

		UpdateRecord updateRecord = new UpdateRecord(before, after, message);

		return getAccountUpdateLog(userId, updateType, updateRecord);
	}

	public static AccountUpdateLog getAccountUpdateLog(String userId, AccountUpdateType updateType, UpdateRecord updateRecord) {

		WebSiteType webSiteType = ThreadLocalUtils.getWebSiteType();
		CurrencyType currencyType = ThreadLocalUtils.getCurrencyType();

		return AccountUtils.getAccountUpdateLog(
			userId,
			webSiteType.unique(),
			updateType, updateRecord,
			updaterForUpdateLog.get(),
			ipForUpdateLog.get(),
			currencyType.unique());
	}
}
