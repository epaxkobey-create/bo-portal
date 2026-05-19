package com.nv.commons.constants;

/**
 * 獎金樣板 賠率盤口
 */
public enum OddsType {

	// 歐洲
	EU(1),
	// 香港
	HK(2),
	// 馬來西亞
	MY(3),
	// 印度
	INDO(4),
	// 美國
	US(5),
	// 緬甸
	MMR(6),

	//
	OTHER(9)
	;
	
	private final int value;
	
	OddsType(int value) {
		this.value = value;
	}
	
	public int unique() {
		return value;
	}

	/*
	 */
	public static OddsType getInstance(int value) {
		for (OddsType e : OddsType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}
}
