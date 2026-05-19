package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum AccountStatusType implements UniqueValueHolder, I18nKeyHolder {
	INACTIVE(0, "label-default", "form.text.backOffice.status.inactive"),//灰
	ACTIVE(1, "label-success", "form.text.backOffice.status.active"),//綠
	SUSPEND(2, "label-warning", "form.text.backOffice.status.suspend"),//黃,停止任何交易
	LOCKED(3, "label-danger", "form.text.backOffice.status.locked")//紅 is password locked
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (AccountStatusType accountStatusType : AccountStatusType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(accountStatusType.unique()));
				jGenerator.writeStringField("name", accountStatusType.name());
				jGenerator.writeStringField("css", accountStatusType.getCss());
				jGenerator.writeStringField("fullName", accountStatusType.getFullName());
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
	private final String css;
	private final String fullName;

	public static AccountStatusType getInstanceOf(int value) {
		for (AccountStatusType e : AccountStatusType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	AccountStatusType(int value, String css, String fullName) {
		this.value = value;
		this.css = css;
		this.fullName = fullName;
	}

	public int unique() {
		return value;
	}

	public String getCss() {
		return css;
	}

	public String getFullName() {
		return fullName;
	}

	public String getI18nKey() {
		return fullName;
	}

	public static List<AccountStatusType> getSortedAccountStatusTypes() {
		return Arrays.stream(AccountStatusType.values())
			.sorted(Comparator.comparing((t) -> (t.getFullName(LanguageType.ENGLISH.getLangMessage())))).toList();
	}
}
