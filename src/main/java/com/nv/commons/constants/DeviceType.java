/**
 *
 */
package com.nv.commons.constants;

import com.nv.commons.dto.BrowserUserAgent;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Luke.Chi
 * MEMO: 目前需求只有區分 PC 跟不是 PC, 不是 PC 通通都算 MOBILE
 * ref: net.sf.uadetector.ReadableDeviceCategory.Category
 * UserAgent detector library 有更細的分類
 */
public enum DeviceType implements UniqueValueHolder {

	PERSONAL_COMPUTER(0) {
		@Override
		public PlatformType getPlatformType() {
			return PlatformType.WEB;
		}
	},
	MOBILE(1) {
		@Override
		public PlatformType getPlatformType() {
			return PlatformType.HTML5;
		}
	};

	private final int value;

	DeviceType(int value) {
		this.value = value;
	}

	public static DeviceType getInstanceOf(int value) {
		for (DeviceType e : DeviceType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public static DeviceType getInstance(HttpServletRequest request) {

		final BrowserUserAgent browserUserAgent = UserAgentUtils.getBrowserUserAgent(request);

		if (browserUserAgent == null) {
			return PERSONAL_COMPUTER;
		}
		return browserUserAgent.isMobileOrTablet() ? MOBILE : PERSONAL_COMPUTER;
	}

	@Override
	public int unique() {
		return value;
	}

	public abstract PlatformType getPlatformType();

	public boolean isMobile() {
		return this == MOBILE;
	}
}
