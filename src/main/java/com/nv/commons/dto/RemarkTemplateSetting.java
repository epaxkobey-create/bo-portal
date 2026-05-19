package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class RemarkTemplateSetting {

	@Column(name = "ID")
	private long id;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "CURRENCY_TYPE_ID")
	private int currencyTypeId;

	@Column(name = "REMARK_TYPE")
	private int remarkType;

	@Column(name = "ACTION_TYPE")
	private String actionType;

	@Column(name = "MANDATORY")
	private Boolean mandatory;

	@Column(name = "UN_EDITABLE")
	private Boolean unEditable;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getRemarkType() {
		return remarkType;
	}

	public void setRemarkType(int remarkType) {
		this.remarkType = remarkType;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean getUnEditable() {
		return unEditable;
	}

	public void setUnEditable(Boolean unEditable) {
		this.unEditable = unEditable;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

//	public static RemarkTemplateSettingBuilder builder() {
//		return new RemarkTemplateSettingBuilder();
//	}

//	public static class RemarkTemplateSettingBuilder {
//		private long id;
//		private int websiteType;
//		private int currencyTypeId;
//		private int remarkType;
//		private String actionType;
//		private Boolean mandatory;
//		private Boolean unEditable;
//		private Timestamp createTime;
//
//		public RemarkTemplateSettingBuilder id(long id) {
//			this.id = id;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder websiteType(int websiteType) {
//			this.websiteType = websiteType;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder currencyTypeId(int currencyTypeId) {
//			this.currencyTypeId = currencyTypeId;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder remarkType(int remarkType) {
//			this.remarkType = remarkType;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder actionType(String actionType) {
//			this.actionType = actionType;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder mandatory(Boolean mandatory) {
//			this.mandatory = mandatory;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder unEditable(Boolean unEditable) {
//			this.unEditable = unEditable;
//			return this;
//		}
//
//		public RemarkTemplateSettingBuilder createTime(Timestamp createTime) {
//			this.createTime = createTime;
//			return this;
//		}
//
//		public RemarkTemplateSetting build() {
//			RemarkTemplateSetting setting = new RemarkTemplateSetting();
//			setting.setId(this.id);
//			setting.setWebsiteType(this.websiteType);
//			setting.setCurrencyTypeId(this.currencyTypeId);
//			setting.setRemarkType(this.remarkType);
//			setting.setActionType(this.actionType);
//			setting.setMandatory(this.mandatory);
//			setting.setUnEditable(this.unEditable);
//			setting.setCreateTime(this.createTime);
//			return setting;
//		}
//	}
}
