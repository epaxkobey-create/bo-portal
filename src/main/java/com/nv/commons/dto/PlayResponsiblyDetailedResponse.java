package com.nv.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for getPlayerAllResponsibilitiesWithJGenerator response (BO usage)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayResponsiblyDetailedResponse {

	@JsonProperty("sessionExpiry")
	private PlayResponsiblyDetailData sessionExpiry;

	@JsonProperty("selfExclusion")
	private PlayResponsiblyDetailData selfExclusion;

	@JsonProperty("wagerLimits")
	private LimitsDetailData wagerLimits;

	@JsonProperty("lossLimits")
	private LimitsDetailData lossLimits;

	@JsonProperty("realityCheck")
	private PlayResponsiblyDetailData realityCheck;

	@JsonProperty("depositLimits")
	private LimitsDetailData depositLimits;

	@JsonProperty("accountReviewReminder")
	private PlayResponsiblyDetailData accountReviewReminder;

	@JsonProperty("timeSpentLimit")
	private PlayResponsiblyDetailData timeSpentLimit;


	public PlayResponsiblyDetailedResponse() {
	}

	public PlayResponsiblyDetailData getSessionExpiry() {
		return sessionExpiry;
	}

	public void setSessionExpiry(PlayResponsiblyDetailData sessionExpiry) {
		this.sessionExpiry = sessionExpiry;
	}

	public PlayResponsiblyDetailData getSelfExclusion() {
		return selfExclusion;
	}

	public void setSelfExclusion(PlayResponsiblyDetailData selfExclusion) {
		this.selfExclusion = selfExclusion;
	}

	public LimitsDetailData getWagerLimits() {
		return wagerLimits;
	}

	public void setWagerLimits(LimitsDetailData wagerLimits) {
		this.wagerLimits = wagerLimits;
	}

	public LimitsDetailData getLossLimits() {
		return lossLimits;
	}

	public void setLossLimits(LimitsDetailData lossLimits) {
		this.lossLimits = lossLimits;
	}

	public PlayResponsiblyDetailData getRealityCheck() {
		return realityCheck;
	}

	public void setRealityCheck(PlayResponsiblyDetailData realityCheck) {
		this.realityCheck = realityCheck;
	}

	public LimitsDetailData getDepositLimits() {
		return depositLimits;
	}

	public void setDepositLimits(LimitsDetailData depositLimits) {
		this.depositLimits = depositLimits;
	}

	public void setAccountReviewReminder(PlayResponsiblyDetailData accountReviewReminder) {
		this.accountReviewReminder = accountReviewReminder;
	}

	public PlayResponsiblyDetailData getAccountReviewReminder() {
		return accountReviewReminder;
	}

	public PlayResponsiblyDetailData getTimeSpentLimit() {
		return timeSpentLimit;
	}

	public void setTimeSpentLimit(PlayResponsiblyDetailData timeSpentLimit) {
		this.timeSpentLimit = timeSpentLimit;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class PlayResponsiblyDetailData {

		@JsonProperty("currentValue")
		private String currentValue;

		@JsonProperty("status")
		private Integer status;

		@JsonProperty("newValue")
		private String newValue;

		@JsonProperty("effectiveTime")
		private Long effectiveTime;

		@JsonProperty("effectiveEndTime")
		private Long effectiveEndTime;

		@JsonProperty("message")
		private String message;

		public PlayResponsiblyDetailData() {
		}

		public PlayResponsiblyDetailData(String currentValue, Integer status, String newValue) {
			this.currentValue = currentValue;
			this.status = status;
			this.newValue = newValue;
		}

		public String getCurrentValue() {
			return currentValue;
		}

		public void setCurrentValue(String currentValue) {
			this.currentValue = currentValue;
		}

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}

		public String getNewValue() {
			return newValue;
		}

		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}

		public Long getEffectiveTime() {
			return effectiveTime;
		}

		public void setEffectiveTime(Long effectiveTime) {
			this.effectiveTime = effectiveTime;
		}

		public Long getEffectiveEndTime() {
			return effectiveEndTime;
		}

		public void setEffectiveEndTime(Long effectiveEndTime) {
			this.effectiveEndTime = effectiveEndTime;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class LimitsDetailData {

		@JsonProperty("daily")
		private PlayResponsiblyDetailData daily;

		@JsonProperty("weekly")
		private PlayResponsiblyDetailData weekly;

		@JsonProperty("monthly")
		private PlayResponsiblyDetailData monthly;

		public LimitsDetailData() {
		}

		public PlayResponsiblyDetailData getDaily() {
			return daily;
		}

		public void setDaily(PlayResponsiblyDetailData daily) {
			this.daily = daily;
		}

		public PlayResponsiblyDetailData getWeekly() {
			return weekly;
		}

		public void setWeekly(PlayResponsiblyDetailData weekly) {
			this.weekly = weekly;
		}

		public PlayResponsiblyDetailData getMonthly() {
			return monthly;
		}

		public void setMonthly(PlayResponsiblyDetailData monthly) {
			this.monthly = monthly;
		}
	}
}
