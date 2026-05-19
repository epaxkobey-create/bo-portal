package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

/**
 * Title: com.nv.commons.dto.PGMethod<br>
 * Description: 線上支付公司對應銀行-資料庫物件
 *
 * @author: Daniel.Hsieh
 * @version: 1.0
 */
public class PGMethod {
	// company_id
	// bank_id
	// code
	// create_time
	// update_time

	@Column(name = "company_id")
	private int companyId;

	@Column(name = "bank_id")
	private int bankId;

	@Column(name = "code")
	private String code;

	@Column(name = "method_type")
	private int methodType;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "status")
	private int status;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	@Column(name = "payment_type")
	private int paymentType;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getBankId() {
		return bankId;
	}

	public void setBankId(int bankId) {
		this.bankId = bankId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getMethodType() {
		return methodType;
	}

	public void setMethodType(int methodType) {
		this.methodType = methodType;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(int paymentType) {
		this.paymentType = paymentType;
	}
}
