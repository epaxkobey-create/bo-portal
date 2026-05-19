package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum KycDocumentStatusType implements UniqueValueHolder {

	UNVERIFIED(0, "Unverified", "label-default"),
	VERIFYING(1, "Verifying", "label-primary"),
	VERIFIED(2, "Verified", "label-success"),
	FAILED(-1, "Failed", "label-danger"),
	;

	private final int value;
	private final String name;
	private final String css;
	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (KycDocumentStatusType kycDocumentStatusType : KycDocumentStatusType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(kycDocumentStatusType.unique()));
				jGenerator.writeStringField("name", kycDocumentStatusType.name());
				jGenerator.writeStringField("columnName", kycDocumentStatusType.getName());
				jGenerator.writeStringField("css", kycDocumentStatusType.getCss());
				jGenerator.writeEndObject();
				jGenerator.writeNumberField(kycDocumentStatusType.name(), kycDocumentStatusType.unique());
			}

			jGenerator.writeArrayFieldStart("order");
			for (DocumentStatusType documentStatusType : DocumentStatusType.values()) {
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

	KycDocumentStatusType(int value, String name, String css) {
		this.value = value;
		this.name = name;
		this.css = css;
	}

	@Override
	public int unique() {
		return value;
	}

	public String getCss() {
		return css;
	}

	public String getName() {
		return name;
	}

	public static String toJsonString() {
		return json;
	}

	public static List<KycDocumentStatusType> getSortedValues() {
		return Arrays.stream(KycDocumentStatusType.values())
			.sorted(Comparator.comparing((t) -> (t.getName()))).toList();
	}

}
