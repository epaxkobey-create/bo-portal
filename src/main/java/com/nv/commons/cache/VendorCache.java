package com.nv.commons.cache;

import com.nv.commons.bo.VendorBO;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.VendorDAO;
import com.nv.commons.dao.WebsiteVendorDAO;
import com.nv.commons.dto.Vendor;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.VendorUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VendorCache extends AbstractCache {

	private final ConcurrentHashMap<Integer, Vendor> cache = new ConcurrentHashMap<>();

	private final Map<WebSiteType, ConcurrentHashMap<Integer, WebsiteVendor>> cacheByWebSite = new EnumMap<>(
		WebSiteType.class);

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final VendorCache instance = new VendorCache();

	private VendorCache() {
	}

	public static VendorCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {
			List<Vendor> allVendors = VendorDAO.findAll(conn);

			for (Vendor vendor : allVendors) {
				cache.put(vendor.getId(), vendor);
			}

			List<WebsiteVendor> websiteVendorList = WebsiteVendorDAO.getAll(conn);

			for (WebsiteVendor websiteVendor : websiteVendorList) {

				if (!WebSiteType.checkWebsiteType(websiteVendor.getWebsiteType())) {
					continue;
				}

				cacheByWebSite
					.computeIfAbsent(
						WebSiteType.getInstance(websiteVendor.getWebsiteType()), o -> new ConcurrentHashMap<>())
					.put(websiteVendor.getVendorId(), websiteVendor);
			}

			lastUpdateTime = System.currentTimeMillis();

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch Vendor list", ex);
		}
	}

	public Vendor getVendor(int id) {

		return cache.computeIfAbsent(id, vendorId -> {
			try {
				return VendorBO.getVendorById(id);

			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
			return null;
		});
	}



	/**
	 * 依據不同WebSiteType回傳WebsiteVendor
	 *
	 * @param webSiteType WebSiteType
	 * @param code        Vendor的code
	 * @return WebsiteVendor
	 */
	public WebsiteVendor getWebSiteVendor(WebSiteType webSiteType, String code) {

		Map<Integer, WebsiteVendor> mapInCache = cacheByWebSite
			.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>());

		Vendor vendor = cache.values().stream().filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
		if (vendor == null) {
			return null;
		}
		return mapInCache.get(vendor.getId());
	}

	public WebsiteVendor getWebSiteVendor(WebSiteType webSiteType, int vendorId) {

		Map<Integer, WebsiteVendor> mapInCache = cacheByWebSite
			.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>());

		return mapInCache.get(vendorId);
	}

	//沒有依照後台Vendor狀態過濾, 前台畫面不適合使用
	public List<WebsiteVendor> getByProviderId(WebSiteType webSiteType, int providerId) {

		Set<Integer> vendorIds = cache.values().stream()
			.filter(e -> e.getProviderId() == providerId)
			.map(Vendor::getId)
			.collect(Collectors.toSet());

		return cacheByWebSite.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>()).values()
			.stream()
			.filter(webSiteVendor -> vendorIds.contains(webSiteVendor.getVendorId()))
			.collect(Collectors.toList());
	}

	public Vendor getByVendorCode(String code) {
		return cache.values().stream().filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}


	/**
	 * used by GuestAjaxServlet and main.jsp
	 */
	// MEMO: allow show VendorStatusType.MAINTENANCE
	public List<WebsiteVendor> getWebsiteVendors(WebSiteType webSiteType, GameType gameType,
		CurrencyType currencyType) {

		return cacheByWebSite.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>()).values()
			.stream().filter(webSiteVendor -> VendorUtils.isVendorAvailable(webSiteType, webSiteVendor, currencyType)
				&& GameType.getGameTypes(webSiteVendor.getGameType()).contains(gameType))
			.sorted(Comparator.comparing(vendor -> 0))
			.collect(Collectors.toList());
	}

	@Override
	public void update() {
		Timestamp queryTimestamp = new Timestamp(lastUpdateTime);

		try (Connection conn = DBPool.getReadConnection()) {

			boolean isUpdated = false;
			long maxUpdateTime = queryTimestamp.getTime();

			//VendorList

			List<Vendor> vendorList = VendorDAO.getVendorByUpdateTime(conn, queryTimestamp);
			for (Vendor vendor : vendorList) {

				maxUpdateTime = Math.max(maxUpdateTime, vendor.getUpdateTime().getTime());

				Vendor vendorInCache = cache.get(vendor.getId());

				if (vendorInCache.getUpdateTime().getTime() != vendor.getUpdateTime().getTime()) {
					vendorInCache.setApiKey(vendor.getApiKey());
					vendorInCache.setStatus(vendor.getStatus());
					vendorInCache.setMaintenanceStart(vendor.getMaintenanceStart());
					vendorInCache.setMaintenanceEnd(vendor.getMaintenanceEnd());
					vendorInCache.setUpdateTime(vendor.getUpdateTime());
					vendorInCache.setTrialPlayType(vendor.getTrialPlayType());

					isUpdated = true;
				}
			}

			//WebsiteVendor
			List<WebsiteVendor> websiteVendorList = WebsiteVendorDAO.getVendorByUpdateTime(conn, queryTimestamp);

			for (WebsiteVendor websiteVendor : websiteVendorList) {

				maxUpdateTime = Math.max(maxUpdateTime, websiteVendor.getUpdateTime().getTime());

				WebSiteType webSiteType = WebSiteType.getInstance(websiteVendor.getWebsiteType());

				Map<Integer, WebsiteVendor> mapInCache = cacheByWebSite.computeIfAbsent(webSiteType,
					k -> new ConcurrentHashMap<>());

				WebsiteVendor vendorInCache = mapInCache.get(websiteVendor.getVendorId());

				if (vendorInCache == null) {
					mapInCache.put(websiteVendor.getVendorId(), websiteVendor);
					isUpdated = true;
					continue;
				}

				if (vendorInCache.getUpdateTime().getTime() != websiteVendor.getUpdateTime().getTime()) {
					vendorInCache.setDisplayName(websiteVendor.getDisplayName());
					vendorInCache.setGameType(websiteVendor.getGameType());
					vendorInCache.setCategories(websiteVendor.getCategories());
					vendorInCache.setTitle(websiteVendor.getTitle());
					vendorInCache.setDescription(websiteVendor.getDescription());
					vendorInCache.setStatus(websiteVendor.getStatus());
					vendorInCache.setMaintenanceStart(websiteVendor.getMaintenanceStart());
					vendorInCache.setMaintenanceEnd(websiteVendor.getMaintenanceEnd());
					vendorInCache.setWebIcon(websiteVendor.getWebIcon());
					vendorInCache.setH5Icon(websiteVendor.getH5Icon());
					vendorInCache.setUpdateTime(websiteVendor.getUpdateTime());

					isUpdated = true;
				}
			}

			if (isUpdated) {

				lastUpdateTime = maxUpdateTime - ERROR_VALUE;

				update();

			} else {
				lastUpdateTime = maxUpdateTime;
			}

		} catch (Exception ex) {
			LogUtils.SYS.error("error while refresh WebsiteVendor ", ex);
		}
	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh vendor cache.");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public Map<Integer, WebsiteVendor> getWebsiteVendors(WebSiteType websiteType) {
		return cacheByWebSite.computeIfAbsent(websiteType, k -> new ConcurrentHashMap<>());
	}


	public List<WebsiteVendor> getNonInactiveWebsiteVendors(WebSiteType websiteType) {
		Map<Integer, WebsiteVendor> websiteVendors = getWebsiteVendors(websiteType);
		return websiteVendors.values().stream()
			.filter(o -> VendorUtils.isVendorAvailableIgnoreCurrency(websiteType, o)).collect(Collectors.toList());
	}

}
