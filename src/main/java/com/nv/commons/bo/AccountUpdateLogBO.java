package com.nv.commons.bo;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.ThreadPoolType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountUpdateLogDAO;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.PageResult;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PageUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class AccountUpdateLogBO {

	public static void insert(AccountUpdateLog accountUpdateLog) {
		GlobalThreadPool.execute(() -> {
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);
				AccountUpdateLogDAO.insert(conn, accountUpdateLog);
				conn.commit();
			} catch (Exception e) {
				DbUtils.rollback(conn);
				LogUtils.SYS.error(e.getMessage(), e);
			} finally {
				DbUtils.close(conn);
			}
		}, ThreadPoolType.ACCOUNT_UPDATE_LOG.getExecutor());
	}

	public static void insert(AccountUpdateLog accountUpdateLog, Timestamp updateTime) {
		GlobalThreadPool.execute(() -> {
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);
				AccountUpdateLogDAO.insert(conn, accountUpdateLog, updateTime);
				conn.commit();
			} catch (Exception e) {
				DbUtils.rollback(conn);
				LogUtils.SYS.error(e.getMessage(), e);
			} finally {
				DbUtils.close(conn);
			}
		}, ThreadPoolType.ACCOUNT_UPDATE_LOG.getExecutor());
	}

	public static void batchInsert(List<AccountUpdateLog> accountUpdateLogList) {
		GlobalThreadPool.execute(() -> {
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);
				AccountUpdateLogDAO.batchInsert(conn, accountUpdateLogList);
				conn.commit();
			} catch (Exception e) {
				DbUtils.rollback(conn);
				LogUtils.SYS.error(e.getMessage(), e);
			} finally {
				DbUtils.close(conn);
			}
		}, ThreadPoolType.ACCOUNT_UPDATE_LOG.getExecutor());
	}

	// MEMO: 每種 update type 都只會取回一筆，所以不用分頁
	public static String getAccountUpdateLog(String userId, WebSiteType webSiteType) throws Exception {
		Connection conn = null;
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try {
			conn = DBPool.getReadConnection();
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("recordList");

			List<AccountUpdateLog> accountUpdateLogList = AccountUpdateLogDAO.getAccountUpdateLog(conn, userId,
				webSiteType.unique());

			if (!CollectionUtils.isEmpty(accountUpdateLogList)) {
				for (AccountUpdateLog log : accountUpdateLogList) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("accountUpdateType", log.getLogType());
					jGenerator.writeStringField("updater", log.getUpdater());
					jGenerator.writeStringField("updateTime", FormatUtils.dateFormat(log.getUpdateTime()));
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray();
				jGenerator.writeEndObject();
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static String getFullAccountUpdateLog(String userId, Timestamp startTime, Timestamp endTime,
		int type, List<AccountUpdateType> accessUpdateTypeOption,
		List<Integer> managerCurrencyList, WebSiteType webSiteType, String column, int pageNumber,
		int showCount, DBOrderType orderType, LangMessage langMessage) throws Exception {

		boolean enableShowEmail = true;
//		boolean enableShowContactPhone = true;
//		boolean enableShowPartOfContactPhone = true;

		List<AccountUpdateType> filteredAccessUpdateTypeOption = accessUpdateTypeOption.stream()
			.filter(accessUpdateType -> {
				if (AccountUpdateType.EMAIL.unique() == accessUpdateType.unique()) {
					return true;
				}

				return true;
			}).collect(Collectors.toList());

		try {
			PageResult<AccountUpdateLog> pager = getAccountUpdateLogPageResult(userId, startTime, endTime, type,
				filteredAccessUpdateTypeOption, managerCurrencyList, webSiteType, column, pageNumber, showCount,
				orderType);

			JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
				jGenerator.writeNumberField("status", 200);
				jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pager.getTotalCount());
				jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pager.getTotalCount());
				jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

				for (AccountUpdateLog log : pager.getResultList()) {

					String beforeUpdate = StringUtils.EMPTY;
					String afterUpdate = StringUtils.EMPTY;

					if (log.getAccountUpdateRecord() != null) {
						if (log.getAccountUpdateRecord().getBeforeUpdate() != null) {
							if (log.getLogType() == AccountUpdateType.ADDRESS.unique()) {

								beforeUpdate = JSONUtils.formatJsonToKeyValueLines(
									log.getAccountUpdateRecord().getBeforeUpdate());

							} else {
								beforeUpdate = log.getAccountUpdateRecord().getBeforeUpdate();
							}
						}

						if (log.getAccountUpdateRecord().getAfterUpdate() != null) {
							if (log.getLogType() == AccountUpdateType.ADDRESS.unique()) {

								afterUpdate = JSONUtils.formatJsonToKeyValueLines(
									log.getAccountUpdateRecord().getAfterUpdate());

							} else {
								afterUpdate = log.getAccountUpdateRecord().getAfterUpdate();
							}
						}
					}

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("logType", log.getLogType());
					jGenerator.writeNumberField("id", log.getId());
					String beforeData = updateLogDisplayContent(log.getLogType(), beforeUpdate,
						langMessage);
					String afterData = updateLogDisplayContent(log.getLogType(), afterUpdate,  langMessage);

					jGenerator.writeStringField("beforeUpdate", beforeData);
					jGenerator.writeStringField("afterUpdate", afterData);

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

	private static PageResult<AccountUpdateLog> getAccountUpdateLogPageResult(String userId, Timestamp startTime,
		Timestamp endTime, int type, List<AccountUpdateType> accessUpdateTypeOption, List<Integer> managerCurrencyList,
		WebSiteType webSiteType, String column, int pageNumber, int showCount, DBOrderType orderType) {

		PageResult<AccountUpdateLog> result = new PageResult<>();

		try (Connection conn = DBPool.getReadConnection()) {
			result = AccountUpdateLogDAO.findAccountUpdateLogRpt(conn, startTime, endTime, userId,
				AccountUpdateType.getInstanceOf(type), accessUpdateTypeOption, -1, managerCurrencyList, column,
				pageNumber, showCount, orderType, webSiteType);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return result;
	}

	private static String updateLogDisplayContent(int logType, String content,
		LangMessage langMessage) {

		content = "null".equals(content) ? "" : content;

		if (!"".equals(content)) {
			if (logType == AccountUpdateType.ALLOW_GAME_TYPE.unique()) {
				content = GameType.getGameName(Long.parseLong(content), langMessage);
			}
		}
		return content;

	}

}
