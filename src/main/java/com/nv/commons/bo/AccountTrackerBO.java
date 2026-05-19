package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.DeviceType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountTrackerDAO;
import com.nv.commons.dto.AccountTracker;
import com.nv.commons.dto.PageResult;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;

public class AccountTrackerBO {

	/**
	 * 新增AccountTracker
	 */
	public static void addAccountTracker(AccountTracker accountTracker) {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			AccountTrackerDAO.create(conn, accountTracker);
			conn.commit();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw new Deviation(SystemConstants.INTERNAL_EXCEPTION);
		} finally {
			DbUtils.close(conn);
		}
	}



	public static String encryptCookieSession(String sessionId) {
		return EncryptUtil.encryptMD5ToHex(sessionId);
	}


	public static String searchProfileLoginLog(String userId, Timestamp startTime,
		Timestamp endTime,
		WebSiteType webSiteType, String sortCondition, DBOrderType orderType, PageInfo pageInfo) throws Exception {

		PageResult<AccountTracker> pageResult;
		try (Connection conn = DBPool.getReadConnection()) {
			pageResult = AccountTrackerDAO.getAccountTrackerWithPageResult(conn, userId,
				startTime, endTime, webSiteType, sortCondition, orderType, pageInfo);
		}

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pageResult.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pageResult.getTotalCount());
			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);
			for (AccountTracker data : pageResult.getResultList()) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("ip", data.getIp());
				jGenerator.writeStringField("loginDate",
					FormatUtils.dateFormat(data.getLoginDate()));
				jGenerator.writeStringField("country", data.getCountry());
				jGenerator.writeNumberField("loginDateTime", data.getLoginDate().getTime());
				String deviceTypeName = data.getDeviceType() == DeviceType.MOBILE.unique() ? "MOBILE" : "COMPUTER";
				jGenerator.writeStringField("deviceType", deviceTypeName);
				jGenerator.writeStringField("loginType", "ACCOUNT");
				jGenerator.writeStringField("browserHash", data.getBrowserHash());
				jGenerator.writeStringField("deviceHash", data.getDeviceHash());
				jGenerator.writeStringField("cookieSessionHash", data.getCookieSessionHash());

				jGenerator.writeStringField("userAgentType", data.getUserAgentType());
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
		};

		return JSONUtils.getJSONString(processor);
	}
}