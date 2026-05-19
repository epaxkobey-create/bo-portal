package com.nv.commons.constants;

import com.nv.commons.cache.AbstractCache;
import com.nv.commons.cache.AccountCache;
import com.nv.commons.cache.AccountCardCache;
import com.nv.commons.cache.AccountContactInfoCache;
import com.nv.commons.cache.AccountContactInfoVerificationCache;
import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.BankCache;
import com.nv.commons.cache.GameCache;
import com.nv.commons.cache.MailTemplateCache;
import com.nv.commons.cache.MoneyTransactionCache;
import com.nv.commons.cache.PaymentDisplaySettingCache;
import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.cache.ProviderAgentCache;
import com.nv.commons.cache.ProviderAgentMappingCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.RoleResourceCache;
import com.nv.commons.cache.SystemSettingCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.cache.WebsiteBankCache;
import com.nv.commons.cache.WebsiteCountrySettingCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.module.engagement.cache.EngageServiceAccountCache;
import com.nv.module.engagement.cache.EngageServiceAccountDefaultCache;
import com.nv.module.engagement.cache.EngageServiceProviderCache;
import com.nv.module.engagement.cache.WebsiteEngageProviderCache;

public enum CacheType {
	PAYMENT_GATEWAY_CACHE(3) {
		public PaymentGatewayCache getCache() {
			return PaymentGatewayCache.getInstance();
		}

		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},

	ACCOUNT_PROVIDER_CACHE(4) {
		public AccountProviderCache getCache() {
			return AccountProviderCache.getInstance();
		}

		public int getBelongedServerType() {
			return ServerNodeType.MANAGER.unique();
		}
	},

	GAME_CACHE(7) {
		public GameCache getCache() {
			return GameCache.getInstance();
		}

		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},

	PROVIDER_CACHE(8) {
		public ProviderCache getCache() {
			return ProviderCache.getInstance();
		}

		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	VENDOR_CACHE(9) {
		public VendorCache getCache() {
			return VendorCache.getInstance();
		}

		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	SYSTEM_SETTING_CACHE(10) {
		public SystemSettingCache getCache() {
			return SystemSettingCache.getInstance();
		}

		public int getBelongedServerType() {
			return ServerNodeType.getAllServerType();
		}
	},



	WEBSITE_SYSTEM_SETTING_CACHE(17) {
		@Override
		public AbstractCache getCache() {
			return WebsiteSystemSettingCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}

	},

	WEBSITE_INFO_CACHE(20) {
		@Override
		public AbstractCache getCache() {
			return WebsiteInfoCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return (ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique()
					| ServerNodeType.MANAGER.unique() | ServerNodeType.API.unique()
			);
		}
	},

	BANK_CACHE(22) {
		@Override
		public AbstractCache getCache() {
			return BankCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},

	PROVIDER_AGENT_CACHE(23) {
		@Override
		public ProviderAgentCache getCache() {
			return ProviderAgentCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},

	PROVIDER_AGENT_MAPPING_CACHE(24) {
		@Override
		public AbstractCache getCache() {
			return ProviderAgentMappingCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},
	PAYMENT_DISPLAY_SETTING_CACHE(26) {
		public PaymentDisplaySettingCache getCache() {
			return PaymentDisplaySettingCache.getInstance();
		}

		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiServerNodeTypes;
		}
	},

	WEBSITEBANK_CACHE(36) {
		@Override
		public WebsiteBankCache getCache() {
			return WebsiteBankCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerServerNodeTypes;
		}

		@Override
		public void update() {
			getCache().update();
		}
	},

	WEBSITE_CURRENCY_SETTING(49) {
		public WebsiteCurrencySettingCache getCache() {
			return WebsiteCurrencySettingCache.getInstance();
		}

		public int getBelongedServerType() {
			return (ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique()
					| ServerNodeType.MANAGER.unique() | ServerNodeType.API.unique()
			);
		}

		@Override
		public void update() {
			getCache().update();
		}
	},
	WEBSITE_COUNTRY_SETTING(50) {
		public WebsiteCountrySettingCache getCache() {
			return WebsiteCountrySettingCache.getInstance();
		}

		public int getBelongedServerType() {
			return (ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique()
					| ServerNodeType.MANAGER.unique() | ServerNodeType.API.unique()
			);
		}

		@Override
		public void update() {
			getCache().update();
		}
	},
	ENGAGE_SERVICE_PROVIDER_CACHE(51) {
		@Override
		public EngageServiceProviderCache getCache() {
			return EngageServiceProviderCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return (bitOrPlayerBackendManagerServerNodeTypes);
		}
	},
	WEBSITE_ENGAGE_PROVIDER_CACHE(52) {
		@Override
		public WebsiteEngageProviderCache getCache() {
			return WebsiteEngageProviderCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return (bitOrPlayerBackendManagerServerNodeTypes);
		}
	},
	ENGAGE_SERVICE_ACCOUNT_CACHE(53) {
		@Override
		public EngageServiceAccountCache getCache() {
			return EngageServiceAccountCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return (bitOrPlayerBackendManagerServerNodeTypes);
		}
	},
	ENGAGE_SERVICE_ACCOUNT_DEFAULT_CACHE(54) {
		@Override
		public EngageServiceAccountDefaultCache getCache() {
			return EngageServiceAccountDefaultCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return (bitOrPlayerBackendManagerServerNodeTypes);
		}
	},

	MAIL_TEMPLATE_CACHE(65) {
		@Override
		public MailTemplateCache getCache() {
			return MailTemplateCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerServerNodeTypes
				;
		}

		@Override
		public void update() {
			getCache().update();
		}
	},

	ROLE_RESOURCE_CACHE(68) {
		@Override
		public AbstractCache getCache() {
			return RoleResourceCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return ServerNodeType.MANAGER.unique();
		}
	},

	ACCOUNT_PLAY_RESPONSIBLY(69) {
		@Override
		public AbstractCache getCache() {
			return AccountPlayResponsiblySettingCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	ACCOUNT_CACHE(70) {
		@Override
		public AbstractCache getCache() {
			return AccountCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	ACCOUNT_CONTACT_INFO_CACHE(71) {
		@Override
		public AbstractCache getCache() {
			return AccountContactInfoCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	ACCOUNT_CONTACT_INFO_VERIFICATION_CACHE(72) {
		@Override
		public AbstractCache getCache() {
			return AccountContactInfoVerificationCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	ACCOUNT_CARD_CACHE(73) {
		@Override
		public AbstractCache getCache() {
			return AccountCardCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	},

	MONEY_TRANSACTION_CACHE(74) {
		@Override
		public AbstractCache getCache() {
			return MoneyTransactionCache.getInstance();
		}

		@Override
		public int getBelongedServerType() {
			return bitOrPlayerBackendManagerApiSettleServerNodeTypes;
		}
	};

	private static final CacheType[] VALUES = CacheType.values();

	private static final int bitOrPlayerBackendManagerServerNodeTypes =
		(ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique() | ServerNodeType.MANAGER.unique());

	private static final int bitOrPlayerBackendManagerApiServerNodeTypes =
		(ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique() | ServerNodeType.MANAGER.unique()
		 | ServerNodeType.API.unique());

	private static final int bitOrPlayerBackendManagerApiSettleServerNodeTypes =
		(ServerNodeType.PLAYER.unique()
		 | ServerNodeType.BACKEND_API.unique() | ServerNodeType.MANAGER.unique() | ServerNodeType.API.unique()
		);

	public static CacheType getInstanceOf(int value) {
		for (CacheType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	private final int value;

	CacheType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract AbstractCache getCache();

	public abstract int getBelongedServerType();

	// MEMO: default call getCache().update(), 有特殊邏輯才需要 Override
	public void update() {
		getCache().update();
	}
}
