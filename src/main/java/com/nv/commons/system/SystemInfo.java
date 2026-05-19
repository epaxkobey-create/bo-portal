package com.nv.commons.system;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.nv.commons.bo.ServerInfoBO;
import com.nv.commons.constants.EnvironmentType;
import com.nv.commons.constants.ServerNodeType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dto.ServerInfoEntity;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.LogUtils;
import jakarta.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.logging.log4j.Logger;

public class SystemInfo {

	private static final SystemInfo instance = new SystemInfo();

	private String serverID = "node1";

	// 1 : player, 2: agnet, 4:manager
	private int serverType = 0;

	private int sessionTimeout;

	// BO 系統維護中
	private boolean systemMaintainOn = false;

	// for dev in case didn't setup external upload
	private boolean hasExternalUpload = false;

//	public boolean isProduction() {
//		return this.environmentType == EnvironmentType.PRODUCT;
//	}

	public boolean isUat() {
		return this.environmentType == EnvironmentType.UAT;
	}

	public boolean isDev() {
		return this.environmentType == EnvironmentType.DEV || this.environmentType == EnvironmentType.EXTERNAL;
	}

	private String serverInfo = "none";

	private String webSiteServerType = SystemConstants.SERVER_SITE_NAME;

	private String uploadFolder = null;

	private String serverInfoDescription = "local";

	private final Logger logger = LogUtils.SYS;

	private EnvironmentType environmentType = EnvironmentType.DEV;

	private SystemInfo() {
		super();

//		isProduction = HostAddressUtils.isProductionIP(localIPAddress);
//		isUat = HostAddressUtils.isUat(localIPAddress);
//		isDev = HostAddressUtils.isOfficeIP(localIPAddress)
//			// for dev jenkins docker container
//			|| "127.0.0.1".equals(localIPAddress);
	}

	public static SystemInfo getInstance() {
		return instance;
	}

	public void setEnvironmentType(EnvironmentType environmentType) {
		this.environmentType = environmentType;
	}

	public void init(String serverID, int serverType, int sessionTimeout, EnvironmentType environmentType) {

		LogUtils.SYS.info("init SystemInfo start");

		long currentTime = System.currentTimeMillis();
		this.serverID = serverID;
		this.serverType = serverType;
		this.sessionTimeout = sessionTimeout;
		this.environmentType = environmentType;

		// server描述訊息
		initServerInfoDescription();

		// Statistics.update(Statistics.RemotingCaller,
		// System.currentTimeMillis() - currentTime);
		long duration = System.currentTimeMillis() - currentTime;
		if (duration > 1000) {
			LogUtils.SYS.error("Use : " + duration);
		}

		serverInfo = this.webSiteServerType + "-"
			+ ServerNodeType.getAllServerTypeNames(this.serverType) + " - "
			+ HostAddressUtils.getLocalIPAddress();

		LogUtils.SYS.info("SystemInfo.serverInfo: " + serverInfo);
		LogUtils.SYS.info("init SystemInfo end");

		initUploadFolder();
	}

	public String getServerID() {
		return this.serverID;
	}

	public int getServerType() {
		return this.serverType;
	}

	public boolean isSystemMaintainOn() {
		return systemMaintainOn;
	}

	public void update(int serverType) {
		this.serverType = serverType;
	}

	public boolean isHasExternalUpload() {
		return hasExternalUpload;
	}


	private void initServerInfoDescription() {

		try {
			List<ServerInfoEntity> serverList = ServerInfoBO.queryAllServerInfo();
			for (ServerInfoEntity serverInfoEntity : serverList) {
				if (String.valueOf(serverInfoEntity.getId()).equalsIgnoreCase(this.serverID)) {
					this.serverInfoDescription = serverInfoEntity.getDescription();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public EnvironmentType getEnvironmentType() {
		return environmentType;
	}

	private void initUploadFolder() {

		String externalUploadFolder = getExternalUploadFolder();

		if (externalUploadFolder != null) {
			uploadFolder = externalUploadFolder;
		}

		if (uploadFolder == null) {
//			throw new RuntimeException("Context path \"/external/upload\" setting NOT found in server.xml !!!");
		}
	}

	private String getExternalUploadFolder() {

		String extranalUploadFolder = null;

		StandardService catalinaService = getCatalinaService();

		if (catalinaService == null) {
			return null;
		}

		Engine engine = catalinaService.getContainer();

		Host host = (Host) engine.findChild(engine.getDefaultHost());

		for (Container container : host.findChildren()) {

			if (container instanceof StandardContext) {

				StandardContext context = (StandardContext) container;

				if ("/external/upload".equals(context.getPath())) {
					hasExternalUpload = true;

					String docBase = context.getDocBase();

					if (!docBase.endsWith("\\") && !docBase.endsWith("/")) {
						docBase = docBase + File.separator;
					}
					extranalUploadFolder = docBase;

				}
			}
		}

		return extranalUploadFolder;
	}

	private static final String CATALINA_SERVICE_NAME = "Catalina";

	protected StandardService getCatalinaService() {

		StandardService service = null;
		try {
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

			org.apache.catalina.core.StandardServer server = (StandardServer) mbeanServer
				.getAttribute(new ObjectName("Catalina:type=Server"), "managedResource");

			if (server != null) {

				for (Service aService : server.findServices()) {

					if (aService.getName().equalsIgnoreCase(CATALINA_SERVICE_NAME)) {
						service = (StandardService) aService;
					}
				}
			}

		} catch (Throwable t) {
			logger.fatal("Fatal Error Recovering StandardServer from MBeanServer : "
				+ t.getClass().getName() + ": " + t.getMessage(), t);
		}
		return service;
	}
}
