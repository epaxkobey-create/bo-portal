package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.PGCompany;
import com.nv.commons.model.database.DBQueryRunner;

/**
 * Title: com.nv.commons.dao.PGCompanyDAO<br>
 * Description: 線上支付公司DAO
 *
 * @author: Daniel.Hsieh
 * @version: 1.0
 */
public class PGCompanyDAO {

	/**
	 * 取出所有公司
	 */
	public static List<PGCompany> findAll(Connection conn) throws SQLException {
		String sql = "SELECT * FROM pgcompany";

		return DBQueryRunner.getBeanList(conn, PGCompany.class, sql);
	}


	public static List<PGCompany> findPGCompanyByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, PGCompany.class, "SELECT * FROM pgcompany WHERE update_time > ? ORDER BY update_time", updateTime);
	}
	
}
