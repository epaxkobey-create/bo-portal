package com.nv.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountAddress {
	@JsonProperty("line1")
	private String line1;

	@JsonProperty("country")
	private String country;

	@JsonProperty("postalCode")
	private String postalCode;

	@JsonProperty("subdivision")
	private String subdivision;

	@JsonProperty("city")
	private String city;

	@JsonProperty("street")
	private String street;

//	public AccountAddress() {
//	}

//	public AccountAddress(String line1, String country, String postalCode, String subdivision, String city, String street) {
//		this.line1 = line1;
//		this.country = country;
//		this.postalCode = postalCode;
//		this.subdivision = subdivision;
//		this.city = city;
//		this.street = street;
//	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getSubdivision() {
		return subdivision;
	}

	public void setSubdivision(String subdivision) {
		this.subdivision = subdivision;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
}