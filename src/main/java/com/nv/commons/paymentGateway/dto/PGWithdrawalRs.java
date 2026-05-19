package com.nv.commons.paymentGateway.dto;

public class PGWithdrawalRs {
	private boolean success;

	private String data;

	private String errorMsg;

	public PGWithdrawalRs() {
		success = true;
		autoApprove = false;
	}

	private boolean autoApprove;

	public boolean isSuccess() {
		return success;
	}

	public String getData() {
		return data;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setError(String errorMsg){
		this.success = false;
		this.errorMsg = errorMsg;
	}

	public void setSuccess(String data){
		this.success = true;
		this.data = data;
	}

	public boolean isAutoApprove() {
		return autoApprove;
	}

	public void setAutoApprove(boolean autoApprove) {
		this.autoApprove = autoApprove;
	}
}
