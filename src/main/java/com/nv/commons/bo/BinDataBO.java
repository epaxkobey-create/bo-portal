package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.dao.BinDataDAO;
import com.nv.commons.dto.BinData;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class BinDataBO {


	public static BinData findByBin(String bin) throws Exception {
		BinData binData;
		try (Connection conn = DBPool.getReadConnection()) {
			binData = BinDataDAO.findByBin(conn, bin);
		}
		return binData;
	}

}
