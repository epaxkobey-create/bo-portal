package com.nv.commons.utils;

import com.nv.commons.cache.RemotingCaller;
import com.nv.commons.constants.CacheType;

public class NotifyUtils {

	/**
	 * for reload other web cache
	 *
	 * @param cacheType
	 * @return
	 */
	private static String getParam(int cacheType) {
		return "isRemoteCall=Y&cacheType=" + cacheType
			+ "&tk=" + RemotingCaller.getInstance().getTkCode() + "&method=reloadCache";
	}

	public static void updateCache(CacheType cacheType) {
		int serverType = cacheType.getBelongedServerType();
		RemotingCaller.getInstance().sendMessage(serverType, getParam(cacheType.unique()));
	}
}
