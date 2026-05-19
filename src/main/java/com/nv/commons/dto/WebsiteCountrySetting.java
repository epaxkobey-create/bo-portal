package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;

public class WebsiteCountrySetting {

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "country_Type")
	private int countryType;

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

	public int getCountryType() {
		return countryType;
	}

	public void setCountryType(int countryType) {
		this.countryType = countryType;
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
		WebsiteCountrySetting that = (WebsiteCountrySetting) o;
		return websiteType == that.websiteType &&
				countryType == that.countryType &&
				settingType == that.settingType &&
				Objects.equals(settingValue, that.settingValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, countryType, settingType, settingValue);
	}

}
