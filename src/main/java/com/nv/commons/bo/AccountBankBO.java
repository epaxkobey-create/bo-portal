package com.nv.commons.bo;

import java.util.List;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.dao.AccountBankDao;
import com.nv.commons.dto.AccountBank;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.utils.AccountUpdateLogUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;

public class AccountBankBO {

	public static List<AccountBank> getAccountBankByUserId(String userId, int webSiteType, boolean queryAll)
		throws Exception {
		return DbExecutor.query(conn ->
			AccountBankDao.getAccountBankListByUserId(conn, userId, webSiteType, queryAll)
		);
	}

	public static AccountBank getAccountBankById(int id) throws Exception {
		return DbExecutor.query(conn -> AccountBankDao.getAccountBankById(conn, id));
	}

	public static int addNewAccountBank(String userId, String bankAccountNumber,
		int bankId, int webSiteType, String bankName) throws Exception {

		return DbExecutor.update(conn -> {

			AccountBank accountBank = new AccountBank(
				userId,                    // userId
				webSiteType,      // websiteType
				bankId,                    // bankId
				bankName,                 // bankName
				bankAccountNumber,         // bankAccNumber
				userId,          // bankAccName
				null,                      // bankBranch
				0,                         // id (will be auto-generated)
				null,                      // extraData
				null,                      // financeCode
				null,                      // remark
				0,                         // verifiedType (0 = unverified)
				-1,                        // documentId (default)
				null,                      // updateTime
				null,                      // createTime
				-1,                        // documentType (default)
				0                          // isDeleted (0 = false) (1 = true)
			);

			int result = AccountBankDao.addAccountBank(conn, accountBank);

			if (result != 1) {
				throw new Exception("Failed to add account bank.");
			}

			return result;

		});
	}

	public static int manageAccountBankStatus(
		int id, int status) throws Exception {

		return DbExecutor.update(conn -> {
			int result = AccountBankDao.manageAccountBankStatus(conn, id, status);

			if (result != 1) {
				throw new Exception("Failed to manage account bank status.");
			}

			return result;
		});
	}

	public static void addAccountUpdateLog(String userId, String beforeUpdate,
		String afterUpdate, String updater, String updaterIp) {
		AccountUpdateLogUtils.setInfo(updater, updaterIp);

		AccountUpdateLog accountUpdateLog = AccountUpdateLogUtils.getAccountUpdateLog(
			userId, AccountUpdateType.BANK,
			beforeUpdate, afterUpdate, "Update Bank Info");

		AccountUpdateLogBO.insert(accountUpdateLog);
	}


	public static String getAvailableAccountCard(String userId, int webSiteType)
		throws Exception {

		List<AccountBank> accountBanks = getAccountBankByUserId(userId, webSiteType, false);

		JsonGenerateProcessor generateProcessor = (jGenerator -> {
			jGenerator.writeArrayFieldStart("data");
			for (AccountBank accountBank : accountBanks) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("bankName", accountBank.getBankName());
				jGenerator.writeStringField("bankAccountNumber", accountBank.getBankAccNumber());
				jGenerator.writeNumberField("value", accountBank.getId());
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
		});

		return JSONUtils.getJSONString(generateProcessor);

	}

}
