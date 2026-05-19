package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;


import com.nv.commons.annotation.Column;

public class Provider {
	// id
	// system_code
	// provider_name
	// icon_path
	// status
	// display_order
	// create_date
	// last_update

	@Column(name = "id")
	private int id;

	
	@Column(name = "system_code")
	private String systemCode;

	
	@Column(name = "provider_name")
	private String providerName;

	@Column(name = "class_name")
	private String className;

	@Column(name = "icon_path")
	private String iconPath;

	@Column(name = "status")
	private int status;

	@Column(name = "display_order")
	private int displayOrder;

	@Column(name = "create_date")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "connection_info")
	private String connectionInfo;

	@Column(name = "maintenance_start")
	private Timestamp maintenanceStart;

	@Column(name = "maintenance_end")
	private Timestamp maintenanceEnd;

	@Column(name = "auto_maintenance")
	private int autoMaintenance;

	@Column(name = "updater")
	private String updater;

	@Column(name = "is_sync_by_currency")
	private int isSyncByCurrency;

	@Column(name = "has_multi_Wallet")
	// todo @Ben 移除欄位
	private int hasMultiWallet;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
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

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(String connectionInfo) {
		this.connectionInfo = connectionInfo;
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

	public int getAutoMaintenance() {
		return autoMaintenance;
	}

	public void setAutoMaintenance(int autoMaintenance) {
		this.autoMaintenance = autoMaintenance;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public int getIsSyncByCurrency() {
		return isSyncByCurrency;
	}

	public void setIsSyncByCurrency(int isSyncByCurrency) {
		this.isSyncByCurrency = isSyncByCurrency;
	}

	public int getHasMultiWallet() {
		return hasMultiWallet;
	}

	public void setHasMultiWallet(int hasMultiWallet) {
		this.hasMultiWallet = hasMultiWallet;
	}

	public boolean isSyncByCurrency() {
		return isSyncByCurrency == 1;
	}

	public boolean hasMultiWallet() {
		return hasMultiWallet == 1;
	}


	public Provider(){}


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		Provider provider = (Provider) o;
		return id == provider.id && status == provider.status && displayOrder == provider.displayOrder
			&& autoMaintenance == provider.autoMaintenance && isSyncByCurrency == provider.isSyncByCurrency
			&& hasMultiWallet == provider.hasMultiWallet && Objects.equals(systemCode, provider.systemCode)
			&& Objects.equals(providerName, provider.providerName) && Objects.equals(className,
			provider.className) && Objects.equals(iconPath, provider.iconPath) && Objects.equals(
			createTime, provider.createTime) && Objects.equals(updateTime, provider.updateTime)
			&& Objects.equals(connectionInfo, provider.connectionInfo) && Objects.equals(
			maintenanceStart, provider.maintenanceStart) && Objects.equals(maintenanceEnd,
			provider.maintenanceEnd) && Objects.equals(updater, provider.updater);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, systemCode, providerName, className, iconPath, status, displayOrder, createTime,
			updateTime,
			connectionInfo, maintenanceStart, maintenanceEnd, autoMaintenance, updater, isSyncByCurrency,
			hasMultiWallet);
	}
}
