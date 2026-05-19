package com.nv.commons.constants;

public enum MarketingGroupType {
	RSG("rsg"),
	;

	private final String marketingName;

	MarketingGroupType(String marketingName) {
		this.marketingName = marketingName;
	}

	public static MarketingGroupType getInstance(String marketingName) {
		for (MarketingGroupType e : MarketingGroupType.values()) {
			if (e.getMarketingName().equalsIgnoreCase(marketingName)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No enum const MarketingGroup. name:" + marketingName);
	}


	public String getMarketingName() {
		return this.marketingName;
	}
}
