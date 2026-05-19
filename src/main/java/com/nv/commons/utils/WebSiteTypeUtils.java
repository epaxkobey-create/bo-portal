package com.nv.commons.utils;

import java.util.Arrays;
import java.util.List;

import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dto.WebsiteInfo;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Luke Chi
 */
public class WebSiteTypeUtils {

	/*
	 *
	 */
	public static WebSiteType getWebSiteByBoDomain(String boDomain) {
		try {

			for (WebsiteInfo websiteInfo : WebsiteInfoCache.getInstance().getAll()) {
				WebSiteType websiteType = WebSiteType.getInstance(websiteInfo.getId());

				if (boDomain.equals(websiteType.getBoDomain())) {
					return websiteType;
				}
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		LogUtils.SYS.warn("Cannot find website by BO domain: {}, websiteCache: {}" , boDomain, JSONUtils.toJsonString(WebsiteInfoCache.getInstance().getAll()));

		return null;
	}

	public static WebSiteType getWebSiteByBoAfDomain(String domain) {
		try {
			for (WebsiteInfo websiteInfo : WebsiteInfoCache.getInstance().getAll()) {
				WebSiteType websiteType = WebSiteType.getInstance(websiteInfo.getId());

				String boAffDomainSettingValue = WebsiteSystemSettingCache.getInstance()
					.getValueByKey(websiteType.unique(), websiteType.getDefaultCurrencyType().unique(),
						WebsiteSystemSettingType.BO_AFFILIATE_DOMAIN.unique());

				List<String> affFomains = !Validator.isEmpty(boAffDomainSettingValue) ?
					Arrays.asList(boAffDomainSettingValue.split(";")) :
					null;
				if (affFomains != null && affFomains.contains(domain)) {
					return websiteType;
				}
			}
		} catch (Exception e) {
			LogUtils.affiliate.error(e.getMessage(), e);
		}
		return null;
	}

	/*
	 *
	 */
	public static WebSiteType getWebSiteByFeDomain() {
		return WebSiteType.RSG;
	}

	public static WebSiteType getWebSiteTypeForImageCache(ServletRequest request, HttpSession session) {
		WebSiteType webSiteType = null;
		if (session != null && session.getAttribute(SessionKeyConstants.WEB_SITE_TYPE) != null) {
			int webSiteTypeVal = (int) session.getAttribute(SessionKeyConstants.WEB_SITE_TYPE);
			webSiteType = WebSiteType.getInstance(webSiteTypeVal);
		} else {

			String serverName = request.getServerName();
			if (ServerInfoUtils.isManagerServer()) {
				webSiteType = getWebSiteByBoDomain(serverName);
			}
			if (webSiteType == null && ServerInfoUtils.isPlayerServer()) {
				webSiteType = getWebSiteByFeDomain();
			}
		}
		return webSiteType;
	}


	public static WebSiteType getWebSiteTypeForManagerOrPlayer(String serverName) {

		WebSiteType webSiteType = null;

		if (ServerInfoUtils.isManagerServer()) {
			webSiteType = WebSiteTypeUtils.getWebSiteByBoDomain(serverName);
		}
		if (webSiteType == null && ServerInfoUtils.isPlayerServer()) {
			webSiteType = WebSiteTypeUtils.getWebSiteByFeDomain();
		}
		return webSiteType;
	}

//	// TODO for informal back office
//	public static boolean isInformalDomain(String domain, WebSiteType websiteType) {
//
//		return false;
//	}


}
