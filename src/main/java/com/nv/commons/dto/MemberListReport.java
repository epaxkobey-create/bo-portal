package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.nv.commons.annotation.Column;

public class MemberListReport {

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "email")
	private String email;

	@Column(name = "email_verified")
	private int emailVerified;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "phone_number_verified")
	private int phoneNumberVerified;

	@Column(name = "birthday")
	private Date birthday;

	@Column(name = "vip_level")
	private int vipLevel;

	@Column(name = "summary_convertion_point")
	private BigDecimal vipPoint;

	@Column(name = "affiliate")
	private String affiliate;

	@Column(name = "affiliate_id")
	private long affiliateId;

	@Column(name = "status")
	private int status;

	@Column(name = "total_balance")
	private BigDecimal totalBalance;

	@Column(name = "login_ip")
	private String loginIp;

	@Column(name = "sign_up_ip")
	private String signUpIp;

	@Column(name = "sign_up_city")
	private String signUpCity;

	@Column(name = "sign_up_state")
	private String signUpState;

	@Column(name = "login_time")
	private Timestamp loginTime;

	@Column(name = "last_deposit_time")
	private Timestamp lastDepositTime;

	@Column(name = "last_bet_time")
	private Timestamp lastBetTime;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	@Column(name = "affiliate_name")
	private String affiliateName;

	@Column(name = "user_channel_type")
	private int userChannelType;

	//@Column(name = "friend_referrer")
	// todo @ben 移除欄位
	//private String friendReferrer;

	@Column(name = "sign_up_country")
	private String signUpCountry;

	@Column(name = "sign_up_time")
	private Timestamp signUpTime;

	@Column(name = "deposit_amount")
	private BigDecimal depositAmount;

	private boolean kycVerified = false;

	private boolean bankVerified = false;

	private String userAgent;

	private int deviceType;

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(int emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getPhoneNumberVerified() {
		return phoneNumberVerified;
	}

	public void setPhoneNumberVerified(int phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public BigDecimal getVipPoint() {
		return vipPoint;
	}

	public void setVipPoint(BigDecimal vipPoint) {
		this.vipPoint = vipPoint;
	}

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public long getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(long affiliateId) {
		this.affiliateId = affiliateId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public BigDecimal getTotalBalance() {
		return totalBalance;
	}

	public void setTotalBalance(BigDecimal totalBalance) {
		this.totalBalance = totalBalance;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getSignUpIp() {
		return signUpIp;
	}

	public void setSignUpIp(String signUpIp) {
		this.signUpIp = signUpIp;
	}

	public String getSignUpCity() {
		return signUpCity;
	}

	public void setSignUpCity(String signUpCity) {
		this.signUpCity = signUpCity;
	}

	public String getSignUpState() {
		return signUpState;
	}

	public void setSignUpState(String signUpState) {
		this.signUpState = signUpState;
	}

	public Timestamp getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
	}

	public Timestamp getLastDepositTime() {
		return lastDepositTime;
	}

	public void setLastDepositTime(Timestamp lastDepositTime) {
		this.lastDepositTime = lastDepositTime;
	}

	public Timestamp getLastBetTime() {
		return lastBetTime;
	}

	public void setLastBetTime(Timestamp lastBetTime) {
		this.lastBetTime = lastBetTime;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public String getAffiliateName() {
		return affiliateName;
	}

	public void setAffiliateName(String affiliateName) {
		this.affiliateName = affiliateName;
	}

	public int getUserChannelType() {
		return userChannelType;
	}

	public void setUserChannelType(int userChannelType) {
		this.userChannelType = userChannelType;
	}

	public String getSignUpCountry() {
		return signUpCountry;
	}

	public void setSignUpCountry(String signUpCountry) {
		this.signUpCountry = signUpCountry;
	}

	public Timestamp getSignUpTime() {
		return signUpTime;
	}

	public void setSignUpTime(Timestamp signUpTime) {
		this.signUpTime = signUpTime;
	}

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.depositAmount = depositAmount;
	}

	public boolean isKycVerified() {
		return kycVerified;
	}

	public void setKycVerified(boolean kycVerified) {
		this.kycVerified = kycVerified;
	}

	public boolean isBankVerified() {
		return bankVerified;
	}

	public void setBankVerified(boolean bankVerified) {
		this.bankVerified = bankVerified;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

}