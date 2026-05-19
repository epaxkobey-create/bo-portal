package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum TokenRecordStatusType implements UniqueValueHolder {
	INACTIVE(0),
	ACTIVE(1),
	EXPIRED(2);

	private final int value;

	TokenRecordStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}
}
