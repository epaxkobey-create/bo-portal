package com.nv.commons.cache;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.dto.UserSessionNetPosition;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.websocket.WebSocketConnectionManager;
import com.nv.websocket.message.MessageType;
import com.nv.websocket.message.WebSocketMessage;
import com.nv.websocket.message.data.PushData;

/**
 * Cache for Net Position
 * 用於存儲玩家在各遊戲中的淨部位資訊
 *
 * Key: userId (String)
 * Value: Map<gameId, netPosition>
 */
public class NetPositionCache extends AbstractCache {

	// userId -> Map<gameId, netPosition>
	private final ConcurrentHashMap<String, UserSessionNetPosition> cache = new ConcurrentHashMap<>();

	private static final NetPositionCache instance = new NetPositionCache();

	private NetPositionCache() {
	}

	public static NetPositionCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		// NetPosition 是運行時資料，不需要從資料庫載入
		// 系統啟動時為空，當玩家進入遊戲時才初始化
		cache.clear();
		LogUtils.SYS.info("NetPositionCache initialized");
	}

	@Override
	public void update() {
		// NetPosition 是即時更新的資料，不需要定期更新
		// 由 updateNetPosition API 主動更新
	}

	@Override
	public void refresh() {
		// 清空所有 NetPosition 資料
		cache.clear();
		LogUtils.SYS.info("NetPositionCache refreshed");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache);
	}

	/**
	 * 初始化玩家的遊戲 NetPosition
	 * 當玩家取得遊戲登入連結時呼叫
	 * 每次進入遊戲都是新的遊戲階段，NetPosition 重置為 0
	 *
	 * @param userKey 使用者 ID
	 */
	public void initNetPosition(String userKey) {
		UserSessionNetPosition userSessionNetPosition = cache.computeIfAbsent(
			userKey,
			k -> new UserSessionNetPosition(getUserIdByUserKey(userKey))
		);

		LogUtils.SYS.info("Init NetPosition for userId:{}", userKey);
	}

	/**
	 * 更新玩家的遊戲 NetPosition
	 * 當 Wallet 通知損益變化時呼叫
	 *
	 * @param userKey 使用者 ID
	 * @param gameId 遊戲 ID
	 * @param amount 損益金額（正數為盈利，負數為虧損）
	 * @return 更新後的 NetPosition
	 */
	public BigDecimal updateNetPosition(String userKey, int gameId, BigDecimal amount) {

		UserSessionNetPosition user = cache.computeIfAbsent(
			userKey,
			k -> new UserSessionNetPosition(getUserIdByUserKey(userKey))
		);
		var newSessionNetPosition = user.getLoginSessionNetPosition().add(amount);
		user.setLoginSessionNetPosition(newSessionNetPosition);


		// 更新 NetPosition：新值 = 舊值 + amount
		var gameSessionNetPositions = user.getGameSessionNetPositions();
		BigDecimal newNetPosition = gameSessionNetPositions.merge(
			gameId,
			amount,
			BigDecimal::add
		);

		LogUtils.SYS.info("Update NetPosition for userId:{}, gameId:{}, amount:{}, newNetPosition:{}",
			userKey, gameId, amount, newNetPosition);
		Thread.startVirtualThread( () -> sendWebSocketNotification(userKey));

		return newSessionNetPosition;
	}

	private void sendWebSocketNotification(String userKey) {
		// 此cache存在於WEB站台, 與websocket服務同機器
		// 因此不透過WebsocketApi推送, 而直接用WebSocketConnectionManager推送
		// 若之後雙方拆開機器, 需改用WebsocketApi推送
		try {
			var netPositions = NetPositionCache.getInstance().getNetPositions(userKey);
			var pushMsg = new PushData("NET_POSITION",
				"NET_POSITION",
				netPositions,
				"DATA_UPDATE",
				System.currentTimeMillis()
			);

			var message = WebSocketMessage.builder()
//				.type(MessageType.NET_POSITION)
//				.data(netPositions)
				.type(MessageType.PUSH)
				.data(pushMsg)
				.timestamp(System.currentTimeMillis())
				.build();

			WebSocketConnectionManager.getInstance().sendToUser(userKey, message);

		} catch (Exception e) {
			LogUtils.SYS.error("Failed to send net position update message to user: {}", userKey, e);
		}
	}


	/**
	 * 取得玩家的所有遊戲 NetPosition
	 *
	 * @param userKey 使用者 ID
	 * @return Map<gameId, netPosition>，如果不存在則返回空 Map
	 */
	public UserSessionNetPosition getNetPositions(String userKey) {
		UserSessionNetPosition userNetPositions = cache.computeIfAbsent(userKey ,
			k -> new UserSessionNetPosition(getUserIdByUserKey(userKey))
		);

		return userNetPositions;
	}

	/**
	 * 取得玩家的所有遊戲 NetPosition
	 *
	 * @param userKey 使用者 ID
	 * @return Map<gameId, netPosition>，如果不存在則返回空 Map
	 */
	public Map<Integer, BigDecimal> getGameNetPositions(String userKey) {
		ConcurrentHashMap<Integer, BigDecimal> userNetPositions = cache.get(userKey).getGameSessionNetPositions();

		if (userNetPositions == null) {
			return new ConcurrentHashMap<>();
		}

		// 返回副本以避免外部修改
		return new ConcurrentHashMap<>(userNetPositions);
	}

	/**
	 * 取得玩家特定遊戲的 NetPosition
	 *
	 * @param userKey 使用者 ID
	 * @param gameId 遊戲 ID
	 * @return NetPosition，如果不存在則返回 0
	 */
	public BigDecimal getNetPosition(String userKey, int gameId) {
		var userNetPositions = cache.get(userKey);

		if (userNetPositions == null) {
			return BigDecimal.ZERO;
		}
		var gameSessionNetPositions = userNetPositions.getGameSessionNetPositions();
		if (gameSessionNetPositions == null) {
			return BigDecimal.ZERO;
		}
		return gameSessionNetPositions.getOrDefault(gameId, BigDecimal.ZERO);
	}

	/**
	 * 清除玩家的所有 NetPosition
	 *
	 * @param userKey 使用者 ID
	 */
	public void clearUserNetPositions(String userKey) {
		cache.remove(userKey);
		LogUtils.SYS.info("Clear NetPositions for userId:{}", userKey);
	}

	/**
	 * 清除玩家特定遊戲的 NetPosition
	 *
	 * @param userKey 使用者 ID
	 * @param gameId 遊戲 ID
	 */
	public void clearGameSessionNetPosition(String userKey, int gameId) {
		try {
			var userNetPositions = cache.get(userKey);
			if (userNetPositions == null) {
				LogUtils.SYS.info("UserNetPositions not found for userId:{}, cannot clear gameId:{}", userKey, gameId);
				return;
			}
			var gameSessionNetPositions = userNetPositions.getGameSessionNetPositions();
			if (gameSessionNetPositions != null) {
				gameSessionNetPositions.remove(gameId);
				LogUtils.SYS.info("Clear NetPosition for userId:{}, gameId:{}", userKey, gameId);
				sendWebSocketNotification(userKey);
			}

		} catch (Exception e) {
			LogUtils.SYS.error("Failed to clear net position for user: {}, gameId: {}", userKey,
				gameId, e);
		}
	}

	/**
	 * 取得當前快取中的使用者數量
	 *
	 * @return 使用者數量
	 */
	public int getUserCount() {
		return cache.size();
	}

	private String getUserIdByUserKey(String userKey)
	{
		if(!userKey.contains("_"))
		{
			return userKey;
		}
		return userKey.split("_")[1];
	}
}
