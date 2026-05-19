package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum BinaryStatusType implements UniqueValueHolder {
	INACTIVE(0),
	ACTIVE(1);

	private final int value;

	BinaryStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}
}
