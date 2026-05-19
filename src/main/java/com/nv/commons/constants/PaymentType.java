package com.nv.commons.constants;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Title: com.nv.commons.constants.PGAccountActionType<br>
 * Description: 線上支付類型
 *
 */
public enum PaymentType implements UniqueValueHolder {
	NOT_FOUND(-1, "Not Found", "none", null),
	LOCAL_BANKING(0, "LOCAL BANKING", "ui.text.paymentType.localBanking", BankType.LOCAL_BANK),
	ONLINE_BANKING(1, "ONLINE BANKING", "ui.text.paymentType.onlineBanking", BankType.ONLINE_BANKING),
	CREDIT_CARD(2, "CREDIT_CARD BANKING", "ui.text.paymentType.creditCard", BankType.CREDIT_CARD),
	;

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (PaymentType type : PaymentType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(type.unique()));
				jGenerator.writeStringField("name", type.name());
				jGenerator.writeStringField("getName", type.getName());
				jGenerator.writeStringField("getFullName", type.getFullName());
//				jGenerator.writeNumberField("getBankType", type.getBankType().unique());
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

	public static final PaymentType[] VALUES = PaymentType.values();

	private final int value;
	private final String name;
	private final String frontName;
	private final BankType bankType;

	PaymentType(int value, String name, String frontName, BankType bankType) {
		this.value = value;
		this.name = name;
		this.frontName = frontName;
		this.bankType = bankType;
	}

	public static PaymentType getInstanceOf(int value) {
		for (PaymentType e : VALUES) {
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
		return langMessage.get(frontName);
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public BankType getBankType() {
		return bankType;
	}

	public String getFullName() {
		return frontName;
	}

}
