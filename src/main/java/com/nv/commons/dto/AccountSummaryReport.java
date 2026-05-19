package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.utils.Validator;

public class AccountSummaryReport {

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "TRANSACTION_TIME")
	private Timestamp transactionTime;

	@Column(name = "PAYMENT_TYPE")
	private int paymentType;
	
	private BigDecimal amount;
	
	private BigDecimal bonus;
	
	private BigDecimal profit;
	
	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "AFFILIATE_ID")
	private long affiliateId;

	private String currency;

	@Column(name = "USER_NAME")
	private String userName;

	public AccountSummaryReport() {
		this.amount = BigDecimal.ZERO;
		this.bonus = BigDecimal.ZERO;
		this.profit = BigDecimal.ZERO;
	}

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

	public Timestamp getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Timestamp transactionTime) {
		this.transactionTime = transactionTime;
	}

	public int getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(int paymentType) {
		this.paymentType = paymentType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBonus() {
		return bonus;
	}

	public void setBonus(BigDecimal bonus) {
		this.bonus = bonus;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public long getAffiliateId() {
		return affiliateId;
	}

	public void setAffiliateId(long affiliateId) {
		this.affiliateId = affiliateId;
	}

	public String getCurrency() {
		return currency;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setCurrency(String currency) {
		if (Validator.isNumeric(currency)) {
			currency = CurrencyType.getInstance(Integer.parseInt(currency)).name();
		}
		this.currency = currency;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountSummaryReport that = (AccountSummaryReport) o;
		return websiteType == that.websiteType &&
				paymentType == that.paymentType &&
				Objects.equals(userId, that.userId) &&
				Objects.equals(transactionTime, that.transactionTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, websiteType, transactionTime, paymentType);
	}
}
