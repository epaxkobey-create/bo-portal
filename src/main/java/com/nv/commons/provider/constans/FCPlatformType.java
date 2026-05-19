package com.nv.commons.provider.constans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.provider.dto.ConnectionInfo;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import org.apache.commons.lang3.StringUtils;

/**
 * 因為我們的 vendor 和 gameType 跟 FC 會有差異，故將這些參數都放在 enum，以方便擴充用
 *
 * @author Ken
 */
public enum FCPlatformType implements UniqueValueHolder {

	SLOT(1, VendorSystemCodeConstants.FC_SYSTEM_CODE),
	;

	private final int value;
	private final String vendorCode;

	public static final FCPlatformType[] VALUES = FCPlatformType.values();

	FCPlatformType(int value, String vendorCode) {
		this.value = value;
		this.vendorCode = vendorCode;
	}


	@Override
	public int unique() {
		return value;
	}

	public static FCPlatformType getFCPlatformTypeByVendorCode(String vendorCode) {
		for (FCPlatformType e : VALUES) {
			if (e.vendorCode.equals(vendorCode)) {
				return e;
			}
		}
		return null;
	}


	public Map<String, Object> getGameLoginParameters(AccountProvider accountProvider, Game game,
		PlatformType platformType) {

		String providerAccount = accountProvider.getProviderAccount();

		Map<String, Object> params = new HashMap<>();

		params.put("MemberAccount", providerAccount);
		params.put("GameID", game.getCode());
		params.put("CloseFeatureBuy", Boolean.toString(false));
		params.put("IsLandscapeGame", Boolean.toString(!platformType.isApp()));

		return params;
	}

}
