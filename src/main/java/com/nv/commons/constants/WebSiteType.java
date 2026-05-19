package com.nv.commons.constants;

import com.nv.commons.cache.ProviderAgentCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.dto.WebsiteSystemSetting;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.CountryLookup;
import com.nv.commons.utils.HostAddressUtils;

import java.util.ArrayList;
import java.util.List;

public enum WebSiteType {

	RSG(1) {
		@Override
		public String getShortName() {
			return "rsg";
		}

		@Override
		public String getTempName() {
			return "rsg";
		}
	},
	;

	public static WebSiteType getInstance(int value) {
		for (WebSiteType e : WebSiteType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public static boolean checkWebsiteType(int value) {
		for (WebSiteType e : WebSiteType.values()) {
			if (e.value == value) {
				return true;
			}
		}
		return false;
	}

	public abstract String getShortName();

	public abstract String getTempName();

	public String getSiteFolder() {
		return getShortName();
	}

	private final int value;

	private String boDomain;
	private String boBackupDomain;

	/*
	 *
	 */
	WebSiteType(int value) {
		this.value = value;
	}

	public int unique() {
		return this.value;
	}

	public CurrencyType getDefaultCurrencyType() {
		return WebsiteCurrencySettingCache.getInstance().getDefaultCurrency(this);
	}

	public List<LanguageType> getLanguageTypes() {
		return new ArrayList<>(WebsiteCurrencySettingCache.getInstance().getLanguages(this));
	}

	public String getBoDomain() {

		if (this.boDomain == null) {

			WebsiteSystemSetting websiteSystemSetting = WebsiteSystemSettingCache.getInstance()
				.getSettingByKey(this.value, WebsiteSystemSettingType.BO_DOMAIN.unique()).stream().findFirst()
				.orElse(null);

			if (websiteSystemSetting != null) {
				this.boDomain = websiteSystemSetting.getValue();
			} else {
				this.boDomain = "";
			}
		}
		return boDomain;
	}

	public void reloadBackUpDomain() {
		this.boBackupDomain = null;
	}

	public WebsiteInfo getWebsiteInfo() {
		return WebsiteInfoCache.getInstance().getByWebType(this.unique());
	}

	public boolean isSupported(Provider provider, CurrencyType currencyType) {
		WebsiteInfo websiteInfo = getWebsiteInfo();
		if (websiteInfo != null) {
			for (CurrencyType type : websiteInfo.getCurrencyTypes()) {

				if (type.unique() == currencyType.unique() && ProviderAgentCache.getInstance()
					.has(this, provider, currencyType)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEnableBetType() {
		return false;
	}
}
