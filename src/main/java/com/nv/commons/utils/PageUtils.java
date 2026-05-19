package com.nv.commons.utils;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.dto.PageResult;
import org.apache.commons.collections.CollectionUtils;

public class PageUtils {
	
	public static String TOTAL_COUNT = "iTotalRecords";
	public static String DISPLAY_COUNT = "iTotalDisplayRecords";
	public static String TOTAL_AMOUNT = "iTotalAmount";
	public static String SHOW_DATA = "aaData";

	public static <T> PageResult<T> getPage(List<T> list, int pageNumber, int showCount) {
		return getPage(list, pageNumber, showCount, null);
	}
	
	public static <T> PageResult<T> getPage(List<T> list, int pageNumber, int showCount, DBOrderType orderType) {
		if (CollectionUtils.isEmpty(list)) {
			return new PageResult<>();
		}

		if (orderType != null && orderType == DBOrderType.DESC) {
			Collections.reverse(list);
		}
		
		Number totalCount = list.size();
		if (totalCount.intValue() == 0) {
			return new PageResult<T>();
		}
		
		if (showCount < 1) {
			showCount = totalCount.intValue();
		}
		
		int totalCount_int = totalCount.intValue();
		int totalPage = totalCount_int / showCount;
		if (totalCount_int % showCount != 0) {
			totalPage = totalPage + 1;
		}

		if (pageNumber < 1) {
			pageNumber = 1;
		} else if (pageNumber > totalPage) {
			pageNumber = totalPage;
		}
		
		int offset = (pageNumber - 1) * showCount;
		
		int endset = showCount * pageNumber;
		
		list = list.subList(offset, endset >= totalCount_int ? totalCount_int : endset);
		
		PageResult<T> pageResult = new PageResult<>();
		pageResult.setResultList(list);
		pageResult.setTotalCount(totalCount.longValue());
		pageResult.setShowCount(showCount);
		pageResult.setCurrentPage(pageNumber);
		pageResult.setTotalPage(totalPage);
		
		return pageResult;
	}
	
	public static String emptyDataTable() {
		JsonGenerateProcessor processor = (JsonGenerator jsonGenerator) -> {
			jsonGenerator.writeNumberField(TOTAL_COUNT, 0);
			jsonGenerator.writeNumberField(DISPLAY_COUNT, 0);
			jsonGenerator.writeArrayFieldStart(SHOW_DATA);
			jsonGenerator.writeEndArray();
		};
		return JSONUtils.getJSONString(processor);
	}
}
