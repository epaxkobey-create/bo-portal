package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.utils.FormatUtils;

public class AccountStats {

	@Column(name = "user_id")
	private String userId;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "currency")
	private String currency;

	@Column(name = "affiliate_id")
	private long affiliateId;

	@Column(name = "summary_date")
	private Timestamp summaryDate;

	@Column(name = "deposit_count")
	private long depositCount;

	@Column(name = "deposit_amount")
	private BigDecimal depositAmount;

	@Column(name = "withdrawal_count")
	private long withdrawalCount;

	@Column(name = "withdrawal_amount")
	private BigDecimal withdrawalAmount;

	@Column(name = "adjustment_count")
	private long adjustmentCount;

	@Column(name = "adjustment_amount")
	private BigDecimal adjustmentAmount;

	@Column(name = "bonus_count")
	private long bonusCount;

	@Column(name = "bonus_amount")
	private BigDecimal bonusAmount;

	@Column(name = "profit_loss")
	private BigDecimal profitLoss;

	@Column(name = "turnover")
	private BigDecimal turnover;

	@Column(name = "bet_count")
	private long betCount;

	@Column(name = "transfer_in")
	private BigDecimal transferIn;

	@Column(name = "transfer_out")
	private BigDecimal transferOut;

	@Column(name = "POINT_TO_BALANCE")
	private BigDecimal pointToBalance;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "recycle_balance")
	private BigDecimal recycleBalance;

	@Column(name = "referral_commission")
	private BigDecimal referralCommission;

	private Timestamp lastTransferInTime;

	private Timestamp lastTransferOutTime;

	private String lastTransferInTimeStr = "";

	private String lastTransferOutTimeStr = "";

	// for bonus
	private Timestamp createTime;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public long getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(long affiliateId) {
		this.affiliateId = affiliateId;
	}

	public Timestamp getSummaryDate() {
		return summaryDate;
	}

	public void setSummaryDate(Timestamp summaryDate) {
		this.summaryDate = summaryDate;
	}

	public long getDepositCount() {
		return depositCount;
	}

	public void setDepositCount(long depositCount) {
		this.depositCount = depositCount;
	}

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.depositAmount = depositAmount;
	}

	public long getWithdrawalCount() {
		return withdrawalCount;
	}

	public void setWithdrawalCount(long withdrawalCount) {
		this.withdrawalCount = withdrawalCount;
	}

	public BigDecimal getWithdrawalAmount() {
		return withdrawalAmount;
	}

	public void setWithdrawalAmount(BigDecimal withdrawalAmount) {
		this.withdrawalAmount = withdrawalAmount;
	}

	public long getAdjustmentCount() {
		return adjustmentCount;
	}

	public void setAdjustmentCount(long adjustmentCount) {
		this.adjustmentCount = adjustmentCount;
	}

	public BigDecimal getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	public long getBonusCount() {
		return bonusCount;
	}

	public void setBonusCount(long bonusCount) {
		this.bonusCount = bonusCount;
	}

	public BigDecimal getBonusAmount() {
		return bonusAmount;
	}

	public void setBonusAmount(BigDecimal bonusAmount) {
		this.bonusAmount = bonusAmount;
	}

	public BigDecimal getProfitLoss() {
		return profitLoss;
	}

	public void setProfitLoss(BigDecimal profitLoss) {
		this.profitLoss = profitLoss;
	}

	public BigDecimal getTurnover() {
		return turnover;
	}

	public void setTurnover(BigDecimal turnover) {
		this.turnover = turnover;
	}

	public long getBetCount() {
		return betCount;
	}

	public void setBetCount(long betCount) {
		this.betCount = betCount;
	}

	public BigDecimal getTransferIn() {
		return transferIn;
	}

	public void setTransferIn(BigDecimal transferIn) {
		this.transferIn = transferIn;
	}

	public BigDecimal getTransferOut() {
		return transferOut;
	}

	public void setTransferOut(BigDecimal transferOut) {
		this.transferOut = transferOut;
	}

	public BigDecimal getPointToBalance() {
		return pointToBalance;
	}

	public void setPointToBalance(BigDecimal pointToBalance) {
		this.pointToBalance = pointToBalance;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public BigDecimal getRecycleBalance() {
		return recycleBalance;
	}

	public void setRecycleBalance(BigDecimal recycleBalance) {
		this.recycleBalance = recycleBalance;
	}

	public BigDecimal getReferralCommission() {
		return referralCommission;
	}

	public void setReferralCommission(BigDecimal referralCommission) {
		this.referralCommission = referralCommission;
	}

	public Timestamp getLastTransferInTime() {
		return lastTransferInTime;
	}

	public Timestamp getLastTransferOutTime() {
		return lastTransferOutTime;
	}

	public String getLastTransferInTimeStr() {
		return lastTransferInTimeStr;
	}

	public void setLastTransferInTimeStr(String lastTransferInTimeStr) {
		this.lastTransferInTimeStr = lastTransferInTimeStr;
	}

	public String getLastTransferOutTimeStr() {
		return lastTransferOutTimeStr;
	}

	public void setLastTransferOutTimeStr(String lastTransferOutTimeStr) {
		this.lastTransferOutTimeStr = lastTransferOutTimeStr;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public void setLastTransferInTime(Timestamp lastTransferInTime) {
		this.lastTransferInTime = lastTransferInTime;
		if (null != lastTransferInTime) {
			this.lastTransferInTimeStr = FormatUtils.dateFormat(lastTransferInTime);
		}
	}

	public void setLastTransferOutTime(Timestamp lastTransferOutTime) {
		this.lastTransferOutTime = lastTransferOutTime;
		if (null != lastTransferOutTime) {
			this.lastTransferOutTimeStr = FormatUtils.dateFormat(lastTransferOutTime);
		}
	}
}
