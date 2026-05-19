package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.constants.BankType;
import com.nv.commons.dto.Bank;
import com.nv.commons.model.database.DBQueryRunner;

public class BankDAO {

	public static List<Bank> findAll(Connection conn) throws SQLException {
		String sql = "SELECT * FROM bank";

		return DBQueryRunner.getBeanList(conn, Bank.class, sql);
	}

	public static Bank getBank(Connection conn, int id) throws SQLException {
		String sql = "select * from bank where id=? and bank_type = ?";
		return DBQueryRunner.getBean(conn, Bank.class, sql, id, BankType.ONLINE_BANKING.unique());
	}

	public static List<Bank> getBanks(Connection conn) throws SQLException {
		String sql = "select * from bank where bank_type = ? order by bank_name asc";
		return DBQueryRunner.getBeanList(conn, Bank.class, sql, BankType.ONLINE_BANKING.unique());
	}
}
