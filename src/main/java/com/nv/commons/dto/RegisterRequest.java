package com.nv.commons.dto;

import java.util.List;
import javax.enterprise.inject.Default;

import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.RegisterType;
import com.nv.commons.constants.WebSiteType;

public class RegisterRequest {

	public RegisterRequest() {
	}

	private WebSiteType webSiteType;

	private LanguageType languageType;

	private String userId;

	@Default
	private String userName = "";

	@Default
	private String legalFirstName = "";

	@Default
	private String legalLastName = "";

	private String password;

	private String confirmPassword;

	private int currencyTypeId;

	private String dateOfBirth;

	private String phoneNumber;

	private String email;

	private String accessToken;

	@Default
	private int gender = -1;

	@Default
	private int marital = -1;

	@Default
	private String qqId = "";

	@Default
	private String wechatId = "";

	private int userBankId;

	private String userBankAccountNo;

	private String affiliateCode;

	private String affiliateKeyword;

	private String affiliateDomain;

	private String requestDomain;

	private String realIp;

	private String realFingerprint;

	private int contactTypeId;

	private String contactContent;

	private List<AccountDocument> applyDocumentList;

	private String countryCallingCode;

	private String cpf;

	private AccountStatusType accountStatusType;

	private String the3partUserKey;

	private boolean isFromFrontend = true;

	private String captcha;

	private String random;

	private String verifyCode;

	@Default
	private int registerType = RegisterType.ACCOUNT.unique();

	private List<AccountPlayResponsiblySetting> wagerLimits;

	private List<AccountPlayResponsiblySetting> lossLimits;

	private List<AccountPlayResponsiblySetting> realityCheck;

	private List<AccountPlayResponsiblySetting> depositLimits;

	public boolean isRegisterWithMail() {
//		return RegisterType.EMAIL.unique() == this.registerType;
		return false;
	}

	public boolean isRegisterWithPhone() {
//		return RegisterType.PHONE.unique() == this.registerType;
		return false;
	}


	public boolean isRegisterWithPassword() {
//		return RegisterType.ACCOUNT.unique() == this.registerType;
		return false;
	}

	public boolean isRegisterWitFacebook() {
//		return RegisterType.FACEBOOK.unique() == this.registerType;
		return false;
	}
	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public void setWebSiteType(WebSiteType webSiteType) {
		this.webSiteType = webSiteType;
	}

	public LanguageType getLanguageType() {
		return languageType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLegalFirstName() {
		return legalFirstName;
	}

	public void setLegalFirstName(String legalFirstName) {
		this.legalFirstName = legalFirstName;
	}

	public String getLegalLastName() {
		return legalLastName;
	}

	public void setLegalLastName(String legalLastName) {
		this.legalLastName = legalLastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
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

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getMarital() {
		return marital;
	}

	public void setMarital(int marital) {
		this.marital = marital;
	}

	public String getQqId() {
		return qqId;
	}

	public void setQqId(String qqId) {
		this.qqId = qqId;
	}

	public String getWechatId() {
		return wechatId;
	}

	public void setWechatId(String wechatId) {
		this.wechatId = wechatId;
	}

	public int getUserBankId() {
		return userBankId;
	}

	public void setUserBankId(int userBankId) {
		this.userBankId = userBankId;
	}

	public String getUserBankAccountNo() {
		return userBankAccountNo;
	}

	public void setUserBankAccountNo(String userBankAccountNo) {
		this.userBankAccountNo = userBankAccountNo;
	}

	public String getAffiliateCode() {
		return affiliateCode;
	}

	public void setAffiliateCode(String affiliateCode) {
		this.affiliateCode = affiliateCode;
	}

	public String getAffiliateKeyword() {
		return affiliateKeyword;
	}

	public void setAffiliateKeyword(String affiliateKeyword) {
		this.affiliateKeyword = affiliateKeyword;
	}

	public String getAffiliateDomain() {
		return affiliateDomain;
	}

	public void setAffiliateDomain(String affiliateDomain) {
		this.affiliateDomain = affiliateDomain;
	}

	public String getRequestDomain() {
		return requestDomain;
	}

	public void setRequestDomain(String requestDomain) {
		this.requestDomain = requestDomain;
	}

	public String getRealIp() {
		return realIp;
	}

	public void setRealIp(String realIp) {
		this.realIp = realIp;
	}

	public String getRealFingerprint() {
		return realFingerprint;
	}

	public void setRealFingerprint(String realFingerprint) {
		this.realFingerprint = realFingerprint;
	}

	public int getContactTypeId() {
		return contactTypeId;
	}

	public void setContactTypeId(int contactTypeId) {
		this.contactTypeId = contactTypeId;
	}

	public String getContactContent() {
		return contactContent;
	}

	public void setContactContent(String contactContent) {
		this.contactContent = contactContent;
	}

	public List<AccountDocument> getApplyDocumentList() {
		return applyDocumentList;
	}

	public void setApplyDocumentList(List<AccountDocument> applyDocumentList) {
		this.applyDocumentList = applyDocumentList;
	}

	public String getCountryCallingCode() {
		return countryCallingCode;
	}

	public void setCountryCallingCode(String countryCallingCode) {
		this.countryCallingCode = countryCallingCode;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public AccountStatusType getAccountStatusType() {
		return accountStatusType;
	}

	public void setAccountStatusType(AccountStatusType accountStatusType) {
		this.accountStatusType = accountStatusType;
	}

	public String getThe3partUserKey() {
		return the3partUserKey;
	}

	public void setThe3partUserKey(String the3partUserKey) {
		this.the3partUserKey = the3partUserKey;
	}

	public boolean isFromFrontend() {
		return isFromFrontend;
	}

	public void setFromFrontend(boolean isFromFrontend) {
		this.isFromFrontend = isFromFrontend;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public int getRegisterType() {
		return registerType;
	}

	public void setRegisterType(int registerType) {
		this.registerType = registerType;
	}

	public boolean isRegisterWitGoogle() {
//		return RegisterType.GOOGLE.unique() == this.registerType;
		return false;
	}

	public List<AccountPlayResponsiblySetting> getWagerLimits() {
		return wagerLimits;
	}

	public void setWagerLimits(List<AccountPlayResponsiblySetting> wagerLimits) {
		this.wagerLimits = wagerLimits;
	}

	public List<AccountPlayResponsiblySetting> getLossLimits() {
		return lossLimits;
	}

	public void setLossLimits(List<AccountPlayResponsiblySetting> lossLimits) {
		this.lossLimits = lossLimits;
	}

	public List<AccountPlayResponsiblySetting> getRealityCheck() {
		return realityCheck;
	}

	public void setRealityCheck(List<AccountPlayResponsiblySetting> realityCheck) {
		this.realityCheck = realityCheck;
	}

	public List<AccountPlayResponsiblySetting> getDepositLimits() {
		return depositLimits;
	}

	public void setDepositLimits(List<AccountPlayResponsiblySetting> depositLimits) {
		this.depositLimits = depositLimits;
	}

	public static RegisterRequestBuilder builder() {
		return new RegisterRequestBuilder();
	}

	public static class RegisterRequestBuilder {
		private WebSiteType webSiteType;
		private LanguageType languageType;
		private String userId;
		private String userName = "";
		private String legalFirstName = "";
		private String legalLastName = "";
		private String password;
		private String confirmPassword;
		private int currencyTypeId;
		private String dateOfBirth;
		private String phoneNumber;
		private String email;
		private String accessToken;
		private int gender = -1;
		private int marital = -1;
		private String qqId = "";
		private String wechatId = "";
		private int userBankId;
		private String userBankAccountNo;
		private String affiliateCode;
		private String affiliateKeyword;
		private String affiliateDomain;
		private String requestDomain;
		private String realIp;
		private String realFingerprint;
		private int contactTypeId;
		private String contactContent;
		private List<AccountDocument> applyDocumentList;
		private String countryCallingCode;
		private String cpf;
		private AccountStatusType accountStatusType;
		private String the3partUserKey;
		private boolean isFromFrontend = true;
		private String captcha;
		private String random;
		private String verifyCode;
		private int registerType = RegisterType.ACCOUNT.unique();
		private List<AccountPlayResponsiblySetting> wagerLimits;
		private List<AccountPlayResponsiblySetting> lossLimits;
		private List<AccountPlayResponsiblySetting> realityCheck;
		private List<AccountPlayResponsiblySetting> depositLimits;

		public RegisterRequestBuilder webSiteType(WebSiteType webSiteType) {
			this.webSiteType = webSiteType;
			return this;
		}

		public RegisterRequestBuilder languageType(LanguageType languageType) {
			this.languageType = languageType;
			return this;
		}

		public RegisterRequestBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public RegisterRequestBuilder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public RegisterRequestBuilder legalFirstName(String legalFirstName) {
			this.legalFirstName = legalFirstName;
			return this;
		}

		public RegisterRequestBuilder legalLastName(String legalLastName) {
			this.legalLastName = legalLastName;
			return this;
		}

		public RegisterRequestBuilder password(String password) {
			this.password = password;
			return this;
		}

		public RegisterRequestBuilder confirmPassword(String confirmPassword) {
			this.confirmPassword = confirmPassword;
			return this;
		}

		public RegisterRequestBuilder currencyTypeId(int currencyTypeId) {
			this.currencyTypeId = currencyTypeId;
			return this;
		}

		public RegisterRequestBuilder dateOfBirth(String dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
			return this;
		}

		public RegisterRequestBuilder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public RegisterRequestBuilder email(String email) {
			this.email = email;
			return this;
		}

		public RegisterRequestBuilder accessToken(String accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public RegisterRequestBuilder gender(int gender) {
			this.gender = gender;
			return this;
		}

		public RegisterRequestBuilder marital(int marital) {
			this.marital = marital;
			return this;
		}

		public RegisterRequestBuilder qqId(String qqId) {
			this.qqId = qqId;
			return this;
		}

		public RegisterRequestBuilder wechatId(String wechatId) {
			this.wechatId = wechatId;
			return this;
		}

		public RegisterRequestBuilder userBankId(int userBankId) {
			this.userBankId = userBankId;
			return this;
		}

		public RegisterRequestBuilder userBankAccountNo(String userBankAccountNo) {
			this.userBankAccountNo = userBankAccountNo;
			return this;
		}

		public RegisterRequestBuilder affiliateCode(String affiliateCode) {
			this.affiliateCode = affiliateCode;
			return this;
		}

		public RegisterRequestBuilder affiliateKeyword(String affiliateKeyword) {
			this.affiliateKeyword = affiliateKeyword;
			return this;
		}

		public RegisterRequestBuilder affiliateDomain(String affiliateDomain) {
			this.affiliateDomain = affiliateDomain;
			return this;
		}

		public RegisterRequestBuilder requestDomain(String requestDomain) {
			this.requestDomain = requestDomain;
			return this;
		}

		public RegisterRequestBuilder realIp(String realIp) {
			this.realIp = realIp;
			return this;
		}

		public RegisterRequestBuilder realFingerprint(String realFingerprint) {
			this.realFingerprint = realFingerprint;
			return this;
		}

		public RegisterRequestBuilder contactTypeId(int contactTypeId) {
			this.contactTypeId = contactTypeId;
			return this;
		}

		public RegisterRequestBuilder contactContent(String contactContent) {
			this.contactContent = contactContent;
			return this;
		}

		public RegisterRequestBuilder applyDocumentList(List<AccountDocument> applyDocumentList) {
			this.applyDocumentList = applyDocumentList;
			return this;
		}

		public RegisterRequestBuilder countryCallingCode(String countryCallingCode) {
			this.countryCallingCode = countryCallingCode;
			return this;
		}

		public RegisterRequestBuilder cpf(String cpf) {
			this.cpf = cpf;
			return this;
		}

		public RegisterRequestBuilder accountStatusType(AccountStatusType accountStatusType) {
			this.accountStatusType = accountStatusType;
			return this;
		}

		public RegisterRequestBuilder the3partUserKey(String the3partUserKey) {
			this.the3partUserKey = the3partUserKey;
			return this;
		}

		public RegisterRequestBuilder isFromFrontend(boolean isFromFrontend) {
			this.isFromFrontend = isFromFrontend;
			return this;
		}

		public RegisterRequestBuilder captcha(String captcha) {
			this.captcha = captcha;
			return this;
		}

		public RegisterRequestBuilder random(String random) {
			this.random = random;
			return this;
		}

		public RegisterRequestBuilder verifyCode(String verifyCode) {
			this.verifyCode = verifyCode;
			return this;
		}

		public RegisterRequestBuilder registerType(int registerType) {
			this.registerType = registerType;
			return this;
		}

		public RegisterRequestBuilder wagerLimits(List<AccountPlayResponsiblySetting> wagerLimits) {
			this.wagerLimits = wagerLimits;
			return this;
		}

		public RegisterRequestBuilder lossLimits(List<AccountPlayResponsiblySetting> lossLimits) {
			this.lossLimits = lossLimits;
			return this;
		}

		public RegisterRequestBuilder realityCheck(List<AccountPlayResponsiblySetting> realityCheck) {
			this.realityCheck = realityCheck;
			return this;
		}

		public RegisterRequestBuilder depositLimits(List<AccountPlayResponsiblySetting> depositLimits) {
			this.depositLimits = depositLimits;
			return this;
		}

		public RegisterRequest build() {
			RegisterRequest registerRequest = new RegisterRequest();
			registerRequest.webSiteType = this.webSiteType;
			registerRequest.languageType = this.languageType;
			registerRequest.userId = this.userId;
			registerRequest.userName = this.userName;
			registerRequest.legalFirstName = this.legalFirstName;
			registerRequest.legalLastName = this.legalLastName;
			registerRequest.password = this.password;
			registerRequest.confirmPassword = this.confirmPassword;
			registerRequest.currencyTypeId = this.currencyTypeId;
			registerRequest.dateOfBirth = this.dateOfBirth;
			registerRequest.phoneNumber = this.phoneNumber;
			registerRequest.email = this.email;
			registerRequest.accessToken = this.accessToken;
			registerRequest.gender = this.gender;
			registerRequest.marital = this.marital;
			registerRequest.qqId = this.qqId;
			registerRequest.wechatId = this.wechatId;
			registerRequest.userBankId = this.userBankId;
			registerRequest.userBankAccountNo = this.userBankAccountNo;
			registerRequest.affiliateCode = this.affiliateCode;
			registerRequest.affiliateKeyword = this.affiliateKeyword;
			registerRequest.affiliateDomain = this.affiliateDomain;
			registerRequest.requestDomain = this.requestDomain;
			registerRequest.realIp = this.realIp;
			registerRequest.realFingerprint = this.realFingerprint;
			registerRequest.contactTypeId = this.contactTypeId;
			registerRequest.contactContent = this.contactContent;
			registerRequest.applyDocumentList = this.applyDocumentList;
			registerRequest.countryCallingCode = this.countryCallingCode;
			registerRequest.cpf = this.cpf;
			registerRequest.accountStatusType = this.accountStatusType;
			registerRequest.the3partUserKey = this.the3partUserKey;
			registerRequest.isFromFrontend = this.isFromFrontend;
			registerRequest.captcha = this.captcha;
			registerRequest.random = this.random;
			registerRequest.verifyCode = this.verifyCode;
			registerRequest.registerType = this.registerType;
			registerRequest.wagerLimits = this.wagerLimits;
			registerRequest.lossLimits = this.lossLimits;
			registerRequest.realityCheck = this.realityCheck;
			registerRequest.depositLimits = this.depositLimits;
			return registerRequest;
		}
	}
}
