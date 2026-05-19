package com.nv.commons.cache;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteCurrencySettingType;
import com.nv.commons.dao.WebsiteCurrencySettingDAO;
import com.nv.commons.dto.WebsiteCurrencySetting;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebsiteCurrencySettingCache extends AbstractCache {

	private static final WebsiteCurrencySettingCache instance = new WebsiteCurrencySettingCache();

	private ConcurrentHashMap<WebSiteType, Set<WebsiteCurrencySetting>> cache = new ConcurrentHashMap<>();


	public static WebsiteCurrencySettingCache getInstance() {
		return instance;
	}

	private static final Comparator<WebsiteCurrencySetting> COMPARATOR = Comparator
		.comparing(WebsiteCurrencySetting::getDisplayOrder)
		.thenComparing(WebsiteCurrencySetting::getCreateTime);

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {
			List<WebsiteCurrencySetting> websiteCurrencySettingList = WebsiteCurrencySettingDAO.getAll(conn);

			ConcurrentHashMap<WebSiteType, Set<WebsiteCurrencySetting>> temp = new ConcurrentHashMap<>();
			for (WebsiteCurrencySetting websiteCurrencySetting : websiteCurrencySettingList) {
				WebSiteType webSiteType = WebSiteType.getInstance(websiteCurrencySetting.getWebsiteType());
				if (webSiteType == null) {
					continue;
				}
				Set<WebsiteCurrencySetting> sets = temp.computeIfAbsent(webSiteType, k -> new CopyOnWriteArraySet<>());
				sets.add(websiteCurrencySetting);
			}
			cache = temp;
			checkDefaultSetting();
		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch WebsiteCurrencySetting", ex);
		}
	}

	@Override
	public void update() {
		init();

		LogUtils.SYS.info("update Website Currency Setting Cache");

	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh WebsiteCurrencySetting cache.");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public String getWebsiteCurrencySettingAllBT(WebSiteType websiteType, CountryType countryTypeByIp) throws IOException {

		List<CurrencyType> currencyTypeList = new ArrayList<>(getCurrencyTypes(websiteType));
		Set<WebsiteCurrencySetting> websiteCSSet = cache.get(websiteType);

		JsonGenerator jGenerator = null;
		StringWriter writer = new StringWriter();

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(writer);
			jGenerator.writeStartObject();
			jGenerator.writeArrayFieldStart("currencyMapping");

			for (CurrencyType currencyType : currencyTypeList) {
				genFrontEndBTJson(jGenerator, websiteCSSet, currencyType);
			}

			jGenerator.writeEndArray();

			CurrencyType accessCurrency;
			CountryType accessCountryType;

			// TODO: Since the Affiliate information has been removed, can no get the Marketing Group from the domain.
			MarketingGroupType marketingGroupType = MarketingGroupType.RSG;

			if (countryTypeByIp == null) {

				accessCurrency = getDefaultMarketGroupCurrency(websiteType, marketingGroupType);

				accessCountryType = getDefaultCountryType(websiteType, accessCurrency);

			} else {
				accessCurrency = WebsiteCountrySettingCache.getInstance()
					.getMarketingDefaultCurrency(websiteType, countryTypeByIp, marketingGroupType);
				accessCountryType = countryTypeByIp;
			}

			jGenerator.writeNumberField("accessCurrencyTypeId", accessCurrency.unique());
			jGenerator.writeNumberField("accessCountryTypeId", accessCountryType.unique());

			jGenerator.writeEndObject();
		} finally {
			JSONUtils.close(jGenerator);
		}
		return writer.toString();
	}

	private void genFrontEndBTJson(JsonGenerator jGenerator, Set<WebsiteCurrencySetting> data,
		CurrencyType currencyType)
		throws IOException {
		jGenerator.writeStartObject();
		jGenerator.writeNumberField("currencyTypeId", currencyType.unique());
		jGenerator.writeBooleanField("isMaintain", false);
		//Type
		jGenerator.writeArrayFieldStart("mapping");

		Set<Integer> settingTypeSet = data
			.stream()
			.filter(k -> k.getCurrencyTypeId() == currencyType.unique())
			.map(WebsiteCurrencySetting::getSettingType)
			.collect(Collectors.toSet());

		for (Integer settingType : settingTypeSet) {
			jGenerator.writeStartObject();
			jGenerator.writeNumberField("currencySettingTypeId", settingType);

			jGenerator.writeFieldName("mappingData");
			String mappingData = JSONUtils.toJsonString(data.stream()
				.filter(k -> k.getSettingType() == settingType)
				.filter(k -> k.getCurrencyTypeId() == currencyType.unique())
				.filter(k -> k.getStatus() == BinaryStatusType.ACTIVE.unique())
				.sorted(COMPARATOR)
				.map(WebsiteCurrencySetting::getSettingValue)
				.collect(Collectors.toList()));

			jGenerator.writeRawValue(mappingData);

			jGenerator.writeEndObject();
		}
		jGenerator.writeEndArray();
		jGenerator.writeEndObject();
	}

	private void checkDefaultSetting() {
		try {
			for (WebsiteInfo temp : WebsiteInfoCache.getInstance().getAll()) {
				if (!WebSiteType.checkWebsiteType(temp.getId())) {
					continue;
				}
				WebSiteType webSiteType = WebSiteType.getInstance(temp.getId());
				getDefaultCountryType(webSiteType);
				getDefaultLanguage(webSiteType);
				getDefaultTimeZone(webSiteType);
			}
		} catch (Exception ex) {
			LogUtils.SYS.error(ex.getMessage(), ex);
		}
	}

	private Stream<WebsiteCurrencySetting> getAsStream(WebSiteType websiteType, CurrencyType currencyType,
		WebsiteCurrencySettingType settingType) {
		Stream<WebsiteCurrencySetting> stream =
			websiteType != null && cache.get(websiteType) != null ?
				cache.get(websiteType).stream() :
				cache.values().stream().flatMap(Set::stream);

		return stream.filter(k -> k.getStatus() == BinaryStatusType.ACTIVE.unique())
			.filter(k -> settingType == null || k.getSettingType() == settingType.unique())
			.filter(k -> currencyType == null || k.getCurrencyTypeId() == currencyType.unique());
	}

	/*
		WebsiteType Default Setting,
		Default Setting一定要存在, 否則就要跳錯人工檢查資料設定哪邊錯誤.
	 */
	public CurrencyType getDefaultCurrency(WebSiteType websiteType) {
		int min = getAsStream(websiteType, null, WebsiteCurrencySettingType.CURRENCY)
			.min(Comparator.comparing(WebsiteCurrencySetting::getDisplayOrder))
			.map(WebsiteCurrencySetting::getDisplayOrder).orElse(0);
		return CurrencyType.getInstance(
			getAsStream(websiteType, null, WebsiteCurrencySettingType.CURRENCY)
				.filter(k -> k.getDisplayOrder() == min)
				.min(Comparator.comparing(WebsiteCurrencySetting::getCreateTime))
				.orElseThrow(
					() -> new RuntimeException("default currency not found, websiteType : " + websiteType.name()))
				.getCurrencyTypeId());
	}

	public CountryType getDefaultCountryType(WebSiteType websiteType) {
		return getDefaultCountryType(websiteType, getDefaultCurrency(websiteType));
	}

	public CountryType getDefaultCountryType(WebSiteType websiteType, CurrencyType currencyType) {
		int min = getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.COUNTRY)
			.min(Comparator.comparing(WebsiteCurrencySetting::getDisplayOrder))
			.map(WebsiteCurrencySetting::getDisplayOrder).orElse(0);
		return CountryType.getInstance(Integer.parseInt(
			getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.COUNTRY)
				.filter(k -> k.getDisplayOrder() == min)
				.min(Comparator.comparing(WebsiteCurrencySetting::getCreateTime))
				.orElseThrow(() -> new RuntimeException(
					"default countryType not found, websiteType : " + websiteType.name() + ",currencyType : "
						+ currencyType.name()))
				.getSettingValue()));
	}

	public LanguageType getDefaultLanguage(WebSiteType websiteType) {
		return getDefaultLanguage(websiteType, getDefaultCurrency(websiteType));
	}

	public LanguageType getDefaultLanguage(WebSiteType websiteType, CurrencyType currencyType) {
		int min = getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.LANGUAGE)
			.min(Comparator.comparing(WebsiteCurrencySetting::getDisplayOrder))
			.map(WebsiteCurrencySetting::getDisplayOrder).orElse(0);

		return LanguageType.getInstance(Integer.parseInt(
			getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.LANGUAGE)
				.filter(k -> k.getDisplayOrder() == min)
				.min(Comparator.comparing(WebsiteCurrencySetting::getCreateTime))
				.orElseThrow(() -> new RuntimeException(
					"default language not found, websiteType : " + websiteType.name() + ",currencyType : "
						+ currencyType
						.name()))
				.getSettingValue()));
	}

	public TimeZone getDefaultTimeZone(WebSiteType websiteType) {
		return getDefaultTimeZone(websiteType, getDefaultCurrency(websiteType));
	}

	public TimeZone getDefaultTimeZone(WebSiteType websiteType, CurrencyType currencyType) {
		int min = getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.TIMEZONE)
			.min(Comparator.comparing(WebsiteCurrencySetting::getDisplayOrder))
			.map(WebsiteCurrencySetting::getDisplayOrder).orElse(0);

		return TimeZone.getTimeZone(
			getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.TIMEZONE)
				.filter(k -> k.getDisplayOrder() == min)
				.min(Comparator.comparing(WebsiteCurrencySetting::getCreateTime))
				.orElseThrow(() -> new RuntimeException(
					"default timezone not found, websiteType : " + websiteType.name() + ",currencyType : "
						+ currencyType
						.name()))
				.getSettingValue());
	}

	/*
		WebsiteType Support Setting
	 */
	public Set<CurrencyType> getCurrencyTypes(WebSiteType websiteType) {
		LinkedHashSet<CurrencyType> set = new LinkedHashSet<>();

		getAsStream(websiteType, null, WebsiteCurrencySettingType.CURRENCY)
			.sorted(COMPARATOR)
			.map(k -> CurrencyType.getInstance(Integer.parseInt(k.getSettingValue())))
			.forEach(set::add);
		return Collections.unmodifiableSet(set);
	}


	public Set<String> getCallingCodeByWebsiteCurrencySetting(WebSiteType websiteType,
		Set<CurrencyType> currencyTypeSet) {
		Stream<WebsiteCurrencySetting> websiteCurrencySettingStream = getAsStream(websiteType, null,
			WebsiteCurrencySettingType.COUNTRY);
		return websiteCurrencySettingStream.filter(
				k -> currencyTypeSet == null || currencyTypeSet.contains(CurrencyType.getInstance(k.getCurrencyTypeId())))
			.map(WebsiteCurrencySetting::getSettingValue)
			.map(k -> CountryType.getInstance(Integer.parseInt(k)).getCallingCode()).collect(Collectors.toSet());
	}

	public Stream<WebsiteCurrencySetting> getWebsiteCurrencySetting(WebSiteType websiteType,
		WebsiteCurrencySettingType settingType) {
		return getAsStream(websiteType, null, settingType);
	}

	public Set<CountryType> getCountryTypes(WebSiteType websiteType, CurrencyType currencyType) {
		LinkedHashSet<CountryType> set = new LinkedHashSet<>();

		getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.COUNTRY)
			.sorted(COMPARATOR)
			.map(k -> CountryType.getInstance(Integer.parseInt(k.getSettingValue())))
			.forEach(set::add);
		return Collections.unmodifiableSet(set);
	}

	public Set<LanguageType> getLanguages(WebSiteType websiteType) {
		return getLanguages(websiteType, null);
	}

	public Set<LanguageType> getLanguages(WebSiteType websiteType, CurrencyType currencyType) {
		LinkedHashSet<LanguageType> set = new LinkedHashSet<>();

		getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.LANGUAGE)
			.sorted(COMPARATOR)
			.map(k -> LanguageType.getInstance(Integer.parseInt(k.getSettingValue())))
			.forEach(set::add);
		return Collections.unmodifiableSet(set);
	}

	/*
		Utils
		以下要不要調整到Utils去 ?
	 */
	public boolean isValidCurrency(WebSiteType websiteType, int currencyTypeId) {
		try {
			CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);
			return isValidCurrency(websiteType, currencyType);
		} catch (Exception e) {
			// currencyTypeId 查不到 CurrencyType 時
			return false;
		}
	}

	public boolean isValidCurrency(WebSiteType websiteType, CurrencyType currencyType) {
		return getCurrencyTypes(websiteType).contains(currencyType);
	}


	public MarketingGroupType getMarketGroup(WebSiteType websiteType, CurrencyType currencyType) {
		return getAsStream(websiteType, currencyType, WebsiteCurrencySettingType.MARKETINGGROUP)
			.findFirst()
			.map(k -> MarketingGroupType.getInstance(k.getSettingValue()))
			.orElse(null);
	}

	public CurrencyType getDefaultMarketGroupCurrency(WebSiteType websiteType, MarketingGroupType marketingGroupType) {

		if (marketingGroupType == null) {
			return getDefaultCurrency(websiteType);
		}

		return getAsStream(websiteType, null, WebsiteCurrencySettingType.CURRENCY)
			.filter(k -> {

				CurrencyType currencyType = CurrencyType.getInstance(Integer.parseInt(k.getSettingValue()));

				MarketingGroupType settingMarketingGroupType = getMarketGroup(websiteType, currencyType);

				return settingMarketingGroupType != null && settingMarketingGroupType == marketingGroupType;
			}).min(COMPARATOR)
			.map(k -> CurrencyType.getInstance(k.getCurrencyTypeId()))
			.orElseThrow(() -> new RuntimeException(
				"marketing default currency not found, websiteType : " + websiteType.name()));
	}

	public Stream<CurrencyType> getMarketingGroupSupportCurrencyType(WebSiteType websiteType,
		MarketingGroupType marketingGroupType) {
		return getAsStream(websiteType, null, WebsiteCurrencySettingType.MARKETINGGROUP)
			.filter(k -> k.getSettingValue().equals(marketingGroupType.getMarketingName()))
			.map(k -> CurrencyType.getInstance(k.getCurrencyTypeId()));
	}

}