package com.nv.commons.utils;

import java.sql.Timestamp;
import java.util.Set;

import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.cache.WebsiteCountrySettingCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.VendorStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.system.Setting;
import com.nv.commons.system.SystemInfo;

import com.nv.module.backendapi.cache.PlayerLocalCache;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;

public class FrontendUtils {

	private FrontendUtils() {
		throw new AssertionError();
	}

	public static String getExecuteFailPath() {
		return "/page/common/error.jsp";
	}

	public static String getDeviationPath() {
		return "/page/common/deviation.jsp";
	}

	public static String getNotFoundPath() {
		return "/page/common/404.jsp";
	}

	public static String getForbiddenPath() {
		return "/page/common/forbidden.jsp";
	}

	public static String getEmptyPath() {
		return "/page/common/empty.jsp";
	}

	public static String getBackOffice() {
		return "/page/manager/dashboard.jsp";
	}

	public static void noCache(HttpServletResponse response) {
		noCache(response, false);
	}

	public static void noCache(HttpServletResponse response, boolean isCDN) {
		if (isCDN) {
			response.setHeader("Cache-Control", "max-age=1");
		} else {
			response.setHeader("CACHE-CONTROL", "NO-CACHE");// HTTP 1.1
			response.setHeader("PRAGMA", "NO-CACHE");// HTTP 1.0
			// prevents caching at the proxy server
			response.setDateHeader("expires", 0);
		}
	}

	public static String getJQueryPath() {
		// 異動性太低，所以這邊先不加版本號
		// MEMO : jquery 公司有額外附加一段 jQuery.ajaxSetup 的程式，如果有更改，新版請記得也附加上去
		return "/js/common/jquery-1.10.2.min.js"; // lido, tf, cy & yb pt client
		// return "/js/common/jquery-1.12.4.min.js"; // 公版 & yb web & Design team
		// return "/js/common/jquery-3.2.1.min.js";
	}

	public static String getJQueryUIPath() {
		// 異動性太低，所以這邊先不加版本號
		return "/js/plugins/jquery-ui/jquery-ui-1.10.2.custom.min.js";
	}

	public static LanguageType getLanguageType(HttpSession session, HttpServletRequest request) {

		// BO 以後不會是 default ENGLISH

		LanguageType languageType = null;
		LanguageType languageTypeInCookie = null;
		try {
			// default cn
			String displayLanguage = LanguageType.ENGLISH.getLanguageResourceKey();

			WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteByBoDomain(request.getServerName());
			if (webSiteType == null) {
				webSiteType = WebSiteTypeUtils.getWebSiteByFeDomain();
			}
			if (webSiteType == null) {
				return LanguageType.ENGLISH;
			}

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
				String sessionLanguage = String.valueOf(session.getAttribute(SessionKeyConstants.LANGUAGE));

				// 多確認一次 session 內的 language 是否可以在該 website 使用
				Set<LanguageType> languageTypes = WebsiteCurrencySettingCache.getInstance().getLanguages(webSiteType);

				if (languageTypes.contains(LanguageType.getInstance(sessionLanguage))) {
					displayLanguage = sessionLanguage;
				} else {
					if (!languageTypes.isEmpty()) {
						//TODO Check 單純取語言的話沒有幣別可以抓Default
						displayLanguage = WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType)
							.getLanguageResourceKey();
					}
				}

			} else if (WebsiteCurrencySettingCache.getInstance().getLanguages(webSiteType).size() == 1) {
				// 如果 webSiteType 只支援一種 language 就直接用
				displayLanguage = WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType)
					.getLanguageResourceKey();

			} else {
				// 第一次登入 or language cookie 被清掉
				CountryType countryTypeByIp = CountryType.getInstance(CountryLookup.getInstance().getCountry(request));

				if (countryTypeByIp != null) {
					// ip -> country -> currency -> language
					displayLanguage = WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType,
							WebsiteCountrySettingCache.getInstance().getDefaultCurrency(webSiteType, countryTypeByIp))
						.getLanguageResourceKey();

				} else {
					displayLanguage = WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType)
						.getLanguageResourceKey();
				}
			}

			String requestURI = request.getRequestURI();

			/**
			 * MEMO: 因為日後會有 1 台 tomcat 身兼多種 serverType, 所以改成用 path 判斷
			 */
			if (requestURI.startsWith(SystemConstants.AUTO_LOGOUT_PAGE)) {

				String referer = request.getHeader("Referer");
				if (referer != null) {

					referer = referer.split("://")[1].split("/")[0].split(":")[0];
					// Manager
					if (WebSiteTypeUtils.getWebSiteByBoDomain(referer) != null) {
						displayLanguage = LanguageType.ENGLISH.getLanguageResourceKey();
					}
				}
			} else {
				if (requestURI.startsWith("/manager") || requestURI.startsWith("/page/manager") || requestURI
					.startsWith("/login/manager")) {
					displayLanguage = LanguageType.ENGLISH.getLanguageResourceKey();
				}
			}

			/**
			 * check with currency-language map of webSiteType
			 */
			final String _displayLanguage = displayLanguage;

			final CurrencyType userCurrencyType = getUserCurrencyType(request, webSiteType);

			//			final List<LanguageType> languageTypeList = webSiteType.getCurrencyLangMap().get(userCurrencyType);
			final Set<LanguageType> languageTypeSet = WebsiteCurrencySettingCache.getInstance()
				.getLanguages(webSiteType, userCurrencyType);
			if (languageTypeSet != null && languageTypeSet.size() > 0) {
				displayLanguage = languageTypeSet
					.stream()
					.filter(langType -> langType.getLanguageResourceKey().equals(_displayLanguage))
					.findAny()
					.orElse(WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType, userCurrencyType))
					.getLanguageResourceKey();
			}

			languageType = LanguageType.getInstance(displayLanguage);

		} catch (
			Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		// MEMO: for YB should default CHINESE
		return (languageType != null) ? languageType : LanguageType.ENGLISH;
	}

	public static LangMessage getLangMessage(HttpSession session, HttpServletRequest request) {
		return getLanguageType(session, request).getLangMessage();
	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("x-requested-with"));
	}

	public static int getJsFileVersion() {
		// return (int) Math.random();
		return Setting.JS_FILE_VERSION;
	}

	public static boolean isVisitor(HttpSession session) {
		try {
			return (session == null) || (session.getAttribute(SessionKeyConstants.USER_KEY) == null);

		} catch (IllegalStateException e) {
			// for guest: jsp session timeout
			LogUtils.SYS.error(e.getMessage());
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return true;
	}

	public static void setBtag(HttpServletRequest request, HttpServletResponse response) {

		String btag = RequestParser.getStringParameter(request, 1240, "btag", "");

		if (StringUtils.isNotEmpty(btag)) {
			Cookie btagCookie = CookieUtils.getCookie(request, "btag");
			if (btagCookie == null || btagCookie.getMaxAge() == -1) {

				String path = request.getContextPath() == null ? "/" : request.getContextPath();
				if ("".equals(path)) {
					path = "/";
				}

				StringBuilder str = new StringBuilder();
				str.append("btag=");
				str.append(btag).append("; ");
				str.append("Max-Age=");
				str.append((30 * 24 * 60 * 60) + "; ");
				str.append("Domain=");
				str.append(request.getServerName()).append("; ");
				str.append("Path=");
				str.append(path).append("; ");
				str.append("SameSite=None; ");
				str.append("Secure");
				response.addHeader("Set-Cookie", str.toString());
				//			CookieUtils.setCookie(request, response, "btag", btag, request.getServerName(), 30 * 24 * 60 * 60);
			}
		}
	}

	public static boolean needShowBoMaintain() {
		return SystemInfo.getInstance().isSystemMaintainOn();
	}

//	public static boolean isWebUnderMaintenance(ServletRequest request) {
//
//		WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteByFeDomain();
//		CurrencyType currencyType = FrontendUtils.getUserCurrencyType((HttpServletRequest) request, webSiteType);
//
//		if (WebsiteStatusType.isUnderMaintenance(webSiteType, currencyType)) {
//			final String userRealIP = HostAddressUtils.getRealIPAddresses((HttpServletRequest) request);
//
//			if (HostAddressUtils.isSafeIP(userRealIP)) {
//				return false;
//			}
//
//			return IPRuleCache.getInstance()
//				.getAllIPRuleBelowWebsite(webSiteType.unique(), ServerNodeType.MANAGER)
//				.values()
//				.stream()
//				.filter(rule -> rule.getRuleType() == IPRuleType.WHITE_LIST.unique())
//				.map(IPRule::getIp)
//				.noneMatch(o -> o.equals(userRealIP));
//		}
//		return false;
//	}

//	public static boolean isWebUnderMaintenance(WebSiteType webSiteType, CurrencyType currencyType,
//		String requestRealIP) {
//		if (WebsiteStatusType.isUnderMaintenance(webSiteType, currencyType)) {
//			if (HostAddressUtils.isSafeIP(requestRealIP)) {
//				return false;
//			}
//
//			return IPRuleCache.getInstance()
//				.getAllIPRuleBelowWebsite(webSiteType.unique(), ServerNodeType.MANAGER)
//				.values()
//				.stream()
//				.filter(rule -> rule.getRuleType() == IPRuleType.WHITE_LIST.unique())
//				.map(IPRule::getIp)
//				.noneMatch(o -> o.equals(requestRealIP));
//		}
//		return false;
//	}

	public static Timestamp getMaintenanceStartTimestamp(WebsiteVendor webSiteVendor, Vendor vendor,
		WebsiteProvider webSiteProvider) {
		Provider provider = ProviderCache.getInstance().getProvider(webSiteProvider.getProviderId());
		Timestamp maintenanceStart = null;

		if (webSiteVendor.getStatus() == VendorStatusType.MAINTENANCE.unique()) {
			maintenanceStart = webSiteVendor.getMaintenanceStart();

		} else if (vendor.getStatus() == VendorStatusType.MAINTENANCE.unique()) {
			maintenanceStart = vendor.getMaintenanceStart();

		} else if (webSiteProvider.getStatus() == ProviderStatusType.MAINTENANCE.unique()) {
			maintenanceStart = webSiteProvider.getMaintenanceStart();

		} else if (provider.getStatus() == ProviderStatusType.MAINTENANCE.unique()) {
			maintenanceStart = provider.getMaintenanceStart();
		}
		return maintenanceStart;
	}

	public static Timestamp getMaintenanceEndTimestamp(
		WebsiteVendor webSiteVendor, Vendor vendor, WebsiteProvider webSiteProvider) {
		Provider provider = ProviderCache.getInstance().getProvider(webSiteProvider.getProviderId());

		Timestamp maintenanceEnd = null;

		if (webSiteVendor.getStatus() == VendorStatusType.MAINTENANCE.unique()) {
			maintenanceEnd = webSiteVendor.getMaintenanceEnd();

		} else if (vendor.getStatus() == VendorStatusType.MAINTENANCE.unique()) {
			maintenanceEnd = vendor.getMaintenanceEnd();

		} else if (webSiteProvider.getStatus() == ProviderStatusType.MAINTENANCE.unique()) {
			maintenanceEnd = webSiteProvider.getMaintenanceEnd();

		} else if (provider.getStatus() == ProviderStatusType.MAINTENANCE.unique()) {
			maintenanceEnd = provider.getMaintenanceEnd();
		}
		return maintenanceEnd;
	}

	// TODO 下面的程式一直到最後的程式看一下
	/*
	 *
	 */
	public static CurrencyType getUserCurrencyType(HttpServletRequest request, WebSiteType webSiteType) {

		try {
			HttpSession session = request.getSession(false);

			if (session != null) {
				Account player = FrontendUtils.getPlayerAccount(session);
				if (player != null) {
					return CurrencyType.getInstance(player.getCurrencyTypeId());
				}
			}

			int currencyTypeId = -1;

			if (session != null && session.getAttribute(SessionKeyConstants.CURRENCY) != null) {

				currencyTypeId = (int) session.getAttribute(SessionKeyConstants.CURRENCY);

				// 避免 session 取到的 currency id 錯誤 (測試環境 inhouse 會發生此狀況)
				if (!WebsiteCurrencySettingCache.getInstance().isValidCurrency(webSiteType, currencyTypeId)) {
					currencyTypeId = WebsiteCurrencySettingCache.getInstance().getDefaultCurrency(webSiteType).unique();
				}
			} else {
				//直接調整為取預設
				if (WebsiteCurrencySettingCache.getInstance().getCurrencyTypes(webSiteType).size() == 1) {
					// single currency
					currencyTypeId = WebsiteCurrencySettingCache.getInstance().getCurrencyTypes(webSiteType).stream()
						.findFirst().get().unique();

				} else {
					// multi currency
					CountryType countryTypeByIp = CountryType
						.getInstance(CountryLookup.getInstance().getCountry(request));

					if (countryTypeByIp != null) {
						currencyTypeId = WebsiteCountrySettingCache.getInstance()
							.getDefaultCurrency(webSiteType, countryTypeByIp).unique();
					}

					if (!WebsiteCurrencySettingCache.getInstance().isValidCurrency(webSiteType, currencyTypeId)) {
						currencyTypeId = WebsiteCurrencySettingCache.getInstance().getDefaultCurrency(webSiteType)
							.unique();
					}

				}

				if (session != null && currencyTypeId != -1) {
					LogUtils.SYS.debug("getUserCurrencyType set SessionKeyConstants.CURRENCY to " + currencyTypeId);
					session.setAttribute(SessionKeyConstants.CURRENCY, currencyTypeId);
				}
			}

			if (currencyTypeId != -1) {
				return CurrencyType.getInstance(currencyTypeId);
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public static VendorStatusType getProviderVendorStatus(LangMessage langMessage,
		WebSiteType webSiteType, CurrencyType currencyType, String vendorCode,
		boolean ignoreProviderMaintain) {

		WebsiteVendor webSiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, vendorCode);

		if (!VendorUtils.isVendorAvailable(webSiteType, webSiteVendor, currencyType)) {

			throw new Deviation().setI18N("msg.error.validation.notFindVendor", new String[] {vendorCode, "G0003"} );
		}

		//check vendor
		Vendor vendor = VendorCache.getInstance().getVendor(webSiteVendor.getVendorId());

		if (VendorUtils.isVendorInvisible(webSiteVendor, vendor)) {

			String displayNameByLang = vendorCode;

			throw new Deviation(
				langMessage.get("msg.error.validation.vendorIsNotValid", new String[] {displayNameByLang}));

		} else if (VendorUtils.isVendorMaintain(webSiteVendor, vendor) && !ignoreProviderMaintain) {

			return VendorStatusType.MAINTENANCE;
		}

		//check provider
		WebsiteProvider webSiteProvider = ProviderCache.getInstance()
			.getWebsiteProvider(webSiteType, vendor.getProviderId());

		if (ProviderUtils.isProviderInactive(webSiteProvider)) {

			throw new Deviation(
				langMessage.get("msg.error.validation.notFindProvider"));

		} else if (ProviderUtils.isProviderMaintain(webSiteProvider) && !ignoreProviderMaintain) {

			return VendorStatusType.MAINTENANCE;
		}

		return VendorStatusType.ACTIVE;
	}

	public static Account getPlayerAccount(HttpSession session) {
		final String userKeyInSession = (String) session.getAttribute(SessionKeyConstants.USER_KEY);
		if (userKeyInSession == null) {
			return null;
		}
		return PlayerLocalCache.getInstance().get(userKeyInSession);
	}

}
