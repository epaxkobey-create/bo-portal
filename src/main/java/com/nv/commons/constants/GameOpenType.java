package com.nv.commons.constants;

public enum GameOpenType {
	//for FE api used
	NORMAL(1), //url
	HTML(3), //html
	;

	private final int value;

	GameOpenType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
