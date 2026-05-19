package com.nv.commons.constants;

public enum WebsiteCountrySettingType {

	CURRENCY(1) {

	},
	;

	private final int value;

	WebsiteCountrySettingType(int value) {
		this.value = value;
	}

	public int unique() {
		return this.value;
	}

}
