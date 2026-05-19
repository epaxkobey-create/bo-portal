package com.nv.commons.paymentGateway.proxy;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.bo.AccountCardBO;
import com.nv.commons.cache.BankCache;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.PGCallBackStatusType;
import com.nv.commons.constants.PGCompanyType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.paymentGateway.dto.PGCallBackResult;
import com.nv.commons.paymentGateway.dto.PGDeposit;
import com.nv.commons.paymentGateway.dto.PGSyncData;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.StringUtil;
import com.nv.module.okHttp.OkHttpClientManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

public class ABCMartPayProxy extends PaymentDepositProxy {

	private static final String callBackApiUrl = "https://cy-api.rsgdev.ac";

	private static final String DEPOSIT_VIA_CREDIT_CARD_API_URL = "/api/placeOrder/creditCard";
	private static final String DEPOSIT_BANKCARD_API_URL = "/api/placeOrder/bankCard";

	public ABCMartPayProxy() {
		super(PGCompanyType.ABCMARTPAY.unique());
	}

	@Override
	public int getForwardTypeFromWebToApi() {
		return REQUEST_FORWARD;
	}

	@Override
	public String makePGDeposit(PGDeposit pgDeposit) throws Exception {

		MoneyTransaction moneyTransaction = pgDeposit.getMoneyTransaction();
		PGAccount pgAccount = pgDeposit.getPgAccount();

		CurrencyType currencyType = CurrencyType.getInstance(pgAccount.getCurrencyTypeId());

		BigDecimal payAmount = convertPayoutAmount(moneyTransaction.getAmount(),
			pgDeposit.getAccount().getCurrencyTypeId());

		// fromBankId attribute is the account card id
		int fromBankId = moneyTransaction.getFromBankId();

		if (moneyTransaction.getToPaymentType() == PaymentType.CREDIT_CARD.unique()) {

			AccountCard accountCard = AccountCardBO.findById(fromBankId);
			if (accountCard == null) {
				throw new Deviation("msg.error.account.paymentMethod.notFound");
			}
			if (accountCard.getStatus() != BinaryStatusType.ACTIVE.unique()) {
				throw new Deviation("msg.error.account.paymentMethod.notActive");
			}
			if (!accountCard.getUserId().equalsIgnoreCase(pgDeposit.getAccount().getUserId())) {
				LogUtils.SYS.error("Card ownership verification failed - userId: {}, cardId: {}, cardOwner: {}",
					pgDeposit.getAccount().getUserId(), fromBankId, accountCard.getUserId());
				throw new Deviation("msg.error.account.paymentMethod.userId.incorrect");
			}

			Map<String, String> postParam = new LinkedHashMap<>();
			postParam.put("amount", payAmount.toPlainString());
			postParam.put("currency", currencyType.name());
			postParam.put("cardNumber", accountCard.getCardNo());
			postParam.put("expMonthYear", accountCard.getExpMonthYear());
			postParam.put("merchantCode", pgAccount.getMerchantId());
			postParam.put("merchantTradeNo", String.valueOf(moneyTransaction.getId()));
			postParam.put("bankName", accountCard.getBankName());
			postParam.put("ReturnURL", getBasicCallbackUrl(pgAccount));
			postParam.put("callbackUrl", getBasicCallbackUrl(pgAccount));

			postParam.put("email", pgDeposit.getAccount().getEmail());
			postParam.put("paymentType", "credit");
			postParam.put("tradeDesc", "deposit");
			postParam.put("cardType", accountCard.getCardSchemeType());
			postParam.put("holderName", accountCard.getCardholderName());

			return getDepositRedirectUrl(JSONUtils.toJsonString(postParam), DEPOSIT_VIA_CREDIT_CARD_API_URL);
		} else if (moneyTransaction.getToPaymentType() == PaymentType.ONLINE_BANKING.unique()) {

			Bank fromBank = BankCache.getInstance().getBank(currencyType, fromBankId);
			if (fromBank == null) {
				throw new Deviation("msg.error.account.bank.isNotValidated");
			}

			Map<String, Object> postParam = new LinkedHashMap<>();
			postParam.put("merchantCode", pgAccount.getMerchantId());
			postParam.put("merchantTradeNo", String.valueOf(moneyTransaction.getId()));

			postParam.put("amount", payAmount.doubleValue());
			postParam.put("currency", currencyType.name());
			postParam.put("email", pgDeposit.getAccount().getEmail());

			postParam.put("holderName", pgDeposit.getAccount().getKycPersonalInfo().getFullName());
			postParam.put("bankName", fromBank.getBankName());

			postParam.put("callbackUrl", getBasicCallbackUrl(pgAccount));

			return getDepositRedirectUrl(JSONUtils.toJsonString(postParam), DEPOSIT_BANKCARD_API_URL);
		}

		throw new Exception("Not supported Payment Type");
	}

	@Override
	public void buildPGDepositRequest(PGDeposit pgDeposit) throws Exception {
		// not supported
	}

	@Override
	public void makeDepositFromWebShop(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// not supported
	}

	@Override
	public PGSyncData getSyncData(HttpServletRequest request) throws Exception {
		var result = new PGSyncData();

		//取得request body, 用Json轉為map
		String body = request.getReader().lines()
			.reduce("", (accumulator, actual) -> accumulator + actual);
		Map<String, String> paramMap = JSONUtils.jsonToMap(body, String.class, String.class);

		result.setOrderNo(paramMap.get("merchantTradeNo"));
		result.setCallbackDate(DateUtils.getNow().getTime());
		result.setReferenceNo(paramMap.get("id"));
		result.setAmount(new BigDecimal(paramMap.get("amount")));
		result.setCurrency(paramMap.get("currency"));
		result.setCardNumber(paramMap.get("cardNumber"));
		result.setHolderName(paramMap.get("holderName"));
		result.setBankName(paramMap.get("bankName"));

		return result;
	}

	@Override
	protected void checkOrderStatus(PGSyncData pgCallBackData, PGCallBackResult pgCallBackResult,
		MoneyTransaction moneyTransaction, PGAccount pgAccount) throws Exception {

		var postRequest = OkHttpClientManager.getInstance()
			.getHttpJsonPostRequest(companyInfo.getApiURL() + "/api/checkorder");
		var content = JSONUtils.toJsonString(Map.of(
			"merchantID", pgAccount.getMerchantId(),
			"merchantTradeNo", String.valueOf(moneyTransaction.getId()),
			"orderId", pgCallBackData.getReferenceNo())
		);
		postRequest.setJson(content);

		var resp = postRequest.execute();
		if (resp.getStatusCode() != 200) {
			throw new Exception("check order failed: " + resp.getStatusCode() + " - " + resp.getContent());
		}
	}

	@Override
	protected PGCallBackStatusType checkOrderStatus(MoneyTransaction moneyTransaction, PGAccount pgAccount)
		throws Exception {

		var postRequest = OkHttpClientManager.getInstance()
			.getHttpJsonPostRequest(companyInfo.getApiURL() + "/api/checkorder");
		var content = JSONUtils.toJsonString(Map.of(
			"merchantID", pgAccount.getMerchantId(),
			"merchantTradeNo", String.valueOf(moneyTransaction.getId()),
			"orderId", String.valueOf(moneyTransaction.getReferenceNo())
		));
		postRequest.setJson(content);

		// check response status code
		var resp = postRequest.execute();
		if (resp.getStatusCode() != 200) {
			throw new Exception("check order failed: " + resp.getStatusCode() + " - " + resp.getContent());
		}

		// check content
		var temopraryRespContent = resp.getContent();
		LogUtils.paymentGateway.debug("check order response: " + temopraryRespContent);

		var respContent = JSONUtils.jsonToMap(temopraryRespContent, String.class, Object.class);
		if (respContent == null || respContent.isEmpty()) {
			throw new Exception("check order failed: content is null/empty - " + temopraryRespContent);
		}

		// check success and code
		var success = respContent.get("success").toString();
		var code = respContent.get("code").toString();
		if (!success.equals("true") || !code.equals("0")) {
			throw new Exception(
				"check order failed: success: " + success + ", code: " + code + " - " + temopraryRespContent);
		}

		// check data
		var data = (Map<String, Object>) respContent.get("data");
		if (data == null || data.isEmpty()) {
			throw new Exception("check order failed: data is null/empty - " + temopraryRespContent);
		}

		// check order status
		var orderStatus = data.get("orderStatus").toString();
		return switch (orderStatus) {
			case "COMPLETED", "1" -> PGCallBackStatusType.SUCCESS;
			case "PENDING", "0" -> PGCallBackStatusType.PENDING;
			case "FAILED", "2" -> PGCallBackStatusType.FAIL;
			default -> PGCallBackStatusType.FAIL;
		};
	}

	@Override
	protected void checkCallBackData(PGSyncData pgCallBackData, PGCallBackResult pgCallBackResult,
		MoneyTransaction moneyTransaction, PGAccount pgAccount) throws Exception {
		pgCallBackResult.setEncryptCheck(true);
	}

	@Override
	protected String responseMessage(MoneyTransaction moneyTransaction, PGCallBackResult pgCallBackResult)
		throws Exception {
		return JSONUtils.getJSONString("status", "OK");
	}

	private String getDepositRedirectUrl(String paramJsonStr, String targetUrl) throws Exception {

		LogUtils.SYS.info("paramJsonStr: {}", paramJsonStr);

		OkHttpClientManager.HTTPResponse response = sendPost(targetUrl, paramJsonStr);

		String result = response.getContent();
		LogUtils.SYS.debug("{}\n{}", targetUrl, result);

		if (StringUtils.isEmpty(result)) {
			throw new Exception("Deposit failed");
		}

		JsonNode resultJson = JSONUtils.toJsonNode(result);

		if (!resultJson.get("success").asBoolean() || resultJson.get("code").asInt() != 0) {
			LogUtils.SYS.error("deposit bank card failed: {}", result);
			throw new Exception("Deposit failed");
		}

		JsonNode dataJson = resultJson.get("data");

		if (dataJson.isNull() || dataJson.isEmpty() || !dataJson.has("redirectUrl")) {
			LogUtils.SYS.error("deposit bank card failed: {}", result);
			throw new Exception("Deposit failed");
		}

		return dataJson.get("redirectUrl").asText();
	}

	private OkHttpClientManager.HTTPResponse sendPost(String url, String jsonContent) throws Exception {
		long ts = Instant.now().getEpochSecond();
		String fullUrl = companyInfo.getApiURL() + url;
		LogUtils.SYS.debug("fullUrl: {}", fullUrl);

		var postRequest = OkHttpClientManager.getInstance().getHttpJsonPostRequest(fullUrl);
		postRequest.addHeader("X-App-Access-Ts", String.valueOf(ts));
		postRequest.setJson(jsonContent);

		OkHttpClientManager.HTTPResponse response = postRequest.execute();

		if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
			LogUtils.SYS.error("{} error: status code {}", fullUrl, response.getStatusCode());
		}

		return response;
	}
}
