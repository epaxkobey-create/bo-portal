package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum PeriodType implements UniqueValueHolder {

	INDEFINITE(0) {
		@Override
		public String getName() {
			return "Indefinite";
		}

	},
	DAILY(1) {
		@Override
		public String getName() {
			return "Daily";
		}

	},
	WEEKLY(2) {
		@Override
		public String getName() {
			return "Weekly";
		}

	},
	MONTHLY(3) {
		@Override
		public String getName() {
			return "Monthly";
		}

	};

	public static final PeriodType[] VALUES = PeriodType.values();

	public static PeriodType getInstanceOf(int value) {
		for (PeriodType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const PeriodType. value:" + value);
	}

	private final int value;

	PeriodType(int value) {
		this.value = value;
	}

	public abstract String getName();

	public int unique() {
		return value;
	}
}
