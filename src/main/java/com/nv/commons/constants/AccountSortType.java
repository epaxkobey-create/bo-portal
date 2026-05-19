package com.nv.commons.constants;

public enum AccountSortType {

	CREATE_TIME(0, "create_time"),
	EMAIL(1, "email"),
	FULL_NAME(2, "full_name"),
	BIRTHDAY(3, "birthday"),
	STATUS(4, "status"),
	VERIFICATION_STATUS(5, "verification_status"),
	PHONE_NUMBER(15, "phone_number"),
	CURRENCY_TYPE_ID(18, "currency_type_id"),
	LOGIN_TIME(6, "login_time"),
	;

	private final int value;

	private final String sortCondition;

	AccountSortType(int value, String sortCondition){
		this.value = value;
		this.sortCondition = sortCondition;
	}

	public static AccountSortType getInstanceOf(int value){
		for (AccountSortType accountSortType : AccountSortType.values()) {
			if (accountSortType.value == value){
				return accountSortType;
			}
		}
		return CREATE_TIME;
	}

	public int getValue() {
		return value;
	}

	public String getSortCondition(){
		return sortCondition;
	}
}
