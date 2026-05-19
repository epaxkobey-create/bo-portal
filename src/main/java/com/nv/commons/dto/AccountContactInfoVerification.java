package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.BinaryStatusType;

public class AccountContactInfoVerification {
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "CONTACT_TYPE")
	private int contactType;

	@Column(name = "CONTENT")
	private String content;

	@Column(name = "VERIFY_CODE")
	private String verifyCode;

	@Column(name = "VALID_FROM")
	private Timestamp validFrom;

	@Column(name = "VALID_TO")
	private Timestamp validTo;

	@Column(name = "IS_VERIFIED")
	private int isVerified;

	@Column(name = "CREATOR")
	private String creator;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATER")
	private String updater;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getContactType() {
		return contactType;
	}

	public void setContactType(int contactType) {
		this.contactType = contactType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public Timestamp getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Timestamp validFrom) {
		this.validFrom = validFrom;
	}

	public Timestamp getValidTo() {
		return validTo;
	}

	public void setValidTo(Timestamp validTo) {
		this.validTo = validTo;
	}

	public int getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(int isVerified) {
		this.isVerified = isVerified;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public AccountContactInfoVerification() {
		this.isVerified = BinaryStatusType.INACTIVE.unique();
	}

	public boolean isVerified() {
		return this.isVerified == BinaryStatusType.ACTIVE.unique();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()){
			return false;
		}

		AccountContactInfoVerification that = (AccountContactInfoVerification) o;
		return websiteType == that.websiteType &&
			contactType == that.contactType &&
			Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, contactType, content);
	}
}
