package com.nv.commons.utils;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import com.nv.commons.constants.EnvironmentType;
import com.nv.commons.system.SystemInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Neutec
 */
public class CountryLookup {

	private IP2Location ip2l = null;
	private static final CountryLookup instance = new CountryLookup();

	private static final String SESSION_REGISTERED_IP = "s_reg_ip";
	private static final String SESSION_REGISTERED_COUNTRY = "s_reg_country";
	public static final String SESSION_ALLOW_COUNTRY = "allow_country";

	private static final String colon = ":";

	private CountryLookup() {
		try {
			ip2l = new IP2Location();

			EnvironmentType environmentType = SystemInfo.getInstance().getEnvironmentType();
			// 依照不同環境取得資訊
			ip2l.IPDatabasePath = environmentType.getIPDatabasePath();
			ip2l.IPLicensePath = environmentType.getIPLicensePath();

			update(ip2l);
		} catch (Exception ex) {
			LogUtils.SYS.error("IP2Location init fail.", ex);
		}
	}

	/**
	 * for test
	 */
	public void update(IP2Location ip2l) {
		try {
			this.ip2l = ip2l;
			LogUtils.SYS.info("[IP2Locaiton][INIT]loc.IPDatabasePath=" + ip2l.IPDatabasePath);
			LogUtils.SYS.info("[IP2Locaiton][INIT]loc.IPLicensePath=" + ip2l.IPLicensePath);
		} catch (Exception ex) {
			LogUtils.SYS.error("IP2Location init fail.", ex);
			this.ip2l = null;
		}
	}

	public static CountryLookup getInstance() {
		return instance;
	}

	public String getCountry(HttpServletRequest request) {

		String currentIp = HostAddressUtils.getRealIPAddresses(request);
		HttpSession session = request.getSession(false);
		if (session == null) {
			return getCountry(currentIp);
		}

		String sessionCountry = (String) session.getAttribute(SESSION_REGISTERED_COUNTRY);
		String sessionIp = (String) session.getAttribute(SESSION_REGISTERED_IP);

		if (sessionCountry == null || sessionIp == null || !sessionIp.equals(currentIp)) {
			sessionCountry = getCountry(currentIp);
			session.setAttribute(SESSION_REGISTERED_COUNTRY, sessionCountry);
			session.setAttribute(SESSION_REGISTERED_IP, currentIp);
			session.removeAttribute(CountryLookup.SESSION_ALLOW_COUNTRY);
		}

		return sessionCountry;
	}

	public String getCountry(String ip) {

		if (ip == null || ip.contains(colon)) {
			return null;
		}

		try {
			IPResult c = ip2l.IPQuery(ip.trim());
			if (c != null) {
				return c.getCountryShort();
			}
		} catch (Exception ignored) {

		}
		return null;

	}

	public IPResult getIPResult(String ip) {
		if (ip.contains(colon)) {
			return null;
		}
		try {
			return ip2l.IPQuery(ip);
		} catch (StackOverflowError ignored) {
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}
}
