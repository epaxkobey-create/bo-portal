package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum MessageRoleType implements UniqueValueHolder {
	GUEST(1),
	PLAYER(2);

	private final int value;

	MessageRoleType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
