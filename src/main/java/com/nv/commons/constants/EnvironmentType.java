package com.nv.commons.constants;

import com.nv.commons.utils.ResourceUtils;

public enum EnvironmentType {

	EXTERNAL("external") {
		@Override
		public String getPropertiesFolder() {
			return "/properties/external";
		}
	},
	DEV("dev") {
		@Override
		public String getPropertiesFolder() {
			return "/properties";
		}

		@Override
		public String getIPDatabasePath() {
			return ResourceUtils.findClassloader().getResource("./lib/IP-COUNTRY-REGION-CITY-ISP.BIN").getPath();
		}

		@Override
		public String getIPLicensePath() {
			return ResourceUtils.findClassloader().getResource("./lib/license.key").getPath();
		}
	},
	UAT("uat") ,
	PROD("prod") ,
	;

	private final String code;

	EnvironmentType(String code) {
		this.code = code;
	}

	public static EnvironmentType getInstanceOf(String code) {
		for (EnvironmentType e : EnvironmentType.values()) {
			if (e.code.equals(code)) {
				return e;
			}
		}
		return null;
	}

	public String getPropertiesFolder(){
		return System.getProperty("catalina.base") + "/properties";
	};

	public String getIPDatabasePath() {
		return System.getProperty("catalina.base") + "/lib/IPV6-COUNTRY-REGION-CITY-ISP.BIN";
	}

	public String getIPLicensePath() {
		return System.getProperty("catalina.base") + "/lib/license.key";
	}
}
