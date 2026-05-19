package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;


import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.annotation.Column;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

//import java.util.Date;

public class MoneyTransaction {
//	id
//	user_id
//	website_type
//	pg_ref_id
//	bonus_id
//	type
//	from_bank_id
//	from_bank_number
//	from_bank_account
//	to_bank_id
//	to_bank_number
//	to_bank_account
//	currency
//	amount
//	status
//	proof
//	bonus
//	remark
//	create_date
//	create_userid
//	verified_userid
//	verified_date
//	verified_note
//	approved_userid
//	approved_date
//	approved_note
//	target_vendor
//	multiple
//	note

	
	@Column(name = "id")
	private long id;

	
	@Column(name = "user_id")
	private String userId;

	@Column(name = "website_type")
	private int websiteType;
	
	@Column(name = "vip_level")
	private int vipLevel;

	
	@Column(name = "reference_no")
	private String referenceNo;
	
//	@Column(name = "pg_ref_id")
//	private String pgRefId;

	
	@Column(name = "bonus_id")
	private long bonusId;

	
	@Column(name = "bonus_title")
	private String bonusTitle;

	
	@Column(name = "transaction_type")
	private int transactionType;

	
	@Column(name = "from_bank_id")
	private int fromBankId;

	@Column(name = "from_bank_name")
	private String fromBankName;

	
	@Column(name = "from_bank_number")
	private String fromBankNumber;

	
	@Column(name = "from_bank_account")
	private String fromBankAccount;

	
	@Column(name = "from_bank_branch")
	private String fromBankBranch;

	
	@Column(name = "from_finance_code")
	private String fromFinanceCode;

	
	@Column(name = "to_bank_id")
	private Integer toBankId;

	
	@Column(name = "to_bank_name")
	private String toBankName;
	
	@Column(name = "to_bank_number")
	private String toBankNumber;

	@Column(name = "to_bank_account")
	private String toBankAccount;
	
	@Column(name = "to_bank_branch")
	private String toBankBranch;
	
	@Column(name = "to_finance_code")
	private String toFinanceCode;

	
	@Column(name = "to_payment_type")
	private Integer toPaymentType;

	@Column(name = "currency")
	private String currency;

	
	@Column(name = "amount")
	private BigDecimal amount;

	
	@Column(name = "status")
	private int status;

	@Column(name = "proof")
	private String proof;

	@Column(name = "bonus")
	private BigDecimal bonus;

	@Column(name = "remark")
	private String remark;

	
	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	
	@Column(name = "creator")
	private String creator;

	@Column(name = "verified_userid")
	private String verifiedUserid;

	@Column(name = "verified_time")
	private Timestamp verifiedTime;

	@Column(name = "verified_note")
	private String verifiedNote;

	
	@Column(name = "approved_userid")
	private String approvedUserid;

	
	@Column(name = "approved_time")
	private Timestamp approvedTime;

	@Column(name = "approved_note")
	private String approvedNote;

	@Column(name = "target_vendor")
	private int targetVendor;

	@Column(name = "executor")
	private String executor;
	
	@Column(name = "AMOUNT_FEE")
	private BigDecimal amountFee;

	@Column(name = "BANK_EXTRA_DATA")
	private String bankExtraData;

	@Column(name = "REAL_AMOUNT")
	private BigDecimal realAmount;

	@Column(name = "FEE_ID")
	private long feeId;

	@Column(name = "EXPIRE_TIME")
	private Timestamp expireTime;

	private int expireInterval;

	@Column(name = "AWAITING_VERIFIED_TIME")
	private Timestamp awaitingVerifiedTime;

	private int awaitingInterval;

	@Column(name = "EXCHANGE_AMOUNT")
	private BigDecimal exchangeAmount;

	@Column(name = "EXCHANGE_RATE")
	private BigDecimal exchangeRate;

	@Column(name = "REAL_EXCHANGE_AMOUNT")
	private BigDecimal realExchangeAmount;

	@Column(name = "EXTERNAL_MESSAGE")
	private String externalMessage;

	@Column(name = "transfer_type")
	private int transferType;

	private JsonNode bankExtraDataJsonNode = null;

	private String walletReferenceNo;

	//	public String getPgRefId() {
//		return pgRefId;
//	}
//
//	public void setPgRefId(String pgRefId) {
//		this.pgRefId = pgRefId;
//	}

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

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public long getBonusId() {
		return bonusId;
	}

	public void setBonusId(long bonusId) {
		this.bonusId = bonusId;
	}

	public String getBonusTitle() {
		return bonusTitle;
	}

	public void setBonusTitle(String bonusTitle) {
		this.bonusTitle = bonusTitle;
	}

	public int getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(int transactionType) {
		this.transactionType = transactionType;
	}

	public int getFromBankId() {
		return fromBankId;
	}

	public void setFromBankId(int fromBankId) {
		this.fromBankId = fromBankId;
	}

	public String getFromBankName() {
		return fromBankName;
	}

	public void setFromBankName(String fromBankName) {
		this.fromBankName = fromBankName;
	}

	public String getFromBankNumber() {
		return fromBankNumber;
	}

	public void setFromBankNumber(String fromBankNumber) {
		this.fromBankNumber = fromBankNumber;
	}

	public String getFromBankAccount() {
		return fromBankAccount;
	}

	public void setFromBankAccount(String fromBankAccount) {
		this.fromBankAccount = fromBankAccount;
	}

	public String getFromBankBranch() {
		return fromBankBranch;
	}

	public void setFromBankBranch(String fromBankBranch) {
		this.fromBankBranch = fromBankBranch;
	}

	public String getFromFinanceCode() {
		return fromFinanceCode;
	}

	public void setFromFinanceCode(String fromFinanceCode) {
		this.fromFinanceCode = fromFinanceCode;
	}

	public Integer getToBankId() {
		return toBankId;
	}

	public void setToBankId(Integer toBankId) {
		this.toBankId = toBankId;
	}

	public String getToBankName() {
		return toBankName;
	}

	public void setToBankName(String toBankName) {
		this.toBankName = toBankName;
	}

	public String getToBankNumber() {
		return toBankNumber;
	}

	public void setToBankNumber(String toBankNumber) {
		this.toBankNumber = toBankNumber;
	}

	public String getToBankAccount() {
		return toBankAccount;
	}

	public void setToBankAccount(String toBankAccount) {
		this.toBankAccount = toBankAccount;
	}

	public String getToBankBranch() {
		return toBankBranch;
	}

	public void setToBankBranch(String toBankBranch) {
		this.toBankBranch = toBankBranch;
	}

	public String getToFinanceCode() {
		return toFinanceCode;
	}

	public void setToFinanceCode(String toFinanceCode) {
		this.toFinanceCode = toFinanceCode;
	}

	public Integer getToPaymentType() {
		return toPaymentType;
	}

	public void setToPaymentType(Integer toPaymentType) {
		this.toPaymentType = toPaymentType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getProof() {
		return proof;
	}

	public BigDecimal getBonus() {
		return bonus;
	}

	public void setBonus(BigDecimal bonus) {
		this.bonus = bonus;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getVerifiedUserid() {
		return verifiedUserid;
	}

	public void setVerifiedUserid(String verifiedUserid) {
		this.verifiedUserid = verifiedUserid;
	}

	public Timestamp getVerifiedTime() {
		return verifiedTime;
	}

	public void setVerifiedTime(Timestamp verifiedTime) {
		this.verifiedTime = verifiedTime;
	}

	public String getVerifiedNote() {
		return verifiedNote;
	}

	public void setVerifiedNote(String verifiedNote) {
		this.verifiedNote = verifiedNote;
	}

	public String getApprovedUserid() {
		return approvedUserid;
	}

	public void setApprovedUserid(String approvedUserid) {
		this.approvedUserid = approvedUserid;
	}

	public Timestamp getApprovedTime() {
		return approvedTime;
	}

	public void setApprovedTime(Timestamp approvedTime) {
		this.approvedTime = approvedTime;
	}

	public String getApprovedNote() {
		return approvedNote;
	}

	public void setApprovedNote(String approvedNote) {
		this.approvedNote = approvedNote;
	}

	public int getTargetVendor() {
		return targetVendor;
	}

	public void setTargetVendor(int targetVendor) {
		this.targetVendor = targetVendor;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public BigDecimal getAmountFee() {
		return amountFee;
	}

	public void setAmountFee(BigDecimal amountFee) {
		this.amountFee = amountFee;
	}

	public String getBankExtraData() {
		return bankExtraData;
	}

	public BigDecimal getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(BigDecimal realAmount) {
		this.realAmount = realAmount;
	}

	public long getFeeId() {
		return feeId;
	}

	public void setFeeId(long feeId) {
		this.feeId = feeId;
	}

	public Timestamp getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Timestamp expireTime) {
		this.expireTime = expireTime;
	}

	public int getExpireInterval() {
		return expireInterval;
	}

	public void setExpireInterval(int expireInterval) {
		this.expireInterval = expireInterval;
	}

	public Timestamp getAwaitingVerifiedTime() {
		return awaitingVerifiedTime;
	}

	public void setAwaitingVerifiedTime(Timestamp awaitingVerifiedTime) {
		this.awaitingVerifiedTime = awaitingVerifiedTime;
	}

	public int getAwaitingInterval() {
		return awaitingInterval;
	}

	public void setAwaitingInterval(int awaitingInterval) {
		this.awaitingInterval = awaitingInterval;
	}

	public BigDecimal getExchangeAmount() {
		return exchangeAmount;
	}

	public void setExchangeAmount(BigDecimal exchangeAmount) {
		this.exchangeAmount = exchangeAmount;
	}

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public BigDecimal getRealExchangeAmount() {
		return realExchangeAmount;
	}

	public void setRealExchangeAmount(BigDecimal realExchangeAmount) {
		this.realExchangeAmount = realExchangeAmount;
	}

	public String getExternalMessage() {
		return externalMessage;
	}

	public void setExternalMessage(String externalMessage) {
		this.externalMessage = externalMessage;
	}

	public int getTransferType() {
		return transferType;
	}

	public void setTransferType(int transferType) {
		this.transferType = transferType;
	}

	public String getWalletReferenceNo() {
		if (StringUtils.isEmpty(this.walletReferenceNo)) {
			this.walletReferenceNo = referenceNo;
		}
		return this.walletReferenceNo;
	}

	public void setWalletReferenceNo(String walletReferenceNo) {
		this.walletReferenceNo = walletReferenceNo;
	}

	public void setProof(String proof) {
		if (proof != null && proof.length() > 4000) {
			this.proof = proof.substring(0,4000);
		} else {
			this.proof = proof;
		}
	}

	public void setBankExtraData(String bankExtraData) {
		this.bankExtraData = bankExtraData;
		if(StringUtils.isNotBlank(bankExtraData)){
			try {
				bankExtraDataJsonNode = JSONUtils.getObjectMapper().readTree(bankExtraData);
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}

	public JsonNode getExtraDataJsonNode() {
		return bankExtraDataJsonNode;
	}

}
