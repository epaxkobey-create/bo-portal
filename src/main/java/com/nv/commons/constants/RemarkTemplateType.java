package com.nv.commons.constants;

public enum RemarkTemplateType {

	DEPOSIT(1) {

	},

	WITHDRAWAL(2) {

	},

	ADJUSTMENT(3) {

	},

	FORCE_SERVE(4) {

	},

	DOCUMENT(6) {

	},

	RISK_REMARK(7) {

	},
	;

	private final int value;

	RemarkTemplateType(int value) {
		this.value = value;

	}

	public int unique() {
		return value;
	}

}
