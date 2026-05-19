package com.nv.commons.cache;

import com.nv.commons.constants.BankType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.WebsiteBankDAO;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.WebsiteBank;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsiteBankCache extends AbstractCache {

	private final Map<WebSiteType, Map<Integer, WebsiteBank>> cache = new EnumMap<>(WebSiteType.class);

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private Timestamp lastUpdateTime = new Timestamp(0);

	private static final WebsiteBankCache instance = new WebsiteBankCache();

	private WebsiteBankCache() {
	}

	public static WebsiteBankCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {
		update();
	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public void update() {

		if (lastUpdateTime.getTime() == 0) {
			initWithBankCache();
		}

		List<WebsiteBank> websiteBankList = queryWebsiteBankListFromDB();
		if (websiteBankList.isEmpty()) {
			return;
		}

		boolean hasNewData = false;

		Timestamp maxUpdateTime = lastUpdateTime;

		for (WebsiteBank websiteBankInDB : websiteBankList) {

			if (websiteBankInDB.getUpdateTime().compareTo(maxUpdateTime) > 0) {
				maxUpdateTime = websiteBankInDB.getUpdateTime();
			}

			if (!WebSiteType.checkWebsiteType(websiteBankInDB.getWebsiteType())) {
				continue;
			}

			Map<Integer, WebsiteBank> websiteBankSet = cache.computeIfAbsent(
				WebSiteType.getInstance(websiteBankInDB.getWebsiteType()), k -> new ConcurrentHashMap<>());

			WebsiteBank websiteBankInCache = websiteBankSet.get(websiteBankInDB.getBankId());

			boolean dbDataChanged =
				(websiteBankInCache != null && cacheDataIsDifferentFromDB(websiteBankInDB, websiteBankInCache));

			if (websiteBankInCache == null || dbDataChanged) {
				// update cache
				websiteBankSet.put(websiteBankInDB.getBankId(), websiteBankInDB);
				hasNewData = true;
			}
		}

		if (hasNewData) {
			lastUpdateTime = new Timestamp(maxUpdateTime.getTime() - ERROR_VALUE);
		} else {
			lastUpdateTime = maxUpdateTime;
		}

		// self update
		if (hasNewData) {
			update();
		}
	}

	private void initWithBankCache() {

		if (!cache.isEmpty()) {
			// already initialized
			return;
		}

		for (WebsiteInfo websiteInfo : WebsiteInfoCache.getInstance().getAll()) {

			if (!WebSiteType.checkWebsiteType(websiteInfo.getId())) {
				continue;
			}

			WebSiteType webSiteType = WebSiteType.getInstance(websiteInfo.getId());
			if (webSiteType == null) {
				continue;
			}

			for (CurrencyType currencyType : CurrencyType.values()) {

				for (Bank bank : BankCache.getInstance().getAll(currencyType)) {

					if (bank.getCurrencyTypeId() == currencyType.unique() && isRealBank(bank)) {

						Map<Integer, WebsiteBank> set = cache
							.computeIfAbsent(webSiteType, k -> new ConcurrentHashMap<>());
						// default
						WebsiteBank websiteBank = new WebsiteBank();
						websiteBank.setWebsiteType(webSiteType.unique());
						websiteBank.setBankId(bank.getId());
						websiteBank.setDisplayName(bank.getBankName());
						websiteBank.setStatus(BinaryStatusType.ACTIVE.unique());
						websiteBank.setDisplayOrder(0);
						websiteBank.setCreator("SYS");
						websiteBank.setUpdater("SYS");
						websiteBank.setCreateTime(lastUpdateTime);
						websiteBank.setUpdateTime(lastUpdateTime);
						//
						set.put(websiteBank.getBankId(), websiteBank);
					}
				}
			}
		}
	}

	private boolean isRealBank(Bank bank) {

		return bank.getId() != 0
			// MEMO: 沒有 bankType 是 0 (LOCAL_BANK) 的銀行, 所有實體銀行都是 bankType ONLINE_BANKING , 其他都是第三方支付
			&& bank.getBankType() == BankType.ONLINE_BANKING.unique();
	}

	private List<WebsiteBank> queryWebsiteBankListFromDB() {

		List<WebsiteBank> websiteBankList = Collections.emptyList();

		// select 回傳後就可以 close conn, 不用等 list loop 完
		try (Connection conn = DBPool.getReadConnection()) {

			if (lastUpdateTime.getTime() == 0) {
				websiteBankList = WebsiteBankDAO.findAll(conn);
			} else {
				websiteBankList = WebsiteBankDAO.findByUpdateTime(conn, lastUpdateTime);
			}
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		return websiteBankList;
	}


	private boolean cacheDataIsDifferentFromDB(WebsiteBank websiteBankInDB, WebsiteBank websiteBankInCache) {
		return websiteBankInCache.getStatus() != websiteBankInDB.getStatus()
			|| websiteBankInCache.getDisplayOrder() != websiteBankInDB.getDisplayOrder()
			|| !websiteBankInCache.getDisplayName().equals(websiteBankInDB.getDisplayName())
			|| !websiteBankInCache.getUpdateTime().equals(websiteBankInDB.getUpdateTime());
	}


	/**
	 * TODO: impl
	 */
	@Override
	public String getCacheInfo() {
		return "{}";
	}
}
