package com.nv.commons.constants;

public enum ManagerRoleStatusType {
	INACTIVE(0) {

	},
	ACTIVE(1) {

	};

	private final int value;

	ManagerRoleStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
