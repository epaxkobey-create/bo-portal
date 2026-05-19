package com.nv.commons.provider.dto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Provider;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

public class ConnectionInfo {

	protected static final String CURRENCY_SEPARATOR = "#@";

	protected static final String LANGUAGE_SEPARATOR = "$@";

	private WebSiteType webSiteType;

	protected CurrencyType currencyType;

	protected Provider provider;

	protected Map<String, String> properties = new HashMap<>();

	protected Map<String, String> propertiesByCurrency = new HashMap<>();

	protected Map<String, String> propertiesByLanguage = new HashMap<>();

	public ConnectionInfo(WebSiteType webSiteType, Provider provider, CurrencyType currencyType,
		String extendConnectionInfo, String agentInfo) {
		this.webSiteType = webSiteType;
		this.provider = provider;
		this.currencyType = currencyType;
		try {
			putProperties(provider.getConnectionInfo());
			putProperties(agentInfo);
			putProperties(extendConnectionInfo);
		} catch (Exception e) {
			StringBuilder errorMessageBuilder = new StringBuilder();
			errorMessageBuilder.append("[").append(HostAddressUtils.getLocalIPAddress()).append("][ConnectionInfo][Error]\r\n");
			errorMessageBuilder.append("Building ConnectionInfo fails.\r\n");
			errorMessageBuilder.append("WebsiteType: ").append(webSiteType.name()).append("\r\n");
			errorMessageBuilder.append("Provider: ").append(provider.getProviderName()).append("\r\n");
			errorMessageBuilder.append("Currency: ").append(currencyType.name()).append("\r\n");
			errorMessageBuilder.append("Message: ").append(e.getMessage()).append("\r\n");

			String errorMessage = errorMessageBuilder.toString();

			LogUtils.SYS.error(errorMessage, e);
		}
	}

	public ConnectionInfo(Provider provider, CurrencyType currencyType) {
		this.provider = provider;
		this.currencyType = currencyType;
	}

	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public CurrencyType getCurrencyType() {
		return currencyType;
	}

	public Provider getProvider() {
		return provider;
	}

	public String get(String key) {
		return this.properties.get(key);
	}

	public String get(int currency, String key) {
		return this.propertiesByCurrency.get(currency + CURRENCY_SEPARATOR + key);
	}

//	public String getLanguageProperties(LanguageType languageType, String key) {
//		if (languageType == null) {
//			return get(key);
//		}
//		String value = this.propertiesByLanguage.get(languageType.unique() + LANGUAGE_SEPARATOR + key);
//		if (value == null) {
//			value = get(key);
//		}
//		return value;
//	}

	protected void putProperties(String jsonStr) throws IOException {

		if (StringUtils.isNotEmpty(jsonStr)) {

			JSONUtils.getObjectMapper().readTree(jsonStr).fields().forEachRemaining(entry -> {

				String key = entry.getKey();
				String value = entry.getValue().asText();

				String propertyKey = getPropertyKey(key);
				String propertyByCurrencyKey = getPropertyByCurrencyKey(key);
				String propertyByLanguageKey = getPropertyByLanguageKey(key);

				if (propertyKey != null) {
					properties.put(propertyKey, value);
				}
				if (propertyByCurrencyKey != null) {
					propertiesByCurrency.put(propertyByCurrencyKey, value);
				}
				if (propertyByLanguageKey != null) {
					propertiesByLanguage.put(propertyByLanguageKey, value);
				}
			});
		}
	}

	protected String getPropertyKey(String key) {
		return getPropertyKey(key, false);
	}

	protected String getPropertyKey(String key, boolean checkProvider) {

		if (!key.contains(CURRENCY_SEPARATOR) && !key.contains(LANGUAGE_SEPARATOR)) {
			return key;
		}
		// 以下是 key.contains(SEPARATOR) == true
		if (key.contains(CURRENCY_SEPARATOR)) {
			String[] keys = key.split(CURRENCY_SEPARATOR);

			if (Integer.parseInt(keys[0]) == currencyType.unique()
				&& (!checkProvider || provider.isSyncByCurrency())) {
				return keys[1];
			}
		}

		return null;
	}

	protected String getPropertyByCurrencyKey(String key) {
		return getPropertyByCurrencyKey(key, false);
	}

	protected String getPropertyByCurrencyKey(String key, boolean checkProvider) {

		if (key.contains(CURRENCY_SEPARATOR)) {
			return key;
		}
		if (!checkProvider || provider.isSyncByCurrency()) {
			return currencyType.unique() + CURRENCY_SEPARATOR + key;
		}
		return null;
	}

	protected String getPropertyByLanguageKey(String key) {

		if (key.contains(LANGUAGE_SEPARATOR)) {
			return key;
		}
		return null;
	}

}
