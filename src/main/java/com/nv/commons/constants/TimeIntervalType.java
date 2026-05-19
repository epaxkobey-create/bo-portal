package com.nv.commons.constants;

import java.util.HashMap;
import java.util.Map;

public class TimeIntervalType {

	private static final int MAX = 3;
	private static final int HALF_HOUR_MINUTES = 30;
	private static final int HOUR_MINUTES = 60;

	private final int value;

	private final int[] interval;

	private static final Map<Integer, TimeIntervalType> typeMap = new HashMap<>();

	static {
		for (int i = 0; i <= MAX; i++) {
			typeMap.put(i, new TimeIntervalType(i));
		}
		typeMap.put(-1, new TimeIntervalType(-1));
	}


	private TimeIntervalType(int index) {
		this.value = index;

		int intervalStart = 0;
		int intervalEnd = (index + 1) * HALF_HOUR_MINUTES;
		if (index > 0) {
			TimeIntervalType lastIntervalType = getInstance(index - 1);
			intervalStart = lastIntervalType.interval[1];

			if (intervalStart % HOUR_MINUTES == 0) {
				intervalEnd = index * HOUR_MINUTES;
			}
		}

		if (index < MAX) {
			this.interval = new int[] { intervalStart, intervalEnd};
		} else {
			this.interval = new int[] { intervalStart };
		}

	}

	public static final TimeIntervalType[] VALUES = typeMap.values().toArray(new TimeIntervalType[0]);

	public static TimeIntervalType getInstance(int value) {
		return typeMap.get(value);
	}

	public int unique() {
		return value;
	}

	public int[] getInterval() {
		return interval;
	}

	public String getDisplayName() {
		if (this.unique() == -1) {
			return "form.text.backOffice.customerFee.feeType.Overdue";
		}
		String displayName = String.valueOf(this.interval[0]);
		if (this.interval.length > 1) {
			displayName += "-" + this.interval[1];
		} else {
			displayName += "+";
		}
		return displayName;
	}
}
