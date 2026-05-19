package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class AccountCard {

	@Column(name = "id")
	private long id;

	@Column(name = "user_id", maxLength = 50)
	private String userId;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "card_no", maxLength = 16)
	private String cardNo;

	@Column(name = "card_scheme_type", maxLength = 20)
	private String cardSchemeType;

	@Column(name = "bank_name", maxLength = 120)
	private String bankName;

	@Column(name = "exp_month_year", maxLength = 5)
	private String expMonthYear;

	@Column(name = "cardholder_name", maxLength = 50)
	private String cardholderName;

	@Column(name = "status", maxLength = 1)
	private int status;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardSchemeType() {
		return cardSchemeType;
	}

	public void setCardSchemeType(String cardSchemeType) {
		this.cardSchemeType = cardSchemeType;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getExpMonthYear() {
		return expMonthYear;
	}

	public void setExpMonthYear(String expMonthYear) {
		this.expMonthYear = expMonthYear;
	}

	public String getCardholderName() {
		return cardholderName;
	}

	public void setCardholderName(String cardholderName) {
		this.cardholderName = cardholderName;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
}
