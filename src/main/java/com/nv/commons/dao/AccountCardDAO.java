package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.model.database.DBQueryRunner;

public class AccountCardDAO {

	public static List<AccountCard> findAll(Connection conn)
		throws SQLException {
		String sql = "SELECT * FROM AccountCard ";
		return DBQueryRunner.getBeanList(conn, AccountCard.class, sql);
	}

	public static List<AccountCard> findAllByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		String sql = "SELECT * FROM AccountCard WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, AccountCard.class, sql, updateTime);
	}

	public static int insert(Connection conn, AccountCard accountCard) throws SQLException {

		String sql = "INSERT INTO accountcard (id, user_id, website_type, card_no, card_scheme_type, "
			+ " bank_name, exp_month_year, cardholder_name, status, create_time, update_time) "
			+ " SELECT ACCOUNTCARD_ID_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,SYSTIMESTAMP,SYSTIMESTAMP "
			+ " FROM dual "
			+ " WHERE NOT EXISTS (SELECT 1 FROM accountcard WHERE user_id = ? AND website_type = ? AND status = 1)";

		List<Object> params = new ArrayList<>();

		params.add(accountCard.getUserId());
		params.add(accountCard.getWebsiteType());
		params.add(accountCard.getCardNo());
		params.add(accountCard.getCardSchemeType());
		params.add(accountCard.getBankName());
		params.add(accountCard.getExpMonthYear());
		params.add(accountCard.getCardholderName());
		params.add(accountCard.getStatus());

		// check not exists params
		params.add(accountCard.getUserId());
		params.add(accountCard.getWebsiteType());

		return DBQueryRunner.update(conn, sql, params);
	}

	public static List<AccountCard> findActiveListByUserId(Connection conn, String userId, int websiteType)
		throws SQLException {
		String sql = " SELECT * FROM accountcard WHERE user_id = ? AND website_type = ? AND status = 1 "
			+ " ORDER BY create_time DESC ";

		return DBQueryRunner.getBeanList(conn, AccountCard.class, sql, userId, websiteType);
	}

	public static AccountCard findById(Connection conn, long id) throws SQLException {
		String sql = " SELECT * FROM accountcard WHERE id = ? ";

		return DBQueryRunner.getBean(conn, AccountCard.class, sql, id);
	}

	public static int updateAsInactive(Connection conn, String userId, int websiteType)
		throws SQLException {
		String sql = "UPDATE accountCard SET status = ?, update_time = SYSTIMESTAMP "
			+ "WHERE user_id = ? AND website_type = ? AND status = ? ";
		return DBQueryRunner.update(conn, sql,
			BinaryStatusType.INACTIVE.unique(), userId, websiteType, BinaryStatusType.ACTIVE.unique());
	}
}
