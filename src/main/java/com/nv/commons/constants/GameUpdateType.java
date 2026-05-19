package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum GameUpdateType implements UniqueValueHolder {

	STATUS(1, "status", "Status",  "form.text.backOffice.status"),
	DISPLAY_ORDER (2, "displayOrder", "Display Order", "form.text.backOffice.message.displayOrder"),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (GameUpdateType accountUpdateType : GameUpdateType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(accountUpdateType.unique()));
				jGenerator.writeStringField("name", accountUpdateType.name());
				jGenerator.writeStringField("getName", accountUpdateType.getName());
				jGenerator.writeStringField("getDisplayName", accountUpdateType.getDisplayName());
				jGenerator.writeStringField("getFullName", accountUpdateType.getFullName());
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
	private final String displayName;
	private final String fullName;

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	GameUpdateType(int value, String name, String displayName,
		String fullName) {
		this.value = value;
		this.name = name;
		this.displayName = displayName;
		this.fullName = fullName;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFullName() {
		return fullName;
	}

}
