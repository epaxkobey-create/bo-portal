package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.utils.JSONUtils;

public class GameUpdateLog {

//	public GameUpdateLog() {}
//
//	public GameUpdateLog(long id, String gameId, int websiteType, int logType, String records, String updater,
//		Timestamp updateTime, String updaterIp, UpdateRecord accountUpdateRecord, String logTypeStr) {
//		this.id = id;
//		this.gameId = gameId;
//		this.websiteType = websiteType;
//		this.logType = logType;
//		this.records = records;
//		this.updater = updater;
//		this.updateTime = updateTime;
//		this.updaterIp = updaterIp;
//		this.accountUpdateRecord = accountUpdateRecord;
//		this.logTypeStr = logTypeStr;
//	}

	@Column(name = "id")
	private long id;

	@Column(name = "provider_id")
	private String gameId;
	
	@Column(name = "website_type")
	private int websiteType;
	
	@Column(name = "log_type")
	private int logType;
	
	private String records;
	
	private String updater;
	
	@Column(name = "update_time")
	private Timestamp updateTime;
	
	@Column(name = "updater_ip")
	private String updaterIp;
	
	@Column(name = "currency_type_id")
	private int currencyTypeId;
	
	private UpdateRecord accountUpdateRecord;

	@Column (name = "log_type_str")
	private String logTypeStr;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getgameId() {
		return gameId;
	}

	public void setgameId(String gameId) {
		this.gameId = gameId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public String getRecords() {
		return records;
	}

	public void setRecords(String records) {
		this.records = records;
		if(records != null){
			this.accountUpdateRecord = JSONUtils.jsonToObject(records, UpdateRecord.class);
		}
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

	public String getUpdaterIp() {
		return updaterIp;
	}

	public void setUpdaterIp(String updaterIp) {
		this.updaterIp = updaterIp;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public UpdateRecord getAccountUpdateRecord() {
		return accountUpdateRecord;
	}

	public void setAccountUpdateRecord(UpdateRecord accountUpdateRecord) {
		this.accountUpdateRecord = accountUpdateRecord;
	}

	public String getLogTypeStr() {
		return logTypeStr;
	}

	public void setLogTypeStr(String logTypeStr) {
		this.logTypeStr = logTypeStr;
	}

}