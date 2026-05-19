package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum AccountRemarkType implements UniqueValueHolder {
	userRemark(0),
//	addressRemark(2),
	;

	private final int value;

	public static AccountRemarkType getInstance(int value) {
		for (AccountRemarkType e : AccountRemarkType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	AccountRemarkType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
