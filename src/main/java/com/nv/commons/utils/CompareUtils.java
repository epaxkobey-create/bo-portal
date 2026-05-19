package com.nv.commons.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CompareUtils {

	public static boolean different(Object a, Object b) {
		if (null == a && null == b) {
			return false;
		}

		if (null == a || null == b) {
			return true;
		}
		
		if (a instanceof BigDecimal && b instanceof BigDecimal) {
			return ((BigDecimal) a).compareTo((BigDecimal) b) != 0;
		}

		if (a instanceof Timestamp && b instanceof Timestamp) {
			return ((Timestamp) a).compareTo((Timestamp) b) != 0;
		}
		
		return !a.equals(b);
	}

}
