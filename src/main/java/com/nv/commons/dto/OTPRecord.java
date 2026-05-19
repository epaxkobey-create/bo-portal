package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.utils.DateUtils;

public class OTPRecord {

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "otp_type")
	private int otpType;

	@Column(name = "code")
	private String code;

	@Column(name = "time")
	private Timestamp time;


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

	public int getOtpType() {
		return otpType;
	}

	public void setOtpType(int otpType) {
		this.otpType = otpType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OTPRecord otpRecord = (OTPRecord) o;
		return websiteType == otpRecord.websiteType &&
				otpType == otpRecord.otpType &&
				Objects.equals(userId, otpRecord.userId) &&
				Objects.equals(code, otpRecord.code) &&
				Objects.equals(time, otpRecord.time);
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, userId, otpType, code, time);
	}

}