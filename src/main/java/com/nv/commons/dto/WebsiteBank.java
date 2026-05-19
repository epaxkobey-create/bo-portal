package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;


import com.nv.commons.annotation.Column;

public class WebsiteBank {

	
	@Column(name = "bank_id")
	private int bankId;

	@Column(name = "website_type")
	private int websiteType;

	
	@Column(name = "display_name")
	private String displayName;

	private int status;

	@Column(name = "display_order")
	private int displayOrder;

	private String creator;

	@Column(name = "create_time")
	private Timestamp createTime;

	private String updater;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public int getBankId() {
		return bankId;
	}

	public void setBankId(int bankId) {
		this.bankId = bankId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
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

	/**
	 *
	 */
	@Override
	public int hashCode() {
		return Objects.hash(websiteType, bankId);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		WebsiteBank that = (WebsiteBank) other;
		return websiteType == that.websiteType
			&& bankId == that.bankId;
	}
}
