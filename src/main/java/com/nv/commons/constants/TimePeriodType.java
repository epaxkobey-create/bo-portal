package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.DateTimeBuilder;

import java.sql.Timestamp;
import java.time.LocalDate;

public enum TimePeriodType implements UniqueValueHolder {
	TODAY(1) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{DateTimeBuilder.localDateTime().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().plusDays(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "today";
		}

	},
	YESTERDAY(2) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{DateTimeBuilder.localDateTime().minusDays(1).withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().minusDays(1).withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().plusDays(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "yesterday";
		}

	},
	THIS_WEEK(3) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{DateTimeBuilder.localDateTime().firstDayOfWeek().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().lastDayOfWeek().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().firstDayOfWeek().plusWeeks(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "thisWeek";
		}

	},
	LAST_WEEK(4) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{
				DateTimeBuilder.localDateTime().minusWeeks(1).firstDayOfWeek().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().minusWeeks(1).lastDayOfWeek().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().firstDayOfWeek().plusWeeks(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "lastWeek";
		}

	},
	THIS_MONTH(5) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{DateTimeBuilder.localDateTime().firstDayOfMonth().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().lastDayOfMonth().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().firstDayOfMonth().plusMonths(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "thisMonth";
		}

	},
	LAST_MONTH(6) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{
				DateTimeBuilder.localDateTime().minusMonths(1).firstDayOfMonth().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().minusMonths(1).lastDayOfMonth().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().firstDayOfMonth().plusMonths(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "lastMonth";
		}

	},
	BEFORE_LAST_MONTH(7) {
		@Override
		protected Timestamp[] buildDuration() {
			return new Timestamp[]{
				DateTimeBuilder.localDateTime().minusMonths(2).firstDayOfMonth().withMinTime().toTimestamp(),
				DateTimeBuilder.localDateTime().minusMonths(2).lastDayOfMonth().withMaxTime().toTimestamp()};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().firstDayOfMonth().plusMonths(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "beforeLastMonth";
		}
	},
	LAST_HALF_MONTH(8) {
		@Override
		protected Timestamp[] buildDuration() {

			LocalDate now = LocalDate.now();

			Timestamp firstHalfMonth = DateTimeBuilder.localDateTime(
				LocalDate.of(now.getYear(), now.getMonth(), 15).atStartOfDay()).withMinTime().toTimestamp();

			Timestamp startTime;
			Timestamp endTime;

			Timestamp today = DateTimeBuilder.localDateTime().withMinTime().toTimestamp();
			if (today.getTime() <= firstHalfMonth.getTime()) {
				LocalDate earlier = now.minusMonths(1);

				LocalDate latterHalfMonth = LocalDate.of(earlier.getYear(), earlier.getMonth(), 16);

				startTime = DateTimeBuilder.localDateTime(latterHalfMonth.atStartOfDay()).withMinTime().toTimestamp();
				endTime = DateTimeBuilder.localDateTime().minusMonths(1).lastDayOfMonth().withMaxTime().toTimestamp();
			} else {
				startTime = DateTimeBuilder.localDateTime().firstDayOfMonth().withMinTime().toTimestamp();
				endTime = DateTimeBuilder.localDateTime(firstHalfMonth).withMaxTime().toTimestamp();
			}

			return new Timestamp[]{startTime, endTime};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().plusDays(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "lastHalfMonth";
		}

	},
	THIS_HALF_MONTH(9) {
		@Override
		protected Timestamp[] buildDuration() {

			LocalDate now = LocalDate.now();

			Timestamp firstHalfMonth = DateTimeBuilder.localDateTime(
				LocalDate.of(now.getYear(), now.getMonth(), 15).atStartOfDay()).withMinTime().toTimestamp();

			Timestamp startTime;
			Timestamp endTime;

			Timestamp today = DateTimeBuilder.localDateTime().withMinTime().toTimestamp();
			if (today.getTime() <= firstHalfMonth.getTime()) {
				startTime = DateTimeBuilder.localDateTime().firstDayOfMonth().withMinTime().toTimestamp();
				endTime = DateTimeBuilder.localDateTime(firstHalfMonth).withMaxTime().toTimestamp();
			} else {
				startTime = DateTimeBuilder.localDateTime(firstHalfMonth).plusDays(1).withMinTime().toTimestamp();
				endTime = DateTimeBuilder.localDateTime().lastDayOfMonth().withMaxTime().toTimestamp();
			}

			return new Timestamp[]{startTime, endTime};
		}

		@Override
		protected long makeFlag() {
			return DateTimeBuilder.localDateTime().plusDays(1).withMinTime().toTimeMilli();
		}

		@Override
		protected String getName() {
			return "thisHalfMonth";
		}
	};

	private long flag = 0;

	private Timestamp[] interval;

	private final int value;

	TimePeriodType(int unique) {
		this.value = unique;
	}

	@Override
	public int unique() {
		return this.value;
	}

	protected abstract Timestamp[] buildDuration();

	protected abstract long makeFlag();

	protected abstract String getName();

	public Timestamp[] getDuration() {
		if (System.currentTimeMillis() < flag) {
			return interval;
		}
		interval = this.buildDuration();
		flag = this.makeFlag();
		return interval;
	}

	public static TimePeriodType getInstanceOf(String name) {
		for (TimePeriodType e : TimePeriodType.values()) {
			if (e.getName().equalsIgnoreCase(name)) {
				return e;
			}
		}
		return null;
	}

}
