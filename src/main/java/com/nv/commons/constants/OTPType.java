package com.nv.commons.constants;

public enum OTPType {
	FORGOT_PASSWORD_EMAIL(4),
	;

	private final int value;

	OTPType(int value) {
		this.value = value;
	}

	public static OTPType getInstance(int value) {
		for (OTPType e : OTPType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const OTPType. value:" + value);
	}

	public int unique() {
		return value;
	}

}