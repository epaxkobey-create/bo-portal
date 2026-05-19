package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AccountTracker;
import com.nv.commons.dto.PageResult;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBQueryRunner;

public class AccountTrackerDAO {

	/*
	 * create new account
	 */
	public static int create(Connection conn, AccountTracker accountTracker) throws SQLException {
		String seqSql = "SELECT ACCOUNTTRACKER_ID_SEQ.NEXTVAL FROM DUAL";
		accountTracker.setId(DBQueryRunner.getNumber(conn, seqSql).longValue());

		String sql = "INSERT INTO accountTracker ("
			+ " USER_ID, WEBSITE_TYPE, IP, LOGIN_DATE, AFFILIATE, VIP_LEVEL, COUNTRY, "
			+ " USER_AGENT, USER_AGEN_TYPE, IP_TRACKER, FINGERPRINT, FINGERPRINT2, "
			+ " FINGERPRINT_CANVAS, FINGERPRINT_ACTIVEX, FINGERPRINT_RESOLUTION, FINGERPRINT4, "
			+ " BROWSER_HASH, DEVICE_HASH, COOKIE_SESSION_HASH, ISP, CITY, STATE, "
			+ " DEVICE_TYPE, PLATFORM_TYPE, ID, LOGIN_TYPE, IS_FIRST_LOGIN "
			+ ") VALUES ("
			+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
			+ ")";

		List<Object> params = new ArrayList<>();
		params.add(accountTracker.getUserId());
		params.add(accountTracker.getWebsiteType());
		params.add(accountTracker.getIp());
		params.add(accountTracker.getLoginDate() != null ?
			accountTracker.getLoginDate() :
			new Timestamp(System.currentTimeMillis()));
		params.add(accountTracker.getAffiliate());
		params.add(accountTracker.getVipLevel());
		params.add(accountTracker.getCountry());
		params.add(accountTracker.getUserAgent());
		params.add(accountTracker.getUserAgentType());
		params.add(accountTracker.getIpTracker());
		params.add(accountTracker.getFingerPrint());
		params.add(accountTracker.getFingerPrint2());
		params.add(accountTracker.getFingerPrintCanvas());
		params.add(accountTracker.getFingerPrintActiveX());
		params.add(accountTracker.getFingerPrintResolution());
		params.add(accountTracker.getFingerPrint4());
		params.add(accountTracker.getBrowserHash());
		params.add(accountTracker.getDeviceHash());
		params.add(accountTracker.getCookieSessionHash());
		params.add(accountTracker.getIsp());
		params.add(accountTracker.getCity());
		params.add(accountTracker.getState());
		params.add(accountTracker.getDeviceType());
		params.add(accountTracker.getPlatformType());
		params.add(accountTracker.getId());
		params.add(accountTracker.getLoginType());
		params.add(accountTracker.isFirstLogin() ? 1 : 0);

		return DBQueryRunner.update(conn, sql, params);
	}

	public static PageResult<AccountTracker> getAccountTrackerWithPageResult(Connection conn, String userId,
		Timestamp startTime, Timestamp endTime, WebSiteType webSiteType, String sortCondition, DBOrderType orderType,
		PageInfo pageInfo) throws Exception {


		String sql =
			"SELECT a.ip, a.country, a.device_type, a.login_date, a.BROWSER_HASH, a.DEVICE_HASH, a.COOKIE_SESSION_HASH, " +
				"  CASE " +
				"    WHEN a.platform_type = 1 THEN 'Web' " +
				"    WHEN a.platform_type = 2 THEN 'Html5' " +
				"    WHEN a.platform_type = 4 THEN 'App' " +
				"    ELSE TO_CHAR(a.platform_type) " +
				"  END || '(' || a.user_agen_type || ')' AS user_agen_type " +
				"FROM accountTracker a " +
				"WHERE a.USER_ID = ? " +
				"  AND a.WEBSITE_TYPE = ? " +
				"  AND a.LOGIN_DATE >= ? " +
				"  AND a.LOGIN_DATE <= ? " +
				"ORDER BY " + sortCondition + " " + orderType.getSqlString();


		List<Object> params = new ArrayList<>();
		params.add(userId);
		params.add(webSiteType.unique());
		params.add(startTime);
		params.add(endTime);

		return DBQueryRunner.getPageResult(conn, AccountTracker.class, sql, pageInfo.getPageNumber(),
			pageInfo.getPageSize(), params);
	}

}