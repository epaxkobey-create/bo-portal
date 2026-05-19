package com.nv.commons.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import org.apache.commons.lang3.SerializationUtils;

public class WebsiteProvider implements Serializable {

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "provider_id")
	private int providerId;

	@Column(name = "status")
	private int status;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "display_order")
	private long displayOrder;
	
	@Column(name = "maintenance_start")
	private Timestamp maintenanceStart;

	@Column(name = "maintenance_end")
	private Timestamp maintenanceEnd;

	@Column(name = "create_time")	
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "updater")
	private String updater;

	@Column(name = "extend_connection_info")
	private String extendConnectionInfo;

	@Column(name = "bonus_system_code")
	private String bonusSystemCode;

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getProviderId() {
		return providerId;
	}

	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(long displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Timestamp getMaintenanceStart() {
		return maintenanceStart;
	}

	public void setMaintenanceStart(Timestamp maintenanceStart) {
		this.maintenanceStart = maintenanceStart;
	}

	public Timestamp getMaintenanceEnd() {
		return maintenanceEnd;
	}

	public void setMaintenanceEnd(Timestamp maintenanceEnd) {
		this.maintenanceEnd = maintenanceEnd;
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

	public String getExtendConnectionInfo() {
		return extendConnectionInfo;
	}

	public void setExtendConnectionInfo(String extendConnectionInfo) {
		this.extendConnectionInfo = extendConnectionInfo;
	}

	public String getBonusSystemCode() {
		return bonusSystemCode;
	}

	public void setBonusSystemCode(String bonusSystemCode) {
		this.bonusSystemCode = bonusSystemCode;
	}

	public WebsiteProvider deepCopy() {
		return SerializationUtils.clone(this);
	}

	public WebsiteProvider() {
	}
}
