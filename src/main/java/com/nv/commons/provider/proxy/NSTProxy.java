package com.nv.commons.provider.proxy;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nv.commons.bo.AccountProviderBO;
import com.nv.commons.bo.GameTransactionBO;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.OddsType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.GameTransaction;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.exceptions.ProviderAPINoSupportException;
import com.nv.commons.provider.dto.APIResponse;
import com.nv.commons.provider.intercept.ProxyInterceptor;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.gameprovider.nst.NSTProxyActionType;
import com.nv.module.gameprovider.nst.dto.NSTGetOnlineMemberRequest;
import com.nv.module.gameprovider.nst.dto.NSTGetOnlineMemberResponse;
import com.nv.module.gameprovider.nst.dto.NSTGetTicketByIdRequest;
import com.nv.module.gameprovider.nst.dto.NSTLogoutRequest;
import com.nv.module.gameprovider.nst.dto.NSTLogoutResponse;
import com.nv.module.gameprovider.nst.dto.NSTRegisterRequest;
import com.nv.module.gameprovider.nst.dto.NSTTicket;
import com.nv.module.okHttp.OkHttpClientManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

public class NSTProxy extends SingleWalletProxy {

	private static final int PROVIDER_ACCOUNT_LIMIT = 16;
	public static final int MAX_DELAY_MIN = 10;
	public static String STRING_COMPANY_KEY = "Company-Key";
	public static String STRING_API_PASSWORD = "Api-Password";
	public static String STRING_API_KEY = "Api-Key";
	public static String STRING_API_URL = "apiUrl";
	public static String STRING_AUTH_URL = "authUrl";

	public NSTProxy(int providerId, String systemCode) {
		super(providerId, systemCode);
	}

	protected int getHttpSocketTimeout() {

		if (ServerInfoUtils.isAPIServer()) {
			return MAX_DELAY_MIN;
		} else {
			return -1;
		}
	}

	//region 實作/複寫 Provider Proxy方法

	@Override
	public boolean hasGameList(GameType gameType) {
		return false;
	}

	/**
	 * NST Batch Logout API
	 * <p>Since NST don't have specifically batchLogout API,
	 * will loop memberIdList and pass each memberId to {@link #logout(String)} method.
	 *
	 * @return {@code true} Expected to return true,
	 * but when 1 failed logout will return false to indicate have failure.
	 */
	private boolean batchLogout(List<String> memberIdList) throws Exception {

		LogUtils.SYS.debug("NSTProxy batchLogout");

		boolean isLogoutSuccess = true;
		for (String memberId : memberIdList) {
			isLogoutSuccess = isLogoutSuccess && this.logout(memberId);
		}
		return isLogoutSuccess;
	}

	/**
	 * NST Logout API
	 * <p>On fail/error, will retry for 3 times before return false.
	 */
	private boolean logout(String memberId) throws Exception {

		LogUtils.SYS.debug("NSTProxy logout: {}", memberId);

		if (StringUtils.isBlank(memberId)) {
			LogUtils.SYS.debug("NSTProxy logout failed: memberId is not provided");
			return false;
		}

		NSTLogoutRequest request = new NSTLogoutRequest();
		request.setMemberId(memberId);

		String result;
		for (int RETRY = 1; RETRY <= 3; RETRY++) {

			try {

				result = this.post(NSTProxyActionType.LOGOUT, request);

				LogUtils.SYS.debug("NSTProxy logout result: {}", JSONUtils.toJsonString(result));

				if (result.isEmpty()) {
					return true; // empty is success
				}

				NSTLogoutResponse rs = JSONUtils.jsonToObject(result, NSTLogoutResponse.class);
				LogUtils.SYS.debug("NSTProxy logout failed: {}", JSONUtils.toJsonString(rs));
			} catch (Exception e) {
				LogUtils.SYS.error("NSTProxy logout error: {}", e.getMessage(), e);
			}

			LogUtils.SYS.debug("NSTProxy logout RETRY: {}", RETRY);
		}

		return false;
	}

	private List<String> getOnlineMember() throws Exception {

		try {

			NSTGetOnlineMemberRequest request = new NSTGetOnlineMemberRequest();

			String result = this.post(NSTProxyActionType.GET_ONLINE_MEMBER, request);

			NSTGetOnlineMemberResponse rs = JSONUtils.jsonToObject(result, NSTGetOnlineMemberResponse.class);
			LogUtils.SYS.debug("NSTProxy getOnlineMember result: {}", JSONUtils.toJsonString(rs));

			if (rs != null && rs.total > 0 && rs.memberIdList != null && !rs.memberIdList.isEmpty()) {
				return rs.memberIdList;
			}
		} catch (Exception e) {
			LogUtils.SYS.error("NSTProxy getOnlineMember error: {}", e.getMessage(), e);
		}

		return Collections.emptyList();
	}

	@Override
	public boolean kickOutAllUser() throws Exception {
		return this.batchLogout(this.getOnlineMember());
	}

	@Override
	public boolean kickOutUser(AccountProvider accountProvider) throws Exception {
		if (accountProvider == null) {
			LogUtils.SYS.debug("NSTProxy kickOutUser failed: accountProvider is not provided");
			return false;
		}
		return this.logout(accountProvider.getProviderAccount());
	}

	@Override
	public boolean kickOutByGame(Game game) throws Exception {
		throw new ProviderAPINoSupportException("not support");
	}

	@Override
	public List<String> getAllActiveUsers() throws Exception {
		return getOnlineMember();
	}

	@Override
	public String getGameLoginUrl(AccountProvider accountProvider, Game game, PlatformType platformType,
		String loginIP, LanguageType languageType) throws Exception {

		var actionType = NSTProxyActionType.LOGIN;
		var url = getConnectionInfo().get(actionType.getDomain()) + actionType.getPath();

		Map<String, String> parameters = new HashMap<>();

		parameters.put("companyKey", getConnectionInfo().get(STRING_COMPANY_KEY));
		parameters.put("memberId", accountProvider.getProviderAccount());

		StringBuilder sb = new StringBuilder(url).append("?");
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			sb.append(entry.getKey()).append("=")
				.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()))
				.append("&");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	/**
	 * provider限制註冊帳號長度16位
	 */
	@Override
	protected String buildProviderAccount(Account account, boolean isMainWallet) throws Exception {

		String prefix = getProviderAccountDefaultPrefix(WebSiteType.getInstance(account.getWebsiteType()),
			isMainWallet);

		return getProviderAccountWithLength(prefix, PROVIDER_ACCOUNT_LIMIT).toLowerCase();
	}

	public String getViewGameUrl(Game game, PlatformType platformType, String fromIp, String requestURL,
		LanguageType languageType)
		throws Exception {
		/// TODO: NST沒有提供遊戲登入URL, 待確認是否有其他方式可以直接取得登入URL
		throw new Exception("not support!");
	}

	protected String getStatusCode(APIResponse response) {
		return null;
	}

	protected String getStatusDescription(APIResponse response) {
		return null;
	}

	//這隻方法不會寫入DAO, 不該被公開調用而指該調用父類別的 createAccountProvider
	public AccountProvider createAccount(Account account) throws Exception {
		AccountProvider accountProvider = buildAccountProvider(account);

		NSTRegisterRequest request = new NSTRegisterRequest();
		request.setMemberId(accountProvider.getProviderAccount());
		request.setCurrencyId(getConnectionInfo().getCurrencyType().getName());

		String result = this.post(NSTProxyActionType.REGISTER, request);

		getLogger().debug("Fetch API result:{}", result);

		return accountProvider;
	}

	@Override
	protected boolean isAccountDuplicate(APIResponse response) {
		return false;
	}

	@Override
	protected OddsType getOddsType(String oddsType) {
		return switch (oddsType.toLowerCase()) {
			case "decimal" -> OddsType.EU;
			case "malay" -> OddsType.MY;
			case "hongkong" -> OddsType.HK;
			case "indo" -> OddsType.INDO;
			default -> OddsType.OTHER;
		};
	}

	@Override
	public void updateGameTransactionFromProvider(WebSiteType webSiteType) throws Exception {
		List<WebsiteVendor> websiteVendors = VendorCache.getInstance().getByProviderId(webSiteType, providerId);

		List<GameTransaction> gameTxns = new ArrayList<>();

		for (WebsiteVendor websiteVendor : websiteVendors) {
			gameTxns.addAll(GameTransactionBO.getOddsAndOddsTypeIsNULLRecords(
				websiteVendor.getWebsiteType(), websiteVendor.getVendorId()));
		}

		List<String> ticketIds = gameTxns.stream()
			.map(GameTransaction::getVendorTxnId)
			.filter(Objects::nonNull)
			.toList();

		if (ticketIds.isEmpty()) {
			return;
		}

		List<NSTTicket> updatedTickets = getTicketById(ticketIds);
		List<GameTransaction> alteredGameTxns = new ArrayList<>();

		gameTxns.forEach(gameTxn ->
			updatedTickets.stream()
				.filter(ticket -> ticket.getDetail() != null)
				.flatMap(ticket -> ticket.getDetail().stream())
				.filter(detail -> Objects.equals(gameTxn.getVendorTxnId(), detail.getTicketId()))
				.findFirst()
				.ifPresent(detail -> {
					gameTxn.setOdds(new BigDecimal(detail.getOdds()));
					gameTxn.setOddsType(getOddsType(detail.getOddsDisplayType()).unique());
					alteredGameTxns.add(gameTxn);
				})
		);

		GameTransactionBO.updateOddsAndOddsType(alteredGameTxns);
	}

	protected BigDecimal doGetBalance(AccountProvider accountProvider) throws Exception {
		return null;
	}

	@Override
	public APIResponse getGameTransaction(Date startDate, Date endDate) throws Exception {
		return null;
	}

	@Override
	public List<Map.Entry<String, String>> getDefaultHeaders() throws Exception {
		/*
		 * connection資料來源：在ProviderProxyCache中
		 * 從ProviderAgent的agentInfo欄位 和 WebsiteProvider的ExtendInfo欄位 設置
		 */
		var coneectionInfo = getConnectionInfo();
		var companyKey = coneectionInfo.get(STRING_COMPANY_KEY);
		var apiKey = coneectionInfo.get(STRING_API_KEY);
		var apiPassword = produceApiPassword(apiKey, companyKey);

		Map<String, String> headers = new HashMap<>();
		headers.put(STRING_COMPANY_KEY, companyKey);
		headers.put(STRING_API_PASSWORD, apiPassword);
		headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

		return headers.entrySet().stream().toList();
	}
	//endregion

	//region NST provider business methods
	public boolean checkLogin(String providerAccountId) throws Exception {
		var providerId = getConnectionInfo().getProvider().getId();
		var webSite = getConnectionInfo().getWebSiteType();

		var accountProvider = AccountProviderBO.findAccountProviderByProviderAccount(providerId, webSite.unique(),
			providerAccountId);
		if (accountProvider == null) {
			throw new Deviation("account not found for providerAccount: " + providerAccountId);
		}
		var userKey = AccountUtils.getUserKey(webSite.unique(), accountProvider.getUserId());
		Account playerInCache = PlayerLocalCache.getInstance().get(userKey);
		var isLogin = playerInCache != null;

		return isLogin;
	}

	public boolean login(AccountProvider accountProvider) throws Exception {
		LogUtils.SYS.debug("login Member for NST: {}", JSONUtils.toJsonString(accountProvider));

		if (accountProvider == null) {
			return false;
		} else {

			String memberAccount = accountProvider.getProviderAccount();
			if (StringUtils.isBlank(memberAccount)) {
				return false;
			}

			Map<String, String> param = new HashMap<>();

			param.put("companyKey", getConnectionInfo().get(STRING_COMPANY_KEY));
			param.put("memberId", memberAccount);

			try {
				var result = this.get(NSTProxyActionType.LOGIN, param);
				LogUtils.providerNST.info("login result: {}", result);
			} catch (Exception e) {
				LogUtils.providerNST.error(e);
				return false;
			}
			return true;

		}

	}

	public List<NSTTicket> getTicketById(List<String> ticketIds) throws Exception {
		NSTGetTicketByIdRequest request = new NSTGetTicketByIdRequest();
		request.setTicketIds(ticketIds);

		String result = this.post(NSTProxyActionType.GET_TICKET_BY_ID, request);
		LogUtils.providerNST.info("getTicketById result: {}", result);
		return JSONUtils.parseJsonToObjectList(result, NSTTicket.class);
	}
	//endregion

	//region NST provider tool methods
	public String get(NSTProxyActionType actionType, Map<String, String> param) throws Exception {

		var url = getConnectionInfo().get(actionType.getDomain()) + actionType.getPath();
		var request = OkHttpClientManager.getInstance().getHttpGetRequest(url);

		var requestParameters = request.getParameters();
		requestParameters.putAll(param);

		for (Map.Entry<String, String> header : getDefaultHeaders()) {
			request.addHeader(header.getKey(), header.getValue());
		}

		request.addInterceptor(new ProxyInterceptor(getLogger(), "[NSTProxy_" + actionType.name() + "]"));

		//兩百以外會丟exception
		return getOKHttpResponseSuccessContentWithInterceptLog(request);
	}

	public String post(NSTProxyActionType actionType, Object postBody) throws Exception {

		var url = getConnectionInfo().get(actionType.getDomain()) + actionType.getPath();

		var request = OkHttpClientManager.getInstance().getHttpJsonPostRequest(url);

		var json = JSONUtils.toJsonString(postBody);

		request.setJson(json);

		for (Map.Entry<String, String> header : getDefaultHeaders()) {
			request.addHeader(header.getKey(), header.getValue());
		}

		request.addInterceptor(new ProxyInterceptor(getLogger(), "[NSTProxy_" + actionType.name() + "]"));

		//兩百以外會丟exception
		return getOKHttpResponseSuccessContentWithInterceptLog(request);
	}

	public static String produceApiPassword(String apiKey, String companyKey) throws Exception {
		if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(companyKey)) {
			return null;
		}

		// 長度不足16, 前面補0 -> 長度超過16, 取後16位
		int length = companyKey.length();
		if (length < 16) {
			companyKey = StringUtils.leftPad(companyKey, 16, '0');
		} else if (length > 16) {
			companyKey = StringUtils.substring(companyKey, length - 16, length);
		}

		companyKey = companyKey.toLowerCase();

		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

		String plainText = DigestUtils.md5Hex((apiKey + companyKey).toLowerCase()) + timestamp;

		return EncryptUtil.encryptAESWithCBC(plainText, companyKey, apiKey);
	}

	public static String decodeApiPassword(String apiPassword, String apiKey, String companyKey) throws Exception {
		if (StringUtils.isBlank(apiPassword)) {
			return null;
		}

		// 長度不足16, 前面補0 -> 長度超過16, 取後16位
		int length = companyKey.length();
		if (length < 16) {
			companyKey = StringUtils.leftPad(companyKey, 16, '0');
		} else if (length > 16) {
			companyKey = StringUtils.substring(companyKey, length - 16, length);
		}

		companyKey = companyKey.toLowerCase();

		String plainText = EncryptUtil.decryptAESWithCBC(apiPassword, companyKey, apiKey);

		if (StringUtils.isBlank(plainText)) {
			return null;
		}

		return plainText.substring(plainText.length() - 10);
	}

	/*
	取解密字串后 10 字元为时间戳 (timestamp)
	时间戳时间需在 10 分钟内
	* */
	public boolean validateApiPassword(String apiPassword) {
		try {
			if (StringUtils.isBlank(apiPassword)) {
				return false;
			}

			String decodedPassword = decodeApiPassword(apiPassword, getConnectionInfo().get(STRING_API_KEY),
				getConnectionInfo().get(STRING_COMPANY_KEY));

			if (StringUtils.isBlank(decodedPassword)) {
				return false;
			}
			long timestamp = Long.parseLong(decodedPassword);
			long currentTimestamp = System.currentTimeMillis() / 1000;

			return Math.abs(currentTimestamp - timestamp) <= MAX_DELAY_MIN * 60;

		} catch (Exception e) {
			LogUtils.SYS.error("validateApiPassword error", e);
			return false;
		}
	}
	//endregion
}
