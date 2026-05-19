package com.nv.commons.constants;

public enum MessageStatusType {
	INACTIVE(0) {

	},
	ACTIVE(1) {

	};

	private final int value;

	MessageStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
