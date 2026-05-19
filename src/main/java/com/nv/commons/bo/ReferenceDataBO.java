package com.nv.commons.bo;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.nv.commons.dao.ReferenceDataDAO;
import com.nv.commons.dto.ReferenceData;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

public class ReferenceDataBO {

	public static String getText(String referenceKey, Timestamp startTime, Timestamp endTime) throws Exception {
		try {
			return DbExecutor.query(conn -> ReferenceDataDAO.get(conn, referenceKey, startTime, endTime)
				.stream()
				.sorted(Comparator.comparing(ReferenceData::getTextOrder))
				.map(ReferenceData::getText)
				.collect(Collectors.joining(StringUtils.EMPTY)));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}
	}

}
