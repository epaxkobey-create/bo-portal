package com.nv.commons.constants;

import java.util.Arrays;

import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.Validator;

public enum WebsiteStatusType {

	INACTIVE(-1) {

	},

	ACTIVE(1) {

	};

	private final int value;

	WebsiteStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	private static boolean isInactive(WebSiteType websiteType, boolean ifAny) {
		try {
			WebsiteInfo websiteInfo = websiteType.getWebsiteInfo();
			if (websiteInfo == null) {

				return true;
			}

			long inactiveCount = Arrays.stream(websiteInfo.getCurrencyTypes()).filter(currencyType -> {
				String value = WebsiteSystemSettingCache.getInstance()
					.getValueByKey(websiteType.unique(), currencyType.unique(),
						WebsiteSystemSettingType.WEBSITE_STATUS.unique());

				if (Validator.isEmpty(value)) {
					return true;
				}

				int status = Integer.parseInt(value);
				return status == INACTIVE.unique();

			}).count();

			if (ifAny && inactiveCount > 0) {
				return true;
			}

			int allCurrencySize = websiteInfo.getCurrencyTypes().length;

			return !ifAny && inactiveCount == allCurrencySize;

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return true;
	}

	public static boolean isAllInactive(WebSiteType websiteType) {
		return isInactive(websiteType, false);
	}

}
