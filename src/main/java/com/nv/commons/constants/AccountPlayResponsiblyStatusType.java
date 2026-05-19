package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum AccountPlayResponsiblyStatusType implements UniqueValueHolder {
	PENDING(0),
	ACTIVE(1),
	;

	private final int value;

	AccountPlayResponsiblyStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}
}
