package com.nv.commons.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

import java.io.IOException;
import java.io.StringWriter;

public enum WithdrawalSearchType implements UniqueValueHolder, I18nKeyHolder {
	NORMAL(0, "Normal", "form.text.backOffice.withdrawal.searchType.normal"),
	PROCESS(1, "For Process", "form.text.backOffice.withdrawal.searchType.forProcess"),
	APPROVE(2, "For Approved", "form.text.backOffice.withdrawal.searchType.forApproved"),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (WithdrawalSearchType moneyTransactionType : WithdrawalSearchType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(moneyTransactionType.unique()));
				jGenerator.writeStringField("name", moneyTransactionType.name());
				jGenerator.writeStringField("getName", moneyTransactionType.getName());
				jGenerator.writeStringField("getFullName", moneyTransactionType.getFullName());
				jGenerator.writeEndObject();
				jGenerator.writeNumberField(moneyTransactionType.name(), moneyTransactionType.unique());
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

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	private final int value;
	private final String name;
	private final String fullName;

	WithdrawalSearchType(int value, String name, String fullName) {
		this.value = value;
		this.name = name;
		this.fullName = fullName;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getI18nKey() {
		return fullName;
	}

}
