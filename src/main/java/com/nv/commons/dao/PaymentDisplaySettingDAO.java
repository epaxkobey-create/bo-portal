package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.PaymentDisplaySetting;
import com.nv.commons.model.database.DBQueryRunner;

public class PaymentDisplaySettingDAO {

	public static List<PaymentDisplaySetting> findAll(Connection conn) throws SQLException {
		return DBQueryRunner.getBeanList(conn, PaymentDisplaySetting.class, "SELECT * FROM paymentDisplaySetting");
	}
	
	
	public static List<PaymentDisplaySetting> findSettingByUpdateTime(Connection conn, Timestamp updateTime) throws SQLException {
		return DBQueryRunner.getBeanList(conn, PaymentDisplaySetting.class, "SELECT * FROM paymentDisplaySetting WHERE update_time > ? ", updateTime);
	}

}
