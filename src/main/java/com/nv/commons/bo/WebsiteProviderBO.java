package com.nv.commons.bo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.constants.CacheType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.ProviderUpdateType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ProviderUpdateLogDAO;
import com.nv.commons.dao.WebsiteProviderDAO;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.ProviderUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.NotifyUtils;
import com.nv.commons.utils.PageUtils;

import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebsiteProviderBO {

	/*
	 * WebsiteProvider & Provider 狀態都需要檢查
	 *
	 */
	public static String getJsonByWebsiteType(WebSiteType websiteType, int pageNumber, int showCount, DBOrderType orderType, String column) throws Exception {

		try (Connection conn = DBPool.getReadConnection()) {


			PageResult<WebsiteProvider> pageResult = WebsiteProviderDAO.getAllProviders(conn,pageNumber,showCount,column, orderType, websiteType.unique());

			JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
				jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, pageResult.getTotalCount());
				jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, pageResult.getTotalCount());
				jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);

				for (WebsiteProvider wp : pageResult.getResultList()) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("id", wp.getProviderId());
					jGenerator.writeStringField("displayName", wp.getDisplayName());
					jGenerator.writeNumberField("status", wp.getStatus());
					jGenerator.writeNumberField("displayOrder", wp.getDisplayOrder());

					if (wp.getMaintenanceStart() != null) {
						jGenerator.writeStringField("maintenanceStart", FormatUtils.dateFormat(wp.getMaintenanceStart()));
					}
					if(wp.getMaintenanceEnd() != null){
						jGenerator.writeStringField("maintenanceEnd", FormatUtils.dateFormat(wp.getMaintenanceEnd()));
					}
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





	public static void update(WebsiteProvider provider) throws Exception {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			WebsiteProviderDAO.updateProvider(conn, provider);

			conn.commit();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.close(conn);
		}

		ProviderCache.getInstance().update();
		NotifyUtils.updateCache(CacheType.PROVIDER_CACHE);
	}



	public static void compareProviderObjectAndInsertUpdateLog(
		WebsiteProvider oldProvider, WebsiteProvider newProvider,
		 String updater, String updaterIp, int currencyTypeId) throws Exception {
		if (oldProvider == null || newProvider == null || oldProvider.equals(newProvider)) {
			return;
		}

		List<ProviderUpdateLog> updateLogs = new ArrayList<>();

		if (!Objects.equals(oldProvider.getDisplayName(), newProvider.getDisplayName())) {
			updateLogs.add(generateUpdateLog(String.valueOf(newProvider.getProviderId()),
				ProviderUpdateType.DISPLAY_NAME.unique(), newProvider.getWebsiteType(),
				new UpdateRecord(nullToDefault(oldProvider.getDisplayName()),
					nullToDefault(newProvider.getDisplayName()), "provider name updated"),
				updater, updaterIp,currencyTypeId
			));
		}

		if (!Objects.equals(oldProvider.getStatus(), newProvider.getStatus())) {
			updateLogs.add(generateUpdateLog(String.valueOf(newProvider.getProviderId()),
				ProviderUpdateType.STATUS.unique(),newProvider.getWebsiteType(),
				new UpdateRecord(nullToDefault(oldProvider.getStatus()),
					nullToDefault(newProvider.getStatus()), "provider status updated"),
				updater, updaterIp,currencyTypeId
			));
		}

		String newMaintainStart = newProvider.getMaintenanceStart() != null ?
			DateTimeBuilder.localDateTime(newProvider.getMaintenanceStart())
				.toString(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss) : null;

		String newMaintenanceEnd = newProvider.getMaintenanceEnd() != null ?
			DateTimeBuilder.localDateTime(newProvider.getMaintenanceEnd())
				.toString(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss) : null;

		String newDateRange = (newMaintainStart != null && newMaintenanceEnd != null) ?
			String.join(" - ", newMaintainStart, newMaintenanceEnd) : null;

		String oldMaintainStart = oldProvider.getMaintenanceStart() != null ?
			DateTimeBuilder.localDateTime(oldProvider.getMaintenanceStart())
				.toString(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss) : null;
		String oldMaintenanceEnd = oldProvider.getMaintenanceEnd() != null ?
			DateTimeBuilder.localDateTime(oldProvider.getMaintenanceEnd())
				.toString(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss) : null;
		String oldDateRange = (oldMaintainStart != null && oldMaintenanceEnd != null) ?
			String.join(" - ", oldMaintainStart, oldMaintenanceEnd) : null;

		if (!Objects.equals(oldDateRange, newDateRange)) {
			updateLogs.add(generateUpdateLog(String.valueOf(newProvider.getProviderId()),
				ProviderUpdateType.MAINTAIN_DATE.unique(), newProvider.getWebsiteType(),
				new UpdateRecord(nullToDefault(oldDateRange),
					nullToDefault(newDateRange), "provider maintenance date updated"),
				updater, updaterIp,currencyTypeId
			));
		}
		DbExecutor.update(conn ->
			ProviderUpdateLogDAO.insertAll(conn, updateLogs)
		);

	}

	private static ProviderUpdateLog generateUpdateLog(
		String providerId, int logType, int websiteType,
		UpdateRecord updateRecord, String updater, String updaterIp,int currencyTypeId

	) {
		ProviderUpdateLog updateLog = new ProviderUpdateLog();
		updateLog.setProviderId(providerId);
		updateLog.setLogType(logType);
		updateLog.setWebsiteType(websiteType);
		updateLog.setRecords(JSONUtils.toJsonString(updateRecord));
		updateLog.setUpdater(updater);
		updateLog.setUpdaterIp(updaterIp);
		updateLog.setLogTypeStr(ProviderUpdateType.getInstanceOf(logType).getName());
		updateLog.setCurrencyTypeId(currencyTypeId);
		return updateLog;
	}

	private static String nullToDefault(Object value) {
		if (value == null) {
			return "-";
		}
		String strValue = value.toString();
		if (strValue.trim().isEmpty()) {
			return "-";
		}
		return strValue;
	}

	public static void kickoutAllUser(int providerId, CurrencyType currencyType, WebSiteType webSiteType) throws Exception {
		ProviderProxy proxy = ProviderProxyCache.getInstance().getProviderProxy(webSiteType, providerId, currencyType);
		proxy.kickOutAllUser();
	}

	public static void kickOutByGame(int providerId, CurrencyType currencyType, WebSiteType webSiteType, Game game)
		throws Exception {
		ProviderProxy proxy = ProviderProxyCache.getInstance().getProviderProxy(webSiteType, providerId, currencyType);
		proxy.kickOutByGame(game);
	}

	public static String getSingleProviderDetails(int providerId, WebSiteType websiteType) throws Exception {

		WebsiteProvider provider = ProviderCache.getInstance().getWebsiteProvider(websiteType, providerId);

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("id", provider.getProviderId());
			jGenerator.writeStringField("providerName", provider.getDisplayName());
			jGenerator.writeNumberField("status", provider.getStatus());
			if (provider.getMaintenanceStart() != null) {
				jGenerator
					.writeStringField("maintenanceStart", FormatUtils.dateFormat(provider.getMaintenanceStart()));
			}
			if (provider.getMaintenanceEnd() != null) {
				jGenerator.writeStringField("maintenanceEnd", FormatUtils.dateFormat(provider.getMaintenanceEnd()));
			}

			jGenerator.writeEndObject();

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	public static List<String> getAllActivePlayers(int providerId, CurrencyType currencyType, WebSiteType webSiteType)throws Exception {
		ProviderProxy proxy = ProviderProxyCache.getInstance().getProviderProxy(webSiteType, providerId, currencyType);
		return proxy.getAllActiveUsers();
	}

}
