package com.nv.commons.paymentGateway.proxy;

import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.cache.key.PGMethodKey;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.dto.PGMethod;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.paymentGateway.dto.PGDeposit;
import com.nv.commons.paymentGateway.dto.PGServerInfo;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PaymentGatewayUtils;
import com.nv.commons.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Title: com.nv.commons.paymentGateway.proxy.PaymentGatewayProxy<br>
 * Description: 所有線上支付通道的父類別
 *
 */
public abstract class PaymentDepositProxy extends PaymentGatewayProxy {

	public static final int RESPONSE_SEND_REDIRECT = 1;

	public static final int REQUEST_FORWARD = 2;


	public PaymentDepositProxy(int proxyId) {
		super(proxyId);
	}

	/**
	 * 付款, 在Web Server上處理前置請求參數工作
	 */
	public PGDeposit makeDeposit(PGServerInfo pgServerInfo, MoneyTransaction moneyTransaction, PGAccount pgAccount,
		Account account, LangMessage lang) throws Exception {
		//建立一個PGdeposit物件
		PGDeposit pgDeposit = new PGDeposit(pgServerInfo, moneyTransaction, pgAccount, account, lang);

		pgDeposit.setCallBackUrl(getBasicCallbackUrl(pgAccount));
		//將pgDeposit 物件放進去
		setPGDepositInfo(pgDeposit);
		//各自的實作
		buildPGDepositRequest(pgDeposit);

		checkPGDeposit(pgDeposit);

		Map<String, String> postParam = pgDeposit.getRequest().getPostParam();

		StringBuilder encryptStr = PaymentGatewayUtils.buildHttpGetParams(postParam, true);


		String encrypt = EncryptUtil.encryptSHA1ToHex(encryptStr.toString(), true);

		pgDeposit.getMoneyTransaction().setProof(encrypt);

		if (!MoneyTransactionBO.updatePaymentGatewayDepositDetailWithProof(pgDeposit.getMoneyTransaction())) {
			LogUtils.paymentGateway
				.debug("redirectRequest : updatePaymentGatewayDepositDetail false, moneyTransaction id = {}",
					pgDeposit.getMoneyTransaction().getId());

			throw new Exception("redirectRequest : updatePaymentGatewayDepositDetail false, moneyTransaction id = " +
								pgDeposit.getMoneyTransaction().getId());
		}

		return pgDeposit;
	}

	public PGDeposit getPGDeposit(PGServerInfo pgServerInfo, MoneyTransaction moneyTransaction, PGAccount pgAccount,
		Account account, LangMessage lang) throws Exception {
		//建立一個PGdeposit物件
		PGDeposit pgDeposit = new PGDeposit(pgServerInfo, moneyTransaction, pgAccount, account, lang);

		pgDeposit.setCallBackUrl(getBasicCallbackUrl(pgAccount));
		//將pgDeposit 物件放進去
		setPGDepositInfo(pgDeposit);

		return pgDeposit;
	}

	/**
	 * Web server 跳轉到 API server 的方式
	 * RESPONSE_SEND_REDIRECT 跳轉到 API server 的 Domain 避免 支付商 阻擋web server的 Domain
	 * REQUEST_FORWARD
	 */
	public abstract int getForwardTypeFromWebToApi();

	/**
	 * 設置發送需求所需參數
	 *
	 * @param pgDeposit
	 * @return
	 * @throws Exception
	 */
	public abstract void buildPGDepositRequest(PGDeposit pgDeposit)
		throws Exception;

	public void makeDeposit(HttpServletRequest request, HttpServletResponse response, MoneyTransaction moneyTransaction,
		LangMessage langMessage)
		throws Exception {

		try {
			makeDepositFromWebShop(request, response);
		} catch (Exception e) {
			moneyTransaction.setApprovedUserid("SYS");
			moneyTransaction.setApprovedNote("auto rejected - API Failed");
			throw e;
		}
	}

	public String makePGDeposit(PGDeposit pgDeposit) throws Exception {
		throw new Deviation("not support this deposit flow");
	}

	/**
	 * 付款, 在API Server上處理後續工作
	 *
	 */
	public abstract void makeDepositFromWebShop(HttpServletRequest request, HttpServletResponse response)
		throws Exception;

	protected void checkPGDeposit(PGDeposit pgDeposit)
		throws Exception {

		PGDeposit.Request pgDepositReq = pgDeposit.getRequest();
		if (StringUtil.isEmpty(pgDepositReq.getRedirectUrl())) {
			Exception e = new IllegalArgumentException("PGDeposit.request.redirectUrl is not exists");
			LogUtils.paymentGateway.error(e.getMessage(), e);
			throw e;
		}

		if (getForwardTypeFromWebToApi() == REQUEST_FORWARD && pgDepositReq.getPostParam().size() == 0) {
			Exception e = new IllegalArgumentException("PGDeposit.request.postParam is empty");
			LogUtils.paymentGateway.error(e.getMessage(), e);
			throw e;
		}
	}


	/**
	 * 同時設定
	 * MoneyTransaction.toPaymentType
	 * PGDeposit.paymentBankCode
	 * PGDeposit.paymentBankCurrency
	 *
	 */
	protected void setPGDepositInfo(PGDeposit pgDeposit) {

		String paymentBankCode = StringUtils.EMPTY;

		final MoneyTransaction moneyTransaction = pgDeposit.getMoneyTransaction();
		final Bank bank = pgDeposit.getBank();

		PaymentType paymentType = PaymentType.getInstanceOf(moneyTransaction.getToPaymentType());

		CurrencyType currencyType = CurrencyType.valueOf(moneyTransaction.getCurrency());

		if (paymentType != null) {
			PGMethodKey pgMethodKey = new PGMethodKey(currencyType.unique(), bank.getId(), paymentType.unique());
			PGMethod pgMethod = methodMap.get(pgMethodKey);

			if (pgMethod != null) {
				boolean isWAP = paymentType.getName().endsWith("WAP");
				boolean isH5 = paymentType.getName().endsWith("H5");
				paymentBankCode = pgMethod.getCode();
				if (isWAP) {
					pgDeposit.setWapMode(true);
				} else if (isH5) {
					pgDeposit.setH5Mode(true);
				}
			}

		}
		pgDeposit.setPaymentBankCode(paymentBankCode);
		pgDeposit.setForwardTypeFromWebToApi(getForwardTypeFromWebToApi());
	}

}
