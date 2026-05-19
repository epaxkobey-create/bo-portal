package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
public class ProviderAgentMapping {

	@Column(name="ID")
	private int id;

	@Column(name="PROVIDER_AGENT_ID")
	private int providerAgentId;

	@Column(name="WEBSITE_TYPE")
	private int websiteType;

	@Column(name="CREATE_TIME")
	private Timestamp createTime;

	@Column(name="UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name="UPDATER")
	private String updater;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProviderAgentId() {
		return providerAgentId;
	}

	public void setProviderAgentId(int providerAgentId) {
		this.providerAgentId = providerAgentId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
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
