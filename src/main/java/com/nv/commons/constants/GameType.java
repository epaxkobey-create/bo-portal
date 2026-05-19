package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;
import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GameType implements UniqueValueHolder, I18nKeyHolder { // keep
	SLOT(0b0001, "Slot", "global.text.game.type.slot"),    // 1
	CASINO(0b0010, "Casino", "global.text.game.type.casino"),    // 2
	Sport(0b0100, "Sport", "global.text.game.type.sport"),    // 4
	FH(0b1000, "Fish", "global.text.game.type.fh"),    // 8
	CARD(0b10000, "Card", "global.text.game.type.card"),    // 16fe.text.nav.others
	ESport(0b100000, "ESport", "global.text.game.type.esport"),    // 32
	LOTTERY(0b1000000, "Lottery", "global.text.game.type.lottery"),    // 64
	P2P(0b10000000, "P2P", "global.text.game.type.poker"), // 128
	TABLE(0b100000000, "Table", "global.text.game.type.table"), // 256
	OTHERS(0b1000000000, "Others", "global.text.game.type.others"), // 512
	ARCADE(0b10000000000, "Arcade", "global.text.game.type.arcade"), // 1024
	COCK_FIGHTING(0b100000000000, "CockFighting", "global.text.game.type.cockfighting"), // 2048
	RAIN(0b1000000000000, "Rain", "global.text.game.type.rain"), // 4096
	CRASH(0b10000000000000, "Crash", "global.text.game.type.crash") // 8192
	;

	public static final int ALL =
		GameType.SLOT.unique() | GameType.CASINO.unique() | GameType.Sport.unique() | GameType.FH.unique()
			| GameType.CARD.unique() | GameType.ESport.unique() | GameType.LOTTERY.unique() | GameType.P2P.unique()
			| GameType.TABLE.unique() | GameType.OTHERS.unique() | GameType.ARCADE.unique()
			| GameType.COCK_FIGHTING.unique() | GameType.RAIN.unique();

	private final int value;
	private final String shortName;
	private final String fullName;

	GameType(int value, String shortName, String fullName) {
		this.value = value;
		this.shortName = shortName;
		this.fullName = fullName;
	}

	public static GameType getInstance(int value) {
		for (GameType e : GameType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const GameType. value:" + value);
	}

	public static GameType getInstance(String value) {
		for (GameType e : GameType.values()) {
			if (e.shortName.equalsIgnoreCase(value)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const GameType. value:" + value);
	}

	public static List<GameType> getGameTypes(long sum) {
		return Arrays.stream(GameType.values()).filter(gameType -> gameType.in(sum)).collect(Collectors.toList());
	}

	public static String getGameName(long sum, LangMessage langMessage) {
		return getGameTypes(sum).stream().map(game -> game.getFullName(langMessage)).collect(Collectors.joining(","));
	}

	// [1,2,4..]
	public static String getGameTypeIds(long sum) {
		return JSONUtils.toJsonString(
			getGameTypes(sum).stream().map(gameType -> gameType.unique()).collect(Collectors.toList()));
	}

	public static List<GameType> getSortList() {
		List<GameType> sortList = Arrays.stream(GameType.values()).filter(gameType -> gameType != GameType.OTHERS)
			.collect(Collectors.toList());

		sortList.add(GameType.OTHERS);
		return sortList;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return this.name();
	}

	public String getShortName() {
		return shortName;
	}

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	public boolean isSport() {
		return this == Sport;
	}

	public boolean isESport() {
		return this == ESport;
	}

	public boolean isLottery() {
		return this == LOTTERY;
	}

	public boolean isCockFighting() {
		return this == COCK_FIGHTING;
	}

	public boolean in(long sum) {
		return (sum & this.unique()) == this.unique();
	}

	public String getI18nKey() {
		return this.fullName;
	}
}
