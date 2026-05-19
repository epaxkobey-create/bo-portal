package com.nv.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.exceptions.Deviation;
import org.apache.commons.lang3.StringUtils;
import org.apache.juli.logging.Log;

/**
 * MoneyTransaction 相關工具類
 */
public final class MoneyTransactionUtils {

	private static final Pattern adjustmentIdPattern = Pattern.compile("^A(\\d{8,9})$");
	private static final Pattern depositIdPattern = Pattern.compile("^D(\\d{8,9})$");
	private static final Pattern withdrawalIdPattern = Pattern.compile("^W(\\d{8,9})$");
	private static final Pattern frontendTransactionIdPattern = Pattern.compile("^[ADW](\\d{8,9})$");

	private MoneyTransactionUtils() {
		throw new AssertionError();
	}

	public static String formatId(long id, int type) {
		return formatId(id, MoneyTransactionType.getInstance(type));
	}

	public static String formatId(long id, MoneyTransactionType type) {
		if (MoneyTransactionType.WITHDRAWALS == type || MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY == type) {
			return "W" + StringUtils.leftPad(id + "", 9, "0");
		}
		if (MoneyTransactionType.ADJUSTMENT == type || MoneyTransactionType.REVENUE_ADJUSTMENT == type) {
			return "A" + StringUtils.leftPad(id + "", 9, "0");
		}
		return "D" + StringUtils.leftPad(id + "", 9, "0");
	}

	public static long parseAdjustmentId(String frontendId) throws Deviation {
		Matcher matcher = adjustmentIdPattern.matcher(frontendId);
		if (matcher.matches()) {
			try {
				return Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
		return 0;
	}

	public static long parseDepositId(String frontendId) throws Deviation {
		Matcher matcher = depositIdPattern.matcher(frontendId);
		if (matcher.matches()) {
			try {
				return Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
		return 0;
	}

	public static long parseWithdrawalId(String frontendId) throws Deviation {
		Matcher matcher = withdrawalIdPattern.matcher(frontendId);
		if (matcher.matches()) {
			try {
				return Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
		return 0;
	}

	public static Long deformatFrontendId(String frontendId) {
		Matcher matcher = frontendTransactionIdPattern.matcher(frontendId);
		if (matcher.matches()) {
			try {
				return Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
		throw new Deviation("Transaction ID is invalid");
	}


}
