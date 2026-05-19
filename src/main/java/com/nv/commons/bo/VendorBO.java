package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.SQLException;

import com.nv.commons.dao.VendorDAO;
import com.nv.commons.dto.Vendor;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;

public class VendorBO {


	public static Vendor getVendorById(int id) throws SQLException {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return VendorDAO.findVendorById(conn, id);
		} finally {
			DbUtils.close(conn);
		}
	}

}
