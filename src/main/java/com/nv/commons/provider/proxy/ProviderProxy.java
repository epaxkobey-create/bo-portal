package com.nv.commons.provider.proxy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nv.commons.bo.AccountProviderBO;
import com.nv.commons.bo.GameBO;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.key.AccountProviderCacheKey;
import com.nv.commons.cache.key.AccountProviderKey;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.InternalErrorCodeType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.OddsType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountProviderDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.exceptions.InternalErrorException;
import com.nv.commons.exceptions.ProviderAPINoSupportException;
import com.nv.commons.exceptions.ProviderException;
import com.nv.commons.exceptions.ProviderTimeoutException;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.provider.dto.APIResponse;
import com.nv.commons.provider.dto.ConnectionInfo;
import com.nv.commons.provider.dto.ProviderGameRs;
import com.nv.commons.provider.service.ProviderService;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.module.okHttp.OkHttpClientManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ProviderProxy implements ProviderService {

	static final String messagePrefix = "[" + HostAddressUtils.getLocalIPAddress() + "][TransferFailCheck][Error]";
	private static final ThreadLocal<ConnectionInfo> connInfoThreadLocal = new ThreadLocal<>();
	protected final int providerId;
	protected final String systemCode;

	public int getProviderId() {
		return providerId;
	}

	public String getSystemCode() {
		return systemCode;
	}

	protected final Logger logger;
	protected List<String> parameterKeyList;
	protected final int PROVIDER_ACCOUNT_DEFAULT_LENGTH = 20;
	protected int HTTP_SOCKET_TIMEOUT;//使用預設值

	public ProviderProxy(int providerId, String systemCode) {
		this.providerId = providerId;
		this.systemCode = systemCode;
		this.logger = LogManager.getLogger("provider" + systemCode);
		this.parameterKeyList = new ArrayList<>();
		this.HTTP_SOCKET_TIMEOUT = getHttpSocketTimeout();
		//		this.httpRequestConfig = getRequestConfig();

		if (this.logger == null || !this.logger.getLevel().equals(Level.DEBUG)) {
			throw new RuntimeException("\r\n" + systemCode + "\tlogger setting error\r\n");
		}

	}

	/**
	 * 1. 轉換轉帳金額小數點後的格式位數 預設小數點後兩位
	 *
	 * @param amount 玩家傳入的金額
	 * @return 回傳實際轉入Provider的金額
	 * @throws Exception
	 */
	public BigDecimal getActualTransferAmount(BigDecimal amount) {
		String providerTransferScale = getConnectionInfo().get("providerTransferScale");
		int newScale = (providerTransferScale != null) ? Integer.parseInt(providerTransferScale) : 2;
		return amount.setScale(newScale, RoundingMode.DOWN);
	}

	/**
	 * 玩家餘額查詢
	 *
	 * @return BigDecimal
	 * @throws Exception
	 */
	public BigDecimal getBalance(AccountProvider accountProvider) throws Exception {
		BigDecimal balance = doGetBalance(accountProvider);
		accountProvider.setProviderBalance(balance);
		modifyProviderBalance(accountProvider);

		return accountProvider.getProviderBalance();
	}

	public void getBalance(AccountProvider... accountProviders) throws Exception {
		doGetBalance(accountProviders);

		for (AccountProvider accountProvider : accountProviders) {

			modifyProviderBalance(accountProvider);
		}
	}

	protected void modifyProviderBalance(AccountProvider accountProvider) {

		BigDecimal balance = accountProvider.getProviderBalance();

		BigDecimal exposure = accountProvider.getExposure();

		BigDecimal providerConversion = getProviderConversion();

		BigDecimal systemConversion = CurrencyType.getInstance(accountProvider.getCurrencyTypeId())
			.getSystemConversion();

		if (providerConversion.compareTo(systemConversion) != 0) {
			balance = balance.multiply(providerConversion).divide(systemConversion, 4, RoundingMode.DOWN);
			exposure = exposure.multiply(providerConversion).divide(systemConversion, 4, RoundingMode.HALF_UP);
		}
		accountProvider.setExposure(exposure);
		accountProvider.setProviderBalance(balance);

	}

	/**
	 * 更新有交易單玩家的balance
	 * for syncTaskV2使用 切換時 必須注意 原本 method : updateAccountProviderBalance 有proxy進行覆寫
	 *
	 * @param accountProviderSet
	 * @return
	 */
	public void syncAccountProviderBalance(Set<AccountProvider> accountProviderSet) {
		if (CollectionUtils.isEmpty(accountProviderSet)) {
			return;
		}

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			for (AccountProvider accountProvider : accountProviderSet) {
				try {

					this.getBalance(accountProvider);
					AccountProviderDAO.updateBalance(conn, accountProvider);

					conn.commit();

				} catch (Exception e) {

					DbUtils.rollback(conn);

					this.getLogger().error(
						"update balance fail, websiteType={}, providerId={}, userId={}, bonusTurnoverId={}, errorMessage={}",
						accountProvider.getWebsiteType(), accountProvider.getProviderId(), accountProvider.getUserId(),
						accountProvider.getBonusTurnoverId(),
						e.getMessage());

					this.getLogger().error(e.getMessage(), e);
				}
			}

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	/**
	 * 分群 thread 呼叫 API 更新餘額
	 *
	 * @param accountProviderSet
	 * @param group
	 */
	public void syncAccountProviderBalanceByGroup(Set<AccountProvider> accountProviderSet, int group) {
		if (CollectionUtils.isEmpty(accountProviderSet)) {
			return;
		}

		getLogger().debug("accountProviderSet size:{}", accountProviderSet.size());
		long startTime = System.currentTimeMillis();

		int sizePerGroup = (accountProviderSet.size() / group) + 1;

		List<List<AccountProvider>> keyGroupList = accountProviderSet.stream()
			.collect(com.nv.commons.utils.CollectionUtils.groupingBy(sizePerGroup));

		List<Runnable> runnableList = new ArrayList<>();

		for (List<AccountProvider> keyList : keyGroupList) {
			Runnable runnable = () -> {
				Connection conn = null;
				try {
					conn = DBPool.getWriteConnection();
					conn.setAutoCommit(false);

					for (AccountProvider accountProvider : keyList) {

						try {

							ProviderProxy proxy = ProviderProxyCache.getInstance()
								.getProviderProxy(WebSiteType.getInstance(accountProvider.getWebsiteType()),
									providerId, CurrencyType.getInstance(accountProvider.getCurrencyTypeId()));

							proxy.getBalance(accountProvider);
							AccountProviderDAO.updateBalance(conn, accountProvider);

							conn.commit();

						} catch (Exception e) {

							DbUtils.rollback(conn);

							this.getLogger().error(
								"update balance fail, websiteType={}, providerId={}, userId={}, bonusTurnoverId={}, errorMessage={}",
								accountProvider.getWebsiteType(), providerId, accountProvider.getUserId(),
								accountProvider.getBonusTurnoverId(), e.getMessage());

							this.getLogger().error(e.getMessage(), e);
						}

					}
				} catch (Exception e) {
					DbUtils.rollback(conn);
					getLogger().error(e.getMessage(), e);
				} finally {
					DbUtils.close(conn);
				}
			};

			runnableList.add(runnable);
		}

		GlobalThreadPool.await(runnableList, group);

		getLogger().debug("syncAccountProviderBalanceByGroup takes:{}", DateUtils.secondsElapsedSince(startTime));
	}

	/**
	 * 一次API更新多個玩家的餘額
	 *
	 * @param accountProviderSet
	 * @param count
	 */
	public void syncMultiAccountProviderBalance(Set<AccountProvider> accountProviderSet, int count) {
		if (CollectionUtils.isEmpty(accountProviderSet)) {
			return;
		}

		long startTime = System.currentTimeMillis();

		List<List<AccountProvider>> groupList = accountProviderSet.stream()
			.collect(com.nv.commons.utils.CollectionUtils.groupingBy(count));

		for (List<AccountProvider> subList : groupList) {
			Connection conn = null;
			try {
				conn = DBPool.getWriteConnection();
				conn.setAutoCommit(false);

				AccountProvider[] accountProviders = subList.toArray(new AccountProvider[0]);
				this.getBalance(accountProviders);

				for (AccountProvider accountProvider : accountProviders) {
					AccountProviderDAO.updateBalance(conn, accountProvider);
					conn.commit();
				}
			} catch (Exception e) {
				DbUtils.rollback(conn);
				this.getLogger().error(e.getMessage(), e);
			} finally {
				DbUtils.close(conn);
			}
		}

		getLogger().debug("updateAccountProviderBalance takes:{}", DateUtils.secondsElapsedSince(startTime));
	}

	/**
	 * 更新有交易單玩家的balance
	 *
	 * @param gameTxnList
	 * @return
	 */
	public void updateAccountProviderBalance(List<GameTransaction> gameTxnList) {
		if (CollectionUtils.isEmpty(gameTxnList)) {
			return;
		}

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			Map<AccountProviderCacheKey, Set<Integer>> uniqueAccountProviderCacheKeyWithProviderSet = gameTxnList
				.stream()
				.collect(Collectors.groupingBy(
					g -> new AccountProviderCacheKey(g.getWebsiteType(), g.getUserId()),
					Collectors.mapping(
						gameTransaction -> ProviderCache.getInstance().getByVendorId(gameTransaction.getVendorId())
							.getId(), Collectors.toSet())
				));

			for (Map.Entry<AccountProviderCacheKey, Set<Integer>> entry : uniqueAccountProviderCacheKeyWithProviderSet
				.entrySet()) {

				AccountProviderCacheKey accountProviderCacheKey = entry.getKey();
				Set<Integer> gameTransactionProviderSet = entry.getValue();

				for (int gameTransactionProvider : gameTransactionProviderSet) {
					try {
						AccountProviderKey key = new AccountProviderKey(accountProviderCacheKey.websiteType(),
							gameTransactionProvider, accountProviderCacheKey.userId());

						AccountProvider accountProvider = AccountProviderCache.getInstance().getAccountProvider(key);
						if (accountProvider == null) {
							this.getLogger().error("update balance fail, get accountProvider is null, "
									+ "websiteType={}, providerId={}, userId={}",
								accountProviderCacheKey.websiteType(), gameTransactionProvider,
								accountProviderCacheKey.userId());
						}

						this.getBalance(accountProvider);
						AccountProviderDAO.updateBalance(conn, accountProvider);

						conn.commit();

					} catch (Exception e) {

						DbUtils.rollback(conn);

						this.getLogger().error(
							"update balance fail, websiteType={}, providerId={}, userId={}, errorMessage={}",
							accountProviderCacheKey.websiteType(), gameTransactionProvider,
							accountProviderCacheKey.userId(),
							e.getMessage());

						this.getLogger().error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			DbUtils.close(conn);
		}
	}

	public void updateAccountProviderBalance(List<GameTransaction> gameTxnList, int group) {
		if (CollectionUtils.isEmpty(gameTxnList)) {
			return;
		}

		getLogger().debug("gameTxnList size:{}", gameTxnList.size());
		long startTime = System.currentTimeMillis();

		Map<AccountProviderCacheKey, Set<Integer>> accountProviderCacheKeyMap = gameTxnList
			.stream()
			.collect(Collectors.groupingBy(
				g -> new AccountProviderCacheKey(g.getWebsiteType(), g.getUserId()),
				Collectors.mapping(
					gameTransaction -> ProviderCache.getInstance().getByVendorId(gameTransaction.getVendorId()).getId(),
					Collectors.toSet())
			));

		Set<AccountProviderCacheKey> keySet = accountProviderCacheKeyMap.keySet();

		int sizePerGroup = (keySet.size() / group) + 1;

		List<List<AccountProviderCacheKey>> keyGroupList = keySet.stream()
			.collect(com.nv.commons.utils.CollectionUtils.groupingBy(sizePerGroup));

		List<Runnable> runnableList = new ArrayList<>();

		for (List<AccountProviderCacheKey> keyList : keyGroupList) {
			Runnable runnable = () -> {
				Connection conn = null;
				try {
					conn = DBPool.getWriteConnection();
					conn.setAutoCommit(false);

					for (AccountProviderCacheKey accountProviderCacheKey : keyList) {
						Set<Integer> providerIdSet = accountProviderCacheKeyMap.get(accountProviderCacheKey);

						for (int providerId : providerIdSet) {
							try {
								AccountProviderKey key = new AccountProviderKey(
									accountProviderCacheKey.websiteType(),
									providerId, accountProviderCacheKey.userId());

								AccountProvider accountProvider = AccountProviderCache.getInstance()
									.getAccountProvider(key);

								ProviderProxy proxy = ProviderProxyCache.getInstance()
									.getProviderProxy(WebSiteType.getInstance(accountProvider.getWebsiteType()),
										providerId, CurrencyType.getInstance(accountProvider.getCurrencyTypeId()));

								proxy.getBalance(accountProvider);
								AccountProviderDAO.updateBalance(conn, accountProvider);

								conn.commit();

							} catch (Exception e) {

								DbUtils.rollback(conn);

								this.getLogger().error(
									"update balance fail, websiteType={}, providerId={}, userId={}, errorMessage={}",
									accountProviderCacheKey.websiteType(), providerId,
									accountProviderCacheKey.userId(),
									e.getMessage());

								this.getLogger().error(e.getMessage(), e);
							}
						}
					}
				} catch (Exception e) {
					DbUtils.rollback(conn);
					getLogger().error(e.getMessage(), e);
				} finally {
					DbUtils.close(conn);
				}
			};

			runnableList.add(runnable);
		}

		GlobalThreadPool.await(runnableList, group);

		getLogger().debug("updateAccountProviderBalance takes:{}", DateUtils.secondsElapsedSince(startTime));
	}

	/**
	 * 取得餘額
	 *
	 * @param accountProvider
	 * @throws Exception
	 */
	protected abstract BigDecimal doGetBalance(AccountProvider accountProvider) throws Exception;

	/**
	 * 取得餘額
	 *
	 * @param accountProviders
	 * @throws Exception
	 */
	protected void doGetBalance(AccountProvider[] accountProviders) throws Exception {
		throw new Deviation("API Not support.");
	}

	public String getProviderBOAccount(AccountProvider accountProvider) throws Exception {

		final String[] boNameFormat = {null};

		Optional.ofNullable(getConnectionInfo()).ifPresent(connectionInfo ->
			boNameFormat[0] = connectionInfo.get("boNameFormat"));

		final String providerAccount = accountProvider.getProviderAccount();

		return StringUtils.isBlank(boNameFormat[0]) ? providerAccount :
			boNameFormat[0].replace("($)", providerAccount);
	}

	/*
	public boolean updateAccountProviderBalanceByAccountProviders(int providerAgentId, int currencyTypeId,
		List<AccountProvider> accountProviders) {
		boolean isSuccess = true;
		try {
			ProviderAgent providerAgent = ProviderAgentCache.getInstance().get(providerAgentId);
			TaskConnectionInfo taskConnectionInfo = ProviderProxyCache.getInstance()
				.getTaskConnectionInfo(providerAgent, CurrencyType.getInstance(currencyTypeId));
			setConnectionInfo(taskConnectionInfo);

			for (AccountProvider accountProvider : accountProviders) {
				try {
					this.getBalance(accountProvider);
					AccountProviderBO.updateBalance(accountProvider);
				} catch (Exception e) {
					this.getLogger().error(
						"update balance fail, websiteType={}, providerId={}, userId={}, bonusTurnoverId={}, errorMessage={}",
						accountProvider.getWebsiteType(), providerId, accountProvider.getUserId(),
						accountProvider.getBonusTurnoverId(), e.getMessage());
				}
			}

		} catch (Exception e) {
			this.getLogger().error(e.getMessage(), e);
			isSuccess = false;
		}

		return isSuccess;
	}
	 */

//	public boolean asyncTransferWhenLogin() {
//		return true;
//	}

	/**
	 * 建立主錢包帳號
	 *
	 * @param account
	 * @return
	 * @throws Exception
	 */
	public AccountProvider createAccountProvider(Account account) throws Exception {

		if (AccountProviderBO.exists(account.getWebsiteType(), providerId, account.getUserId())) {
			throw new Exception(" Already been created in AccountProvider, userId:" + account.getUserId()
				+ ", providerId:" + providerId);
		}

		if (getConnectionInfo() == null) {
			Provider provider = ProviderCache.getInstance().getProvider(providerId);
			ConnectionInfo connectionInfo = ProviderProxyCache.getInstance()
				.getConnectionInfo(WebSiteType.getInstance(account.getWebsiteType()),
					provider, CurrencyType.getInstance(account.getCurrencyTypeId()));
			setConnectionInfo(connectionInfo);
		}

		AccountProvider accountProvider = createAccount(account);

		AccountProviderBO.insert(accountProvider);

		return accountProvider;
	}

	public String gameLoginGameUrl(AccountProvider accountProvider, Game game, PlatformType platformType,
		LanguageType languageType,
		String userRealIP, String requestURL) throws Exception {
		try {
			String gameUrl;
			if (accountProvider == null) {
				gameUrl = getViewGameUrl(game, platformType, userRealIP, requestURL, languageType);

			} else {
				gameUrl = getGameLoginUrl(accountProvider, game, platformType, userRealIP, languageType);
			}

			if (game.getGameType() == GameType.Sport.unique() && !gameUrl.startsWith("/")) {
				URI uri = URI.create(gameUrl);
				if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
					gameUrl = "https://" + gameUrl;
				} else if ("http".equals(uri.getScheme())) {
					gameUrl = gameUrl.replace("http", "https");
				}
			}

			return gameUrl;

		} catch (Deviation d) {
			throw d;
		} catch (Exception e) {

			if (e instanceof NullPointerException) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
			throw new InternalErrorException(InternalErrorCodeType.GET_PLAY_GAME_URL_ERROR,
				e.getMessage(), e);
		}
	}

	/**
	 * 取得遊戲平台回覆的登入url資訊
	 *
	 * @param game
	 * @param platformType
	 * @param languageType
	 * @return
	 */
	public abstract String getGameLoginUrl(AccountProvider accountProvider, Game game,
		PlatformType platformType, String loginIP, LanguageType languageType)
		throws Exception;

	/**
	 * 取得遊戲平台回覆的登入url資訊 - 試玩
	 *
	 * @param game
	 * @param platformType
	 * @param languageType
	 * @return
	 */
	public abstract String getViewGameUrl(Game game, PlatformType platformType, String fromIp,
		String requestURL, LanguageType languageType) throws Exception;

	/**
	 * 取得遊戲交易資料
	 *
	 * @param startDate
	 * @param endDate
	 * @return APIResponse
	 * @throws Exception
	 */
	public abstract APIResponse getGameTransaction(Date startDate, Date endDate) throws Exception;

	/**
	 * 更新帳戶資訊，若有此需求的Provider需Override此方法
	 *
	 * @param accountProvider
	 * @throws Exception
	 */
	public boolean updateAccount(AccountProvider accountProvider, String oldPassword) throws Exception {
		throw new UnsupportedOperationException("Not support!");
	}

	public String getGameTxnResultUrl(GameTransaction gameTxn) throws Exception {
		return StringUtils.EMPTY;
	}

	/**
	 * 取得遊戲平台回覆的遊戲列表
	 *
	 * @return
	 */
	public ProviderGameRs getGameList(Vendor vendor) throws Exception {
		throw new Exception("Not Support!");
	}

	public boolean hasGameList(GameType gameType)
	{
		//預設判斷不使用game list的類型, 不同實作需Override此方法
		if(gameType.isSport() || gameType.isESport() || gameType.isLottery() || gameType.isCockFighting())
			return false;
		else
			return true;
	}

	/**
	 * 可以在admin新增遊戲各自實作
	 *
	 * @param vendor
	 * @param gameData
	 * @return
	 * @throws Exception
	 */
	public Game getInsertGame(Vendor vendor, String gameData) throws Exception {
		throw new Exception("Not Support!");
	}

	public ConnectionInfo getConnectionInfo() {
		return connInfoThreadLocal.get();
	}

	void setConnectionInfo(int website, int currency) {

		WebSiteType webSiteType = WebSiteType.getInstance(website);

		Provider provider = ProviderCache.getInstance().getProvider(providerId);

		CurrencyType currencyType = CurrencyType.getInstance(currency);

		ConnectionInfo connectionInfo = ProviderProxyCache.getInstance()
			.getConnectionInfo(webSiteType, provider, currencyType);

		this.setConnectionInfo(connectionInfo);
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		connInfoThreadLocal.set(connectionInfo);
	}

	public final Logger getLogger() {
		return logger;
	}

	/**
	 * 遊戲平台的回傳代碼
	 *
	 * @return String
	 */
	protected abstract String getStatusCode(APIResponse response);

	/**
	 * 遊戲平台的回傳錯誤訊息
	 *
	 * @return String
	 */
	protected abstract String getStatusDescription(APIResponse response);

	/**
	 * 平台開戶
	 *
	 * @param account
	 * @throws Exception
	 */
	protected abstract AccountProvider createAccount(Account account) throws Exception;

	/**
	 * 帳號是否重復
	 *
	 * @return boolean
	 */
	protected abstract boolean isAccountDuplicate(APIResponse response);

	protected abstract OddsType getOddsType(String oddsType);

	public abstract void updateGameTransactionFromProvider(WebSiteType webSiteType) throws Exception;

	/**
	 * @param account
	 * @return
	 */
	public AccountProvider buildAccountProvider(Account account) throws Exception {
		AccountProvider accountProvider = new AccountProvider();
		accountProvider.setUserId(account.getUserId());
		accountProvider.setWebsiteType(account.getWebsiteType());
		accountProvider.setProviderId(providerId);

		boolean isMainWallet = true;
		accountProvider.setProviderAccount(
			buildProviderAccount(account, isMainWallet));
		//		accountProvider.setProviderAccount(
		//			buildProviderAccount(account, bonusTurnoverId == BonusWalletType.MAIN.unique()));

		accountProvider.setProviderPassword(account.getPassword());
		accountProvider.setProviderBalance(BigDecimal.ZERO);
		accountProvider.setCurrencyTypeId(getConnectionInfo().getCurrencyType().unique());
		accountProvider.setProviderCreateTime(new Timestamp(System.currentTimeMillis()));
		accountProvider.setProviderUpdateTime(new Timestamp(System.currentTimeMillis()));
		return accountProvider;
	}

	/**
	 * providerAccount prefix 組成   website+環境+錢包
	 */
	protected String getProviderAccountDefaultPrefix(WebSiteType webSiteType, boolean isMainWallet) {
		String env;
//		if (SystemInfo.getInstance().isProduction()) {
//			env = "p";
//		} else
			if (SystemInfo.getInstance().isUat()) {
			env = "t";
		} else {
			env = "d";
		}

		return webSiteType.getShortName() + env + (isMainWallet ? "m" : "b");
	}

	protected String getProviderAccountWithLength(String prefix, int length) throws Exception {
		String providerAccountSeq = GameBO.getProviderAccountSeq(systemCode);

		int providerAccountSeqLength = length - prefix.length();

		return prefix + StringUtils.leftPad(providerAccountSeq, providerAccountSeqLength, "0");
	}

	protected String buildProviderAccount(Account account, boolean isMainWallet) throws Exception {

		String prefix = getProviderAccountDefaultPrefix(WebSiteType.getInstance(account.getWebsiteType()),
			isMainWallet);

		return getProviderAccountWithLength(prefix, PROVIDER_ACCOUNT_DEFAULT_LENGTH);
	}

	public BigDecimal getProviderConversion() {
		return BigDecimal.ONE;
	}

	/**
	 * providerAccount prefix 組成   website+環境+錢包
	 */

	protected int getHttpSocketTimeout() {
		return -1;
	}

	public boolean kickOutAllUser() throws Exception {
		throw new ProviderAPINoSupportException("not support");
	}

	public boolean kickOutUser(AccountProvider accountProvider) throws Exception {
		throw new ProviderAPINoSupportException("not support");
	}

	public boolean kickOutByGame(Game game) throws Exception {
		throw new ProviderAPINoSupportException("not support");
	}

	protected void setOkHttpConfig(OkHttpClientManager.BaseRequest okHttpBaseReq) {

		//		if (SquidProxySetting.enabled()) {
		//			okHttpBaseReq.setProxy(SquidProxySetting.ip(), SquidProxySetting.port());
		//		}
		if (HTTP_SOCKET_TIMEOUT != -1) {
			okHttpBaseReq.setTimeout(HTTP_SOCKET_TIMEOUT * 1000);
		}
	}

	protected String getOKHttpResponseSuccessContentWithInterceptLog(OkHttpClientManager.BaseRequest okHttpBaseReq)
		throws Exception {

		setOkHttpConfig(okHttpBaseReq);
		String result;
		int status;
		try {

			OkHttpClientManager.HTTPResponse httpResponse = okHttpBaseReq.execute();

			status = httpResponse.getStatusCode();
			result = httpResponse.getContent();

		} catch (java.net.UnknownHostException | java.net.SocketTimeoutException
				 | org.apache.http.conn.ConnectTimeoutException e) {
			getLogger().error(e.getMessage(), e);
			throw new ProviderTimeoutException(getLogger(), "", systemCode, StringUtils.EMPTY, e.getMessage());
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
			throw e;
		}

		if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
			throw new ProviderException(getLogger(), "Response status not success", systemCode, String.valueOf(status),
				result);
		}

		return result;

	}

//	protected OkHttpClientManager.HTTPResponse getHttpResponse(OkHttpClientManager.BaseRequest okHttpBaseReq)
//		throws Exception {
//
//		setOkHttpConfig(okHttpBaseReq);
//
//		return getOKHttpResponse(okHttpBaseReq);
//	}

//	protected OkHttpClientManager.HTTPResponse getOKHttpResponse(OkHttpClientManager.BaseRequest okHttpBaseReq)
//		throws Exception {
//
//		try {
//			return okHttpBaseReq.execute();
//		} catch (java.net.UnknownHostException | java.net.SocketTimeoutException |
//				 org.apache.http.conn.ConnectTimeoutException e) {
//			getLogger().error(e.getMessage(), e);
//			throw new ProviderTimeoutException(getLogger(), "", systemCode, StringUtils.EMPTY, e.getMessage());
//		} catch (Exception e) {
//			getLogger().error(e.getMessage(), e);
//			throw e;
//		}
//
//	}

	public List<Map.Entry<String, String>> getDefaultHeaders() throws Exception{
		return Collections.emptyList();
	}
}
