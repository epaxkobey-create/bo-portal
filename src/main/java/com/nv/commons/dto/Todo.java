package com.nv.commons.dto;


import java.math.BigDecimal;
import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class Todo {
	@Column(name = "id")
	private long id;

	@Column(name = "title", maxLength = 100)
	private String title;

	@Column(name = "desc", maxLength = 200)
	private String desc;

	@Column(name = "progress")
	private BigDecimal progress;

	@Column(name = "completed")
	private boolean completed;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getProgress() {
		return progress;
	}

	public void setProgress(BigDecimal progress) {
		this.progress = progress;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public final boolean equals(Object object) {
		if (!(object instanceof Todo todo))
			return false;

		return getId() == todo.getId();
	}

	@Override
	public int hashCode() {
		return Long.hashCode(getId());
	}
}

