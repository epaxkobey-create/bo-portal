package com.nv.commons.bo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.utils.LogUtils;
import com.nv.websocket.WebSocketClient;

public class WebSocketBO {

	public static void sendRealityCheckReminder(Account playerInCache) {

		try {

			// get reality check reminder info
			String json = BTUserBO.getRealityCheckReminderInfo(playerInCache, "REALITY_CHECK_REMINDER");

			// increase reality check time
			playerInCache.getRealityCheckReminderData().increaseRealityCheckTime();

			// web socket send notification to player
			WebSocketClient.getInstance().notificationToUser(playerInCache.getUserKey(), json);

			// set reality check time updated to false
			playerInCache.getRealityCheckReminderData().setRealityCheckTimeUpdated(false);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public static void sendDepositLimitUsageUpdate(Account playerInCache) {

		try {
			// get deposit limit usage info
			String json = BTUserBO.getPaymentLimitUsageInfo(playerInCache, "DEPOSIT_LIMIT_USAGE_UPDATE", "");

			// web socket send push to player
			WebSocketClient.getInstance().pushToUser(playerInCache.getUserKey(), json);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public static void notifyTimeSpentLimit(String userKey) {

		try {

			GameSessionUsage gameSessionUsage = GameSessionUsageBO.getGameSessionUsage(userKey);

			if (gameSessionUsage == null) {
				LogUtils.SYS.warn("notifyTimeSpentLimit: no gameSessionUsage found for user {}", userKey);
				return;
			}

			BigDecimal usageInHour = gameSessionUsage.getUsage()
				.divide(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER), 0, RoundingMode.FLOOR);
			BigDecimal limitSnapShotInHour = gameSessionUsage.getLimitSnapshot()
				.divide(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER), 0, RoundingMode.FLOOR);
			String json = BTUserBO.getGameSessionUsageInfo("TIME_SPENT_LIMIT", limitSnapShotInHour, usageInHour);

			WebSocketClient.getInstance().notificationToUser(userKey, json);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}
}
