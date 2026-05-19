package com.nv.commons.constants;

public enum GameTxnSummaryType {

	NOT_SUMMARIZED(0),
	SUMMARIZED(1),
	;

	private final int status;

	GameTxnSummaryType(int status) {
		this.status = status;
	}

	public int unique() {
		return this.status;
	}

}
