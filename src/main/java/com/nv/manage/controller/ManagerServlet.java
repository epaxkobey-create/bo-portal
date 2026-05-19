package com.nv.manage.controller;

import java.io.IOException;
import java.io.Serial;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.bo.AccountBO;
import com.nv.commons.bo.ManagerBO;
import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.cache.ManagerCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.LengthType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.TimeIntervalType;
import com.nv.commons.constants.TimePeriodType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Manager;
import com.nv.commons.exceptions.AccessDeniedException;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.CookieUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ManagerUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.Validator;
import com.nv.commons.utils.WebSiteTypeUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;

@WebServlet(urlPatterns = {
	"/login/manager/managerController/*",
	"/manager/managerController/*"})
public class ManagerServlet extends HttpServlet {

	@Serial
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
		if (session == null) {
			request.getRequestDispatcher(FrontendUtils.getEmptyPath()).forward(request, response);
			return;
		}

		Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);

		String cookieCurrency = CookieUtils.getCookieValue(request, SystemConstants.CURRENCY);
		CurrencyType currencyType = null;
		if (manager != null) {
			if (cookieCurrency == null) {
				currencyType = manager.getCurrencyTypeSet()
					.stream()
					.findFirst()
					.orElseThrow(() -> new Deviation(SystemConstants.INTERNAL_EXCEPTION));
			} else {
				currencyType = CurrencyType.getInstance(Integer.parseInt(cookieCurrency));
			}
		}

		LangMessage lang = ManagerUtils.getLangMessage(session, request);

		try {
			String pathInfo = request.getPathInfo();
			if ("/login".equals(pathInfo)) {
				doLogin(request, response, session, lang);
			} else if ("/logout".equals(pathInfo)) {
				doLogout(request, response, session);
			} else if ("/initSideBarElement".equals(pathInfo)) {
				initSideBarElement(response, manager, lang);
			} else if ("/getSideBarElement".equals(pathInfo)) {
				querySideBarElement(response, manager, lang);
//			} else if ("/getPendingData".equals(pathInfo)) {
//				getPendingData(response, session, manager, lang);
			} else if ("/changeSelfPassword".equals(pathInfo)) {
				changePassword(request, response, manager, lang);
			} else if ("/registerUserDetail".equals(pathInfo)) {
				queryRegisterUser(request, response, session, manager, lang, currencyType);
			} else if ("/profitLossDetail".equals(pathInfo) ) {
				queryProfitLossDetail(request, response, manager, currencyType);
			} else if ("/depositDetail".equals(pathInfo) ) {
				queryDepositDetail(request, response, manager, currencyType, lang);
			} else if ("/pendingDepositDetail".equals(pathInfo)) {
				queryPendingDepositDetail(request, response, manager, currencyType, lang);
			} else if ("/withdrawalDetail".equals(pathInfo) ) {
				queryWithdrawalDetail(request, response, manager, currencyType, lang);
			} else if ("/pendingWithdrawalDetail".equals(pathInfo)) {
				queryPendingWithdrawalDetail(request, response, manager, currencyType, lang);
//			} else if ("/searchAllNotification".equals(pathInfo)) {
//				queryAllNotification(request, response, manager, lang);
			} else if ("/getPendingWithdrawal".equals(pathInfo)) {
				getPendingWithdrawal(response, manager, lang);
			}

		} catch (Deviation e) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(e.getMessage(), e.getI18NValues()));
		} catch (AccessDeniedException e) {
			if (FrontendUtils.isAjaxRequest(request)) {
				ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.ACCESS_DENIED));
				return;
			}
			request.getRequestDispatcher(FrontendUtils.getForbiddenPath()).forward(request, response);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

	}

	private void doLogin(HttpServletRequest request, HttpServletResponse response, HttpSession session,
		LangMessage lang)
		throws IOException {

		List<String> errors = new ArrayList<>();
		String userID = RequestParser.getStringParameter(request, LengthType.ManagerUserId.getLength(), "username");
		String password = RequestParser.getStringParameter(request, 100, "password");
		String randomCode = RequestParser.getStringParameter(request, 37, "randomCode", null);

		if (!userID.equals(SystemConstants.MANAGER_ROOT) && !Validator.isValidatedManagerId(userID)) {
			errors.add(lang.get("msg.error.validation.userIDIsNotValid"));
		}
		if (randomCode == null) {
			errors.add(lang.get("msg.login.loginStatusType.1"));
		}

		if (!errors.isEmpty()) {

			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {
				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartObject();
				jGenerator.writeArrayFieldStart("errors");
				for (String error : errors) {
					jGenerator.writeString(error);
				}
				jGenerator.writeEndArray();
				jGenerator.writeEndObject();
			} catch (IOException e) {
				throw e;
			} finally {
				JSONUtils.close(jGenerator);
			}
			ResponseUtils.sendJsonResponse(response, out.toString());
			return;
		}
		// ------------------check end----------------

		String ip = HostAddressUtils.getRealIPAddresses(request);
		String serverName = request.getServerName();
		try {
			WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteByBoDomain(serverName);
			if (webSiteType == null) {
				throw new Deviation().setI18N("msg.error.validation.unknownWebSite", request.getServerName());
			}

			Manager manager;
			// super admin manager doesn't authenticate
			if (SystemConstants.BO_SUPER_ADMIN.equals(userID)) {

				if (!password.equals(SystemConstants.BO_SUPER_ADMIN_PASSWORD)) {
					throw new Deviation();
				}

				manager = ManagerUtils.setSuperAdminManager(session.getId(), webSiteType.unique());

			} else {
				manager = ManagerBO.managerAuthenticate(webSiteType, userID, password, session.getId(),
					false);
			}

			ManagerCache.getInstance().add(manager.getUserId(), manager);

			Manager oldManager = (Manager) session.getAttribute(
				SessionKeyConstants.ManagerRole); // 同一瀏覽器,不同帳號登入,將舊的帳號刪除掉
			if (oldManager != null
				&& oldManager.getWebsiteTypeObj() == manager.getWebsiteTypeObj()
				&& !oldManager.getUserId().equals(manager.getUserId())) {
				ManagerCache.getInstance().remove(oldManager.getWebsiteTypeObj(), oldManager.getUserId());
			}
			session.setAttribute(SessionKeyConstants.ManagerRole, manager);

			LogUtils.SYS
				.info("[" + manager.getUserId() + "] LOGIN " + manager.getSessionID() + " - "
					  + request.getServerName() + " - " + request.getServerPort() + " - " + session.getId());

			String forwardPage = FrontendUtils.getBackOffice();

			if (Validator.isEmpty(forwardPage)) {
				request.getRequestDispatcher(FrontendUtils.getEmptyPath()).forward(request, response);
				return;
			}

			ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString("status", "200", "page",
				forwardPage));

		} catch (Deviation e) {
			// 當有人在try密碼的時候，需要有相關訊息
			LogUtils.SYS.info("[" + userID + "] LOGIN FAIL " + ip + " - "
							  + request.getServerName() + " - " + request.getServerPort() + " - " + e.getMessage() + " - "
							  + session.getId());
			ResponseUtils.sendJsonErrorResponse(response, lang.get("msg.error.validation.userIDorPasswordIsNotValid"));
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}
	}

	private void doLogout(HttpServletRequest request, HttpServletResponse response, HttpSession session)
		throws Exception {
		if (session != null) {
			Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
			try {
				ManagerBO.logout(manager);
				AccountUtils.logoutManager(session);
				CookieUtils.deleteCookie(request, response, SystemConstants.CURRENCY);
				LogUtils.SYS.info("[" + manager.getUserId() + "] LOGOUT " + session.getId());
			} catch (Exception e) {
				LogUtils.SYS.error(e.getMessage(), e);
				request.setAttribute("message", e.getMessage());
				request.getRequestDispatcher(FrontendUtils.getDeviationPath()).forward(request, response);
				return;
			}
		}

		response.sendRedirect(SystemConstants.MANAGER_LOGIN_PAGE);
	}

	private void initSideBarElement(HttpServletResponse response, Manager manager,
		LangMessage lang) {
		ResponseUtils.sendJsonResponse(response,
			ManagerUtils.fetchAllowSidebar(lang));
	}

	private void querySideBarElement(HttpServletResponse response,
		Manager manager, LangMessage lang) {

		WebSiteType websiteType = manager.getWebsiteTypeObj();

		Manager managerInCache = ManagerCache.getInstance()
			.getManager(manager.getWebsiteTypeObj(), manager.getUserId());

		if (!managerInCache.isUpdateAccessRight()) {
			ResponseUtils.sendJsonResponse(response, "");
			return;
		}

		managerInCache.setUpdateAccessRight(false);

//		ManagerRole role = SystemConstants.BO_SUPER_ADMIN.equals(manager.getUserId()) ?
//			manager.getManagerRole() :
//			ManagerRoleCache.getInstance().getRole(manager.getRoleID());

		ResponseUtils.sendJsonResponse(response, ManagerUtils.fetchAllowSidebar(lang));
	}

	//	private void updateMenuAccess(HttpServletRequest request, HttpServletResponse response)
	//			throws IOException {
	//		
	//		HttpSession session = request.getSession(false);
	//		Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
	//		if (manager == null) {
	//			return;
	//		}
	//		
	//		try {
	//			ResponseUtils.sendJsonResponse(response, ManagerRoleCache.getInstance().getAccessRights(manager.getWebsiteTypeObj(), manager.getRoleID()));
	//		} catch (Exception e) {
	//			ResponseUtils.sendJsonErrorResponse(response, SystemConstants.INTERNAL_EXCEPTION);
	//		}
	//	}

	// TODO: what is this for?
//	private void getPendingData(HttpServletResponse response, HttpSession session, Manager manager, LangMessage lang)
//		throws Exception {
//		ResponseUtils.sendJsonResponse(response, ManagerBO.getPendingData(manager, lang));
//	}

	private void queryRegisterUser(HttpServletRequest request, HttpServletResponse response,
		HttpSession session, Manager manager, LangMessage lang, CurrencyType currencyType) throws Exception {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		//		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int pageSize = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);
		String queryType = RequestParser.getStringParameter(request, 10, "queryType", null);
		String queryTime = RequestParser.getStringParameter(request, 20, "queryTime", "");
//		String sortName = RequestParser.getStringParameter(request, 20, "sortName", null);

		long affiliateId = -1;

		Timestamp startTimestamp = null;
		Timestamp endTimestamp = null;

			Timestamp[] timeRange = getTimeRange(queryTime);

			startTimestamp = timeRange[0];
			endTimestamp = timeRange[1];

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(pageNumber);

		final String sortColumn = getSortColumn(sortConditionInt);

		String data = AccountBO.getRegisterUsers(manager.getWebsiteTypeObj(), startTimestamp, endTimestamp,
			currencyType.unique(), pageInfo, sortColumn,
			DBOrderType.getInstanceOf(sortDir), affiliateId, lang);

		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	private static Timestamp[] getTimeRange(String queryTime) {
		TimePeriodType timeKey = TimePeriodType.getInstanceOf(queryTime);
		return (timeKey == null) ? null : timeKey.getDuration();
	}

	@NotNull
	private static String getSortColumn(int sortConditionInt) {
		final String sortColumn;
		if (1 == sortConditionInt) {
			sortColumn = "affiliate";
		} else if (2 == sortConditionInt) {
			sortColumn = "user_id";
		} else if (3 == sortConditionInt) {
			sortColumn = "sign_up_time";
		} else if (4 == sortConditionInt) {
			sortColumn = "user_name";
		} else if (5 == sortConditionInt) {
			sortColumn = "deposit_amount";
		} else if (6 == sortConditionInt) {
			sortColumn = "sign_up_country";
		} else if (7 == sortConditionInt) {
			sortColumn = "sign_up_state";
		} else if (8 == sortConditionInt) {
			sortColumn = "sign_up_city";
		} else if (9 == sortConditionInt) {
			sortColumn = "user_channel_type";
		} else {
			sortColumn = "sign_up_time";
		}
		return sortColumn;
	}

	private void queryProfitLossDetail(HttpServletRequest request, HttpServletResponse response, Manager manager,
		CurrencyType currencyType)
		throws Exception {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		String queryTime = RequestParser.getStringParameter(request, 20, "queryTime");

		Timestamp[] timeRange = getTimeRange(queryTime);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(showCount);
		pageInfo.setPageNumber(pageNumber);

//		String data = AccountSummaryReportBO
//			.getProfitLoss(timeRange[0], timeRange[1], currencyType,
//				pageInfo, sortColumn, DBOrderType.getInstanceOf(sortDir), manager.getWebsiteTypeObj());
//
//		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	private void queryDepositDetail(HttpServletRequest request, HttpServletResponse response, Manager manager,
		CurrencyType currencyType, LangMessage lang)
		throws Exception {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		String queryTime = RequestParser.getStringParameter(request, 20, "queryTime", "");

		Timestamp[] timeRange = getTimeRange(queryTime);

		//		ResponseUtils.sendJsonResponse(response, MoneyTransactionBO
		//			.getDashboardDeposit(timeRange[0], timeRange[1], currencyType, manager.getWebsiteTypeObj(),
		//				pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));

		ResponseUtils.sendJsonResponse(response, MoneyTransactionBO
			.getDashboardDeposit(timeRange[0], timeRange[1], currencyType, manager.getWebsiteTypeObj(),
				pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));

	}

	private void queryPendingDepositDetail(HttpServletRequest request, HttpServletResponse response, Manager manager,
		CurrencyType currencyType, LangMessage lang) {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		ResponseUtils.sendJsonResponse(response,
			MoneyTransactionBO.getPendingDashboardDeposit(currencyType, manager.getWebsiteTypeObj(),
				pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));
	}

	private void queryWithdrawalDetail(HttpServletRequest request, HttpServletResponse response, Manager manager,
		CurrencyType currencyType, LangMessage lang)
		throws Exception {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		String queryTime = RequestParser.getStringParameter(request, 20, "queryTime", "");

		Timestamp[] timeRange = getTimeRange(queryTime);

		//		ResponseUtils.sendJsonResponse(response, MoneyTransactionBO.getDashboardWithdrawal(timeRange[0], timeRange[1], currencyType, manager.getWebsiteTypeObj(),
		//					pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));

		ResponseUtils.sendJsonResponse(response,
			MoneyTransactionBO.getDashboardWithdrawal(timeRange[0], timeRange[1], currencyType,
				manager.getWebsiteTypeObj(),
				pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));
	}

	private void queryPendingWithdrawalDetail(HttpServletRequest request, HttpServletResponse response, Manager manager,
		CurrencyType currencyType, LangMessage lang) {

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);
		String sortColumn = RequestParser.getStringParameter(request, 20, "sortName", null);
		int sortDir = RequestParser.getIntParameter(request, "sortOrder");
		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		ResponseUtils.sendJsonResponse(response,
			MoneyTransactionBO.getPendingDashboardWithdrawal(currencyType, manager.getWebsiteTypeObj(),
				pageNumber, showCount, sortColumn, DBOrderType.getInstanceOf(sortDir), lang));
	}

	private void changePassword(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		List<String> errors = new ArrayList<>();
		String loginIP = HostAddressUtils.getRealIPAddresses(request);
		request.setCharacterEncoding("utf-8");

		String oldPassword = RequestParser.getStringParameter(request, 100, "oldPassword");
		String password = RequestParser.getStringParameter(request, 100, "password");

		if (manager == null) {
			throw new Deviation("msg.error.info.userID.isEmpty");
		}

		String passwordInSession = manager.getPassword();
		String userID = manager.getUserId();

		if (password == null) {
			errors.add(lang.get("msg.error.validation.passwordIsNotValidated"));
		} else {
			if (password.equals(passwordInSession)) {
				errors.add(lang.get("msg.error.validation.newPassSameAsOldPass"));
			}
			if (password.equals(EncryptUtil.encryptSHA1ToHex(userID, false))) {
				errors.add(lang.get("msg.error.validation.managerPasswordIsNotValidated"));
			}
		}

		if (!oldPassword.equals(passwordInSession)) {
			errors.add(lang.get("msg.error.validation.newPassSameAsOldPass"));
		}

		if (!errors.isEmpty()) {
			StringWriter out = new StringWriter();
			JsonGenerator jGenerator = null;

			try {
				jGenerator = JSONUtils.getFactory().createGenerator(out);
				jGenerator.writeStartObject();
				jGenerator.writeArrayFieldStart("errors");
				for (String error : errors) {
					jGenerator.writeString(error);
				}
				jGenerator.writeEndArray();
				jGenerator.writeEndObject();
			} catch (IOException e) {
				throw e;
			} finally {
				JSONUtils.close(jGenerator);
			}
			ResponseUtils.sendJsonResponse(response, out.toString());
			return;
		}

		Manager updateManager = new Manager();
		updateManager.setUserId(userID);
		updateManager.setPassword(password);
		updateManager.setUpdater(userID);

		ManagerBO.changeManagerPassword(updateManager, manager, loginIP, manager.getWebsiteTypeObj());

		ManagerCache.getInstance().update();
	}

	private void getPendingWithdrawal(HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		Map<TimeIntervalType, Integer> pendingFeeWithdrawal = MoneyTransactionBO
			.getPendingWithdrawal(manager.getWebsiteTypeObj());

		if (pendingFeeWithdrawal.isEmpty()) {
			JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
				jGenerator.writeStringField("name", lang.get("form.text.backOffice.slider.noEligibleWithdrawals"));
				jGenerator.writeNullField("feeType");
			};

			ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString(processor));
			return;
		}

		JsonGenerateProcessor processor = (JsonGenerator jGenerator) -> {
			jGenerator.writeStringField("name", lang.get("global.text.moneyTransactionType.WITHDRAWALS"));
			jGenerator.writeArrayFieldStart("feeType");
			for (Map.Entry<TimeIntervalType, Integer> pendingFee : pendingFeeWithdrawal.entrySet()) {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("key", pendingFee.getKey().unique());
				jGenerator.writeStringField("name", lang.get(pendingFee.getKey().getDisplayName()));
				jGenerator.writeNumberField("count", pendingFee.getValue());
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();
		};

		ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString(processor));
	}

}
