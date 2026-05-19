package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum DocumentStatusType implements UniqueValueHolder {

	NO_RECORD(0, "No Record", "label-default", "No Record", "global.text.documentStatusType.NO_RECORD"),
	PENDING(1, "Pending", "label-primary", "Create", "global.text.documentStatusType.PENDING"),
	APPROVED(2, "Approved", "label-success", "Approve", "global.text.documentStatusType.APPROVED"),
	ON_HOLD(3, "On Hold", "label-hold", "Hold", "global.text.documentStatusType.ON_HOLD"), CREATED(5, "Created", "label-default", "Create", "global.text.documentStatusType.CREATED"),
	REJECTED(-1, "Rejected", "label-danger", "Disapproved", "global.text.documentStatusType.REJECTED"),
	REMOVED(-2, "Removed", "label-revert", "Removed", "global.text.documentStatusType.REMOVED"),
	;

	private static String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for(DocumentStatusType documentStatusType : DocumentStatusType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(documentStatusType.unique()));
				jGenerator.writeStringField("name", documentStatusType.name());
				jGenerator.writeStringField("columnName", documentStatusType.getName());
				jGenerator.writeStringField("css", documentStatusType.getCss());
				jGenerator.writeStringField("action", documentStatusType.getAction());
				jGenerator.writeStringField("fullName", documentStatusType.getFullName());
				jGenerator.writeEndObject();
				jGenerator.writeNumberField(documentStatusType.name(), documentStatusType.unique());
			}

			jGenerator.writeArrayFieldStart("order");
			for(DocumentStatusType documentStatusType : DocumentStatusType.values()) {
				jGenerator.writeNumber(documentStatusType.unique());
			}

			jGenerator.writeEndArray();
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
	private final String css;
	private final String action;
	private final String fullName;

	DocumentStatusType(int value,  String name, String css, String action, String fullName) {
		this.value = value;
		this.name = name;
		this.css = css;
		this.action = action;
		this.fullName = fullName;
	}

	public static DocumentStatusType getInstance(int value) {
		for (DocumentStatusType e : DocumentStatusType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const DocumentStatusType. value:" + value);
	}

	@Override
	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getCss() {
		return css;
	}

	public String getAction() {
		return action;
	}

	public String getFullName() {
		return fullName;
	}

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

}
