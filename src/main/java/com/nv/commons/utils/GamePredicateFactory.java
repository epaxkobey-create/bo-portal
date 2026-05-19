package com.nv.commons.utils;

import java.util.Set;
import java.util.function.Predicate;

import com.nv.commons.dto.Game;

public class GamePredicateFactory extends PredicateFactory {

	public static Predicate<Game> isThisVendor(int vendorId) {
		return game -> game.getVendorId() == vendorId;
	}

	public static Predicate<Game> isThisType(int type) {
		return game -> game.getGameType() == type;
	}

	public static Predicate<Game> isThisPlatformType(int platformType) {
		return game -> (game.getPlatformType() & platformType) > 0;
	}

	public static Predicate<Game> isThoseGame(Set<Integer> gameId) {
		return game -> gameId.contains(game.getId());
	}

	public static Predicate<Game> hasVendorInThisWebsite(Set<Integer> websiteVendorIdSet) {
		return game -> websiteVendorIdSet.contains(game.getVendorId());
	}


}
