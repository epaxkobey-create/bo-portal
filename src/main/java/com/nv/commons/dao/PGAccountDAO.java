package com.nv.commons.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.constants.DBQueryType;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.model.database.DBQueryRunner;

/**
 * Title: com.nv.commons.dao.PGAccountDAO<br>
 * Description: 線上支付收款帳號DAO
 *
 */
public class PGAccountDAO {

	/**
	 * 取出所有帳號
	 *
	 */
	public static List<PGAccount> findAll(Connection conn) throws SQLException {
		String sql = "SELECT * FROM pgaccount";

		return DBQueryRunner.getBeanList(conn, PGAccount.class, sql);
	}


	/**
	 * 根據ID 取出單一帳號
	 */
	public static PGAccount findAccountByID(Connection conn, int accountID, DBQueryType dbQueryType) throws SQLException {
		String sql = "SELECT * FROM pgaccount WHERE id = ?";
		if (dbQueryType != null) {
			sql += dbQueryType.getSqlString();
		}

		return DBQueryRunner.getBean(conn, PGAccount.class, sql, accountID);
	}

	public static List<PGAccount> findPGAccountByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, PGAccount.class, "SELECT * FROM pgaccount WHERE update_time > ? ORDER BY update_time", updateTime);
	}

}
