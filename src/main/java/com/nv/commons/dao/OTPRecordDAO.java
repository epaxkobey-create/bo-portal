package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.OTPRecord;
import com.nv.commons.model.database.DBQueryRunner;

public class OTPRecordDAO {


	public static List<OTPRecord> findAfterTime(Connection conn, Timestamp updateTime) throws SQLException {
		String sql = "SELECT * FROM otp_record WHERE time >= ?";

		return DBQueryRunner.getBeanList(conn, OTPRecord.class, sql, updateTime);
	}

	public static void insert(Connection conn, OTPRecord otpRecord) throws SQLException {
		String sql = "INSERT INTO otp_record(website_type, user_id, otp_type, code, time) values(?, ?, ?, ?, ?)";

		DBQueryRunner.update(conn, sql, otpRecord.getWebsiteType(), otpRecord.getUserId(), otpRecord.getOtpType(),
			otpRecord.getCode(), otpRecord.getTime());
	}

}
