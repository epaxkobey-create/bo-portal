package com.nv.commons.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nv.commons.annotation.Column;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;
import org.apache.commons.lang3.StringUtils;

public class KycPersonalInfo {
	@Column(name = "ACCOUNT_DOCUMENT_ID")
	private Long accountDocumentId;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "DOCUMENT_NO")
	private String documentNo;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Column(name = "DOB")
	private Timestamp dob;

	@Column(name = "STREET")
	private String street;

	@Column(name = "CITY")
	private String city;

	@Column(name = "POSTAL_CODE")
	private String postalCode;

	@Column(name = "COUNTRY")
	private String country;

	@Column(name = "CREATOR")
	private String creator;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATER")
	private String updater;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@JsonProperty("fullName")
	public String getFullName() {
		return StringUtils.trimToEmpty(StringUtils.trimToEmpty(this.firstName) + " " + StringUtils.trimToEmpty(this.lastName));
	}

	@JsonProperty("dobStr")
	public String getDobStr() {
		return this.dob == null ? "" : DateUtils.toString(this.dob, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
	}

	public Long getAccountDocumentId() {
		return accountDocumentId;
	}

	public void setAccountDocumentId(Long accountDocumentId) {
		this.accountDocumentId = accountDocumentId;
	}

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

	public String getDocumentNo() {
		return documentNo;
	}

	public void setDocumentNo(String documentNo) {
		this.documentNo = documentNo;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Timestamp getDob() {
		return dob;
	}

	public void setDob(Timestamp dob) {
		this.dob = dob;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
}
