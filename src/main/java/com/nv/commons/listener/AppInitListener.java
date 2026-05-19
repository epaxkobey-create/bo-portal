package com.nv.commons.listener;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.BankCache;
import com.nv.commons.cache.GameCache;
import com.nv.commons.cache.ManagerCache;
import com.nv.commons.cache.OTPRecordCache;
import com.nv.commons.cache.PaymentDisplaySettingCache;
import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.cache.ProviderAgentCache;
import com.nv.commons.cache.ProviderAgentMappingCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.RoleResourceCache;
import com.nv.commons.cache.SystemSettingCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.cache.WebsiteBankCache;
import com.nv.commons.cache.WebsiteCountrySettingCache;
import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.cache.WebsiteSystemSettingCache;
import com.nv.commons.constants.CacheType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.EnvironmentType;
import com.nv.commons.constants.ServerNodeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteSystemSettingType;
import com.nv.commons.scheduler.ScheduleType;
import com.nv.commons.system.Setting;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.CountryLookup;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.commons.utils.secret.SecurityBase;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.backendapi.cache.TokenRecordDBCache;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import com.nv.websocket.WebSocketClient;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.lang3.StringUtils;

/**
 * Application Lifecycle Listener implementation class AppListener
 *
 * @author Neutec
 */
@WebListener
public class AppInitListener implements ServletContextListener {

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {

		// 檢查目前server的時區是否正確
		checkTimeZone();

		// 加密相關
		SecurityBase.enableAESProvider();

		ServletContext context = event.getServletContext();

		final String serverId = this.getServerId(context);

		final int initServerType = this.getServerType(context);

		final EnvironmentType environmentType = this.getEnvironmentType(context);

		final int sessionTimeout = context.getSessionTimeout();

		LogUtils.SYS.debug("ServerId:{}, ServerType:{}, SessionTimout:{}",
			serverId, initServerType, sessionTimeout);

		// MEMO: need to init before initCacheServer
		SystemInfo.getInstance().init(serverId, initServerType, sessionTimeout, environmentType);

		// force init ip2location, CHT vpn default remote ip
		CountryLookup.getInstance().getCountry("61.218.22.2");

		// 如果是正式環境，檢查是否為資料庫內的IP
		//		checkServerIpAddress();

		String walletApiUrl = context.getInitParameter("walletApiUrl");
		if (walletApiUrl != null) {
			SeamlessWalletApiService.getInstance().setWalletApiUrl(walletApiUrl);
		}

		/* init */
		initializeCache();

		String webSocketApiUrl = WebsiteSystemSettingCache.getInstance().getValueByKey(WebSiteType.RSG.unique(),
			CurrencyType.EUR.unique(), WebsiteSystemSettingType.BO_AFFILIATE_DOMAIN.unique());
		if (!StringUtils.isEmpty(webSocketApiUrl)) {
			WebSocketClient.getInstance().setWebSocketApiUrl("https://" + webSocketApiUrl);
		}

		/* init all schedule types */
		for (ScheduleType scheduleType : ScheduleType.values()) {
			scheduleType.execute();
		}

		//		for (GameTxnScheduleType type : GameTxnScheduleType.values()) {
		//			type.execute();
		//		}

		// initialize JS File Version. Reset when restarting server.
		Setting.JS_FILE_VERSION = Integer.parseInt(DateUtils.toString(new java.util.Date(), "yyyyMMddHH"));
		Setting.FILE_VERSION = DateTimeBuilder
			.localDateTime("2020/11/03 23:59:59", FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss).withMaxTime()
			.toTimeMilli();

		try (InputStream inputStream = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			Attributes attributes = new Manifest(inputStream).getMainAttributes();
			if (attributes.getValue("Git-Revision") != null) {
				Setting.SVN_REVISION = attributes.getValue("Git-Revision");
			}
		} catch (Exception ex) {
			LogUtils.SYS.error(ex.getMessage(), ex);
		}

	}

	/**
	 *
	 */
	private void initializeCache() {

		SystemSettingCache.getInstance().eager();
		WebsiteInfoCache.getInstance().eager();
		WebsiteCurrencySettingCache.getInstance().eager();
		WebsiteCountrySettingCache.getInstance().eager();
		WebsiteSystemSettingCache.getInstance().eager();

		/*
		 * MEMO: must init after SystemInfo.init() and after WebsiteInfoCache.init()
		 */
		//		initDbMonitorCache();

		/*=========Product =========*/
		ProviderCache.getInstance().eager();
		ProviderAgentCache.getInstance().eager();
		ProviderAgentMappingCache.getInstance().eager();
		ProviderProxyCache.getInstance().eager();

		VendorCache.getInstance().eager();

		GameCache.getInstance().eager();

		AccountProviderCache.getInstance().eager();

		/*========= Payment =========*/
		PaymentDisplaySettingCache.getInstance().eager();
		BankCache.getInstance().eager();
		WebsiteBankCache.getInstance().eager();
		PaymentGatewayCache.getInstance().eager();
		//		BonusTemplateUtil.getCacheInstance().eager();

		/*========= Engagement: Email, SMS, etd. =========*/

		Arrays.stream(new CacheType[] {
				CacheType.ENGAGE_SERVICE_PROVIDER_CACHE,
				CacheType.WEBSITE_ENGAGE_PROVIDER_CACHE,
				CacheType.ENGAGE_SERVICE_ACCOUNT_CACHE,
				CacheType.ENGAGE_SERVICE_ACCOUNT_DEFAULT_CACHE,
			})
			.forEach(cacheType -> {

				final List<ServerNodeType> list = new ArrayList<>();

				for (ServerNodeType serverNodeType : ServerNodeType.values()) {

					final int unique = serverNodeType.unique();

					if ((unique & cacheType.getBelongedServerType()) == unique) {
						list.add(serverNodeType);
					}
				}

				cacheType.getCache().eager(list.toArray(new ServerNodeType[0]));
			});

		OTPRecordCache.getInstance().lazy(ServerNodeType.PLAYER, ServerNodeType.BACKEND_API);

		RoleResourceCache.getInstance().eager(ServerNodeType.MANAGER);

		TokenRecordDBCache.getInstance().eager(ServerNodeType.BACKEND_API);

		AccountPlayResponsiblySettingCache.getInstance()
			.eager(ServerNodeType.PLAYER, ServerNodeType.MANAGER, ServerNodeType.API, ServerNodeType.BACKEND_API);

		/*
		 */
		if (ServerInfoUtils.isPlayerServer()) {
			PlayerLocalCache.getInstance().init();
		}

		if (ServerInfoUtils.isManagerServer()) {
			ManagerCache.getInstance().init();
		}
	}

	private void checkTimeZone() {
		ZonedDateTime systemZoneDateTime = ZonedDateTime.now(ZoneId.systemDefault());
		ZoneId localZone = ZoneId.of("Asia/Taipei");
		//		ZoneId localZone = ZoneId.of("America/Toronto");
		ZonedDateTime localZoneDateTime = ZonedDateTime.now(localZone);

		// Define the custom date time formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");

		// Format the ZonedDateTime objects using the formatter
		String formattedSystemZoneDateTime = systemZoneDateTime.format(formatter);
		String formattedLocalZoneDateTime = localZoneDateTime.format(formatter);

		// Compare the formatted strings
		if (!formattedLocalZoneDateTime.equals(formattedSystemZoneDateTime)) {
			throw new RuntimeException(
				"Server Time Zone Error !!  system:" + formattedSystemZoneDateTime + ", local: "
				+ formattedLocalZoneDateTime);
		}
	}

	/*
	 * web01, web02, api01, mg01 serverId 定義在context.xml
	 * <Context><Parameter name="serverId" value="web01"/></Context>
	 * */
	private String getServerId(ServletContext context) {
		if (context.getInitParameter("serverId") == null) {

			String localIpAddress = HostAddressUtils.getLocalIPAddress();

			if (localIpAddress != null) {
				// only work for ipv4, not for ipv6
				String[] data = localIpAddress.split("\\.");
				return data[data.length - 1];
			}

			return "1";
		}

		return context.getInitParameter("serverId");
	}

	/**
	 * serverType 定義在context.xml
	 * <Context><Parameter name="serverType" value="1"/></Context>
	 */
	private int getServerType(ServletContext context) {
		String serverTypeStr = context.getInitParameter("serverType");
		if (serverTypeStr == null) {
			// 如果為null，則預設是all
			return ServerNodeType.PLAYER.unique() | ServerNodeType.BACKEND_API.unique()
				   | ServerNodeType.MANAGER.unique() | ServerNodeType.API.unique();
		}

		return Integer.parseInt(serverTypeStr);
	}

	/**
	 * environmentType 定義在context.xml
	 * <Context><Parameter name="env" value="dev"/></Context>
	 */
	private EnvironmentType getEnvironmentType(ServletContext context) {

		String env = context.getInitParameter("env");

		//		boolean isProductionServer = HostAddressUtils.isProductionIP(HostAddressUtils.getLocalIPAddress());

		// 防呆, Production 環境還是需要保護一下
		//		if (isProductionServer && env == null) {
		//			return EnvironmentType.PRODUCT;
		//		}

		if (env == null) {
			return EnvironmentType.DEV;
		}

		EnvironmentType environmentType = EnvironmentType.getInstanceOf(env);
		LogUtils.SYS.debug(  "env:{}, EnvironmentType:{}", env, environmentType);
		return environmentType == null ? EnvironmentType.DEV : environmentType;
	}
}
