package com.nv.commons.bo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.dao.GameUpdateLogDAO;
import com.nv.commons.dto.GameUpdateLog;
import com.nv.commons.dto.PageResult;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;

import java.sql.Timestamp;

public class GameUpdateLogBO {

	public static String getFullGameUpdateLog(int gameId, Timestamp startTime, Timestamp endTime,
		int type, CurrencyType currency,
		int webSiteType, String column, int pageNumber,
		int showCount, DBOrderType orderType) throws Exception {

		try {
			PageResult<GameUpdateLog> gameUpdateLogPageResult = getGameUpdateLogPageResult(
				gameId, startTime, endTime, type, currency.unique(), column, pageNumber, showCount, orderType,
				webSiteType);

			JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
				jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, gameUpdateLogPageResult.getTotalCount());
				jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, gameUpdateLogPageResult.getTotalCount());
				jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

				for (GameUpdateLog log : gameUpdateLogPageResult.getResultList()) {
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

	public static PageResult<GameUpdateLog> getGameUpdateLogPageResult(
		int gameId, Timestamp startDate,
		Timestamp endDate, int type,
		int currency,
		String column, int pageNumber, int showCount, DBOrderType orderType, int websiteType
	) throws Exception {

		return DbExecutor.query(conn -> GameUpdateLogDAO.findGameUpdateLogPageResult(conn, startDate, endDate, gameId, type, currency,
			column, pageNumber, showCount, orderType, websiteType));
	}

	public static void insertGameUpdateLog(GameUpdateLog gameUpdateLog) throws Exception {

		DbExecutor.update(conn ->
			GameUpdateLogDAO.insert(conn, gameUpdateLog)
		);
	}

}
