package com.nv.commons.cache;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.bo.RemotingBO;
import com.nv.commons.bo.ServerInfoBO;
import com.nv.commons.constants.ServerNodeType;
import com.nv.commons.dto.ServerInfoEntity;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.HttpRequester;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ServerInfoUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Neutec
 */
// MEMO: RemotingCaller 其實不是 cache 不需要 extends AbstractCache
public class RemotingCaller {

	private static final RemotingCaller instance = new RemotingCaller();

	private Timestamp serverInfoLastUpdateDate = new Timestamp(0);
	private final Object lock = new Object();

	private String localIP; //this server IP
	private int localPort; //this server Port

	private final Map<Long, ServerInfoEntity> playerServerInfos = new ConcurrentHashMap<>();
	private final Map<Long, ServerInfoEntity> backendApiServerInfos = new ConcurrentHashMap<>();
	private final Map<Long, ServerInfoEntity> managerServerInfos = new ConcurrentHashMap<>();
	private final Map<Long, ServerInfoEntity> apiServerInfos = new ConcurrentHashMap<>();

	/**
	 *
	 */
	private final HashMap<ServerNodeType, Map<Long, ServerInfoEntity>> serverTypeToServerInfosMap = new HashMap<>() {{

		if (ServerInfoUtils.isManagerServer()) {
			//========== PlayerServer ==========
			put(ServerNodeType.PLAYER, playerServerInfos);
			//========== BackendApiServer ==========
			put(ServerNodeType.BACKEND_API, backendApiServerInfos);
			//========== ManagerServer ==========
			put(ServerNodeType.MANAGER, managerServerInfos);
			//========== APIServer ==========
			put(ServerNodeType.API, apiServerInfos);
		}
	}};

	// 給remote call使用
	private Class<? extends RemotingBO> actionClass;

	private Object actionObj;

	private final String tkCode = "YK9089897x";

	private RemotingCaller() {
		super();
		try {
			actionClass = Class.forName(RemotingBO.class.getName()).asSubclass(RemotingBO.class);
			actionObj = actionClass.getDeclaredConstructor().newInstance();
		} catch (Throwable e) {
			LogUtils.SYS.info(e.getMessage(), e);
		}
	}

	public static RemotingCaller getInstance() {
		return instance;
	}


	//執行方法
	private boolean executeMethod(String methodName, HttpServletRequest request, HttpServletResponse response)
		throws SecurityException, IllegalArgumentException {
		try {
			Method method = getActionClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			return (Boolean) method.invoke(getActionObject(), request, response);
		} catch (Throwable e) {
			LogUtils.SYS.info("call methodName : {}", methodName);
			LogUtils.SYS.info(e.getMessage(), e);
		}
		return false;
	}

	private Class<? extends RemotingBO> getActionClass() throws Throwable {
		if (actionClass != null) {
			return actionClass;
		}
		return Class.forName(RemotingBO.class.getName()).asSubclass(RemotingBO.class);

	}

	private Object getActionObject()
		throws Throwable {
		if (actionObj == null) {
			actionObj = getActionClass().getDeclaredConstructor().newInstance();
		}
		return actionObj;
	}

	private void response(HttpServletResponse response, String msg) {
		try (ServletOutputStream out = response.getOutputStream()) {
			out.print(msg);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.info(e.getMessage(), e);
		}
	}

	public void receive(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tk = RequestParser.getStringParameter(request, 10, "tk", null);
		request.setCharacterEncoding("UTF-8");
		String localIP = request.getLocalAddr();
		//		String remoteIP = request.getRemoteAddr();

		//如果密碼不對, 如果不在IP列表內, 返回
		if (!tk.equals(tkCode)) {
			response(response, "code error");
			return;
		}
		String method = RequestParser.getStringParameter(request, 15, "method", null);
		executeMethod(method, request, response);
		response(response, "Received: method:" + method + " from ip:" + localIP);
	}

	public void init() {

		LogUtils.SYS.info("init serverInfo cache start");

		long currentTime = System.currentTimeMillis();
		long time1 = -1;

		try {
			this.localIP = HostAddressUtils.getLocalIPAddress();
			//更新自己在資料庫內的狀態
			ServerInfoBO.updateStatusByIP(localIP, true);

			List<ServerInfoEntity> serverList = ServerInfoBO.queryAllServerInfo();

			if (!serverList.isEmpty()) {

				time1 = this.serverInfoLastUpdateDate.getTime();

				LogUtils.SYS.info("serverList : {}", JSONUtils.toJsonString(serverList));

				for (ServerInfoEntity serverInfo : serverList) {

					time1 = Math.max(time1, serverInfo.getUpdateTime().getTime());

					final int serverType = serverInfo.getServerType();
					final int selfServerType = SystemInfo.getInstance().getServerType();

					// MEMO: double confirm selfServerType match db serverType
					if ((selfServerType & serverType) > 0
						&& serverInfo.getIp().equals(localIP)) {
						localPort = serverInfo.getPort();
					}

					// 這邊要考慮 serverType 是 複合型態, 所以只能一個個條件判別
					serverTypeToServerInfosMap.forEach((serverNodeType, serverInfos) -> {

						if ((serverNodeType.unique() & serverType) > 0) {

							LogUtils.SYS.info("Add serverInfo to {} : {}",
								serverNodeType,
								JSONUtils.toJsonString(serverInfo));

							serverInfos.put(serverInfo.getId(), serverInfo);
						}
					});
				}
			}
			synchronized (lock) {
				if (time1 > 0) {
					this.serverInfoLastUpdateDate.setTime(time1);
				}
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		long duration = System.currentTimeMillis() - currentTime;
		if (duration > 1000) {
			LogUtils.SYS.error("Use : {}", duration);
		}

		LogUtils.SYS.info("init serverInfo cache end");

	}

	private void updateServerInfo(Map<Long, ServerInfoEntity> serverInfos, ServerInfoEntity serverInfo) {
		ServerInfoEntity record = serverInfos.get(serverInfo.getId());
		if (record == null) {
			return;
		}
		record.setIsActive(serverInfo.isActive());
		record.setUpdateTime(serverInfo.getUpdateTime());
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		try {
			List<ServerInfoEntity> serverInfoList = ServerInfoBO
				.queryServerInfoListByUpdateDate(serverInfoLastUpdateDate);

			if (!serverInfoList.isEmpty()) {

				Timestamp time1 = this.serverInfoLastUpdateDate;

				for (ServerInfoEntity serverInfo : serverInfoList) {

					time1 = DateUtils.max(time1, serverInfo.getUpdateTime());

					final int serverType = serverInfo.getServerType();

					// 這邊要考慮 serverType 是 複合型態, 所以只能一個個條件判別
					serverTypeToServerInfosMap.forEach((serverNodeType, serverInfos) -> {

						if ((serverNodeType.unique() & serverType) > 0) {

							updateServerInfo(serverInfos, serverInfo);
						}
					});
				}

				synchronized (lock) {
					if (time1 != null) {
						this.serverInfoLastUpdateDate = time1;
					}
				}
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		long duration = System.currentTimeMillis() - currentTime;
		if (duration > 1000) {
			LogUtils.SYS.error("Use : {}", duration);
		}
	}

	/**
	 * 送出 command 訊息給指定型態的web server(自身除外)
	 *
	 * @param serverType Server類型
	 * @param msg        訊息
	 */
	public void sendMessage(final int serverType, String msg) {

		final Set<String> serverInfoMapSet = new HashSet<>();

		LogUtils.SYS.info("sendMessage serverInfoMapSet : {}", serverInfoMapSet);
		LogUtils.SYS.info("sendMessage serverType : {}", serverType);

		serverTypeToServerInfosMap.forEach((serverNodeType, serverInfos) -> {

			LogUtils.SYS.info("sendMessage serverNodeType : {}", serverNodeType);
			LogUtils.SYS.info("sendMessage (serverNodeType.unique() & serverType) > 0 : {}",
				(serverNodeType.unique() & serverType) > 0);

			if ((serverNodeType.unique() & serverType) > 0) {

				addServerInfoMapSet(serverInfoMapSet, serverInfos.values());
			}
		});

		sendMessage(serverInfoMapSet, msg);
	}

	/**
	 * 加入 Active 的 Server 資訊(自身除外)
	 *
	 * @param serverInfoMapSet 不重複且 Active 的 Server 資訊(自身除外)
	 * @param serverInfos      Server 資訊物件集合
	 */
	private void addServerInfoMapSet(Set<String> serverInfoMapSet, Collection<ServerInfoEntity> serverInfos) {

		for (ServerInfoEntity serverInfo : serverInfos) {

			LogUtils.SYS.info("add serverInfo : {}", JSONUtils.toJsonString(serverInfo));

			if (serverInfo.getIp().equals(localIP) && serverInfo.getPort() == localPort) {
				LogUtils.SYS.info("skip self serverInfo");
				continue;
			}
			if (!serverInfo.isActive()) {
				LogUtils.SYS.info("skip inactive serverInfo");
				continue;
			}
			serverInfoMapSet.add(serverInfo.getIp() + ":" + serverInfo.getPort());
		}
	}

	/**
	 * 傳送訊息到指定清單中的 Server
	 *
	 * @param serverInfoList Server清單
	 * @param msg            訊息
	 */
	private void sendMessage(Collection<String> serverInfoList, final String msg) {
		try {

			LogUtils.SYS.info("sendMessage serverInfoList:{}", JSONUtils.toJsonString(serverInfoList));

			for (final String host : serverInfoList) {
				GlobalThreadPool.execute(() -> {
					try {
						String res = "http://" + host + "/notifycontroller?" + msg;

						LogUtils.SYS.info("send get to {}", res);

						HttpRequester.sendGet(res);

					} catch (java.net.ConnectException e) {
						LogUtils.SYS.info(e.getMessage());
					} catch (Exception e) {
						LogUtils.SYS.info("fail send get to {}", host);
						LogUtils.SYS.info(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			LogUtils.SYS.info(e.getMessage(), e);
		}
	}

	public String getTkCode() {
		return tkCode;
	}

}
