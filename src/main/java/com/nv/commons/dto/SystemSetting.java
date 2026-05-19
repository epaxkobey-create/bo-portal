package com.nv.commons.dto;

import java.io.IOException;
import java.sql.Timestamp;

import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.annotation.Column;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

/**
 * @author Neutec
 */
public class SystemSetting {
	//	key
	//	value
	//	remark
	//	create_time
	//	update_time

	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

	private JsonNode jsonNode = null;

	@Column(name = "image")
	private byte[] image;

	@Column(name = "remark")
	private String remark;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public JsonNode getJsonNode() {
		if (jsonNode == null) {
			try {
				jsonNode = JSONUtils.getObjectMapper().readTree(this.value);
			} catch (IOException e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
		return jsonNode;
	}

	public void setJsonNode(JsonNode jsonNode) {
		this.jsonNode = jsonNode;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

}
