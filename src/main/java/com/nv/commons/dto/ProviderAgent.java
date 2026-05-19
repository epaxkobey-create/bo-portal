package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.stream.Stream;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.CurrencyType;

public class ProviderAgent {

	@Column(name = "ID")
	private int id;

	@Column(name = "PROVIDER_ID")
	private int providerId;

	@Column(name = "CURRENCY_TYPE_ID")
	private String currencyTypeId;

	@Column(name = "AGENT_INFO")
	private String agentInfo;

	@Column(name = "AGENT_NAME")
	private String agentName;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "UPDATER")
	private String updater;

	private CurrencyType[] currencyTypes;

	public CurrencyType[] getCurrencyTypes() {
		if (currencyTypes == null) {
			currencyTypes = Stream.of(currencyTypeId.split(","))
				.map(currency -> CurrencyType.getInstance(Integer.parseInt(currency)))
				.toArray(CurrencyType[]::new);
		}
		return currencyTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProviderAgent that = (ProviderAgent) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProviderId() {
		return providerId;
	}

	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}

	public String getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(String currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public String getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(String agentInfo) {
		this.agentInfo = agentInfo;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
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

	public void setCurrencyTypes(CurrencyType[] currencyTypes) {
		this.currencyTypes = currencyTypes;
	}
}
