package com.nv.commons.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

public class HttpUtils {

	private HttpUtils() {
		throw new AssertionError();
	}

	public static boolean isPreCompileRequest(ServletRequest request) {
		if ((request.getRemoteAddr().startsWith("192.168.") || request.getRemoteAddr()
			.startsWith("127.0.0.1")) && request.getParameter("jsp_precompile") != null) {
			return true;
		}
		return false;
	}

	public static boolean isPostMethod(HttpServletRequest httpRequest) {
		return "POST".equalsIgnoreCase(httpRequest.getMethod());
	}

	public static boolean isGetMethod(HttpServletRequest httpRequest) {
		return "GET".equalsIgnoreCase(httpRequest.getMethod());
	}

	public static boolean isJsonContentType(HttpServletRequest httpRequest) {
		return Optional.ofNullable(httpRequest.getContentType()).orElse("").contains("application/json");
	}

	public static Map<String, String> getParameterResultMap(HttpServletRequest request) {
		Enumeration<String> params = request.getParameterNames();
		Map<String, String> result = new HashMap<>();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			// unknown length
			String paramValue = request.getParameterValues(paramName)[0];
			result.put(paramName, paramValue);
		}
		return result;
	}
}