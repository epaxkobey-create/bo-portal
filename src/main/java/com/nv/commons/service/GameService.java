package com.nv.commons.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.nv.commons.cache.GameCache;
import com.nv.commons.constants.GameStatusType;
import com.nv.commons.constants.GameType;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.utils.GamePredicateFactory;

public class GameService {

	// === Sort types ===
	public enum SortType {
		RECOMMENDED,
		LATEST,
		NAME,
		FAVORITE
	}

	// === Common comparators ===
	// Display order: ascending (lower first)
	private static final Comparator<Game> comparatorByDisplayOrder = Comparator
		.comparing(Game::getDisplayOrder, Comparator.nullsLast(Integer::compareTo));

	// English name: ascending (A → Z, case-insensitive, null-safe)
	private static final Comparator<Game> comparatorByNameEn = Comparator
		.comparing(Game::getNameEn, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

	// Latest: descending (newer first, fallback by name)
	private static final Comparator<Game> comparatorByLatest = Comparator
		.comparing(Game::getUpdateTime, Comparator.nullsLast(Date::compareTo))
		.reversed();

	private static final Map<SortType, Comparator<Game>> COMPARATOR_REGISTRY = new EnumMap<>(SortType.class);

	static {
		COMPARATOR_REGISTRY.put(SortType.RECOMMENDED, comparatorByDisplayOrder.thenComparing(comparatorByNameEn));
		COMPARATOR_REGISTRY.put(SortType.LATEST, comparatorByLatest.thenComparing(comparatorByNameEn));
		COMPARATOR_REGISTRY.put(SortType.NAME, comparatorByNameEn);
	}

	public static List<Game> findGamesByVendorAndType(
		List<WebsiteVendor> websiteVendors,
		GameType gameType,
		SortType sortType,
		List<Integer> favoriteGameList
	) {
		List<Game> allGames = new ArrayList<>();

		for (WebsiteVendor websiteVendor : websiteVendors) {

			List<Predicate<Game>> gamePredicates = new ArrayList<>();
			gamePredicates.add(GamePredicateFactory.isThisType(gameType.unique()));
			gamePredicates.add(GamePredicateFactory.isThisVendor(websiteVendor.getVendorId()));
			allGames.addAll(
				GameCache.getInstance()
					.getGames(GamePredicateFactory.multiPredicate(true, gamePredicates))
			);
		}

		Comparator<Game> comparator;
		if (sortType == SortType.FAVORITE) {
			comparator = getFavoriteGameComparator(favoriteGameList).thenComparing(comparatorByNameEn);
		} else {
			comparator = COMPARATOR_REGISTRY.getOrDefault(sortType, comparatorByNameEn);
		}

		return allGames.stream()
			.filter(game -> game.getStatus() == GameStatusType.ACTIVE.unique())
			.distinct()
			.sorted(comparator)
			.collect(Collectors.toList());
	}

	public static List<Game> findRecommendedGames(List<WebsiteVendor> websiteVendors, GameType gameType) {
		return findGamesByVendorAndType(websiteVendors, gameType, SortType.RECOMMENDED, null);
	}

	public static List<Game> findLatestGames(List<WebsiteVendor> websiteVendors, GameType gameType) {
		return findGamesByVendorAndType(websiteVendors, gameType, SortType.LATEST, null);
	}

	public static List<Game> findGamesSortedByName(List<WebsiteVendor> websiteVendors, GameType gameType) {
		return findGamesByVendorAndType(websiteVendors, gameType, SortType.NAME, null);
	}

	public static List<Game> findFavoriteGames(List<WebsiteVendor> websiteVendors, GameType gameType,
		List<Integer> favoriteGameList) {
		return findGamesByVendorAndType(websiteVendors, gameType, SortType.FAVORITE, favoriteGameList);
	}

	private static Comparator<Game> getFavoriteGameComparator(List<Integer> favoriteGameList) {

		Set<Integer> favoriteSet = favoriteGameList == null ? Set.of() : new HashSet<>(favoriteGameList);

		return Comparator
			.comparing((Game g) -> !favoriteSet.contains(g.getId()))
			.thenComparing(comparatorByNameEn);
	}

	public static Comparator<Game> generateGameComparator(String sortName) {
		Comparator<Game> gameComparator;
		if ("latest".equalsIgnoreCase(sortName)) {
			gameComparator = COMPARATOR_REGISTRY.getOrDefault(SortType.LATEST, comparatorByNameEn);
		} else if ("az".equalsIgnoreCase(sortName)) {
			gameComparator = COMPARATOR_REGISTRY.getOrDefault(SortType.NAME, comparatorByNameEn);
		} else {
			gameComparator = COMPARATOR_REGISTRY.getOrDefault(SortType.RECOMMENDED, comparatorByNameEn);
		}

		return gameComparator;
	}

}