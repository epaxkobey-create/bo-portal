package com.nv.commons.cache;

import com.nv.commons.dao.RoleResourceDAO;
import com.nv.commons.dto.RoleResource;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// TODO 確認是否還需要這個類別
public class RoleResourceCache extends AbstractCache {

	private final ConcurrentHashMap<Integer, RoleResource> resourceStore = new ConcurrentHashMap<>();

	private List<RoleResource> currentResourceList = Collections.emptyList();

	private static final RoleResourceCache instance = new RoleResourceCache();

	private RoleResourceCache() {
	}

	public static RoleResourceCache getInstance() {
		return instance;
	}


	@Override
	protected void init() {
		try (Connection conn = DBPool.getReadConnection()) {

			List<RoleResource> tempList = RoleResourceDAO.queryAllFunction(conn);
			for (RoleResource resource : tempList) {
				resourceStore.put(resource.getId(), resource);
			}
			currentResourceList = Collections.unmodifiableList(tempList);

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch roleResource list", ex);
		}
	}



	public List<RoleResource> getResourceList() {
		return currentResourceList;
	}

	@Override
	public void update() {

	}

	@Override
	public void refresh() {

	}

	@Override
	public String getCacheInfo() {
		return null;
	}


}
