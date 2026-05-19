package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;

public enum AccountPlayResponsiblyPeriodType implements UniqueValueHolder, I18nKeyHolder {
	DAILY(1, "form.text.backOffice.setting.playResponsibly.daily"),
	WEEKLY(2, "form.text.backOffice.setting.playResponsibly.weekly"),
	MONTHLY(3, "form.text.backOffice.setting.playResponsibly.monthly"),
	;

	private final int value;
	private final String name;

	public static AccountPlayResponsiblyPeriodType getInstanceOf(int value) {
		for (AccountPlayResponsiblyPeriodType e : AccountPlayResponsiblyPeriodType.values()) {
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

		return langMessage.get(name);
	}

	AccountPlayResponsiblyPeriodType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int unique() {
		return value;
	}

	public String getI18nKey() {
		return name;
	}
}
