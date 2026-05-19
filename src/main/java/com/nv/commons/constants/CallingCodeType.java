package com.nv.commons.constants;

public enum CallingCodeType {

	Afghanistan("93"),
	Malaysia("60"),
	Maldives("960"),
	Bangladesh("880"),
	Bhutan("975"),
	Nepal("977"),
	Brunei("673"),
	Myanmar("95"),
	Pakistan("92"),
	Cambodia("855"),
	China("86"),
	Philippines("63"),
	Singapore("65"),
	HongKong("852"),
	SouthKorea("82"),
	India("91"),
	Indonesia("62"),
	SriLanka("94"),
	Iran("98"),
	Thailand("66"),
	Vietnam("84"),
	Laos("856"),
	Mexico("52"),
	US("1"),
	Brazil("55"),
	Nigeria("234"),
	ZA("27"),
	GH("233"),
	Australia("61"),
	NewZealand("64"),
	Canada("1"),
	Malta("356"),
	UK("44"),
	CYPRUS("357")
	;

	private final String code;

	CallingCodeType(String code) {
		this.code = code;
	}

	public String unique() {
		return this.code;
	}

	public static CallingCodeType getInstance(String code) {
		for (CallingCodeType type : CallingCodeType.values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("CallingCodeType:" + code + " not found");
	}

}
