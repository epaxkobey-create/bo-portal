package com.nv.commons.dto;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.lang3.StringUtils;

public class BrowserUserAgent {
	private final UserAgent userAgent;
	private final String userAgentString;
	private final String deviceClass;
	private final String agentName;
	private final String agentVersion;
	private final int majorVersion;
	private final String operatingSystemName;
	private final String browserName;

	public BrowserUserAgent(String userAgentString, UserAgentAnalyzer analyzer) {
		this.userAgentString = userAgentString;
		this.userAgent = analyzer.parse(userAgentString);
		this.deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
		this.agentName = userAgent.getValue(UserAgent.AGENT_NAME);
		this.agentVersion = userAgent.getValue(UserAgent.AGENT_VERSION);
		this.operatingSystemName = userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME);
		this.majorVersion = parseMajorVersion();
		this.browserName = normalizeBrowserName(this.agentName);
	}

	private int parseMajorVersion() {
		String major = userAgent.getValue(UserAgent.AGENT_VERSION_MAJOR);
		if (StringUtils.isNotEmpty(major) && !"??".equals(major)) {
			try {
				return Integer.parseInt(major);
			} catch (NumberFormatException ignored) {
				return -1;
			}
		}
		return -1;
	}

	private static String normalizeBrowserName(String agentName) {
		if (agentName == null || "??".equals(agentName)) {
			return "Unknown";
		}
		if (agentName.startsWith("Chrome")) {
			return "Chrome";
		}
		if ("Internet Explorer".equals(agentName)) {
			return "IE";
		}
		return agentName;
	}

	public String getOperatingSystemName() {
		return this.operatingSystemName;
	}

	public String getDeviceTypeName() {
		return switch (this.deviceClass) {
			case "Desktop" -> "PC";
			case "Phone" -> "Mobile";
			case "Tablet" -> "Tablet";
			default -> this.deviceClass;
		};
	}

	public String getBrowserName() {
		return this.browserName;
	}

	public String getBrowserInfo() {
		String result = this.browserName;
		if (StringUtils.isNotEmpty(this.agentVersion) && !"??".equals(this.agentVersion)) {
			String[] versionParts = this.agentVersion.split("\\.");
			if (versionParts.length > 0 && StringUtils.isNotEmpty(versionParts[0])) {
				result += " " + versionParts[0];
				if (versionParts.length > 1 && StringUtils.isNotEmpty(versionParts[1])) {
					result += "." + versionParts[1];
				}
			}
		}
		return result;
	}

	public boolean isMobile() {
		return "Phone".equals(this.deviceClass);
	}

	public boolean isIOS() {
		return this.operatingSystemName != null && this.operatingSystemName.startsWith("iOS");
	}

	public boolean isAndroid() {
		return this.operatingSystemName != null && this.operatingSystemName.startsWith("Android");
	}

	public boolean isIPad() {
		return isIOS() && isTablet();
	}

	public boolean isIPhone() {
		return isIOS() && isMobile();
	}

	public boolean isComputer() {
		return "Desktop".equals(this.deviceClass);
	}

	public boolean isTablet() {
		return "Tablet".equals(this.deviceClass);
	}

	public boolean isMobileOrTablet() {
		return isMobile() || isTablet();
	}

	public boolean isIE(int requiredMajorVersion) {
		if (!"IE".equals(this.browserName)) {
			return false;
		}
		if (requiredMajorVersion > 0) {
			return this.majorVersion == requiredMajorVersion;
		}
		return true;
	}

	public boolean isIE(int minMajorVersion, int maxMajorVersion) {
		if (!"IE".equals(this.browserName)) {
			return false;
		}
		if (minMajorVersion >= 0 && this.majorVersion < minMajorVersion) {
			return false;
		}
		return maxMajorVersion < 0 || this.majorVersion <= maxMajorVersion;
	}

	public boolean isInvalidIE() {
		if (!"IE".equals(this.browserName)) {
			return false;
		}
		return this.majorVersion < 9;
	}

	public boolean isChrome(int requiredMinMajorVersion) {
		if (!"Chrome".equals(this.browserName)) {
			return false;
		}
		if (requiredMinMajorVersion > 0) {
			return this.majorVersion >= requiredMinMajorVersion;
		}
		return true;
	}

	public boolean isChrome(int minMajorVersion, int maxMajorVersion) {
		if (!"Chrome".equals(this.browserName)) {
			return false;
		}
		if (minMajorVersion >= 0 && this.majorVersion < minMajorVersion) {
			return false;
		}
		return maxMajorVersion < 0 || this.majorVersion <= maxMajorVersion;
	}

	public boolean isSafari(int requiredMinMajorVersion) {
		if (!"Safari".equals(this.browserName)) {
			return false;
		}
		if (requiredMinMajorVersion > 0) {
			return this.majorVersion >= requiredMinMajorVersion;
		}
		return true;
	}

	public boolean isSafari(int minMajorVersion, int maxMajorVersion) {
		if (!"Safari".equals(this.browserName)) {
			return false;
		}
		if (minMajorVersion >= 0 && this.majorVersion < minMajorVersion) {
			return false;
		}
		return maxMajorVersion < 0 || this.majorVersion <= maxMajorVersion;
	}

	public String getUserAgentString() {
		return userAgentString;
	}

}
