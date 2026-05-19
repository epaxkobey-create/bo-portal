package com.nv.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateRecord {
	
	@JsonProperty("before")
	private String beforeUpdate;
	@JsonProperty("after")
	private String afterUpdate;
	@JsonProperty("message")
	private String message;
	
	public UpdateRecord(String beforeUpdate, String afterUpdate, String message) {
		this.beforeUpdate = beforeUpdate;
		this.afterUpdate = afterUpdate;
		this.message = message;
	}
	
	public UpdateRecord() {}//jackson會用到

	public String getBeforeUpdate() {
		return beforeUpdate;
	}

	public void setBeforeUpdate(String beforeUpdate) {
		this.beforeUpdate = beforeUpdate;
	}

	public String getAfterUpdate() {
		return afterUpdate;
	}

	public void setAfterUpdate(String afterUpdate) {
		this.afterUpdate = afterUpdate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
