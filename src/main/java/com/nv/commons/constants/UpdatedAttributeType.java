package com.nv.commons.constants;

/**
 * 投注種類
 *
 * @author user
 */
public enum UpdatedAttributeType {
//	NONE(0b000000),// 無
//	ACCOUNT_IN_GROUPS(0b000001), // 1
//	ACCOUNT_PROVIDER(0b000010), // 2
//	ACCOUNT_INBOX(0b000100), // 4
	ACCOUNT_BANK(0b001000), // 8
	ACCOUNT_CONTACT(0b010000), // 16
	MULTIPLE_TRANSACTION(0b100000), // 32
//	ACCOUNT_BONUS_TURNOVER(0b1000000), // 64
	ACCOUNT_DOCUMENT(0b10000000) // 128
	;

	private final int value;

	UpdatedAttributeType(int value) {
		this.value = value;
	}


	public int unique() {
		return value;
	}

}
