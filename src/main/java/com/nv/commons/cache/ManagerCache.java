package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.ManagerDAO;
import com.nv.commons.dto.Manager;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class ManagerCache {

	private static final ManagerCache instance = new ManagerCache();

	public static ManagerCache getInstance() {
		return instance;
	}

	private long lastUpdateTime = 0;
	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private final Map<WebSiteType, ConcurrentHashMap<String, Manager>> userStore = new EnumMap<>(WebSiteType.class);

	private ManagerCache() {
		super();
	}

	public void init() {

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			ManagerDAO.clearSessionIdAndServerId(conn);
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error("error while init manager cache", e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public Manager add(String account, Manager loginManager) {
		//同一帳號用不同session login，用同一個參考會無法判斷誰是舊的，要以login的為主
		loginManager.setModifyTimeMillis(System.currentTimeMillis());

		Map<String, Manager> managerInMap = userStore.computeIfAbsent(loginManager.getWebsiteTypeObj(),
			k -> new ConcurrentHashMap<>());

		managerInMap.put(account, loginManager);

		return loginManager;
	}

	public Manager getManager(WebSiteType webSiteType, String managerID) {

		Manager manager = null;

		Map<String, Manager> managerInMap = userStore.get(webSiteType);

		if (managerInMap != null) {
			manager = managerInMap.get(managerID);
		}

		return manager;

	}

	public String[] getAffiliate(WebSiteType webSiteType, String managerID) {
		String[] affiliates = null;

		Manager userInCache = getManager(webSiteType, managerID);

		if (userInCache.getAffiliate() != null) {
			affiliates = userInCache.getAffiliate().split(",");
			Arrays.sort(affiliates);
		}

		return affiliates;
	}

	public void remove(WebSiteType webSiteType, String managerID) {
		userStore.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>()).remove(managerID);
	}

	public void update() {
		long currentTime = System.currentTimeMillis();

		Timestamp queryTimestamp = new Timestamp(lastUpdateTime);

		try {
			List<Manager> onlineManager;
			try (Connection conn = DBPool.getReadConnection()) {

				onlineManager = ManagerDAO.findAllManagerByServerID(conn, SystemInfo.getInstance().getServerID());
			}

			for (Manager manager : onlineManager) {

				WebSiteType webSiteType = WebSiteType.getInstance(manager.getWebsiteType());

				Map<String, Manager> managerInMap = userStore.computeIfAbsent(webSiteType,
					k -> new ConcurrentHashMap<>());

				Manager userInCache = managerInMap.get(manager.getUserId());

				if (null == userInCache) {
					Manager newUserInCache = managerInMap.putIfAbsent(manager.getUserId(), manager);
					if (null == newUserInCache) {
						userInCache = manager;
					} else {
						userInCache = newUserInCache;
					}
				}

				if (userInCache.getUpdateTime().getTime() != manager.getUpdateTime().getTime()) {
					userInCache.setUserName(manager.getUserName());
					userInCache.setPassword(manager.getPassword());
					if (userInCache.getRoleID() != manager.getRoleID()) {
						userInCache.setUpdateAccessRight(true);
					}
					userInCache.setRoleID(manager.getRoleID());
					userInCache.setLoginTime(manager.getLoginTime());
					userInCache.setServerID(manager.getServerID());
					userInCache.setSessionID(manager.getSessionID());
					userInCache.setUpdater(manager.getUpdater());
					userInCache.setStatus(manager.getStatus());
					userInCache.setEnablePopup(manager.getEnablePopup());
					userInCache.setAffiliate(manager.getAffiliate());
					userInCache.setUpdateTime(manager.getUpdateTime());
					userInCache.setVirtual(manager.isVirtual());
				}
				userInCache.setModifyTimeMillis(currentTime);
			}

			//移除cache中過期的資料(可能從別台server登入, 或被close了)
			for (Map<String, Manager> managerInMap : userStore.values()) {
				for (Manager manager : managerInMap.values()) {
					if (SystemConstants.BO_SUPER_ADMIN.equals(manager.getUserId())) {
						continue;
					}
					// 表示剛剛沒有被更新到
					if (manager.getModifyTimeMillis() < currentTime) {
						this.remove(WebSiteType.getInstance(manager.getWebsiteType()), manager.getUserId());
					}
				}
			}

			lastUpdateTime = queryTimestamp.getTime() - ERROR_VALUE;

			long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());
			if (duration > 1) {
				LogUtils.backOfficeMonitor.debug("ManagerCache update Use {} secs ", duration);
			}

		} catch (Exception e) {
			LogUtils.SYS.error("error while fetch manager list", e);
		}
	}

	public void updateAccessRight(int roleId, WebSiteType webSiteType) {
		Map<String, Manager> managerInMap = userStore.get(webSiteType);

		if (managerInMap == null) {
			return;
		}

		for (Manager manager : managerInMap.values()) {
			if (manager.getRoleID() == roleId) {
				manager.setUpdateAccessRight(true);
			}
		}
	}

}
