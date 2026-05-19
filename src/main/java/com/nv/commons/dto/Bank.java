package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;


import com.nv.commons.annotation.Column;

public class Bank implements Comparable<Bank> {

	@Column(name = "id")
	private int id;

	
	@Column(name = "bank_name")
	private String bankName;

	@Column(name = "bank_type")
	private int bankType;

	@Column(name = "image_path")
	private String imagePath;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	@Column(name = "update_time")
	private Timestamp updateTime;



	@Override
	public int compareTo(Bank o) {
		return this.id - o.id;
	}

	// Generally, the value of compareTo should return zero if and only if
	// equals returns true.
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Bank other)) {
			return false;
		}
		return id == other.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public int getBankType() {
		return bankType;
	}

	public void setBankType(int bankType) {
		this.bankType = bankType;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

}
