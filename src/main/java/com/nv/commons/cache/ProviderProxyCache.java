package com.nv.commons.cache;

import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ProviderAgentDAO;
import com.nv.commons.dao.ProviderAgentMappingDAO;
import com.nv.commons.dao.ProviderDAO;
import com.nv.commons.dao.WebsiteProviderDAO;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.ProviderAgent;
import com.nv.commons.dto.ProviderAgentMapping;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.provider.dto.ConnectionInfo;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderProxyCache extends AbstractCache {

	private static final ProviderProxyCache instance = new ProviderProxyCache();

	private final Map<Integer, ProviderProxy> providerProxyMap = new ConcurrentHashMap<>();

	/*Map<websiteType,Map<providerId,Map<currencyId,ConnectionInfo>>>*/
	private final Map<Integer, Map<Integer, Map<Integer, ConnectionInfo>>> connectionInfoMap = new ConcurrentHashMap<>();

	private ProviderProxyCache() {
	}

	public static ProviderProxyCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {

			initConnectionInfo(conn);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public <E> E getProviderProxy(WebSiteType webSiteType, int providerId, CurrencyType currencyType) throws Exception {
		Provider provider = ProviderCache.getInstance().getProvider(providerId);
		if (provider == null) {
			return null;
		}
		return getProviderProxy(webSiteType, provider, currencyType);
	}

	public <E> E getProviderProxy(WebSiteType webSiteType, Provider provider, CurrencyType currencyType)
		throws Exception {

		ProviderProxy providerProxy = getProviderProxy(provider);

		ConnectionInfo connectionInfo = this.getConnectionInfo(webSiteType, provider, currencyType);

		providerProxy.setConnectionInfo(connectionInfo);

		return (E) providerProxy;
	}



	public ConnectionInfo getConnectionInfo(WebSiteType webSiteType, Provider provider, CurrencyType currencyType) {

		final CurrencyType switchedCurrencyType = CurrencyType.getInstance(currencyType.unique());

		return connectionInfoMap.computeIfAbsent(webSiteType.unique(), ConcurrentHashMap::new)
			.computeIfAbsent(provider.getId(), ConcurrentHashMap::new)
			.computeIfAbsent(switchedCurrencyType.unique(), currency -> {

				final Optional<ProviderAgent> optional = ProviderAgentCache.getInstance()
					.findFirst(webSiteType, provider, switchedCurrencyType);

				if (optional.isEmpty()) {
					return null;
				}
				ProviderAgent providerAgent = optional.get();

				WebsiteProvider websiteProvider = ProviderCache.getInstance()
					.getWebsiteProvider(webSiteType, provider.getId());

				return new ConnectionInfo(webSiteType, provider, switchedCurrencyType,
					websiteProvider.getExtendConnectionInfo(), providerAgent.getAgentInfo());
			});
	}

	void clearConnectionInfo() {
		connectionInfoMap.clear();
	}

	@Override
	public void update() {

	}

	@Override
	public void refresh() {

	}

	@Override
	public String getCacheInfo() {
		return "{}";
	}

	public <E> E getProviderProxy(Provider provider) throws Exception {

		int providerId = provider.getId();
		ProviderProxy providerProxy = providerProxyMap.get(providerId);

		if (providerProxy == null) {
			try {
				String proxyClassPath = SystemConstants.GAME_PROXY_FOLDER_PATH + provider.getClassName();

				providerProxy = (ProviderProxy) Class.forName(proxyClassPath)
					.getConstructor(Integer.TYPE, String.class)
					.newInstance(providerId, provider.getSystemCode());

				providerProxyMap.put(providerId, providerProxy);

			} catch (Throwable e) {
				LogUtils.SYS.error(e.getMessage(), e);
				throw new Exception(e);
			}
		}
		return (E) providerProxy;
	}

	private void initConnectionInfo(Connection conn) throws SQLException {
		Map<Integer, Provider> providerMap = ProviderDAO.getMap(conn);

		Map<Integer, ProviderAgent> providerAgentMap = ProviderAgentDAO.getMap(conn);

		for (WebSiteType webSiteType : WebSiteType.values()) {

			List<ProviderAgentMapping> mappingList = ProviderAgentMappingDAO.get(conn, webSiteType);

			Map<Integer, WebsiteProvider> websiteProviderMap = WebsiteProviderDAO.getMap(conn, webSiteType);

			for (ProviderAgentMapping mapping : mappingList) {

				ProviderAgent providerAgent = providerAgentMap.get(mapping.getProviderAgentId());

				WebsiteProvider websiteProvider = websiteProviderMap.get(providerAgent.getProviderId());
				if (websiteProvider != null) {

					Map<Integer, ConnectionInfo> connInfoSubMap = connectionInfoMap
						.computeIfAbsent(webSiteType.unique(), ConcurrentHashMap::new)
						.computeIfAbsent(websiteProvider.getProviderId(), ConcurrentHashMap::new);

					Provider provider = providerMap.get(websiteProvider.getProviderId());

					for (String currencyId : providerAgent.getCurrencyTypeId().split(",")) {
						CurrencyType currencyType = CurrencyType.getInstance(Integer.parseInt(currencyId));

						ConnectionInfo connectionInfo = new ConnectionInfo(webSiteType, provider, currencyType,
							websiteProvider.getExtendConnectionInfo(), providerAgent.getAgentInfo());

						connInfoSubMap.put(currencyType.unique(), connectionInfo);
					}
				}
			}
		}
	}
}
