package com.nv.commons.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.JSONUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum DocumentType {

	ID(1) {
		public String getName() {
			return "id";
		}

		public String getDisplayName() {
			return "form.text.documentType.id";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}
	},

	HEADSHOT(2) {
		public String getName() {
			return "headshot";
		}

		public String getDisplayName() {
			return "form.text.documentType.headshot";
		}

		@Override
		public String getFrontFieldName() {
			return "headshotImage";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}

	},

	UTILITIES_BILL(4) {
		public String getName() {
			return "Utilities Bill";
		}

		public String getDisplayName() {
			return "form.text.documentType.utilities_bill";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}

	},

	ADDRESS_PROOF(8) {
		public String getName() {
			return "address proof";
		}

		public String getDisplayName() {
			return "form.text.documentType.addressProof";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}
	},

	SUMSUB_KYC(16) {
		public String getName() {
			return "sumsubKYC";
		}

		public String getDisplayName() {
			return "form.text.documentType.sumsubKYC";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}
	},

	DOCUMENT(9999999) {
		public String getName() {
			return "document";
		}

		public String getDisplayName() {
			return "form.text.documentType.document";
		}

		public int getGroupType() {
			return DocumentGroupType.DOCUMENT.unique();
		}
	};

	private static String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (DocumentType contactType : DocumentType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(contactType.unique()));
				jGenerator.writeStringField("name", contactType.name());
				jGenerator.writeStringField("columnName", contactType.getName());
				jGenerator.writeStringField("displayName", contactType.getDisplayName());
				jGenerator.writeNumberField("groupType", contactType.getGroupType());
				jGenerator.writeEndObject();
			}

			jGenerator.writeArrayFieldStart("order");
			for (DocumentType documentType : DocumentType.values()) {
				jGenerator.writeNumber(documentType.unique());
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

	public static final DocumentType[] VALUES = new DocumentType[]{ID, HEADSHOT, UTILITIES_BILL, ADDRESS_PROOF,
		SUMSUB_KYC,};

	public static final DocumentType[] BASIC_VALUES = new DocumentType[]{ID,};

	public static DocumentType getInstance(int value) {
		for (DocumentType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	private final int value;

	DocumentType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getName();

	public abstract String getDisplayName();

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(getDisplayName()).replace("\\", "");
	}

	public abstract int getGroupType();

	public static DocumentType[] getDocumentTypesByCurrency() {
		DocumentType[] documentTypes = {};
		documentTypes = Stream.of(documentTypes, BASIC_VALUES).flatMap(Arrays::stream).distinct()
			.toArray(DocumentType[]::new);
		return documentTypes;
	}

	public static List<Integer> getDocumentTypesUnique(int sum) {
		List<Integer> result = new ArrayList<>();
		for (DocumentType type : VALUES) {
			if ((sum & type.unique()) == type.unique()) {
				result.add(type.unique());
			}
		}
		return result;
	}

	public String getFrontFieldName() {
		return "documentFrontImage";
	}

	public String getBackFieldName() {
		return "documentBackImage";
	}

}
