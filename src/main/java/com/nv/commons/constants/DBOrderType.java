package com.nv.commons.constants;

public enum DBOrderType {

	ASC(0) {
		public String getSqlString() {
			return " ASC";
		}
	},
	DESC(1) {
		public String getSqlString() {
			return " DESC";
		}
	};

	private final int value;

	public static final DBOrderType[] VALUES = DBOrderType.values();

	public static DBOrderType getInstanceOf(int value) {
		for (DBOrderType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	DBOrderType(int value) {
		this.value = value;
	}

	public abstract String getSqlString();
}
