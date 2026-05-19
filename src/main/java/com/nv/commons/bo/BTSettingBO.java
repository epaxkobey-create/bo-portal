package com.nv.commons.bo;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.WebsiteCountrySettingCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CountryType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;

public class BTSettingBO {

	public static String getSiteSetting(WebSiteType webSiteType, CurrencyType currencyType, CountryType countryTypeByIp) {

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {

			final int enableEmbedGame = webSiteType.getWebsiteInfo().getEnableEmbedGame();
			// iframe or new table
			jGenerator.writeBooleanField("isEnableEmbedGame", enableEmbedGame == BinaryStatusType.ACTIVE.unique());

			jGenerator.writeBooleanField("isEnableForgotPasswordUnlockAccount",
				webSiteType.getWebsiteInfo().getForgotPwdUnlockAccount() == BinaryStatusType.ACTIVE.unique());

			CurrencyType accessCurrency;
			CountryType accessCountryType;

			// TODO: Since the Affiliate information has been removed, can no get the Marketing Group from the domain.
			MarketingGroupType marketingGroupType = MarketingGroupType.RSG;

			if (countryTypeByIp == null) {

				accessCurrency = WebsiteCurrencySettingCache.getInstance()
					.getDefaultMarketGroupCurrency(webSiteType, marketingGroupType);

				accessCountryType = WebsiteCurrencySettingCache.getInstance()
					.getDefaultCountryType(webSiteType, accessCurrency);

			} else {
				accessCurrency = WebsiteCountrySettingCache.getInstance()
					.getMarketingDefaultCurrency(webSiteType, countryTypeByIp, marketingGroupType);
				accessCountryType = countryTypeByIp;
			}

			jGenerator.writeNumberField("accessCurrencyTypeId", accessCurrency.unique());
			jGenerator.writeNumberField("accessCountryTypeId", accessCountryType.unique());
		};
		return JSONUtils.getJSONString(processor);
	}
}
