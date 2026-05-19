package com.nv.commons.model;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.PageUtils;

public class PageInfo {

	/**
	 * 每頁資料數
	 */
	private int pageSize = SystemConstants.PAGE_SIZE;

	/**
	 * 目前的頁數
	 */
	private int pageNumber = 1;

	/**
	 * 資料總數
	 */
	private int totalCount;

	/**
	 * for ElasticSearch searchAfter
	 */
	private Object[] searchAfter;

	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * @param page
	 *            The page to set.
	 */
	public void setPageNumber(int page) {
		this.pageNumber = page;
	}

	/**
	 * @return Returns the pageSize.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize
	 *            The pageSize to set.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return Returns the totalCount.
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount
	 *            The totalCount to set.
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		if(this.pageNumber != 1) {
			//避免有查詢條件的case在改變條件後，會產生當前頁數與總數不一致的情形
			adjustPageNumberByTotalPage();
		}
	}

	public Object[] getSearchAfter() {
		return searchAfter;
	}

	public void setSearchAfter(Object[] searchAfter) {
		this.searchAfter = searchAfter;
	}

	public boolean hasNext() {
		return this.pageNumber * this.pageSize < this.totalCount;
	}

	public int next() {
		if (hasNext()) {
			return this.pageNumber + 1;
		}
		return this.pageNumber;
	}

	public boolean hasPrevious() {
		return this.pageNumber > 1;
	}

	public int previous() {
		if (hasPrevious()) {
			return this.pageNumber - 1;
		}
		return this.pageNumber;
	}

	public int getTotalPage() {
		int totalPage = this.totalCount / this.pageSize;
		if (this.totalCount % this.pageSize > 0) {
			totalPage++;
		}
		return totalPage;
	}

	public int getOffSet() {
		return (pageNumber - 1) * pageSize;
	}

	public final int getFirstRowNumber() {
		return getPageSize() * (getPageNumber() - 1);
	}
	
	public final int getLastRowNumber() {
		return getPageSize() * getPageNumber();
	}
	
	public void writeJson(JsonGenerator jg) throws IOException {
		jg.writeFieldName("pageInfo");
		jg.writeStartObject();
		jg.writeNumberField("totalCount", getTotalCount());
		jg.writeNumberField("pageNumber", getPageNumber());
		jg.writeNumberField("pageSize", getPageSize());
		jg.writeEndObject();
	}
		
//	public String getPageInfoJson(String data) throws IOException {
//		JsonGenerateProcessor processor = jGenerator -> {
//			jGenerator.writeFieldName("pageInfo");
//			jGenerator.writeStartObject();
//			jGenerator.writeNumberField("totalPage", getTotalPage());
//			jGenerator.writeNumberField("currentPage", getPageNumber());
//			jGenerator.writeNumberField("totalRecords", getTotalCount());
//			jGenerator.writeNumberField("perPageSize", getPageSize());
//			jGenerator.writeEndObject();
//			jGenerator.writeFieldName("records");
//			jGenerator.writeRawValue(data);
//		};
//
//		return JSONUtils.getJSONString(processor);
//	}
	
//	public String getPageInfoJson(String data, String totalAmountJson) throws IOException {
//
//		JsonGenerateProcessor processor = jGenerator -> {
//			jGenerator.writeFieldName("pageInfo");
//			jGenerator.writeStartObject();
//			jGenerator.writeNumberField("totalPage", getTotalPage());
//			jGenerator.writeNumberField("currentPage", getPageNumber());
//			jGenerator.writeNumberField("totalRecords", getTotalCount());
//			jGenerator.writeNumberField("perPageSize", getPageSize());
//			jGenerator.writeEndObject();
//			jGenerator.writeFieldName("totalAmount");
//			jGenerator.writeRawValue(totalAmountJson);
//			jGenerator.writeFieldName("records");
//			jGenerator.writeRawValue(data);
//		};
//
//		return JSONUtils.getJSONString(processor);
//	}

	public String getPageInfoJson(String... args) throws IOException {
		if (args.length % 2 != 0) {
			throw new RuntimeException("error number of arguments");
		}

		JsonGenerateProcessor processor = jGenerator -> {
			jGenerator.writeFieldName("pageInfo");
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("totalPage", getTotalPage());
			jGenerator.writeNumberField("currentPage", getPageNumber());
			jGenerator.writeNumberField("totalRecords", getTotalCount());
			jGenerator.writeNumberField("perPageSize", getPageSize());
			jGenerator.writeEndObject();
			for (int i = 0; i < args.length; i = i + 2) {
				jGenerator.writeFieldName(args[i]);
				jGenerator.writeRawValue(args[i + 1]);
			}
		};
		return JSONUtils.getJSONString(processor);
	}

	public String getJsonWithoutTotalPage(String... args) {
		if (args.length % 2 != 0) {
			throw new RuntimeException("error number of arguments");
		}

		JsonGenerateProcessor processor = jGenerator -> {
			jGenerator.writeFieldName("pageInfo");
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("currentPage", getPageNumber());
			jGenerator.writeNumberField("perPageSize", getPageSize());
			jGenerator.writeEndObject();
			jGenerator.writeObjectField("searchAfter", getSearchAfter());
			for (int i = 0; i < args.length; i = i + 2) {
				jGenerator.writeFieldName(args[i]);
				jGenerator.writeRawValue(args[i + 1]);
			}
		};
		return JSONUtils.getJSONString(processor);
	}

	public String getDataTableJson(String data) throws IOException {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeFieldName(PageUtils.SHOW_DATA);
			jGenerator.writeRawValue(data);
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, getTotalCount());
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, getTotalCount());
			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}
	
	public String getDataTableJson(String data, String total) throws IOException {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeFieldName(PageUtils.SHOW_DATA);
			jGenerator.writeRawValue(data);
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, getTotalCount());
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, getTotalCount());
			jGenerator.writeFieldName(PageUtils.TOTAL_AMOUNT);
			jGenerator.writeRawValue(total);
			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}
	
	public String getDataTableJson(Object data) throws IOException {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeFieldName(PageUtils.SHOW_DATA);
			jGenerator.writeObject(data);
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, getTotalCount());
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, getTotalCount());
			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public void adjustPageNumberByTotalPage() {
		if (getPageNumber() > getTotalPage() || -1 == pageSize) {
			setPageNumber(1);
		}
	}
	
}