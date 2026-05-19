package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum JwtTokenType implements UniqueValueHolder {

	RefreshToken(0),
	AccessToken(1),
	PreLoginToken(2),
	;

	JwtTokenType(int value) {
		this.value = value;
	}

	final int value;

	@Override
	public int unique() {
		return value;
	}

	public static JwtTokenType getInstance(int tokenTypeId) {
		for (JwtTokenType type : JwtTokenType.values()) {
			if (type.unique() == tokenTypeId) {
				return type;
			}
		}
		return null;
	}

}

