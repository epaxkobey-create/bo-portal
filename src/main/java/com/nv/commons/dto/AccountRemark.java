package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.AccountRemarkType;

public class AccountRemark {

	@Column(name = "user_id")
	private String userId;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "remark_type")
	private int remarkType;

	@Column(name = "remark")
	private String remark;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "updater")
	private String updater;

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

	public int getRemarkType() {
		return remarkType;
	}

	public void setRemarkType(int remarkType) {
		this.remarkType = remarkType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

}
