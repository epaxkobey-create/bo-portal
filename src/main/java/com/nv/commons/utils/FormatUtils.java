package com.nv.commons.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 時間/數字 的基本格式化物件
 *
 * @author shipper
 */
public class FormatUtils {

	public static final String DATE_PATTERN_SLASH_ddMMyyyy = "dd/MM/yyyy";
	public static final String DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma = "dd/MM/yyyy, HH:mm";
	public static final String DATE_PATTERN_SLASH_ddMMyyyy_HHmmss = "dd/MM/yyyy HH:mm:ss";
	public static final String DATE_PATTERN_SLASH_yyyyMMdd = "yyyy/MM/dd";
	public static final String DATE_PATTERN_SLASH_yyyyMMdd_HHmmss = "yyyy/MM/dd HH:mm:ss";
	public static final String DATE_PATTERN_SLASH_yyyyMMdd_HHmmss_ISO8601 = "yyyy/MM/dd'T'HH:mm:ssZ"; // ISO8601
	public static final String DATE_PATTERN_DASH_yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";

	public static final String NUMBER_PATTERN_THOUSAND_SEPARATOR_NO_DECIMAL = "#,###";
	/**
	 * 對日期作格式化
	 *
	 * @param c
	 * @return
	 */
	public static String dateFormat(Calendar c) {
		return dateFormat(c, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);
	}

	/**
	 * 對日期作格式化
	 *
	 * @param c
	 * @param format
	 * @return
	 */
	public static String dateFormat(Calendar c, String format) {
		return (c == null ? "" : DateFormatUtils.format(c, format));
	}

	/**
	 * 對日期作格式化
	 *
	 * @param d
	 * @return
	 */
	public static String dateFormat(Date d) {
		return dateFormat(d, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, null);
	}

	public static String dateFormat(Date d, String format) {
		return dateFormat(d, format, null);
	}

	/**
	 * 對日期作格式化
	 *
	 * @param d
	 * @param format
	 * @return
	 */
	public static String dateFormat(Date d, String format, Locale locale) {
		if (d == null) {
			return "";
		}
		if (locale != null) {
			return DateFormatUtils.format(d, format, locale);
		} else {
			return DateFormatUtils.format(d, format);
		}
	}

	/**
	 * 對日期作格式化
	 *
	 * @param d
	 * @return
	 */
	public static String dateFormat(java.sql.Date d) {
		return dateFormat(d, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);
	}

	/**
	 * 對日期作格式化
	 *
	 * @param d
	 * @param format
	 * @return
	 */
	public static String dateFormat(java.sql.Date d, String format) {
		return (d == null ? "" : DateFormatUtils.format(d, format));
	}

	public static String dateFormat(java.sql.Date d, String format, Locale locale) {
		return (d == null ? "" : DateFormatUtils.format(d, format, locale));
	}

	public static String dateFormat(Timestamp t) {
		return (t == null ? "" : DateFormatUtils.format(t, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss));
	}

	/**
	 * 將時間轉為該時區的時間，並且後面加上 (GMT +?) 的字串
	 *
	 * @param t
	 * @param timeZone
	 * @return e.g 16:00:00 ->15:00:00 (GMT+7:00)
	 */
	public static String dateFormatWithTimeZone(Timestamp t, TimeZone timeZone) {
		return dateFormatWithTimeZone(t, DATE_PATTERN_SLASH_yyyyMMdd_HHmmss, timeZone);
	}

	public static String dateFormatWithTimeZone(Timestamp t, String format, TimeZone timeZone) {
		if (t == null) {
			return "";
		} else {
			return String
				.format("%s (%s)", DateFormatUtils.format(t, format, timeZone), timeZone.getID());
		}
	}

	public static String dateFormat(Timestamp t, String format) {
		return (t == null ? "" : DateFormatUtils.format(t, format));
	}

	public static String dateFormat(Timestamp t, String format, TimeZone tz) {
		return (t == null ? "" : DateFormatUtils.format(t, format, tz));
	}

	public static String dateFormat(Timestamp t, TimeZone tz) {
		return (t == null ? "" : DateFormatUtils.format(t, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, tz));
	}

	public static String dateFormat(Timestamp t, String format, Locale locale) {
		return (t == null ? "" : DateFormatUtils.format(t, format, locale));
	}

	/**
	 * 對日期作格式化
	 *
	 * @param mt
	 * @return
	 */
	public static String dateFormat(long mt) {
		return DateFormatUtils.format(mt, DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);
	}

	/**
	 * 對日期作格式化
	 *
	 * @param mt
	 * @param format
	 * @return
	 */
	public static String dateFormat(long mt, String format) {
		return DateFormatUtils.format(mt, format);
	}

	/**
	 * 將指定的日期字串, 依照指定的格式轉成日期物件
	 *
	 * @param pattern String
	 * @param source String
	 * @return
	 */
	public static Date parseDate(String pattern, String source) {

		try {
			return ThreadLocalUtils.getSimpleDateFormat(pattern).parse(source);
		} catch (ParseException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

	//---------------------------------------------------

	/**
	 * 對數字做格式化
	 *
	 * @param number BigDecimal
	 * @return String
	 */
	public static String numberFormat(BigDecimal number) {
		return numberFormat(number.doubleValue());
	}

	public static String numberFormat(double number) {
		return numberFormat(number, "###,##0.00");
	}

	/**
	 * 對數字做格式化
	 *
	 * @param number double
	 * @return String
	 */
	public static String numberFormat(double number, String format) {
		return ThreadLocalUtils.getDecimalFormat(format).format(number);
	}

	/**
	 * 對數字做格式化
	 *
	 * @param number
	 * @return
	 */
	public static String numberFormat(int number) {
		return String.format("%,d", number);
	}

	/**
	 * 對數字做格式化
	 *
	 * @param number
	 * @return
	 */
	public static String numberFormat(long number) {
		return String.format("%,d", number);
	}

	/**
	 * 對數字做格式化
	 *
	 * @param number
	 * @return
	 */
	public static String numberFormat(Integer number) {
		// d 只接受整數, 所以 arg 不能用 Number 因為 Double 也屬於 Number
		return String.format("%,d", number);
	}

	public static String numberFormat(Long number) {
		// d 只接受整數, 所以 arg 不能用 Number 因為 Double 也屬於 Number
		return String.format("%,d", number);
	}

	/**
	 * 對數字做格式化
	 *
	 * @param number
	 * @return
	 */
	public static String numberFormat(Number number, String format) {
		return ThreadLocalUtils.getDecimalFormat(format).format(number);
	}

	public static String displayTimeZone(TimeZone tz) {

		long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
		long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset())
			- TimeUnit.HOURS.toMinutes(hours);
		// avoid -4:-30 issue
		minutes = Math.abs(minutes);

		String result = "";
		if (hours > 0) {
			result = String.format("GMT+%d:%02d", hours, minutes);
		} else {
			result = String.format("GMT%d:%02d", hours, minutes);
		}

		return result;

	}

	private FormatUtils() {
		throw new AssertionError();
	}
}
