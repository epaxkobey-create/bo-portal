package com.nv.commons.constants;


public enum BetType {

	BONUS_WALLET(0, "Bonus Wallet");

	private final int value;
	private final String name;

	BetType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public static BetType getInstance(int betTypeId) {
		for (BetType betType : BetType.values()) {
			if (betType.unique() == betTypeId) {
				return betType;
			}
		}
		return BONUS_WALLET;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

}
