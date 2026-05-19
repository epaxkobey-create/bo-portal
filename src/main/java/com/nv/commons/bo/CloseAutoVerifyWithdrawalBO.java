package com.nv.commons.bo;

import java.sql.Connection;

import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CloseAutoVerifyWithdrawalType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class CloseAutoVerifyWithdrawalBO {

	public static int verifyPlayerWithdrawalStatus(int webSiteTypeId, int currencyTypeId,
		String userId, String message, CloseAutoVerifyWithdrawalType type) {
		Connection conn = null;
		int count = 0;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			if (checkWebsiteSystemSetting(webSiteTypeId, currencyTypeId, type)) {
				count = AccountDAO.updateCloseAutoVerification(conn, webSiteTypeId, userId);
			}
			conn.commit();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}

		if (count > 0) {
			saveUpdateLog(userId, webSiteTypeId, currencyTypeId, message);
		}
		return count;
	}

	private static boolean checkWebsiteSystemSetting(int websiteTypeId, int currencyTypeId,
		CloseAutoVerifyWithdrawalType type) {

		String settingValue = WebsiteSystemSettingCache.getInstance()
			.getValueByKey(websiteTypeId, currencyTypeId, WebsiteSystemSettingType.CLOSE_AUTO_VERIFY_WITHDRAWAL
				.unique());

		if (settingValue == null) {
			return false;
		}

		int settingValueInt = Integer.parseInt(settingValue);

		return (settingValueInt & type.unique()) == type.unique();
	}

	private static void saveUpdateLog(String userId, int websiteTypeId, int currencyTypeId,
		String message){

		UpdateRecord updateRecord = new UpdateRecord(String.valueOf(BinaryStatusType.ACTIVE.unique()),
			String.valueOf(BinaryStatusType.INACTIVE.unique()),
			"Change Auto Verify Withdrawal NO to YES By " + message);

		AccountUpdateLog updateLog = AccountUtils
			.getAccountUpdateLog(userId, websiteTypeId, AccountUpdateType.AUTO_VERIFICATION,
				updateRecord, "SYS", "0.0.0.0", currencyTypeId);

		AccountUpdateLogBO.insert(updateLog);
	}
}
