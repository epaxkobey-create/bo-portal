package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameSessionUsageDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.logging.log4j.Logger;

public class GameSessionUsageBO {

	// insert  new session usage
	private final static int MAX_RETRIES = 3;
	private static final Logger logger = LogUtils.SYS;

	public static int initNewGameSession(String userKey) throws Exception {
		String[] parseUserKey = AccountUtils.parseUserKeyEnhancement(userKey);
		WebSiteType webSiteType = WebSiteType.getInstance(Integer.parseInt(parseUserKey[0]));

		AccountPlayResponsiblySetting accountPlayResponsiblySetting = AccountPlayResponsiblySettingBO.getPlayResponsiblyFromCacheOrDB(
			parseUserKey[1], webSiteType, AccountPlayResponsiblyType.TIME_SPENT_LIMIT,
			AccountPlayResponsiblyPeriodType.DAILY
		);

		BigDecimal currentLimitSnapShot;

		if (accountPlayResponsiblySetting != null) {
			currentLimitSnapShot = BigDecimal.valueOf(Integer.parseInt(accountPlayResponsiblySetting.getCurrentValue()))
				.multiply(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER));
		} else {
			currentLimitSnapShot = BigDecimal.ZERO;
		}

		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			// Re-read on every attempt to get the latest version and data
			GameSessionUsage existing = getGameSessionUsage(userKey);

			if (existing == null) {
				try{
					Timestamp periodStartTime = new Timestamp(System.currentTimeMillis());
					return insertNewGameUsage(userKey, currentLimitSnapShot, BinaryStatusType.ACTIVE.unique(),
						periodStartTime);
				}catch(Exception e){
					if (attempt < MAX_RETRIES - 1) {
						logger.debug("Retrying to create new game session attempt {} for user {}", attempt + 1, userKey);
						continue;
					}
					logger.error("create new game session failed for user {}: {}", userKey, e.getMessage(), e);
					return 0;
				}

				//					DbExecutor.update(conn ->
				//					GameSessionUsageDAO.insertNewGameSessionUsage(conn, userKey, currentLimitSnapShot,
				//						BinaryStatusType.ACTIVE.unique())
				//				);
			}
			if (BinaryStatusType.INACTIVE.unique() == existing.getSessionStatus()) {
				try {
					final BigDecimal version = existing.getVersion();
					return DbExecutor.update(conn -> {
						int result = GameSessionUsageDAO.updateActiveTimeAndStatus(conn, userKey, version);
						if (result == 0)
							throw new SQLException("Optimistic lock failure on reactivate");
						return result;
					});
				} catch (Exception e) {
					if (attempt < MAX_RETRIES - 1) {
						logger.debug("Retrying reactivate attempt {} for user {}", attempt + 1, userKey);
						continue;
					}
					logger.error("reactivate failed for user {}: {}", userKey, e.getMessage(), e);
					return 0;
				}
			}

			// Active session — accumulate elapsed time from last heartbeat
			BigDecimal usage;
			if (existing.getUsage().compareTo(BigDecimal.ZERO) > 0) {
				long remainingTime = System.currentTimeMillis() - existing.getLastActiveTime().getTime();
				BigDecimal calculateCurrentUsedUsage = existing.getUsage()
					.add(BigDecimal.valueOf(remainingTime / 1000));

				boolean hasLimit = existing.getLimitSnapshot().compareTo(BigDecimal.ZERO) > 0;
				if (hasLimit && calculateCurrentUsedUsage.compareTo(existing.getLimitSnapshot()) > 0) {
					usage = existing.getLimitSnapshot();
				} else {
					usage = calculateCurrentUsedUsage;
				}
			} else {
				usage = BigDecimal.ZERO;
			}

			try {
				final BigDecimal version = existing.getVersion();
				return DbExecutor.update(conn -> {
					int result = GameSessionUsageDAO.updateGameSessionUsage(conn, userKey, usage, version);
					if (result == 0) {
						throw new SQLException("Optimistic lock failure on accumulate");
					}
					return result;
				});
			} catch (Exception e) {
				if (attempt < MAX_RETRIES - 1) {
					logger.debug("Retrying update game usage attempt {} for user {}", attempt + 1, userKey);
					continue;
				}
				logger.error("Error in update game usage for user {}: {}", userKey, e.getMessage(), e);
				return 0;
			}
		}
		logger.error("Max retries exceeded in initNewGameSession for user {}", userKey);
		return 0;
	}

	public static GameSessionUsage getGameSessionUsage(String userKey) throws Exception {
		return DbExecutor.query(conn -> GameSessionUsageDAO.getGameSessionUsage(conn, userKey));
	}

	public static boolean endSession(String userKey) throws Exception {
		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			// Re-read on every attempt to get the latest version and data
			GameSessionUsage existing = getGameSessionUsage(userKey);

			if (existing == null) {
				logger.warn("endSession called but no record found for user {}", userKey);
				return false;
			}

			if (BinaryStatusType.INACTIVE.unique() == existing.getSessionStatus()) {
				logger.debug("endSession: session already ended for user {}, skip", userKey);
				return false;
			}

			long now = System.currentTimeMillis();
			BigDecimal elapsed = BigDecimal.valueOf(
				(now - existing.getLastActiveTime().getTime()) / 1000
			);

			BigDecimal totalUsage = existing.getUsage().add(elapsed);
			boolean hasLimit = existing.getLimitSnapshot().compareTo(BigDecimal.ZERO) > 0;
			if (hasLimit && totalUsage.compareTo(existing.getLimitSnapshot()) > 0) {
				totalUsage = existing.getLimitSnapshot();
			}

			final BigDecimal finalTotal = totalUsage;
			final BigDecimal version = existing.getVersion();

			try {
				int result = DbExecutor.update(conn -> {
					int r = GameSessionUsageDAO.endGameSession(conn, userKey, finalTotal, version);
					if (r == 0)
						throw new SQLException("Optimistic lock failure on endSession");
					return r;
				});
				logger.info("endSession success for user {}, totalUsage={}s", userKey, finalTotal);
				return result > 0;

			} catch (Exception e) {
				if (attempt < MAX_RETRIES - 1) {
					logger.debug("Retrying endSession attempt {} for user {}", attempt + 1, userKey);
					continue;
				}
				logger.error("endSession failed for user {}: {}", userKey, e.getMessage(), e);
				return false;
			}
		}

		logger.error("endSession max retries exceeded for user {}", userKey);
		return false;
	}

	private static int updateUsageAndLimitSnapShot(String userKey, BigDecimal limitSnapshot, BigDecimal version,
		BigDecimal usage, Timestamp periodStartTime)
		throws Exception {
		return DbExecutor.update(conn -> GameSessionUsageDAO.updateLimitSnapShotAndUsage(
			conn, userKey, limitSnapshot, usage, version, periodStartTime
		));
	}

	public static void updateGameSessionUsageIfLimitUpdateImmediate(String userKey, BigDecimal newLimitSnapshot,
		Timestamp periodStartTime)
		throws Exception {
		GameSessionUsage gameSessionUsage = getGameSessionUsage(userKey);
		BigDecimal newGameSessionUsageLimitValue = newLimitSnapshot
			.multiply(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER));

		if (gameSessionUsage != null) {
			updateUsageAndLimitSnapShot(
				userKey, newGameSessionUsageLimitValue, gameSessionUsage.getVersion(), gameSessionUsage.getUsage(),
				periodStartTime
			);
			return;
		}
		// if null, insert a new session for him, to avoid incorrect reset time (period start time)
		insertNewGameUsage(userKey, newGameSessionUsageLimitValue, BinaryStatusType.INACTIVE.unique(), periodStartTime);
	}

	private static int insertNewGameUsage(String userKey, BigDecimal limitSnapshot, int sessionStatus,
		Timestamp periodStartTime)
		throws Exception {
		return DbExecutor.update(
			conn -> GameSessionUsageDAO.insertNewGameSessionUsage(conn, userKey, limitSnapshot, sessionStatus,
				periodStartTime));
	}

	/**
	 * server 关闭时调用
	 * 把所有 session_status = ACTIVE 的 usage 更新到最新，然后 end session
	 * 防止 server 突然关闭时 usage 没有被记录
	 */
	public static void batchEndAllActiveSessions() {
		logger.info("batchEndAllActiveSessions start");

		try {
			// 1. 拉出所有 ACTIVE 的 session
			List<GameSessionUsage> activeSessions = DbExecutor.query(
				GameSessionUsageDAO::getAllActiveGameSessionUsage
			);

			if (activeSessions.isEmpty()) {
				logger.info("batchEndAllActiveSessions: no active sessions found");
				return;
			}

			logger.info("batchEndAllActiveSessions: found {} active sessions", activeSessions.size());

			// 2. 用同一个 connection 批量处理
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);

				long now = System.currentTimeMillis();
				int successCount = 0;

				for (GameSessionUsage session : activeSessions) {
					// 计算 elapsed
					long elapsedMs = now - session.getLastActiveTime().getTime();
					BigDecimal elapsedSeconds = BigDecimal.valueOf(elapsedMs / 1000L);

					// 累积 totalUsage
					BigDecimal totalUsage = session.getUsage().add(elapsedSeconds);

					// cap 住 limitSnapshot（0 = 无限制，跳过 cap）
					boolean hasLimit = session.getLimitSnapshot().compareTo(BigDecimal.ZERO) > 0;
					if (hasLimit && totalUsage.compareTo(session.getLimitSnapshot()) > 0) {
						totalUsage = session.getLimitSnapshot();
					}

					int result = GameSessionUsageDAO.endGameSession(
						conn, session.getUserKey(), totalUsage, session.getVersion()
					);

					if (result > 0) {
						successCount++;
					} else {
						logger.warn("batchEndAllActiveSessions: failed to end session for user {}",
							session.getUserKey());
					}
				}

				conn.commit();
				logger.info("batchEndAllActiveSessions done: {}/{} sessions ended",
					successCount, activeSessions.size());

			} catch (Exception e) {
				DbUtils.rollback(conn);
				logger.error("batchEndAllActiveSessions failed: {}", e.getMessage(), e);
			} finally {
				DbUtils.close(conn);
			}

		} catch (Exception e) {
			logger.error("batchEndAllActiveSessions query failed: {}", e.getMessage(), e);
		}
	}

	public static boolean batchEndActiveSessions(List<String> userKeys) throws Exception {
		if (userKeys == null || userKeys.isEmpty()) {
			logger.info("batchEndActiveSessions: empty userKeys, skip");
			return true;
		}

		Map<String, BigDecimal> activeUsersAndUsage = getActiveUsersAndUsage(userKeys);

		int result = DbExecutor.update(conn -> GameSessionUsageDAO.batchEndGameSessions(conn, activeUsersAndUsage));
		return result > 0;

	}

	private static Map<String, BigDecimal> getActiveUsersAndUsage(List<String> userKeys) throws Exception {
		List<GameSessionUsage> gemeSessionUsageList = DbExecutor.query(
			conn -> GameSessionUsageDAO.getGameSessionUsageByUserKeys(conn, userKeys));

		long now = System.currentTimeMillis();
		Map<String, BigDecimal> activeUsersAndUsageMap = new HashMap<>();

		for (GameSessionUsage gameSessionUsage : gemeSessionUsageList) {

			long elapsedMs = now - gameSessionUsage.getLastActiveTime().getTime();
			BigDecimal elapsedSeconds = BigDecimal.valueOf(elapsedMs / 1000L);
			BigDecimal totalUsage = gameSessionUsage.getUsage().add(elapsedSeconds);

			boolean hasLimit = gameSessionUsage.getLimitSnapshot().compareTo(BigDecimal.ZERO) > 0;
			if (hasLimit && totalUsage.compareTo(gameSessionUsage.getLimitSnapshot()) > 0) {
				totalUsage = gameSessionUsage.getLimitSnapshot();
			}

			activeUsersAndUsageMap.put(gameSessionUsage.getUserKey(), totalUsage);
		}

		return activeUsersAndUsageMap;

	}

}
