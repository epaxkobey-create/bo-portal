package com.nv.commons.constants;

public enum WebsiteCurrencySettingType {

	CURRENCY(1) {

	},
	LANGUAGE(2) {

	},
	COUNTRY(3) {

	},
	TIMEZONE(4) {

	},
	MARKETINGGROUP(5) {

	},
	;

	private final int value;

	WebsiteCurrencySettingType(int value) {
		this.value = value;
	}

	public int unique() {
		return this.value;
	}

}
