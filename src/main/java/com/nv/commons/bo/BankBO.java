package com.nv.commons.bo;

import java.util.List;

import com.nv.commons.dao.BankDAO;
import com.nv.commons.dto.Bank;
import com.nv.commons.utils.DbExecutor;

public class BankBO {

	public static Bank getBank(int bankId) throws Exception {
		return DbExecutor.query(conn -> BankDAO.getBank(conn, bankId));
	}

	public static List<Bank> getBanks() throws Exception {
		return DbExecutor.query(BankDAO::getBanks);
	}
}
