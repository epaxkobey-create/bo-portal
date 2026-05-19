package com.nv.commons.exceptions;

public class Deviation extends RuntimeException {
	//private String message;

	private String I18Nkey;
	private String[] I18NValues = new String[0];

//	private static final long serialVersionUID = -2588096601668585710L;

	public Deviation() {
		super();
	}

	public Deviation(String s) {
		this.I18Nkey = s;
	}

	public Deviation setI18N(String key, String... values) {
		this.I18Nkey = key;
		if (values != null) {
			this.I18NValues = values;
		}
		return this;
	}

	public String getMessage() {
		if (super.getMessage() != null && super.getMessage().length() > 0) {
			return super.getMessage();
		}
		if (null != this.I18Nkey) {
			return this.I18Nkey;
		}
		return "";
	}

	public String getI18Nkey() {
		if (null != this.I18Nkey) {
			return this.I18Nkey;
		}
		return "";
	}

	public String[] getI18NValues() {
		return this.I18NValues;
	}
}
