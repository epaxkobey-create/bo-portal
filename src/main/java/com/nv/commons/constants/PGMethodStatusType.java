package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum PGMethodStatusType implements UniqueValueHolder {
	INACTIVE(0),    //灰，忽略
	ACTIVE(1),    //綠，ok
	;

	private final int value;

	PGMethodStatusType(int value) {
		this.value = value;

	}

	public int unique() {
		return value;
	}

}
