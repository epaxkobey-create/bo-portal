package com.nv.commons.cache;

import com.nv.commons.dao.WebsiteInfoDAO;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsiteInfoCache extends AbstractCache {

	private final Map<Integer, WebsiteInfo> cache = new ConcurrentHashMap<>();

	private static final long ERROR_VALUE = 1000;

	private Timestamp lastUpdateTime = new Timestamp(0);

	private static final WebsiteInfoCache instance = new WebsiteInfoCache();

	public static WebsiteInfoCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {

			List<WebsiteInfo> websiteInfoList = WebsiteInfoDAO.getAll(conn);
			for (WebsiteInfo info : websiteInfoList) {
				cache.put(info.getId(), info);
			}

		} catch (Exception e) {
			LogUtils.operator.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {

		boolean isUpdated = false;

		Timestamp maxUpdateTime = Timestamp.from(lastUpdateTime.toInstant());

		try (Connection conn = DBPool.getReadConnection()) {
			List<WebsiteInfo> websiteInfoList = WebsiteInfoDAO.get(conn, maxUpdateTime);
			for (WebsiteInfo websiteInfo : websiteInfoList) {

				maxUpdateTime = DateUtils.max(maxUpdateTime, websiteInfo.getUpdateTime());

				WebsiteInfo infoInCache = cache.get(websiteInfo.getId());

				if (infoInCache == null || websiteInfo.getUpdateTime().compareTo(infoInCache.getUpdateTime()) > 0) {

					cache.put(websiteInfo.getId(), websiteInfo);
					isUpdated = true;
				}
			}

			if (isUpdated) {
				lastUpdateTime = new Timestamp(maxUpdateTime.getTime() - ERROR_VALUE);
				update();
			} else {
				lastUpdateTime = maxUpdateTime;
			}

		} catch (Exception e) {
			LogUtils.operator.error(e.getMessage(), e);
		}

		LogUtils.operator.error("update WebsiteInfo Cache");
	}

	@Override
	public void refresh() {
		init();
		LogUtils.operator.error("refresh WebsiteInfo Cache");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public WebsiteInfo getByWebType(int webSiteType) {
		return cache.get(webSiteType);
	}

	public List<WebsiteInfo> getAll() {
		return new ArrayList<>(cache.values());
	}

}
