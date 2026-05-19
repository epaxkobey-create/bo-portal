package com.nv.commons.dto;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.nv.commons.utils.DateUtils;

public class RealityCheckReminderData {

	private Timestamp loginTime;
	private int intervalInMinutes;
	private int checkedCount;
	private Timestamp realityCheckTime;
	private boolean realityCheckTimeUpdated;

	public RealityCheckReminderData(Timestamp loginTime, int intervalInMinutes) {
		this.loginTime = loginTime;
		this.intervalInMinutes = intervalInMinutes;
		this.checkedCount = 1;
		this.realityCheckTime = Timestamp.from(
			new Timestamp(System.currentTimeMillis()).toInstant()
				.plus(Duration.ofMinutes(this.intervalInMinutes)));
	}

	public Timestamp getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Timestamp loginTime) {
		this.loginTime = loginTime;
	}

	public int getIntervalInMinutes() {
		return intervalInMinutes;
	}

	public void setIntervalInMinutes(int intervalInMinutes) {
		this.intervalInMinutes = intervalInMinutes;
	}

	public int getCheckedCount() {
		return checkedCount;
	}

	public void setCheckedCount(int checkedCount) {
		this.checkedCount = checkedCount;
	}

	public Timestamp getRealityCheckTime() {
		return realityCheckTime;
	}

	public void setRealityCheckTime(Timestamp realityCheckTime) {
		this.realityCheckTime = realityCheckTime;
	}

	public boolean isRealityCheckTimeUpdated() {
		return realityCheckTimeUpdated;
	}

	public void setRealityCheckTimeUpdated(boolean realityCheckTimeUpdated) {
		this.realityCheckTimeUpdated = realityCheckTimeUpdated;
	}

	public int getActiveTime() {
		return this.intervalInMinutes * this.checkedCount;
	}

	public int getRealActiveTime() {
		Instant loginTimeInMinute = this.loginTime.toInstant().truncatedTo(ChronoUnit.MINUTES);
		Instant currentTimeInMinute = Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MINUTES);
		long diffMinutes = ChronoUnit.MINUTES.between(loginTimeInMinute, currentTimeInMinute);
		return (int) diffMinutes;
	}

	public void increaseRealityCheckTime() {
		if (this.realityCheckTime == null) {
			this.realityCheckTime = new Timestamp(System.currentTimeMillis());
		}

		this.realityCheckTime = Timestamp.from(
			this.realityCheckTime.toInstant()
				.plus(Duration.ofMinutes(this.intervalInMinutes)));

		this.checkedCount++;
		this.realityCheckTimeUpdated = true;
	}

	public boolean isRealityCheckTimeReached() {
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		return this.realityCheckTime.before(currentTime)
			|| Math.abs(DateUtils.secondsBetween(this.realityCheckTime.getTime(), currentTime.getTime())) <= 60;
	}
}
