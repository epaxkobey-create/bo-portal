package com.nv.commons.cache;

import com.nv.commons.cache.key.WebsiteUserKey;
import com.nv.commons.constants.OTPType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.OTPRecordDAO;
import com.nv.commons.dto.OTPRecord;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class OTPRecordCache extends AbstractCache {

	private ConcurrentHashMap<WebsiteUserKey, ConcurrentHashMap<OTPType, Set<OTPRecord>>> cache = new ConcurrentHashMap<>();

	private Timestamp latestUpdateTime = new Timestamp(System.currentTimeMillis());

	private static final int CACHE_MAX_HOURS = 24;

	private static final OTPRecordCache instance = new OTPRecordCache();

	private OTPRecordCache() {
	}

	public static OTPRecordCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		// cache 初始化，開始時間
		Date date = DateUtils.addHours(new Date(), -CACHE_MAX_HOURS);
		// 這邊會被 refresh 呼叫，所以要用暫存的方式，避免讀取時被改到
		ConcurrentHashMap<WebsiteUserKey, ConcurrentHashMap<OTPType, Set<OTPRecord>>> tempCache = new ConcurrentHashMap<>();
		try (Connection conn = DBPool.getReadConnection()) {

			List<OTPRecord> otpRecordList = OTPRecordDAO.findAfterTime(conn, new Timestamp(date.getTime()));

			for (OTPRecord otpRecord : otpRecordList) {
				WebsiteUserKey websiteUserKey = new WebsiteUserKey(otpRecord.getWebsiteType(), otpRecord.getUserId());
				OTPType otpType = OTPType.getInstance(otpRecord.getOtpType());
				tempCache.computeIfAbsent(websiteUserKey, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(otpType, l -> new HashSet<>()).add(otpRecord);
			}

			cache = tempCache;
		} catch (Exception ex) {
			LogUtils.SYS.error("error while fetch OTP Record list", ex);
		}
	}

	@Override
	public void update() {

		long aMinuteAgo = System.currentTimeMillis() - 60 * 1000;

		try (Connection conn = DBPool.getReadConnection()) {
			Timestamp temp = new Timestamp(aMinuteAgo);
			List<OTPRecord> otpRecordList = OTPRecordDAO.findAfterTime(conn, latestUpdateTime);

			for (OTPRecord otpRecord : otpRecordList) {
				WebsiteUserKey websiteUserKey = new WebsiteUserKey(otpRecord.getWebsiteType(), otpRecord.getUserId());
				OTPType otpType = OTPType.getInstance(otpRecord.getOtpType());
				cache.computeIfAbsent(websiteUserKey, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(otpType, l -> new HashSet<>()).add(otpRecord);
			}

			//MEMO 若無資料更新 則將下次查詢的時間推進到這次開始做Update時間的前一分鐘
			if (latestUpdateTime.compareTo(temp) == 0) {
				latestUpdateTime = new Timestamp(aMinuteAgo);
			} else {
				latestUpdateTime = temp;
			}
		} catch (Exception ex) {
			LogUtils.SYS.error("error while update OTPRecord list", ex);
		}
	}

	public void clean() {

		Date date = DateUtils.addHours(new Date(), -CACHE_MAX_HOURS);

		Predicate<OTPRecord> isExpired = o -> o.getTime().getTime() < date.getTime();

		cache.forEach((k, v) -> v.forEach((o, l) -> l.removeIf(isExpired)));

	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh OTP Record Cache");
	}

	@Override
	public String getCacheInfo() {
		return JSONUtils.toJsonString(cache.values());
	}

	public OTPRecord getLatestOTPRecordInMinutes(WebSiteType webSiteType, String userId, OTPType[] otpTypes,
		int minute) {

		WebsiteUserKey websiteUserKey = new WebsiteUserKey(webSiteType.unique(), userId);

		ConcurrentHashMap<OTPType, Set<OTPRecord>> userOTPMap = cache.get(websiteUserKey);

		if (userOTPMap == null) {
			return null;
		}

		Set<OTPRecord> otpRecordSet = new HashSet<>();

		for (OTPType otpType : otpTypes) {
			Set<OTPRecord> records = userOTPMap.get(otpType);
			if (records != null) {
				otpRecordSet.addAll(records);
			}
		}

		if (otpRecordSet.isEmpty()) {
			return null;
		}

		Date date = DateUtils.addMinutes(new Date(), -minute);
		return otpRecordSet.stream().filter(o -> o.getTime().getTime() > date.getTime())
			.max(Comparator.comparing(OTPRecord::getTime)).orElse(null);
	}

}