package com.nv.commons.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dto.BrowserUserAgent;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class UserAgentUtils {

	public static final String USER_AGENT = "User-Agent";

	private static final int MAX_USER_AGENT_LENGTH = 512;

	private static Map<String, BrowserUserAgent> userAgentCache = new ConcurrentHashMap<String, BrowserUserAgent>();

	private static volatile UserAgentAnalyzer analyzer;

	static UserAgentAnalyzer getAnalyzer() {
		if (analyzer == null) {
			synchronized (UserAgentUtils.class) {
				if (analyzer == null) {
					analyzer = UserAgentAnalyzer.newBuilder()
						.hideMatcherLoadStats()
						.withCache(10000)
						.withField(UserAgent.DEVICE_CLASS)
						.withField(UserAgent.AGENT_NAME)
						.withField(UserAgent.AGENT_VERSION)
						.withField(UserAgent.AGENT_VERSION_MAJOR)
						.withField(UserAgent.OPERATING_SYSTEM_NAME)
						.withField(UserAgent.OPERATING_SYSTEM_CLASS)
						.build();
				}
			}
		}
		return analyzer;
	}

	public static BrowserUserAgent getBrowserUserAgent(String userAgentString) {

		BrowserUserAgent browserUserAgent = (userAgentString == null)
			? null
			: userAgentCache.get(userAgentString);

		if (null == browserUserAgent) {
			browserUserAgent = new BrowserUserAgent(userAgentString, getAnalyzer());
			userAgentCache.put(userAgentString, browserUserAgent);
		}
		return browserUserAgent;
	}

	public static BrowserUserAgent getBrowserUserAgent(HttpServletRequest request) {
		return getBrowserUserAgent(request.getHeader("User-Agent"));
	}


	public static String getUserAgent(HttpServletRequest request) {
		String value = request.getHeader("User-Agent");

		if (value.length() > MAX_USER_AGENT_LENGTH) {
			value = value.substring(0, MAX_USER_AGENT_LENGTH);
		}

		return value;
	}


}
