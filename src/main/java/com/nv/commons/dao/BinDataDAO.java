package com.nv.commons.dao;

import java.sql.Connection;
import java.util.List;

import com.nv.commons.dto.BinData;
import com.nv.commons.model.database.DBQueryRunner;

public class BinDataDAO {


	public static BinData findByBin(Connection conn, String bin) throws Exception {
		String sql = "SELECT * FROM bindata WHERE bin = ?";
		return DBQueryRunner.getBean(conn, BinData.class, sql, bin);
	}

}
