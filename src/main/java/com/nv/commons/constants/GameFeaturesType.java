package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

/**
 * @author Neutec
 */
public enum GameFeaturesType implements UniqueValueHolder {

	JACKPOT(0b0001),
	FREESPIN(0b0010);

	private final int value;

	GameFeaturesType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public boolean in(long sum) {
		return (sum & this.unique()) == this.unique();
	}

}
