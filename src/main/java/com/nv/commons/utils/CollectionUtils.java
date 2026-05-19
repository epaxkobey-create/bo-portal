package com.nv.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectionUtils {

	public static <T> Collector<T, ?, T> singletonCollector() {
		return Collectors.collectingAndThen(
			Collectors.toList(),
			list -> {
				if (list.size() != 1) {
					throw new IllegalStateException();
				}
				return list.get(0);
			}
		);
	}

	/**
	 * 將List分群
	 *
	 * @param size int
	 *             <p>
	 *             list = [1,2,3,4,5,6].groupingBy(2)
	 *             => [[1,2],[3,4],[5,6]]
	 *             <p>
	 *             list = [1,2,3,4,5,6,7].groupingBy(3)
	 *             => [[1,2,3],[4,5,6],[7]]
	 **/
	public static <T> Collector<T, ?, List<List<T>>> groupingBy(int size) {
		final AtomicInteger counter = new AtomicInteger(0);
		return Collectors.collectingAndThen(
			Collectors.groupingBy(o -> counter.getAndIncrement() / size, TreeMap::new, Collectors.toList()),
			m -> new ArrayList<>(m.values())
		);
	}

	@SafeVarargs
	public static <T> Set<T> newHashSet(T... values) {

		return new HashSet<T>() {{
			addAll(Arrays.asList(values));
		}};
	}

	/**
	 *
	 */
	public static <T> T[] reverse(T[] values) {

		List<T> list = Arrays.asList(values);
		// do reverse
		Collections.reverse(list);

		int i = 0;

		for (T value : list) {
			// Array 不支援 generic, 只好 loop re-assign
			values[i++] = value;
		}
		return values;
	}

	public static <K, V> Collector<Map.Entry<K, V>, ?, List<Map<K, V>>> mapSize(int limit) {
		return Collector.of(ArrayList::new,
			(l, e) -> {
				if (l.isEmpty() || l.get(l.size() - 1).size() == limit) {
					l.add(new HashMap<>());
				}
				l.get(l.size() - 1).put(e.getKey(), e.getValue());
			},
			(l1, l2) -> {
				if (l1.isEmpty()) {
					return l2;
				}
				if (l2.isEmpty()) {
					return l1;
				}
				if (l1.get(l1.size() - 1).size() < limit) {
					Map<K, V> map = l1.get(l1.size() - 1);
					ListIterator<Map<K, V>> mapsIte = l2.listIterator(l2.size());
					while (mapsIte.hasPrevious() && map.size() < limit) {
						Iterator<Map.Entry<K, V>> ite = mapsIte.previous().entrySet().iterator();
						while (ite.hasNext() && map.size() < limit) {
							Map.Entry<K, V> entry = ite.next();
							map.put(entry.getKey(), entry.getValue());
							ite.remove();
						}
						if (!ite.hasNext()) {
							mapsIte.remove();
						}
					}
				}
				l1.addAll(l2);
				return l1;
			}
		);
	}
}
