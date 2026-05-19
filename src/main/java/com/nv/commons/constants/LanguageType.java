package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;

import java.util.concurrent.ConcurrentHashMap;

public enum LanguageType {

	ENGLISH(1, "en") {

	},
//	CHINESE(2, "cn") {
//
//	},
//	VN(4, "vn") {
//
//	},
//	KOREA(5, "kr") {
//
//	},
//	THAI(6, "th") {
//
//	},
//	ID(7, "id") {
//
//	},

	;

	private static final ConcurrentHashMap<String, LangMessage> LANG_MESSAGE_MAP = new ConcurrentHashMap<>();

	public static final LanguageType[] BACKOFFICELANGUAGE = {
//		LanguageType.CHINESE,
		LanguageType.ENGLISH,
//		LanguageType.KOREA, LanguageType.THAI
	};

	protected final String resourceKey;

	public static LanguageType getInstance(int id) {
		for (LanguageType e : LanguageType.values()) {
			if (e.id == id) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const LanguageType. id:" + id);
	}

	private final int id;

	LanguageType(int id, String resourceKey) {
		this.resourceKey = resourceKey;
		this.id = id;
	}

	public static LanguageType getInstance(String languageResourceKey) {
		for (LanguageType e : LanguageType.values()) {
			if (e.getLanguageResourceKey().equalsIgnoreCase(languageResourceKey)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const LanguageType. languageResourceKey: " + languageResourceKey);
	}

	public int unique() {
		return id;
	}

	public String getLanguageResourceKey() {
		return resourceKey;
	}

	public boolean isChinese() {
		return false;
//			this == LanguageType.CHINESE;
	}

	public LangMessage getLangMessage() {
		return LANG_MESSAGE_MAP.computeIfAbsent(this.resourceKey, key -> new LangMessage("message_" + key));
	}

}
