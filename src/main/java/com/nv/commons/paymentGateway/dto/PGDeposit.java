package com.nv.commons.paymentGateway.dto;

import java.util.HashMap;
import java.util.Map;

import com.nv.commons.cache.BankCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DeviceType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.dto.PGAccount;
import com.nv.commons.message.LangMessage;
import com.nv.commons.system.SystemInfo;

/***
 * 存放 給 paymentproxy處理的參數
 */
public class PGDeposit {

	public PGDeposit() {
		this.account = null;
		this.pgDepositReq = new Request();
	}

	public PGDeposit(PGServerInfo pgServerInfo, MoneyTransaction moneyTransaction, PGAccount pgAccount,
		Account account, LangMessage lang) {
		this.account = account;
		this.moneyTransaction = moneyTransaction;
		this.pgAccount = pgAccount;
		this.bank = BankCache.getInstance().getBank(moneyTransaction.getToBankId());
		this.deviceType = DeviceType.getInstanceOf(account.getDeviceType());
		this.ip = pgServerInfo.getIp();
		this.serverName = pgServerInfo.getServerName();
		this.pgServerInfo = pgServerInfo;
		this.pgDepositReq = new Request();
		this.pgResultPage = pgAccount.getPaymentWebshopApiUrl() +
			"/page/api/pg/pg_result.jsp?w=" +
			(
//				SystemInfo.getInstance().isProduction() ? "https://" :
					"http://") +
			this.serverName +
			(
//				SystemInfo.getInstance().isProduction() ? "" :
				":" + pgServerInfo.getServerPort());
		this.lang = lang;
	}

	private final Request pgDepositReq;

	private final Account account;

	private MoneyTransaction moneyTransaction;

	private PGAccount pgAccount;

	private DeviceType deviceType;

	private Bank bank;

	private String paymentBankCode;

	private CurrencyType paymentBankCurrency;

	private String ip;

	private String serverName;

	private boolean wapMode = false;

	private boolean h5Mode = false;

	private String pgResultPage;

	private int forwardTypeFromWebToApi;

	private String callBackUrl;

	private LangMessage lang;

	private PGServerInfo pgServerInfo;

	public MoneyTransaction getMoneyTransaction() {
		return moneyTransaction;
	}

	public void setMoneyTransaction(MoneyTransaction moneyTransaction) {
		this.moneyTransaction = moneyTransaction;
	}

	public Account getAccount() {
		return account;
	}

	//    public void setAccount(Account account) {
	//        this.account = account;
	//    }

	public PGAccount getPgAccount() {
		return pgAccount;
	}

	public void setPgAccount(PGAccount pgAccount) {
		this.pgAccount = pgAccount;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	public String getPaymentBankCode() {
		return paymentBankCode;
	}

	public void setPaymentBankCode(String paymentBankCode) {
		this.paymentBankCode = paymentBankCode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Request getRequest() {
		return pgDepositReq;
	}

	public boolean isWapMode() {
		return wapMode;
	}

	public void setWapMode(boolean wapMode) {
		this.wapMode = wapMode;
	}

	public boolean isH5Mode() {
		return h5Mode;
	}

	public void setH5Mode(boolean h5Mode) {
		this.h5Mode = h5Mode;
	}

	public static class Request {

		private Request() {
		}

		private String redirectUrl;

		private Map<String, String> postParam;

		public String getRedirectUrl() {
			return redirectUrl;
		}

		public void setRedirectUrl(String redirectUrl) {
			this.redirectUrl = redirectUrl;
		}

		public Map<String, String> getPostParam() {
			if (postParam == null) {
				postParam = new HashMap<>();
			}
			return postParam;
		}

		public void setPostParam(Map<String, String> postParam) {
			this.postParam = postParam;
		}
	}

	public String getPgResultPage() {
		return pgResultPage;
	}

	public void setPgResultPage(String pgResultPage) {
		this.pgResultPage = pgResultPage;
	}

	public int getForwardTypeFromWebToApi() {
		return forwardTypeFromWebToApi;
	}

	public void setForwardTypeFromWebToApi(int forwardTypeFromWebToApi) {
		this.forwardTypeFromWebToApi = forwardTypeFromWebToApi;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public LangMessage getLang() {
		return lang;
	}

	public PGServerInfo getPgServerInfo() {
		return pgServerInfo;
	}
}
