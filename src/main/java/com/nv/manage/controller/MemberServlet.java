package com.nv.manage.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nv.commons.bo.AccountBO;
import com.nv.commons.bo.AccountCardBO;
import com.nv.commons.bo.AccountContactInfoBO;
import com.nv.commons.bo.AccountDocumentBO;
import com.nv.commons.bo.AccountPlayResponsiblySettingBO;
import com.nv.commons.bo.AccountRemarkBO;
import com.nv.commons.bo.AccountSummaryReportBO;
import com.nv.commons.bo.AccountTrackerBO;
import com.nv.commons.bo.AccountUpdateLogBO;
import com.nv.commons.bo.BTReportBO;
import com.nv.commons.bo.GameTransactionBO;
import com.nv.commons.bo.GameTransactionSummaryBO;
import com.nv.commons.bo.KycPersonalInfoBO;
import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.bo.PlayerBO;
import com.nv.commons.bo.UserBO;
import com.nv.commons.cache.AccountProviderCache;
import com.nv.commons.cache.ManagerCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.cache.VendorCache;
import com.nv.commons.cache.key.AccountProviderKey;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountRemarkType;
import com.nv.commons.constants.AccountSortType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.DocumentGroupType;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.GameType;
import com.nv.commons.constants.ImageType;
import com.nv.commons.constants.LengthType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.SystemTxnStatusType;
import com.nv.commons.constants.TimePeriodType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountProvider;
import com.nv.commons.dto.AccountRemark;
import com.nv.commons.dto.AccountRequest;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.dto.Manager;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.dto.WebsiteVendor;
import com.nv.commons.exceptions.AccessDeniedException;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.provider.dto.fc.FCGameLoginRs;
import com.nv.commons.provider.proxy.FCProxy;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.BigDecimalUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.EncryptUtil;
import com.nv.commons.utils.FileUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ManagerUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.StringUtil;
import com.nv.commons.utils.ThreadLocalUtils;
import com.nv.commons.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

@MultipartConfig
@WebServlet(urlPatterns = "/manager/member/*")
public class MemberServlet extends HttpServlet {

	@Serial
	private static final long serialVersionUID = 372381918252936890L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		process(request, response);
	}

	public void process(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		HttpSession session = request.getSession(false);
		LangMessage lang = ManagerUtils.getLangMessage(session, request);
		Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
		try {
			String pathInfo = request.getPathInfo();

			if ("/searchMember".equals(pathInfo)) {
				searchMember(request, response, session, manager, lang);
			} else if ("/createMemberAccount".equals(pathInfo)) {
				createMemberAccount(request, response, manager, lang);
			} else if ("/checkProfileAffiliate".equals(pathInfo)) {
				checkProfileAffiliate(request, response, manager, lang);
			} else if ("/getProfileOverview".equals(pathInfo)) {
				getProfileOverview(request, response, session, manager, lang);
			} else if ("/getProfileAccountUpdateLog".equals(pathInfo)) {
				getAccountUpdateLog(request, response, manager, lang);
			} else if ("/getAccountContactVerify".equals(pathInfo)) {
				getAccountContactVerify(request, response, manager, lang);
			} else if ("/updateStatus".equals(pathInfo)) {
				updateStatus(request, response, manager, lang);
				//			} else if ("/updateRiskRemark".equals(pathInfo)
				//			) {
				//				updateRiskRemark(request, response, manager, lang);
			} else if ("/getNewPassword".equals(pathInfo)) {
				getNewPassword(response);
				//			} else if ("/updateProfileContact".equals(pathInfo)
				//			) {
				//				updateProfileContact(request, response, session, manager, lang);
			} else if ("/verifyProfileContact".equals(pathInfo)) {
				verifyProfileContact(request, response, session, manager, lang);
			} else if ("/searchProfileUpdateLog".equals(pathInfo)) {
				searchProfileUpdateLog(request, response, manager, lang);
			} else if ("/getAllUserIdForMemberSearch".equals(pathInfo)) {
				searchAllUserId(request, response, manager);
			} else if ("/updatePassword".equals(pathInfo)) {
				updatePassword(request, response, manager, lang);

				//			} else if ("/updateAddress".equals(pathInfo) ) {
				//				updateProfileAddress(request, response, session, manager, lang);

			} else if ("/searchProfileLoginLog".equals(pathInfo)) {
				searchProfileLoginLog(request, response, manager, lang);
			} else if ("/searchProfileReportBetSettled".equals(pathInfo)) {
				searchProfileReportBetSettled(request, response, manager, lang);
			} else if ("/searchProfileReportBetSummary".equals(pathInfo)) {
				searchProfileReportBetSummary(request, response, manager, lang);
			} else if ("/searchProfileReportBetUnsettled".equals(pathInfo)) {
				searchProfileReportBetUnsettled(request, response, manager, lang);
			} else if ("/searchBetSummaryDetails".equals(pathInfo)) {
				searchBetSummaryDetails(request, response, manager, lang);
			} else if ("/searchBetSummaryDetailsFromTransaction".equals(pathInfo)) {
				searchBetSummaryDetailsFromTransaction(request, response, manager, lang);
			} else if ("/searchBetDetails".equals(pathInfo)) {
				searchBetDetails(request, response, manager, lang);
			} else if ("/searchGameTransactionDetail".equals(pathInfo)) {
				searchGameTransactionDetail(request, response, manager, lang);
			} else if ("/updateUserRemark".equals(pathInfo)) {
				updateUserRemark(request, response, manager, lang);
			} else if ("/updateKycStatus".equals(pathInfo)) {
				updateKycStatus(request, response, manager, lang);
			} else if ("/editIdDocument".equals(pathInfo)) {
				editIdDocument(request, response, session, manager, lang);
			} else if ("/viewSumsubDocument".equals(pathInfo)) {
				viewSumsubDocument(request, response, manager, lang);
			} else if ("/viewKycImage".equals(pathInfo)) {
				viewImageForUpdateLog(request, response, manager, lang);
			} else if ("/playerResponsibilities".equals(pathInfo)) {
				getPlayerResponsibility(request, response, manager, lang);
			} else if ("/updatePlayerResponsibilities".equals(pathInfo)) {
				updatePlayerResponsibility(request, response, manager, lang);
			} else if ("/cancelPlayerResponsibilities".equals(pathInfo)) {
				cancelPlayerResponsibility(request, response, manager, lang);
			} else if ("/getBetReport".equals(pathInfo)) {
				getBetReport(request, response, manager, lang);
			} else if ("/searchBetReport".equals(pathInfo)) {
				searchBetReport(request, response, manager, lang);
			} else if ("/getOriginBetReport".equals(pathInfo)) {
				getOriginBetReport(request, response, manager, lang);
			} else if ("/removeAccountCard".equals(pathInfo)) {
				removeAccountCard(request, response, manager, lang);
			} else if ("/getMoneyTransactionRecord".equals(pathInfo)) {
				getMoneyTransactionRecord(request, response, manager, lang);
			}
		} catch (NumberFormatException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, lang.get("msg.error.validation.invalidFormat"));
		} catch (Deviation e) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(e.getMessage(), e.getI18NValues()));
		} catch (AccessDeniedException e) {
			if (FrontendUtils.isAjaxRequest(request)) {
				ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.ACCESS_DENIED));
			} else {
				request.getRequestDispatcher(FrontendUtils.getForbiddenPath()).forward(request, response);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}
	}

	public static void searchMember(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpSession session,
		Manager manager,
		LangMessage lang)
		throws Exception {
		boolean enableShowEmail = true;
		boolean enableShowContactPhone = true;
		boolean enableShowPartOfContactPhone = true;

		String userId = RequestParser.getStringParameter(request, 5000, "userId", null);

		if (StringUtils.isNotEmpty(userId)) {
			userId = userId.trim();
		}

		// change to fullName
		String userName = RequestParser.getStringParameter(request, 101, "fullName", null);

		String email = RequestParser.getStringParameter(request, 50, "email", null);
		if (null != email) {
			email = email.toLowerCase();
		}

		DateFormat dateTimeDf =
			ThreadLocalUtils.getSimpleDateFormat(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

		String lastDepositSinceStr =
			RequestParser.getStringParameter(request, 19, "lastDepositSince", null);
		Timestamp lastDepositSince = null;
		if (null != lastDepositSinceStr) {
			Date lastDepositDate = dateTimeDf.parse(lastDepositSinceStr);
			lastDepositSince = new Timestamp(lastDepositDate.getTime());
		}

		String lastWithdrawSinceStr = RequestParser.getStringParameter(request, 19, "lastWithdrawSince", null);
		Timestamp lastWithdrawSince = null;
		if (null != lastWithdrawSinceStr) {
			Date lastWithdrawDate = dateTimeDf.parse(lastWithdrawSinceStr);
			lastWithdrawSince = new Timestamp(lastWithdrawDate.getTime());
		}

		String lastBetTimeSinceStr =
			RequestParser.getStringParameter(request, 19, "lastBetTimeSince", null);
		Timestamp lastBetTimeSince = null;
		if (null != lastBetTimeSinceStr) {
			Date lastBetTimeSinceDate = dateTimeDf.parse(lastBetTimeSinceStr);
			lastBetTimeSince = new Timestamp(lastBetTimeSinceDate.getTime());
		}

		String noLoginSinceStr = RequestParser.getStringParameter(request, 19, "noLoginSince", null);
		Timestamp noLoginSince = null;
		if (null != noLoginSinceStr) {
			Date noLoginSinceDate = dateTimeDf.parse(noLoginSinceStr);
			noLoginSince = new Timestamp(noLoginSinceDate.getTime());
		}

		String loginIp = RequestParser.getStringParameter(request, 15, "lastLoginIp", null);

		String lastRegisterDateStr = RequestParser.getStringParameter(request, 19, "lastRegister", null);
		Date lastRegisterDate = null;
		Timestamp lastRegisterSince = null;
		if (null != lastRegisterDateStr) {
			lastRegisterDate = dateTimeDf.parse(lastRegisterDateStr);
			lastRegisterSince = new Timestamp(lastRegisterDate.getTime());
		}
		String lastLoginSinceStr =
			RequestParser.getStringParameter(request, 19, "lastLoginSince", null);
		Timestamp lastLoginSince = null;
		if (null != lastLoginSinceStr) {
			Date lastLoginSinceDate = dateTimeDf.parse(lastLoginSinceStr);
			lastLoginSince = new Timestamp(lastLoginSinceDate.getTime());
		}

		DateFormat dateDf = ThreadLocalUtils.getSimpleDateFormat(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);

		// 1:Normal
		// 2:Block to receive Promotion
		// 3:Block to receive Bonus

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		// mapping rule to DataTable column index

		final String sortCondition = AccountSortType.getInstanceOf(sortConditionInt).getSortCondition();

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt); // datatable 0:asc, 1:desc

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		HashMap<ContactType, String> contactInfoCondition = new HashMap<>();

		contactInfoCondition.put(ContactType.Email, email);

		boolean queryRecentData = RequestParser.getBooleanParameter(request, "defaultConditionFlag");
		int status = RequestParser.getIntParameter(request, "status");
		int verificationStatus = RequestParser.getIntParameter(request, "verification");

		AccountRequest.AccountRequestBuilder accountRequestBuilder =
			AccountRequest.builder()
				.userIds(userId)
				.userName(userName)
				.currencyType(26)
				.contactInfoCondition(contactInfoCondition)
				.lastLoginSince(lastLoginSince)
				.lastDepositTime(lastDepositSince)
				.lastBetTime(lastBetTimeSince)
				.loginTime(noLoginSince)
				.lastWithdrawTime(lastWithdrawSince)
				.sortCondition(sortCondition)
				.orderType(orderType)
				.webSiteType(manager.getWebsiteTypeObj())
				.pageInfo(pageInfo)
				.currencyList(manager.getCurrencyTypeIdList())
				.enableShowEmail(enableShowEmail)
				.enableShowContactPhone(enableShowContactPhone)
				.enableShowPartOfContactPhone(enableShowPartOfContactPhone)
				.loginIp(loginIp)
				.queryRecentData(queryRecentData)
				.status(status)
				.lastRegister(lastRegisterSince)
				.verificationStatus(verificationStatus);

		String data = AccountBO.searchMember(accountRequestBuilder.build());

		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	private void createMemberAccount(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang)
		throws Exception {

		// Validation delegated to AccountBO.createMemberAccount → UserBO.registerFromBO → BaseRegisterValidator
		String password = RequestParser.getStringParameter(request, 40, "createPassword");
		String confirmPassword = RequestParser.getStringParameter(request, 40, "createConfirmPassword", null);
		String email = RequestParser.getStringParameter(request, 50, "createEmail").trim();
		String userRemark = RequestParser.getStringParameter(request, 3000, "userRemark", null);
		String userId = email;
		AccountStatusType accountStatusType = AccountStatusType.getInstanceOf(1);

		boolean registerResult = AccountBO.createMemberAccount(
			manager,
			lang,
			userId,
			password,
			confirmPassword,
			email,
			HostAddressUtils.getRealIPAddresses(request),
			accountStatusType
		);

		// create remark
		if (registerResult && userRemark != null) {

			AccountRemark accountUserRemark = new AccountRemark();
			accountUserRemark.setWebsiteType(manager.getWebsiteType());
			accountUserRemark.setUserId(userId);
			accountUserRemark.setRemark(StringUtil.escapeHtmlContent(userRemark));
			accountUserRemark.setRemarkType(AccountRemarkType.userRemark.unique());
			accountUserRemark.setUpdater(manager.getUserId());

			AccountRemarkBO.insert(
				manager.getWebsiteType(),
				userId,
				HostAddressUtils.getRealIPAddresses(request),
				accountUserRemark);
		}
		if (registerResult) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	/**
	 *
	 */

	private void checkProfileAffiliate(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang) {
		WebSiteType webSiteType = manager.getWebsiteTypeObj();

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.staff.name"));
		}

		boolean accessAffiliate = true;

		Account account = AccountBO.getAccountByUserId(userId, webSiteType);

		String[] affiliates = ManagerCache.getInstance().getAffiliate(webSiteType, manager.getUserId());

		if (affiliates != null) {
			accessAffiliate =
				Arrays.stream(affiliates).anyMatch(o -> Long.parseLong(o) == account.getAffiliateId());
		}

		if (!accessAffiliate) {
			ResponseUtils.sendJsonResponse(
				response,
				JSONUtils.getJSONString("status", "200", "page", FrontendUtils.getNotFoundPath()));
		}
	}

	public static void getProfileOverview(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpSession session,
		Manager manager,
		LangMessage lang)
		throws Exception {
		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.staff.name"));
		}

		int currency = RequestParser.getIntParameter(request, "currency", -1);

		if (currency == -1) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.currencyType"));
		}

		String result =
			AccountBO.getProfileOverviewWithJGenerator(userId, manager.getWebsiteTypeObj(), currency);

		if (null == result) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
			return;
		}
		ResponseUtils.sendJsonResponse(response, result);
	}

	private void getAccountUpdateLog(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.staff.name"));
		}
		String accountUpdateLog =
			AccountUpdateLogBO.getAccountUpdateLog(userId, manager.getWebsiteTypeObj());
		if (Validator.isEmpty(accountUpdateLog)) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonResponse(response, accountUpdateLog);
	}

	public static void getAccountContactVerify(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.staff.name"));
		}
		String verifiedData =
			AccountContactInfoBO.getVerifiedDataList(userId, manager.getWebsiteTypeObj());
		if (Validator.isEmpty(verifiedData)) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonResponse(response, verifiedData);
	}

	//	private void getProfileBalanceAndRefreshProviderBalance(
	//		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
	//		throws Exception {
	//
	//		String userId =
	//			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
	//
	//		if (!Validator.isValidatedUserId(userId)) {
	//			throw new Deviation()
	//				.setI18N(
	//					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.staff.name"));
	//		}
	//
	//		long bonusTurnoverId =
	//			RequestParser.getLongParameter(request, "bonusTurnoverId", BonusWalletType.MAIN.unique());
	//
	//		String result =
	//			AccountBO.getProfileBalanceAndRefreshProviderBalance(
	//				userId, manager.getWebsiteTypeObj(), bonusTurnoverId);
	//
	//		if (null == result) {
	//			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	//			return;
	//		}
	//		ResponseUtils.sendJsonResponse(response, result);
	//	}

	private void getNewPassword(
		HttpServletResponse response)
		throws Exception {

		String password = AccountUtils.generatePlayerPassword();

		// 暫時註解，使用3DES所產生的密碼會與前端密碼規則衝突(包含了符號 如:+,=)
		String encodePassword =
			EncryptUtil.encrypt3DESToBase64(password, SystemConstants.PASSWORD_PROTECTION_KEY);
		ResponseUtils.respondSuccessWithMessage(response, encodePassword);
	}

	public static void updatePassword(HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang) throws Exception {
		final WebSiteType websiteTypeObj = manager.getWebsiteTypeObj();
		String password = null;
		password = RequestParser.getStringParameter(request, 20, "password", null);
		String confirmPassword =
			RequestParser.getStringParameter(request, 20, "confirmPassword", null);

		if (StringUtils.isEmpty(password)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.password"));
		}

		if (StringUtils.isEmpty(confirmPassword)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.password"));
		}

		if (!password.equals(confirmPassword)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.password"));
		}

		if (!Validator.isValidatedPlayerStrictPassword(password)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.password"));
		}

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		Account account = AccountBO.getAccountByUserId(userId, websiteTypeObj);
		if (account == null) {
			throw new Deviation()
				.setI18N("msg.error.account.accountNotFound");
		}

		account.setPassword(password);
		boolean success =
			UserBO.changePassword(
				null,
				password,
				manager.getWebsiteTypeObj(),
				account,
				AccountUpdateType.UPDATE_PASSWORD,
				manager.getUserId(),
				HostAddressUtils.getRealIPAddresses(request)
			);

		if (success) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void updateStatus(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang)
		throws Exception {

		final WebSiteType websiteTypeObj = manager.getWebsiteTypeObj();
		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		int status = RequestParser.getIntParameter(request, 1, "status");
		AccountStatusType accountStatusType = AccountStatusType.getInstanceOf(status);
		if (null == accountStatusType) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.status"));
		}

		Account account = AccountBO.getAccountByUserId(userId, websiteTypeObj);
		if (status == account.getStatus()) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		boolean success =
			AccountBO.updateStatus(
				websiteTypeObj, account, status, manager, HostAddressUtils.getRealIPAddresses(request));

		if (success) {
			String userKey = AccountUtils.getUserKey(websiteTypeObj.unique(), userId);
			PlayerBO.logoutByBO(userKey);
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	//	private void updateRiskRemark(
	//		HttpServletRequest request,
	//		HttpServletResponse response,
	//		Manager manager,
	//		LangMessage lang)
	//		throws Exception {
	//
	//		final WebSiteType websiteTypeObj = manager.getWebsiteTypeObj();
	//		String userId =
	//			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
	//
	//		String riskRemark = RequestParser.getStringParameter(request, 1001, "riskRemark", null);
	//		if (null != riskRemark && riskRemark.getBytes(StandardCharsets.UTF_8).length > 1000) {
	//			throw new Deviation()
	//				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.riskRemark"));
	//		}
	//
	//		AccountRemark accountRiskRemark = new AccountRemark();
	//		accountRiskRemark.setWebsiteType(websiteTypeObj.unique());
	//		accountRiskRemark.setUserId(userId);
	//		accountRiskRemark.setRemark(StringUtil.escapeHtmlContent(riskRemark));
	//		accountRiskRemark.setRemarkType(AccountRemarkType.riskRemark.unique());
	//		accountRiskRemark.setUpdater(manager.getUserId());
	//
	//		AccountRemarkBO.upsertAccountRemark(
	//			websiteTypeObj.unique(),
	//			userId,
	//			HostAddressUtils.getRealIPAddresses(request),
	//			accountRiskRemark);
	//
	//		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	//	}

	//	private void updateProfileContact(
	//		HttpServletRequest request,
	//		HttpServletResponse response,
	//		HttpSession session,
	//		Manager manager,
	//		LangMessage lang)
	//		throws Exception {
	//
	//		WebSiteType webSiteType = manager.getWebsiteTypeObj();
	//		//		boolean enableShowContactPhone =
	//		//			FrontendUtils.isValidatedRole(session, FunctionType.MEMBER_SEARCH_SHOW_CONTACT_PHONE);
	//
	//		String userId =
	//			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
	//		if (!Validator.isValidatedUserId(userId)) {
	//			throw new Deviation()
	//				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
	//		}
	//
	//		int currencyType = RequestParser.getIntParameter(request, 2, "currencyType");
	//
	//		CurrencyType currencyTypeObj = CurrencyType.getInstance(currencyType);
	//
	//		Map<AccountContactInfoKey, String> contactInfoMap = new HashMap<>();
	//
	//		for (ContactType contactType : ContactType.VALUES) {
	//
	//			boolean enableEditContactData = true;
	//			String contactData =
	//				enableEditContactData
	//					? RequestParser.getStringParameter(request, 3000, contactType.getName(), null)
	//					: null;
	//			if (contactData != null) {
	//
	//				if (contactType == ContactType.Email) {
	//					contactData = contactData.toLowerCase();
	//				}
	//
	//				if (!contactType.isValidated(contactData, null)) {
	//					throw new Deviation()
	//						.setI18N(
	//							"msg.error.validation.fieldNotValid",
	//							lang.get(contactType.getDisplayName(), new String[] {""}));
	//
	//				}
	//			}
	//
	//			contactInfoMap.put(new AccountContactInfoKey(contactType.unique(), 1), contactData);
	//		}
	//
	//		if (contactInfoMap.isEmpty()) {
	//			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	//			return;
	//		}
	//
	//		ManagerRole roleInCache =
	//			ManagerCache.getInstance().getManager(webSiteType, manager.getUserId()).getManagerRole();
	//
	//		boolean result =
	//			AccountBO.updateProfileContact(
	//				contactInfoMap,
	//				userId,
	//				webSiteType,
	//				manager.getUserId(),
	//				HostAddressUtils.getRealIPAddresses(request),
	//				currencyTypeObj.unique(),
	//				lang);
	//
	//		if (result) {
	//			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	//			return;
	//		}
	//		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	//	}

	//	private void updateProfileAddress(
	//		HttpServletRequest request,
	//		HttpServletResponse response,
	//		HttpSession session,
	//		Manager manager,
	//		LangMessage lang
	//	) throws Exception {
	//		WebSiteType webSiteType = manager.getWebsiteTypeObj();
	//		String userId =
	//			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
	//		if (!Validator.isValidatedUserId(userId)) {
	//			throw new Deviation()
	//				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
	//		}
	//		int currencyType = RequestParser.getIntParameter(request, 2, "currencyType");
	//		CurrencyType currencyTypeObj = CurrencyType.getInstance(currencyType);
	//		ManagerRole roleInCache =
	//			ManagerCache.getInstance().getManager(webSiteType, manager.getUserId()).getManagerRole();
	//		String addressFromRequest = RequestParser.getStringParameter(request, 1000, "address");
	//		String remark = RequestParser.getStringParameter(request, 1000, "userRemark", "");
	//
	//		boolean success = AccountContactInfoBO.updateProfileAddress(addressFromRequest, userId, webSiteType,
	//			manager.getUserId(),
	//			HostAddressUtils.getRealIPAddresses(request),
	//			currencyTypeObj.unique());
	//
	//		if (success && remark != null) {
	//			List<AccountRemark> accountRemarkList = new ArrayList<>();
	//
	//			AccountRemark accountUserRemark = new AccountRemark();
	//			accountUserRemark.setWebsiteType(webSiteType.unique());
	//			accountUserRemark.setUserId(userId);
	//			accountUserRemark.setRemark(StringUtil.escapeHtmlContent(remark));
	//			accountUserRemark.setRemarkType(AccountRemarkType.userRemark.unique());
	//			accountUserRemark.setUpdater(manager.getUserId());
	//
	//			accountRemarkList.add(accountUserRemark);
	//
	//			AccountRemarkBO.upsertAccountRemark(
	//				webSiteType.unique(),
	//				userId,
	//				HostAddressUtils.getRealIPAddresses(request),
	//				accountRemarkList);
	//		}
	//		if (success) {
	//			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	//			return;
	//		}
	//
	//		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	//
	//	}

	private void verifyProfileContact(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpSession session,
		Manager manager,
		LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int verifycontactType = RequestParser.getIntParameter(request, "contactType", 0);
		if (verifycontactType == 0) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.contactType"));
		}

		ContactType contactType = ContactType.getInstanceOf(verifycontactType);

		int contentNo = RequestParser.getIntParameter(request, "contentNo", 0);
		if (contentNo == 0) {
			throw new Deviation();
		}

		boolean result =
			AccountContactInfoBO.updateAsVerifiedByBO(
				manager.getWebsiteType(), userId, manager.getUserId(), contactType, contentNo,
				HostAddressUtils.getRealIPAddresses(request), CurrencyType.EUR.unique());

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void searchProfileUpdateLog(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchDateRange", null);

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

		// Access right

		int type = RequestParser.getIntParameter(request, "updateType", -99);

		List<AccountUpdateType> accessUpdateTypeOption = AccountUpdateType.getAll();

		int currencyTypeId = RequestParser.getIntParameter(request, "currencyTypeId");

		String sortConditionParam = request.getParameter("sortCondition");

		String sortCondition = null;

		DBOrderType orderType = null;

		if (sortConditionParam != null && !sortConditionParam.trim().isEmpty()) {

			int sortConditionInt = RequestParser.getIntParameter(request, 1, "sortCondition");

			if (sortConditionInt < 1 || sortConditionInt == 2 || sortConditionInt == 3) {

				throw new Deviation()

					.setI18N(

						"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));

			}

			sortCondition = switch (sortConditionInt) {
				case 1 -> "logTypeStr";
				case 4 -> "updater";
				case 5 -> "updateTime";
				case 6 -> "updaterIp";
				default -> null;
			};

		}

		String sortOrderParam = request.getParameter("sortOrder");

		if (sortOrderParam != null && !sortOrderParam.trim().isEmpty()) {

			int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

			orderType = DBOrderType.getInstanceOf(sortOrderInt);

		} else {

			// Default order type if not provided, assuming DBOrderType.getInstanceOf(0) provides a sensible default (e.g., ASC).

			orderType = DBOrderType.getInstanceOf(0);

		}

		int pageNumber = RequestParser.getIntParameter(request, "pageNumber", 1);

		int showCount = RequestParser.getIntParameter(request, "pageSize", SystemConstants.PAGE_SIZE);

		if (showCount <= 0) {

			throw new Deviation("Invalid page size");

		}        //		ManagerRole roleInCache =
		//			ManagerCache.getInstance()
		//				.getManager(manager.getWebsiteTypeObj(), manager.getUserId())
		//				.getManagerRole();

		String data =
			AccountUpdateLogBO.getFullAccountUpdateLog(
				userId,
				startTime,
				endTime,
				type,
				accessUpdateTypeOption,
				manager.getCurrencyTypeIdList(),
				manager.getWebsiteTypeObj(),
				sortCondition,
				pageNumber,
				showCount,
				orderType,
				lang);

		ResponseUtils.sendJsonResponse(response, data);
	}

	private void searchAllUserId(
		HttpServletRequest request, HttpServletResponse response, Manager manager) {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "search");

		String selectUserId =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "selectUserId", null);
		List<String> selectUserIdList =
			null == selectUserId
				? new ArrayList<>()
				: Arrays.stream(selectUserId.split(",")).collect(Collectors.toList());

		int currency = RequestParser.getIntParameter(request, 2, "currencyType");

		Set<Integer> currencySet = new HashSet<>();
		if (currency != -1) {
			currencySet.add(currency);
		} else {
			currencySet.addAll(manager.getCurrencyTypeIdList());
		}
		ResponseUtils.sendJsonResponse(
			response,
			AccountBO.searchUserIdByCurrency(
				manager.getWebsiteTypeObj(), userId, currencySet, selectUserIdList));
	}

	public static void searchProfileLoginLog(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int currencyTypeId = RequestParser.getIntParameter(request, 2, "currencyTypeId", -1);

		if (currencyTypeId <= 0) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.currencyType"));
		}

		CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchDateRange", null);

		Timestamp startTime = null;
		Timestamp endTime = null;
		if (dateRange != null) {

			Date startDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			startTime = new Timestamp(startDate.getTime());

			Date endDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
			endTime = new Timestamp(endDate.getTime());
		} else {
			Date now = new Date();
			Date oneMonthAgo = DateUtils.getNextNMonth(now, -1);

			startTime = DateTimeBuilder.localDateTime(oneMonthAgo.getTime()).withMinTime().toTimestamp();
			endTime = TimePeriodType.TODAY.getDuration()[1];
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 1 || sortConditionInt > 10) {
			throw new Deviation("Sort Condition is invalid");
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 1 -> "IP";
			case 2 -> "LOGIN_DATE";
			case 3 -> "COUNTRY";
			case 4 -> "DEVICE_TYPE";
			case 5 -> "USER_AGEN_TYPE";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, SystemConstants.PAGE_SIZE, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data =
			AccountTrackerBO.searchProfileLoginLog(
				userId,
				startTime,
				endTime,
				manager.getWebsiteTypeObj(),
				sortCondition,
				orderType,
				pageInfo);

		ResponseUtils.sendJsonResponse(response, data);
	}

	public static void searchProfileReportBetSettled(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int transactionType = RequestParser.getIntParameter(request, 2, "transactionType");
		AccountSummaryReportType accountSummaryReportType = null;
		if (transactionType != -1) {
			accountSummaryReportType = AccountSummaryReportType.getInstanceOf(transactionType);
		}

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchDateRange", null);

		Date startDate = null;
		Date endDate = null;

		if (dateRange != null) {
			startDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			endDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 1 || sortConditionInt > 4) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid",
				lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 1 -> "transaction_time";
			case 2 -> "amount";
			case 3 -> "profit";
			case 4 -> "turnover";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = AccountSummaryReportBO.getAccountSummaryReport(userId, manager.getWebsiteTypeObj(),
			accountSummaryReportType, startDate, endDate, sortCondition, orderType, pageInfo);

		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	/**
	 * 依日期區間撈取 GameTransactionSummary（不依 vendor / game_type 分組，每日一列）。
	 * 入參格式同 {@link #searchProfileReportBetSettled}：searchDateRange = "dd/MM/yyyy HH:mm:ss-dd/MM/yyyy HH:mm:ss"。
	 * 回傳結構同 settled：每筆含 transactionTime / transactionTimeStr / paymentType / amount / profit / turnover。
	 */
	public static void searchProfileReportBetSummary(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchDateRange", null);

		Date startDate = null;
		Date endDate = null;

		if (dateRange != null) {
			startDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			endDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
		} else {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid",
					lang.get("form.text.backOffice.report.transactionDateRange"));
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 1 || sortConditionInt > 4) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid",
				lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 1 -> "summary_date";
			case 2 -> "amount";
			case 3 -> "profit";
			case 4 -> "turnover";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = GameTransactionBO.getBetSummaryByDateRange(userId, manager.getWebsiteTypeObj(),
			startDate, endDate, sortCondition, orderType, pageInfo);

		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	public static void searchProfileReportBetUnsettled(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchDateRange", null);

		Date startDate = null;
		Date endDate = null;

		if (dateRange != null) {
			startDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[0]);
			endDate = FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, dateRange[1]);
		} else {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid",
					lang.get("form.text.backOffice.report.transactionDateRange"));
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 1 || sortConditionInt > 8) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid",
				lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 1 -> "txn_time";
			case 2 -> "g.create_time";
			case 3 -> "g.vendor_id";
			case 4 -> "g.game_type";
			case 5 -> "game_name";
			case 6 -> "odds";
			case 7 -> "odds_type";
			case 8 -> "bet_amount";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = GameTransactionBO.getUnsettledBetDetailsByMultiCondition(userId, manager.getWebsiteTypeObj(),
			startDate, endDate, sortCondition, orderType, pageInfo, lang, null);

		ResponseUtils.sendJsonResponse(response, data);
	}

	public static void searchBetSummaryDetails(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String transactionDate = RequestParser.getStringParameter(request, 10, "transactionDate");
		Timestamp dateTime = new Timestamp(Long.parseLong(transactionDate) * 1000);
		String date = DateUtils.toString(dateTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
		if (!Validator.isValidatedDate(date)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid",
					lang.get("form.text.backOffice.payment.transactionDate"));
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 0 || sortConditionInt > 5) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 0 -> "summary_date";
			case 1 -> "vendor_id";
			case 2 -> "game_type";
			case 3 -> "sum_bet_amount";
			case 4 -> "profit";
			case 5 -> "turnover";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = GameTransactionSummaryBO.getBetSummaryDetails(userId, manager.getWebsiteTypeObj(),
			null, null, date, date, sortCondition, orderType, pageInfo, lang);

		ResponseUtils.sendJsonResponse(response, data);
	}

	/**
	 * 與 {@link #searchBetSummaryDetails} 同 shape，但資料源改為從 GAMETRANSACTION 即時聚合
	 * （不依賴 GAMETRANSACTIONSUMMARY 預先彙總的結果）。
	 */
	public static void searchBetSummaryDetailsFromTransaction(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String transactionDate = RequestParser.getStringParameter(request, 10, "transactionDate");
		Timestamp dateTime = new Timestamp(Long.parseLong(transactionDate) * 1000);
		String date = DateUtils.toString(dateTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
		if (!Validator.isValidatedDate(date)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid",
					lang.get("form.text.backOffice.payment.transactionDate"));
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 0 || sortConditionInt > 5) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 0 -> "summary_date";
			case 1 -> "vendor_id";
			case 2 -> "game_type";
			case 3 -> "sum_bet_amount";
			case 4 -> "profit";
			case 5 -> "turnover";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = GameTransactionBO.getBetSummaryDetailsFromTransaction(userId, manager.getWebsiteTypeObj(),
			null, null, date, date, sortCondition, orderType, pageInfo, lang);

		ResponseUtils.sendJsonResponse(response, data);
	}

	public static void searchBetDetails(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String transactionDate = RequestParser.getStringParameter(request, 10, "transactionDate");
		Timestamp dateTime = new Timestamp(Long.parseLong(transactionDate) * 1000);
		String date = DateUtils.toString(dateTime, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
		if (!Validator.isValidatedDate(date)) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid",
					lang.get("form.text.backOffice.payment.transactionDate"));
		}

		int vendorId = RequestParser.getIntParameter(request, 9, "vendorId");
		if (null == VendorCache.getInstance().getVendor(vendorId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("ui.text.slot.vendor_group"));
		}
		String inputGameType = RequestParser.getStringParameter(request, 9, "gameType", null);
		Integer gameType = null;
		if (null != inputGameType) {
			gameType = Integer.parseInt(inputGameType);
			GameType.getInstance(gameType);
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 0 || sortConditionInt > 12) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = switch (sortConditionInt) {
			case 0 -> "txn_time";
			case 1 -> "settle_time";
			case 2 -> "create_time";
			case 3 -> "vendor_id";
			case 4 -> "game_type";
			case 5 -> "game_id";
			case 6 -> "odds";
			case 7 -> "odds_type";
			case 8 -> "balance_before";
			case 9 -> "bet_amount";
			case 10 -> "profit_loss";
			case 11 -> "balance_after";
			case 12 -> "turnover";
			default -> null;
		};

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data = GameTransactionBO.getNotUnsettledBetDetailsByMultiCondition(userId, manager.getWebsiteTypeObj(),
			vendorId, gameType, date, sortCondition, orderType, pageInfo, lang, null);

		ResponseUtils.sendJsonResponse(response, data);
	}

	private void searchGameTransactionDetail(
		HttpServletRequest request, HttpServletResponse response, Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		long id = RequestParser.getLongParameter(request, "id");

		String txnDate = RequestParser.getStringParameter(request, 10, "txnDate");

		String settleDate = RequestParser.getStringParameter(request, 10, "settleDate", null);

		Timestamp txnTime = new Timestamp(Long.parseLong(txnDate) * 1000);

		Timestamp settleTime = null;
		if (!StringUtil.isEmpty(settleDate) && !settleDate.equals("0")) {
			settleTime = new Timestamp(Long.parseLong(settleDate) * 1000);
		}

		String gameTxnJson = GameTransactionBO.getGameTxnByUserId(id, userId, manager.getWebsiteTypeObj(),
			txnTime, settleTime, lang);

		if (gameTxnJson == null) {
			throw new Deviation("msg.error.validation.notFindGameRecord");
		}

		ResponseUtils.sendJsonResponse(response, gameTxnJson);
	}

	// TODO: js 那邊應該可以判斷是 insert or update。之後應該調整 js
	private static void updateUserRemark(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang
	) throws Exception {
		WebSiteType webSiteType = manager.getWebsiteTypeObj();
		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		String remark = RequestParser.getStringParameter(request, 3000, "userRemark", "");

		AccountRemark accountUserRemark = new AccountRemark();
		accountUserRemark.setWebsiteType(webSiteType.unique());
		accountUserRemark.setUserId(userId);
		accountUserRemark.setRemark(StringUtil.escapeHtmlContent(remark));
		accountUserRemark.setRemarkType(AccountRemarkType.userRemark.unique());
		accountUserRemark.setUpdater(manager.getUserId());

		AccountRemarkBO.insertOrUpdate(
			webSiteType.unique(),
			userId,
			HostAddressUtils.getRealIPAddresses(request),
			accountUserRemark);

		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	}

	private boolean isBlankOrNull(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static void updateKycStatus(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang
	) throws Exception {
		WebSiteType webSiteType = manager.getWebsiteTypeObj();
		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "kycUserId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int kycStatus = RequestParser.getIntParameter(request, 2, "kycStatus");

		CurrencyType currencyType = CurrencyType.getInstance(
			RequestParser.getIntParameter(request, 2, "currencyTypeId", -1));

		List<AccountDocument> accountDocumentList = AccountDocumentBO.findAccountDocuments(userId, webSiteType);
		Optional<AccountDocument> accountDocument = accountDocumentList.stream()
			.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
			.max(Comparator.comparing(AccountDocument::getId));

		if (accountDocument.isEmpty()) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get("ui.error.message.sumsub.doc.not.found"));
			return;
		}

		KycPersonalInfo kycPersonalInfo = KycPersonalInfoBO.find(accountDocument.get().getId(), userId, webSiteType);

		if (kycPersonalInfo == null) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
			return;
		}

		if (accountDocument.get().getStatus() == kycStatus) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		// check if the document is filled in completely
		// either one of the field is empty, do not allow user to update KYC status

		if (!AccountDocumentBO.checkKYCCompleteInformation(kycPersonalInfo, accountDocument.get())) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get("ui.error.message.sumsub.doc.not.found"));
			return;
		}

		boolean result = AccountDocumentBO.updateKycStatus(userId, webSiteType, DocumentType.SUMSUB_KYC,
			DocumentStatusType.getInstance(kycStatus),
			manager.getUserId(), HostAddressUtils.getRealIPAddresses(request), currencyType, accountDocument.get()
		);

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
		} else {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

	}

	public static void editIdDocument(HttpServletRequest request, HttpServletResponse response, HttpSession session,
		Manager manager, LangMessage lang) throws Exception {
		LogUtils.accountDocument.info("starts ");

		WebSiteType webSiteType = manager.getWebsiteTypeObj();
		String managerUserId = manager.getUserId();

		LogUtils.accountDocument.info("get file item map: ... ");

		// Validate userId
		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		CurrencyType currencyType = CurrencyType.getInstance(
			RequestParser.getIntParameter(request, 2, "currencyTypeId", -1));
		// Mandatory fields validation
		String documentNo = RequestParser.getStringParameter(request, 50, "documentNo");

		String fullName = RequestParser.getStringParameter(request, 101, "fullName");

		// Date of birth validation (must be 18+ years old)
		LocalDateTime birthday = RequestParser
			.getLocalDateTimeParameter(request, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy, "dob");

		if (Period.between(birthday.toLocalDate(), LocalDate.now()).getYears() < 18) {
			throw new Deviation(lang.get("msg.error.account.birthday.isNotValidated"));
		}
		Timestamp dobTimestamp = DateTimeBuilder.localDateTime(birthday).withMinTime().toTimestamp();

		LocalDateTime expiryDate = RequestParser
			.getLocalDateTimeParameter(request, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy, "expiryDate");

		Timestamp expiryTimestamp = DateTimeBuilder.localDateTime(expiryDate).withMinTime().toTimestamp();

		// Address fields validation
		String street = RequestParser.getStringParameter(request, 200, "street");

		String city = RequestParser.getStringParameter(request, 100, "city");

		String postalCode = RequestParser.getStringParameter(request, 20, "postalCode");

		boolean frontPhotoChanged = RequestParser.getBooleanParameter(request, "frontPhotoChanged", false);
		boolean backPhotoChanged = RequestParser.getBooleanParameter(request, "backPhotoChanged", false);
		boolean addressImageChanged = RequestParser.getBooleanParameter(request, "addressImageChanged", false);

		// Optional verification remark
		String verificationRemark = RequestParser.getStringParameter(request, 1000, "verificationRemark", "");

		// 1. Save uploaded images to file system
		String frontImageExtension = null;
		String backImageExtension = null;
		String addressImageExtension = null;

		Part frontPhotoFileItem = null;
		Part backPhotoFileItem = null;
		Part addressPhotoFileItem = null;

		// upload image & set image path
		if (frontPhotoChanged) {
			frontPhotoFileItem = RequestParser.getPart(request, "frontPhotoFile");
			if (frontPhotoFileItem != null) {
				if (!isValidFileName(frontPhotoFileItem.getSubmittedFileName())) {
					throw new Deviation("Upload failed: Invalid file name format.");
				}
				LogUtils.accountDocument.info("validate file extension: frontPhotoFile");
				frontImageExtension = ImageType.getSupportedFileExtension(frontPhotoFileItem.getSubmittedFileName());
				LogUtils.accountDocument.info("validate image data: frontPhotoFile");
				validateImageData(frontPhotoFileItem);
			}
		}

		if (backPhotoChanged) {
			backPhotoFileItem = RequestParser.getPart(request, "backPhotoFile");
			if (backPhotoFileItem != null) {
				if (!isValidFileName(backPhotoFileItem.getSubmittedFileName())) {
					throw new Deviation("Upload failed: Invalid file name format.");
				}
				LogUtils.accountDocument.info("validate file extension: backPhotoFile");
				backImageExtension = ImageType.getSupportedFileExtension(backPhotoFileItem.getSubmittedFileName());
				LogUtils.accountDocument.info("validate image data: backPhotoFile");
				validateImageData(backPhotoFileItem);
			}
		}

		if (addressImageChanged) {
			addressPhotoFileItem = RequestParser.getPart(request, "addressPhotoFile");
			if (addressPhotoFileItem != null) {
				if (!isValidFileName(addressPhotoFileItem.getSubmittedFileName())) {
					throw new Deviation("Upload failed: Invalid file name format.");
				}
				LogUtils.accountDocument.info("validate file extension: addressPhotoFile");
				addressImageExtension = ImageType.getSupportedFileExtension(
					addressPhotoFileItem.getSubmittedFileName());
				LogUtils.accountDocument.info("validate image data: addressPhotoFile");
				validateImageData(addressPhotoFileItem);
			}
		}

		AccountDocument accountDocument =
			AccountDocumentBO.findAccountDocuments(userId, webSiteType)
				.stream()
				.filter(doc -> doc.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
				// TODO: find newest 1 record?
				.max(Comparator.comparing(AccountDocument::getId))
				.orElse(null);

		if (accountDocument == null) {
			accountDocument = new AccountDocument();
			accountDocument.setWebsiteType(webSiteType.unique());
			accountDocument.setUserId(userId);
			accountDocument.setDocumentType(DocumentType.SUMSUB_KYC.unique());
			accountDocument.setGroupType(DocumentGroupType.DOCUMENT.unique());
			accountDocument.setDocumentIndex(1);
			accountDocument.setStatus(DocumentStatusType.APPROVED.unique());
			accountDocument.setCurrencyTypeId(currencyType.unique());
			accountDocument.setApprovedRemark(verificationRemark);
			accountDocument.setCreator(userId);

			if (frontPhotoFileItem != null) {
				accountDocument.setOriginalFrontImage(frontPhotoFileItem.getInputStream().readAllBytes());
				accountDocument.setFrontImageExtension(frontImageExtension);
			}

			if (backPhotoFileItem != null) {
				accountDocument.setOriginalBackImage(backPhotoFileItem.getInputStream().readAllBytes());
				accountDocument.setFrontImageExtension(backImageExtension);
			}

			if (addressPhotoFileItem != null) {
				accountDocument.setOriginalAddressImage(addressPhotoFileItem.getInputStream().readAllBytes());
				accountDocument.setAddressImageExtension(addressImageExtension);
			}

			accountDocument.setExpiredDate(expiryTimestamp);

			accountDocument.setId(AccountDocumentBO.apply(userId, webSiteType, accountDocument));

			LogUtils.accountDocument.info("inserted to account document table");

			// images
			// verification remarks
			List<AccountUpdateLog> accountUpdateLogList = new ArrayList<>();

			List<Object[]> updates = List.of(
				new Object[] {AccountUpdateType.DOCUMENT_FRONT_PHOTO, "Document Front Image",
					"-", accountDocument.getFrontImagePath()},
				new Object[] {AccountUpdateType.DOCUMENT_BACK_PHOTO, "Document Back Image",
					"-", accountDocument.getBackImagePath()},
				new Object[] {AccountUpdateType.DOCUMENT_ADDRESS_PHOTO, "Residence Image",
					"-", accountDocument.getResidenceImagePath()},
				new Object[] {AccountUpdateType.KYC_VERIFICATION_REMARK, "Verification Remark",
					"-", accountDocument.getApprovedRemark()},
				new Object[] {AccountUpdateType.DOCUMENT_EXPIRY_DATE, "Expiry Date",
					"-", accountDocument.getExpiredDate()
				},
				new Object[] {AccountUpdateType.KYC_VERIFICATION_STATUS, "Verification Status",
					"-", accountDocument.getStatus()
				}
			);

			updates.forEach(update -> {
				AccountUpdateType type = (AccountUpdateType) update[0];
				String fieldName = (String) update[1];
				String before = update[2] != null ? Objects.toString(update[2]) : null;
				String after = update[3] != null ? Objects.toString(update[3]) : null;

				if (after != null && !after.equals(before)) {
					accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(
						userId, webSiteType.unique(), type,
						new UpdateRecord(before, after, "add" + fieldName + "."),
						managerUserId, HostAddressUtils.getRealIPAddresses(request), currencyType.unique()));
				}
			});

			if (!accountUpdateLogList.isEmpty()) {
				AccountUpdateLogBO.batchInsert(accountUpdateLogList);

				LogUtils.accountDocument.info("inserted to account update log table");
			}

		} else {

			AccountDocumentBO.updateRemarkOrImagePathOrExpiryDate(
				accountDocument,
				frontPhotoFileItem,
				backPhotoFileItem,
				addressPhotoFileItem,
				managerUserId, verificationRemark, HostAddressUtils.getRealIPAddresses(request),
				currencyType.unique(),
				expiryTimestamp,
				frontPhotoChanged,
				backPhotoChanged,
				addressImageChanged
			);

		}

		List<AccountUpdateLog> accountUpdateLogList = new ArrayList<>();
		// 2. Update KYC personal information
		KycPersonalInfo kycPersonalInfo = new KycPersonalInfo();
		kycPersonalInfo.setAccountDocumentId(accountDocument.getId());
		kycPersonalInfo.setUserId(userId);
		kycPersonalInfo.setWebsiteType(webSiteType.unique());
		kycPersonalInfo.setDocumentNo(documentNo);
		kycPersonalInfo.setFirstName(extractFirstName(fullName));
		kycPersonalInfo.setLastName(extractLastName(fullName));
		kycPersonalInfo.setDob(dobTimestamp);
		kycPersonalInfo.setStreet(street);
		kycPersonalInfo.setCity(city);
		kycPersonalInfo.setPostalCode(postalCode);
		kycPersonalInfo.setCountry(""); // Country not provided in current requirements
		kycPersonalInfo.setCreator(managerUserId);
		kycPersonalInfo.setUpdater(managerUserId);

		if (KycPersonalInfoBO.isExistRecord(accountDocument.getId(), webSiteType, userId)) {

			KycPersonalInfoBO.update(kycPersonalInfo, currencyType, accountUpdateLogList, managerUserId,
				HostAddressUtils.getRealIPAddresses(request));

			AccountUpdateLogBO.batchInsert(accountUpdateLogList);
			LogUtils.accountDocument.info("Kyc personal info:  Update account log table");

		} else {
			KycPersonalInfoBO.insert(kycPersonalInfo);
			LogUtils.accountDocument.info("insert Kyc personal info");

			// document no
			// full name
			// address
			List<Object[]> updates = List.of(
				new Object[] {AccountUpdateType.DOCUMENT_NO, "Insert Document No",
					"-", documentNo},
				new Object[] {AccountUpdateType.FULL_NAME, "Insert Full Name",
					"-", fullName},
				new Object[] {AccountUpdateType.ADDRESS, "Insert Address",
					"-", JSONUtils.getJSONString("Street", street, "City", city, "Postal code", postalCode)},
				new Object[] {AccountUpdateType.DOB, "Insert DOB",
					"-", kycPersonalInfo.getDobStr()
				}
			);

			updates.forEach(update -> {
				AccountUpdateType type = (AccountUpdateType) update[0];
				String fieldName = (String) update[1];
				String before = update[2].toString();
				String after = Objects.toString(update[3]);
				accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(
					kycPersonalInfo.getUserId(), webSiteType.unique(), type,
					new UpdateRecord(before, after, fieldName),
					managerUserId, HostAddressUtils.getRealIPAddresses(request), currencyType.unique()));

			});
			AccountUpdateLogBO.batchInsert(accountUpdateLogList);
			LogUtils.accountDocument.info("insert Kyc personal info to update account log table");
		}
		LogUtils.accountDocument.info("end");

		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	}

	private static String extractFirstName(String fullName) {
		if (StringUtils.isEmpty(fullName)) {
			return "";
		}
		String[] nameParts = fullName.trim().split("\\s+");
		return nameParts[0];
	}

	private static String extractLastName(String fullName) {
		if (StringUtils.isEmpty(fullName)) {
			return "";
		}
		String[] nameParts = fullName.trim().split("\\s+");
		if (nameParts.length > 1) {
			// Join all parts except the first as last name
			return String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length));
		}
		return ""; // Only one name provided, no last name
	}

	private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

	// Validate image size and type
	private static void validateImageData(Part part) throws Exception {
		// Check file size
		if (part.getSize() > MAX_FILE_SIZE) {
			throw new Deviation("Upload failed: Only JPG, PNG, HEIC, WEBP or PDF files under 50 MB are allowed.");
		}

		// 讀取檔案前12個byte做magic number檢查
		byte[] fileHeader = new byte[12];
		try (InputStream is = part.getInputStream()) {
			// 讀取最多12 byte，寫入到 fileHeader 陣列的前 12 個位置，實際讀取到的位元組數量會存到 read 變數中
			int read = is.read(fileHeader, 0, 12);
			// 至少 4 個位元組：常見檔案簽章（例如 PNG、JPEG、PDF 等）需要前幾個位元組來辨識，太少無法判定，視為無效。
			if (read < 4) {
				throw new Deviation("Upload failed: Invalid file format.");
			}
		}

		if (!ImageType.isSupportedImageType(fileHeader)) {
			throw new Deviation("Upload failed: Only JPG, PNG, HEIC, WEBP or PDF files are allowed.");
		}
	}

	// invalid_file_name_with_special_chars!@#.exec
	private static boolean isValidFileName(String fileName) {
		// Check length (reasonable limit)
		if (fileName.length() > 255) {
			return false;
		}

		// Check for invalid characters (Windows + Unix reserved characters)
		String invalidChars = "<>:\"/\\|?*";

		for (char c : invalidChars.toCharArray()) {
			if (fileName.indexOf(c) >= 0) {
				return false;
			}
		}

		// Check for control characters (ASCII 0-31)
		for (char c : fileName.toCharArray()) {
			if (c <= 31) {
				return false;
			}
		}

		return true;
	}

	private void viewSumsubDocument(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		AccountDocument accountDocument = AccountDocumentBO.findAccountDocuments(userId, manager.getWebsiteTypeObj())
			.stream()
			.filter(o -> o.getDocumentType() == DocumentType.SUMSUB_KYC.unique())
			.findFirst()
			.orElse(null);

		JsonGenerateProcessor processor = jGenerator -> {
			jGenerator.writeObjectFieldStart("image");
			if (accountDocument != null) {
				String frontImagePath = accountDocument.getFrontImagePath();
				String backImagePath = accountDocument.getBackImagePath();
				String residenceImagePath = accountDocument.getResidenceImagePath();

				if (StringUtils.isNotEmpty(frontImagePath)) {
					jGenerator.writeStringField("front",
						Base64.encodeBase64String(FileUtils.fileToByte(new File(frontImagePath))));
				}
				if (StringUtils.isNotEmpty(backImagePath)) {
					jGenerator.writeStringField("back",
						Base64.encodeBase64String(FileUtils.fileToByte(new File(backImagePath))));
				}
				if (StringUtils.isNotEmpty(residenceImagePath)) {
					jGenerator.writeStringField("residence",
						Base64.encodeBase64String(FileUtils.fileToByte(new File(residenceImagePath))));
				}
			}
			jGenerator.writeEndObject();
		};

		ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString(processor));
	}

	private void viewImageForUpdateLog(
		HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang
	) throws Exception {
		String imagePath = RequestParser.getStringParameter(request, 1000, "imagePath");

		if (imagePath != null) {

			JsonGenerateProcessor processor = jGenerator -> {
				jGenerator.writeStringField("path",
					Base64.encodeBase64String(FileUtils.fileToByte(new File(imagePath))));
			};

			ResponseUtils.sendJsonResponse(response, JSONUtils.getJSONString(processor));
			return;

		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	public static void getPlayerResponsibility(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang
	) throws Exception {
		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		final WebSiteType websiteTypeObj = manager.getWebsiteTypeObj();

		String result = AccountPlayResponsiblySettingBO.getPlayerAllResponsibilitiesWithJGenerator(userId,
			websiteTypeObj);

		ResponseUtils.sendJsonResponse(response, result);
	}

	public static void updatePlayerResponsibility(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang
	) throws Exception {
		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int playerResponsibilityType = RequestParser.getIntParameter(request, 1, "playerResponsibilityType");

		String accountPlayResponsiblyListFromRequest = RequestParser.getStringParameter(request, 1000,
			"responsibleList");
		List<AccountPlayResponsiblySetting> accountPlayResponsiblySettingList = JSONUtils.parseJsonToObjectList(
			accountPlayResponsiblyListFromRequest, AccountPlayResponsiblySetting.class);

		AccountPlayResponsiblyType type = AccountPlayResponsiblyType.getInstanceOf(playerResponsibilityType);
		AccountPlayResponsiblySettingBO.validateRequest(type, accountPlayResponsiblySettingList);

		boolean result = AccountPlayResponsiblySettingBO.update(userId,
			type,
			accountPlayResponsiblySettingList,
			manager.getUserId(),
			HostAddressUtils.getRealIPAddresses(request),
			true, false);

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));

	}

	public static void cancelPlayerResponsibility(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");

		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		int playerResponsibilityType = RequestParser.getIntParameter(request, 1, "playerResponsibilityType");
		int periodType = RequestParser.getIntParameter(request, 2, "periodType", -1);

		// TODO: playerResponsibilityType 的 getInstanceOf() 可能回傳 null，會導致 switch(type) NPE 而非 Deviation；periodType 在 BO 層已有 null 檢查
		boolean result = AccountPlayResponsiblySettingBO.cancel(userId,
			AccountPlayResponsiblyType.getInstanceOf(playerResponsibilityType),
			AccountPlayResponsiblyPeriodType.getInstanceOf(periodType), manager.getUserId(),
			HostAddressUtils.getRealIPAddresses(request));

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void getBetReport(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "email", null);

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "transactionDaterange", null);

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

		String maxAmountStr = RequestParser.getStringParameter(request, 10, "maxAmount", null);
		String minAmountStr = RequestParser.getStringParameter(request, 10, "minAmount", null);

		BigDecimal minAmount = null;
		BigDecimal maxAmount = null;

		if (StringUtils.isNotEmpty(maxAmountStr)) {
			String value = maxAmountStr.replaceAll(",", "");
			maxAmount = BigDecimalUtils.getInstance(value).setScale(4, RoundingMode.DOWN);
		}

		if (StringUtils.isNotEmpty(minAmountStr)) {
			String value = minAmountStr.replaceAll(",", "");
			minAmount = BigDecimalUtils.getInstance(value).setScale(4, RoundingMode.DOWN);
		}

		if (minAmount != null && maxAmount != null) {
			if (minAmount.compareTo(maxAmount) > 0) {
				throw new Deviation("The Min amount must not be greater than the Max amount.");
			}
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 2);

		if (sortConditionInt < 1 || sortConditionInt > 12) {
			throw new Deviation("Sort Condition is invalid");
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 1);

		String sortCondition = switch (sortConditionInt) {
			case 1 -> "user_id";
			case 2 -> "txn_time";
			case 3 -> "settle_time";
			case 4 -> "create_time";
			case 5 -> "vendor_id";
			case 6 -> "game_type";
			case 7 -> "game_id";
			case 8 -> "balance_before";
			case 9 -> "bet_amount";
			case 10 -> "profit_loss";
			case 11 -> "balance_after";
			case 12 -> "turnover";
			default -> null;
		};

		String result = BTReportBO.getBetReport(manager.getWebsiteTypeObj(), userId, startTime, endTime,
			minAmount, maxAmount, sortCondition,
			DBOrderType.getInstanceOf(sortOrderInt));

		ResponseUtils.sendJsonResponse(response, result);

	}

	public static void searchBetReport(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang
	) throws Exception {
		// Parse userId
		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "email", null);

		// Parse date range
		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "transactionDaterange", null);

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

		String txnStatus = RequestParser.getStringParameter(request, 10, "txnStatus", null);
		SystemTxnStatusType systemTxnStatusType = SystemTxnStatusType.valueOf(txnStatus);

		// Parse amount filters
		String maxAmountStr = RequestParser.getStringParameter(request, 10, "maxAmount", null);
		String minAmountStr = RequestParser.getStringParameter(request, 10, "minAmount", null);

		BigDecimal minAmount = null;
		BigDecimal maxAmount = null;

		if (StringUtils.isNotEmpty(maxAmountStr)) {
			String value = maxAmountStr.replaceAll(",", "");
			maxAmount = BigDecimalUtils.getInstance(value).setScale(4, RoundingMode.DOWN);
		}

		if (StringUtils.isNotEmpty(minAmountStr)) {
			String value = minAmountStr.replaceAll(",", "");
			minAmount = BigDecimalUtils.getInstance(value).setScale(4, RoundingMode.DOWN);
		}

		if (minAmount != null && maxAmount != null) {
			if (minAmount.compareTo(maxAmount) > 0) {
				throw new Deviation("The Min amount must not be greater than the Max amount.");
			}
		}

		// Parse sort conditions
		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 2);
		if (sortConditionInt < 1 || sortConditionInt > 12) {
			throw new Deviation("Sort Condition is invalid");
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 1);

		String sortCondition = null;
		if (systemTxnStatusType == SystemTxnStatusType.SETTLED) {
			sortCondition = switch (sortConditionInt) {
				case 1 -> "user_id";
				case 2 -> "txn_time";
				case 3 -> "settle_time";
				case 4 -> "create_time";
				case 5 -> "vendor_id";
				case 6 -> "game_type";
				case 7 -> "gm.NAME"; // Sort by game name (from GAME table), not game_id
				case 8 -> "odds";
				case 9 -> "odds_type";
				case 10 -> "bet_amount";
				case 11 -> "profit_loss";
				case 12 -> "turnover";
				default -> null;
			};
		} else if (systemTxnStatusType == SystemTxnStatusType.UNSETTLED)  {
			sortCondition = switch (sortConditionInt) {
				case 1 -> "user_id";
				case 2 -> "txn_time";
				case 3 -> "create_time";
				case 4 -> "vendor_id";
				case 5 -> "game_type";
				case 6 -> "gm.NAME"; // Sort by game name (from GAME table), not game_id
				case 7 -> "odds";
				case 8 -> "odds_type";
				case 9 -> "bet_amount";
				default -> null;
			};
		}

		DBOrderType orderType = DBOrderType.getInstanceOf(sortOrderInt);

		// Create PageInfo for pagination
		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize");

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		// Call BO method with pagination
		String data = BTReportBO.getBetReportWithPagination(
			manager.getWebsiteTypeObj(),
			userId,
			systemTxnStatusType,
			startTime,
			endTime,
			minAmount,
			maxAmount,
			sortCondition,
			orderType,
			pageInfo
		);

		// Return response directly (already contains pageInfo, aaData, subtotals, grandtotal)
		ResponseUtils.sendJsonResponse(response, data);
	}

	private void getOriginBetReport(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		String gameType = RequestParser.getStringParameter(request, 4, "gameType");
		String recordId = RequestParser.getStringParameter(request, 30, "recordId");

		int vendorId = RequestParser.getIntParameter(request, "vendorId");
		int websiteType = manager.getWebsiteType();

		WebsiteVendor websiteVendor = VendorCache.getInstance()
			.getWebSiteVendor(WebSiteType.getInstance(websiteType), vendorId);
		int providerId = websiteVendor.getWebsiteProviderId();

		FCProxy proxy = ProviderProxyCache.getInstance()
			.getProviderProxy(WebSiteType.getInstance(manager.getWebsiteType()), providerId,
				manager.getWebsiteTypeObj().getDefaultCurrencyType());

		AccountProvider accountProvider = AccountProviderCache.getInstance().getAccountProvider(new AccountProviderKey(
			manager.getWebsiteType(), providerId, userId
		));

		FCGameLoginRs result = proxy.getPlayerReport(accountProvider.getProviderAccount(), recordId);

		if (result.getResult() != 0) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
			return;
		}

		ResponseUtils.sendJsonResponse(response, JSONUtils.toJsonString(result));
	}

	private void removeAccountCard(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.userId"));
		}

		WebSiteType webSiteType = manager.getWebsiteTypeObj();

		boolean result = AccountCardBO.removeAccountCard(userId, webSiteType, manager.getUserId(),
			HostAddressUtils.getRealIPAddresses(request));

		if (!result) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
			return;
		}

		ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
	}

	public static void getMoneyTransactionRecord(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId", null);

		String[] dateRange =
			RequestParser.getStringParameterValues(
				request, StringUtil.DASH_PATTERN, 50, "searchProfileUpdateLogDaterange", null);

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

		int transactionType = RequestParser.getIntParameter(request, "transactionType", 0);

		int status = RequestParser.getIntParameter(request, 10, "status", -999);

		BigDecimal minAmount = RequestParser.getBigDecimalParameter(request, "minAmount", BigDecimal.ZERO);

		BigDecimal maxAmount = RequestParser.getBigDecimalParameter(request, "maxAmount", BigDecimal.ZERO);

		if (minAmount.compareTo(BigDecimal.ZERO) > 0 && maxAmount.compareTo(BigDecimal.ZERO) > 0
			&& minAmount.compareTo(maxAmount) > 0) {
			throw new Deviation("The Min amount must not be greater than the Max amount.");
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 5);
		if (sortConditionInt < 1 || sortConditionInt > 23) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 0);
		String sortCondition = "create_time";

		if (transactionType == MoneyTransactionType.ADJUSTMENT.unique()
			|| transactionType == MoneyTransactionType.REVENUE_ADJUSTMENT.unique()) {
			sortCondition = switch (sortConditionInt) {
				case 1 -> "id";
				case 2 -> "amount";
				case 3 -> "creator";
				case 4 -> "create_time";
				default -> sortCondition; // 保持原值或设为 null
			};
		} else {
			sortCondition = switch (sortConditionInt) {
				case 1 -> "id";
				case 2 -> "amount";
				case 3 -> "status";
				case 4 -> "creator";
				case 5 -> "create_time";
				case 6 -> "approved_userid";
				case 7 -> "approved_time";
				default -> sortCondition; // 保持原值或设为 null
			};
		}
		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize", 10);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String result = MoneyTransactionBO.getUserMoneyTransactionReport(
			userId, status, minAmount, maxAmount, manager.getWebsiteType(),
			sortCondition, DBOrderType.getInstanceOf(sortOrderInt), startTime,
			endTime, MoneyTransactionType.getInstance(transactionType), pageInfo.getPageNumber(), pageSize);

		ResponseUtils.sendJsonResponse(response, result);

	}

}
