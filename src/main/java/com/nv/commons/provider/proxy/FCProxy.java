package com.nv.commons.provider.proxy;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameFeaturesType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.OddsType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.Vendor;
import com.nv.commons.exceptions.ProviderAPINoSupportException;
import com.nv.commons.exceptions.ProviderException;
import com.nv.commons.provider.constans.FCPlatformType;
import com.nv.commons.provider.dto.APIResponse;
import com.nv.commons.provider.dto.fc.FCBaseRs;
import com.nv.commons.provider.dto.fc.FCGameIconListResponseDTO;
import com.nv.commons.provider.dto.fc.FCGameLoginRs;
import com.nv.commons.provider.dto.fc.FCGameLogoutRs;
import com.nv.commons.provider.intercept.ProxyInterceptor;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.module.okHttp.OkHttpClientManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicNameValuePair;

public class FCProxy extends SingleWalletProxy {

	private static final int PROVIDER_ACCOUNT_LIMIT = 16;

//	private static final String DEPOSIT = "deposit";
//
//	private static final String WITHDRAW = "withdraw";
//
//	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
//
//	public static final String DATE_HOUR_FORMAT = "yyyy-MM-dd'T'HHXXX";
//
//	public static final int MAX_SYNC_DATA = 30000;
//
//	public static final int DELAY_MILLISECOND = 10000;

	public static final int MAX_DELAY_MIN = 60;

//	public static final String DEFAULT_GAME_LOBBY_CODE = "GameLobby";

//	public static final String BCSPORT_PARLAY_CODE = "PARLAY";
//
//	private static final String TIP_GAME_TYPE = "tip";

	public FCProxy(int providerId, String systemCode) {
		super(providerId, systemCode);
	}

	protected int getHttpSocketTimeout() {

		if (ServerInfoUtils.isAPIServer()) {
			return MAX_DELAY_MIN;
		} else {
			return -1;
		}
	}

	/**
	 * FC Batch Logout API
	 * <p>On fail/error, will retry for 3 times before return false.
	 */
	private boolean batchLogout() throws Exception {

		LogUtils.SYS.debug("FCProxy batchLogout");

		String result;
		for (int RETRY = 1; RETRY <= 3; RETRY++) {

			try {

				result = this.walletPost("[kickOutAll]", "KickoutAll", new HashMap<>());

				FCGameLogoutRs rs = JSONUtils.jsonToObject(result, FCGameLogoutRs.class);
				LogUtils.SYS.debug("FCProxy batchLogout result: {}", JSONUtils.toJsonString(rs));

				if (rs != null && (rs.getResult() == 0 || rs.getResult() == 504)) {
					return true;
				}
			} catch (Exception e) {
				LogUtils.SYS.error("FCProxy batchLogout error: {}", e.getMessage(), e);
			}

			LogUtils.SYS.debug("FCProxy batchLogout RETRY: {}", RETRY);
		}

		return false;
	}

	/**
	 * FC Logout API
	 * <p>On fail/error, will retry for 3 times before return false.
	 */
	private boolean logout(String memberAccount) throws Exception {

		LogUtils.SYS.debug("FCProxy logout: {}", memberAccount);

		if (StringUtils.isBlank(memberAccount)) {
			LogUtils.SYS.debug("FCProxy logout failed: memberAccount is not provided");
			return false;
		}

		Map<String, Object> params = new HashMap<>();
		params.put("MemberAccount", memberAccount);

		String result;
		for (int RETRY = 1; RETRY <= 3; RETRY++) {

			try {

				result = this.walletPost("[kickOut]", "KickOut", params);

				FCGameLogoutRs rs = JSONUtils.jsonToObject(result, FCGameLogoutRs.class);
				LogUtils.SYS.debug("FCProxy logout result: {}", JSONUtils.toJsonString(rs));

				if (rs != null && (rs.getResult() == 0 || rs.getResult() == 504)) {
					return true;
				}
			} catch (Exception e) {
				LogUtils.SYS.error("FCProxy logout error: {}", e.getMessage(), e);
			}

			LogUtils.SYS.debug("FCProxy logout RETRY: {}", RETRY);
		}

		return false;
	}

	@Override
	public boolean kickOutAllUser() throws Exception {
		return this.batchLogout();
	}

	@Override
	public boolean kickOutUser(AccountProvider accountProvider) throws Exception {
		if (accountProvider == null) {
			LogUtils.SYS.debug("FCProxy kickOutUser failed: accountProvider is not provided");
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
		throw new ProviderAPINoSupportException("not support");
	}

	private String walletPost(String desc, String path, Map<String, Object> params) throws Exception {
		String uri = getConnectionInfo().get("apiUrl") + path;
		return doPost(desc, params, uri);
	}

	public String doPost(String desc, Map<String, Object> params, String url) throws Exception {

//		LogUtils.SYS.debug("{} {}", url, JSONUtils.toJsonString(params));

		List<BasicNameValuePair> reqParams = new ArrayList<>(Arrays.asList(
			new BasicNameValuePair("AgentCode", getConnectionInfo().get("agentCode")),
			new BasicNameValuePair("Currency", CurrencyType.EUR.name()),
			new BasicNameValuePair("Params",
				aesEncrypt(JSONUtils.toJsonString(params), getConnectionInfo().get("agentKey"))),
			new BasicNameValuePair("Sign", EncryptUtil.encryptMD5ToHex(JSONUtils.toJsonString(params)))
		));

		OkHttpClientManager.HttpPostRequest request = OkHttpClientManager.getInstance().getHttpPostRequest(url);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		for (BasicNameValuePair reqParam : reqParams) {
			String value = Optional.ofNullable(reqParam.getValue()).orElse("");
			request.addParameter(reqParam.getName(), value);
		}

		request.addInterceptor(new ProxyInterceptor(getLogger(), desc));

		return getOKHttpResponseSuccessContentWithInterceptLog(request);
	}

	/*
	 *  TODO: check all the game params (how to get)
	 * */
	@Override
	public String getGameLoginUrl(AccountProvider accountProvider, Game game, PlatformType platformType,
		String loginIP, LanguageType languageType) throws Exception {

		Vendor vendor = VendorCache.getInstance().getVendor(game.getVendorId());
//		Provider provider = ProviderCache.getInstance().getByVendorId(game.getVendorId());

		FCPlatformType fcPlatformType = FCPlatformType.getFCPlatformTypeByVendorCode(vendor.getCode());

		if (fcPlatformType == null) {
			throw new Exception("not support");
		}

		Map<String, Object> params = fcPlatformType.getGameLoginParameters(accountProvider, game, platformType);

		params.put("LanguageID", "1");

		if (GameFeaturesType.JACKPOT.in(game.getJackpot())) {
			params.put("JackpotStatus", String.valueOf(Boolean.TRUE));
		}

		String result = this.walletPost("[getGameLoginUrl]", "Login", params);
		FCGameLoginRs rs = JSONUtils.jsonToObject(result, FCGameLoginRs.class);

		if (rs == null || !rs.isSuccess()) {
			throw new ProviderException(getLogger(), "getGameLoginUrl fail", systemCode,
				getStatusCode(rs), getStatusDescription(rs));
		}
		/*
		else {
			if (isFreesspin && isFirstLogin) {
				AccountBonusTicketBO.updateTicketGameId(accountBonusTicket.getId(), game.getId(), accountProvider.getUserId(), accountProvider.getWebsiteType(), "system");
			}
		}
		*/
		return rs.getUrl();
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
		throw new Exception("not support!");
	}

	protected String getStatusCode(APIResponse rs) {
		return null;
	}

	protected String getStatusDescription(APIResponse rs) {
		return null;
	}

	public AccountProvider createAccount(Account account) throws Exception {
		AccountProvider accountProvider = buildAccountProvider(account);

		Map<String, Object> params = new HashMap<>();
		params.put("MemberAccount", accountProvider.getProviderAccount());

		String result = this.walletPost("[createAccount]", "AddMember", params);

		getLogger().debug("Fetch API result:{}", result);

		FCBaseRs rs = JSONUtils.jsonToObject(result, FCBaseRs.class);
		// 外部平台回覆失敗 且 失敗原因不是帳戶重複則拋錯
		if (rs == null || !rs.isSuccess()) {
			throw new ProviderException(getLogger(), "create account fail", systemCode,
				getStatusCode(rs), getStatusDescription(rs));
		}

		return accountProvider;
	}

	@Override
	protected boolean isAccountDuplicate(APIResponse response) {
		return false;
	}

	@Override
	protected OddsType getOddsType(String oddsType) {
		return null;
	}

	@Override
	public void updateGameTransactionFromProvider(WebSiteType webSiteType) throws Exception {
		return;
	}

	protected BigDecimal doGetBalance(AccountProvider accountProvider) throws Exception {
		return null;
	}

	@Override
	public APIResponse getGameTransaction(Date startDate, Date endDate) throws Exception {
		return null;
	}

	public FCGameIconListResponseDTO getGameIconList() throws Exception {

		String result = this.walletPost("[getGameIconList]", "GetGameIconList", new HashMap<>());

		return JSONUtils.jsonToObject(result, FCGameIconListResponseDTO.class);
	}

	public FCGameLoginRs getPlayerReport(String memberAccount, String recordId) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("MemberAccount", memberAccount);
		params.put("RecordID", recordId);
		params.put("GameType", 2);
		params.put("ShowAccount", 1);
		params.put("ShowPoints", 1);
		params.put("LanguageID", 1);
		String result = this.walletPost("[getPlayerReport]", "GetPlayerReport", params);

		return JSONUtils.jsonToObject(result, FCGameLoginRs.class);

	}

	private static String aesEncrypt(String data, String key) throws Exception {
		Base64.Encoder encoder = Base64.getEncoder();
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		return encoder.encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
	}

	private static String aesDecrypt(String data, String key) throws Exception {
		Base64.Decoder decoder = Base64.getDecoder();
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		return new String(cipher.doFinal(decoder.decode(data)));
	}

	public String encrypt(String data) throws Exception {
		return aesEncrypt(data, getConnectionInfo().get("agentKey"));
	}

	public String decrypt(String data) throws Exception {
		return aesDecrypt(data, getConnectionInfo().get("agentKey"));
	}
}
