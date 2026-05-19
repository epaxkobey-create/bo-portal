package com.nv.commons.constants;

import com.nv.commons.utils.CookieUtils;
import com.nv.commons.utils.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum PlatformType {

	WEB(0b0001, "Web"),
	HTML5(0b0010, "Html5"),
	APP(0b0100, "App");

	private final int value;
	private final String name;

	public static final PlatformType[] VALUES = PlatformType.values();

	PlatformType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public static PlatformType getInstance(int value) {
		for (PlatformType type : VALUES) {
			if (type.value == value) {
				return type;
			}
		}
		return null;
	}

	public static List<PlatformType> getPlatformTypes(long sum) {
		List<PlatformType> result = new ArrayList<>();
		for (PlatformType type : VALUES) {
			if ((sum & type.unique()) == type.unique()) {
				result.add(type);
			}
		}
		return result;
	}

	public static List<Integer> getPlatformTypesUnique(long sum) {
		List<Integer> result = new ArrayList<>();
		for (PlatformType type : VALUES) {
			if ((sum & type.unique()) == type.unique()) {
				result.add(type.unique());
			}
		}
		return result;
	}

	public static PlatformType getInstance(HttpServletRequest request) {

		boolean isNative = CookieUtils.getCookie(request, SystemConstants.APP_NAME_COOKIE_NAME) != null;
		// when cookie is empty, try to search header "App-name"
		if (isNative || StringUtils.isNotEmpty(request.getHeader("App-name"))) {
			return APP;
		}
		return UserAgentUtils.getBrowserUserAgent(request).isMobileOrTablet() ? HTML5 : WEB;
	}

	public static boolean showOnH5(long sum) {
		return (sum & PlatformType.HTML5.unique()) == PlatformType.HTML5.unique();
	}

	public static boolean showOnWeb(long sum) {
		return (sum & PlatformType.WEB.unique()) == PlatformType.WEB.unique();
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public boolean isWeb() {
		return this == WEB;
	}

	public boolean isApp() {
		return this == APP;
	}

	public DeviceType getDeviceType() {
		return isWeb() ? DeviceType.PERSONAL_COMPUTER : DeviceType.MOBILE;
	}
}
