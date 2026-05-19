package com.nv.commons.constants;

import com.nv.commons.model.UniqueValueHolder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 伺服器歸屬的服務類別
 *
 * @author SYSTEM
 */
public enum ServerNodeType implements UniqueValueHolder {
	MAINTAIN(0),
	PLAYER(1),
	MANAGER(2),
	API(4),
	BACKEND_API(32),
	;

	private static int ALL_SERVER_TYPE = -1;

	private final int value;

	ServerNodeType(int value) {
		this.value = value;
	}

	@Override
	public int unique() {
		return value;
	}

	public static int getAllServerType() {
		if (ALL_SERVER_TYPE == -1) {
			int result = 0;
			for (ServerNodeType serverNodeType : ServerNodeType.values()) {
				if (serverNodeType == ServerNodeType.MAINTAIN) {
					continue;
				}
				result = result | serverNodeType.unique();
			}
			ALL_SERVER_TYPE = result;
		}
		return ALL_SERVER_TYPE;
	}

	/**
	 *
	 */
	private static final Map<Integer, String> allServerTypeNamesCache = new HashMap<>();

	public static String getAllServerTypeNames(int serverType) {

		if (serverType < 0) {
			return "";
		}
		if (allServerTypeNamesCache.containsKey(serverType)) {
			return allServerTypeNamesCache.get(serverType);
		}
		String allServerTypeNames = "";

		for (ServerNodeType nodeType : ServerNodeType.values()) {

			if (nodeType.unique() == serverType) {
				allServerTypeNames = nodeType.name();
				break;
			}

			if ((nodeType.unique() & serverType) > 0) {

				if (StringUtils.isEmpty(allServerTypeNames)) {
					allServerTypeNames = nodeType.name();
				} else {
					allServerTypeNames += ("," + nodeType.name());
				}
			}
		}

		if (StringUtils.isNotEmpty(allServerTypeNames)) {
			allServerTypeNamesCache.put(serverType, allServerTypeNames);
		}
		return allServerTypeNames;
	}
}
