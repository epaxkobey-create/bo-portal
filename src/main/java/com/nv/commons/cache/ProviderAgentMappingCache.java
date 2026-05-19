package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ProviderAgentMappingDAO;
import com.nv.commons.dto.ProviderAgentMapping;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

public class ProviderAgentMappingCache extends AbstractCache {

	private static final ProviderAgentMappingCache instance = new ProviderAgentMappingCache();

	private Map<Integer, ProviderAgentMapping> cache = new ConcurrentHashMap<>();

	private ProviderAgentMappingCache(){}

	public static ProviderAgentMappingCache getInstance(){
		return instance;
	}

	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {
			ProviderAgentMappingDAO.getAll(conn).forEach(p -> cache.put(p.getId(), p));
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public Stream<ProviderAgentMapping> getAsStream(WebSiteType webSiteType){
		return cache.values()
			.stream()
			.filter(mapping -> mapping.getWebsiteType() == webSiteType.unique());
	}




	@Override
	public void update() {
		try (Connection conn = DBPool.getReadConnection()) {
			cache = ProviderAgentMappingDAO.getAll(conn)
				.stream()
				.collect(Collectors.toConcurrentMap(ProviderAgentMapping::getId, Function.identity()));

		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		ProviderProxyCache.getInstance().clearConnectionInfo();
	}

	@Override
	public void refresh() {
		this.update();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache);
	}

}
