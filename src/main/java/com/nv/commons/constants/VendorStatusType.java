package com.nv.commons.constants;

import com.nv.commons.message.LangMessage;

public enum VendorStatusType {

	/*
	 * UI 不 show, 目前 for Casino, Fishing and Sport
	 * vendors = VendorCache.getInstance().getWebsiteVendors(GameType.SLOT);
	 * fishings  = VendorCache.getInstance().getWebsiteVendors(GameType.FH);
	 */
	INVISIBLE(-1) {
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.invisible");
		}
	},

	/*
	 * allow show VendorStatusType.INACTIVE
	 * Play Game -> maintainVendor.jsp
	 */
	MAINTENANCE(0) {
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.maintenance");
		}
	},
	ACTIVE(1) {
		@Override
		public String getDisplayName(LangMessage langMessage) {
			if (langMessage == null) {
				langMessage = LanguageType.ENGLISH.getLangMessage();
			}
			return langMessage.get("form.text.backOffice.status.active");
		}
	};

	private final int value;

	VendorStatusType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getDisplayName(LangMessage langMessage);

}
