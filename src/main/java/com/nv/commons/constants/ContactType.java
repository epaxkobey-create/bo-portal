package com.nv.commons.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.Validator;

import java.io.IOException;
import java.io.StringWriter;

public enum ContactType {

	Email(1) {
		public String getName() {
			return "email";
		}

		public String getDisplayName() {
			return "form.text.contactType.email";
		}

		public AccountUpdateType getAccountUpdateType() {
			return AccountUpdateType.EMAIL;
		}

		public boolean isValidated(String value, CallingCodeType callingCodeType) {
			return Validator.isValidatedEmail(value);
		}

	},

//	Phone(2) {
//		public String getName() {
//			return "phoneNumber";
//		}
//
//		public String getDisplayName() {
//			return "form.text.contactType.phone";
//		}
//
//		public AccountUpdateType getAccountUpdateType() {
//			return AccountUpdateType.PHONE_NUMBER;
//		}
//
//		public boolean isValidated(String value, CallingCodeType callingCodeType) { //valid rule 15, db 20
//			return Validator.isValidatedCellPhone(value, callingCodeType);
//		}
//
//		@Override
//		public boolean isNeedCallingCode() {
//			return true;
//		}
//	},
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (ContactType contactType : ContactType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(contactType.unique()));
				jGenerator.writeStringField("name", contactType.name());
				jGenerator.writeStringField("columnName", contactType.getName());
				jGenerator.writeStringField("displayName", contactType.getDisplayName());
				jGenerator.writeNumberField("updateType", contactType.getAccountUpdateType().unique());
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

	public static final ContactType[] VALUES = ContactType.values();

	public static ContactType getInstanceOf(int value) {
		for (ContactType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	private final int value;

	ContactType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getName();

	public abstract String getDisplayName();

	public abstract AccountUpdateType getAccountUpdateType();

	public abstract boolean isValidated(String value, CallingCodeType callingCodeType);

//	public boolean isSkipDuplicateCheck() {
//		return false;
//	}

//	public boolean isNeedCallingCode() {
//		return false;
//	}
}
