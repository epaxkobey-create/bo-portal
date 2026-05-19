package com.nv.commons.cache;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyStatusType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.PeriodType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountPlayResponsiblySettingDAO;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Cache for Account Play Responsibly Settings with DB table accountPlayResponsibly
 * 負責快取玩家自我限制的設定資料
 */
public class AccountPlayResponsiblySettingCache extends AbstractCache {

	private static final AccountPlayResponsiblySettingCache instance = new AccountPlayResponsiblySettingCache();

	private final Map<CompositeKey, List<AccountPlayResponsiblySetting>> cache = new ConcurrentHashMap<>();

	public static AccountPlayResponsiblySettingCache getInstance() {
		return instance;
	}

	private Timestamp latestUpdateTime = new Timestamp(System.currentTimeMillis());

	@Override
	protected void init() {

		cache.clear();

		try (Connection conn = DBPool.getReadConnection()) {

			List<AccountPlayResponsiblySetting> list = AccountPlayResponsiblySettingDAO.findAll(conn);

			for (AccountPlayResponsiblySetting playResponsibly : list) {
				WebSiteType webSite = WebSiteType.getInstance(playResponsibly.getWebsiteType());
				String userId = playResponsibly.getUserId();
				AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(playResponsibly.getType());

				cache.computeIfAbsent(new CompositeKey(webSite, userId, type), k -> new ArrayList<>())
					.add(playResponsibly);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void update() {
		long aMinuteAgo = System.currentTimeMillis() - 60 * 1000;

		try (Connection conn = DBPool.getReadConnection()) {
			Timestamp temp = new Timestamp(aMinuteAgo);

			List<AccountPlayResponsiblySetting> list = AccountPlayResponsiblySettingDAO.getEntities(conn, latestUpdateTime);

			for (AccountPlayResponsiblySetting playResponsibly : list) {

				WebSiteType webSite = WebSiteType.getInstance(playResponsibly.getWebsiteType());
				String userId = playResponsibly.getUserId();
				AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(playResponsibly.getType());
				PeriodType periodTypeOfDbData = PeriodType.getInstanceOf(playResponsibly.getPeriodType());

				// primary key (USER_ID, WEBSITE_TYPE, TYPE, PERIOD_TYPE)
				CompositeKey key = new CompositeKey(webSite, userId, type);

				List<AccountPlayResponsiblySetting> settings = cache.computeIfAbsent(key, k -> new ArrayList<>());

				if (CollectionUtils.isNotEmpty(settings)) {
					OptionalInt existingIndex = IntStream.range(0, settings.size())
						.filter(i -> settings.get(i).getPeriodType() == periodTypeOfDbData.unique())
						.findFirst();

					if (existingIndex.isPresent()) {
						settings.set(existingIndex.getAsInt(), playResponsibly);
					} else {
						settings.add(playResponsibly);
					}
				} else {
					settings.add(playResponsibly);
				}

				if (playResponsibly.getUpdateTime().after(temp)) {
					temp = playResponsibly.getUpdateTime();
				}
			}

			//MEMO 若無資料更新 則將下次查詢的時間推進到這次開始做Update時間的前一分鐘
			if (latestUpdateTime.compareTo(temp) == 0) {
				latestUpdateTime = new Timestamp(aMinuteAgo);
			} else {
				latestUpdateTime = temp;
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.toString());
	}

	public AccountPlayResponsiblySetting getPlayResponsiblyOrDefault(WebSiteType webSiteType, String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType) {

		List<AccountPlayResponsiblySetting> settings = getPlayResponsiblyByType(webSiteType, userId, type);

		for (AccountPlayResponsiblySetting setting : settings) {
			if (setting.getPeriodType() == periodType.unique()) {
				return setting;
			}
		}

		// 都匹配不到，回傳預設值
		AccountPlayResponsiblySetting playResponsibly = new AccountPlayResponsiblySetting();
		playResponsibly.setUserId(userId);
		playResponsibly.setWebsiteType(webSiteType.unique());
		playResponsibly.setType(type.unique());
		playResponsibly.setPeriodType(periodType.unique());
		playResponsibly.setCurrentValue(String.valueOf(type.getDefaultValue()));
		playResponsibly.setNewValue(String.valueOf(type.getDefaultValue()));
		playResponsibly.setStatus(AccountPlayResponsiblyStatusType.ACTIVE.unique());
		playResponsibly.setEffectiveTime(null);

		return playResponsibly;
	}

	public AccountPlayResponsiblySetting getPlayResponsiblyOrNull(WebSiteType webSiteType, String userId,
		AccountPlayResponsiblyType type, AccountPlayResponsiblyPeriodType periodType) {

		List<AccountPlayResponsiblySetting> settings = getPlayResponsiblyByType(webSiteType, userId, type);

		if (settings.isEmpty()) {
			return null;
		}

		for (AccountPlayResponsiblySetting setting : settings) {
			// 只有 creator 不為 null 且不為空，且 periodType 相符時才回傳
			// TODO: 当playResponsibly是全新的 就是从cache里找不到的？所以 getCreator() 會是 null? by luke
			if (setting.getCreator() != null && !setting.getCreator().isEmpty()
				&& setting.getPeriodType() == periodType.unique()) {
				return setting;
			}
		}

		return null;
	}

	@NotNull
	public List<AccountPlayResponsiblySetting> getPlayResponsiblyByType(WebSiteType webSiteType, String userId,
		AccountPlayResponsiblyType type) {

		return cache.getOrDefault(new CompositeKey(webSiteType, userId, type), Collections.emptyList());
	}

	public record CompositeKey(WebSiteType webSiteType, String userId, AccountPlayResponsiblyType type) {

	}
}
