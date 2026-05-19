package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
public class GameTransactionSummary {

	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "VENDOR_ID")
	private int vendorId;
	
	@Column(name = "WEBSITE_TYPE")
	private int websiteType;
	
	@Column(name = "GAME_TYPE")
	private int gameType;
	
	@Column(name = "SUMMARY_DATE")
	private Timestamp summaryDate;
	
	@Column(name = "VIP_LEVEL")
	private int vipLevel;
	
	private String currency;
	
	@Column(name = "SUM_BET_AMOUNT")
	private BigDecimal sumBetAmount;
	
	private BigDecimal turnover;
	
	@Column(name = "BET_COUNT")
	private int betCount;
	
	private BigDecimal profit;
	
	@Column(name = "REBATE_RATIO")
	private BigDecimal rebateRatio;
	
	@Column(name = "REBATE_AMOUNT")
	private BigDecimal rebateAmount;
	
	@Column(name = "CREATE_TIME")
	private Timestamp createTime;
	
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	
	@Column(name = "PROGRESS_BET_AMOUNT")
	private BigDecimal progressBetAmount;
	
	@Column(name = "PROGRESS_PROFIT_LOSS")
	private BigDecimal progressProfitLoss;
	
	@Column(name = "AFFILIATE_ID")
	private long affiliateId;
	
	private int playerCount;
	
	private String vendorName;

	// for af performance report
	private boolean internal;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public Timestamp getSummaryDate() {
		return summaryDate;
	}

	public void setSummaryDate(Timestamp summaryDate) {
		this.summaryDate = summaryDate;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getSumBetAmount() {
		return sumBetAmount;
	}

	public void setSumBetAmount(BigDecimal sumBetAmount) {
		this.sumBetAmount = sumBetAmount;
	}

	public BigDecimal getTurnover() {
		return turnover;
	}

	public void setTurnover(BigDecimal turnover) {
		this.turnover = turnover;
	}

	public int getBetCount() {
		return betCount;
	}

	public void setBetCount(int betCount) {
		this.betCount = betCount;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public BigDecimal getRebateRatio() {
		return rebateRatio;
	}

	public void setRebateRatio(BigDecimal rebateRatio) {
		this.rebateRatio = rebateRatio;
	}

	public BigDecimal getRebateAmount() {
		return rebateAmount;
	}

	public void setRebateAmount(BigDecimal rebateAmount) {
		this.rebateAmount = rebateAmount;
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

	public BigDecimal getProgressBetAmount() {
		return progressBetAmount;
	}

	public void setProgressBetAmount(BigDecimal progressBetAmount) {
		this.progressBetAmount = progressBetAmount;
	}

	public BigDecimal getProgressProfitLoss() {
		return progressProfitLoss;
	}

	public void setProgressProfitLoss(BigDecimal progressProfitLoss) {
		this.progressProfitLoss = progressProfitLoss;
	}

	public long getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(long affiliateId) {
		this.affiliateId = affiliateId;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

}
