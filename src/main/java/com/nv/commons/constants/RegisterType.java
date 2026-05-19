package com.nv.commons.constants;

public enum RegisterType {

	ACCOUNT(0);

	private final int value;

	RegisterType(int value) {
		this.value = value;

	}

	public int unique() {
		return value;
	}

}
