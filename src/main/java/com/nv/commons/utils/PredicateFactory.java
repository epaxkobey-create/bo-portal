package com.nv.commons.utils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class PredicateFactory {

	/**
	 * 複合條件
	 * @param <T> Predicate<T>
	 * 
	 * @param allPredicates
	 * @param fullMatch(完全符合 true, 任一條件符合 false)
	 * @return Predicate<BonusTemplate>
	 */
	public static <T> Predicate<T> multiPredicate(boolean fullMatch, List<Predicate<T>> allPredicates) {
		if (fullMatch) {
			return allPredicates.stream().reduce(g -> true, Predicate::and);
		} else {
			return allPredicates.stream().reduce(g -> false, Predicate::or);
		}
		
	}
	
}
