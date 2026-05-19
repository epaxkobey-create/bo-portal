package com.nv.commons.utils;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.RoleResourceCache;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.ManagerStatusType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dto.Manager;
import com.nv.commons.dto.RoleResource;
import com.nv.commons.message.LangMessage;
import com.nv.commons.system.SystemInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class ManagerUtils {

	private ManagerUtils() {
		throw new AssertionError();
	}


	public static LanguageType getLanguageType(HttpSession session, HttpServletRequest request) {

		LanguageType languageType = null;
		LanguageType languageTypeInCookie = null;

		try {

			String displayLanguage = LanguageType.ENGLISH.getLanguageResourceKey();

			try {
				Cookie cookie = CookieUtils.getCookie(request, SessionKeyConstants.LANGUAGE);
				if (cookie != null) {
					languageTypeInCookie = LanguageType.getInstance(cookie.getValue());
				}
			} catch (IllegalArgumentException ignore) {
				// when lang in cookie is not valid will have IllegalArgumentException
			}

			if (languageTypeInCookie != null) {
				displayLanguage = languageTypeInCookie.getLanguageResourceKey();
			} else if (session != null && session.getAttribute(SessionKeyConstants.LANGUAGE) != null) {
				// 使用者有可能在執行到一半的時候突然清掉cookie
				displayLanguage = String.valueOf(session.getAttribute(SessionKeyConstants.LANGUAGE));
			}

			languageType = LanguageType.getInstance(displayLanguage);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return languageType;
	}

	public static LangMessage getLangMessage(HttpSession session, HttpServletRequest request) {
		return getLanguageType(session, request).getLangMessage();
	}

//	public static String maskPartOfContent(String content) {
//
//		if (Validator.isEmpty(content)) {
//			return "";
//		}
//
//		String[] contentStrArr = content.split("-");
//
//		StringBuilder sb = new StringBuilder();
//		sb.append(contentStrArr[0]);
//		sb.append("-");
//
//		sb.append("*".repeat(Math.max(0, contentStrArr[1].length() - 5)));
//
//		sb.append(contentStrArr[1].substring(contentStrArr[1].length() - 5));
//
//		return sb.toString();
//	}

	public static Manager setSuperAdminManager(String sessionId, int webSiteType) {
		Manager manager = new Manager();
		manager.setUserId(SystemConstants.BO_SUPER_ADMIN);
		manager.setPassword(SystemConstants.BO_SUPER_ADMIN_PASSWORD);
		manager.setWebsiteType(webSiteType);
		manager.setServerID(SystemInfo.getInstance().getServerID());
		manager.setSessionID(sessionId);
		manager.setStatus(ManagerStatusType.ACTIVE.unique());
		manager.setEnablePopup(1);
		manager.setLoginTime(new Timestamp(System.currentTimeMillis()));

		return manager;
	}

	public static String fetchAllowSidebar(LangMessage lang) {

		List<RoleResource> resourceList = RoleResourceCache.getInstance().getResourceList();
		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeArrayFieldStart("menus");
			for (RoleResource resource : resourceList) {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("id", resource.getId());
				jGenerator.writeNumberField("parent", resource.getParentId());
				jGenerator.writeStringField("name", lang.get(resource.getDisplayName()));
				jGenerator.writeNumberField("level", resource.getMenuLevel());
				jGenerator.writeNumberField("displayID", resource.getDisplayOrder());
				jGenerator.writeStringField("url", resource.getUrl());
				jGenerator.writeStringField("icon", resource.getIcon());
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
			jGenerator.writeStringField("index", FrontendUtils.getBackOffice());
			jGenerator.writeBooleanField("isAllowCustomerFee", false);
		};
		return JSONUtils.getJSONString(processor);
	}

}
