package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
public class BankAttachment {

	@Column(name = "image_no")
	private int imageNo;
	
	@Column(name = "companyBank_id")
	private int companyBankId;
	
	private byte[] image;
	
	private int status;
	
	@Column(name = "creator")
	private String creator;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name = "updater")
	private String updater;
	
	@Column(name = "update_time")
	private Timestamp updateTime;

	public int getImageNo() {
		return imageNo;
	}

	public void setImageNo(int imageNo) {
		this.imageNo = imageNo;
	}

	public int getCompanyBankId() {
		return companyBankId;
	}

	public void setCompanyBankId(int companyBankId) {
		this.companyBankId = companyBankId;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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
