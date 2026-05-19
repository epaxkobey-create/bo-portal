package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum TimeSpentLimitType implements UniqueValueHolder {
	NO_TIME_SPENT_LIMIT(0, "-","No Time Spent Limit"),
	_1H(1, "1h","1 Hour"),
	_2H(2, "2h","2 Hours"),
	_3H(3, "3h","3 Hours"),
	_4H(4, "4h","4 Hours"),
	_5H(5, "5h","5 Hours"),
	_6H(6, "6h","6 Hours"),
	_7H(7, "7h","7 Hours"),
	_8H(8, "8h","8 Hours");

	private final int value;
	private final String name;
	private final String description;
	private static String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (TimeSpentLimitType type : TimeSpentLimitType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(type.unique()));
				jGenerator.writeStringField("name", type.getName());
				jGenerator.writeStringField("description", type.getDescription());
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

	public static TimeSpentLimitType getInstanceOf(String value) {
		int intValue = Integer.parseInt(value);
		return getInstanceOf(intValue);
	}

	public static TimeSpentLimitType getInstanceOf(int value) {
		for (TimeSpentLimitType e : TimeSpentLimitType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public static String toJsonString() {
		return json;
	}

	TimeSpentLimitType(int value, String name, String description) {
		this.value = value;
		this.name = name;
		this.description = description;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
