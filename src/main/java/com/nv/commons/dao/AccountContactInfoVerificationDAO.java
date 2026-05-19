package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.dto.AccountContactInfoVerification;
import com.nv.commons.model.database.DBQueryRunner;

public class AccountContactInfoVerificationDAO {

	public static List<AccountContactInfoVerification> findAll(Connection conn)
		throws SQLException {
		String sql = "SELECT * FROM AccountContactInfoVerification ";
		return DBQueryRunner.getBeanList(conn, AccountContactInfoVerification.class, sql);
	}

	public static List<AccountContactInfoVerification> findAllByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		String sql = "SELECT * FROM AccountContactInfoVerification WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, AccountContactInfoVerification.class, sql, updateTime);
	}

	public static int insert(Connection conn, AccountContactInfoVerification contactInfoVerification)
		throws SQLException {

		String sql = """
			INSERT INTO accountContactInfoVerification(user_id, website_type, contact_type, content,
			verify_code, valid_from, valid_to, is_verified, creator, create_time, updater, update_time)
			VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, SYSTIMESTAMP)
			""";

		return DBQueryRunner.update(conn, sql,
			contactInfoVerification.getUserId(),
			contactInfoVerification.getWebsiteType(),
			contactInfoVerification.getContactType(),
			contactInfoVerification.getContent(),

			contactInfoVerification.getVerifyCode(),
			contactInfoVerification.getValidFrom(),
			contactInfoVerification.getValidTo(),
			contactInfoVerification.getIsVerified(),

			contactInfoVerification.getCreator(),
			contactInfoVerification.getUpdater()
		);
	}

	public static int update(Connection conn, AccountContactInfoVerification contactInfoVerification)
		throws SQLException {

		String sql = """
			UPDATE accountContactInfoVerification SET is_verified = ?, updater = ?, update_time = SYSTIMESTAMP
			WHERE user_id = ? AND website_type = ? AND contact_type = ? AND content = ?
			""";

		List<Object> params = new ArrayList<>();
		params.add(contactInfoVerification.getIsVerified());
		params.add(contactInfoVerification.getUpdater());
		params.add(contactInfoVerification.getUserId());
		params.add(contactInfoVerification.getWebsiteType());
		params.add(contactInfoVerification.getContactType());
		params.add(contactInfoVerification.getContent());

		return DBQueryRunner.update(conn, sql, params);
	}

	public static int updateAsVerified(Connection conn, AccountContactInfoVerification contactInfoVerification)
		throws SQLException {

		String sql = """
			UPDATE accountContactInfoVerification SET is_verified = ?, updater = ?, update_time = SYSTIMESTAMP
			WHERE user_id = ? AND website_type = ? AND contact_type = ? AND content = ?
			""";

		List<Object> params = new ArrayList<>();
		params.add(BinaryStatusType.ACTIVE.unique());
		params.add(contactInfoVerification.getUpdater());
		params.add(contactInfoVerification.getUserId());
		params.add(contactInfoVerification.getWebsiteType());
		params.add(contactInfoVerification.getContactType());
		params.add(contactInfoVerification.getContent());

		return DBQueryRunner.update(conn, sql, params);
	}

	public static AccountContactInfoVerification findLatest(Connection conn, String userId,
		int webSiteType, int contactType, String content) throws SQLException {
		return DBQueryRunner.getBean(conn, AccountContactInfoVerification.class,
			"""
				SELECT * FROM (
					SELECT * FROM accountContactInfoVerification
					WHERE user_id = ? AND website_type = ? AND contact_type = ? AND content = ?
					ORDER BY create_time DESC
				) WHERE ROWNUM = 1
				""",
			userId, webSiteType, contactType, content);
	}

	public static AccountContactInfoVerification findLatest(Connection conn, String verifyCode) throws SQLException {
		return DBQueryRunner.getBean(conn, AccountContactInfoVerification.class,
			"""
				SELECT * FROM (
					SELECT * FROM accountContactInfoVerification
					WHERE verify_code = ?
					ORDER BY create_time DESC
				) WHERE ROWNUM = 1
				""",
			verifyCode);
	}
}
