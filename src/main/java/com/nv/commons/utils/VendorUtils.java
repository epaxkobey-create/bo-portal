package com.nv.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.VendorStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.dto.WebsiteVendor;

public class VendorUtils {

	private VendorUtils() {
		throw new AssertionError();
	}

	/*
	 *
	 */
	public static boolean isVendorInvisible(WebsiteVendor webSiteVendor, Vendor vendor) {
		return webSiteVendor.getStatus() == VendorStatusType.INVISIBLE.unique()
			|| vendor.getStatus() == VendorStatusType.INVISIBLE.unique();
	}

	public static boolean isVendorMaintain(WebsiteVendor webSiteVendor, Vendor vendor) {
		return webSiteVendor.getStatus() == VendorStatusType.MAINTENANCE.unique()
			|| vendor.getStatus() == VendorStatusType.MAINTENANCE.unique();
	}


	/*
	 * only used by BO
	 */
	public static boolean isVendorAvailableIgnoreCurrency(WebSiteType webSiteType, WebsiteVendor webSiteVendor) {

		return isVendorAvailable(webSiteType, webSiteVendor, null, true);
	}

	public static boolean isVendorAvailable(WebSiteType webSiteType, WebsiteVendor webSiteVendor,
		CurrencyType currencyType) {

		return isVendorAvailable(webSiteType, webSiteVendor, currencyType, false);
	}

//	public static boolean hasTrialPlay(Vendor vendor, GameType gameType) {
//		TrialPlayType trialPlayType = TrialPlayType.getInstance(gameType.unique());
//		if (trialPlayType == TrialPlayType.NONE) {
//			return false;
//		}
//		return gameType.in(vendor.getGameType()) && trialPlayType.in(vendor.getTrialPlayType());
//	}

	private static boolean isVendorAvailable(WebSiteType webSiteType, WebsiteVendor webSiteVendor,
		CurrencyType currencyType, boolean ignoreCurrency) {

		if (!ignoreCurrency && currencyType == null) {
			throw new RuntimeException("Missing currencyType");
		}

		Vendor vendor = (webSiteVendor == null)
			? null
			: VendorCache.getInstance().getVendor(webSiteVendor.getVendorId());

		int providerId = (vendor == null)
			? 0
			: vendor.getProviderId();

		WebsiteProvider webSiteProvider = ProviderCache.getInstance().getWebsiteProvider(webSiteType, providerId);

		Provider provider = ProviderCache.getInstance().getProvider(providerId);

		return webSiteVendor != null
			&& vendor != null
			&& webSiteProvider != null
			&& provider != null
			&& !isVendorInvisible(webSiteVendor, vendor)
			&& !ProviderUtils.isProviderInactive(webSiteProvider)
			&& (ignoreCurrency || webSiteType.isSupported(provider, currencyType));
	}

	public static List<WebsiteVendor> getVendorOfWebsiteType(WebSiteType webSiteType,
		Collection<WebsiteProvider> websiteProviders, CurrencyType currencyType) {

		List<WebsiteVendor> vendorOfWebsiteTypeList = new ArrayList<>();

		for (WebsiteProvider webSiteProvider : websiteProviders) {

			List<WebsiteVendor> websiteVendors = VendorCache.getInstance()
				.getByProviderId(webSiteType, webSiteProvider.getProviderId());

			for (WebsiteVendor websiteVendor : websiteVendors) {
				if (isVendorAvailable(webSiteType, websiteVendor, currencyType)) {
					vendorOfWebsiteTypeList.add(websiteVendor);
				}
			}
		}

		return vendorOfWebsiteTypeList;
	}


}
