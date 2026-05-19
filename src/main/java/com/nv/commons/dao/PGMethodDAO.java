package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.PGMethod;
import com.nv.commons.model.database.DBQueryRunner;

/**
 * Title: com.nv.commons.dao.PGMethodDAO<br>
 * Description: 線上支付公司對應銀行DAO
 *
 */
public class PGMethodDAO {

	/**
	 * 取出所有對應
	 *
	 */
	public static List<PGMethod> findAll(Connection conn) throws SQLException {
		String sql = "SELECT * FROM pgmethod";

		return DBQueryRunner.getBeanList(conn, PGMethod.class, sql);
	}
	

	public static List<PGMethod> findPGMethodByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, PGMethod.class, "SELECT * FROM pgmethod WHERE update_time > ? ORDER BY update_time", updateTime);
	}


}
