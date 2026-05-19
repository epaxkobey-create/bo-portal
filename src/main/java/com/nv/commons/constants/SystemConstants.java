package com.nv.commons.constants;

import com.nv.commons.utils.EncryptUtil;

public class SystemConstants {

	public static final String SERVER_SITE_NAME = "rsg";

	//包含保留映射IPv6地址的IPv4地址 = 45個字符, ex. ABCD:ABCD:ABCD:ABCD:ABCD:ABCD:192.168.158.190
	public static final int IP_VALID_MAX_LENGTH = 45;

	public static final String RUNTIME_ENV = "runtime_env";

	public static final int PAGE_SIZE = 10;

	public static final String RUN_JUNIT = "run.junit";

	// 自動登出轉址位置: 統一由autoLogout.jsp判斷如何處理
	public static final String AUTO_LOGOUT_PAGE = "/autoLogout.jsp";

	public static final int MAX_LIMIT_MONTH = 6;

	// MEMO: Native App first request header has App-name,
	// Bruce 建議作法：當收到第一個request時 check App-name，如果有，就存入cookie以便後續辨識
	public static final String APP_NAME_COOKIE_NAME = "appNameCookie";

	public static final String REQUEST_HEADER_AUTHORIZATION = "Authorization";

	public static final String INTERNAL_EXCEPTION = "msg.error.internalException";

	// In House Api Url (不只 Payment 在用)
	public static final String IN_HOUSE_API_URL = "http://localhost";
	public static final String TEST_IN_HOUSE_API_URL = "https://apiuat.mcd105proxies.net";

	public static final String BO_SUPER_ADMIN = "superadmin";
	public static final String BO_SUPER_ADMIN_PASSWORD = "b2e98ad6f6eb8508dd6a14cfa704bad7f05f6fb1";

	public static final String PG_PROXY_FOLDER_PATH = "com.nv.commons.paymentGateway.proxy.";

	public static final int AGE_19 = 19;

	public static final String GAME_PROXY_FOLDER_PATH = "com.nv.commons.provider.proxy.";

	public final static int ACCESS_TOKEN_TIMEOUT_HOUR_PROD = 6;
	public final static int REFRESH_TOKEN_TIMEOUT_HOUR_PROD = 10;

	public static final int LOGIN_FAILURE_LIMIT = 5;

	public static final String PG_SYNC_BASE_URL = "/pg/";

	// 後台登入頁
	public static final String MANAGER_LOGIN_PAGE = "/page/manager/login.jsp";

	public static final String MANAGER_ROOT = "root";

	public static final String CURRENCY = "currency";

	public static final String ACCESS_DENIED = "msg.login.loginStatusType.9";

	public static final String PASSWORD_PROTECTION_KEY = EncryptUtil
		.encryptMD5ToBase64(String.valueOf(System.nanoTime()));

	//for game of backendApi Server
	public static final int DEFAULT_GAMES_PER_PAGE_OF_API_SERVER = 24;
	public static final int GAMES_MAX_PER_PAGE_OF_API_SERVER = 72;

	public static final int GAME_PAGE_SIZE = 25;

	//for other data of backendApi Server
	public static final int DATA_DEFAULT_PER_PAGE_OF_API_SERVER = 10;
	public static final int DATA_MAX_PER_PAGE_OF_API_SERVER = 20;

	public static final int NO_LIMIT_SETTING = 0;
	public static final int MIN_LIMIT_SETTING = 0;
	public static final int MAX_LIMIT_SETTING = 1_000_000;

	//default brand name for mail content, it should be setting in websitesystemsetting
	public static final String DEFAULT_BRAND_NAME = "TS";

	public static final int GAME_SESSION_USAGE_MULTIPLIER = 3600;
	public static final int GAME_SESSION_USAGE_RESET_MINUTES = 24 * 60;
}

