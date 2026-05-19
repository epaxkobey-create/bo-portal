package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum PGCompanyPurposeType implements UniqueValueHolder {
	INFLOW(1),//錢流入公司
	OUTFLOW(2)//錢從公司流出
	;

	private final int value;

	PGCompanyPurposeType(int value) {
		this.value = value;

	}

	public int unique() {
		return value;
	}

}
