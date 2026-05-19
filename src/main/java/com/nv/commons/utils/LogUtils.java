package com.nv.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {

	public static final Logger SYS = LogManager.getLogger("SYS");

	public static final Logger ip = LogManager.getLogger("ip");

	public static final Logger sqlPrint = LogManager.getLogger("sqlPrint");

	public static final Logger operator = LogManager.getLogger("operator");

	public static final Logger accountBank = LogManager.getLogger("accountBank");

	public static final Logger depositSetting = LogManager.getLogger("depositSetting");

	public static final Logger paymentGateway = LogManager.getLogger("paymentGateway");

	public static final Logger report = LogManager.getLogger("report");

	public static final Logger okHttpMonitor = LogManager.getLogger("okHttpMonitor");

	public static final Logger providerMonitor = LogManager.getLogger("providerMonitor");

	public static final Logger mailTemplate = LogManager.getLogger("mailTemplate");

	public static final Logger backOfficeMonitor = LogManager.getLogger("backOfficeMonitor");

	public static final Logger accountDocument = LogManager.getLogger("accountDocument");

	public static final Logger register = LogManager.getLogger("register");

	public static final Logger playerCache = LogManager.getLogger("playerCache");

	public static final Logger moneyTransaction = LogManager.getLogger("moneyTransaction");

	public static final Logger balance = LogManager.getLogger("balance");

	public static final Logger accountGroup = LogManager.getLogger("accountGroup");

	public static final Logger affiliate = LogManager.getLogger("affiliate");

	public static final Logger backendApi = LogManager.getLogger("backendApi");

	public static final Logger tokenRecord = LogManager.getLogger("tokenRecord");
	public static final Logger generalMonitor = LogManager.getLogger("generalMonitor");

	public static final Logger accountContactInfo = LogManager.getLogger("accountContactInfo");

	// for Sumsub callback servlet
	public static final Logger sumsub = LogManager.getLogger("sumsub");
	// for we call Sumsub API
	public static final Logger sumsubService = LogManager.getLogger("sumsubService");
	// sumsubCallback 用於記錄 sumsub 的回調信息
	public static final Logger sumsubCallback = LogManager.getLogger("sumsubCallback");

	public static Logger seamlessWalletService = LogManager.getLogger("seamlessWalletService");
	;
	public static final Logger accountPlayResponsibly = LogManager.getLogger("accountPlayResponsibly");

	public static Logger managerBO = LogManager.getLogger("managerBO");
	public static Logger filter = LogManager.getLogger("filter");

	public static Logger providerNST = LogManager.getLogger("providerNST");

	private LogUtils() {
		throw new AssertionError();
	}
}
