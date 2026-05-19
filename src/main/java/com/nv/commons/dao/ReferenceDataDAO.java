package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.ReferenceData;
import com.nv.commons.model.database.DBQueryRunner;

public class ReferenceDataDAO {

	public static List<ReferenceData> get(Connection conn, String referenceKey, Timestamp startTime, Timestamp endTime)
		throws SQLException {
		String sql = "SELECT * FROM referenceData WHERE reference_key = ? AND settle_time >= ? AND settle_time <= ? ";
		return DBQueryRunner.getBeanList(conn, ReferenceData.class, sql, referenceKey, startTime, endTime);
	}

}
