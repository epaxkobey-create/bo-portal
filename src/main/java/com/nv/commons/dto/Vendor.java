package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

//@Deprecated //not yet used
public class Vendor {
	// id
	// provider_id
	// code
	// name
	// icon_path
	// status
	// game_type
	// maNUMBERenance_start
	// maNUMBERenance_end
	// create_date
	// last_update
	// positiontaking
	// display_order
	// order_by
	// api_key

	@Column(name = "id")
	private int id;

	@Column(name = "provider_id")
	private int providerId;

	@Column(name = "code")
	private String code;

	@Column(name = "name")
	private String name;

	@Column(name = "icon_path")
	private String iconPath;

	@Column(name = "status")
	private int status;

	@Column(name = "game_type")
	private int gameType;

	@Column(name = "maintenance_start")
	private Timestamp maintenanceStart;

	@Column(name = "maintenance_end")
	private Timestamp maintenanceEnd;

	@Column(name = "auto_maintenance")
	private int autoMaintenance;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	private String updater;

	@Column(name = "POSITIONTAKING")
	private BigDecimal positiontaking;

	//	@Column(name = "DISPLAY_ORDER")
	//	private int displayOrder;

	@Column(name = "ORDER_BY")
	private int orderBy;

	@Column(name = "API_KEY")
	private String apiKey;

	@Column(name = "THIRD_PARTY_CODE")
	private String thirdPartyCode;

	//	// example "1,2,3,4,5"
	//	@Column(name = "CATEGORIES")
	//	private String categories;

	/*
	 * value == GameType 複合值, 目前有試玩的只有 Slot
	 */
	@Column(name = "TRIAL_PLAY_TYPE")
	private int trialPlayType;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
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

	public BigDecimal getPositiontaking() {
		return positiontaking;
	}

	public void setPositiontaking(BigDecimal positiontaking) {
		this.positiontaking = positiontaking;
	}

	public int getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getThirdPartyCode() {
		return thirdPartyCode;
	}

	public void setThirdPartyCode(String thirdPartyCode) {
		this.thirdPartyCode = thirdPartyCode;
	}

	public int getTrialPlayType() {
		return trialPlayType;
	}

	public void setTrialPlayType(int trialPlayType) {
		this.trialPlayType = trialPlayType;
	}

	//	public int getDisplayOrder() {
	//		return displayOrder;
	//	}
	//
	//	public void setDisplayOrder(int displayOrder) {
	//		this.displayOrder = displayOrder;
	//	}

	//	public String getCategories() {
	//		return categories;
	//	}
	//
	//	public void setCategories(String categories) {
	//		this.categories = categories;
	//	}

}
