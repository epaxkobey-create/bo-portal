package com.nv.manage.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.nv.commons.bo.AccountBO;
import com.nv.commons.bo.AccountBankBO;
import com.nv.commons.bo.AccountCardBO;
import com.nv.commons.bo.BankBO;
import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.constants.APIResponseType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.LengthType;
import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.PaymentType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountBank;
import com.nv.commons.dto.AccountCard;
import com.nv.commons.dto.Bank;
import com.nv.commons.dto.Manager;
import com.nv.commons.dto.MoneyTransaction;
import com.nv.commons.exceptions.AccessDeniedException;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.BigDecimalUtils;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ManagerUtils;
import com.nv.commons.utils.MoneyTransactionUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.Validator;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/manager/payment/*")
public class PaymentServlet extends HttpServlet {

	private static final long serialVersionUID = 208269887591184241L;

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

			if ("/searchDeposit".equals(pathInfo)) {
				searchDeposit(request, response, manager, lang);
			} else if ("/getMoneyTransactionDetail".equals(pathInfo)) {
				getMoneyTransactionDetail(request, response, manager, lang);
			} else if ("/getBatchMoneyTransactionDetails".equals(pathInfo)) {
				getBatchMoneyTransactionDetails(request, response, manager, lang);
			} else if ("/disapproveDeposit".equals(pathInfo)) {
				disapproveDeposit(request, response, manager, lang);
			} else if ("/approveDeposit".equals(pathInfo)) {
				approveDeposit(request, response, manager, lang);
			} else if ("/searchAdjustment".equals(pathInfo)) {
				searchAdjustment(request, response, manager, lang);
			} else if ("/getAdjustmentInfoForCreate".equals(pathInfo)) {
				getAdjustmentInfoForCreate(request, response, manager, lang);
			} else if ("/createAdjustment".equals(pathInfo)) {
				createAdjustment(request, response, manager, lang);
			} else if ("/queryAllUserId".equals(pathInfo)) {
				queryAllUserId(request, response, manager);
			} else if ("/searchWithdrawal".equals(pathInfo)) {
				searchWithdrawal(request, response, manager, lang);
			} else if ("/getAccountCardByUser".equals(pathInfo)) {
				getAccountCardByUser(request, response, manager, lang);
			} else if ("/getAvailableBalance".equals(pathInfo)) {
				getAvailableBalance(request, response, manager, lang);
			} else if ("/createWithdrawal".equals(pathInfo)) {
				createWithdrawal(request, response, manager, lang);
			} else if ("/approveWithdrawal".equals(pathInfo)) {
				boApproveWithdrawal(request, response, manager, lang);
			} else if ("/disapproveWithdrawal".equals(pathInfo)) {
				disapproveWithdrawal(request, response, manager, lang);
			} else if ("/deleteAccountBank".equals(pathInfo)) {
				deleteAccountBank(request, response, manager, lang);
			} else if ("/addAccountBank".equals(pathInfo)) {
				addAccountBank(request, response, manager, lang);
			} else if("/getAvailableBankList".equals(pathInfo)) {
				getAvailableBankList(request, response, manager, lang);
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

	public static void searchDeposit(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang)
		throws Exception {

		String transactionId = RequestParser.getStringParameter(request, 10, "transactionId", null);
		Long transactionIdCondition =
			null == transactionId ? null : MoneyTransactionUtils.parseDepositId(transactionId);

		String email = RequestParser.getStringParameter(request, 50, "email", null);

		int status = RequestParser.getIntParameter(request, 5, "status", -999);

		BigDecimal minAmount = RequestParser.getBigDecimalParameter(request, "minAmount", BigDecimal.ZERO);

		BigDecimal maxAmount = RequestParser.getBigDecimalParameter(request, "maxAmount", BigDecimal.ZERO);

		if (minAmount.compareTo(BigDecimal.ZERO) > 0 && maxAmount.compareTo(BigDecimal.ZERO) > 0
			&& minAmount.compareTo(maxAmount) > 0) {
			throw new Deviation("The Min amount must not be greater than the Max amount.");
		}

		String createdBy = RequestParser.getStringParameter(request, 50, "createdBy", null);

		String createdSince = RequestParser.getStringParameter(request, 50, "createdSince", null);

		String updatedBy = RequestParser.getStringParameter(request, 50, "updatedBy", null);

		String updatedSince = RequestParser.getStringParameter(request, 50, "updatedSince", null);

		Timestamp createdSinceTime = null;
		Timestamp updatedSinceTime = null;
		if (createdSince != null) {
			java.util.Date startDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, createdSince);
			createdSinceTime = new Timestamp(startDate.getTime());

		}

		if (updatedSince != null) {
			java.util.Date endDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, updatedSince);
			updatedSinceTime = new Timestamp(endDate.getTime());
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 1);
		if (sortConditionInt < 1 || sortConditionInt > 23) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 0);

		String sortCondition = null;
		if (1 == sortConditionInt) {
			sortCondition = "user_id";
		} else if (2 == sortConditionInt) {
			sortCondition = "id";
		} else if (3 == sortConditionInt) {
			sortCondition = "amount";
		} else if (4 == sortConditionInt) {
			sortCondition = "status";
		} else if (5 == sortConditionInt) {
			sortCondition = "creator";
		} else if (6 == sortConditionInt) {
			sortCondition = "create_time";
		} else if (7 == sortConditionInt) {
			sortCondition = "approved_userid";
		} else if (8 == sortConditionInt) {
			sortCondition = "approved_time";
		}

		DBOrderType orderType = (1 == sortOrderInt ? DBOrderType.DESC : DBOrderType.ASC);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize", 10);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data =
			MoneyTransactionBO.getDepositReport(
				email, transactionIdCondition, status, minAmount, maxAmount, createdBy, createdSinceTime, updatedBy,
				updatedSinceTime,
				manager.getWebsiteType(), sortCondition, orderType,
				manager.getWebsiteTypeObj().getDefaultCurrencyType().unique(),
				pageInfo.getPageNumber(), pageSize
			);

		ResponseUtils.sendJsonResponse(response, data);
	}

	public static void getMoneyTransactionDetail(
		HttpServletRequest request,
		HttpServletResponse response,
		Manager manager,
		LangMessage lang)
		throws Exception {

		String transactionId = RequestParser.getStringParameter(request, 10, "transactionId", null);
		Long transactionIdCondition =
			transactionId == null ? null : MoneyTransactionUtils.deformatFrontendId(transactionId);

		String result = MoneyTransactionBO.getPaymentMoneyTransactionDetail(transactionIdCondition);

		if (null == result) {
			ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
			return;
		}

		ResponseUtils.sendJsonResponse(response, result);
	}

	private void getBatchMoneyTransactionDetails(HttpServletRequest request, HttpServletResponse response,
		Manager manager, LangMessage lang) throws Exception {

		String batchDeposit =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "batchDeposit", null);
		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 6);
		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 1);
		DBOrderType orderType = (1 == sortOrderInt ? DBOrderType.DESC : DBOrderType.ASC);

		String sortField = null;
		if (1 == sortConditionInt) {
			sortField = "user_id";
		} else if (2 == sortConditionInt) {
			sortField = "id";
		} else if (3 == sortConditionInt) {
			sortField = "amount";
		} else if (4 == sortConditionInt) {
			sortField = "status";
		} else if (5 == sortConditionInt) {
			sortField = "creator";
		} else if (6 == sortConditionInt) {
			sortField = "create_time";
		} else if (7 == sortConditionInt) {
			sortField = "approved_userid";
		} else if (8 == sortConditionInt) {
			sortField = "approved_time";
		}
		List<Long> depositIdList;
		try {
			depositIdList = JSONUtils.parseJsonToObjectList(batchDeposit, String.class).stream()
				.map(MoneyTransactionUtils::deformatFrontendId)
				.toList();
		} catch (IOException e) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}
		if (depositIdList.isEmpty()) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		String result = MoneyTransactionBO.getBatchMoneyTransactionDetails(depositIdList, sortField, orderType);

		ResponseUtils.sendJsonResponse(response, result);
	}

	// batch / single disapprove
	public static void disapproveDeposit(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		String batchDeposit =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "batchDeposit", null);
		List<Long> depositIdList;
		try {
			depositIdList = JSONUtils.parseJsonToObjectList(batchDeposit, String.class).stream()
				.map(MoneyTransactionUtils::deformatFrontendId)
				.toList();
		} catch (IOException e) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		List<MoneyTransaction> moneyTransactionList = MoneyTransactionBO.getMoneyTransactionList(depositIdList, null,
			null);

		if (moneyTransactionList == null || moneyTransactionList.isEmpty()) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		boolean result = MoneyTransactionBO.rejectDeposit(lang, manager.getUserId(),
			moneyTransactionList.toArray(new MoneyTransaction[0]));

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	public static void approveDeposit(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {
		String batchDeposit =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "batchDeposit", null);
		List<Long> depositIdList;

		try {
			depositIdList = JSONUtils.parseJsonToObjectList(batchDeposit, String.class).stream()
				.map(MoneyTransactionUtils::deformatFrontendId)
				.toList();
		} catch (IOException e) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		if (depositIdList.isEmpty()) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		int result = MoneyTransactionBO.batchApproveDeposit(depositIdList, manager.getUserId());

		if (result == 0 || result == 1) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
		}
	}

	private void searchAdjustment(HttpServletRequest request, HttpServletResponse response,
		Manager manager, LangMessage lang)
		throws Exception {

		String userId = RequestParser.getStringParameter(request, 5000, "userId", null);

		String transactionId =
			RequestParser.getStringParameter(request, 10, "transactionId", null);
		Long transactionIdCondition =
			transactionId == null ? null : MoneyTransactionUtils.parseAdjustmentId(transactionId);

		String pattern = FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss;

		String createDateStartStr =
			RequestParser.getStringParameter(request, pattern.length(), "createDateStart", null);
		Date createDateStart = DateUtils.parseDate(pattern, createDateStartStr, null);

		String creator =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "creator", null);

		int adjustmentType = RequestParser.getIntParameter(request, 2, "adjustmentType", -1);

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition");
		if (sortConditionInt < 1 || sortConditionInt > 5) {
			throw new Deviation().setI18N(
				"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder");

		String sortCondition = null;
		if (1 == sortConditionInt) {
			sortCondition = "moneytransaction.user_id";
		} else if (2 == sortConditionInt) {
			sortCondition = "id";
		} else if (3 == sortConditionInt) {
			sortCondition = "amount";
		} else if (4 == sortConditionInt) {
			sortCondition = "creator";
		} else if (5 == sortConditionInt) {
			sortCondition = "moneytransaction.create_time";
		}

		DBOrderType orderType = (1 == sortOrderInt ? DBOrderType.DESC : DBOrderType.ASC);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize", SystemConstants.PAGE_SIZE);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));

		String data =
			MoneyTransactionBO.searchAdjustment(
				transactionIdCondition,
				userId,
				createDateStart,
				creator,
				sortCondition,
				orderType,
				pageInfo,
				manager.getCurrencyTypeSet(),
				adjustmentType);

		ResponseUtils.sendJsonResponse(response, pageInfo.getDataTableJson(data));
	}

	private void getAdjustmentInfoForCreate(HttpServletRequest request, HttpServletResponse response,
		Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation()
				.setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.email"));
		}

		String result = MoneyTransactionBO.getAdjustmentInfoByStatusForCreate(userId);

		if (result == null) {
			ResponseUtils.sendJsonErrorResponse(
				response,
				lang.get(
					"msg.error.validation.fieldNotFound",
					new String[] {lang.get("form.text.backOffice.user")}));
			return;
		}

		ResponseUtils.sendJsonResponse(response, result);
	}

	private void createAdjustment(HttpServletRequest request, HttpServletResponse response,
		Manager manager, LangMessage lang)
		throws Exception {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId", null);
		if (userId == null || userId.isEmpty()) {
			throw new Deviation("This field is required.");
		}
		if (!Validator.isValidatedUserId(userId)) {
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("form.text.account.email"));
		}

		String inputAmount = RequestParser.getStringParameter(request, 22, "amount", null);
		if (inputAmount == null || inputAmount.isEmpty()) {
			throw new Deviation("This field is required.");
		}
		BigDecimal amount = BigDecimalUtils.getInstance(inputAmount).setScale(2, RoundingMode.HALF_UP);

		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			throw new Deviation("Adjustment amount cannot be 0.");
		}

		// Cap positive adjustments to max €1,000
		if (amount.compareTo(new BigDecimal("1000.00")) > 0) {
			amount = new BigDecimal("1000.00");
		}

		Account account = AccountBO.getAccountByUserId(userId, manager.getWebsiteTypeObj());

		if (account == null) {
			throw new Deviation().setI18N("msg.error.account.accountNotFound");
		}

		WebSiteType webSiteType = manager.getWebsiteTypeObj();
		String userKey = AccountUtils.getUserKey(webSiteType, userId);
//		// Cap negative adjustments to wallet balance
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			BigDecimal walletBalance = SeamlessWalletApiService.getInstance().getBalance(userKey);
			if (amount.abs().compareTo(walletBalance) > 0) {
				amount = walletBalance.negate();
			}
		}

		MoneyTransaction moneyTransaction = new MoneyTransaction();
		moneyTransaction.setUserId(userId);
		moneyTransaction.setWebsiteType(manager.getWebsiteType());
		moneyTransaction.setAmount(amount);
		moneyTransaction.setCreator(manager.getUserId());
		moneyTransaction.setApprovedUserid(manager.getUserId());
		moneyTransaction.setCurrency(account.getCurrencyTypeName());

		boolean result = MoneyTransactionBO.createAdjustment(account, moneyTransaction);

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void queryAllUserId(HttpServletRequest request, HttpServletResponse response,
		Manager manager) {

		String userId =
			RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "search");
		int currency = RequestParser.getIntParameter(request, 2, "currency", -1);

		String selectUserId =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "selectUserId", null);
		List<String> selectUserIdList =
			null == selectUserId
				? new ArrayList<>()
				: Arrays.stream(selectUserId.split(",")).collect(Collectors.toList());

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

	public static void searchWithdrawal(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		String transactionId =
			RequestParser.getStringParameter(request, 10, "transactionId", null);
		Long transactionIdCondition =
			transactionId == null ? null : MoneyTransactionUtils.parseWithdrawalId(transactionId);

		String email = RequestParser.getStringParameter(request, 50, "email", null);

		int status = RequestParser.getIntParameter(request, 5, "status", -999);

		BigDecimal minAmount = RequestParser.getBigDecimalParameter(request, "minAmount", BigDecimal.ZERO);

		BigDecimal maxAmount = RequestParser.getBigDecimalParameter(request, "maxAmount", BigDecimal.ZERO);

		if (minAmount.compareTo(BigDecimal.ZERO) > 0 && maxAmount.compareTo(BigDecimal.ZERO) > 0
			&& minAmount.compareTo(maxAmount) > 0) {
			throw new Deviation("The Min amount must not be greater than the Max amount.");
		}

		String createdBy = RequestParser.getStringParameter(request, 50, "createdBy", null);

		String createdSince = RequestParser.getStringParameter(request, 50, "createdSince", null);

		String updatedBy = RequestParser.getStringParameter(request, 50, "updatedBy", null);

		String updatedSince = RequestParser.getStringParameter(request, 50, "updatedSince", null);

		Timestamp createdSinceTime = null;
		Timestamp updatedSinceTime = null;
		if (createdSince != null) {
			java.util.Date startDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, createdSince);
			createdSinceTime = new Timestamp(startDate.getTime());
		}

		if (updatedSince != null) {
			java.util.Date endDate =
				FormatUtils.parseDate(FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss, updatedSince);
			updatedSinceTime = new Timestamp(endDate.getTime());
		}

		int sortConditionInt = RequestParser.getIntParameter(request, 2, "sortCondition", 1);
		if (sortConditionInt < 1 || sortConditionInt > 23) {
			throw new Deviation()
				.setI18N(
					"msg.error.validation.fieldNotValid", lang.get("form.text.backOffice.sortCondition"));
		}

		int sortOrderInt = RequestParser.getIntParameter(request, 1, "sortOrder", 0);

		String sortCondition = null;
		if (1 == sortConditionInt) {
			sortCondition = "user_id";
		} else if (2 == sortConditionInt) {
			sortCondition = "id";
		} else if (3 == sortConditionInt) {
			sortCondition = "amount";
		} else if (4 == sortConditionInt) {
			sortCondition = "status";
		} else if (5 == sortConditionInt) {
			sortCondition = "creator";
		} else if (6 == sortConditionInt) {
			sortCondition = "create_time";
		} else if (7 == sortConditionInt) {
			sortCondition = "approved_userid";
		} else if (8 == sortConditionInt) {
			sortCondition = "approved_time";
		}

		DBOrderType orderType = (1 == sortOrderInt ? DBOrderType.DESC : DBOrderType.ASC);

		int pageSize = RequestParser.getIntParameter(request, 100, "pageSize", 10);

		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(pageSize);
		pageInfo.setPageNumber(RequestParser.getIntParameter(request, "pageNumber", 1));
		String data =
			MoneyTransactionBO.getWithdrawalReport(
				email, transactionIdCondition, status, minAmount, maxAmount, createdBy, createdSinceTime, updatedBy,
				updatedSinceTime,
				manager.getWebsiteType(), sortCondition, orderType,
				manager.getWebsiteTypeObj().getDefaultCurrencyType().unique(),
				pageInfo.getPageNumber(), pageSize
			);

		ResponseUtils.sendJsonResponse(response, data);
	}

	private void getAvailableBalance(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		String email = RequestParser.getStringParameter(request, 50, "email");

		String userKey = AccountUtils.getUserKey(manager.getWebsiteType(), email);

		String result = SeamlessWalletApiService.getInstance().getWalletBalanceJson(userKey);

		if (result != null) {
			ResponseUtils.sendJsonResponse(response, result);
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, "No balance");
	}

	private void getAccountCardByUser(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {
		String email = RequestParser.getStringParameter(request, 50, "email", null);

		String dropdownResult = AccountCardBO.getAccountCardsByUserId(email, manager.getWebsiteType());

		if (dropdownResult != null && !dropdownResult.isEmpty()) {
			ResponseUtils.sendJsonResponse(response, dropdownResult);
		} else {
			ResponseUtils.sendJsonResponse(response, JSONUtils.EMPTY_JSON_STRING);
		}
	}

	private void createWithdrawal(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		// TODO: email 未做 null/empty/format 檢查 — null email 會走到 account==null 分支回傳 accountNotFound，功能上安全但錯誤訊息不精確
		String email = RequestParser.getStringParameter(request, 50, "email", null);

		BigDecimal withdrawAmount = RequestParser.getBigDecimalParameter(request, "amount", BigDecimal.ZERO);

		if (Validator.verifyLengthAndEqualToZero(withdrawAmount, 20)) { // only positive or negative, cannot be 0
			throw new Deviation().setI18N("msg.error.validation.fieldNotValid", lang.get("ui.text.amount"));
		}

		// either card ID or account bank ID
		int id = RequestParser.getIntParameter(request, "id");

		Account account = AccountBO.getAccountByUserId(email, manager.getWebsiteTypeObj());

		// for bank
		//AccountBank accountBank = AccountBankBO.getAccountBankById(bankId);

		// for card
		AccountCard accountCard = AccountCardBO.findById(id);
		if (accountCard == null) {
			throw new Deviation().setI18N("msg.error.account.paymentMethod.notFound");
		}
		if (account == null) {
			throw new Deviation().setI18N("msg.error.account.accountNotFound");
		}
		// Verify card ownership - prevent cross-account withdrawal via mismatched card ID
		if (!accountCard.getUserId().equalsIgnoreCase(account.getUserId())) {
			throw new Deviation().setI18N("msg.error.account.paymentMethod.userId.incorrect");
		}

		MoneyTransaction moneyTransaction = new MoneyTransaction();
		MoneyTransactionBO.setMoneyTransactionObj(moneyTransaction,
			account.getUserId(), WebSiteType.getInstance(account.getWebsiteType()),
			MoneyTransactionType.WITHDRAWALS, PaymentType.CREDIT_CARD, MoneyTransactionStatusType.NEW,
			id, accountCard.getBankName(), Long.toString(accountCard.getId()),
			accountCard.getCardholderName(), accountCard.getCardNo(), withdrawAmount, withdrawAmount,
			account.getVipLevel(), account.getCurrencyTypeName(), manager.getUserId());

		boolean result = MoneyTransactionBO.createWithdrawal(moneyTransaction, account);

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}

		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	public static void boApproveWithdrawal(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		String batchWithdrawal =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "batchWithdrawal", null);
		List<Long> withdrawalIdList;
		try {
			withdrawalIdList = JSONUtils.parseJsonToObjectList(batchWithdrawal, String.class).stream()
				.map(MoneyTransactionUtils::deformatFrontendId)
				.toList();
		} catch (IOException e) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		boolean result = MoneyTransactionBO.boApproveWithdrawal(withdrawalIdList, manager.getUserId());

		if (result) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	public static void disapproveWithdrawal(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang)
		throws Exception {

		String batchWithdrawal =
			RequestParser.getStringParameter(request, Integer.MAX_VALUE, "batchWithdrawal", null);
		List<Long> withdrawalIdList;
		try {
			withdrawalIdList = JSONUtils.parseJsonToObjectList(batchWithdrawal, String.class).stream()
				.map(MoneyTransactionUtils::deformatFrontendId)
				.toList();
		} catch (IOException e) {
			throw new Deviation(lang.get(SystemConstants.INTERNAL_EXCEPTION));
		}

		int result = MoneyTransactionBO.disapproveWithdrawal(withdrawalIdList, manager.getUserId());

		if (result >= 0) {
			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));

	}

	private void deleteAccountBank(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {

		int bankId = RequestParser.getIntParameter(request, "bankId");
		String userId = RequestParser.getStringParameter(request, 50, "userId");
		AccountBank bank = AccountBankBO.getAccountBankById(bankId);
		if (bank == null) {
			throw new Deviation("Bank does not exists");
		}
		int result = AccountBankBO.manageAccountBankStatus(bankId, BinaryStatusType.ACTIVE.unique());

		if (result > 0) {

			AccountBankBO.addAccountUpdateLog(userId, JSONUtils.toJsonString(bank), "", manager.getUserId(),
				HostAddressUtils.getRealIPAddresses(request));

			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void addAccountBank(HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {
		String userId = RequestParser.getStringParameter(request, 50, "userId");
		int bankId = RequestParser.getIntParameter(request, "bankId");
		String bankAccountNumber = RequestParser.getStringParameter(request, 34, "bankAccountNumber");

		List<AccountBank> accountBankList = AccountBankBO.getAccountBankByUserId(userId,
			manager.getWebsiteType(), true);

		if (!Validator.isValidatedBankAccNumber(bankAccountNumber)) {
			throw new Deviation().setI18N("msg.error.account.bank.accountNumber.isNotValidated");
		}
		Bank bank = BankBO.getBank(bankId);

		if (bank == null) {
			throw new Deviation().setI18N("msg.error.account.bank.invalidBankId");
		}

		long activeAccountCount = accountBankList.stream()
			.filter(accountBank -> accountBank.getIsDeleted() == BinaryStatusType.INACTIVE.unique())
			.count();

		if (activeAccountCount >= 3) {
			throw new Deviation("Account Bank list is full");
		}

		AccountBank existingAccountBank = accountBankList.stream()
			.filter(accountBank -> bankAccountNumber.trim().equals(accountBank.getBankAccNumber()))
			.findFirst()
			.orElse(null);


		int result = 0;
		if (existingAccountBank != null) {
			if (existingAccountBank.getIsDeleted() == 0) {
				throw new Deviation(APIResponseType.DUPLICATED_BANK_ACCOUNT.getI18nKey());
			} else {
				result = AccountBankBO.manageAccountBankStatus(existingAccountBank.getId(),
					BinaryStatusType.INACTIVE.unique());
			}
		} else {

			result = AccountBankBO.addNewAccountBank(userId, bankAccountNumber, bankId,
				manager.getWebsiteType(), bank.getBankName());
		}
		if (result > 0) {
			// Before addition: no bank
			AccountBank accountBank = new AccountBank();
			accountBank.setBankAccNumber(bankAccountNumber);
			accountBank.setBankName(bank.getBankName());

			AccountBankBO.addAccountUpdateLog(userId, "", JSONUtils.toJsonString(accountBank), manager.getUserId(),
				HostAddressUtils.getRealIPAddresses(request));

			ResponseUtils.respondSuccessWithMessage(response, lang.get("global.text.success"));
			return;
		}
		ResponseUtils.sendJsonErrorResponse(response, lang.get(SystemConstants.INTERNAL_EXCEPTION));
	}

	private void getAvailableBankList (HttpServletRequest request, HttpServletResponse response, Manager manager,
		LangMessage lang) throws Exception {
		String email = RequestParser.getStringParameter(request, 50, "email", null);

		String dropdownResult = AccountBankBO.getAvailableAccountCard(email, manager.getWebsiteType());

		if (dropdownResult != null && !dropdownResult.isEmpty()) {
			ResponseUtils.sendJsonResponse(response, dropdownResult);
		} else {
			ResponseUtils.sendJsonResponse(response, JSONUtils.EMPTY_JSON_STRING);
		}
	}
}
