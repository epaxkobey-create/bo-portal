package com.nv.commons.dto;

import java.sql.Timestamp;


import com.nv.commons.annotation.Column;

/**
 * Title: com.nv.commons.dto.PGCompany<br>
 * Description: 線上支付公司-資料庫物件
 *
 * @author: Daniel.Hsieh
 * @version: 1.0
 */
public class PGCompany {
	// id
	// name
	// classname
	// create_time
	// update_time

	
	@Column(name = "id")
	private int id;

	
	@Column(name = "name")
	private String name;

	@Column(name = "classname")
	private String className;

	@Column(name = "api_url")
	private String apiURL;

	@Column(name = "sync_url")
	private String syncURL;

	@Column(name = "async_url")
	private String asyncURL;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	@Column(name = "status")
	private int status;

	@Column(name = "support_device_type")
	private int supportDeviceType;

	@Column(name = "pass_api_server")
	private int passAPIServer;
//
//	@Column(name = "support_bank_type")
//	private int supportBankType;
	
	private int purposeType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getApiURL() {
		return apiURL;
	}

	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}

	public String getSyncURL() {
		return syncURL;
	}

	public void setSyncURL(String syncURL) {
		this.syncURL = syncURL;
	}

	public String getAsyncURL() {
		return asyncURL;
	}

	public void setAsyncURL(String asyncURL) {
		this.asyncURL = asyncURL;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSupportDeviceType() {
		return supportDeviceType;
	}

	public void setSupportDeviceType(int supportDeviceType) {
		this.supportDeviceType = supportDeviceType;
	}

	public int getPassAPIServer() {
		return passAPIServer;
	}

	public void setPassAPIServer(int passAPIServer) {
		this.passAPIServer = passAPIServer;
	}

	public int getPurposeType() {
		return purposeType;
	}

	public void setPurposeType(int purposeType) {
		this.purposeType = purposeType;
	}

	//	public int getSupportBankType() {
//		return supportBankType;
//	}
//
//	public void setSupportBankType(int supportBankType) {
//		this.supportBankType = supportBankType;
//	}

}
