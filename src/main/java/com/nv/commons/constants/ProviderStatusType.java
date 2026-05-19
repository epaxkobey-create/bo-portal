package com.nv.commons.constants;

import java.util.HashMap;
import java.util.Map;

import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.JSONUtils;

public enum ProviderStatusType {

	/*
	 * UI show
	 * Play Game -> maintainVendor.jsp
	 * 不能轉帳
	 */
	INACTIVE(-1) {
		@Override
		public String getName() {
			return "Inactive";
		}

	},
	MAINTENANCE(0) {
		@Override
		public String getName() {
			return "Maintenance";
		}

	},
	ACTIVE(1) {
		@Override
		public String getName() {
			return "Active";
		}

	};

	public static final ProviderStatusType[] VALUES = ProviderStatusType.values();

	public static final ProviderStatusType[] VALUES_FOR_BO = {ProviderStatusType.ACTIVE, ProviderStatusType.INACTIVE,
		ProviderStatusType.MAINTENANCE};

	static {
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (ProviderStatusType providerStatusType : VALUES) {
			map.put(providerStatusType.unique(), providerStatusType.getName());
		}
		json = JSONUtils.toJsonString(map);
	}

	private static final String json;

	public static String toJsonString() {
		return json;
	}

	private final int value;

	ProviderStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getName();

}
