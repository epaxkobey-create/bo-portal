package com.nv.commons.constants;

public enum ManagerStatusType {
	INACTIVE(0) {

	},
	ACTIVE(1) {

	};

	public static final ManagerStatusType[] VALUES = ManagerStatusType.values();

	public static ManagerStatusType getInstanceOf(int value) {
		for (ManagerStatusType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	private final int value;

	ManagerStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
