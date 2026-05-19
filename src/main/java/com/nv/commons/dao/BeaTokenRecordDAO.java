package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.constants.TokenRecordStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.module.backendapi.dto.TokenRecord;

/**
 * @author Luke Chi
 */
public class BeaTokenRecordDAO {

	/**
	 * Effective is not expired
	 */
	public static List<TokenRecord> getAllEffective(Connection conn, Timestamp activeTime) throws SQLException {

		final String sql = "SELECT * FROM BEATOKENRECORD WHERE CREATE_TIME >= ? and STATUS = ?";

		return DBQueryRunner.getBeanList(conn, TokenRecord.class, sql, activeTime, TokenRecordStatusType.ACTIVE.unique());
	}

	public static List<TokenRecord> getByUpdateTime(Connection conn, Timestamp lastUpdateTime) throws SQLException {

		final String sql = "SELECT * FROM BEATOKENRECORD WHERE UPDATE_TIME > ?";

		return DBQueryRunner.getBeanList(conn, TokenRecord.class, sql, lastUpdateTime);
	}

	public static TokenRecord getBySignature(Connection conn, String jwtSignature) throws SQLException {

		final String sql = "SELECT * FROM BEATOKENRECORD WHERE SIGNATURE = ?";

		return DBQueryRunner.getBean(conn, TokenRecord.class, sql, jwtSignature);
	}

	public static int save(Connection conn, WebSiteType webSiteType, String userId, String token) throws SQLException {

		final String sql =
			"INSERT INTO BEATOKENRECORD(SIGNATURE, TOKEN, WEBSITE_TYPE, USER_ID, STATUS) "
				+ "VALUES(?,?,?,?,?)";

		Object[] values = new Object[] {
			token.split("\\.")[2],
			token,
			webSiteType.unique(),
			userId,
			TokenRecordStatusType.ACTIVE.unique(),
		};

		return DBQueryRunner.update(conn, sql, values);
	}

	public static int updateToExpired(Connection conn, String jwtSignature) throws SQLException {

		final String sql = "UPDATE BEATOKENRECORD SET STATUS = ?, UPDATE_TIME = SYSTIMESTAMP WHERE SIGNATURE = ?";

		return DBQueryRunner.update(conn, sql, TokenRecordStatusType.EXPIRED.unique(), jwtSignature);
	}

	public static int updateActiveToInActive(Connection conn, int websiteType, String userId)
		throws SQLException {

		String sql = "UPDATE BEATOKENRECORD SET STATUS = ?, UPDATE_TIME = SYSTIMESTAMP WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND STATUS = ? ";

		Object[] values = new Object[] {
			TokenRecordStatusType.INACTIVE.unique(),
			websiteType,
			userId,
			TokenRecordStatusType.ACTIVE.unique(),
		};

		return DBQueryRunner.update(conn, sql, values);
	}

	public static int updateActiveToInActive(Connection conn, String signature)
		throws SQLException {

		String sql = "UPDATE BEATOKENRECORD SET STATUS = ?, UPDATE_TIME = SYSTIMESTAMP WHERE SIGNATURE = ? AND STATUS = ? ";

		Object[] values = new Object[] {
			TokenRecordStatusType.INACTIVE.unique(),
			signature,
			TokenRecordStatusType.ACTIVE.unique(),
		};

		return DBQueryRunner.update(conn, sql, values);
	}

	/**
	 * 刪除 三天前的 EXPIRED, INACTIVE token record
	 */
	public static int deleteExpiredAndInactiveThreeDaysAgo(Connection conn) throws SQLException {

		final Timestamp timestamp = DateTimeBuilder.localDateTime().withMaxTime()
			.minusDays(3).toTimestamp();

		final String sql = "DELETE BEATOKENRECORD WHERE (STATUS = ? or STATUS = ?) AND UPDATE_TIME <= ?";

		return DBQueryRunner
			.update(conn, sql, TokenRecordStatusType.EXPIRED.unique(), TokenRecordStatusType.INACTIVE.unique(),
				timestamp);
	}
}
