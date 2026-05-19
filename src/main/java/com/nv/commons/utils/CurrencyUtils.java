package com.nv.commons.utils;

import java.math.BigDecimal;

import com.nv.commons.constants.CurrencyType;

public class CurrencyUtils {

	private CurrencyUtils() {
		throw new AssertionError();
	}


	public static String formatCurrencyWithDigits2(BigDecimal value, CurrencyType currType) {
		return formatCurrencyWithDigits2(value.doubleValue(), currType);
	}

	public static String formatCurrencyWithDigits2(double value, CurrencyType currType) {
		StringBuilder sb = new StringBuilder();
		String absStr = ThreadLocalUtils.getDecimalFormat("###,##0.00").format(Math.abs(value));

		if (value < 0) {
			sb.append("(").append(currType.getCurrencySymbol()).append(absStr).append(")");
		} else {
			sb.append(currType.getCurrencySymbol()).append(absStr);
		}
		return sb.toString();
	}

}
