package com.nv.commons.constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

/**
 * Title: com.nv.commons.constants.BankType<br>
 * Description: 線上銀行類型
 */
public enum BankType implements UniqueValueHolder {
	LOCAL_BANK(0, "BANK_CARD"),
	ONLINE_BANKING(1, "BANK_CARD"),
	CREDIT_CARD(2, "CREDIT_CARD"),
	;

	private final int value;
	private final String category;

	BankType(int value, String category) {
		this.value = value;
		this.category = category;
	}

	public static final BankType[] VALUES = BankType.values();

	public static BankType getInstanceOf(int value) {
		for (BankType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public int unique() {
		return value;
	}

	public String getCategory() {
		return category;
	}

}
