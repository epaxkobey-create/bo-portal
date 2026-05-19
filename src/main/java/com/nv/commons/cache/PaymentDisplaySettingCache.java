package com.nv.commons.cache;

import static com.nv.commons.utils.JSONUtils.toJsonString;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.PaymentDisplaySettingDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.dto.PaymentDisplaySetting;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.paymentGateway.dto.PGDeposit;
import com.nv.commons.paymentGateway.dto.PGServerInfo;
import com.nv.commons.paymentGateway.proxy.PaymentDepositProxy;
import com.nv.commons.utils.LogUtils;

public class PaymentDisplaySettingCache extends AbstractCache {

	private final ConcurrentHashMap<WebSiteType, Map<Long, PaymentDisplaySetting>> cache = new ConcurrentHashMap<>();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastUpdateTime = 0;

	private static final PaymentDisplaySettingCache instance = new PaymentDisplaySettingCache();

	public PaymentDisplaySettingCache() {

	}

	public static PaymentDisplaySettingCache getInstance() {
		return instance;
	}

	@Override
	protected void init() {

		try (Connection conn = DBPool.getReadConnection()) {

			long maxUpdateTime = 0;

			for (PaymentDisplaySetting displaySetting : PaymentDisplaySettingDAO.findAll(conn)) {
				if (!WebSiteType.checkWebsiteType(displaySetting.getWebsiteType())) {
					continue;
				}

				WebSiteType webSiteType = WebSiteType.getInstance(displaySetting.getWebsiteType());

				Map<Long, PaymentDisplaySetting> displaySettingMap = cache
					.computeIfAbsent(webSiteType, k -> new HashMap<>());

				displaySettingMap.put(displaySetting.getId(), displaySetting);

				maxUpdateTime = Math.max(maxUpdateTime, displaySetting.getUpdateTime().getTime());
			}

			lastUpdateTime = maxUpdateTime;

		} catch (Exception ex) {
			LogUtils.depositSetting.error("error while fetch display setting list", ex);
		}

	}


	@Override
	public void update() {
		try (Connection conn = DBPool.getReadConnection()) {

			Timestamp queryTimestamp = new Timestamp(lastUpdateTime);

			boolean isUpdated = false;
			long maxUpdateTime = queryTimestamp.getTime();

			for (PaymentDisplaySetting displaySetting : PaymentDisplaySettingDAO
				.findSettingByUpdateTime(conn, queryTimestamp)) {

				maxUpdateTime = Math.max(maxUpdateTime, displaySetting.getUpdateTime().getTime());

				WebSiteType webSiteType = WebSiteType.getInstance(displaySetting.getWebsiteType());

				Map<Long, PaymentDisplaySetting> displaySettingMap = cache
					.computeIfAbsent(webSiteType, k -> new HashMap<>());

				PaymentDisplaySetting displaySettingInCache = displaySettingMap.get(displaySetting.getId());

				if (displaySettingInCache == null) {

					isUpdated = true;

					displaySettingMap.put(displaySetting.getId(), displaySetting);

					continue;
				}

				if (displaySettingInCache.getUpdateTime().getTime() != displaySetting.getUpdateTime().getTime()) {
					displaySettingInCache.setBankType(displaySetting.getBankType());
					displaySettingInCache.setPaymentMethod(displaySetting.getPaymentMethod());
					displaySettingInCache.setPriority(displaySetting.getPriority());
					displaySettingInCache.setStatus(displaySetting.getStatus());
					displaySettingInCache.setManualInput(displaySetting.getManualInput());
					displaySettingInCache.setDisplay(displaySetting.getDisplay());
					displaySettingInCache.setRecommend(displaySetting.getRecommend());
					displaySettingInCache.setMinAmount(displaySetting.getMinAmount());
					displaySettingInCache.setMaxAmount(displaySetting.getMaxAmount());
					displaySettingInCache.setIntegerAmountJson(displaySetting.getIntegerAmountJson());
					displaySettingInCache.setNoteJson(displaySetting.getNoteJson());
					displaySettingInCache.setDecimalAmount(displaySetting.getDecimalAmount());
					displaySettingInCache.setRandom(displaySetting.isRandom());
					displaySettingInCache.setUpdateTime(displaySetting.getUpdateTime());
					displaySettingInCache.setDisplayName(displaySetting.getDisplayName());

					isUpdated = true;
				}

			}

			if (isUpdated) {
				lastUpdateTime = maxUpdateTime - ERROR_VALUE;
			} else {
				lastUpdateTime = maxUpdateTime;
			}

		} catch (Exception ex) {
			LogUtils.depositSetting.error("update display setting cache error. ", ex);
		}
		LogUtils.depositSetting.info("update display setting cache finish");

	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public String getCacheInfo() {
		return toJsonString(cache.values());
	}

	public String makeDepositForUseWebServer(PGServerInfo pgServerInfo, MoneyTransaction moneyTransaction,
		Account account, LangMessage lang) {

		PaymentType paymentType = PaymentType.getInstanceOf(moneyTransaction.getToPaymentType());
		PGAccount pgAccount = PaymentGatewayCache.getInstance()
			.getPGAccount(Integer.parseInt(moneyTransaction.getToBankAccount()));

		if (paymentType == null || pgAccount == null) {
			LogUtils.paymentGateway.info("paymentType or pgAccount is null");
			throw new Deviation("msg.error.validation.paymentGatewayPermissionInvalid");
		}

		boolean isCreateSuccess = false;
		// Do makeDeposit
		try {
			PaymentDepositProxy pgProxy = PaymentGatewayCache.getInstance().getProxy(pgAccount.getCompanyId());

			moneyTransaction.setToBankBranch(String.valueOf(pgAccount.getCompanyId()));

			if (MoneyTransactionBO.createDeposit(moneyTransaction)) {
				isCreateSuccess = true;

				PGDeposit pgDeposit = pgProxy.getPGDeposit(pgServerInfo, moneyTransaction, pgAccount, account, lang);

				return pgProxy.makePGDeposit(pgDeposit);
			}

		} catch (Deviation d) {
			if (isCreateSuccess) {
				try {
					moneyTransaction.setApprovedUserid("SYS");
					moneyTransaction.setApprovedNote("auto rejected - " + lang.get(d.getMessage()));
					MoneyTransactionBO.rejectDeposit(lang, "SYS", moneyTransaction);
				} catch (Exception e) {
					LogUtils.paymentGateway.error(e.getMessage(), e);
				}
			}

			throw new Deviation(lang.get("global.text.pleaseContactCustomerService"));
		} catch (Exception e) {
			LogUtils.paymentGateway.error(e.getMessage(), e);
		}

		if (isCreateSuccess) {
			try {
				moneyTransaction.setApprovedUserid("SYS");
				moneyTransaction.setApprovedNote("auto rejected - API Failed");
				MoneyTransactionBO.rejectDeposit(lang, "SYS", moneyTransaction);
			} catch (Exception e) {
				LogUtils.paymentGateway.error(e.getMessage(), e);
			}
		}

		LogUtils.paymentGateway.info("pgProxy .makeDeposit error, CompanyId = {}, PaymentTypeId = {}",
			pgAccount.getCompanyId(), paymentType.unique());

		throw new Deviation(lang.get("global.text.pleaseContactCustomerService"));
	}

}
