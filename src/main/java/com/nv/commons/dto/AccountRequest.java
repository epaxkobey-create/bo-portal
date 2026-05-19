package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.model.PageInfo;

public class AccountRequest {

	private String userIds;

	private String userName;

	private Timestamp lastDepositTime;

	private Timestamp lastWithdrawTime;

	private Timestamp lastBetTime;

	private Timestamp loginTime;

	private String loginIp;

	private int vipLevel = -1;

//	@Builder.Default
//	private int allowReceiveBonus = -1;

	private int currencyType;

	private Date lastRegister;

	private Date birthOfDateStart;

	private Date birthOfDateEnd;

	private List<Long> affiliateIds;

	private String sortCondition;

	private DBOrderType orderType;

	private WebSiteType webSiteType;

	private PageInfo pageInfo;

	private List<Integer> currencyList;

	private boolean enableShowEmail;

	private boolean enableShowContactPhone;

	private boolean enableShowPartOfContactPhone;

	private int userChannelTypeId;


	private Map<ContactType, String> contactInfoCondition;


	private boolean queryRecentData;

	private Date lastLoginSince;

	private int status;

	private boolean searchVietnamAccount;

	private int verificationStatus;

	public String getUserIds() {
		return userIds;
	}

	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Timestamp getLastDepositTime() {
		return lastDepositTime;
	}

	public void setLastDepositTime(Timestamp lastDepositTime) {
		this.lastDepositTime = lastDepositTime;
	}

	public Timestamp getLastWithdrawTime() {
		return lastWithdrawTime;
	}

	public void setLastWithdrawTime(Timestamp lastWithdrawTime) {
		this.lastWithdrawTime = lastWithdrawTime;
	}

	public Timestamp getLastBetTime() {
		return lastBetTime;
	}

	public void setLastBetTime(Timestamp lastBetTime) {
		this.lastBetTime = lastBetTime;
	}

	public Timestamp getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public int getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(int currencyType) {
		this.currencyType = currencyType;
	}

	public Date getLastRegister() {
		return lastRegister;
	}

	public void setLastRegister(Date lastRegister) {
		this.lastRegister = lastRegister;
	}

	public Date getBirthOfDateStart() {
		return birthOfDateStart;
	}

	public void setBirthOfDateStart(Date birthOfDateStart) {
		this.birthOfDateStart = birthOfDateStart;
	}

	public Date getBirthOfDateEnd() {
		return birthOfDateEnd;
	}

	public void setBirthOfDateEnd(Date birthOfDateEnd) {
		this.birthOfDateEnd = birthOfDateEnd;
	}

	public List<Long> getAffiliateIds() {
		return affiliateIds;
	}

	public void setAffiliateIds(List<Long> affiliateIds) {
		this.affiliateIds = affiliateIds;
	}

	public String getSortCondition() {
		return sortCondition;
	}

	public void setSortCondition(String sortCondition) {
		this.sortCondition = sortCondition;
	}

	public DBOrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(DBOrderType orderType) {
		this.orderType = orderType;
	}

	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public void setWebSiteType(WebSiteType webSiteType) {
		this.webSiteType = webSiteType;
	}

	public PageInfo getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	public List<Integer> getCurrencyList() {
		return currencyList;
	}

	public void setCurrencyList(List<Integer> currencyList) {
		this.currencyList = currencyList;
	}

	public boolean isEnableShowEmail() {
		return enableShowEmail;
	}

	public void setEnableShowEmail(boolean enableShowEmail) {
		this.enableShowEmail = enableShowEmail;
	}

	public boolean isEnableShowContactPhone() {
		return enableShowContactPhone;
	}

	public void setEnableShowContactPhone(boolean enableShowContactPhone) {
		this.enableShowContactPhone = enableShowContactPhone;
	}

	public boolean isEnableShowPartOfContactPhone() {
		return enableShowPartOfContactPhone;
	}

	public void setEnableShowPartOfContactPhone(boolean enableShowPartOfContactPhone) {
		this.enableShowPartOfContactPhone = enableShowPartOfContactPhone;
	}

	public int getUserChannelTypeId() {
		return userChannelTypeId;
	}

	public void setUserChannelTypeId(int userChannelTypeId) {
		this.userChannelTypeId = userChannelTypeId;
	}

	public Map<ContactType, String> getContactInfoCondition() {
		return contactInfoCondition;
	}

	public void setContactInfoCondition(Map<ContactType, String> contactInfoCondition) {
		this.contactInfoCondition = contactInfoCondition;
	}

	public boolean isQueryRecentData() {
		return queryRecentData;
	}

	public void setQueryRecentData(boolean queryRecentData) {
		this.queryRecentData = queryRecentData;
	}

	public Date getLastLoginSince() {
		return lastLoginSince;
	}

	public void setLastLoginSince(Date lastLoginSince) {
		this.lastLoginSince = lastLoginSince;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isSearchVietnamAccount() {
		return searchVietnamAccount;
	}

	public void setSearchVietnamAccount(boolean searchVietnamAccount) {
		this.searchVietnamAccount = searchVietnamAccount;
	}

	public int getVerificationStatus() {
		return verificationStatus;
	}

	public void setVerificationStatus(int verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	public boolean hasContactInfo(){
		return contactInfoCondition.values().stream()
			.anyMatch(Objects::nonNull);
	}

	public static AccountRequestBuilder builder() {
		return new AccountRequestBuilder();
	}

	public static class AccountRequestBuilder {
		private String userIds;
		private String userName;
		private Timestamp lastDepositTime;
		private Timestamp lastWithdrawTime;
		private Timestamp lastBetTime;
		private Timestamp loginTime;
		private String loginIp;
		private int vipLevel = -1;
		private int currencyType;
		private Date lastRegister;
		private Date birthOfDateStart;
		private Date birthOfDateEnd;
		private List<Long> affiliateIds;
		private String sortCondition;
		private DBOrderType orderType;
		private WebSiteType webSiteType;
		private PageInfo pageInfo;
		private List<Integer> currencyList;
		private boolean enableShowEmail;
		private boolean enableShowContactPhone;
		private boolean enableShowPartOfContactPhone;
		private int userChannelTypeId;
		private Map<ContactType, String> contactInfoCondition;
		private boolean queryRecentData;
		private Date lastLoginSince;
		private int status;
		private boolean searchVietnamAccount;
		private int verificationStatus;

		public AccountRequestBuilder userIds(String userIds) {
			this.userIds = userIds;
			return this;
		}

		public AccountRequestBuilder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public AccountRequestBuilder lastDepositTime(Timestamp lastDepositTime) {
			this.lastDepositTime = lastDepositTime;
			return this;
		}

		public AccountRequestBuilder lastWithdrawTime(Timestamp lastWithdrawTime) {
			this.lastWithdrawTime = lastWithdrawTime;
			return this;
		}

		public AccountRequestBuilder lastBetTime(Timestamp lastBetTime) {
			this.lastBetTime = lastBetTime;
			return this;
		}

		public AccountRequestBuilder loginTime(Timestamp loginTime) {
			this.loginTime = loginTime;
			return this;
		}

		public AccountRequestBuilder loginIp(String loginIp) {
			this.loginIp = loginIp;
			return this;
		}

		public AccountRequestBuilder vipLevel(int vipLevel) {
			this.vipLevel = vipLevel;
			return this;
		}

		public AccountRequestBuilder currencyType(int currencyType) {
			this.currencyType = currencyType;
			return this;
		}

		public AccountRequestBuilder lastRegister(Date lastRegister) {
			this.lastRegister = lastRegister;
			return this;
		}

		public AccountRequestBuilder birthOfDateStart(Date birthOfDateStart) {
			this.birthOfDateStart = birthOfDateStart;
			return this;
		}

		public AccountRequestBuilder birthOfDateEnd(Date birthOfDateEnd) {
			this.birthOfDateEnd = birthOfDateEnd;
			return this;
		}

		public AccountRequestBuilder affiliateIds(List<Long> affiliateIds) {
			this.affiliateIds = affiliateIds;
			return this;
		}

		public AccountRequestBuilder sortCondition(String sortCondition) {
			this.sortCondition = sortCondition;
			return this;
		}

		public AccountRequestBuilder orderType(DBOrderType orderType) {
			this.orderType = orderType;
			return this;
		}

		public AccountRequestBuilder webSiteType(WebSiteType webSiteType) {
			this.webSiteType = webSiteType;
			return this;
		}

		public AccountRequestBuilder pageInfo(PageInfo pageInfo) {
			this.pageInfo = pageInfo;
			return this;
		}

		public AccountRequestBuilder currencyList(List<Integer> currencyList) {
			this.currencyList = currencyList;
			return this;
		}

		public AccountRequestBuilder enableShowEmail(boolean enableShowEmail) {
			this.enableShowEmail = enableShowEmail;
			return this;
		}

		public AccountRequestBuilder enableShowContactPhone(boolean enableShowContactPhone) {
			this.enableShowContactPhone = enableShowContactPhone;
			return this;
		}

		public AccountRequestBuilder enableShowPartOfContactPhone(boolean enableShowPartOfContactPhone) {
			this.enableShowPartOfContactPhone = enableShowPartOfContactPhone;
			return this;
		}

		public AccountRequestBuilder userChannelTypeId(int userChannelTypeId) {
			this.userChannelTypeId = userChannelTypeId;
			return this;
		}

		public AccountRequestBuilder contactInfoCondition(Map<ContactType, String> contactInfoCondition) {
			this.contactInfoCondition = contactInfoCondition;
			return this;
		}

		public AccountRequestBuilder queryRecentData(boolean queryRecentData) {
			this.queryRecentData = queryRecentData;
			return this;
		}

		public AccountRequestBuilder lastLoginSince(Date lastLoginSince) {
			this.lastLoginSince = lastLoginSince;
			return this;
		}

		public AccountRequestBuilder status(int status) {
			this.status = status;
			return this;
		}

		public AccountRequestBuilder searchVietnamAccount(boolean searchVietnamAccount) {
			this.searchVietnamAccount = searchVietnamAccount;
			return this;
		}

		public AccountRequestBuilder verificationStatus(int verificationStatus) {
			this.verificationStatus = verificationStatus;
			return this;
		}

		public AccountRequest build() {
			AccountRequest request = new AccountRequest();
			request.setUserIds(this.userIds);
			request.setUserName(this.userName);
			request.setLastDepositTime(this.lastDepositTime);
			request.setLastWithdrawTime(this.lastWithdrawTime);
			request.setLastBetTime(this.lastBetTime);
			request.setLoginTime(this.loginTime);
			request.setLoginIp(this.loginIp);
			request.setVipLevel(this.vipLevel);
			request.setCurrencyType(this.currencyType);
			request.setLastRegister(this.lastRegister);
			request.setBirthOfDateStart(this.birthOfDateStart);
			request.setBirthOfDateEnd(this.birthOfDateEnd);
			request.setAffiliateIds(this.affiliateIds);
			request.setSortCondition(this.sortCondition);
			request.setOrderType(this.orderType);
			request.setWebSiteType(this.webSiteType);
			request.setPageInfo(this.pageInfo);
			request.setCurrencyList(this.currencyList);
			request.setEnableShowEmail(this.enableShowEmail);
			request.setEnableShowContactPhone(this.enableShowContactPhone);
			request.setEnableShowPartOfContactPhone(this.enableShowPartOfContactPhone);
			request.setUserChannelTypeId(this.userChannelTypeId);
			request.setContactInfoCondition(this.contactInfoCondition);
			request.setQueryRecentData(this.queryRecentData);
			request.setLastLoginSince(this.lastLoginSince);
			request.setStatus(this.status);
			request.setSearchVietnamAccount(this.searchVietnamAccount);
			request.setVerificationStatus(this.verificationStatus);
			return request;
		}
	}
}
