package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
public class Game {

	private int id;

	@Column(name = "VENDOR_ID")
	private int vendorId;

	@Column(name = "GAME_TYPE")
	private int gameType;

	private String name;

	@Column(name = "NAME_EN")
	private String nameEn;

	private String code;

	@Column(name = "EXTRA_DATA")
	private String extraData;

	private int status;

	@Column(name = "ICON_PATH")
	private String iconPath;

	@Column(name = "PROGRESSIVE_CODE")
	private String progressiveCode;
	// 1=true 0=false, 此遊戲是否有 jackpot
	private int jackpot;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "PLATFORM_TYPE")
	private int platformType;

	@Column(name = "DISPLAY_ORDER")
	private int displayOrder;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "EXCLUDE_CURRENCIES")
	private String excludeCurrencies;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getProgressiveCode() {
		return progressiveCode;
	}

	public void setProgressiveCode(String progressiveCode) {
		this.progressiveCode = progressiveCode;
	}

	public int getJackpot() {
		return jackpot;
	}

	public void setJackpot(int jackpot) {
		this.jackpot = jackpot;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public int getPlatformType() {
		return platformType;
	}

	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getExcludeCurrencies() {
		return excludeCurrencies;
	}

	public void setExcludeCurrencies(String excludeCurrencies) {
		this.excludeCurrencies = excludeCurrencies;
	}

	public String getNameByLang(String lang) {

		if (LanguageType.ENGLISH.getLanguageResourceKey().equalsIgnoreCase(lang)) {
			return (name != null) ? name : ""; // cn
		}
		return (nameEn != null) ? nameEn : name;
	}

//	public boolean notExcludeByCurrencies(CurrencyType targetCurrency) {
//		return Optional.ofNullable(excludeCurrencies)
//			.map(currencies -> currencies.split(","))
//			.map(Arrays::stream)
//			.map(currenciesStream -> currenciesStream.noneMatch(currency -> Integer.parseInt(currency) == targetCurrency.unique()))
//			.orElse(true);
//	}
	/*
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Game other)) {
			return false;
		}
		return id == other.id;
	}
}
