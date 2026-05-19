package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;

public class Manager {
	
	@Column(name = "USER_ID", maxLength=12)
	private String userId;
	
	@Column(name = "website_type")
	private int websiteType;
	
	@Column(name = "USER_NAME")
	private String userName;
	
	private String password;
	
	@Column(name = "LOGIN_TIME")
	private Timestamp loginTime;
	
	@Column(name = "SERVER_ID")
	private String serverID;
	
	@Column(name = "SESSION_ID")
	private String sessionID;
	
	private String creator;
	
	@Column(name = "CREATE_TIME")
	private Timestamp createTime;
	
	private String updater;
	
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	
	private int roleID;
	
	private int status;
	
	private String affiliate;
	
	@Column(name = "ENABLE_POPUP")
	private int enablePopup;

	private boolean virtual;
	
	private long modifyTimeMillis;

	private boolean updateAccessRight = false;

//	private ReportExportRecord lastExportRecord;

//	private WebSiteType websiteTypeObj;

	private final Set<CurrencyType> currencyTypeSet = new HashSet<>(CurrencyType.getAllCurrencyTypeList());

	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}

	public WebSiteType getWebsiteTypeObj() {
		return WebSiteType.getInstance(this.websiteType);
	}

//	public void setWebsiteTypeObj(WebSiteType websiteTypeObj) {
//		this.websiteTypeObj = websiteTypeObj;
//	}

//	public List<Integer> getCurrencyTypeList() {
//		return this.managerRole.getCurrencyTypeList();
//	}

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Timestamp getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
	}

	public String getServerID() {
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public int getRoleID() {
		return roleID;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public int getEnablePopup() {
		return enablePopup;
	}

	public void setEnablePopup(int enablePopup) {
		this.enablePopup = enablePopup;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public long getModifyTimeMillis() {
		return modifyTimeMillis;
	}

	public void setModifyTimeMillis(long modifyTimeMillis) {
		this.modifyTimeMillis = modifyTimeMillis;
	}

	public boolean isUpdateAccessRight() {
		return updateAccessRight;
	}

	public void setUpdateAccessRight(boolean updateAccessRight) {
		this.updateAccessRight = updateAccessRight;
	}

//	public ReportExportRecord getLastExportRecord() {
//		return lastExportRecord;
//	}

//	public void setLastExportRecord(ReportExportRecord lastExportRecord) {
//		this.lastExportRecord = lastExportRecord;
//	}

	public Set<CurrencyType> getCurrencyTypeSet() {
		return currencyTypeSet;
	}

	public List<Integer> getCurrencyTypeIdList() {
		return currencyTypeSet.stream().map(CurrencyType::unique).toList();
	}

}
