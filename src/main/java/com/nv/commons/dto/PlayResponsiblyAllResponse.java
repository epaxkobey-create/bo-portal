package com.nv.commons.dto;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for getAllPlayResponsibly response
 */
public class PlayResponsiblyAllResponse {

	@JsonProperty("depositLimits")
	private LimitsData depositLimits;

	@JsonProperty("wagerLimits")
	private LimitsData wagerLimits;

	@JsonProperty("lossLimits")
	private LimitsData lossLimits;

	@JsonProperty("realityCheck")
	private RealityCheckData realityCheck;

	@JsonProperty("accountReviewReminder")
	private AccountReviewReminderData accountReviewReminder;

	@JsonProperty("selfExclusion")
	private SelfExclusionData selfExclusion;

	@JsonProperty("timeSpentLimit")
	private TimeSpentLimitData timeSpentLimit;

	public PlayResponsiblyAllResponse() {
	}

	public PlayResponsiblyAllResponse(LimitsData depositLimits, LimitsData wagerLimits,
		LimitsData lossLimits, RealityCheckData realityCheck, AccountReviewReminderData accountReviewReminder, SelfExclusionData selfExclusion, TimeSpentLimitData timeSpentLimit) {
		this.depositLimits = depositLimits;
		this.wagerLimits = wagerLimits;
		this.lossLimits = lossLimits;
		this.realityCheck = realityCheck;
		this.accountReviewReminder = accountReviewReminder;
		this.selfExclusion = selfExclusion;
		this.timeSpentLimit = timeSpentLimit;
	}

	public LimitsData getDepositLimits() {
		return depositLimits;
	}

	public void setDepositLimits(LimitsData depositLimits) {
		this.depositLimits = depositLimits;
	}

	public LimitsData getWagerLimits() {
		return wagerLimits;
	}

	public void setWagerLimits(LimitsData wagerLimits) {
		this.wagerLimits = wagerLimits;
	}

	public LimitsData getLossLimits() {
		return lossLimits;
	}

	public void setLossLimits(LimitsData lossLimits) {
		this.lossLimits = lossLimits;
	}

	public RealityCheckData getRealityCheck() {
		return realityCheck;
	}

	public void setRealityCheck(RealityCheckData realityCheck) {
		this.realityCheck = realityCheck;
	}

	public AccountReviewReminderData getAccountReviewReminder() {
		return accountReviewReminder;
	}

	public void setAccountReviewReminder(
		AccountReviewReminderData accountReviewReminder) {
		this.accountReviewReminder = accountReviewReminder;
	}

	public static class LimitsData {

		@JsonProperty("daily")
		private Integer daily;

		@JsonProperty("weekly")
		private Integer weekly;

		@JsonProperty("monthly")
		private Integer monthly;

		public LimitsData() {
		}

		public LimitsData(Integer daily, Integer weekly, Integer monthly) {
			this.daily = daily;
			this.weekly = weekly;
			this.monthly = monthly;
		}

		public Integer getDaily() {
			return daily;
		}

		public void setDaily(Integer daily) {
			this.daily = daily;
		}

		public Integer getWeekly() {
			return weekly;
		}

		public void setWeekly(Integer weekly) {
			this.weekly = weekly;
		}

		public Integer getMonthly() {
			return monthly;
		}

		public void setMonthly(Integer monthly) {
			this.monthly = monthly;
		}
	}

	public static class RealityCheckData {

		@JsonProperty("interval")
		private Integer interval;

		public RealityCheckData() {
		}

		public RealityCheckData(Integer interval) {
			this.interval = interval;
		}

		public Integer getInterval() {
			return interval;
		}

		public void setInterval(Integer interval) {
			this.interval = interval;
		}
	}

	public static class AccountReviewReminderData {

		@JsonProperty("months")
		private Integer months;

		public AccountReviewReminderData() {
		}

		public AccountReviewReminderData(Integer months) {
			this.months = months;
		}

		public Integer getMonths() {
			return months;
		}

		public void setMonths(Integer months) {
			this.months = months;
		}
	}

	public static class SelfExclusionData {

		private Integer exclusion;

		public SelfExclusionData(Integer exclusion) {
			this.exclusion = exclusion;
		}

		public Integer getExclusion() {
			return exclusion;
		}

		public void setExclusion(Integer exclusion) {
			this.exclusion = exclusion;
		}
	}

	public static class TimeSpentLimitData {

		private Integer timeSpent;

		public TimeSpentLimitData() {
		}

		public TimeSpentLimitData(Integer timeSpent) {
			this.timeSpent = timeSpent;
		}

		public void setTimeSpent(Integer timeSpent) {
			this.timeSpent = timeSpent;
		}

		public Integer getTimeSpent() {
			return timeSpent;
		}
	}
}
