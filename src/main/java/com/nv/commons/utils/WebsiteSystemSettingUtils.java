package com.nv.commons.utils;

import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.WebsiteSystemSettingType;

public class WebsiteSystemSettingUtils {
	public static String getRegisterMessageSuccess(int websiteType, int currencyType) {
		return WebsiteSystemSettingCache.getInstance()
			.getValueByKey(websiteType, currencyType,
				WebsiteSystemSettingType.REGISTER_SUCCESS_MESSAGE.unique()).replace("&lt;br&gt;", "<br>");
	}

}
