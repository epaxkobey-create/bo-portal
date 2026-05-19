package com.nv.commons.constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.JSONUtils;

public enum AccountSummaryReportType {
	BET(0),
	DEPOSIT(1),
	ADJUSTMENT(2),
	WITHDRAWALS(3),
	TURNOVER(9),
	REVENUE_ADJUSTMENT(18),
	;

	private static final AccountSummaryReportType[] VALUES = AccountSummaryReportType.values();

	public static AccountSummaryReportType getInstanceOf(int value) {
		for (AccountSummaryReportType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}
	
	private static final Map<LanguageType, String> jsonNameMap = new ConcurrentHashMap<>();

	public static String toJsonString(LanguageType languageType) {
		String json = jsonNameMap.get(languageType);
		if (json == null) {
			LangMessage i18n = languageType.getLangMessage();

			Map<Integer, String> map = new HashMap<>();
			for (AccountSummaryReportType value : VALUES) {
				map.put(value.unique(), i18n.get("ui.text.report.type." + value.name()));
			}
			json = JSONUtils.toJsonString(map);
			jsonNameMap.put(languageType, json);
		}
		return json;
	}
	
	private final int value;

	AccountSummaryReportType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

}
