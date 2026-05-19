package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum AccountReviewReminderType implements UniqueValueHolder {
	CHECK_1M(1, "1 Month"),
	CHECK_3M(3, "3 Months"),
	CHECK_6M(6, "6 Months"),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (AccountReviewReminderType type : AccountReviewReminderType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(type.unique()));
				jGenerator.writeStringField("name", type.getFullName());
				jGenerator.writeEndObject();
			}

			jGenerator.writeEndObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		json = out.toString();
	}

	public static String toJsonString() {
		return json;
	}

	private final int value;
	private final String name;


	public static AccountReviewReminderType getInstanceOf(String value) {
		int intValue = Integer.parseInt(value);
		return getInstanceOf(intValue);
	}

	public static AccountReviewReminderType getInstanceOf(int value) {
		for (AccountReviewReminderType e : AccountReviewReminderType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	AccountReviewReminderType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int unique() {
		return value;
	}

	public String getFullName() {
		return name;
	}
}
