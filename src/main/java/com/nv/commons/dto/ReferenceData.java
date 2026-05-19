package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class ReferenceData {

	@Column(name = "REFERENCE_KEY")
	private String referenceKey;

	@Column(name = "TEXT")
	private String text;

	@Column(name = "TEXT_ORDER")
	private int textOrder;

	@Column(name = "SETTLE_TIME")
	private Timestamp settleTime;

	@Column(name = "SYSTEM_TXN_STATUS")
	private int systemTxnStatus;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	public String getReferenceKey() {
		return referenceKey;
	}

	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getTextOrder() {
		return textOrder;
	}

	public void setTextOrder(int textOrder) {
		this.textOrder = textOrder;
	}

	public Timestamp getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(Timestamp settleTime) {
		this.settleTime = settleTime;
	}

	public int getSystemTxnStatus() {
		return systemTxnStatus;
	}

	public void setSystemTxnStatus(int systemTxnStatus) {
		this.systemTxnStatus = systemTxnStatus;
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

	public ReferenceData() {
	}

}
