package com.nv.commons.cache;

import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ProviderDAO;
import com.nv.commons.dao.WebsiteProviderDAO;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ProviderUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 主要是快取所有的遊戲商以及所有網站對應的遊戲商
 */
public class ProviderCache extends AbstractCache {

	// 誤差值
	private static final long ERROR_VALUE = 1000;
	private static final ProviderCache instance = new ProviderCache();

	// 紀錄遊戲商的資料
	private final ConcurrentHashMap<Integer, Provider> cache = new ConcurrentHashMap<>();

	// 紀錄某個網站，有哪些遊戲商
	private final Map<WebSiteType, ConcurrentHashMap<Integer, WebsiteProvider>> cacheByWebSite = new EnumMap<>(
		WebSiteType.class);

	private long lastUpdateTime = 0;

	private ProviderCache() {
	}

	public static ProviderCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try {
			// 1. 記錄所有遊戲商
			List<Provider> providerList;
			try (Connection conn = DBPool.getReadConnection()) {
				providerList = ProviderDAO.findAll(conn);
			}

			for (Provider provider : providerList) {
				cache.put(provider.getId(), provider);
			}

			// 2. 記錄所有網站的遊戲商對應
			List<WebsiteProvider> websiteProviderList;
			try (Connection conn = DBPool.getReadConnection()) {

				websiteProviderList = WebsiteProviderDAO.getAll(conn);
			}

			for (WebsiteProvider websiteProvider : websiteProviderList) {

				WebSiteType webSiteType = WebSiteType.getInstance(websiteProvider.getWebsiteType());

				if (webSiteType == null) {
					continue;
				}

				Map<Integer, WebsiteProvider> mapInCache = cacheByWebSite
					.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>());

				mapInCache.put(websiteProvider.getProviderId(), websiteProvider);
			}

			lastUpdateTime = System.currentTimeMillis();

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch Provider list", ex);
		}
	}

	public List<Provider> getProvider(WebSiteType webSiteType) {
		Map<Integer, WebsiteProvider> mapInCache = cacheByWebSite.get(webSiteType);

		if (mapInCache == null) {
			return Collections.emptyList();
		}

		List<Provider> providers = new ArrayList<>();
		for (WebsiteProvider webSiteProvider : mapInCache.values()) {
			Provider provider = cache.get(webSiteProvider.getProviderId());
			if (provider == null) {
				LogUtils.SYS.warn("ProviderCache: no Provider found for providerId={} referenced by WebsiteProvider (webSiteType={})",
					webSiteProvider.getProviderId(), webSiteType);
				continue;
			}
			if(webSiteProvider.getStatus() != ProviderStatusType.INACTIVE.unique()
				&& provider.getStatus() != ProviderStatusType.INACTIVE.unique()) {
				providers.add(provider);
			}
		}
		return providers;
	}

	public List<WebsiteProvider> getWebsiteProvider(WebSiteType webSiteType) {

		Map<Integer, WebsiteProvider> mapInCache = cacheByWebSite.get(webSiteType);

		if (mapInCache == null) {
			return Collections.emptyList();
		}

		return mapInCache.values().stream().filter(ProviderUtils::isProviderNonInactive)
			.collect(Collectors.toList());
	}

	public WebsiteProvider getWebsiteProvider(WebSiteType webSiteType, int id) {
		return cacheByWebSite
			.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>())
			.get(id);
	}


	public Provider getProvider(int id) {
		return cache.get(id);
	}

	public Provider getByVendorId(int id) {
		Vendor vendor = VendorCache.getInstance().getVendor(id);
		return cache.get(vendor.getProviderId());
	}



	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh provider cache.");
	}

	/*
	 * 更新的方式為置換整個物件，因而若有將Provider、WebsiteProvider宣告為全域變數的地方，要使用時需再回cache重拿
	 */
	@Override
	public void update() {

		Timestamp queryTimestamp = new Timestamp(lastUpdateTime);

		try (Connection conn = DBPool.getReadConnection()) {

			boolean isUpdated = false;

			boolean isConnectionInfoClear = false;

			long maxUpdateTime = queryTimestamp.getTime();

			// 1. Provider
			List<Provider> providerList = ProviderDAO.getProviderByUpdateTime(conn, queryTimestamp);
			for (Provider provider : providerList) {

				maxUpdateTime = Math.max(maxUpdateTime, provider.getUpdateTime().getTime());

				Provider providerInCache = cache.get(provider.getId());
				if (providerInCache == null) {
					cache.put(provider.getId(), provider);
					isUpdated = true;
					isConnectionInfoClear = true;
					LogUtils.SYS.error(
						"providerInCache null while update providerCache, providerId : " + provider.getId());
					continue;
				}
				if (provider.getUpdateTime().compareTo(providerInCache.getUpdateTime()) > 0) {
					cache.put(provider.getId(), provider);
					isUpdated = true;

					isConnectionInfoClear = isConnectionInfoClear ||
						!provider.getConnectionInfo().equals(providerInCache.getConnectionInfo());
				}
			}

			//2. WebsiteProvider
			List<WebsiteProvider> websiteProviderList = WebsiteProviderDAO.getProviderByUpdateTime(conn,
				queryTimestamp);

			for (WebsiteProvider websiteProvider : websiteProviderList) {

				maxUpdateTime = Math.max(maxUpdateTime, websiteProvider.getUpdateTime().getTime());

				WebSiteType webSiteType = WebSiteType.getInstance(websiteProvider.getWebsiteType());

				Map<Integer, WebsiteProvider> mapInCache = cacheByWebSite
					.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>());

				WebsiteProvider websiteProviderInCache = mapInCache.get(websiteProvider.getProviderId());

				if (websiteProviderInCache == null) {
					mapInCache.put(websiteProvider.getProviderId(), websiteProvider);
					isUpdated = true;
					isConnectionInfoClear = true;
					continue;
				}

				if (websiteProvider.getUpdateTime().compareTo(websiteProviderInCache.getUpdateTime()) > 0) {
					mapInCache.put(websiteProvider.getProviderId(), websiteProvider);
					isUpdated = true;

					isConnectionInfoClear = isConnectionInfoClear ||
						!websiteProvider.getExtendConnectionInfo()
							.equals(websiteProviderInCache.getExtendConnectionInfo());
				}
			}

			if (isUpdated) {
				if (isConnectionInfoClear) {
					ProviderProxyCache.getInstance().clearConnectionInfo();
				}

				lastUpdateTime = maxUpdateTime - ERROR_VALUE;

				update();

			} else {
				lastUpdateTime = maxUpdateTime;
			}

		} catch (Exception ex) {
			LogUtils.SYS.error("error while refresh WebsiteProvider ", ex);
		}
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}


	public Provider findByCode (String code) {
		for (Provider provider : cache.values()) {
			if (provider.getSystemCode().equals(code)) {
				return provider;
			}
		}
		return null;
	}

}
