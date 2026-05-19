package com.nv.commons.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.nv.commons.bo.AccountProviderBO;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.WebsiteProvider;

/**
 * @author Neutec
 */
public class ProviderUtils {

	private ProviderUtils() {
		throw new AssertionError();
	}

	/*
	 *
	 */
	public static boolean isProviderInactive(WebsiteProvider webSiteProvider) {

		if (webSiteProvider == null) {
			LogUtils.SYS.error("Can not find provider");
			return true;
		}

		Provider provider = ProviderCache.getInstance().getProvider(webSiteProvider.getProviderId());

		return webSiteProvider.getStatus() == ProviderStatusType.INACTIVE.unique()
			|| provider.getStatus() == ProviderStatusType.INACTIVE.unique();
	}

	public static boolean isProviderMaintain(WebsiteProvider webSiteProvider) {

		Provider provider = ProviderCache.getInstance().getProvider(webSiteProvider.getProviderId());

		return webSiteProvider.getStatus() == ProviderStatusType.MAINTENANCE.unique()
			|| provider.getStatus() == ProviderStatusType.MAINTENANCE.unique();
	}


	/*
	 * MAINTENANCE or ACTIVE
	 */
	public static boolean isProviderNonInactive(WebsiteProvider webSiteProvider) {

		return !isProviderInactive(webSiteProvider);

	}



	/**
	 *
	 */
	public static BigDecimal getProviderBalance(boolean forceCallApi, AccountProvider accountProvider) {

		BigDecimal providerBalance = BigDecimal.ZERO;

		if (accountProvider != null) {

			final WebSiteType webSiteType = WebSiteType.getInstance(accountProvider.getWebsiteType());

			final int providerId = accountProvider.getProviderId();

			final WebsiteProvider webSiteProvider = ProviderCache.getInstance().getWebsiteProvider(
				webSiteType, providerId);

			// rate limit: 60 seconds
			if (forceCallApi && !ProviderUtils.isProviderMaintain(webSiteProvider)
				&& DateUtils.secondsElapsedSince(accountProvider.getLastCallGetBalanceTimeMillis()) > 60) {

				accountProvider.setLastCallGetBalanceTimeMillis(System.currentTimeMillis());

				// direct call API get balance
				providerBalance = AccountProviderBO.callGetBalanceAPI(accountProvider);

			} else {
				// balance from cache
				providerBalance = accountProvider.getProviderBalance();
			}
			// MEMO: 不要自動進位
			providerBalance = BigDecimalUtils.round(providerBalance.doubleValue(), 2, RoundingMode.DOWN);
		}

		return providerBalance;
	}

}
