package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;

public class WebsiteInfo {

	@Column(name = "ID")
	private int id;

	@Column(name = "BUSINESS_TYPE")
	private String businessType;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	/*
	 * 1 = auto force serve, 0 = player can apply manual force serve
	 */
	//	@Setter
	//	@Getter
	//	@Column(name = "ACCOUNT_BONUS_AUTO_SETTLEMENT")
	//	private int accountBonusAutoSettlement;

	//	@Setter
	//	@Getter
	//	@Column(name = "ACCOUNT_VIP_AUTO_UPGRADE")
	//	private int accountVipAutoUpgrade;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "UPDATER")
	private String updater;

	//	@Column(name = "CURRENCY_TYPE_ID")
	//	private String currencyTypeId;

	//	private CurrencyType[] currencyTypes;

	//	@Setter
	//	@Getter
	//	@Column(name = "ENABLE_CACHE_SERVER")
	//	private int enableCacheServer;

	@Column(name = "CACHE_DOMAIN_GP")
	private String cacheDomainGP;

	@Column(name = "CACHE_DOMAIN_GM")
	private String cacheDomainGM;

	@Column(name = "CACHE_DOMAIN_CNP")
	private String cacheDomainCNP;

	@Column(name = "CACHE_DOMAIN_CNM")
	private String cacheDomainCNM;

	@Column(name = "ENABLE_EMBED_GAME")
	private int enableEmbedGame = 0;

	@Column(name = "FORGOT_PWD_UNLOCK_ACCOUNT")
	private int forgotPwdUnlockAccount = 0;

	// todo remove from dto
	public CurrencyType[] getCurrencyTypes() {
		return WebsiteCurrencySettingCache.getInstance().getCurrencyTypes(WebSiteType.getInstance(getId()))
			.toArray(new CurrencyType[0]);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getEnableEmbedGame() {
		return enableEmbedGame;
	}

	public void setEnableEmbedGame(int enableEmbedGame) {
		this.enableEmbedGame = enableEmbedGame;
	}

	public int getForgotPwdUnlockAccount() {
		return forgotPwdUnlockAccount;
	}

	public void setForgotPwdUnlockAccount(int forgotPwdUnlockAccount) {
		this.forgotPwdUnlockAccount = forgotPwdUnlockAccount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		WebsiteInfo that = (WebsiteInfo) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
