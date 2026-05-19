package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;

public class WebsiteCurrencySetting {

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "currency_Type_Id")
	private int currencyTypeId;

	@Column(name = "status")
	private int status;

	@Column(name = "SETTING_TYPE")
	private int settingType;

	@Column(name = "SETTING_VALUE")
	private String settingValue;

	@Column(name = "display_Order")
	private int displayOrder;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSettingType() {
		return settingType;
	}

	public void setSettingType(int settingType) {
		this.settingType = settingType;
	}

	public String getSettingValue() {
		return settingValue;
	}

	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WebsiteCurrencySetting that = (WebsiteCurrencySetting) o;
		return websiteType == that.websiteType &&
				currencyTypeId == that.currencyTypeId &&
				settingType == that.settingType &&
				Objects.equals(settingValue, that.settingValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, currencyTypeId, settingType, settingValue);
	}

}
