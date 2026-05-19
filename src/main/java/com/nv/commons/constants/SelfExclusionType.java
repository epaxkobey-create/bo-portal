package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum SelfExclusionType implements UniqueValueHolder {
	NO_EXCLUSION(0, "No Exclusion"),
	_7D(7, "7d (1 Week)"),
	_14D(14, "14d (2 Weeks)"),
	_30D(30, "30d (1 Month)"),
	_90D(90, "90d (3 Months)"),
	_180D(180, "180d (6 Months)"),
	_270D(270, "270d (9 Months)"),
	_365D(365, "365d (1 Year)"),
	INDEFINITE(-1, "Indefinite"),
	;

	private static String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (SelfExclusionType type : SelfExclusionType.values()) {
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

	public static SelfExclusionType getInstanceOf(String value) {
		for (SelfExclusionType e : SelfExclusionType.values()) {
			if (e.value == Integer.parseInt(value)) {
				return e;
			}
		}
		return null;
	}

	SelfExclusionType(int value, String name) {
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
