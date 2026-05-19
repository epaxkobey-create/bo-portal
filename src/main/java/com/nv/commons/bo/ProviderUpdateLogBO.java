package com.nv.commons.bo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.dao.ProviderUpdateLogDAO;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.ProviderUpdateLog;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;

import java.sql.Timestamp;

public class ProviderUpdateLogBO {
	public static String getFullProviderUpdateLog(int providerId, Timestamp startTime, Timestamp endTime,
		int type, CurrencyType currency,
		int webSiteType, String column, int pageNumber,
		int showCount, DBOrderType orderType) throws Exception {

		try {
			PageResult<ProviderUpdateLog> updateLogPageResult = getProviderUpdateLogPageResult(
				providerId, startTime, endTime, type, currency.unique(), column, pageNumber, showCount, orderType,
				webSiteType);

			JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
				jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, updateLogPageResult.getTotalCount());
				jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, updateLogPageResult.getTotalCount());
				jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

				for (ProviderUpdateLog log : updateLogPageResult.getResultList()) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("logType", log.getLogType());
					jGenerator.writeNumberField("id", log.getId());
					jGenerator.writeStringField("beforeUpdate", log.getAccountUpdateRecord().getBeforeUpdate());
					jGenerator.writeStringField("afterUpdate", log.getAccountUpdateRecord().getAfterUpdate());

					jGenerator.writeStringField("updater", log.getUpdater());
					jGenerator.writeStringField("updateTime",
						FormatUtils.dateFormat(log.getUpdateTime()));
					jGenerator.writeStringField("updaterIp", log.getUpdaterIp());
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray();
			};

			return JSONUtils.getJSONString(processor);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}

	}

	public static PageResult<ProviderUpdateLog> getProviderUpdateLogPageResult(
		int providerId, Timestamp startDate,
		Timestamp endDate, int type,
		int currency,
		String column, int pageNumber, int showCount, DBOrderType orderType, int websiteType
	) throws Exception {

		return DbExecutor.query(conn ->
			{
				try {
					return ProviderUpdateLogDAO.findProviderUpdateLogPageResult(
						conn, startDate, endDate, providerId, type, currency, column, pageNumber, showCount, orderType,
						websiteType
					);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		);
	}

}
