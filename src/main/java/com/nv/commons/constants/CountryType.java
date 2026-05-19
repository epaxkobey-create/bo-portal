package com.nv.commons.constants;

public enum CountryType {

	// 中國
	CN(1, CallingCodeType.China),
	// 新加坡
	SG(2, CallingCodeType.Singapore),
	// 馬來西亞
	MY(3, CallingCodeType.Malaysia),
	// 越南
	VN(4, CallingCodeType.Vietnam),
	// 韓國
	KR(5, CallingCodeType.SouthKorea),
	// 泰國
	TH(6, CallingCodeType.Thailand),
	// 印尼
	ID(7, CallingCodeType.Indonesia),
	// India, GMT+5:30
	IN(8, CallingCodeType.India),
	// Bangladeshi 孟加拉國 (BD, BGD)
	BD(9, CallingCodeType.Bangladesh),
	// philippines 菲律賓
	PH(10, CallingCodeType.Philippines),
	// pakistan 巴基斯坦
	PK(11, CallingCodeType.Pakistan),
	// Mexico 墨西哥(墨西哥披索)
	MX(12, CallingCodeType.Mexico),
	// US 美國(美金)
	US(13, CallingCodeType.US),
	// BR 巴西
	BR(14, CallingCodeType.Brazil),
	// KH 柬埔寨
	KH(15, CallingCodeType.Cambodia),
	// NG 奈及利亞
	NG(16, CallingCodeType.Nigeria),
	// ZA 南非
	ZA(17, CallingCodeType.ZA),
	// GH 迦納
	GH(18, CallingCodeType.GH),
	// LK 斯里蘭卡
	LK(19, CallingCodeType.SriLanka),
	// NP 尼泊爾
	NP(20, CallingCodeType.Nepal),
	// AU 澳洲
	AU(21, CallingCodeType.Australia),
	// NZ 紐西蘭
	NZ(22, CallingCodeType.NewZealand),
	// AF 阿富汗
	AFG(23, CallingCodeType.Afghanistan),
	// BT 不丹
	BT(24, CallingCodeType.Bhutan),
	// MV 馬爾地夫
	MV(25, CallingCodeType.Maldives),
	//IR 伊朗
	IR(26, CallingCodeType.Iran),
	// CA 加拿大
	CA(27, CallingCodeType.Canada),
	// HK 香港
	HK(28, CallingCodeType.HongKong),
	// MT Malta
	MT(29, CallingCodeType.Malta),

	UK(30, CallingCodeType.UK),

	CY(31, CallingCodeType.CYPRUS)

	// Please Note : Add url-pattern into web.xml
	;

	private final int value;
	private final CallingCodeType callingCodeType;

	CountryType(int value, CallingCodeType callingCodeType) {
		this.value = value;
		this.callingCodeType = callingCodeType;
	}

	public int unique() {
		return value;
	}

	public String getCallingCode() {
		return callingCodeType.unique();
	}

	public static CountryType getInstance(int value) {
		for (CountryType e : CountryType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public static CountryType getInstance(String countryShortName) {
		for (CountryType e : CountryType.values()) {
			if (e.name().equalsIgnoreCase(countryShortName)) {
				return e;
			}
		}
		return null;
	}

	public static CountryType getInstanceByCallingCode(String callingCode) {
		for (CountryType e : CountryType.values()) {
			if (e.getCallingCode().equals(callingCode)) {
				return e;
			}
		}
		return null;
	}

}
