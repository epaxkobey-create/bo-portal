package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;

/**
 * MEMO: 這只是 BO 設定 CompanyBank 用的 , 另外 mapping 到 PaymentType
 */
public enum LocalBankType implements UniqueValueHolder {
	REAL_BANK(1, "Real Bank", PaymentType.LOCAL_BANKING, "form.text.backOffice.companyBankPaymentType.REAL_BANK");

	private final int value;
	private final String name;
	private final PaymentType pgPaymentType;
	private final String fullName;

	LocalBankType(int value, String name, PaymentType pgPaymentType, String fullName) {
		this.value = value;
		this.name = name;
		this.pgPaymentType = pgPaymentType;
		this.fullName = fullName;
	}

	public static LocalBankType getInstance(int value) {
		for (LocalBankType e : LocalBankType.values()) {
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

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public PaymentType getPgPaymentType() {
		return pgPaymentType;
	}

	public String getFullName() {
		return fullName;
	}

}
