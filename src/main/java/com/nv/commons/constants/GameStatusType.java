package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

public enum GameStatusType {

	/*
	 * Player 玩到 API 廠商的新遊戲, 但這個遊戲還沒加到 系統裡
	 */
	UNKNOWN(-2) {

		@Override
		public String getName() {
			return "Unknown";
		}
		
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.unknown");
		}
	},
	/*
	 * 遊戲裡的小遊戲, status 會是 Invisible, 這種 game 不會出現在 FE game list 中. 只會出現在 txn report 裡.
	 * 
	 * GameStatusType.INVISIBLE 是某些game拿來對應遊戲交易資料 而不是讓玩家點選進入遊戲用的.
	 * 
	 * @Dean Hsiao
	 */
	INVISIBLE(-1) {

		@Override
		public String getName() {
			return "Invisible";
		}
		
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.invisible");
		}
	},
	/*
	 * UI 不 show
	 */
	INACTIVE(0) {

		@Override
		public String getName() {
			return "Inactive";
		}
		
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.inactive");
		}
	},
	ACTIVE(1) {

		@Override
		public String getName() {
			return "Active";
		}
		
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.active");
		}
	}
	;

	public static final GameStatusType[] VALUES = GameStatusType.values();

	private static final String json;

	static {
		Map<Integer, String> map = new HashMap<Integer, String>();

		for (GameStatusType gameStatusType : VALUES) {
			map.put(gameStatusType.unique(), gameStatusType.getName());
		}
		json = JSONUtils.toJsonString(map);
	}

	private final int value;

	GameStatusType(int value) {
		this.value = value;
	}

	public static GameStatusType getInstanceOf(int value) {
		for (GameStatusType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}
	
	public static String toJsonString(){
		return json;
	}
	
	public int unique() {
		return value;
	}
	
	public abstract String getName();
	public abstract String getDisplayName(LangMessage langMessage);

	public static GameStatusType[] getAll() {
		return VALUES;
	}
}
