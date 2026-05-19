package com.nv.commons.dto;

import com.nv.commons.annotation.Column;

public class RoleResource {

	private int id;
	
	@Column(name = "PARENT_ID")
	private int parentId;
	
	private String title;
	
	@Column(name = "DISPLAY_ORDER")
	private int displayOrder;
	
	private String url;
	
	@Column(name = "MENU_LEVEL")
	private int menuLevel;
	
	private String icon;
	
	private String displayName;

	@Column(name = "STATUS")
	private int status;

	public String getDisplayName() {
		if (displayName == null) {
			displayName = "form.text.backOffice.menu." + id;
		}
		return displayName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMenuLevel() {
		return menuLevel;
	}

	public void setMenuLevel(int menuLevel) {
		this.menuLevel = menuLevel;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return getId() + "_" + getParentId() + "_" + getTitle() + "_" + getDisplayOrder();
	}
}
