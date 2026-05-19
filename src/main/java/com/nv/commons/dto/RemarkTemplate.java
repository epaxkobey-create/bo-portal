package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nv.commons.annotation.Column;

public class RemarkTemplate {

	private long id;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "REMARK_TYPE")
	private int remarkType;

	@Column(name = "PARENT_ACCESS_RIGHT")
	private int parentAccessRight;

	@Column(name = "ACCESS_RIGHT")
	private String accessRight;

	private String title;

	private String template;

	@Column(name = "DISPLAY_ORDER")
	private int displayOrder;

	@Column(name = "currency_type_id")
	private int currencyTypeId;

	private int status;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	private String creator;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	private String updater;

	private List<Integer> accessRightList;

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

	public int getRemarkType() {
		return remarkType;
	}

	public void setRemarkType(int remarkType) {
		this.remarkType = remarkType;
	}

	public int getParentAccessRight() {
		return parentAccessRight;
	}

	public void setParentAccessRight(int parentAccessRight) {
		this.parentAccessRight = parentAccessRight;
	}

	public String getAccessRight() {
		return accessRight;
	}

	public void setAccessRight(String accessRight) {
		this.accessRight = accessRight;
		if (accessRight != null) {
			this.accessRightList = Arrays.stream(accessRight.split(",")).map(Integer::parseInt)
				.collect(Collectors.toList());
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public List<Integer> getAccessRightList() {
		return accessRightList;
	}

	public void setAccessRightList(List<Integer> accessRightList) {
		this.accessRightList = accessRightList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemarkTemplate that = (RemarkTemplate) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
