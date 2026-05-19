package com.nv.commons.cache;

import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.PGMethodStatusType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dao.PGAccountDAO;
import com.nv.commons.dao.PGCompanyDAO;
import com.nv.commons.dao.PGMethodDAO;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.dto.PGCompany;
import com.nv.commons.dto.PGMethod;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.paymentGateway.proxy.PaymentGatewayProxy;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.nv.commons.utils.JSONUtils.toJsonString;

/**
 * Title: com.nv.commons.cache.PaymentGatewayCache<br>
 * Description: 線上支付通道cache
 *
 */
public class PaymentGatewayCache extends AbstractCache {

	// key : company id
	private final ConcurrentHashMap<Integer, PaymentGatewayProxy> proxyCache = new ConcurrentHashMap<>();

	// key : account id
	private final ConcurrentHashMap<Integer, PGAccount> pgAccountCache = new ConcurrentHashMap<>();

	// ConcurrentHashMap key : currencyId , Map key : pgCompanyId
	private final ConcurrentHashMap<Integer, Map<Integer, Set<PaymentType>>> effectivePaymentTypeCache = new ConcurrentHashMap<>();

	// ConcurrentHashMap key : currencyId , Map key : pgCompanyId , Map key : paymentTypeId
	private final ConcurrentHashMap<Integer, Map<Integer, Map<Integer, Set<Bank>>>> effectivePaymentBankCache = new ConcurrentHashMap<>();

	private static final PaymentGatewayCache instance = new PaymentGatewayCache();

	// 誤差值
	private static final long ERROR_VALUE = 1000;

	private long lastPGCompanyUpdateTime = 0;
	private long lastPGMethodUpdateTime = 0;
	private long lastPGAccountUpdateTime = 0;

	private PaymentGatewayCache() {
	}

	public static PaymentGatewayCache getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 */
	@Override
	protected void init() {

		try {
			// PaymentGatewayProxy
			setCacheCompanyList(proxyCache);

			long maxPGMethodUpdateTime = 0;

			List<PGMethod> pgMethods;
			try (Connection conn = DBPool.getReadConnection()) {

				pgMethods = PGMethodDAO.findAll(conn);
			}

			for (PGMethod aPGMethod : pgMethods) {
				try {
					PaymentGatewayProxy pgProxy = proxyCache.get(aPGMethod.getCompanyId());
					pgProxy.addMethod(aPGMethod);

					maxPGMethodUpdateTime = Math.max(maxPGMethodUpdateTime, aPGMethod.getUpdateTime().getTime());
				} catch (Exception ignored) {
				}
			}

			lastPGMethodUpdateTime = maxPGMethodUpdateTime;

			// init effectivePaymentType
			effectivePaymentType(pgMethods);

			// PGAccount
			long maxPGAccountUpdateTime = 0;

			List<PGAccount> all;
			try (Connection conn = DBPool.getReadConnection()) {

				all = PGAccountDAO.findAll(conn);
			}

			for (PGAccount aPGAccount : all) {

				pgAccountCache.put(aPGAccount.getId(), aPGAccount);

				maxPGAccountUpdateTime = Math.max(maxPGAccountUpdateTime, aPGAccount.getUpdateTime().getTime());
			}

			lastPGAccountUpdateTime = maxPGAccountUpdateTime;

		} catch (Exception ex) {
			LogUtils.paymentGateway.error("error while fetch PGCompany list", ex);
		}

	}


	public <E> E getProxy(int proxyId) {
		return (E) proxyCache.get(proxyId);
	}

	public PGAccount getPGAccount(int accountId) {
		PGAccount pgAccount = pgAccountCache.get(accountId);
		if (pgAccount == null) {
			try (Connection conn = DBPool.getReadConnection()) {
				pgAccount = PGAccountDAO.findAccountByID(conn, accountId, DBQueryType.UNLOCK);
				if (pgAccount != null) {
					addPGAccount(pgAccount);
				}
			} catch (Exception ex) {
				LogUtils.paymentGateway.error("update pgAccount cache error. ", ex);
			}
		}
		return pgAccount;
	}


	public void addPGAccount(PGAccount pgAccount) {
		pgAccountCache.put(pgAccount.getId(), pgAccount);
	}

	@Override
	public void update() {
		long currentTime = System.currentTimeMillis();

		try (Connection conn = DBPool.getReadConnection()) {

			Timestamp queryTimestamp = new Timestamp(lastPGCompanyUpdateTime);

			boolean isPGCompanyUpdated = false;
			long maxPGCompanyUpdateTime = queryTimestamp.getTime();

			for (PGCompany aPGCompany : PGCompanyDAO.findPGCompanyByUpdateTime(conn, queryTimestamp)) {

				maxPGCompanyUpdateTime = Math.max(maxPGCompanyUpdateTime, aPGCompany.getUpdateTime().getTime());

				PaymentGatewayProxy proxyInCache = proxyCache.get(aPGCompany.getId());

				if (proxyInCache == null) {

					try {
						String proxyClassName = SystemConstants.PG_PROXY_FOLDER_PATH + aPGCompany.getClassName();
						PaymentGatewayProxy pgProxy = (PaymentGatewayProxy) Class.forName(proxyClassName).getDeclaredConstructor()
							.newInstance();
						pgProxy.setCompanyInfo(aPGCompany);

						proxyCache.put(aPGCompany.getId(), pgProxy);

					} catch (Throwable e) {
						LogUtils.paymentGateway.error("Payment Gateway Proxy initialize error. company={}",
							aPGCompany.getName(), e);
					}

					isPGCompanyUpdated = true;

				} else {
					PGCompany companyInCache = proxyInCache.getCompanyInfo();

					if (companyInCache.getUpdateTime().getTime() != aPGCompany.getUpdateTime().getTime()) {
						companyInCache.setName(aPGCompany.getName());
						companyInCache.setClassName(aPGCompany.getClassName());
						companyInCache.setApiURL(aPGCompany.getApiURL());
						companyInCache.setAsyncURL(aPGCompany.getAsyncURL());
						companyInCache.setSyncURL(aPGCompany.getSyncURL());
						companyInCache.setStatus(aPGCompany.getStatus());
						companyInCache.setUpdateTime(aPGCompany.getUpdateTime());

						companyInCache.setSupportDeviceType(aPGCompany.getSupportDeviceType());
						companyInCache.setPassAPIServer(aPGCompany.getPassAPIServer());

						isPGCompanyUpdated = true;
					}
				}
			}

			queryTimestamp = new Timestamp(lastPGMethodUpdateTime);

			boolean isPGMethodUpdated = false;
			long maxPGMethodUpdateTime = queryTimestamp.getTime();

			List<PGMethod> pgMethods = PGMethodDAO.findPGMethodByUpdateTime(conn, queryTimestamp);
			for (PGMethod aPGMethod : pgMethods) {
				try {
					maxPGMethodUpdateTime = Math.max(maxPGMethodUpdateTime, aPGMethod.getUpdateTime().getTime());

					PaymentGatewayProxy proxyInCache = proxyCache.get(aPGMethod.getCompanyId());
					PGMethod methodInCache = proxyInCache.getMethod(aPGMethod);

					if (methodInCache == null) {

						proxyInCache.addMethod(aPGMethod);

						isPGMethodUpdated = true;

						continue;
					}

					if (methodInCache.getUpdateTime().getTime() != aPGMethod.getUpdateTime().getTime()) {
						methodInCache.setStatus(aPGMethod.getStatus());
						methodInCache.setCurrencyTypeId(aPGMethod.getCurrencyTypeId());
						methodInCache.setCode(aPGMethod.getCode());
						methodInCache.setUpdateTime(aPGMethod.getUpdateTime());
						methodInCache.setPaymentType(aPGMethod.getPaymentType());
						isPGMethodUpdated = true;
					}
				} catch (Exception e) {
					LogUtils.paymentGateway.error("update methodInCache cache error, company id = {}, bank id = {}",
						aPGMethod.getCompanyId(), aPGMethod.getBankId(), e);
				}
			}

			// update effectivePaymentType
			updateEffectivePaymentType(pgMethods);

			queryTimestamp = new Timestamp(lastPGAccountUpdateTime);

			boolean isPGAccountUpdated = false;
			long maxPGAccountUpdateTime = queryTimestamp.getTime();

			for (PGAccount aPGAccount : PGAccountDAO.findPGAccountByUpdateTime(conn, queryTimestamp)) {

				try {
					PGAccount accountInCache = pgAccountCache.putIfAbsent(aPGAccount.getId(), aPGAccount);

					maxPGAccountUpdateTime = Math.max(maxPGAccountUpdateTime, aPGAccount.getUpdateTime().getTime());

					if (accountInCache == null) {

						isPGAccountUpdated = true;

						continue;
					}

					if (accountInCache.getUpdateTime().getTime() != aPGAccount.getUpdateTime().getTime()) {
						accountInCache.setCompanyId(aPGAccount.getCompanyId());
						accountInCache.setDisplayName(aPGAccount.getDisplayName());
						accountInCache.setPaymentMethod(aPGAccount.getPaymentMethod());
						accountInCache.setTransactionLimit(aPGAccount.getTransactionLimit());
						accountInCache.setMerchantId(aPGAccount.getMerchantId());
						accountInCache.setEncryptionPrivateKey(aPGAccount.getEncryptionPrivateKey());
						accountInCache.setEncryptionPublicKey(aPGAccount.getEncryptionPublicKey());
						accountInCache.setStatus(aPGAccount.getStatus());
						accountInCache.setRemark(aPGAccount.getRemark());
						accountInCache.setCurrentAmount(aPGAccount.getCurrentAmount());
						accountInCache.setWebshopApiUrl(aPGAccount.getWebshopApiUrl());
						accountInCache.setPendingTimeLimit(aPGAccount.getPendingTimeLimit());
						accountInCache.setPaymentMethodSetting(aPGAccount.getPaymentMethodSetting());
						accountInCache.setExtraData(aPGAccount.getExtraData());
						accountInCache.setCurrencyTypeId(aPGAccount.getCurrencyTypeId());
						accountInCache.setUpdateTime(aPGAccount.getUpdateTime());
						accountInCache.setStartTime(aPGAccount.getStartTime());
						accountInCache.setEndTime(aPGAccount.getEndTime());
						accountInCache.setPurpose(aPGAccount.getPurpose());
						isPGAccountUpdated = true;
					}
				} catch (Exception e) {
					LogUtils.paymentGateway.error("update accountInCache cache error, PG account id = {}",
						aPGAccount.getId(), e);
				}
			}

			if (isPGCompanyUpdated) {
				lastPGCompanyUpdateTime = maxPGCompanyUpdateTime - ERROR_VALUE;
			} else {
				lastPGCompanyUpdateTime = maxPGCompanyUpdateTime;
			}

			if (isPGMethodUpdated) {
				lastPGMethodUpdateTime = maxPGMethodUpdateTime - ERROR_VALUE;
			} else {
				lastPGMethodUpdateTime = maxPGMethodUpdateTime;
			}

			if (isPGAccountUpdated) {
				lastPGAccountUpdateTime = maxPGAccountUpdateTime - ERROR_VALUE;
			} else {
				lastPGAccountUpdateTime = maxPGAccountUpdateTime;
			}

		} catch (Exception ex) {
			LogUtils.paymentGateway.error("update payment gateway cache error. ", ex);
		}
		LogUtils.paymentGateway.info("update payment gateway cache.");

		long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

		if (duration > 1) {
			LogUtils.SYS.info("MessageTemplateCache init Use {} secs ", duration);
		}
	}

	private void setCacheCompanyList(ConcurrentHashMap<Integer, PaymentGatewayProxy> tempProxyCache)
		throws SQLException {

		long maxPGCompanyUpdateTime = 0;

		List<PGCompany> all;
		try (Connection conn = DBPool.getReadConnection()) {

			all = PGCompanyDAO.findAll(conn);
		}

		for (PGCompany aPGCompany : all) {
			try {
				String proxyClassName = SystemConstants.PG_PROXY_FOLDER_PATH + aPGCompany.getClassName();
				PaymentGatewayProxy pgProxy = (PaymentGatewayProxy) Class.forName(proxyClassName).getDeclaredConstructor()
					.newInstance();
				pgProxy.setCompanyInfo(aPGCompany);
				tempProxyCache.put(aPGCompany.getId(), pgProxy);

				maxPGCompanyUpdateTime = Math.max(maxPGCompanyUpdateTime, aPGCompany.getUpdateTime().getTime());

			} catch (ClassNotFoundException e) {
				LogUtils.paymentGateway.error("{}: Payment Gateway Proxy initialize error. company={}", e.getMessage(),
					aPGCompany.getName());
			} catch (Throwable e) {
				LogUtils.paymentGateway.error("Payment Gateway Proxy initialize error. company={}",
					aPGCompany.getName(), e);
			}
		}

		lastPGCompanyUpdateTime = maxPGCompanyUpdateTime;
	}

	@Override
	public void refresh() {
		init();
		LogUtils.SYS.info("refresh payment gateway cache.");
	}

	@Override
	public String getCacheInfo() {
		return toJsonString(proxyCache.values()) + toJsonString(pgAccountCache.values());
	}

	/**
	 * 針對支付商 pgMethod 設定的 Code, Payment_Type 整理出該支付商可使用的 paymentType。
	 *
	 */
	public void effectivePaymentType(List<PGMethod> pgMethods) {
		for (PGMethod aPGMethod : pgMethods) {
			try {
				// method 關閉 or 找不到 company
				if (aPGMethod.getStatus() != PGMethodStatusType.ACTIVE.unique()
					|| proxyCache.get(aPGMethod.getCompanyId()) == null) {
					continue;
				}
				// 確認 bank
				Bank bank = BankCache.getInstance().getBank(aPGMethod.getBankId());
				if (bank == null) {
					continue;
				}
				// 確認payment type
				PaymentType paymentType = PaymentType.getInstanceOf(aPGMethod.getPaymentType());
				if (paymentType == null) {
					continue;
				}
				addEffectivePaymentBankCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(),
					paymentType.unique(), bank);
				addEffectivePaymentTypeCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(), paymentType);

			} catch (Exception e) { // 避免發生錯誤導致迴圈中斷
				LogUtils.paymentGateway.error(
					"Payment Gateway effectivePaymentType initialize error. company={}, bank={}",
					aPGMethod.getCompanyId(), aPGMethod.getBankId(), e);
			}
		}

	}

	public void updateEffectivePaymentType(List<PGMethod> pgMethods) {

		for (PGMethod aPGMethod : pgMethods) {

			try {
				// 確認 bank
				Bank bank = BankCache.getInstance().getBank(aPGMethod.getBankId());
				if (bank == null) {
					continue;
				}
				// 確認payment type
				PaymentType paymentType = PaymentType.getInstanceOf(aPGMethod.getPaymentType());
				if (paymentType == null) {
					continue;
				}

				if (aPGMethod.getStatus() != PGMethodStatusType.ACTIVE.unique()) {
					removeEffectiveBankCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(),
						paymentType.unique(), bank);
					removeEffectivePaymentTypeCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(),
						paymentType);
				} else {
					addEffectivePaymentBankCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(),
						paymentType.unique(), bank);
					addEffectivePaymentTypeCache(aPGMethod.getCurrencyTypeId(), aPGMethod.getCompanyId(), paymentType);
				}
			} catch (Exception e) {
				LogUtils.paymentGateway.error(
					"Payment Gateway updateEffectivePaymentType Update error. company={}, bank={}",
					aPGMethod.getCompanyId(), aPGMethod.getBankId(), e);
			}

		}

	}

	/**
	 * Get available Company PaymentType List
	 *
	 *
	 */
	public Set<PaymentType> getCompanyPaymentType(int currencyId, int companyId) {
		Map<Integer, Set<PaymentType>> paymentMap = effectivePaymentTypeCache.get(currencyId);
		if (paymentMap == null || paymentMap.isEmpty()) {
			return Collections.emptySet();
		}
		return paymentMap.get(companyId);
	}

	/**
	 * Get available Company Online Bank List
	 *
	 */
	public Set<Bank> getPaymentBank(int currencyId, int companyId, int paymentTypeId) {

		Set<Bank> currencyBankList = effectivePaymentBankCache
			.computeIfAbsent(currencyId, k -> new HashMap<>())
			.computeIfAbsent(companyId, k -> new HashMap<>())
			.get(paymentTypeId);

		return currencyBankList == null ? Collections.emptySet() : currencyBankList;
	}

	/**
	 * Add EffectiveBankCache
	 *
	 *
	 */
	private void addEffectivePaymentBankCache(int currencyId, int companyId, int paymentTypeId, Bank bank) {
		// { currencyId, { pgCompanyId, { paymentTypeId, { Bank in TreeSet } } } }
		effectivePaymentBankCache
			.computeIfAbsent(currencyId, k -> new HashMap<>())
			.computeIfAbsent(companyId, k -> new HashMap<>())
			.computeIfAbsent(paymentTypeId, k -> new TreeSet<>())
			.add(bank);
	}

	/**
	 * Add EffectivePaymentTypeCache
	 *
	 */
	private void addEffectivePaymentTypeCache(int currencyId, int companyId, PaymentType paymentType) {
		// { currencyId, { pgCompanyId, { paymentType in TreeSet } } }
		effectivePaymentTypeCache
			.computeIfAbsent(currencyId, k -> new HashMap<>())
			.computeIfAbsent(companyId, k -> new TreeSet<>())
			.add(paymentType);
	}

	/**
	 * remove EffectiveBankCache
	 *
	 */
	private void removeEffectiveBankCache(int currencyId, int companyId, int paymentTypeId, Bank bank) {
		if (!getPaymentBank(currencyId, companyId, paymentTypeId).isEmpty()) {
			effectivePaymentBankCache.get(currencyId).get(companyId).get(paymentTypeId).remove(bank);
		}
	}

	/**
	 * remove EffectivePaymentTypeCache
	 *
	 */
	private void removeEffectivePaymentTypeCache(int currencyId, int companyId, PaymentType paymentType) {

		boolean remove = getPaymentBank(currencyId, companyId, paymentType.unique()).isEmpty() &&
			!getCompanyPaymentType(currencyId, companyId).isEmpty();

		if (remove) {
			effectivePaymentTypeCache.get(currencyId).get(companyId).remove(paymentType);
		}
	}

}
