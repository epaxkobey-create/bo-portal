package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameSessionUsageDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.GameSessionUsage;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.LogUtils;

public class GameSessionUsageResetJobBO {

	private static final int MAX_RETRIES = 3;

	public static void run() {
		try {
			Timestamp cutoff = new Timestamp(
				System.currentTimeMillis() - (SystemConstants.GAME_SESSION_USAGE_RESET_MINUTES * 60 * 1000L));

			List<GameSessionUsage> allRecords = DbExecutor.query(
				conn -> GameSessionUsageDAO.getExpiredGameSessionUsage(conn, cutoff)
			);
			LogUtils.SYS.info("GameSessionUsageResetJob start, candidates for reset: {}", allRecords.size());
			processInParallel(allRecords);
		} catch (Exception e) {
			LogUtils.SYS.error("GameSessionUsageResetJob failed: {}", e.getMessage(), e);
		}
	}

	private static void processInParallel(List<GameSessionUsage> records) {
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<?>> futures = records.stream()
				.map(record -> executor.submit(() -> {
					try {
						checkAndResetIfExpired(record);
					} catch (Exception e) {
						LogUtils.SYS.error("Failed to reset usage for user: {}, error: {}",
							record.getUserKey(), e.getMessage(), e);
					}
				}))
				.collect(Collectors.toList());

			futures.forEach(future -> {
				try {
					future.get();
				} catch (Exception e) {
					LogUtils.SYS.error("Error waiting for reset task: {}", e.getMessage(), e);
				}
			});
		}
	}

	private static void checkAndResetIfExpired(GameSessionUsage record) throws Exception {
		String[] parseUserKey = AccountUtils.parseUserKeyEnhancement(record.getUserKey());
		WebSiteType webSiteType = WebSiteType.getInstance(Integer.parseInt(parseUserKey[0]));
		String userId = parseUserKey[1];

		// 从 cache 或 DB 拿用户的 TIME_SPENT_LIMIT 设定
		AccountPlayResponsiblySetting setting = AccountPlayResponsiblySettingBO
			.getPlayResponsiblyFromCacheOrDB(
				userId,
				webSiteType,
				AccountPlayResponsiblyType.TIME_SPENT_LIMIT,
				AccountPlayResponsiblyPeriodType.DAILY
			);

		if (setting == null) {
			// 没有设 TIME_SPENT_LIMIT 或还没生效，跳过
			LogUtils.SYS.debug("No active TIME_SPENT_LIMIT setting for user: {}, skip", record.getUserKey());
			return;
		}

		if (record.getPeriodStartTime() == null) {
			LogUtils.SYS.warn("periodStartTime is null for user: {}, skip", record.getUserKey());
			return;
		}

		// if setting == null 就等于 unlimited snapshot = 0;

		// 用 BetRulePeriodUtil 计算当前周期起点
		// 例如：effective_time = 今天 10:00 → 周期起点 = 今天 10:00
		//       effective_time = 昨天 10:00 → 周期起点 = 今天 10:00
		Timestamp now = new Timestamp((System.currentTimeMillis() / 60000) * 60000);

		Timestamp periodStart = getDailyPeriodStart(
			record.getPeriodStartTime(), now
		);

		// periodStartTime 在当前周期起点之前 → 上个周期的记录，需要 reset
		if (!isPeriodExpired(record.getPeriodStartTime(), periodStart)) {
			return;
		}

		LogUtils.SYS.info("Period expired for user: {}, periodStartTime: {}, periodStart: {}",
			record.getUserKey(), record.getPeriodStartTime(), periodStart);

		resetUsage(record, setting);
	}

	/**
	 * periodStartTime 在 periodStart 之前 → 需要 reset
	 */
	private static boolean isPeriodExpired(Timestamp periodStartTime, Timestamp periodStart) {
		if (periodStartTime == null || periodStart == null)
			return true;
		return periodStartTime.before(periodStart);
	}

	private static void resetUsage(GameSessionUsage record, AccountPlayResponsiblySetting setting) throws Exception {
		// 取最新的 limitSnapshot（用户可能改过设定）
		BigDecimal newLimitSnapshot = BigDecimal.valueOf(Integer.parseInt(setting.getCurrentValue()))
			.multiply(BigDecimal.valueOf(SystemConstants.GAME_SESSION_USAGE_MULTIPLIER));

		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			try {
				int result = DbExecutor.update(conn -> {
					// 重新 fetch 最新记录（防并发）
					GameSessionUsage latest = GameSessionUsageDAO.getGameSessionUsage(
						conn, record.getUserKey()
					);

					if (latest == null)
						return 0;

					// 双重检查（另一个进程可能已经 reset 了）
					Timestamp now = new Timestamp((System.currentTimeMillis() / 60000) * 60000);

					Timestamp periodStart = getDailyPeriodStart(
						record.getPeriodStartTime(), now
					);
					if (!isPeriodExpired(latest.getPeriodStartTime(), periodStart)) {
						LogUtils.SYS.debug("Already reset by another process for user: {}", record.getUserKey());
						return 0;
					}

					return GameSessionUsageDAO.resetUsageAndUpdateLimit(
						conn, record.getUserKey(), newLimitSnapshot, latest.getVersion()
					);
				});

				if (result > 0) {
					LogUtils.SYS.info("Reset usage success for user: {}, newLimitSnapshot: {}s",
						record.getUserKey(), newLimitSnapshot);
				}
				return;

			} catch (Exception e) {
				if (attempt == MAX_RETRIES - 1) {
					LogUtils.SYS.debug("Retrying reset time spent limit FAILED {} for user: {}", attempt + 1,
						record.getUserKey());
					throw e;
				}
				LogUtils.SYS.debug("Retrying reset attempt {} for user: {}", attempt + 1, record.getUserKey());

			}
		}
	}

	private static Timestamp getDailyPeriodStart(Timestamp effectiveTime, Timestamp currentTime) {
		LocalDateTime anchor = effectiveTime.toLocalDateTime();
		LocalDateTime current = currentTime.toLocalDateTime();

		// Get today at anchor's time
		LocalDateTime periodStart = current
			.withHour(anchor.getHour())
			.withMinute(anchor.getMinute())
			.withSecond(anchor.getSecond())
			.withNano(anchor.getNano());

		// If current time is before today's anchor time, use yesterday
		if (current.isBefore(periodStart)) {
			periodStart = periodStart.minusDays(1);
		}

		return Timestamp.valueOf(periodStart);
	}
}

