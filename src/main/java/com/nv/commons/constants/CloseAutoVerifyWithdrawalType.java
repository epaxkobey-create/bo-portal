package com.nv.commons.constants;

public enum CloseAutoVerifyWithdrawalType {

	ACCOUNTSTATUS(1),
	;

	private final int value;

	CloseAutoVerifyWithdrawalType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
