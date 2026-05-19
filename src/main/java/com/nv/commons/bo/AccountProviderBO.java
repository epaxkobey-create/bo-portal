package com.nv.commons.bo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.key.AccountProviderKey;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.InternalErrorCodeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountProviderDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.exceptions.InternalErrorException;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import jakarta.validation.constraints.NotNull;

/**
 * @author Luke Chi
 */
public class AccountProviderBO {


	public static void insert(AccountProvider accountProvider) throws SQLException {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountProviderDAO.insert(conn, accountProvider);
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static AccountProvider getAccountProvider(String userId, int websiteType, int providerId)
		throws SQLException {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountProviderDAO.getAccountProvider(conn, userId, websiteType, providerId);
		}
	}

	public static AccountProvider findAccountProviderByProviderAccount(int websiteType, int providerId,String providerAccount)
		throws SQLException {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountProviderDAO.getAccountProviderFromProviderAccount(conn, websiteType, providerId,providerAccount);
		}
	}

	public static List<AccountProvider> findLatestUpdate(Timestamp latestUpdateTime) {

		List<AccountProvider> userAccountProviderListInDb = Collections.emptyList();

		try (Connection conn = DBPool.getReadConnection()) {
			userAccountProviderListInDb = AccountProviderDAO.findLatestUpdate(conn, latestUpdateTime);
		} catch (Exception ex) {
			LogUtils.providerMonitor.error("error while update AccountProvider list", ex);
		}

		return userAccountProviderListInDb;
	}

	@NotNull
	public static List<AccountProvider> getAccountProviderList(int websiteTypeId, String userId) {

		try (Connection conn = DBPool.getReadConnection()) {
			return AccountProviderDAO.getAccountProviderList(conn, websiteTypeId, userId);
		} catch (Exception ex) {
			LogUtils.providerMonitor.error("error while update AccountProvider list", ex);
		}

		return Collections.emptyList();
	}

	public static List<AccountProvider> findByWebsitesAndUserIds(String[][] userKey) {

		List<AccountProvider> dataInDb = Collections.emptyList();

		try (Connection conn = DBPool.getReadConnection()) {
			dataInDb = AccountProviderDAO.findByWebsitesAndUserIds(conn, userKey);
		} catch (Exception ex) {
			LogUtils.providerMonitor.error("error while update AccountProvider list", ex);
		}

		return dataInDb;
	}

	public static boolean exists(int websiteType, int providerId, String userId)
		throws SQLException {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountProviderDAO.exists(conn, websiteType, providerId, userId);
		}
	}


	//	/**
	//	 * Note: account 可能是 playerInSession, 所以可以 lock
	//	 */
	public static AccountProvider createAccountProvider(Account account, int providerId) {
		try {
			WebSiteType webSiteType = WebSiteType.getInstance(account.getWebsiteType());

			CurrencyType currencyType = CurrencyType.getInstance(account.getCurrencyTypeId());

			ProviderProxy proxy = ProviderProxyCache.getInstance()
				.getProviderProxy(webSiteType, providerId, currencyType);

			AccountProviderKey accountProviderKey = new AccountProviderKey(webSiteType.unique(), providerId,
				account.getUserId());

			// handling UI double click, 避免建出多個重複的帳號
			synchronized (account) {

				if (AccountProviderCache.getInstance().getAccountProvider(accountProviderKey) == null) {

					AccountProvider accountProvider = proxy.createAccountProvider(account);

					if (accountProvider != null) {
						return accountProvider;
					}
				}
			}

			return AccountProviderCache.getInstance().getAccountProvider(accountProviderKey);

		} catch (Exception e) {
			throw new InternalErrorException(InternalErrorCodeType.CREATE_ACCOUNT_PROVIDER_ERROR, e.getMessage(),
				e);
		}
	}

	/**
	 *
	 */
	public static BigDecimal callGetBalanceAPI(AccountProvider accountProvider) {
		Connection conn = null;
		try {
			WebSiteType webSiteType = WebSiteType.getInstance(accountProvider.getWebsiteType());
			CurrencyType currencyType = CurrencyType.getInstance(accountProvider.getCurrencyTypeId());

			ProviderProxy proxy = ProviderProxyCache.getInstance()
				.getProviderProxy(webSiteType, accountProvider.getProviderId(), currencyType);

			proxy.getBalance(accountProvider);

			// MEMO: update to DB, 不要直接用 AccountProviderBO.updateBalance()
			// 因為別的地方有用到 AccountProviderBO.updateBalance() 但是不需要 AccountDAO.updateUpdatedAttribute()
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			/*
			 * 避免 logout 時 update account (clearServerId) -> accountProviderDao updateSessionToken 跟這邊互卡, 這邊
			 * update accountProvider 直接 commit
			 */
			AccountProviderDAO.updateBalance(conn, accountProvider);
			conn.commit();

			return accountProvider.getProviderBalance();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
		// in case of exception just return balance in cache
		return accountProvider.getProviderBalance();
	}

}
