package com.nv.commons.dto;

import java.sql.Timestamp;


import com.nv.commons.annotation.Column;

public class AccountTracker {

//	public AccountTracker() {
//	}
//
//	public AccountTracker(String userId, int websiteType, String ip, Timestamp loginDate, String affiliate, int vipLevel, String country, String userAgent, String userAgentType, String ipTracker, String fingerPrint, String fingerPrint2, String fingerPrintCanvas, String fingerPrintActiveX, String fingerPrintResolution, String fingerPrint4, String browserHash, String deviceHash, String cookieSessionHash, String fingerPrintPro, String isp, String city, String state, int deviceType, int platformType, long id, int loginType, boolean isFirstLogin) {
//		this.userId = userId;
//		this.websiteType = websiteType;
//		this.ip = ip;
//		this.loginDate = loginDate;
//		this.affiliate = affiliate;
//		this.vipLevel = vipLevel;
//		this.country = country;
//		this.userAgent = userAgent;
//		this.userAgentType = userAgentType;
//		this.ipTracker = ipTracker;
//		this.fingerPrint = fingerPrint;
//		this.fingerPrint2 = fingerPrint2;
//		this.fingerPrintCanvas = fingerPrintCanvas;
//		this.fingerPrintActiveX = fingerPrintActiveX;
//		this.fingerPrintResolution = fingerPrintResolution;
//		this.fingerPrint4 = fingerPrint4;
//		this.browserHash = browserHash;
//		this.deviceHash = deviceHash;
//		this.cookieSessionHash = cookieSessionHash;
//		this.fingerPrintPro = fingerPrintPro;
//		this.isp = isp;
//		this.city = city;
//		this.state = state;
//		this.deviceType = deviceType;
//		this.platformType = platformType;
//		this.id = id;
//		this.loginType = loginType;
//		this.isFirstLogin = isFirstLogin;
//	}

	/*
	 *
	 */
	@Column(name = "user_id")
	private String userId;

	@Column(name = "website_type")
	private int websiteType;

	
	@Column(name = "ip")
	private String ip;

	
	@Column(name = "login_date")
	private Timestamp loginDate;

	@Column(name = "affiliate")
	private String affiliate;

	@Column(name = "vip_level")
	private int vipLevel;

	
	@Column(name = "country")
	private String country;

	@Column(name = "user_agent")
	private String userAgent;

	@Column(name = "user_agen_type")
	private String userAgentType;

	@Column(name = "ip_tracker")
	private String ipTracker;

	@Column(name = "fingerprint")
	private String fingerPrint;

	@Column(name = "fingerPrint2")
	private String fingerPrint2;

	@Column(name = "fingerprint_canvas")
	private String fingerPrintCanvas;

	@Column(name = "fingerprint_activex")
	private String fingerPrintActiveX;

	@Column(name = "fingerprint_resolution")
	private String fingerPrintResolution;

	@Column(name = "FINGERPRINT4")
	private String fingerPrint4;

	@Column(name = "BROWSER_HASH")
	private String browserHash;

	@Column(name = "DEVICE_HASH")
	private String deviceHash;

	@Column(name = "COOKIE_SESSION_HASH")
	private String cookieSessionHash;

	@Column(name = "FINGERPRINTPRO")
	private String fingerPrintPro;

	@Column(name = "isp")
	private String isp;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "device_type")
	private int deviceType;

	@Column(name = "platform_type")
	private int platformType;

	@Column(name = "id")
	private long id;

	@Column(name = "login_type")
	private int loginType;

	@Column(name = "is_first_login")
	private boolean isFirstLogin;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Timestamp getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Timestamp loginDate) {
		this.loginDate = loginDate;
	}

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUserAgentType() {
		return userAgentType;
	}

	public void setUserAgentType(String userAgentType) {
		this.userAgentType = userAgentType;
	}

	public String getIpTracker() {
		return ipTracker;
	}

	public void setIpTracker(String ipTracker) {
		this.ipTracker = ipTracker;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	public String getFingerPrint2() {
		return fingerPrint2;
	}

	public void setFingerPrint2(String fingerPrint2) {
		this.fingerPrint2 = fingerPrint2;
	}

	public String getFingerPrintCanvas() {
		return fingerPrintCanvas;
	}

	public void setFingerPrintCanvas(String fingerPrintCanvas) {
		this.fingerPrintCanvas = fingerPrintCanvas;
	}

	public String getFingerPrintActiveX() {
		return fingerPrintActiveX;
	}

	public void setFingerPrintActiveX(String fingerPrintActiveX) {
		this.fingerPrintActiveX = fingerPrintActiveX;
	}

	public String getFingerPrintResolution() {
		return fingerPrintResolution;
	}

	public void setFingerPrintResolution(String fingerPrintResolution) {
		this.fingerPrintResolution = fingerPrintResolution;
	}

	public String getFingerPrint4() {
		return fingerPrint4;
	}

	public void setFingerPrint4(String fingerPrint4) {
		this.fingerPrint4 = fingerPrint4;
	}

	public String getBrowserHash() {
		return browserHash;
	}

	public void setBrowserHash(String browserHash) {
		this.browserHash = browserHash;
	}

	public String getDeviceHash() {
		return deviceHash;
	}

	public void setDeviceHash(String deviceHash) {
		this.deviceHash = deviceHash;
	}

	public String getCookieSessionHash() {
		return cookieSessionHash;
	}

	public void setCookieSessionHash(String cookieSessionHash) {
		this.cookieSessionHash = cookieSessionHash;
	}

	public String getFingerPrintPro() {
		return fingerPrintPro;
	}

	public void setFingerPrintPro(String fingerPrintPro) {
		this.fingerPrintPro = fingerPrintPro;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public int getPlatformType() {
		return platformType;
	}

	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getLoginType() {
		return loginType;
	}

	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}

	public boolean isFirstLogin() {
		return isFirstLogin;
	}

	public void setFirstLogin(boolean isFirstLogin) {
		this.isFirstLogin = isFirstLogin;
	}

//	public static AccountTrackerBuilder builder() {
//		return new AccountTrackerBuilder();
//	}

//	public static class AccountTrackerBuilder {
//		private String userId;
//		private int websiteType;
//		private String ip;
//		private Timestamp loginDate;
//		private String affiliate;
//		private int vipLevel;
//		private String country;
//		private String userAgent;
//		private String userAgentType;
//		private String ipTracker;
//		private String fingerPrint;
//		private String fingerPrint2;
//		private String fingerPrintCanvas;
//		private String fingerPrintActiveX;
//		private String fingerPrintResolution;
//		private String fingerPrint4;
//		private String browserHash;
//		private String deviceHash;
//		private String cookieSessionHash;
//		private String fingerPrintPro;
//		private String isp;
//		private String city;
//		private String state;
//		private int deviceType;
//		private int platformType;
//		private long id;
//		private int loginType;
//		private boolean isFirstLogin;
//
//		public AccountTrackerBuilder userId(String userId) {
//			this.userId = userId;
//			return this;
//		}
//
//		public AccountTrackerBuilder websiteType(int websiteType) {
//			this.websiteType = websiteType;
//			return this;
//		}
//
//		public AccountTrackerBuilder ip(String ip) {
//			this.ip = ip;
//			return this;
//		}
//
//		public AccountTrackerBuilder loginDate(Timestamp loginDate) {
//			this.loginDate = loginDate;
//			return this;
//		}
//
//		public AccountTrackerBuilder affiliate(String affiliate) {
//			this.affiliate = affiliate;
//			return this;
//		}
//
//		public AccountTrackerBuilder vipLevel(int vipLevel) {
//			this.vipLevel = vipLevel;
//			return this;
//		}
//
//		public AccountTrackerBuilder country(String country) {
//			this.country = country;
//			return this;
//		}
//
//		public AccountTrackerBuilder userAgent(String userAgent) {
//			this.userAgent = userAgent;
//			return this;
//		}
//
//		public AccountTrackerBuilder userAgentType(String userAgentType) {
//			this.userAgentType = userAgentType;
//			return this;
//		}
//
//		public AccountTrackerBuilder ipTracker(String ipTracker) {
//			this.ipTracker = ipTracker;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrint(String fingerPrint) {
//			this.fingerPrint = fingerPrint;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrint2(String fingerPrint2) {
//			this.fingerPrint2 = fingerPrint2;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrintCanvas(String fingerPrintCanvas) {
//			this.fingerPrintCanvas = fingerPrintCanvas;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrintActiveX(String fingerPrintActiveX) {
//			this.fingerPrintActiveX = fingerPrintActiveX;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrintResolution(String fingerPrintResolution) {
//			this.fingerPrintResolution = fingerPrintResolution;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrint4(String fingerPrint4) {
//			this.fingerPrint4 = fingerPrint4;
//			return this;
//		}
//
//		public AccountTrackerBuilder browserHash(String browserHash) {
//			this.browserHash = browserHash;
//			return this;
//		}
//
//		public AccountTrackerBuilder deviceHash(String deviceHash) {
//			this.deviceHash = deviceHash;
//			return this;
//		}
//
//		public AccountTrackerBuilder cookieSessionHash(String cookieSessionHash) {
//			this.cookieSessionHash = cookieSessionHash;
//			return this;
//		}
//
//		public AccountTrackerBuilder fingerPrintPro(String fingerPrintPro) {
//			this.fingerPrintPro = fingerPrintPro;
//			return this;
//		}
//
//		public AccountTrackerBuilder isp(String isp) {
//			this.isp = isp;
//			return this;
//		}
//
//		public AccountTrackerBuilder city(String city) {
//			this.city = city;
//			return this;
//		}
//
//		public AccountTrackerBuilder state(String state) {
//			this.state = state;
//			return this;
//		}
//
//		public AccountTrackerBuilder deviceType(int deviceType) {
//			this.deviceType = deviceType;
//			return this;
//		}
//
//		public AccountTrackerBuilder platformType(int platformType) {
//			this.platformType = platformType;
//			return this;
//		}
//
//		public AccountTrackerBuilder id(long id) {
//			this.id = id;
//			return this;
//		}
//
//		public AccountTrackerBuilder loginType(int loginType) {
//			this.loginType = loginType;
//			return this;
//		}
//
//		public AccountTrackerBuilder isFirstLogin(boolean isFirstLogin) {
//			this.isFirstLogin = isFirstLogin;
//			return this;
//		}
//
//		public AccountTracker build() {
//			AccountTracker tracker = new AccountTracker();
//			tracker.setUserId(this.userId);
//			tracker.setWebsiteType(this.websiteType);
//			tracker.setIp(this.ip);
//			tracker.setLoginDate(this.loginDate);
//			tracker.setAffiliate(this.affiliate);
//			tracker.setVipLevel(this.vipLevel);
//			tracker.setCountry(this.country);
//			tracker.setUserAgent(this.userAgent);
//			tracker.setUserAgentType(this.userAgentType);
//			tracker.setIpTracker(this.ipTracker);
//			tracker.setFingerPrint(this.fingerPrint);
//			tracker.setFingerPrint2(this.fingerPrint2);
//			tracker.setFingerPrintCanvas(this.fingerPrintCanvas);
//			tracker.setFingerPrintActiveX(this.fingerPrintActiveX);
//			tracker.setFingerPrintResolution(this.fingerPrintResolution);
//			tracker.setFingerPrint4(this.fingerPrint4);
//			tracker.setBrowserHash(this.browserHash);
//			tracker.setDeviceHash(this.deviceHash);
//			tracker.setCookieSessionHash(this.cookieSessionHash);
//			tracker.setFingerPrintPro(this.fingerPrintPro);
//			tracker.setIsp(this.isp);
//			tracker.setCity(this.city);
//			tracker.setState(this.state);
//			tracker.setDeviceType(this.deviceType);
//			tracker.setPlatformType(this.platformType);
//			tracker.setId(this.id);
//			tracker.setLoginType(this.loginType);
//			tracker.setFirstLogin(this.isFirstLogin);
//			return tracker;
//		}
//	}
}
