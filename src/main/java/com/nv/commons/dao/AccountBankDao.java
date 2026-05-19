package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.nv.commons.dto.AccountBank;
import com.nv.commons.model.database.DBQueryRunner;

public class AccountBankDao {

	public static List<AccountBank> getAccountBankListByUserId(Connection conn, String userId, int websiteType,
		boolean queryAll)
		throws SQLException {

		StringBuilder sql = new StringBuilder("Select * from accountBank where user_id =? and website_type = ? ");
		List<Object> params = new ArrayList<>();
		params.add(userId);
		params.add(websiteType);
		if (!queryAll) {
			sql.append("and is_deleted=? ");
			params.add(0);
		}
		sql.append("order by create_time desc");

		return DBQueryRunner.getBeanList(conn, AccountBank.class, sql.toString(), params);
	}

	public static int addAccountBank(Connection conn, AccountBank accountBank) throws SQLException {

		String sql = "INSERT INTO accountbank (id, user_id, website_type, bank_id, bank_name, "
			+ "bank_acc_number, bank_acc_name, bank_branch, extra_data, finance_code, remark, "
			+ "verified_type, document_id, document_type, is_deleted, create_time) "
			+ "VALUES (ACCOUNTBANK_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";

		List<Object> params = new ArrayList<>();
		params.add(accountBank.getUserId());
		params.add(accountBank.getWebsiteType());
		params.add(accountBank.getBankId());
		params.add(accountBank.getBankName());
		params.add(accountBank.getBankAccNumber());
		params.add(accountBank.getBankAccName());
		params.add(accountBank.getBankBranch());
		params.add(accountBank.getExtraData());
		params.add(accountBank.getFinanceCode());
		params.add(accountBank.getRemark());
		params.add(accountBank.getVerifiedType());
		params.add(accountBank.getDocumentId());
		params.add(accountBank.getDocumentType());
		params.add(accountBank.getIsDeleted());

		return DBQueryRunner.update(conn, sql, params);
	}

	public static int manageAccountBankStatus(Connection conn, int id, int status) throws SQLException {

		String sql = "UPDATE accountbank SET is_deleted = ?, update_time = SYSTIMESTAMP "
			+ "WHERE id = ? ";

		List<Object> params = new ArrayList<>();
		params.add(status); // set is_deleted to 0 (ACTIVE)
		params.add(id); // update bankId

		return DBQueryRunner.update(conn, sql, params);
	}

	public static AccountBank getAccountBankById(Connection conn, int id) throws SQLException{
		String sql = "SELECT * FROM accountbank WHERE id = ? ";
		return DBQueryRunner.getBean(conn, AccountBank.class, sql, id);
	}
}
