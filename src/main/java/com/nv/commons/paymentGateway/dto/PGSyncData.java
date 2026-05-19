package com.nv.commons.paymentGateway.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
public class PGSyncData {

    // MEMO: orderNo is txnId = moneyTransaction.getId()
    private String orderNo;
    private String referenceNo;
    private Map<String, String> syncData;
    private Date callbackDate = new Date();
	private BigDecimal amount;
	private String currency;
	private String cardNumber;
	private String holderName;
	private String bankName;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public Map<String, String> getSyncData() {
        return syncData;
    }

    public void setSyncData(Map<String, String> syncData) {
        this.syncData = syncData;
    }

    public Date getCallbackDate() {
        return callbackDate;
    }

    public void setCallbackDate(Date callbackDate) {
        this.callbackDate = callbackDate;
    }

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
}
