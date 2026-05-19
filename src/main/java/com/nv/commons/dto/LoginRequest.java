package com.nv.commons.dto;

import com.nv.commons.constants.DeviceType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.WebSiteType;

public class LoginRequest {

	private String userId;

	private WebSiteType webSiteType;

	private MarketingGroupType marketingGroupType;

	private String password;

	private String accessToken;

	private String loginIp;

	private String sessionId;

	private DeviceType deviceType;

	private PlatformType platformType;

	private boolean isBioLogin;

	private String callingCode;

	private String phoneNumber;

	private String email;

	private int currencyTypeId;

	private String verifyCode;

	private String captcha;

	private String captchaInSession;

//	private String fingerprint;

//	private String fingerprint2;

	private String fingerprintCanvas;

	private String fingerprintActiveX;

	private String fingerprintResolution;

//	private String fingerprint4;

	private String browserHash;

	private String deviceHash;

	private String cookieSessionHash;

//	private String fingerprintPro;

	private String userAgent;

	private String affiliate;

	private String ipTracker;

	private Boolean isFirstLogin = false;

	private int loginType = 0;

	private boolean isFromFrontend = true;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public void setWebSiteType(WebSiteType webSiteType) {
		this.webSiteType = webSiteType;
	}

	public MarketingGroupType getMarketingGroup() {
		return marketingGroupType;
	}

	public void setMarketingGroup(MarketingGroupType marketingGroupType) {
		this.marketingGroupType = marketingGroupType;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public PlatformType getPlatformType() {
		return platformType;
	}

	public void setPlatformType(PlatformType platformType) {
		this.platformType = platformType;
	}

	public boolean isBioLogin() {
		return isBioLogin;
	}

	public void setBioLogin(boolean bioLogin) {
		isBioLogin = bioLogin;
	}

	public String getCallingCode() {
		return callingCode;
	}

	public void setCallingCode(String callingCode) {
		this.callingCode = callingCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getCaptchaInSession() {
		return captchaInSession;
	}

	public void setCaptchaInSession(String captchaInSession) {
		this.captchaInSession = captchaInSession;
	}

	public String getFingerprintCanvas() {
		return fingerprintCanvas;
	}

	public void setFingerprintCanvas(String fingerprintCanvas) {
		this.fingerprintCanvas = fingerprintCanvas;
	}

	public String getFingerprintActiveX() {
		return fingerprintActiveX;
	}

	public void setFingerprintActiveX(String fingerprintActiveX) {
		this.fingerprintActiveX = fingerprintActiveX;
	}

	public String getFingerprintResolution() {
		return fingerprintResolution;
	}

	public void setFingerprintResolution(String fingerprintResolution) {
		this.fingerprintResolution = fingerprintResolution;
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

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public String getIpTracker() {
		return ipTracker;
	}

	public void setIpTracker(String ipTracker) {
		this.ipTracker = ipTracker;
	}

	public Boolean getIsFirstLogin() {
		return isFirstLogin;
	}

	public void setIsFirstLogin(Boolean isFirstLogin) {
		this.isFirstLogin = isFirstLogin;
	}

	public int getLoginType() {
		return loginType;
	}

	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}

	public boolean isFromFrontend() {
		return isFromFrontend;
	}

	public void setFromFrontend(boolean fromFrontend) {
		isFromFrontend = fromFrontend;
	}

	public static LoginRequestBuilder builder() {
		return new LoginRequestBuilder();
	}

	public static class LoginRequestBuilder {
		private String userId;
		private WebSiteType webSiteType;
		private MarketingGroupType marketingGroupType;
		private String password;
		private String accessToken;
		private String loginIp;
		private String sessionId;
		private DeviceType deviceType;
		private PlatformType platformType;
		private boolean isBioLogin;
		private String callingCode;
		private String phoneNumber;
		private String email;
		private int currencyTypeId;
		private String verifyCode;
		private String captcha;
		private String captchaInSession;
		private String fingerprintCanvas;
		private String fingerprintActiveX;
		private String fingerprintResolution;
		private String browserHash;
		private String deviceHash;
		private String cookieSessionHash;
		private String userAgent;
		private String affiliate;
		private String ipTracker;
		private Boolean isFirstLogin = false;
		private int loginType = 0;
		private boolean isFromFrontend = true;

		public LoginRequestBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public LoginRequestBuilder webSiteType(WebSiteType webSiteType) {
			this.webSiteType = webSiteType;
			return this;
		}

		public LoginRequestBuilder marketingGroup(MarketingGroupType marketingGroupType) {
			this.marketingGroupType = marketingGroupType;
			return this;
		}

		public LoginRequestBuilder password(String password) {
			this.password = password;
			return this;
		}

		public LoginRequestBuilder accessToken(String accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public LoginRequestBuilder loginIp(String loginIp) {
			this.loginIp = loginIp;
			return this;
		}

		public LoginRequestBuilder sessionId(String sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		public LoginRequestBuilder deviceType(DeviceType deviceType) {
			this.deviceType = deviceType;
			return this;
		}

		public LoginRequestBuilder platformType(PlatformType platformType) {
			this.platformType = platformType;
			return this;
		}

		public LoginRequestBuilder isBioLogin(boolean isBioLogin) {
			this.isBioLogin = isBioLogin;
			return this;
		}

		public LoginRequestBuilder callingCode(String callingCode) {
			this.callingCode = callingCode;
			return this;
		}

		public LoginRequestBuilder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public LoginRequestBuilder email(String email) {
			this.email = email;
			return this;
		}

		public LoginRequestBuilder currencyTypeId(int currencyTypeId) {
			this.currencyTypeId = currencyTypeId;
			return this;
		}

		public LoginRequestBuilder verifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
			return this;
		}

		public LoginRequestBuilder captcha(String captcha) {
			this.captcha = captcha;
			return this;
		}

		public LoginRequestBuilder captchaInSession(String captchaInSession) {
			this.captchaInSession = captchaInSession;
			return this;
		}

		public LoginRequestBuilder fingerprintCanvas(String fingerprintCanvas) {
			this.fingerprintCanvas = fingerprintCanvas;
			return this;
		}

		public LoginRequestBuilder fingerprintActiveX(String fingerprintActiveX) {
			this.fingerprintActiveX = fingerprintActiveX;
			return this;
		}

		public LoginRequestBuilder fingerprintResolution(String fingerprintResolution) {
			this.fingerprintResolution = fingerprintResolution;
			return this;
		}

		public LoginRequestBuilder browserHash(String browserHash) {
			this.browserHash = browserHash;
			return this;
		}

		public LoginRequestBuilder deviceHash(String deviceHash) {
			this.deviceHash = deviceHash;
			return this;
		}

		public LoginRequestBuilder cookieSessionHash(String cookieSessionHash) {
			this.cookieSessionHash = cookieSessionHash;
			return this;
		}

		public LoginRequestBuilder userAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public LoginRequestBuilder affiliate(String affiliate) {
			this.affiliate = affiliate;
			return this;
		}

		public LoginRequestBuilder ipTracker(String ipTracker) {
			this.ipTracker = ipTracker;
			return this;
		}

		public LoginRequestBuilder isFirstLogin(Boolean isFirstLogin) {
			this.isFirstLogin = isFirstLogin;
			return this;
		}

		public LoginRequestBuilder loginType(int loginType) {
			this.loginType = loginType;
			return this;
		}

		public LoginRequestBuilder isFromFrontend(boolean isFromFrontend) {
			this.isFromFrontend = isFromFrontend;
			return this;
		}

		public LoginRequest build() {
			LoginRequest request = new LoginRequest();
			request.setUserId(this.userId);
			request.setWebSiteType(this.webSiteType);
			request.setMarketingGroup(this.marketingGroupType);
			request.setPassword(this.password);
			request.setAccessToken(this.accessToken);
			request.setLoginIp(this.loginIp);
			request.setSessionId(this.sessionId);
			request.setDeviceType(this.deviceType);
			request.setPlatformType(this.platformType);
			request.setBioLogin(this.isBioLogin);
			request.setCallingCode(this.callingCode);
			request.setPhoneNumber(this.phoneNumber);
			request.setEmail(this.email);
			request.setCurrencyTypeId(this.currencyTypeId);
			request.setVerifyCode(this.verifyCode);
			request.setCaptcha(this.captcha);
			request.setCaptchaInSession(this.captchaInSession);
			request.setFingerprintCanvas(this.fingerprintCanvas);
			request.setFingerprintActiveX(this.fingerprintActiveX);
			request.setFingerprintResolution(this.fingerprintResolution);
			request.setBrowserHash(this.browserHash);
			request.setDeviceHash(this.deviceHash);
			request.setCookieSessionHash(this.cookieSessionHash);
			request.setUserAgent(this.userAgent);
			request.setAffiliate(this.affiliate);
			request.setIpTracker(this.ipTracker);
			request.setIsFirstLogin(this.isFirstLogin);
			request.setLoginType(this.loginType);
			request.setFromFrontend(this.isFromFrontend);
			return request;
		}
	}
}
