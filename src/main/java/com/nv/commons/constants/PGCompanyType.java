package com.nv.commons.constants;

public enum PGCompanyType {
	ABCMARTPAY(1, PGCompanyPurposeType.INFLOW),
	;

	private final int value;

	private final PGCompanyPurposeType pgCompanyPurposeType;

	PGCompanyType(int value, PGCompanyPurposeType pgCompanyPurposeType) {
		this.value = value;
		this.pgCompanyPurposeType = pgCompanyPurposeType;
	}

	public static final PGCompanyType[] VALUES = PGCompanyType.values();

	public static PGCompanyType getInstanceOf(int value) {
		for (PGCompanyType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public int unique() {
		return value;
	}

	public PGCompanyPurposeType getPgCompanyPurposeType() {
		return pgCompanyPurposeType;
	}

}
