package com.nv.commons.utils;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 以Java 8新的time package為基礎做轉換，效能較佳
 * 採用builder設計模式跟lambda組合方式，讓程式更直覺
 * 讓String、Date、Calendar、LocalDateTime的相互轉換更有彈性
 *
 * @author Alan 2020.04.30
 */
public class DateTimeBuilder {

	@FunctionalInterface
	public interface DateProcessor {

		ZonedDateTime process(ZonedDateTime zonedDateTime);
	}

	private final ZonedDateTime zonedDateTime;

	private List<DateProcessor> dateProcessors = null;

	public static final TemporalField FIRST_DAY_OF_WEEK = WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek();

	private DateTimeBuilder(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}

	private DateTimeBuilder(LocalDateTime localDateTime) {
		this.zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
	}

	private DateTimeBuilder(LocalDateTime localDateTime, ZoneId zoneId) {
		this.zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
	}

	/**
	 * 採用預設時區
	 *
	 * @return
	 */
	public static DateTimeBuilder localDateTime() {
		return new DateTimeBuilder(LocalDateTime.now());
	}

	public static DateTimeBuilder localDateTime(String dateStr, String pattern, ZoneId zoneId) {
		try {
			// Java 8的轉換有問題，當pattern跟dateStr裡面資料對應不起來的時候，java 9有修復，所以現在先暫用舊的方式
			Date date = ThreadLocalUtils.getSimpleDateFormat(pattern).parse(dateStr);
			return localDateTime(date, zoneId);
		} catch (ParseException e) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * 採用預設時區
	 *
	 * @param dateStr
	 * @param pattern 裡面不能含有時區資料
	 * @return
	 */
	public static DateTimeBuilder localDateTime(String dateStr, String pattern) {
		return localDateTime(dateStr, pattern, ZoneId.systemDefault());
	}

	public static DateTimeBuilder localDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
		return new DateTimeBuilder(localDateTime, zoneId);
	}

	/**
	 * 採用預設時區
	 *
	 * @param localDateTime 本身沒有時區資訊
	 * @return
	 */
	public static DateTimeBuilder localDateTime(LocalDateTime localDateTime) {
		return localDateTime(localDateTime, ZoneId.systemDefault());
	}

	/**
	 * 採用預設時區
	 *
	 * @param timeMillis 本身沒有時區資訊
	 * @return
	 */
	public static DateTimeBuilder localDateTime(long timeMillis) {
		Instant instant = Instant.ofEpochMilli(timeMillis);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		return new DateTimeBuilder(localDateTime, ZoneId.systemDefault());
	}

	/**
	 * 含年月日，時分秒
	 *
	 * @param date   本身沒有時區資訊
	 * @param zoneId
	 * @return
	 */
	public static DateTimeBuilder localDateTime(Date date, ZoneId zoneId) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return new DateTimeBuilder(localDateTime, zoneId);
	}

	public static DateTimeBuilder localDateTime(Date date) {
		return localDateTime(date, ZoneId.systemDefault());
	}

	/**
	 * 時區採用Calendar內的時區
	 * 含年月日，時分秒
	 *
	 * @param calendar
	 * @return
	 */
	public static DateTimeBuilder localDateTime(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		TimeZone timeZone = calendar.getTimeZone();
		ZoneId zoneId = (timeZone == null ? ZoneId.systemDefault() : timeZone.toZoneId());
		LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneOffset.systemDefault());
		return new DateTimeBuilder(localDateTime, zoneId);
	}

	/**
	 * Timestamp本身沒有時區資訊
	 *
	 * @param timestamp
	 * @return
	 */
	public static DateTimeBuilder localDateTime(Timestamp timestamp, ZoneId zoneId) {
		LocalDateTime localDateTime = timestamp.toLocalDateTime();
		return new DateTimeBuilder(localDateTime, zoneId);
	}

	public static DateTimeBuilder localDateTime(Timestamp timestamp) {
		return localDateTime(timestamp, ZoneId.systemDefault());
	}

	/**
	 * 含年月日，時分秒，pattern需含時區資訊
	 *
	 * @return
	 */
	public static DateTimeBuilder zonedDateTime(String dateStr, DateTimeFormatter dateTimeFormatter) {
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr, dateTimeFormatter);
		return new DateTimeBuilder(zonedDateTime);
	}

	public static DateTimeBuilder zonedDateTime(String dateStr, String pattern) {
		return zonedDateTime(dateStr, DateTimeFormatter.ofPattern(pattern));
	}

	private void add(DateProcessor processor) {
		if (this.dateProcessors == null) {
			this.dateProcessors = new ArrayList<>();
		}
		this.dateProcessors.add(processor);
	}

	public DateTimeBuilder with(int hour, int minute, int second) {
		add((localDateTime) -> localDateTime.withHour(hour).withMinute(minute).withSecond(second));
		return this;
	}

	public DateTimeBuilder withHour(int value) {
		add((localDateTime) -> localDateTime.withHour(value));
		return this;
	}

	public DateTimeBuilder withMinute(int value) {
		add((localDateTime) -> localDateTime.withMinute(value));
		return this;
	}

	public DateTimeBuilder withSecond(int value) {
		add((localDateTime) -> localDateTime.withSecond(value));
		return this;
	}

	public DateTimeBuilder withMilliSecond(int value) {
		add((localDateTime) -> localDateTime.with(ChronoField.MILLI_OF_SECOND, value));
		return this;
	}

	public DateTimeBuilder withNanoSecond(int value) {
		add((localDateTime) -> localDateTime.with(ChronoField.NANO_OF_SECOND, value));
		return this;
	}

	public DateTimeBuilder withMaxTime() {
		return withMaxTime(null);
	}

	public DateTimeBuilder withMaxTime(ChronoUnit unit) {
		if (unit == null || unit == ChronoUnit.HOURS) {
			add((zonedDateTime) -> {
				LocalDateTime localDateTime = zonedDateTime.toLocalDate().atTime(LocalTime.MAX);
				return ZonedDateTime.of(localDateTime, zonedDateTime.getZone());
			});
		} else {
			add((zonedDateTime) -> {
				switch (unit) {
					case MINUTES:
						zonedDateTime = zonedDateTime.withMinute(59);
					case SECONDS:
						zonedDateTime = zonedDateTime.withSecond(59);
					case MILLIS:
						zonedDateTime = zonedDateTime.with(ChronoField.MILLI_OF_SECOND, 999);
				}
				return zonedDateTime;
			});
		}
		return this;
	}

	public DateTimeBuilder withMinTime() {
		add((zonedDateTime) -> {
			LocalDateTime localDateTime = zonedDateTime.toLocalDate().atTime(LocalTime.MIN);
			return ZonedDateTime.of(localDateTime, zonedDateTime.getZone());
		});
		return this;
	}

	public DateTimeBuilder withMinTime(ChronoUnit unit) {
		if (unit == null || unit == ChronoUnit.HOURS) {
			add((zonedDateTime) -> {
				LocalDateTime localDateTime = zonedDateTime.toLocalDate().atTime(LocalTime.MIN);
				return ZonedDateTime.of(localDateTime, zonedDateTime.getZone());
			});
		} else {
			add((zonedDateTime) -> {
				switch (unit) {
					case MINUTES:
						zonedDateTime = zonedDateTime.withMinute(0);
					case SECONDS:
						zonedDateTime = zonedDateTime.withSecond(0);
					case MILLIS:
						zonedDateTime = zonedDateTime.with(ChronoField.MILLI_OF_SECOND, 0);
				}
				return zonedDateTime;
			});
		}
		return this;
	}

	public DateTimeBuilder withNoonTime() {
		add((zonedDateTime) -> {
			LocalDateTime localDateTime = zonedDateTime.toLocalDate().atTime(LocalTime.NOON);
			return ZonedDateTime.of(localDateTime, zonedDateTime.getZone());
		});
		return this;
	}

	public DateTimeBuilder plusMillis(int value) {
		add((zonedDateTime) -> zonedDateTime.plus(value, ChronoUnit.MILLIS));
		return this;
	}

	public DateTimeBuilder minusMillis(int value) {
		add((zonedDateTime) -> zonedDateTime.minus(value, ChronoUnit.MILLIS));
		return this;
	}

	public DateTimeBuilder minusNanos(int value) {
		add((zonedDateTime) -> zonedDateTime.minus(value, ChronoUnit.NANOS));
		return this;
	}

	public DateTimeBuilder plusSeconds(int value) {
		add((zonedDateTime) -> zonedDateTime.plus(value, ChronoUnit.SECONDS));
		return this;
	}

	public DateTimeBuilder minusSeconds(int value) {
		add((zonedDateTime) -> zonedDateTime.minus(value, ChronoUnit.SECONDS));
		return this;
	}

	public DateTimeBuilder plusMinutes(int value) {
		add((zonedDateTime) -> zonedDateTime.plus(value, ChronoUnit.MINUTES));
		return this;
	}

	public DateTimeBuilder minusMinutes(int value) {
		add((zonedDateTime) -> zonedDateTime.minus(value, ChronoUnit.MINUTES));
		return this;
	}

	public DateTimeBuilder plusHours(int value) {
		add((zonedDateTime) -> zonedDateTime.plus(value, ChronoUnit.HOURS));
		return this;
	}

	public DateTimeBuilder minusHours(int value) {
		add((zonedDateTime) -> zonedDateTime.minus(value, ChronoUnit.HOURS));
		return this;
	}

	public DateTimeBuilder plusDays(int value) {
		add((zonedDateTime) -> zonedDateTime.plusDays(value));
		return this;
	}

	public DateTimeBuilder minusDays(int value) {
		add((zonedDateTime) -> zonedDateTime.minusDays(value));
		return this;
	}

	public DateTimeBuilder plusWeeks(int value) {
		add((zonedDateTime) -> zonedDateTime.plusWeeks(value));
		return this;
	}

	public DateTimeBuilder minusWeeks(int value) {
		add((zonedDateTime) -> zonedDateTime.minusWeeks(value));
		return this;
	}

	public DateTimeBuilder plusMonths(int value) {
		add((zonedDateTime) -> zonedDateTime.plusMonths(value));
		return this;
	}

	public DateTimeBuilder minusMonths(int value) {
		add((zonedDateTime) -> zonedDateTime.minusMonths(value));
		return this;
	}

	public DateTimeBuilder plusYears(int value) {
		add((zonedDateTime) -> zonedDateTime.plusYears(value));
		return this;
	}

	public DateTimeBuilder minusYears(int value) {
		add((zonedDateTime) -> zonedDateTime.minusYears(value));
		return this;
	}

	public DateTimeBuilder firstDayOfWeek() {
		add((zonedDateTime) -> zonedDateTime.with(FIRST_DAY_OF_WEEK, 1));
		return this;
	}

	public DateTimeBuilder lastDayOfWeek() {
		add((zonedDateTime) -> zonedDateTime.with(FIRST_DAY_OF_WEEK, 7));
		return this;
	}

	public DateTimeBuilder firstDayOfMonth() {
		add((zonedDateTime) -> zonedDateTime.with(TemporalAdjusters.firstDayOfMonth()));
		return this;
	}

	public DateTimeBuilder firstDayOfMonth(int week) {
		add((zonedDateTime) -> zonedDateTime.with((temporal) -> temporal.with(DAY_OF_MONTH, week)));
		return this;
	}

	public DateTimeBuilder lastDayOfMonth() {
		add((zonedDateTime) -> zonedDateTime.with(TemporalAdjusters.lastDayOfMonth()));
		return this;
	}

	public DateTimeBuilder firstDayOfYear() {
		add((zonedDateTime) -> zonedDateTime.with(TemporalAdjusters.firstDayOfYear()));
		return this;
	}

	public DateTimeBuilder lastDayOfYear() {
		add((zonedDateTime) -> zonedDateTime.with(TemporalAdjusters.lastDayOfYear()));
		return this;
	}

	public DateTimeBuilder truncatedTo(ChronoUnit unit) {
		add((zonedDateTime) -> zonedDateTime.truncatedTo(unit));
		return this;
	}

	public DateTimeBuilder toTimeZone(ZoneId zoneId) {
		add((zonedDateTime) -> zonedDateTime.withZoneSameInstant(zoneId));
		return this;
	}

	private ZonedDateTime process() {
		// 避免異動到原始值
		//		LocalDateTime localDateTime = this.localDateTime;
		ZonedDateTime zonedDateTime = this.zonedDateTime;

		if (this.dateProcessors != null) {
			for (DateProcessor dateProcessor : this.dateProcessors) {
				zonedDateTime = dateProcessor.process(zonedDateTime);
			}
		}
		return zonedDateTime;
	}

	public String toString(DateTimeFormatter dateTimeFormatter, ZoneId zoneId) {
		ZonedDateTime zonedDateTime = process();
		if (zoneId != null && zoneId != zonedDateTime.getZone()) {
			zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
			return zonedDateTime.format(dateTimeFormatter);
		} else {
			return zonedDateTime.toLocalDateTime().format(dateTimeFormatter);
		}
	}

	public String toString(String pattern, ZoneId zoneId) {
		return toString(DateTimeFormatter.ofPattern(pattern), zoneId);
	}

	public String toString(DateTimeFormatter dateTimeFormatter) {
		return toString(dateTimeFormatter, null);
	}

	public String toString(String pattern) {
		return toString(DateTimeFormatter.ofPattern(pattern), null);
	}

	public String toString() {
		return toString("yyyy-MM-dd HH:mm:ss");
	}

	public LocalDateTime toLocalDateTime(ZoneId zoneId) {
		ZonedDateTime zonedDateTime = process();
		if (zoneId != null && zoneId != zonedDateTime.getZone()) {
			return zonedDateTime.withZoneSameInstant(zoneId).toLocalDateTime();
		} else {
			return zonedDateTime.toLocalDateTime();
		}
	}

	public LocalDateTime toLocalDateTime() {
		return toLocalDateTime(null);
	}

	public Calendar toCalendar(ZoneId zoneId) {
		if (zoneId == null) {
			throw new IllegalArgumentException();
		}

		ZonedDateTime zonedDateTime = process();
		if (zoneId != zonedDateTime.getZone()) {
			zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
		}
		return GregorianCalendar.from(zonedDateTime);
	}

	public Calendar toCalendar() {
		ZonedDateTime zonedDateTime = process();
		return GregorianCalendar.from(zonedDateTime);
	}

	public Date toDate(ZoneId zoneId) {
		if (zoneId == null) {
			throw new IllegalArgumentException();
		}

		ZonedDateTime zonedDateTime = process();
		Instant instant;
		if (zoneId != zonedDateTime.getZone()) {
			// 從傳入的時區轉成指定的時區
			zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
			// 由於已經轉換過了，所以傳回的Date只單純的表示時間，所以要轉成系統預設時間
			instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		} else {
			instant = zonedDateTime.toInstant();
		}
		return Date.from(instant);
	}

	public Date toDate() {
		ZonedDateTime zonedDateTime = process();
		// 由於已經轉換過了，所以傳回的Timestamp只單純的表示時間，所以要轉成系統預設時間
		Instant instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public java.sql.Date toSqlDate(ZoneId zoneId) {
		if (zoneId == null) {
			throw new IllegalArgumentException();
		}

		ZonedDateTime zonedDateTime = process();
		Instant instant;
		if (zoneId != zonedDateTime.getZone()) {
			zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
			// 由於已經轉換過了，所以傳回的Date只單純的表示時間，所以要轉成系統預設時間
			instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		} else {
			instant = zonedDateTime.toInstant();
		}
		return new java.sql.Date(instant.toEpochMilli());
	}

	public java.sql.Date toSqlDate() {
		ZonedDateTime zonedDateTime = process();
		// 由於已經轉換過了，所以傳回的Timestamp只單純的表示時間，所以要轉成系統預設時間
		Instant instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		return new java.sql.Date(instant.toEpochMilli());
	}

	public Timestamp toTimestamp(ZoneId zoneId) {
		if (zoneId == null) {
			throw new IllegalArgumentException();
		}

		ZonedDateTime zonedDateTime = process();
		Instant instant;
		if (zoneId != zonedDateTime.getZone()) {
			zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
			// 由於已經轉換過了，所以傳回的Timestamp只單純的表示時間，所以要轉成系統預設時間
			instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		} else {
			instant = zonedDateTime.toInstant();
		}
		return Timestamp.from(instant);
	}

	public Timestamp toTimestamp() {
		ZonedDateTime zonedDateTime = process();
		// 由於已經轉換過了，所以傳回的Timestamp只單純的表示時間，所以要轉成系統預設時間
		Instant instant = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), ZoneId.systemDefault()).toInstant();
		return Timestamp.from(instant);
	}

	public long toTimeMilli() {
		ZonedDateTime zonedDateTime = process();
		Instant instant = zonedDateTime.toInstant();
		return instant.toEpochMilli();
	}

	public void clear() {
		if (this.dateProcessors != null) {
			this.dateProcessors.clear();
		}
	}

}
