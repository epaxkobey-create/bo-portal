package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class BinData {

	@Column(name = "id")
	private Long id;

	@Column(name = "bin")
	private String bin;

	@Column(name = "card_scheme_type")
	private String cardSchemeType;

	@Column(name = "bank_name")
	private String bankName;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getCardSchemeType() {
		return cardSchemeType;
	}

	public void setCardSchemeType(String cardSchemeType) {
		this.cardSchemeType = cardSchemeType;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
}