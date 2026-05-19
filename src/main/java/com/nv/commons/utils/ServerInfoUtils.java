package com.nv.commons.utils;

import com.nv.commons.constants.ServerNodeType;
import com.nv.commons.system.SystemInfo;

/**
 * @author Luke Chi
 */
public class ServerInfoUtils {

	public static boolean isPlayerServer() {
		int type = SystemInfo.getInstance().getServerType();
		return ((type & ServerNodeType.PLAYER.unique()) > 0);
	}

	public static boolean isBackendApiServer() {
		int type = SystemInfo.getInstance().getServerType();
		return ((type & ServerNodeType.BACKEND_API.unique()) > 0);
	}

	public static boolean isManagerServer() {
		int type = SystemInfo.getInstance().getServerType();
		return ((type & ServerNodeType.MANAGER.unique()) > 0);
	}

//	public static boolean isSettlementServer() {
//		int type = SystemInfo.getInstance().getServerType();
//		return ((type & ServerNodeType.SETTLEMENT.unique()) > 0);
//	}

	public static boolean isAPIServer() {
		int type = SystemInfo.getInstance().getServerType();
		return ((type & ServerNodeType.API.unique()) > 0);
	}

//	public static boolean isOperatorServer() {
//		int type = SystemInfo.getInstance().getServerType();
//		return ((type & ServerNodeType.OPERATOR.unique()) > 0);
//	}

//	public static boolean isAffiliateServer() {
//		int type = SystemInfo.getInstance().getServerType();
//		return ((type & ServerNodeType.AFFILIATE.unique()) > 0);
//	}
}
