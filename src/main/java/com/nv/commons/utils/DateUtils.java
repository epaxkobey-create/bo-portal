package com.nv.commons.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//import org.apache.commons.net.ntp.TimeStamp;

/**
 * also use DateUtils
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	public static final int BEGIN = 0;

	public static final int END = 1;

	private DateUtils() {
		throw new AssertionError();
	}

	public static LocalDateTime getLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * 設定時間部分(小時,分,秒,毫秒)的最小值
	 *
	 * @param calendar
	 */
	private static void setTimeActualMinimum(Calendar calendar) {
		calendar.set(GregorianCalendar.HOUR_OF_DAY, calendar.getActualMinimum(GregorianCalendar.HOUR_OF_DAY));
		calendar.set(GregorianCalendar.MINUTE, calendar.getActualMinimum(GregorianCalendar.MINUTE));
		calendar.set(GregorianCalendar.SECOND, calendar.getActualMinimum(GregorianCalendar.SECOND));
		calendar.set(GregorianCalendar.MILLISECOND, calendar.getActualMinimum(GregorianCalendar.MILLISECOND));
	}

	/**
	 * 設定時間部分(小時,分,秒,毫秒)的最大值
	 *
	 * @param calendar
	 */
	private static void setTimeActualMaximum(Calendar calendar) {
		calendar.set(GregorianCalendar.HOUR_OF_DAY, calendar.getActualMaximum(GregorianCalendar.HOUR_OF_DAY));
		calendar.set(GregorianCalendar.MINUTE, calendar.getActualMaximum(GregorianCalendar.MINUTE));
		calendar.set(GregorianCalendar.SECOND, calendar.getActualMaximum(GregorianCalendar.SECOND));
		calendar.set(GregorianCalendar.MILLISECOND, calendar.getActualMaximum(GregorianCalendar.MILLISECOND));
	}

	/**
	 * 取得指定日期的凌晨或是午夜時段
	 *
	 * @param date
	 * @param point (begin or end)
	 * @return
	 */
	public static Date getSpecifyDate(Date date, int point) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		if (point == BEGIN) {
			setTimeActualMinimum(calendar);
		} else {
			setTimeActualMaximum(calendar);
		}
		return (Date) calendar.getTime().clone();
	}

	/**
	 * 取得增減後的指定日期
	 *
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date getNextNDay(Date date, int n) {
		return getNextNDay(date.getTime(), n).getTime();
	}

	public static Calendar getNextNDay(long timeMillis, int n) {
		return getNextNDay(timeMillis, n, null);
	}

	public static Calendar getNextNDay(long timeMillis, int n, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.add(GregorianCalendar.DAY_OF_MONTH, n);

		if (timeZone != null) {
			calendar.setTimeZone(timeZone);
		}

		return calendar;
	}

	public static Calendar getNextNMonth(long timeMillis, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.add(GregorianCalendar.MONTH, n);
		return calendar;
	}

	/**
	 * 取得增減後的指定月份
	 *
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date getNextNMonth(Date date, int n) {
		return getNextNMonth(date.getTime(), n).getTime();
	}

	/**
	 * 是否在指定範圍內
	 *
	 * @param targetDate
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static boolean isBetween(Date targetDate, Date startDate, Date endDate) {
		return (targetDate.getTime() > startDate.getTime() && targetDate.getTime() < endDate.getTime());
	}

//	/**
//	 * 兩個日期間隔的月數
//	 *
//	 * @param startDate
//	 * @param endDate
//	 * @return
//	 */
//	public static int monthsBetween(Date startDate, Date endDate) {
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(startDate);
//		int startMonth = calendar.get(GregorianCalendar.MONTH);
//		int startYear = calendar.get(GregorianCalendar.YEAR);
//		calendar.setTime(endDate);
//		int endMonth = calendar.get(GregorianCalendar.MONTH);
//		int endYear = calendar.get(GregorianCalendar.YEAR);
//		return (endYear - startYear) * 12 + (endMonth - startMonth);
//	}

	/**
	 * 兩個日期間隔的天數
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int daysBetween(Date startDate, Date endDate) {
		return (int) ((endDate.getTime() - startDate.getTime()) / (24 * 3600 * 1000));
	}

	/**
	 * 兩個日期間隔的小時
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int hoursBetween(Date startDate, Date endDate) {
		return (int) ((endDate.getTime() - startDate.getTime()) / (60 * 60 * 1000));
	}

	/**
	 * 兩個日期間隔幾秒鐘
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long secondsBetween(Date startDate, Date endDate) {
		return (endDate.getTime() - startDate.getTime()) / (1000);
	}

	/**
	 * 兩個日期間隔幾秒鐘
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static long secondsBetween(long startTime, long endTime) {
		return (endTime - startTime) / (1000);
	}

//	public static long milliSecondsBetween(long startTime, long endTime) {
//		return Math.abs(startTime - endTime);
//	}

	/**
	 * 距今經過幾毫秒
	 *
	 * @param startTime
	 * @return
	 */
	public static double secondsElapsedSince(long startTime) {
		return (System.currentTimeMillis() - startTime) / 1000.0;
	}

//	public static double milliSecondsElapsedSince(long startTime) {
//		return (System.currentTimeMillis() - startTime);
//	}

	/**
	 * 將字串轉成日期
	 *
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static Date toDate(String dateStr, String pattern) {
		try {
			return ThreadLocalUtils.getSimpleDateFormat(pattern).parse(dateStr);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public static Timestamp toTimestamp(String dateStr, String pattern) {
		try {
			return new Timestamp(ThreadLocalUtils.getSimpleDateFormat(pattern).parse(dateStr).getTime());
		} catch (ParseException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 將日期轉成字串
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String toString(Date date, String pattern) {
		DateFormat df = ThreadLocalUtils.getSimpleDateFormat(pattern);
		return df.format(date);
	}

	public static String toString(Date date, String pattern, TimeZone tz) {
		DateFormat df = ThreadLocalUtils.getSimpleDateFormat(pattern, tz);
		return df.format(date);
	}

//	/*** parse ***/
//	public static Date parseDate(String pattern, String source) {
//		try {
//			return ThreadLocalUtils.getSimpleDateFormat(pattern).parse(source);
//		} catch (ParseException e) {
//			LogUtils.SYS.error(e.getMessage(), e);
//			return null;
//		}
//	}

	public static Date parseDate(String pattern, String source, Date def) {
		try {
			if (source == null) {
				return def;
			}
			return ThreadLocalUtils.getSimpleDateFormat(pattern).parse(source);
		} catch (ParseException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			return null;
		}
	}

//	// 改用 DateTimeBuilder.localDateTime().withMinTime().toCalendar()
//	@Deprecated
//	public static Calendar localDateTime() {
//		return getTodayMidNight(null);
//	}

	// TimeZone : 時區
	public static Calendar getTodayMidNight(TimeZone timeZone) {

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		if (timeZone != null) {
			calendar.setTimeZone(timeZone);
		}

		return calendar;
	}

	// TimeZone : 時區
	public static Calendar getTomorrowMidNight(TimeZone timeZone) {

		Calendar todayCalendar = getTodayMidNight(timeZone);

		return getNextNDay(todayCalendar.getTimeInMillis(), 1);
	}

	public static Calendar getNow() {
		return Calendar.getInstance();
	}

	public static Date getYesterdayMidNight() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/*
	 * returns -1, 0, 1
	 *
	 * -1 means endDate is before date
	 *  0 means date is between start & end
	 *  1 means startDate is equal/after date
	 */
	public static int checkIsDateInStartEndDate(Date startDate, Date endDate,
		Date date) {
		if (endDate.before(date)) {
			return -1;
		}
		if (startDate.after(date) || startDate.equals(date)) {
			return 1;
		}
		return 0;
	}

//	/**
//	 * For Payment Report
//	 */
//	public static String toTimeFormat(long time) { // HH:mm:ss
//		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
//			TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
//			TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1));
//	}

	public static Date getSpecifyEndDateTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(GregorianCalendar.MILLISECOND, calendar.getActualMaximum(GregorianCalendar.MILLISECOND));
		return (Date) calendar.getTime().clone();
	}

//	public static String getRptNameExt() {
//		return ThreadLocalUtils.getSimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
//	}

	public static Timestamp max(Timestamp t1, Timestamp t2) {
		return t1.compareTo(t2) > 0 ? t1 : t2;
	}

	public static Timestamp min(Timestamp t1, Timestamp t2) {
		return t1.compareTo(t2) < 0 ? t1 : t2;
	}

	public static String parseTimestampToISO8601Str(Timestamp timestamp) {
		try {
			return toString(timestamp, FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd_HHmmss_ISO8601);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Parse Sumsub createdAtMs (UTC) to Timestamp (GMT+8).
	 * Sumsub sends createdAtMs in format "yyyy-MM-dd HH:mm:ss.SSS" in UTC timezone.
	 *
	 * @param createdAtMs the createdAtMs string from Sumsub webhook (UTC)
	 * @return Timestamp converted to GMT+8, or null if parsing fails or input is null/empty
	 */
	public static Timestamp parseSumsubCreatedAtMs(String createdAtMs) {
		if (createdAtMs == null || createdAtMs.isEmpty()) {
			return null;
		}

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			LocalDateTime utcDateTime = LocalDateTime.parse(createdAtMs, formatter);

			ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
			ZonedDateTime gmt8Zoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));

			return Timestamp.valueOf(gmt8Zoned.toLocalDateTime());
		} catch (DateTimeParseException e) {
			LogUtils.SYS.warn("Failed to parse Sumsub createdAtMs: {} - {}", createdAtMs, e.getMessage());
			return null;
		}
	}

}
