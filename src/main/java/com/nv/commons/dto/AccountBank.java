package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class AccountBank{
	@Column(name= "user_id")
	private String userId;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name ="bank_id")
	private int bankId;

	@Column(name= "bank_name")
	private String bankName;

	@Column(name = "bank_acc_number")
	private String bankAccNumber;

	@Column(name = "bank_acc_name")
	private String bankAccName;

	@Column(name = "bank_branch")
	private String bankBranch;

	@Column(name = "id")
	private int id;

	@Column(name = "extra_data")
	private String extraData;

	@Column(name = "finance_code")
	private String financeCode;

	@Column(name = "remark")
	private String remark;

	@Column(name = "verified_type")
	private int verifiedType;

	@Column(name = "document_id")
	private long documentId = -1;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "document_type")
	private int documentType = -1;

	@Column(name = "is_deleted")
	private int isDeleted;

	// No-argument constructor required by DBQueryRunner
	public AccountBank() {
	}

	public AccountBank(String userId, int websiteType, int bankId, String bankName, String bankAccNumber,
		String bankAccName, String bankBranch, int id, String extraData, String financeCode, String remark,
		int verifiedType, long documentId, Timestamp updateTime, Timestamp createTime, int documentType,
		int isDeleted) {
		this.userId = userId;
		this.websiteType = websiteType;
		this.bankId = bankId;
		this.bankName = bankName;
		this.bankAccNumber = bankAccNumber;
		this.bankAccName = bankAccName;
		this.bankBranch = bankBranch;
		this.id = id;
		this.extraData = extraData;
		this.financeCode = financeCode;
		this.remark = remark;
		this.verifiedType = verifiedType;
		this.documentId = documentId;
		this.updateTime = updateTime;
		this.createTime = createTime;
		this.documentType = documentType;
		this.isDeleted = isDeleted;
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

	public int getBankId() {
		return bankId;
	}

	public void setBankId(int bankId) {
		this.bankId = bankId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankAccNumber() {
		return bankAccNumber;
	}

	public void setBankAccNumber(String bankAccNumber) {
		this.bankAccNumber = bankAccNumber;
	}

	public String getBankAccName() {
		return bankAccName;
	}

	public void setBankAccName(String bankAccName) {
		this.bankAccName = bankAccName;
	}

	public String getBankBranch() {
		return bankBranch;
	}

	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getFinanceCode() {
		return financeCode;
	}

	public void setFinanceCode(String financeCode) {
		this.financeCode = financeCode;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getVerifiedType() {
		return verifiedType;
	}

	public void setVerifiedType(int verifiedType) {
		this.verifiedType = verifiedType;
	}

	public long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public int getDocumentType() {
		return documentType;
	}

	public void setDocumentType(int documentType) {
		this.documentType = documentType;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

}