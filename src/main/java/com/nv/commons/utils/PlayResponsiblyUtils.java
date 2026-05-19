package com.nv.commons.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.nv.commons.bo.AccountPlayResponsiblySettingBO;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.exceptions.Deviation;
import com.nv.module.backendapi.controller.JsonRequest;

/**
 * PlayResponsibly 相關工具類
 */
public final class PlayResponsiblyUtils {
	public static boolean isPlayResponsiblySettingsRequiredWhenRegister(WebSiteType webSiteType, int currencyTypeId) {
		String settingsValue = WebsiteSystemSettingCache.getInstance()
			.getValueByKey(webSiteType.unique(), currencyTypeId,
				WebsiteSystemSettingType.IS_REGISTER_WITH_PLAY_RESPONSIBLY_SETTINGS.unique());
		if (settingsValue == null) {
			return false;
		}
		try {
			return Integer.parseInt(settingsValue) == BinaryStatusType.ACTIVE.unique();
		} catch (NumberFormatException e) {
			LogUtils.SYS.error("Invalid IS_REGISTER_WITH_PLAY_RESPONSIBLY_SETTINGS value: {}", settingsValue, e);
			return false;
		}
	}

	public static List<AccountPlayResponsiblySetting> validatePlayResponsiblySettingsWhenRegister(
		JsonRequest registerReq, WebSiteType webSiteType, int currencyTypeId, AccountPlayResponsiblyType type,
		String settingsName) throws Exception {

		List<AccountPlayResponsiblySetting> settings = registerReq.getObjectList(settingsName,
			AccountPlayResponsiblySetting.class, null);

		boolean isRegisterWithPRSettings = isPlayResponsiblySettingsRequiredWhenRegister(webSiteType, currencyTypeId);

		if (isRegisterWithPRSettings) {
			Optional.ofNullable(settings).ifPresentOrElse(
				list ->
					AccountPlayResponsiblySettingBO.validateRequest(type, list),
				() -> {
					throw new Deviation().setI18N("fs.parameter.validation", settingsName);
				}
			);
		} else {
			settings = Collections.emptyList(); // set as empty since not required
		}

		return settings;
	}

	public static boolean enableNetPositions(WebSiteType webSiteType, int currencyTypeId) {
		String settingsValue = WebsiteSystemSettingCache.getInstance()
			.getValueByKey(webSiteType.unique(), currencyTypeId,
				WebsiteSystemSettingType.ENABLE_NET_POSITION.unique());
		if (settingsValue == null) {
			return false;
		}
		try {
			return Integer.parseInt(settingsValue) == BinaryStatusType.ACTIVE.unique();
		} catch (NumberFormatException e) {
			LogUtils.SYS.error("Invalid ENABLE_NET_POSITION value: {}", settingsValue, e);
			return false;
		}
	}
}
