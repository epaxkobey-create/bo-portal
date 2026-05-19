package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum LoginStatusType implements UniqueValueHolder {

	CORRECT(0) {

	},
	NOT_LOGIN(1) {

	},
	SESSION_TIMEOUT(2) {

	},
	MULTIPLE_LOGIN(3) {

	},

	CLOSED(5) {

	},

	KICK_OUT(7) {

	},

	;
	private final int value;

	LoginStatusType(int value) {
		this.value = value;
	}

	@Override
	public int unique() {
		return value;
	}

}
