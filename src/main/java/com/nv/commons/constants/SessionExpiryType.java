package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum SessionExpiryType implements UniqueValueHolder {
	NONE(0, "None"),
	_15M(15, "15m"),
	_30M(30, "30m"),
	_45M(45, "45m"),
	_1H(60, "1h"),
	_3H(180, "3h"),
	_6H(360, "6h"),
	_12H(720, "12h"),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (SessionExpiryType type : SessionExpiryType.values()) {
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

	public static SessionExpiryType getInstanceOf(String value) {
		for (SessionExpiryType e : SessionExpiryType.values()) {
			if (e.value == Integer.parseInt(value)) {
				return e;
			}
		}
		return null;
	}

	SessionExpiryType(int value, String name) {
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
