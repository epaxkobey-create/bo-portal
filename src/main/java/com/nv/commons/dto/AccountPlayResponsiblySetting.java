package com.nv.commons.dto;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nv.commons.annotation.Column;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountReviewReminderType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.RealityCheckType;
import com.nv.commons.constants.SelfExclusionType;
import com.nv.commons.constants.SessionExpiryType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeSpentLimitType;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;

public class AccountPlayResponsiblySetting {

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "TYPE")
	private int type;

	@Column(name = "PERIOD_TYPE")
	private int periodType;

	@Column(name = "CURRENT_VALUE")
	private String currentValue;

	@Column(name = "NEW_VALUE")
	private String newValue;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "EFFECTIVE_TIME")
	private Timestamp effectiveTime;

	@Column(name = "EFFECTIVE_END_TIME")
	private Timestamp effectiveEndTime;

	// TODO: dto 不應該出現這種 method，這可以放在 util
	public void setEffectiveEndTime() {

		if (this.type == AccountPlayResponsiblyType.SELF_EXCLUSION.unique()
			&& this.status == AccountPlayResponsiblyStatusType.ACTIVE.unique()) {

			if (Integer.parseInt(this.currentValue) != SelfExclusionType.NO_EXCLUSION.unique()
				&& Integer.parseInt(this.currentValue) != SelfExclusionType.INDEFINITE.unique()) {

				long takeEffectEndMinutes = Long.parseLong(this.currentValue) * 24 * 60;

				this.effectiveEndTime = Timestamp.from(new Timestamp(System.currentTimeMillis()).toInstant()
					.plus(Duration.ofMinutes(takeEffectEndMinutes)));
			} else {
				this.effectiveEndTime = null;
			}
		}
	}

	@Column(name = "CREATOR")
	private String creator;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATER")
	private String updater;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	public AccountPlayResponsiblySetting() {
		this.periodType = AccountPlayResponsiblyPeriodType.DAILY.unique();
	}

	@JsonProperty("currentValueName")
	public String getCurrentValueName() {
		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(this.type);
		if (type == null) {
			return "";
		}
		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS ->
				this.currentValue.equals(String.valueOf(type.getDefaultValue())) ? "-" :
					CurrencyType.EUR.getCurrencySymbol() + " " +
					FormatUtils.numberFormat(Integer.parseInt(this.currentValue),
						FormatUtils.NUMBER_PATTERN_THOUSAND_SEPARATOR_NO_DECIMAL);
			case SESSION_EXPIRY -> this.currentValue.equals(String.valueOf(type.getDefaultValue())) ? "-" :
				SessionExpiryType.getInstanceOf(this.currentValue).getFullName();
			case SELF_EXCLUSION -> this.currentValue.equals(String.valueOf(type.getDefaultValue())) ? "-" :
				SelfExclusionType.getInstanceOf(this.currentValue).getFullName();
			case REALITY_CHECK -> RealityCheckType.getInstanceOf(this.currentValue).getFullName();
			case ANNUAL_REMINDER -> "";
			case ACCOUNT_REVIEW_REMINDER -> AccountReviewReminderType.getInstanceOf(this.currentValue).getFullName();
			case TIME_SPENT_LIMIT -> TimeSpentLimitType.getInstanceOf(this.currentValue).getName();
		};
	}

	@JsonProperty("currentValueDisplay")
	public String getCurrentValueDisplay() {
		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(this.type);
		if (type == null) {
			return "";
		}
		return switch (type) {
			case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> "";
			case SESSION_EXPIRY -> "";
			case SELF_EXCLUSION -> {
				if (Integer.parseInt(this.currentValue) == SelfExclusionType.NO_EXCLUSION.unique()) {
					yield "-";
				} else if (Integer.parseInt(this.currentValue) == SelfExclusionType.INDEFINITE.unique()) {
					yield "Indefinite";
				} else {
					yield "Definite: " + SelfExclusionType.getInstanceOf(this.currentValue).getFullName() + "\nUntil "
						  + DateUtils.toString(this.effectiveEndTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmm_Comma);
				}
			}
			case REALITY_CHECK -> "";
			case ANNUAL_REMINDER -> "";
			case ACCOUNT_REVIEW_REMINDER -> "";
			case TIME_SPENT_LIMIT -> "";
		};
	}

	@JsonIgnore
	public boolean isActive() {
		return this.status == AccountPlayResponsiblyStatusType.ACTIVE.unique();
	}

	@JsonProperty("message")
	public String getMessage() {
		if (!isActive()) {
			AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(this.type);
			if (type == null) {
				return "";
			}
			return switch (type) {
				case WAGER_LIMITS, LOSS_LIMITS, DEPOSIT_LIMITS -> {
					if (Integer.parseInt(this.newValue) == SystemConstants.NO_LIMIT_SETTING) {
						yield "Limit removal takes effect on {effectiveTime}.";
					} else {
						yield "Limit increase to " + CurrencyType.EUR.getCurrencySymbol() + " " +
							  FormatUtils.numberFormat(Integer.parseInt(this.newValue),
								  FormatUtils.NUMBER_PATTERN_THOUSAND_SEPARATOR_NO_DECIMAL) +
							  " takes effect on {effectiveTime}.";
					}
				}
				case SESSION_EXPIRY -> {
					if (Integer.parseInt(this.newValue) == SessionExpiryType.NONE.unique()) {
						yield "Session expiry removal takes effect on {effectiveTime}.";
					} else {
						yield "Session expiry increase to " +
							  SessionExpiryType.getInstanceOf(this.newValue).getFullName() +
							  " takes effect on {effectiveTime}.";
					}
				}
				case SELF_EXCLUSION -> {
					if (Integer.parseInt(this.newValue) == SelfExclusionType.NO_EXCLUSION.unique()) {
						yield "Self exclusion removal takes effect on {effectiveTime}.";
					} else {
						yield "Self exclusion decrease to " +
							  SelfExclusionType.getInstanceOf(this.newValue).getFullName() +
							  " takes effect on {effectiveTime}.";
					}
				}
				case REALITY_CHECK -> {
					if (Integer.parseInt(this.newValue) > Integer.parseInt(this.currentValue)) {
						yield "Reminder increase to " +
							  RealityCheckType.getInstanceOf(this.newValue).getFullName() +
							  " takes effect on {effectiveTime}.";
					} else {
						yield "";
					}
				}
				case ANNUAL_REMINDER -> "";
				case ACCOUNT_REVIEW_REMINDER -> "";
				case TIME_SPENT_LIMIT -> {
					if (Integer.parseInt(this.newValue) == SystemConstants.NO_LIMIT_SETTING) {
						yield "Limit removal takes effect on {effectiveTime}.";
					} else {
						yield "Limit increase to " +
							this.newValue + "h" +
							" takes effect on {effectiveTime}.";
					}
				}

			};
		}
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AccountPlayResponsiblySetting that = (AccountPlayResponsiblySetting) o;
		return Objects.equals(userId, that.userId) &&
			   websiteType == that.websiteType &&
			   type == that.type &&
			   periodType == that.periodType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, websiteType, type, periodType);
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPeriodType() {
		return periodType;
	}

	public void setPeriodType(int periodType) {
		this.periodType = periodType;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Timestamp getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Timestamp effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public Timestamp getEffectiveEndTime() {
		return effectiveEndTime;
	}

	public void setEffectiveEndTime(Timestamp effectiveEndTime) {
		this.effectiveEndTime = effectiveEndTime;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	public String getUpdater() {
		return updater;
	}
}
