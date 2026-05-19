package com.nv.commons.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

public class CookieUtils {

	private CookieUtils() {
		throw new AssertionError();
	}

	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || name == null || name.length() == 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	public static String getCookieValue(HttpServletRequest request, String name) {
		Cookie cookie = getCookie(request, name);
		if (cookie != null) {
			try {
				return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException ignored) {
			}
		}
		return null;
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, Cookie cookie) {
		if (cookie != null) {
			// Invalidate the cookie

			cookie.setPath(StringUtils.defaultIfBlank(request.getContextPath(), "/"));
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 * delete cookie with specific name
	 *
	 * @param request
	 * @param response
	 * @param name
	 */
	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie cookie = getCookie(request, name);
		deleteCookie(request, response, cookie);
	}

//	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
//		// Save the cookie value for 1 month
//		setCookie(request, response, name, value, null, 60 * 60 * 24 * 30);
//	}
//
//	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, String domain) {
//		// Save the cookie value for 1 month
//		setCookie(request, response, name, value, domain, 60 * 60 * 24 * 30);
//	}

	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
		int maxAge) {
		setCookie(request, response, name, value, null, maxAge);
	}

	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
		String domain, int maxAge) {
		// Check to make sure the new value is not null (appservers like Tomcat
		// 4 blow up if the value is null).
		if (value == null) {
			value = "";
		}
		String path = request.getContextPath() == null ? "/" : request.getContextPath();
		if ("".equals(path)) {
			path = "/";
		}
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		response.addCookie(cookie);
	}

}
