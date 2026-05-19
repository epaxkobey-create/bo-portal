package com.nv.commons.bo;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.APIResponseType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.VendorStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;

@SuppressWarnings("unused")
public class GamePlayBO {

	/**
	 * 第一段檢查 (step 1)
	 * 如有登入檢查 玩家帳號
	 * 第二段檢查 (step 2)
	 * provider vendor狀態
	 * 第三段檢查 (step 3)
	 * 登入: 檢查accountprovider、遊戲狀態、設定遊戲
	 * 試玩: 檢查vendor 是否有試玩 、設定遊戲
	 * <p>
	 * 回傳  維護時的維護連結
	 * 如有 vendor 或是 provider狀態異常(INVISIBLE) throw Deviation
	 *
	 * @return string json 維護時的維護連結
	 */
	public static VendorStatusType checkConditionsBeforePlayGame(boolean isPlayer, WebSiteType webSiteType,
		Account playerInSession, LangMessage langMessage, CurrencyType currencyType,
		String vendorCode, GameType gameType, boolean ignoreProviderMaintain) throws Exception {

		final int gameTypeValue = gameType.unique();

		// step 1
		if (isPlayer && playerInSession != null) {

			final int accountStatus = playerInSession.getStatus();

			if (!AccountUtils.isActive(accountStatus)) {
				String message = AccountUtils.getNotActiveMessage(accountStatus);
				throw new Deviation(message);
			}

			if ((playerInSession.getAllowGameType() & gameTypeValue) == 0) {
				throw new Deviation("msg.error.validation.notPlayGameType");
			}
		}

		// step 2
		VendorStatusType providerVendorStatus = FrontendUtils.getProviderVendorStatus(
			langMessage, webSiteType, currencyType, vendorCode, ignoreProviderMaintain);

		if (providerVendorStatus != VendorStatusType.ACTIVE) {
			return providerVendorStatus;
		}

		Vendor vendor = VendorCache.getInstance().getByVendorCode(vendorCode);
		int providerId = vendor.getProviderId();

		// step 3
		AccountProvider accountProvider;
		if (isPlayer && playerInSession != null) {

			accountProvider = playerInSession.getAccountProvider(providerId);

			String userKey = playerInSession.getUserKey();
			if (accountProvider == null) {

				accountProvider = AccountProviderBO.createAccountProvider(playerInSession, providerId);

				if (accountProvider == null) {
					LogUtils.SYS.error("create account provider fail: providerID={}, player={}", providerId, userKey);
					throw new Deviation(langMessage.get("global.text.pleaseContactCustomerService"));
				}

			}

			// 1. check wallet balance, default use SeamlessWalletApiService?
			SeamlessWalletApiService singleWalletApi = SeamlessWalletApiService.getInstance();

			String result = singleWalletApi.getWalletBalanceJson(userKey);
			JsonNode jsonNode = JSONUtils.toJsonNode(result);

			if (200 != jsonNode.get("status").asInt()) {
				String walletResult = singleWalletApi.createWallet(userKey, BigDecimal.ZERO);

				JsonNode walletResultJsonNode = JSONUtils.toJsonNode(walletResult);

				if (200 != walletResultJsonNode.get("status").asInt()) {
					String errorMessage = walletResultJsonNode.get("message").asText();
					throw new Deviation(langMessage.get("create wallet error", new String[] {errorMessage}));
				}
			}

			String providerAccount = accountProvider.getProviderAccount();

			// TODO: add api check if providerAccount exists
			String respResult = singleWalletApi.addProviderAccount(
				userKey,
				playerInSession.getCurrencyTypeName(),
				accountProvider.getProviderId(),
				providerAccount
			);

			LogUtils.SYS.info(respResult);

			JsonNode jsonResp = JSONUtils.toJsonNode(respResult);

			if (200 != jsonResp.get("status").asInt()) {
				String errorMessage = jsonResp.get("message").asText();
				LogUtils.SYS.error(errorMessage);
			}

		} else {
			throw new Deviation(vendorCode + " has no trial play");
		}

		return VendorStatusType.ACTIVE;
	}
}
