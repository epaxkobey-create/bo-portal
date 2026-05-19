package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum MoneyTransactionType implements UniqueValueHolder, I18nKeyHolder {
	DEPOSIT(0, "Deposit", "global.text.moneyTransactionType.DEPOSIT"),
	WITHDRAWALS(1, "Withdrawal", "global.text.moneyTransactionType.WITHDRAWALS"),
	DEPOSIT_PAYMENT_GATEWAY(2, "Deposit", "global.text.moneyTransactionType.DEPOSIT_PAYMENT_GATEWAY"),
	ADJUSTMENT(3, "Adjustment", "global.text.moneyTransactionType.ADJUSTMENT"),
	WITHDRAWAL_PAYMENT_GATEWAY(4, "Withdrawal",
		"global.text.moneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY"),
	REVENUE_ADJUSTMENT(5, "Revenue Adjustment", "global.text.moneyTransactionType.REVENUE_ADJUSTMENT"),
	;
	//	"交易類型
	//	0=Deposit,
	//	1=Withdrawal,
	//	2=Payment Gateway,
	//	3=Adjustment, "

	private static final String json;

	private static final MoneyTransactionType[] ADJUSTMENT_TYPE = new MoneyTransactionType[] {
		ADJUSTMENT, REVENUE_ADJUSTMENT
	};
	private static final MoneyTransactionType[] DEPOSIT_TYPE = new MoneyTransactionType[] {
		DEPOSIT, DEPOSIT_PAYMENT_GATEWAY
	};
	private static final MoneyTransactionType[] WITHDRAWAL_TYPE = new MoneyTransactionType[] {
		WITHDRAWALS, WITHDRAWAL_PAYMENT_GATEWAY
	};

	public static final int[] ADJUSTMENT_TYPE_UNIQUE = Arrays.stream(ADJUSTMENT_TYPE)
		.mapToInt(MoneyTransactionType::unique).toArray();
	public static final int[] DEPOSIT_TYPE_UNIQUE = Arrays.stream(DEPOSIT_TYPE)
		.mapToInt(MoneyTransactionType::unique).toArray();
	public static final int[] WITHDRAWAL_TYPE_UNIQUE = Arrays.stream(WITHDRAWAL_TYPE)
		.mapToInt(MoneyTransactionType::unique).toArray();

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (MoneyTransactionType moneyTransactionType : MoneyTransactionType.values()) {
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

	public String getAdjustmentTypeName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get("global.text.adjustmentType." + name());
	}

	private final int value;
	private final String name;
	private final String fullName;

	MoneyTransactionType(int value, String name, String fullName) {
		this.value = value;
		this.name = name;
		this.fullName = fullName;
	}

	public static MoneyTransactionType getInstance(int value) {
		for (MoneyTransactionType e : MoneyTransactionType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const MoneyTransactionType. value:" + value);
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

	public static MoneyTransactionType[] getAdjustmentType() {
		return ADJUSTMENT_TYPE;
	}

	public static MoneyTransactionType[] getDepositType() {
		return DEPOSIT_TYPE;
	}

	public static MoneyTransactionType[] getWithdrawalType() {
		return WITHDRAWAL_TYPE;
	}

	public static int[] getTransactionTypeUniqueByName(String name) {
		return switch (name) {
			case "ADJUSTMENT" -> ADJUSTMENT_TYPE_UNIQUE;
			case "DEPOSIT" -> DEPOSIT_TYPE_UNIQUE;
			case "WITHDRAWAL" -> WITHDRAWAL_TYPE_UNIQUE;
			default -> new int[0];
		};
	}
}
