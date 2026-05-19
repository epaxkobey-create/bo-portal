package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.bo.WebsiteSystemSettingBO;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dao.WebsiteSystemSettingDAO;
import com.nv.commons.dto.WebsiteSystemSetting;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

public class WebsiteSystemSettingCache extends AbstractCache {

	private final static WebsiteSystemSettingCache instance = new WebsiteSystemSettingCache();

	private Map<String, WebsiteSystemSetting> cache = new ConcurrentHashMap<>();

	public static WebsiteSystemSettingCache getInstance() {
		return instance;
	}

	private long lastUpdateTime = 0;

	private static final long ERROR_VALUE = 1000;

	@Override
	protected void init() {
		try {
			Map<String, WebsiteSystemSetting> tmp = new ConcurrentHashMap<>();

			List<WebsiteSystemSetting> settingList;
			try (Connection conn = DBPool.getReadConnection()) {

				settingList = WebsiteSystemSettingDAO.getEntities(conn);
			}

			for (WebsiteSystemSetting setting : settingList) {
				tmp.put(buildKey(setting.getWebsiteType(), setting.getKey(), setting.getCurrency()), setting);
			}
			cache = tmp;

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();

			Timestamp queryTime = new Timestamp(lastUpdateTime);

			long maxUpdatedTime = queryTime.getTime();

			boolean isUpdated = false;

			List<WebsiteSystemSetting> settingList = WebsiteSystemSettingDAO
				.getEntities(conn, queryTime);

			for (WebsiteSystemSetting settingInDB : settingList) {
				String key = buildKey(settingInDB.getWebsiteType(), settingInDB.getKey(), settingInDB.getCurrency());

				WebsiteSystemSetting settingInCache = cache.get(key);

				if (settingInCache == null || !settingInDB.getUpdateTime().equals(settingInCache.getUpdateTime())) {
					cache.put(key, settingInDB);
					isUpdated = true;
				}

				maxUpdatedTime = Math.max(maxUpdatedTime, settingInDB.getUpdateTime().getTime());
			}

			if (isUpdated) {
				lastUpdateTime = maxUpdatedTime - ERROR_VALUE;
			} else {
				lastUpdateTime = maxUpdatedTime;
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	/*
	 * 給帶幣別參數使用
	 */
	public String getValueByKey(int websiteType, int currencyTypeId, int key) {

		CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);

		String settingKey = buildKey(websiteType, key, currencyType.name());

		WebsiteSystemSetting setting = cache.get(settingKey);

		if (setting != null) {
			return setting.getValue();
		}

		WebsiteSystemSetting settingInDB = WebsiteSystemSettingBO
			.getEntity(WebSiteType.getInstance(websiteType), key, currencyType.name());

		if (settingInDB == null) {

			final WebsiteSystemSetting nullSetting = new WebsiteSystemSetting();

			nullSetting.setWebsiteType(websiteType);
			nullSetting.setCurrency(currencyType.name());
			nullSetting.setKey(key);
			nullSetting.setValue(null);
			nullSetting.setUpdater("getValueByKey");
			nullSetting.setCreateTime(new Timestamp(System.currentTimeMillis()));
			nullSetting.setUpdateTime(new Timestamp(System.currentTimeMillis()));

			cache.put(settingKey, nullSetting);

			return null;
		}
		cache.put(settingKey, settingInDB);

		return settingInDB.getValue();
	}


	//For BoDomain
	public List<WebsiteSystemSetting> getSettingByKey(int websiteType, int key) {

		List<WebsiteSystemSetting> list = new ArrayList<>();

		WebSiteType webSiteType = WebSiteType.getInstance(websiteType);

		for (CurrencyType currencyType : webSiteType.getWebsiteInfo().getCurrencyTypes()) {

			WebsiteSystemSetting setting = cache.get(buildKey(websiteType, key, currencyType.name()));
			if (setting != null) {
				list.add(setting);
			}
		}

		return list;
	}


	@Override
	public void refresh() {
		update();
	}

	@Override
	public String getCacheInfo() {
		Map<String, Map<String, WebsiteSystemSetting>> cacheMap = new LinkedHashMap<>();
		cache.values().stream()
			.sorted(Comparator.comparing(WebsiteSystemSetting::getWebsiteType))
			.forEach(setting -> cacheMap.computeIfAbsent(
					setting.getWebsiteType() + "-" + WebSiteType.getInstance(setting.getWebsiteType()).name(),
					o -> new LinkedHashMap<>())
				.put(setting.getKey() + "-" + WebsiteSystemSettingType.getInstance(setting.getKey()).name(),
					setting));
		return JSONUtils.toJsonString(cacheMap);
	}

	private String buildKey(int websiteType, int settingKey, String currency) {
		return websiteType + "-" + currency + "-" + settingKey;
	}

}
