package com.nv.commons.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nv.commons.constants.SystemConstants;
import jakarta.servlet.http.HttpServletRequest;

public class HostAddressUtils {

	private static Set<String> uatIPs = CollectionUtils.newHashSet(
		"10.110.54.60",
		"10.110.54.61",
		"10.110.54.62",
		"10.31.2.21",
		"10.31.2.31",
		"10.31.2.41");

	private static Map<String, Set<String>> addressWhiteList = new HashMap<>();

	private HostAddressUtils() {
		throw new AssertionError();
	}

	public static String getRealIPAddresses(HttpServletRequest httpRequest) {
		//incapsula, cdNetwork, akami
		String[] headers = new String[] {"x-forwarded-for", "x-forwarded-ip", "true-client-ip"};
		for (String key : headers) {
			String value = httpRequest.getHeader(key);
			if (value == null) {
				continue;
			}
			int clientIpIndex = value.indexOf(",");
			if (clientIpIndex != -1) {
				value = value.substring(0, clientIpIndex);
			}

			if (value.length() > SystemConstants.IP_VALID_MAX_LENGTH) {
				value = value.substring(0, SystemConstants.IP_VALID_MAX_LENGTH);
			}

			// 預防injection
			if (!Validator.isValidatedIP(value)) {
				continue;
			}

			return value;
		}
		return httpRequest.getRemoteAddr();
	}

	public static boolean isUat(String ip) {
		return uatIPs.contains(ip);
	}

	public static boolean isInternalIP(String ip) {
		boolean isInternalIP = ip.startsWith("10.100.")
			|| ip.startsWith("10.1.")
			|| ip.startsWith("10.10.")
			|| ip.startsWith("10.166.");

		return isInternalIP;
	}

	/**
	 * 取得目前server的IP
	 *
	 * @return 只有 正式環境 或是 office 才回傳真的 ip, 否則都只回傳 127.0.0.1
	 */
	public static String getLocalIPAddress() {
		try {
			Enumeration<NetworkInterface> localNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
			Enumeration<InetAddress> allIPAddresses;

			while (localNetworkInterfaces.hasMoreElements()) {

				allIPAddresses = localNetworkInterfaces.nextElement().getInetAddresses();

				while (allIPAddresses.hasMoreElements()) {

					String ip = allIPAddresses.nextElement().getHostAddress();

					LogUtils.SYS.info("localIPAddress:{}", ip);

					if (isUat(ip) || isInternalIP(ip)) {
						return ip;
					}

				}
			}
		} catch (SocketException sockexc) {
			LogUtils.SYS.info("Socket Exception, Cannot Determine IP Addresses");
			sockexc.printStackTrace();
		}
		//for local test
		return "127.0.0.1";
	}

	/**
	 * 取得ip的最後一個數字
	 *
	 * @param ip IP
	 * @return ip的最後一個數字
	 */
	public static int getLastIPNumber(String ip) {
		String[] data = ip.split("\\.");
		return Integer.parseInt(data[3]);
	}


	public static boolean verifyRemoteAddress(String ipAddressString) {
		String[] var2 = splitIpAddress(ipAddressString);
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			String ipAddress = var2[var4];
			if (whiteListContains(ipAddress)) {
				return true;
			}
		}

		return false;
	}

	static String[] splitIpAddress(String ipAddressString) {
		return ipAddressString == null ?
			new String[0] :
			Arrays.stream(ipAddressString.split(",")).map(String::trim).toArray((x$0) -> new String[x$0]);
	}

	public static boolean whiteListContains(String ipAddress) {
		return addressWhiteList.isEmpty() || addressWhiteList.get("jumio").stream()
			.anyMatch(address -> address.equalsIgnoreCase(ipAddress));
	}



}
