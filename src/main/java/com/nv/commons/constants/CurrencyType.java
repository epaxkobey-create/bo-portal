package com.nv.commons.constants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;

public enum CurrencyType implements I18nKeyHolder {
	EUR(26, "en_US", "€", BigDecimal.ONE) { },
	;

	private final int value;
	private Locale locale;
	private final String currencySymbol;
	private final BigDecimal systemConversion;
	private final String fullName;

	public static final CurrencyType[] VALUES = CurrencyType.values();

	CurrencyType(int value, String localeKey, String currencySymbol, BigDecimal systemConversion) {
		this.value = value;
		this.currencySymbol = currencySymbol;
		this.systemConversion = systemConversion;
		this.fullName = "form.text.currencyType.name." + this.name();

		// 若 localeKey 是帶底線 (_)，代表 getAvailableLocales 已經有了
		for (Locale index : Locale.getAvailableLocales()) {
			if (localeKey.equals(index.toString())) {
				this.locale = index;
				break;
			}
		}

		// create local for special currency
		// 若 localeKey 是帶中線 (-)，代表 getAvailableLocales 內沒有，需要自己另外再 forLanguageTag 一個 Locale 出來
		if (this.locale == null) {
			Locale createLocal = Locale.forLanguageTag(localeKey);
			if (createLocal != null) {
				this.locale = createLocal;
			}
		}
	}

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	public static CurrencyType getInstance(int value) {
		for (CurrencyType e : CurrencyType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const CurrencyType. value:" + value);
	}

	public static CurrencyType getInstance(String value) {
		for (CurrencyType e : CurrencyType.values()) {
			if (e.name().equalsIgnoreCase(value)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const CurrencyType. value:" + value);
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name();
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public BigDecimal getSystemConversion() {
		return systemConversion;
	}

	@Override
	public String getI18nKey() {
		return this.toString();
	}

	public static List<CurrencyType> getAllCurrencyTypeList() {
		return List.of(CurrencyType.VALUES);
	}
}
