package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nv.commons.dto.AccountRemark;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.utils.DbUtils;
import org.apache.commons.lang3.StringUtils;

public class AccountRemarkDAO {

	public static int insert(Connection conn, AccountRemark accountRemark) throws SQLException {
		int updateCount = 0;
		PreparedStatement pstmt = null;
		try {
			boolean hadUpdateTime = accountRemark.getUpdateTime() != null;
			String updateTimeValue = "systimestamp";
			if (hadUpdateTime) {
				updateTimeValue = "?";
			}

			pstmt = conn.prepareStatement(
				"INSERT INTO ACCOUNTREMARK (ID, USER_ID, WEBSITE_TYPE, REMARK_TYPE, REMARK, CREATE_TIME, UPDATE_TIME, UPDATER)"
				+ "VALUES (ACCOUNTREMARK_ID_SEQ.nextval, ?, ?, ?, ?, systimestamp," + updateTimeValue + ", ?)");
			int i = 1;
			pstmt.setString(i++, accountRemark.getUserId());
			pstmt.setInt(i++, accountRemark.getWebsiteType());
			pstmt.setInt(i++, accountRemark.getRemarkType());
			pstmt.setString(i++, accountRemark.getRemark());
			if (hadUpdateTime) {
				pstmt.setTimestamp(i++, accountRemark.getUpdateTime());
			}
			pstmt.setString(i++, accountRemark.getUpdater());

			updateCount = pstmt.executeUpdate();
		} finally {
			DbUtils.close(pstmt);
		}
		return updateCount;
	}

	public static int update(Connection conn, AccountRemark accountRemark) throws SQLException {
		int updateCount = 0;
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(
				"UPDATE ACCOUNTREMARK SET REMARK = ?, UPDATE_TIME = systimestamp, UPDATER = ? "
				+ " WHERE USER_ID = ? AND WEBSITE_TYPE = ? AND REMARK_TYPE = ? ");
			int i = 1;
			pstmt.setString(i++, accountRemark.getRemark());
			pstmt.setString(i++, accountRemark.getUpdater());
			pstmt.setString(i++, accountRemark.getUserId());
			pstmt.setInt(i++, accountRemark.getWebsiteType());
			pstmt.setInt(i++, accountRemark.getRemarkType());

			updateCount = pstmt.executeUpdate();
		} finally {
			DbUtils.close(pstmt);
		}
		return updateCount;
	}

	public static List<AccountRemark> getAllRemarkType(Connection conn, int websiteType, String userId) throws SQLException {
		return getByRemarkType(conn, websiteType, userId, null);
	}

	public static List<AccountRemark> getByRemarkType(Connection conn, int websiteType, String userId,
		Integer remarkType)
		throws SQLException {
		final StringBuilder sql = new StringBuilder(
			"SELECT USER_ID, WEBSITE_TYPE, REMARK_TYPE, REMARK, CREATE_TIME, UPDATE_TIME, UPDATER ");
		sql.append(" FROM ACCOUNTREMARK ");
		sql.append(" WHERE USER_ID = ? ");
		sql.append(" AND WEBSITE_TYPE = ? ");
		if (remarkType != null) {
			sql.append(" AND REMARK_TYPE = ? ");
		}

		List<Object> params = new ArrayList<>();
		params.add(userId);
		params.add(websiteType);
		if (remarkType != null) {
			params.add(remarkType);
		}

		return DBQueryRunner.getBeanList(conn, AccountRemark.class, sql.toString(), params);
	}

}
