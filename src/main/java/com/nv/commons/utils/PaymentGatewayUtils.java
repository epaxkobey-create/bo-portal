package com.nv.commons.utils;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.nv.commons.cache.PaymentGatewayCache;
import com.nv.commons.paymentGateway.proxy.PaymentGatewayProxy;

/**
 * PaymentGateway 相關工具類
 */
public final class PaymentGatewayUtils {

	private static final String SEPARATOR = "&";


	private PaymentGatewayUtils() {
		throw new AssertionError();
	}

	public static Optional<String> getPaymentAccountByToBankBranch(String toBankBranch) {
		return Optional.ofNullable(getPaymentAccountByToBankBranchStr(toBankBranch));
	}

	private static String getPaymentAccountByToBankBranchStr(String toBankBranch) {
		//測試環境會有0，null、ee5這種
		//正式環境會有0，null這種
		if (!Validator.isNumeric(toBankBranch)) {
			return toBankBranch;
		}
		PaymentGatewayProxy proxy = PaymentGatewayCache.getInstance().getProxy(Integer.parseInt(toBankBranch));
		if (null == proxy) {
			return toBankBranch;
		}
		return proxy.getCompanyInfo().getName();
	}


	public static StringBuilder buildHttpGetParams(Map<String, String> params, boolean isAlphabetical) {
		// separator 預設 '&'
		return buildHttpGetParams(params, isAlphabetical, SEPARATOR);
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
	public static StringBuilder buildHttpGetParams(Map<String, String> params, boolean isAlphabetical,
		String separator) {

		if (isAlphabetical && !(params instanceof TreeMap)) {
			params = new TreeMap<>(params);
		}

		StringBuilder plainText = new StringBuilder();

		params.forEach((key, value) -> {
			if (plainText.length() > 0) {
				plainText.append(separator);
			}
			plainText.append(key).append("=").append(value);
		});

		return plainText;
	}

}
