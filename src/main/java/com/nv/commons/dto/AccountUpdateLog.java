package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.utils.JSONUtils;

public class AccountUpdateLog {

//	public AccountUpdateLog() {
//	}
//
//	public AccountUpdateLog(long id, String userId, int websiteType, int logType, String records, String updater, Timestamp updateTime, String updaterIp, int currencyTypeId, UpdateRecord accountUpdateRecord, String logTypeStr) {
//		this.id = id;
//		this.userId = userId;
//		this.websiteType = websiteType;
//		this.logType = logType;
//		this.records = records;
//		this.updater = updater;
//		this.updateTime = updateTime;
//		this.updaterIp = updaterIp;
//		this.currencyTypeId = currencyTypeId;
//		this.accountUpdateRecord = accountUpdateRecord;
//		this.logTypeStr = logTypeStr;
//	}

	@Column(name = "id")
	private long id;

	@Column(name = "user_id")
	private String userId;
	
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

//	public AccountUpdateLog(String userId, int websiteType, String updater, Timestamp updateTime, String updaterIp) {
//		this.userId = userId;
//		this.websiteType = websiteType;
//		this.updater = updater;
//		this.updateTime = updateTime;
//		this.updaterIp = updaterIp;
//	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

//	public static AccountUpdateLogBuilder builder() {
//		return new AccountUpdateLogBuilder();
//	}

//	public static class AccountUpdateLogBuilder {
//		private long id;
//		private String userId;
//		private int websiteType;
//		private int logType;
//		private String records;
//		private String updater;
//		private Timestamp updateTime;
//		private String updaterIp;
//		private int currencyTypeId;
//		private UpdateRecord accountUpdateRecord;
//		private String logTypeStr;
//
//		public AccountUpdateLogBuilder id(long id) {
//			this.id = id;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder userId(String userId) {
//			this.userId = userId;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder websiteType(int websiteType) {
//			this.websiteType = websiteType;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder logType(int logType) {
//			this.logType = logType;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder records(String records) {
//			this.records = records;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder updater(String updater) {
//			this.updater = updater;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder updateTime(Timestamp updateTime) {
//			this.updateTime = updateTime;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder updaterIp(String updaterIp) {
//			this.updaterIp = updaterIp;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder currencyTypeId(int currencyTypeId) {
//			this.currencyTypeId = currencyTypeId;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder accountUpdateRecord(UpdateRecord accountUpdateRecord) {
//			this.accountUpdateRecord = accountUpdateRecord;
//			return this;
//		}
//
//		public AccountUpdateLogBuilder logTypeStr(String logTypeStr) {
//			this.logTypeStr = logTypeStr;
//			return this;
//		}
//
//		public AccountUpdateLog build() {
//			AccountUpdateLog log = new AccountUpdateLog();
//			log.setId((long) this.id);
//			log.setUserId(this.userId);
//			log.setWebsiteType(this.websiteType);
//			log.setLogType(this.logType);
//			log.setRecords(this.records);
//			log.setUpdater(this.updater);
//			log.setUpdateTime(this.updateTime);
//			log.setUpdaterIp(this.updaterIp);
//			log.setCurrencyTypeId(this.currencyTypeId);
//			log.setAccountUpdateRecord(this.accountUpdateRecord);
//			log.setLogTypeStr(this.logTypeStr);
//			return log;
//		}
//	}

}