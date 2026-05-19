package com.nv.commons.cache;

import com.nv.commons.annotation.HttpUpdate;
import com.nv.commons.dao.SystemSettingDAO;
import com.nv.commons.dto.SystemSetting;
import com.nv.commons.exceptions.AccessDeniedException;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.system.Setting;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.RequestParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Neutec
 */
public class SystemSettingCache extends AbstractCache {


	private Map<String, SystemSetting> cache = new ConcurrentHashMap<>();

	private static final SystemSettingCache instance = new SystemSettingCache();

	private SystemSettingCache() {
	}

	public static SystemSettingCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {

			Map<String, SystemSetting> tempCache = new ConcurrentHashMap<>();

			List<SystemSetting> settingList = SystemSettingDAO.findAll(conn);

			for (SystemSetting setting : settingList) {
				tempCache.put(setting.getKey(), setting);
				updateSetting(setting);
			}

			this.cache = tempCache;

		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch SystemSetting list", ex);
		}
	}

	public SystemSetting getByKey(String key) {

		SystemSetting setting = cache.get(key);

		if (setting == null) {
			LogUtils.SYS.info("key:{}, not in cache", key);

			try (Connection conn = DBPool.getReadConnection()) {
				setting = SystemSettingDAO.findByKey(conn, key, null);

			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}

		return setting;
	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public void update() {
		init();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	/**
	 * 覆寫掉Setting.class裡面的數值
	 *
	 */
	public boolean updateSetting(SystemSetting setting) {
		try {
			String key = setting.getKey().toLowerCase();
			if (key.startsWith("setting.")) {
				String keyName = key.substring("setting.".length());
				String value = setting.getValue();
				return SystemSettingCache.getInstance().updateSettingByField(keyName, value)
					|| SystemSettingCache.getInstance().updateSettingByMethod(keyName, value);
			}
		} catch (Exception e) {
			LogUtils.SYS.info(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 比對欄位名稱做修改，物件內使用
	 *
	 */
	private boolean updateSettingByField(String key, String value)
		throws Exception {
		Class<Setting> clazz = Setting.class;
		Field field = clazz.getField(key);

		// 判斷參數是否有宣告 "@HttpUpdate"，有宣告才能修改
		if (!field.isAnnotationPresent(HttpUpdate.class)) {
			throw new AccessDeniedException();
		}

		String type = field.getType().toString();

		switch (type) {
			case "class java.lang.String":
				if (value == null || value.isEmpty()) {
					throw new NoSuchFieldException();
				}
				field.set(clazz, value);
				break;

			case "boolean":
				field.setBoolean(clazz, RequestParser.getBooleanParameter(value));
				break;

			case "int":
				field.setInt(clazz, RequestParser.getIntParameter(value));
				break;

			default:
				return false;
		}

		return true;

	}


	/**
	 * 比對函數名稱做修改，物件內使用
	 */
	private boolean updateSettingByMethod(String key, String value)
		throws Exception {
		Class<Setting> clazz = Setting.class;

		//check method
		Method[] methods = clazz.getMethods();

		if (value == null || value.isEmpty()) {
			return false;
		}

		for (Method temp : methods) {

			String methodName = temp.getName();

			if (methodName.startsWith("set") && methodName.equals(key)) {

				// 判斷方法是否有宣告 "@HttpUpdate"，有宣告才能修改
				if (!temp.isAnnotationPresent(HttpUpdate.class)) {
					throw new AccessDeniedException();
				}

				Class<?>[] parameterTypes = temp.getParameterTypes();

				if (parameterTypes.length == 0) {
					continue;
				}

				String type = parameterTypes[0].getName();

				switch (type) {
					case "java.lang.String":
						temp.invoke(clazz.getDeclaredConstructor().newInstance(), value);
						break;

					case "boolean":
						temp.invoke(clazz.getDeclaredConstructor().newInstance(), RequestParser.getBooleanParameter(value));
						break;

					case "int":
						temp.invoke(clazz.getDeclaredConstructor().newInstance(), RequestParser.getIntParameter(value));
						break;

					default:
						return false;
				}

				return true;
			}
		}
		return false;
	}

}
