package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class GameSessionUsage {

	@Column(name = "user_key")
	private String userKey;

	@Column(name = "usage")
	private BigDecimal usage;

	@Column(name = "version")
	private BigDecimal version;

	@Column(name = "last_active_time")
	private Timestamp lastActiveTime;

	@Column(name = "create_date")
	private Timestamp createDate;

	@Column(name = "update_date")
	private Timestamp updateDate;

	@Column(name = "limit_snapshot")
	private BigDecimal limitSnapshot;

	@Column(name = "session_status")
	private int sessionStatus; // active =1 , inactive =2

	@Column(name = "period_start_time")
	private Timestamp periodStartTime;

	public GameSessionUsage() {
	}

	public GameSessionUsage(String userKey, BigDecimal usage, BigDecimal version, Timestamp lastActiveTime,
		Timestamp createDate,
		Timestamp updateDate, BigDecimal limitSnapshot, int sessionStatus
		, Timestamp periodStartTime) {
		this.userKey = userKey;
		this.usage = usage;
		this.version = version;
		this.lastActiveTime = lastActiveTime;
		this.createDate = createDate;
		this.updateDate = updateDate;
		this.limitSnapshot = limitSnapshot;
		this.sessionStatus = sessionStatus;
		this.periodStartTime = periodStartTime;
	}

	public Timestamp getPeriodStartTime() {
		return periodStartTime;
	}

	public void setPeriodStartTime(Timestamp periodStartTime) {
		this.periodStartTime = periodStartTime;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public BigDecimal getUsage() {
		return usage;
	}

	public void setUsage(BigDecimal usage) {
		this.usage = usage;
	}

	public BigDecimal getVersion() {
		return version;
	}

	public void setVersion(BigDecimal version) {
		this.version = version;
	}

	public Timestamp getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(Timestamp lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Timestamp getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public BigDecimal getLimitSnapshot() {
		return limitSnapshot;
	}

	public void setLimitSnapshot(BigDecimal limitSnapshot) {
		this.limitSnapshot = limitSnapshot;
	}

	public int getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(int sessionStatus) {
		this.sessionStatus = sessionStatus;
	}
}
