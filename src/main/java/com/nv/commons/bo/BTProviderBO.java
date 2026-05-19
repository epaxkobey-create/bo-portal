package com.nv.commons.bo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.cache.GameCache;
import com.nv.commons.cache.NetPositionCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DeviceType;
import com.nv.commons.constants.GameOpenType;
import com.nv.commons.constants.GameStatusType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.VendorStatusType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.Vendor;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.PlayResponsiblyUtils;
import com.nv.commons.utils.ProviderUtils;
import com.nv.commons.utils.VendorUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class BTProviderBO {

	public static String getWebsiteVendorWithGameType(WebSiteType webSiteType, CurrencyType currencyType,
		int[] gameTypes) throws Exception {
		JsonGenerator jGenerator = null;
		StringWriter writer = new StringWriter();
		if (gameTypes != null) {
			try {
				jGenerator = JSONUtils.getFactory().createGenerator(writer);
				jGenerator.writeStartArray();

				for (int gameTypeId : gameTypes) {

					jGenerator.writeStartObject();
					jGenerator.writeNumberField("gameTypeId", gameTypeId);

					GameType gameType = GameType.getInstance(gameTypeId);
					List<WebsiteVendor> websiteVendors = VendorCache.getInstance()
						.getWebsiteVendors(webSiteType, gameType, currencyType);

					jGenerator.writeArrayFieldStart("vendorList");
					for (WebsiteVendor websiteVendor : websiteVendors) {
						Vendor vendor = VendorCache.getInstance().getVendor(websiteVendor.getVendorId());
						int vendorStatus = websiteVendor.getStatus();
						if (vendorStatus == ProviderStatusType.INACTIVE.unique()) {
							jGenerator.writeEndArray();
							continue;
						}
						jGenerator.writeStartObject();
						jGenerator.writeStringField("vendorCode", vendor.getCode());
						jGenerator.writeNumberField("vendorId", vendor.getId());
						// 試玩類型
						jGenerator.writeBooleanField("hasTrialPlay", false);
						jGenerator.writeStringField("vendorName", websiteVendor.getDisplayName());

						WebsiteProvider websiteProvider = ProviderCache.getInstance()
							.getWebsiteProvider(webSiteType, websiteVendor.getWebsiteProviderId());
						Provider provider = ProviderCache.getInstance().getProvider(websiteProvider.getProviderId());

						if (ProviderUtils.isProviderMaintain(websiteProvider)) {
							vendorStatus = ProviderStatusType.MAINTENANCE.unique();

							Timestamp maintainStart = Optional.ofNullable(provider.getMaintenanceStart())
								.orElse(websiteProvider.getMaintenanceStart());

							Timestamp maintainEnd = Optional.ofNullable(provider.getMaintenanceEnd())
								.orElse(websiteProvider.getMaintenanceEnd());

							jGenerator.writeNumberField("maintainStartTimestamp", maintainStart.getTime());
							jGenerator.writeNumberField("maintainEndTimestamp", maintainEnd.getTime());
						}

						if (VendorUtils.isVendorMaintain(websiteVendor, vendor)) {
							vendorStatus = VendorStatusType.MAINTENANCE.unique();

							Timestamp maintainStart = Optional.ofNullable(vendor.getMaintenanceStart())
								.orElse(websiteVendor.getMaintenanceStart());

							Timestamp maintainEnd = Optional.ofNullable(vendor.getMaintenanceEnd())
								.orElse(websiteVendor.getMaintenanceEnd());

							jGenerator.writeNumberField("maintainStartTimestamp", maintainStart.getTime());
							jGenerator.writeNumberField("maintainEndTimestamp", maintainEnd.getTime());
						}

						jGenerator.writeNumberField("vendorStatus", vendorStatus);

						jGenerator.writeEndObject();
					}
					jGenerator.writeEndArray();
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray();
			} finally {
				JSONUtils.close(jGenerator);
			}
		}
		return writer.toString();
	}

	public static String getGamePlayURL(boolean isPlayer, WebSiteType webSiteType, Account playerInSession,
		LangMessage langMessage, CurrencyType currencyType, String basePath, String vendorCode, String gameCode,
		GameType gameType, String actStr, String userRealIP, PlatformType platformType) throws Exception {
		/*
		 * MEMO: (get game play url && 雨天下注) 共用邏輯 收進 checkConditionsBeforePlayGame
		 *
		 * 第一段檢查 (step 1)
		 * 如有登入檢查 玩家帳號
		 * 第二段檢查 (step 2)
		 * provider vendor狀態
		 * 如有 vendor 或是 provider狀態異常(INVISIBLE) throw Deviation
		 */
		//		final boolean ignoreProviderMaintain = ProviderUtils.isIgnoreProviderMaintain(webSiteType, userRealIP);
		final boolean ignoreProviderMaintain = false;

		final VendorStatusType providerVendorStatus = GamePlayBO.checkConditionsBeforePlayGame(isPlayer, webSiteType,
			playerInSession, langMessage, currencyType, vendorCode, gameType, ignoreProviderMaintain);

		if (providerVendorStatus == VendorStatusType.MAINTENANCE) {

			WebsiteVendor webSiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, vendorCode);
			Vendor vendor = VendorCache.getInstance().getVendor(webSiteVendor.getVendorId());
			WebsiteProvider webSiteProvider = ProviderCache.getInstance()
				.getWebsiteProvider(webSiteType, vendor.getProviderId());

			Timestamp startTimestamp = FrontendUtils.getMaintenanceStartTimestamp(webSiteVendor, vendor,
				webSiteProvider);
			Timestamp endTimestamp = FrontendUtils.getMaintenanceEndTimestamp(webSiteVendor, vendor, webSiteProvider);

			JsonGenerateProcessor processor = jGenerator -> {
				jGenerator.writeNumberField("vendorStatusTypeId", providerVendorStatus.unique());
				jGenerator.writeNumberField("maintainStartTimestamp", startTimestamp.getTime());
				jGenerator.writeNumberField("maintainEndTimestamp", endTimestamp.getTime());
				jGenerator.writeNullField("gameUrl");

			};
			return JSONUtils.getJSONString(processor);
		}
		Vendor vendor = VendorCache.getInstance().getByVendorCode(vendorCode);
		int providerId = vendor.getProviderId();
		ProviderProxy proxy = ProviderProxyCache.getInstance().getProviderProxy(webSiteType, providerId, currencyType);
		AccountProvider accountProvider;

		Game game;
		// step 3
		if (isPlayer && playerInSession != null) {

			accountProvider = playerInSession.getAccountProvider(providerId);

			if (platformType == null) {
				DeviceType playerDeviceType = DeviceType.getInstanceOf(playerInSession.getDeviceType());
				if (playerDeviceType != null) {
					platformType = playerDeviceType.getPlatformType();
				} else {
					//default
					platformType = DeviceType.PERSONAL_COMPUTER.getPlatformType();
				}
			}

			if (!proxy.hasGameList(gameType)) {
				game = new Game();
				game.setVendorId(vendor.getId());
				game.setGameType(gameType.unique());
				game.setPlatformType(platformType.unique());
				game.setExtraData(actStr);
				//GPI Lottery use Game Code
				if (StringUtils.isNotEmpty(gameCode)) {
					game.setCode(gameCode);
				}
				game.setStatus(GameStatusType.ACTIVE.unique());
			} else {
				game = GameCache.getInstance().getGame(vendor.getId(), gameCode, gameType, platformType);
			}

			if (game == null) {
				throw new Deviation(langMessage.get("msg.error.validation.incorrectGame", new String[] {gameCode}));
			}

			//TODO: 加回DISPLAYSETTING判斷以符合模組化需求
			//			// Game status 為OP 設定, GameDisplaySetting 為BO 設定
			//			GameDisplaySetting displaySetting = GameDisplaySettingCache.getInstance()
			//				.getDisplaySetting(webSiteType.unique(), gameType, game.getId());
			//
			//			if (null != displaySetting) {
			//				game.setStatus(displaySetting.getStatus());
			//			}

			if (game.getStatus() == GameStatusType.INACTIVE.unique()) {
				throw new Deviation().setI18N("msg.error.game.play.inactive");
			}

		} else {
			throw new Deviation(vendorCode + " has no trial play");
		}

		String gameUrl = proxy.gameLoginGameUrl(accountProvider, game, platformType,
			LanguageType.getInstance(langMessage.getLang()), userRealIP, basePath);

		GameOpenType finalGameOpenType = GameOpenType.NORMAL;
		PlatformType finalPlatformType = platformType;
		JsonGenerateProcessor processor = jGenerator -> {
			jGenerator.writeNumberField("gameOpenTypeId", finalGameOpenType.unique());
			jGenerator.writeStringField("gameUrl", gameUrl);
			jGenerator.writeStringField("extraData", null);
			if (GameOpenType.HTML.unique() == finalGameOpenType.unique()) {
				jGenerator.writeStringField("htmlData", null);
			}
			jGenerator.writeNumberField("vendorStatusTypeId", providerVendorStatus.unique());
			jGenerator.writeStringField("isDesktop", Boolean.valueOf(finalPlatformType.isWeb()).toString());
			jGenerator.writeStringField("userRealIP", userRealIP);

		};

		//若開啟NetPosition功能, 重置遊戲net position
		//		if(PlayResponsiblyUtils.enableNetPositions(webSiteType, currencyType.unique()))
		//			NetPositionCache.getInstance().clearGameSessionNetPosition(playerInSession.getUserKey(),game.getId());

		//    开始记录登入游戏时间
		String userKey = AccountUtils.getUserKey(webSiteType.unique(), playerInSession.getUserId());
		GameSessionUsageBO.initNewGameSession(userKey);

		return JSONUtils.getJSONString(processor);
	}

	public static void generateGameListInfo(WebSiteType webSiteType, LanguageType languageType, Game game,
		JsonGenerator jGenerator, List<Integer> favoriteGameList) throws IOException {
		Vendor vendor = VendorCache.getInstance().getVendor(game.getVendorId());

		// webSiteVendor possible null
		WebsiteVendor webSiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, game.getVendorId());

		// trialPlayType value == GameType 複合值, 目前有試玩的只有 Slot
		int trialPlayType = (webSiteVendor == null) ? 0 : vendor.getTrialPlayType();
		String vendorName = (webSiteVendor == null) ? vendor.getName() : webSiteVendor.getDisplayName();

		jGenerator.writeStartObject();
		jGenerator.writeNumberField("vendorId", vendor.getId());
		jGenerator.writeStringField("vendorCode", vendor.getCode());
		jGenerator.writeStringField("vendorName", vendorName);
		//				jGenerator.writeStringField("type", gameType.getShortName());
		jGenerator.writeNumberField("trialPlayType", trialPlayType);
		jGenerator.writeNumberField("gameTypeId", game.getGameType());

		jGenerator.writeNumberField("gameId", game.getId());
		jGenerator.writeStringField("gameCode", game.getCode());
		jGenerator.writeStringField("gameName",
			(!languageType.isChinese() && game.getNameEn() != null) ? game.getNameEn() : game.getName());

		if (game.getIconPath() != null && !"path".equals(game.getIconPath())) {
			jGenerator.writeStringField("gameIconPath", "/upload/game/" + game.getIconPath());
		} else {
			jGenerator.writeNullField("gameIconPath");
		}

		if (!favoriteGameList.isEmpty()) {
			jGenerator.writeBooleanField("isFavorite", favoriteGameList.contains(game.getId()));
		} else {
			jGenerator.writeBooleanField("isFavorite", false);
		}

		jGenerator.writeEndObject();
	}
}
