package com.nv.manage.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.bo.GameBO;
import com.nv.commons.bo.GameSessionUsageBO;
import com.nv.commons.bo.GameUpdateLogBO;
import com.nv.commons.bo.ProviderUpdateLogBO;
import com.nv.commons.bo.WebsiteProviderBO;
import com.nv.commons.bo.WebsiteVendorBO;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.GameCache;
import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.GameStatusType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.GameUpdateType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.ProviderStatusType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Game;
import com.nv.commons.dto.GameUpdateLog;
import com.nv.commons.dto.Manager;
import com.nv.commons.dto.PageResult;
import com.nv.commons.dto.Provider;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.exceptions.AccessDeniedException;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.GamePredicateFactory;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ManagerUtils;
import com.nv.commons.utils.PageUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/manager/ContentManageController/*")
public class ContentManageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		doProcess(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		doProcess(request, response);
	}

	public void doProcess(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		HttpSession session = request.getSession(false);
		LangMessage lang = ManagerUtils.getLangMessage(session, request);
		Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);

		try {
			String pathInfo = request.getPathInfo();

			if ("/searchGames".equals(pathInfo)) {
				searchGames(session, request, response, manager);
			} else if ("/searchGameById".equals(pathInfo)) {
				searchGameById(request, response, lang);
			} else if ("/updateGameStatus".equals(pathInfo)) {
				updateGameStatus(request, response, lang, manager);
			} else if ("/updateGameDisplayOrder".equals(pathInfo)) {
				updateGameDisplayOrder(request, response, lang, manager);
			} else if ("/queryAllProviders".equals(pathInfo)) {
				queryAllProviders(request, response, manager);
			} else if ("/updateProviderProfile".equals(pathInfo)) {
				updateProviderProfile(request, response, manager);
			} else if ("/getProviderById".equals(pathInfo)) {
				getProviderProfileById(request, response, manager);
			} else if ("/getAllProvidersUpdateLogs".equals(pathInfo)) {
				getAllProvidersUpdateLogs(request, response, manager, lang);
			} else if ("/getAllGameUpdateLogs".equals(pathInfo)) {
				getAllGameUpdateLogs(request, response, manager, lang);
			}

		} catch (AccessDeniedException e) {
			if (FrontendUtils.isAjaxRequest(request)) {
				ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.ACCESS_DENIED));
				return;
			}
			request.getRequestDispatcher(FrontendUtils.getForbiddenPath()).forward(request, response);
		} catch (Deviation e) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(e.getMessage(), e.getI18NValues()));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

	}

	private void searchGames(HttpSession session, HttpServletRequest request, HttpServletResponse response,
		Manager manager) throws Exception {

		WebSiteType webSiteType = manager.getWebsiteTypeObj();
		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		int showCount = RequestParser.getIntParameter(request, "pageSize",
			SystemConstants.GAME_PAGE_SIZE);
		int sortDir = RequestParser.getIntParameter(request, "sortingType");
		int vendor = RequestParser.getIntParameter(request, "vendor");
		int type = RequestParser.getIntParameter(request, "gameType");
		int status = RequestParser.getIntParameter(request, "gameStatus");
		int platformType = RequestParser.getIntParameter(request, "platformType");
		String sortField = RequestParser.getStringParameter(request, 20, "sort");

		int[] gameId = RequestParser.getIntParameterValues(request, StringUtil.COMMA_PATTERN, "games", null);
		String filterBy = RequestParser.getStringParameter(request, 15, "filterBy", "recommended");
		List<Predicate<Game>> gamePredicates = new ArrayList<>();
		//condition:vendorId
		if (vendor != -1) {
			gamePredicates.add(GamePredicateFactory.isThisVendor(vendor));
		} else {
			Set<Integer> websiteVendorIdSet = Collections.emptySet();
			Map<Integer, WebsiteVendor> websiteVendorMap = VendorCache.getInstance()
				.getWebsiteVendors(manager.getWebsiteTypeObj());

			List<WebsiteVendor> vendors = websiteVendorMap.values().stream().toList();

			if (!vendors.isEmpty()) {
				websiteVendorIdSet = vendors.stream().map(WebsiteVendor::getVendorId).collect(Collectors.toSet());
			}
			gamePredicates.add(GamePredicateFactory.hasVendorInThisWebsite(websiteVendorIdSet));
		}

		//condition:gameType
		if (type != -1) {
			gamePredicates.add(GamePredicateFactory.isThisType(type));
		}

		//condition:deviceType
		if (platformType != -99) {
			gamePredicates.add(GamePredicateFactory.isThisPlatformType(platformType));
		}

		//condition:GameId
		if (gameId != null) {

			Set<Integer> gameIdSet = IntStream.of(gameId).boxed().collect(Collectors.toSet());

			gamePredicates.add(GamePredicateFactory.isThoseGame(gameIdSet));
		}

		//		gamePredicates.add(GamePredicateFactory.isThisStatus(GameStatusType.UNKNOWN.unique()).negate());

		PageResult<Game> gamePageResult = GameCache.getInstance().searchGames(
			GamePredicateFactory.multiPredicate(true, gamePredicates), filterBy, pageNumber, showCount);

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeNumberField(PageUtils.TOTAL_COUNT, gamePageResult.getTotalCount());
			jGenerator.writeNumberField(PageUtils.DISPLAY_COUNT, gamePageResult.getTotalCount());
			jGenerator.writeArrayFieldStart(PageUtils.SHOW_DATA);
			List<Game> allGames = gamePageResult.getResultList();
			if (!allGames.isEmpty()) {
				for (int i = 0; i < allGames.size(); ) {
					jGenerator.writeStartObject();
					// 每5個分一個群
					for (int j = 0; j < 5; j++, i++) {
						Game game = (i < allGames.size() ? allGames.get(i) : null);
						jGenerator.writeStringField("record" + (j + 1),
							convertToGameString(game, webSiteType));
					}
					jGenerator.writeEndObject();
				}
			}
			jGenerator.writeEndArray();
		};

		ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString(processor));
	}

	private String convertToGameString(Game game, WebSiteType webSiteType) {
		if (game == null) {
			return "";
		}
		WebsiteVendor websiteVendor = VendorCache.getInstance().getWebSiteVendor(webSiteType, game.getVendorId());
		GameType gameType = GameType.getInstance(game.getGameType());
		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeNumberField("vendorID", game.getVendorId());

			jGenerator.writeStringField("vendorName", websiteVendor != null ?
				websiteVendor.getDisplayName() :
				"");
			jGenerator.writeStringField("gameName", game.getName());
			jGenerator.writeStringField("gameEnName", game.getNameEn());
			jGenerator.writeStringField("gameCode", game.getCode());
			jGenerator.writeStringField("gameType", gameType.getShortName());
			jGenerator.writeNumberField("gameTypeUnique", game.getGameType());

			jGenerator.writeNumberField("display", game.getDisplayOrder());
			String iconPath = game.getIconPath();
			jGenerator.writeStringField("path", "/upload/game/" + iconPath + "?t=" + game.getUpdateTime());
			String vendorCode = VendorCache.getInstance().getVendor(game.getVendorId()).getCode();
			jGenerator.writeNumberField("status", game.getStatus());
			jGenerator.writeNumberField("id", game.getId());
			List<PlatformType> deviceTypeList = PlatformType.getPlatformTypes(game.getPlatformType());
			jGenerator.writeStringField("deviceType",
				deviceTypeList.stream().map(PlatformType::name).collect(Collectors.joining(",")));
			List<Integer> deviceTypeNumList = PlatformType.getPlatformTypesUnique(game.getPlatformType());
			jGenerator.writeStringField("deviceTypeNum",
				deviceTypeNumList.stream().map(String::valueOf).collect(Collectors.joining(",")));
			jGenerator.writeStringField("extraData", game.getExtraData() != null ? game.getExtraData() : "");
			jGenerator.writeNumberField("vendorID", game.getVendorId());
			jGenerator.writeStringField("vendorCode", vendorCode);
		};

		return JSONUtils.getJSONString(processor);
	}

	private void updateGameStatus(HttpServletRequest request, HttpServletResponse response, LangMessage lang,
		Manager manager)
		throws Exception {

		int gameId = RequestParser.getIntParameter(request, "gameId");
		int status = RequestParser.getIntParameter(request, "status");

		GameStatusType gameStatusType = GameStatusType.getInstanceOf(status);

		if (gameStatusType == null) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid",
				lang.get("form.text.backOffice.games.vendor"));
		}

		Game oldGame = GameCache.getInstance().getGameById(gameId);

		if (oldGame == null) {
			throw new Deviation("Game not found");
		}

		if (oldGame.getStatus() == status) {
			throw new Deviation("Game status is the same");
		}

		GameBO.updateGameStatus(gameId, gameStatusType);

		Provider provider = ProviderCache.getInstance().getByVendorId(oldGame.getVendorId());

		if (oldGame.getStatus() != status &&
			status == GameStatusType.INACTIVE.unique()) {
			WebsiteProviderBO.kickOutByGame(provider.getId(),
				manager.getWebsiteTypeObj().getDefaultCurrencyType(),
				WebSiteType.getInstance(manager.getWebsiteType()), oldGame);
		}

		// log to game update log
		GameUpdateLog gameUpdateLog = new GameUpdateLog();
		gameUpdateLog.setgameId(String.valueOf(gameId));
		gameUpdateLog.setLogType(GameUpdateType.STATUS.unique());
		gameUpdateLog.setWebsiteType(manager.getWebsiteType());
		gameUpdateLog.setRecords(JSONUtils.toJsonString(new UpdateRecord(
			String.valueOf(oldGame.getStatus()), String.valueOf(status), "Game status update"
		)));
		gameUpdateLog.setUpdater(manager.getUserId());
		gameUpdateLog.setUpdaterIp(HostAddressUtils.getRealIPAddresses(request));
		gameUpdateLog.setLogTypeStr(GameUpdateType.STATUS.name());
		gameUpdateLog.setCurrencyTypeId(manager.getWebsiteTypeObj().getDefaultCurrencyType().unique());

		GameUpdateLogBO.insertGameUpdateLog(gameUpdateLog);
		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	}

	private void searchGameById(HttpServletRequest request, HttpServletResponse response, LangMessage lang)
		throws Exception {

		int gameId = RequestParser.getIntParameter(request, "gameId");

		Game game = GameCache.getInstance().getGameById(gameId);

		if (game == null) {
			throw new Deviation("Game not found");
		}

		ResponseUtils.sendJsonResponse(response, JSONUtils.toJsonString(game));
	}

	private void updateGameDisplayOrder(HttpServletRequest request, HttpServletResponse response, LangMessage lang,
		Manager manager)
		throws Exception {

		int gameId = RequestParser.getIntParameter(request, "gameId");
		int displayOrder = RequestParser.getIntParameter(request, "displayOrder");

		// 檢查 displayOrder 是否在 0 ~ 100 之間
		if (displayOrder < 0 || displayOrder > 99) {
			throw new Deviation("Please enter a Display Order between 0 and 99.");
		}

		Game oldGame = GameCache.getInstance().getGameById(gameId);

		if (oldGame == null) {
			throw new Deviation("Game not found");
		}

		GameBO.updateGameDisplayOrder(gameId, displayOrder);

		GameUpdateLog gameUpdateLog = new GameUpdateLog();
		gameUpdateLog.setgameId(String.valueOf(gameId));
		gameUpdateLog.setLogType(GameUpdateType.DISPLAY_ORDER.unique());
		gameUpdateLog.setWebsiteType(manager.getWebsiteType());
		gameUpdateLog.setRecords(JSONUtils.toJsonString(new UpdateRecord(
			String.valueOf(oldGame.getDisplayOrder()), String.valueOf(displayOrder), "Game display update"
		)));
		gameUpdateLog.setUpdater(manager.getUserId());
		gameUpdateLog.setUpdaterIp(HostAddressUtils.getRealIPAddresses(request));
		gameUpdateLog.setLogTypeStr(GameUpdateType.DISPLAY_ORDER.name());
		gameUpdateLog.setCurrencyTypeId(manager.getWebsiteTypeObj().getDefaultCurrencyType().unique());

		GameUpdateLogBO.insertGameUpdateLog(gameUpdateLog);

		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	}

	private void queryAllProviders(HttpServletRequest request, HttpServletResponse response, Manager manager)
		throws Exception {
		try {
			int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
			int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);
			String sortField = RequestParser.getStringParameter(request, 20, "sortName");
			int sortDir = RequestParser.getIntParameter(request, "sortOrder");
			WebSiteType webSiteType = manager.getWebsiteTypeObj();

			ResponseUtils.sendJsonResponse(response,
				WebsiteProviderBO.getJsonByWebsiteType(webSiteType, pageNumber, showCount,
					DBOrderType.getInstanceOf(sortDir), sortField));

		} catch (Exception e) {
			LogUtils.operator.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	private void updateProviderProfile(HttpServletRequest request, HttpServletResponse response, Manager manager)
		throws Exception {
		try {
			// TODO: BO 前端有做參數驗證，servlet 後端未來要補上 — providerStatus 未驗證 enum 合法性，providerName 未做 blank/null 檢查
			int providerId = RequestParser.getIntParameter(request, "providerId");
			int status = RequestParser.getIntParameter(request, "providerStatus");
			String providerName = RequestParser.getStringParameter(request, 19, "providerName", null);

			String[] dateRange =
				RequestParser.getStringParameterValues(
					request, StringUtil.DASH_PATTERN, 50, "maintenanceDaterange", null);

			Timestamp startTime = null;
			Timestamp endTime = null;
			if (dateRange != null) {
				Date startDate =
					FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
				startTime = new Timestamp(startDate.getTime());

				Date endDate =
					FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
				endTime = new Timestamp(endDate.getTime());
			}

			WebsiteProvider provider = ProviderCache.getInstance().getWebsiteProvider(WebSiteType.getInstance(
				manager.getWebsiteType()), providerId);
			if (provider == null) {
				LogUtils.SYS.error("provider not found: {}", providerId);
				throw new Deviation("No provider with id " + providerId);
			}

			WebsiteProvider oldProvider = provider.deepCopy();

			int originalStatus = provider.getStatus();

			if (providerName != null) {
				provider.setDisplayName(providerName);
			}
			provider.setMaintenanceStart(startTime);

			provider.setMaintenanceEnd(endTime);

			provider.setStatus(status);
			provider.setUpdater(manager.getUserId());

			boolean isChangeStatus = originalStatus != provider.getStatus();

			WebsiteProviderBO.update(provider);

			// update website vendor also

			WebsiteVendor vendor = new WebsiteVendor();
			vendor.setDisplayName(provider.getDisplayName());
			vendor.setMaintenanceStart(provider.getMaintenanceStart());
			vendor.setMaintenanceEnd(provider.getMaintenanceEnd());
			vendor.setStatus(provider.getStatus());
			vendor.setUpdater(manager.getUserId());
			vendor.setWebsiteProviderId(providerId);
			vendor.setWebsiteType(provider.getWebsiteType());
			vendor.setUpdateTime(new Timestamp(System.currentTimeMillis()));

			WebsiteVendorBO.updateWebsiteVendor(vendor);

			WebsiteProviderBO.compareProviderObjectAndInsertUpdateLog(oldProvider, provider,
				manager.getUserId(),
				HostAddressUtils.getRealIPAddresses(request),
				manager.getWebsiteTypeObj().getDefaultCurrencyType().unique()
			);

			if (isChangeStatus && (provider.getStatus() == ProviderStatusType.MAINTENANCE.unique()
				|| provider.getStatus() == ProviderStatusType.INACTIVE.unique())) {
				WebsiteProviderBO.kickoutAllUser(provider.getProviderId(),
					manager.getWebsiteTypeObj().getDefaultCurrencyType(),
					WebSiteType.getInstance(provider.getWebsiteType()));

				List<String> activePlayerList = WebsiteProviderBO.getAllActivePlayers(providerId,
					manager.getWebsiteTypeObj().getDefaultCurrencyType(),
					WebSiteType.getInstance(provider.getWebsiteType()));

				Map<String, String> providerAccountToUserKey = AccountProviderCache.getInstance()
					.getUserKeysByProviderAccounts(providerId, activePlayerList);

				if (!providerAccountToUserKey.isEmpty()) {
					List<String> activeUserKeys = new ArrayList<>(providerAccountToUserKey.values());
					GameSessionUsageBO.batchEndActiveSessions(activeUserKeys);
				}
			}

			ResponseUtils.sendJsonResponse(response, JSONUtils.STATUS_200_OK);

		} catch (Deviation e) {
			ResponseUtils.sendJsonErrorResponse(response, e.getMessage());
		} catch (Exception e) {
			LogUtils.operator.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, SystemConstants.INTERNAL_EXCEPTION);
		}
	}

	private void getProviderProfileById(HttpServletRequest request, HttpServletResponse response, Manager manager)
		throws Exception {
		String providerId = RequestParser.getStringParameter(request, 3, "providerId");

		ResponseUtils.sendJsonResponse(response,
			WebsiteProviderBO.getSingleProviderDetails(Integer.parseInt(providerId), manager.getWebsiteTypeObj()));
	}

	private void getAllProvidersUpdateLogs(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage langMessage) throws Exception {

		int providerId = RequestParser.getIntParameter(request, "providerId");

		String[] dateRange = RequestParser.getStringParameterValues(
			request, StringUtil.DASH_PATTERN, 50, "searchProviderUpdateLogDaterange", null);

		Timestamp startTime = null;
		Timestamp endTime = null;
		if (dateRange != null) {

			Date startDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			startTime = new Timestamp(startDate.getTime());

			Date endDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
			endTime = new Timestamp(endDate.getTime());
		}
		int type = RequestParser.getIntParameter(request, "updateType", -99);

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");
		int sortConditionInt = RequestParser.getIntParameter(request, 1, "sortCondition");
		String sortCondition = null;
		if (1 == sortConditionInt) {
			sortCondition = "logTypeStr";
		} else if (4 == sortConditionInt) {
			sortCondition = "updater";
		} else if (5 == sortConditionInt) {
			sortCondition = "updateTime";
		} else if (6 == sortConditionInt) {
			sortCondition = "updaterIp";
		}

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);
		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		String result = ProviderUpdateLogBO.getFullProviderUpdateLog(
			providerId, startTime, endTime, type, manager.getWebsiteTypeObj().getDefaultCurrencyType(),
			manager.getWebsiteType(), sortCondition, pageNumber, showCount, orderType);

		ResponseUtils.sendJsonResponse(response, result);
	}

	private void getAllGameUpdateLogs(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage langMessage) throws Exception {

		int gameId = RequestParser.getIntParameter(request, "gameId");

		String[] dateRange = RequestParser.getStringParameterValues(
			request, StringUtil.DASH_PATTERN, 50, "searchGameUpdateLogDaterange", null);

		Timestamp startTime = null;
		Timestamp endTime = null;
		if (dateRange != null) {

			Date startDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			startTime = new Timestamp(startDate.getTime());

			Date endDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
			endTime = new Timestamp(endDate.getTime());
		}
		int type = RequestParser.getIntParameter(request, "updateType", -99);

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");
		int sortConditionInt = RequestParser.getIntParameter(request, 1, "sortCondition");
		String sortCondition = null;
		if (1 == sortConditionInt) {
			sortCondition = "logTypeStr";
		} else if (4 == sortConditionInt) {
			sortCondition = "updater";
		} else if (5 == sortConditionInt) {
			sortCondition = "updateTime";
		} else if (6 == sortConditionInt) {
			sortCondition = "updaterIp";
		}

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);
		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		String result = GameUpdateLogBO.getFullGameUpdateLog(
			gameId, startTime, endTime, type, manager.getWebsiteTypeObj().getDefaultCurrencyType(),
			manager.getWebsiteType(), sortCondition, pageNumber, showCount, orderType);

		ResponseUtils.sendJsonResponse(response, result);
	}
}
