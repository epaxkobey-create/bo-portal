package com.nv.commons.constants;

public enum DocumentGroupType {
	DOCUMENT(0) {
		public String getDisplayName() {
			return "form.text.documentType.document";
		}

	},
	BANK(1) {
		public String getDisplayName() {
			return "form.text.documentType.bankStatement";
		}

	};

	private final int value;

	DocumentGroupType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getDisplayName();

}
