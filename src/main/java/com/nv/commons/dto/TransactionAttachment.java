package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.WebSiteType;
public class TransactionAttachment {
	
	@Column(name = "image_no")
	private int imageNo;
	
	@Column(name = "moneytransaction_id")
	private long moneytransactionId;
	
	private byte[] image;
	
	@Column(name = "creator")
	private String creator;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name = "updater")
	private String updater;
	
	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "image_path")
	private String imagePath;

	private WebSiteType webSiteType;

	private String paymentType;

	private String fileName;

	public int getImageNo() {
		return imageNo;
	}

	public void setImageNo(int imageNo) {
		this.imageNo = imageNo;
	}

	public long getMoneytransactionId() {
		return moneytransactionId;
	}

	public void setMoneytransactionId(long moneytransactionId) {
		this.moneytransactionId = moneytransactionId;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
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

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public void setWebSiteType(WebSiteType webSiteType) {
		this.webSiteType = webSiteType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPassword(){
		return moneytransactionId + updater;
	}

	public boolean checkUploadData(){
		return webSiteType != null && image != null && !paymentType.isEmpty() && !fileName.isEmpty();
	}
}
