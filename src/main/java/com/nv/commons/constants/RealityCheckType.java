package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum RealityCheckType implements UniqueValueHolder {
	CHECK_15M(15, "15m"),
	CHECK_30M(30, "30m"),
	CHECK_45M(45, "45m"),
	CHECK_1H(60, "1h"),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (RealityCheckType type : RealityCheckType.values()) {
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


	public static RealityCheckType getInstanceOf(String value) {
		int intValue = Integer.parseInt(value);
		return getInstanceOf(intValue);
	}

	public static RealityCheckType getInstanceOf(int value) {
		for (RealityCheckType e : RealityCheckType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	RealityCheckType(int value, String name) {
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
