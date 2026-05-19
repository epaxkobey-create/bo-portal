package com.nv.commons.paymentGateway.proxy;

import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.bo.WebSocketBO;
import com.nv.commons.cache.ApacheHttpClientManager;
import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.cache.key.PGMethodKey;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.PGCallBackStatusType;
import com.nv.commons.constants.PGCompanyPurposeType;
import com.nv.commons.constants.PGCompanyType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WalletTransactionStatusType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.dto.PGCompany;
import com.nv.commons.dto.PGMethod;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.paymentGateway.dto.PGCallBackResult;
import com.nv.commons.paymentGateway.dto.PGSyncData;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.BigDecimalUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PaymentGatewayUtils;
import com.nv.commons.utils.ResponseUtils;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Title: com.nv.commons.paymentGateway.proxy.PaymentGatewayProxy<br>
 * Description: 所有線上支付通道的父類別
 *
 */
public abstract class PaymentGatewayProxy {

	protected PGCompany companyInfo = null;

	// Map key : currencyId , Map key : PGMETHOD.BANK_ID
	protected Map<PGMethodKey, PGMethod> methodMap = new HashMap<>();

	protected final int proxyId;

	//	protected static final String SUCCESS_SYNC_MSG = "Payment Success";
	//	protected static final String PENDING_SYNC_MSG = "Pending";
	//	protected static final String ERROR_SYNC_MSG = "Transaction not authentic";
	protected static final String SEPARATOR = "&";
	protected static final String PARAMETER_STAR = "?";

	protected static final String SYNC_RESULT = "sync";
	protected static final String CHECK_ORDER = "checkOrder";

	private static final String ILLEGAL_IP_MSG_PREFIX =
		"[" + HostAddressUtils.getLocalIPAddress() + "][Illegal IP]\r\n";

	private static final String BASIC_CALLBACK_URL = "{0}{1}callback/{2}";


	public PaymentGatewayProxy(int proxyId) {
		this.proxyId = proxyId;
	}

	public abstract PGSyncData getSyncData(HttpServletRequest request)
		throws Exception;

	public PGCompany getCompanyInfo() {
		return companyInfo;
	}

	public void setCompanyInfo(PGCompany interfaceInfoInfo) {
		interfaceInfoInfo
			.setPurposeType(PGCompanyType.getInstanceOf(interfaceInfoInfo.getId()).getPgCompanyPurposeType().unique());
		this.companyInfo = interfaceInfoInfo;
	}

	public void addMethod(PGMethod pgMethod) {
		PGMethodKey pgMethodKey = new PGMethodKey(pgMethod.getCurrencyTypeId(), pgMethod.getBankId(), pgMethod.getPaymentType());
		methodMap.put(pgMethodKey, pgMethod);
	}

	public PGMethod getMethod(PGMethod pgMethod) {
		PGMethodKey pgMethodKey = new PGMethodKey(pgMethod.getCurrencyTypeId(), pgMethod.getBankId(), pgMethod.getPaymentType());
		return methodMap.get(pgMethodKey);
	}

	public Set<PGMethod> getMethodList() {
		return new HashSet<>(methodMap.values());
	}

	/**
	 * 共用方法，組出 GET 參數
	 *
	 * @param elements 欲組合的參數(Key 和 Value 成對加入)
	 * @return Get參數字串
	 * @throws IllegalArgumentException
	 */
	protected StringBuilder buildStringParams(String... elements) throws IllegalArgumentException {
		int length = elements.length;

		if (length % 2 != 0) {
			throw new IllegalArgumentException(
				"params format errors, params: " + JSONUtils.toJsonString(elements));
		}

		StringBuilder plainText = new StringBuilder();
		for (int i = 0; i < length; i += 2) {
			if (!plainText.isEmpty()) {
				plainText.append(SEPARATOR);
			}
			plainText.append(elements[i]).append("=").append(elements[i + 1]);
		}

		return plainText;
	}

	protected StringBuilder buildHttpGetParams(Map<String, String> params, boolean isAlphabetical) {
		// separator 預設 '&'
		return PaymentGatewayUtils.buildHttpGetParams(params, isAlphabetical, SEPARATOR);
	}

	/**
	 * 共用方法，組出 GET 參數 By Alphabetical
	 *
	 * @param params         欲組合的參數
	 * @param isAlphabetical
	 * @return Get參數字串
	 * @parm separator
	 * 組參數中間的間隔號。
	 */
	protected StringBuilder buildHttpGetParams(Map<String, String> params, boolean isAlphabetical, String separator) {

		return PaymentGatewayUtils.buildHttpGetParams(params, isAlphabetical, separator);

	}

	/**
	 * 共用方法，取得PG company 交易結果的請求全部參數
	 *
	 * @param request 請求物件
	 * @return 參數字串
	 */
	protected Map<String, String> getPGResult(HttpServletRequest request) {
		Enumeration<String> params = request.getParameterNames();
		Map<String, String> pgResult = new HashMap<>();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			// unknown length
			String paramValue = request.getParameterValues(paramName)[0];
			pgResult.put(paramName, paramValue);
		}
		return pgResult;
	}

	/**
	 * 取得交易結果全部參數 by 原始順序, is not Alphabetical
	 *
	 * @param request
	 * @return
	 */
	protected Map<String, String> getPGResultByOrder(HttpServletRequest request) {
		Enumeration<String> params = request.getParameterNames();
		Map<String, String> pgResult = new LinkedHashMap<>();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			// unknown length
			String paramValue = request.getParameterValues(paramName)[0];
			pgResult.put(paramName, paramValue);
		}
		return pgResult;
	}

	protected BigDecimal convertPayoutAmount(BigDecimal amount, int currencyTypeId) {
		BigDecimal systemCurrency = CurrencyType.getInstance(currencyTypeId).getSystemConversion();

		if (BigDecimal.ONE.compareTo(systemCurrency) == 0) {
			return amount;
		}

		return BigDecimalUtils.multiply(amount, systemCurrency);
	}

	protected BigDecimal convertToPayinAmount(BigDecimal amount, int currencyTypeId) {
		BigDecimal systemCurrency = CurrencyType.getInstance(currencyTypeId).getSystemConversion();
		if (BigDecimal.ONE.compareTo(systemCurrency) == 0) {
			return amount;
		}
		return BigDecimalUtils.divide(amount, systemCurrency).setScale(2, RoundingMode.DOWN);
	}

	protected String doFormPost(String desc, Map<String, String> postParam, boolean paramOrder, String url)
		throws Exception {
		return doFormPost(desc, new StringEntity(buildHttpGetParams(postParam, paramOrder).toString()), url);
	}

	protected String doFormPost(String desc, StringEntity stringEntity, String url) throws Exception {
		return doPost(desc, stringEntity, url,
			new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
	}

	protected String doJsonPost(String desc, String jsonString, String url) throws Exception {
		return doJsonPost(desc, new StringEntity(jsonString), url);
	}

	protected String doJsonPost(String desc, Map<String, String> postParam, String url) throws Exception {
		return doJsonPost(desc, new StringEntity(JSONUtils.toJsonString(postParam)), url);
	}

	protected String doJsonPost(String desc, StringEntity stringEntity, String url) throws Exception {
		return doPost(desc, stringEntity, url,
			new BasicHeader(HttpHeaders.CONTENT_TYPE, ResponseUtils.JSON_CONTENT_TYPE),
			new BasicHeader("cache-control", "no-cache"));
	}

	protected String doPost(String desc, StringEntity stringEntity, String url, Header... headerList) throws Exception {

		HttpPost httpPost = new HttpPost(url);

		if (headerList != null && headerList.length > 0) {
			for (Header header : headerList) {
				httpPost.setHeader(header);
			}
		}
		httpPost.setEntity(stringEntity);
		return getHttpResponse(httpPost, desc);
	}

	protected String doGet(String desc, String url, Header... headerList) throws Exception {

		HttpGet httpGet = new HttpGet(url);

		if (headerList != null && headerList.length > 0) {
			for (Header header : headerList) {
				httpGet.setHeader(header);
			}
		}

		return getHttpResponse(httpGet, desc);
	}

	protected byte[] doGetByteAry(String desc, String url, Header... headerList) throws Exception {

		HttpGet httpGet = new HttpGet(url);

		if (headerList != null && headerList.length > 0) {
			for (Header header : headerList) {
				httpGet.setHeader(header);
			}
		}

		return getHttpByteResponse(httpGet, desc);
	}

	protected byte[] getHttpByteResponse(HttpRequestBase httpRequestBase, String desc) throws Exception {

		try (CloseableHttpResponse httpResponse = ApacheHttpClientManager.getInstance().getHttpClient()
			.execute(httpRequestBase)) {

			HttpEntity entity = httpResponse.getEntity();

			byte[] result = EntityUtils.toByteArray(entity);

			String encodeStr = "";
			byte[] encodeBase64 = org.apache.tomcat.util.codec.binary.Base64.encodeBase64(result, true);
			encodeStr = EncryptUtil.encryptMD5ToHex(new String(encodeBase64, "UTF-8"));

			LogUtils.paymentGateway.debug("{} [{}], HttpPost Result:{}", companyInfo.getName(), desc, encodeStr);

			return result;
		} catch (Exception e) {

			LogUtils.paymentGateway.debug(e.getMessage(), e);

			throw e;
		}
	}

	protected String getHttpResponse(HttpRequestBase httpRequestBase, String desc) throws Exception {

		try (CloseableHttpResponse httpResponse = ApacheHttpClientManager.getInstance().getHttpClient()
			.execute(httpRequestBase)) {

			HttpEntity entity = httpResponse.getEntity();

			String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);

			LogUtils.paymentGateway.debug("{} [{}], Http Code:{}, HttpPost Result:{}",
				companyInfo.getName(), desc, httpResponse.getStatusLine().getStatusCode(), result);
			return result;
		} catch (Exception e) {

			LogUtils.paymentGateway.debug(e.getMessage(), e);

			throw e;
		}
	}

	public void checkPGAccountValidate(PGAccount pgAccount) throws Deviation {
		// 此PG帳號已達後台設定之交易金額上限
		BigDecimal transactionLimit = BigDecimal.valueOf(pgAccount.getTransactionLimit());
		if (pgAccount.getCurrentAmount().compareTo(transactionLimit) > 0) {
			LogUtils.paymentGateway.info("transactionLimit over limit, CompanyId = {}, PGAccountId = {}",
				proxyId, pgAccount.getId());
			throw new Deviation("The transfer amount exceeds the limit");
		}
	}

	public void processSyncData(HttpServletRequest request, HttpServletResponse response, PGSyncData pgSyncData)
		throws Exception {
		PGCallBackResult pgCallBackResult = new PGCallBackResult();
		MoneyTransaction moneyTransaction = null;
		PGAccount pgAccount = null;
		try {

			if (this.companyInfo.getPurposeType() == PGCompanyPurposeType.INFLOW.unique()) {

				moneyTransaction = Optional.ofNullable(pgSyncData.getOrderNo())
					.map(MoneyTransactionBO::getPaymentGatewayDepositDetail)
					.orElseGet(() -> MoneyTransactionBO.getPaymentGatewayDepositDetailByReferenceNo(
						pgSyncData.getReferenceNo()));

				if (moneyTransaction != null) { // 只撈尚未完成的單, 若該單狀態為已完成(success/fail)時, 不會找到該筆單
					pgAccount = PaymentGatewayCache.getInstance()
						.getPGAccount(Integer.parseInt(moneyTransaction.getToBankAccount()));
				}

			} else if (this.companyInfo.getPurposeType() == PGCompanyPurposeType.OUTFLOW.unique()) {

				moneyTransaction = Optional.ofNullable(pgSyncData.getOrderNo())
					.map(MoneyTransactionBO::getPaymentGatewayWithdrawalDetail)
					.orElseGet(() -> MoneyTransactionBO.getPaymentGatewayWithdrawalDetailByReferenceNo(
						pgSyncData.getReferenceNo()));

				if (moneyTransaction != null) { // 只撈尚未完成的單, 若該單狀態為已完成(success/fail)時, 不會找到該筆單
					pgAccount = PaymentGatewayCache.getInstance().getPGAccount(moneyTransaction.getFromBankId());
				}
			}

			if (moneyTransaction == null) {
				LogUtils.paymentGateway.error("{}, moneyTransaction id : {} not find moneyTransaction!",
					companyInfo.getName(), pgSyncData.getOrderNo());
			} else if (pgAccount == null) {
				LogUtils.paymentGateway.error("{}, moneyTransaction id : {} can not find PGAccount!",
					companyInfo.getName(), pgSyncData.getOrderNo());
			} else {

				moneyTransaction.setWalletReferenceNo(moneyTransaction.getReferenceNo());
				moneyTransaction.setReferenceNo(pgSyncData.getReferenceNo());

				if (StringUtils.isEmpty(moneyTransaction.getFromBankNumber())) {
					moneyTransaction.setFromBankNumber(pgSyncData.getCardNumber());
				}

				pgCallBackResult = processSyncData(pgSyncData, moneyTransaction, pgAccount);

				Account account = new Account();
				account.setWebsiteType(moneyTransaction.getWebsiteType());
				account.setUserId(moneyTransaction.getUserId());

				WebSocketBO.sendDepositLimitUsageUpdate(account);
			}
		} catch (Exception e) {
			LogUtils.paymentGateway.error(e.getMessage(), e);
		}

		ResponseUtils.sendResponse(response, responseMessage(moneyTransaction, pgCallBackResult));
	}

	public PGCallBackResult processSyncData(PGSyncData pgSyncData, MoneyTransaction moneyTransaction,
		PGAccount pgAccount) throws Exception {

		PGCallBackResult pgCallBackResult = new PGCallBackResult();
		try {
			// 驗證參數
			checkCallBackData(pgSyncData, pgCallBackResult, moneyTransaction, pgAccount);

			//驗證參數成功後 需要呼叫API 確認交易單狀態
			if (pgCallBackResult.isEncryptCheck()) {
				try {
					PGCallBackStatusType status = checkOrderStatus(moneyTransaction, pgAccount);
					pgCallBackResult.setPgCallBackStatusType(status);
				} catch (Exception e) {
					// Bug 10/16 fix: catch Exception (not just Deviation) so checkOrderStatus()
					// failures don't skip updateDepositStatus(). Status defaults to PENDING,
					// which updateDepositStatus() handles by logging without state change.
					LogUtils.paymentGateway.warn("money transaction({}) check order fail:{}", moneyTransaction.getId(),
						e.getMessage(), e);
				}
			}

			pgCallBackResult.setMoneyTransaction(moneyTransaction);

			if (this.companyInfo.getPurposeType() == PGCompanyPurposeType.INFLOW.unique()) {
				updateDepositStatus(pgCallBackResult);
			} else if (this.companyInfo.getPurposeType() == PGCompanyPurposeType.OUTFLOW.unique()) {
				updateWithdrawalStatus(pgCallBackResult);
			}
		} catch (Exception e) {
			LogUtils.paymentGateway.error(e.getMessage(), e);
		}
		return pgCallBackResult;
	}

	public void updateDepositOrderStatus(MoneyTransaction moneyTransaction, PGAccount pgAccount) {
		try {
			// deposit payment - pending 、 disapproved
			if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() != moneyTransaction.getTransactionType()) {
				throw new Deviation("Transaction Type is not valid");
			} else if (MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique() == moneyTransaction.getTransactionType()
				&& MoneyTransactionStatusType.NEW.unique() != moneyTransaction.getStatus()
				&& MoneyTransactionStatusType.CLOSE.unique() != moneyTransaction.getStatus()){
				throw new Deviation("Transaction status is not valid");
			}

			PGCallBackStatusType status = PGCallBackStatusType.PENDING;
			try {
				status = checkOrderStatus(moneyTransaction, pgAccount);
			} catch (Exception e) {
				LogUtils.paymentGateway.info("money transaction({}) check order fail:{}",
					moneyTransaction.getId(), e.getMessage());
			}

			if (this.companyInfo.getPurposeType() == PGCompanyPurposeType.INFLOW.unique()) {
				MoneyTransactionStatusType originalStatus = MoneyTransactionStatusType
					.getInstance(moneyTransaction.getStatus());

				if (originalStatus == MoneyTransactionStatusType.NEW
					|| originalStatus == MoneyTransactionStatusType.CLOSE) {
					String note = "update order status to " + status.getStatusType().getName();
					if (originalStatus == MoneyTransactionStatusType.CLOSE) {
						String approvedTime = FormatUtils.dateFormat(moneyTransaction.getApprovedTime());
						note = "Check Order(" + approvedTime + "auto rejected - API Failed->" + status.getStatusType().getName();
					}
					if (status == PGCallBackStatusType.FAIL) {
						moneyTransaction.setApprovedUserid("SYS");
						moneyTransaction.setApprovedNote(note);
						MoneyTransactionBO.rejectDeposit(moneyTransaction);

					} else if (status == PGCallBackStatusType.SUCCESS) {
						moneyTransaction.setApprovedUserid("SYS");
						moneyTransaction.setApprovedNote(note);
						MoneyTransactionBO.approvePaymentDepositByBoCheckOrder(moneyTransaction);
					}
				}
			}
		} catch (Exception e) {
			LogUtils.paymentGateway.error(e.getMessage(), e);
		}
	}

	protected abstract void checkOrderStatus(PGSyncData pgCallBackData, PGCallBackResult pgCallBackResult,
		MoneyTransaction moneyTransaction, PGAccount pgAccount) throws Exception;

	protected abstract PGCallBackStatusType checkOrderStatus(MoneyTransaction moneyTransaction,
		PGAccount pgAccount) throws Exception;

	public PGCallBackStatusType getProxyCheckOrderStatus(MoneyTransaction moneyTransaction, PGAccount pgAccount) throws Exception{
		return checkOrderStatus(moneyTransaction, pgAccount);
	}

	/**
	 * 驗證 call back 參數
	 *
	 * @param pgCallBackData
	 * @param pgCallBackResult
	 * @param moneyTransaction
	 * @param pgAccount
	 * @throws Exception
	 */
	protected abstract void checkCallBackData(PGSyncData pgCallBackData, PGCallBackResult pgCallBackResult,
		MoneyTransaction moneyTransaction, PGAccount pgAccount) throws Exception;

	/**
	 * 回傳給 payment 端的 訊息
	 *
	 */
	protected abstract String responseMessage(MoneyTransaction moneyTransaction, PGCallBackResult pgCallBackResult)
		throws Exception;

	protected void updateDepositStatus(PGCallBackResult pgCallBackResult) {

		MoneyTransaction moneyTransaction = pgCallBackResult.getMoneyTransaction();

		if (pgCallBackResult.isEncryptCheck()) {

			MoneyTransactionStatusType originalStatus = MoneyTransactionStatusType.getInstance(
				moneyTransaction.getStatus());

			if (originalStatus == MoneyTransactionStatusType.NEW) {

				if (pgCallBackResult.getPgCallBackStatusType() == PGCallBackStatusType.FAIL) {

					try {

						moneyTransaction.setApprovedUserid(companyInfo.getName());
						moneyTransaction.setApprovedNote("auto reject");
						moneyTransaction.setApprovedTime(new Timestamp(System.currentTimeMillis()));
						MoneyTransactionBO.rejectDeposit(moneyTransaction);
					} catch (Exception e) {
						LogUtils.paymentGateway.error(e.getMessage(), e);
					}
				} else if (pgCallBackResult.getPgCallBackStatusType() == PGCallBackStatusType.SUCCESS) {

					try {

						moneyTransaction.setApprovedUserid(companyInfo.getName());
						moneyTransaction.setApprovedNote("auto approve");
						moneyTransaction.setApprovedTime(new Timestamp(System.currentTimeMillis()));
						MoneyTransactionBO.approveDepositByPG(moneyTransaction);
					} catch (Exception e) {
						LogUtils.paymentGateway.error(e.getMessage(), e);
					}
				} else {
					LogUtils.paymentGateway.info(
						MessageFormat.format("money transaction {0} status is PENDING, callbackStatus:{1}",
							moneyTransaction.getId(), pgCallBackResult.getPgCallBackStatusType().toString()));
				}
			} else {
				MoneyTransactionBO.updatePaymentGatewayDepositDetail(moneyTransaction);
			}
		} else {
			MoneyTransactionBO.updatePaymentGatewayDepositDetail(moneyTransaction);
		}
	}

	public void updateWithdrawalStatus(PGCallBackResult pgCallBackResult) throws Exception {

		MoneyTransaction moneyTransaction = pgCallBackResult.getMoneyTransaction();

		if (moneyTransaction != null && pgCallBackResult.isEncryptCheck()) {
			MoneyTransactionStatusType originalStatus = MoneyTransactionStatusType
				.getInstance(moneyTransaction.getStatus());
			if (originalStatus == MoneyTransactionStatusType.PROCESSING) {
				moneyTransaction.setApprovedUserid(companyInfo.getName());
				moneyTransaction.setApprovedNote("auto approve");
				moneyTransaction.setApprovedTime(new Timestamp(System.currentTimeMillis()));
				if (pgCallBackResult.getPgCallBackStatusType() == PGCallBackStatusType.SUCCESS) {
					MoneyTransactionBO.settledWithdrawalByPaymentGateway(moneyTransaction, true,
						pgCallBackResult.getCheckPaymentMsg());
				} else if (pgCallBackResult.getPgCallBackStatusType() == PGCallBackStatusType.FAIL) {
					MoneyTransactionBO.settledWithdrawalByPaymentGateway(moneyTransaction, false,
						pgCallBackResult.getCheckPaymentMsg());
				}
			}
		}

	}

	// example: http://{WebShopApiUrl}/pg/callback/ABCMARTPAY
	protected String getBasicCallbackUrl(PGAccount pgAccount) {
		return MessageFormat.format(BASIC_CALLBACK_URL, pgAccount.getPaymentWebshopApiUrl(),
			SystemConstants.PG_SYNC_BASE_URL,
			PGCompanyType.getInstanceOf(this.proxyId).name());
	}

}
