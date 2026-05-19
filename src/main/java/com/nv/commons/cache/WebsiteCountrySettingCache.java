package com.nv.commons.cache;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteCountrySettingType;
import com.nv.commons.dao.WebsiteCountrySettingDAO;
import com.nv.commons.dto.WebsiteCountrySetting;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class WebsiteCountrySettingCache extends AbstractCache {

	private static final WebsiteCountrySettingCache instance = new WebsiteCountrySettingCache();

	private ConcurrentHashMap<WebSiteType, Set<WebsiteCountrySetting>> cache = new ConcurrentHashMap<>();


	public static WebsiteCountrySettingCache getInstance() {
		return instance;
	}

	private static final Comparator<WebsiteCountrySetting> COMPARATOR = Comparator
		.comparing(WebsiteCountrySetting::getDisplayOrder)
		.thenComparing(WebsiteCountrySetting::getCreateTime);

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {
			List<WebsiteCountrySetting> websiteCountrySettingList = WebsiteCountrySettingDAO.getAll(conn);

			ConcurrentHashMap<WebSiteType, Set<WebsiteCountrySetting>> temp = new ConcurrentHashMap<>();

			websiteCountrySettingList.stream()
				.filter(
					websiteCountrySetting -> WebSiteType.checkWebsiteType(websiteCountrySetting.getWebsiteType()))
				.forEach(
					k -> temp
						.computeIfAbsent(WebSiteType.getInstance(k.getWebsiteType()), t -> new CopyOnWriteArraySet<>())
						.add(k)
				);
			cache = temp;
		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch WebsiteCountrySetting", ex);
		}
	}

	@Override
	public void update() {
		init();

	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh WebsiteCountrySetting cache.");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	private Stream<WebsiteCountrySetting> getAsStream(WebSiteType websiteType, CountryType countrytype,
		WebsiteCountrySettingType settingType) {
		Stream<WebsiteCountrySetting> stream =
			websiteType != null
				? cache.computeIfAbsent(websiteType, k -> new CopyOnWriteArraySet<>()).stream()
				: cache.values().stream().flatMap(Set::stream);

		return stream.filter(k -> k.getStatus() == BinaryStatusType.ACTIVE.unique())
			.filter(k -> settingType == null || k.getSettingType() == settingType.unique())
			.filter(k -> countrytype == null || k.getCountryType() == countrytype.unique());
	}

	public CurrencyType getDefaultCurrency(WebSiteType websiteType, CountryType countryType) {
		int min = getAsStream(websiteType, countryType, WebsiteCountrySettingType.CURRENCY)
			.min(Comparator.comparing(WebsiteCountrySetting::getDisplayOrder))
			.map(WebsiteCountrySetting::getDisplayOrder).orElse(0);

		WebsiteCountrySetting setting = getAsStream(websiteType, countryType, WebsiteCountrySettingType.CURRENCY)
			.filter(k -> k.getDisplayOrder() == min)
			.min(Comparator.comparing(WebsiteCountrySetting::getCreateTime))
			.orElse(null);

		return setting != null ?
			CurrencyType.getInstance(Integer.parseInt(setting.getSettingValue())) :
			WebsiteCurrencySettingCache.getInstance().getDefaultCurrency(websiteType);
		//抓不到該國家預設就改抓 WebsiteCurrencySetting 預設, 先把邏輯收在這邊, 如果有其他需求再拉出去各自處理.
	}


	public CurrencyType getMarketingDefaultCurrency(WebSiteType websiteType, CountryType countryType,
		MarketingGroupType marketingGroupType) {

		List<Integer> supportCurrencies = WebsiteCurrencySettingCache.getInstance()
			.getMarketingGroupSupportCurrencyType(websiteType, marketingGroupType)
			.map(CurrencyType::unique)
			.toList();

		WebsiteCountrySetting setting = getAsStream(websiteType, countryType, WebsiteCountrySettingType.CURRENCY)
			.filter(k -> supportCurrencies.contains(Integer.parseInt(k.getSettingValue()))).min(COMPARATOR)
			.orElse(null);

		return setting != null ?
			CurrencyType.getInstance(Integer.parseInt(setting.getSettingValue())) :
			WebsiteCurrencySettingCache.getInstance().getDefaultMarketGroupCurrency(websiteType, marketingGroupType);
	}
}
