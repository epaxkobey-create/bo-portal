package com.nv.commons.bo;

import java.util.Optional;

import com.nv.commons.constants.AccountRemarkType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountRemarkDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountRemark;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class AccountRemarkBO {

	public static int insert(int webSiteType, String userId, String updaterIp,
		AccountRemark accountRemark) throws Exception {

		AccountRemarkType dataType = AccountRemarkType.getInstance(accountRemark.getRemarkType());
		if (dataType == null) {
			return 0;
		}

		return DbExecutor.update(conn -> {

			if (AccountRemarkDAO.insert(conn, accountRemark) > 0) {

				Account account = AccountBO.getAccountByUserId(
					userId, WebSiteType.getInstance(webSiteType));

				AccountUpdateLog updateLog =
					buildAccountUpdateLog(webSiteType, userId, updaterIp, accountRemark,  AccountUpdateType.USER_REMARK, account, null);

				AccountUpdateLogBO.insert(updateLog);

				return 1;
			}
			return 0;
		});
	}

	public static int insertOrUpdate(int webSiteType, String userId, String updaterIp,
		AccountRemark accountRemark) throws Exception {

		AccountRemarkType dataType = AccountRemarkType.getInstance(accountRemark.getRemarkType());
		if (dataType == null) {
			return 0;
		}

		AccountUpdateType updateType = AccountUpdateType.USER_REMARK;


		return DbExecutor.update(conn -> {

			int count = 0;

			Optional<AccountRemark> accountRemarkInDB = AccountRemarkDAO
				.getByRemarkType(conn, webSiteType, userId, dataType.unique())
				.stream()
				//				.filter(dataInDB -> dataInDB.getRemarkType() == dataType.unique())
				.findFirst();

			if (accountRemarkInDB.isEmpty()) {

				count = AccountRemarkDAO.insert(conn, accountRemark);

			} else {

				if (StringUtils.isEmpty(accountRemarkInDB.get().getRemark()) && StringUtils.isEmpty(
					accountRemark.getRemark())) {
					return 0;
				}

				boolean hadModify =
					(accountRemarkInDB.get().getRemark() != null && !accountRemarkInDB.get().getRemark()
						.equals(accountRemark.getRemark()))
						|| (accountRemark.getRemark() != null && !accountRemark.getRemark()
						.equals(accountRemarkInDB.get().getRemark()));

				if (!hadModify) {
					return 0;
				}

				count = AccountRemarkDAO.update(conn, accountRemark);
			}

			if (count > 0) {

				Account account = AccountBO.getAccountByUserId(
					userId, WebSiteType.getInstance(webSiteType));

				AccountUpdateLog updateLog = accountRemarkInDB.map(
						remark -> buildAccountUpdateLog(webSiteType, userId, updaterIp, accountRemark,
							updateType, account, remark))
					.orElseGet(() -> buildAccountUpdateLog(webSiteType, userId, updaterIp, accountRemark,
						updateType, account, null));

				AccountUpdateLogBO.insert(updateLog);

				return 1;
			}

			return 0;
		});
	}

	@NotNull
	private static AccountUpdateLog buildAccountUpdateLog(int webSiteType, String userId, String updaterIp,
		AccountRemark accountRemark,
		AccountUpdateType updateType, Account account, AccountRemark accountRemarkInDB) {

		AccountUpdateLog updateLog = new AccountUpdateLog();
		updateLog.setUserId(userId);
		updateLog.setWebsiteType(webSiteType);
		updateLog.setUpdater(accountRemark.getUpdater());
		//				updateLog.setUpdateTime(now);
		updateLog.setUpdaterIp(updaterIp);
		updateLog.setLogType(updateType.unique());
		updateLog.setLogTypeStr(updateType.getName());

		if (accountRemarkInDB != null && accountRemarkInDB.getRemark() != null) {
			updateLog.setRecords(
				JSONUtils.toJsonString(
					new UpdateRecord(accountRemarkInDB.getRemark(), accountRemark.getRemark(), "")));
		} else {
			updateLog.setRecords(
				JSONUtils.toJsonString(
					new UpdateRecord("", accountRemark.getRemark(), "")));
		}

		updateLog.setCurrencyTypeId(account.getCurrencyTypeId());
		return updateLog;
	}
}
