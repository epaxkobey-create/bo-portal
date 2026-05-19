package com.nv.commons.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 回傳給前端的query page結果，含List<T> 與PageInfo
 * 
 * @author Ethan.Hou
 * 
 */
public class PageResult<T> {

	private long totalCount;

	private long showCount;

	private long currentPage;

	private long totalPage;

	private List<T> resultList = new ArrayList<>();

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getShowCount() {
		return showCount;
	}

	public void setShowCount(long showCount) {
		this.showCount = showCount;
	}

	public long getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(long currentPage) {
		this.currentPage = currentPage;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

}