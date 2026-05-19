package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;

public enum WebsiteSystemSettingType implements UniqueValueHolder {
	WEBSITE_STATUS(2),// Inactive(-1), Maintenance(0), Active(1)
	BO_DOMAIN(10),
	REGISTER_SUCCESS_MESSAGE(11),
	BO_AFFILIATE_DOMAIN(12),
	FORGET_PASSWORD_SEND_TIME_LIMIT(40),
	CLOSE_AUTO_VERIFY_WITHDRAWAL(46),
	BT_USER_VERIFY_LINK_EXPIRY_IN_SECONDS(81),
	SESSION_EXPIRY_MODIFY_TAKE_EFFECT_MINUTES(82),
	SELF_EXCLUSION_MODIFY_TAKE_EFFECT_MINUTES_FROM_DEFINITE(83),
	SELF_EXCLUSION_MODIFY_TAKE_EFFECT_MINUTES_FROM_INDEFINITE(84),
	WAGER_LIMITS_MODIFY_TAKE_EFFECT_MINUTES(85),
	LOSS_LIMITS_MODIFY_TAKE_EFFECT_MINUTES(86),
	DEPOSIT_LIMITS_MODIFY_TAKE_EFFECT_MINUTES(87),
	REALITY_CHECK_MODIFY_TAKE_EFFECT_MINUTES(88),
	WEB_SOCKET_MAX_SESSION_NUMBER(90),
	IS_REGISTER_WITH_PLAY_RESPONSIBLY_SETTINGS(91),
	IS_NEED_PROMPT_REALITY_CHECK(92),
	TIME_SPENT_LIMIT_MODIFY_TAKE_EFFECT_MINUTES(94),
	ENABLE_NET_POSITION(95),
	BRAND_NAME(96),
	;

	private final int key;
	public static final WebsiteSystemSettingType[] VALUES = WebsiteSystemSettingType.values();

	WebsiteSystemSettingType(int key) {
		this.key = key;
	}

	public static WebsiteSystemSettingType getInstance(int key) {
		for (WebsiteSystemSettingType e : VALUES) {
			if (e.unique() == key) {
				return e;
			}
		}
		return null;
	}

	@Override
	public int unique() {
		return key;
	}
}
