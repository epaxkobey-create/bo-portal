<%@page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.AccountStatusType" %>
<%@ page import="com.nv.commons.constants.AccountSummaryReportType" %>
<%@ page import="com.nv.commons.constants.AccountUpdateType" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionStatusType" %>
<%@ page import="com.nv.commons.constants.SystemTxnStatusType" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.utils.JSONUtils" %>
<%@ page import="com.nv.commons.constants.RemarkTemplateType" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="com.nv.commons.constants.KycDocumentStatusType" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionType" %>
<%@ page import="com.nv.commons.constants.AccountUpdateDropDownType" %>
<%

	FrontendUtils.noCache(response);
	List<AccountUpdateType> accessUpdateTypeOptionSortedList = AccountUpdateDropDownType.CY.getAccountUpdateTypeList();

	String userId = RequestParser.getStringParameter(request, 50, "userId");
	int currency = RequestParser.getIntParameter(request, "currency", -1);
	String activeTab = RequestParser.getStringParameter(request, 1, "activeTab", "1");//20?
	Date today = DateTimeBuilder.localDateTime().withMinTime().toCalendar().getTime();
	Date todayEnd = DateUtils.getSpecifyDate(today, DateUtils.END);
	boolean enableDoForceServe = true;

	boolean enableViewTodayBet = true;
	boolean enableViewTransfer = true;
	boolean enableViewDeposit = true;
	boolean enableViewWithdrawal = true;
	boolean enableViewAdjustment = true;
	boolean enableViewLoginLog = true;
	boolean enableViewBonus = true;
	boolean enableViewUpdateLog = true;
	boolean enableViewBetUnsettled = true;
	boolean enableViewBetSettled = true;

	boolean enableShowEmail = true;
	boolean enableViewVip = true;

	String todayEndStr = DateUtils.toString(todayEnd, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayHalfYearAgo = DateUtils.getNextNMonth(today, -SystemConstants.MAX_LIMIT_MONTH);
	String todayHalfYearAgoStr = DateUtils.toString(todayHalfYearAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayOneMonthAgo = DateUtils.getNextNMonth(today, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	boolean isAllowBonusWallet = false;

	String todayOneMonthAgoStrOnlyDate = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
	String todayEndStrOnlyDate = DateUtils.toString(todayEnd, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy);
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.2")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.38")%>
	</title>


	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/plugins.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/member/report.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<style type="text/css">
      .popover-content {
        white-space: pre-line;
        max-height: 350px;
        overflow-y: auto;
      }

      div.ColVis_collectionBackground {
        z-index: 11000;
      }

      div.ColVis_collection {
        /*z-index: 1102;*/
        z-index: 11020
      }

      .loading {
        color: transparent !important;
        background: url('/img/ajax-loading-input.gif') no-repeat 50% 50%;
      }

      .modal-lg {
        width: 1100px;
      }

      .text-wrap {
        overflow-wrap: break-word;
        word-break: break-word;
        max-width: 300px;
        white-space: normal;
      }
	</style>

	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.userId = '<%=userId%>';
		PageConfig.DateFormatPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy%>';
		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';
		PageConfig.enableDoForceServe = <%=enableDoForceServe%>;

		PageConfig.enableViewTodayBet = <%=enableViewTodayBet%>;
		PageConfig.enableViewTransfer = <%=enableViewTransfer%>;
		PageConfig.enableViewDeposit = <%=enableViewDeposit%>;
		PageConfig.enableViewWithdrawal = <%=enableViewWithdrawal%>;
		PageConfig.enableViewAdjustment = <%=enableViewAdjustment%>;
		PageConfig.enableViewLoginLog = <%=enableViewLoginLog%>;
		PageConfig.enableViewBonus = <%=enableViewBonus%>;
		PageConfig.enableViewUpdateLog = <%=enableViewUpdateLog%>;
		PageConfig.enableViewVip = <%=enableViewVip%>;
		PageConfig.enableViewBetUnsettled = <%=enableViewBetUnsettled%>;
		PageConfig.enableViewBetSettled = <%=enableViewBetSettled%>;

		PageConfig.AccountUpdateType = <%=AccountUpdateType.toJsonString()%>;
		PageConfig.AccountStatusType = <%=AccountStatusType.toJsonString()%>;
		PageConfig.DocumentStatusType = <%=KycDocumentStatusType.toJsonString()%>;

		PageConfig.accountCurrency = "<%=currency%>";
		PageConfig.lang = "<%=commonLangMessage%>";
		PageConfig.remark = <%=JSONUtils.toJsonString("{}")%>;

		PageConfig.isAllowBonusWallet = <%=isAllowBonusWallet%>;


		PageConfig.accessRight = {};
		PageConfig.accessRight.forceServeBonusWallet = 0;
		PageConfig.remarkSetting = <%="{}"%>;
		PageConfig.remarkType = <%=RemarkTemplateType.FORCE_SERVE.unique()%>;

		//set I18N
		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.report': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.report")%>',
			'form.text.account.minDeposit': '<%=commonLangMessage.get("form.text.account.minDeposit")%>',
			'form.text.account.bonusReceiveFail': '<%=commonLangMessage.get("form.text.account.bonusReceiveFail")%>',
			'form.text.account.userId': '<%=commonLangMessage.get("form.text.account.userId")%>',
			'form.text.account.legalFirstName': '<%=commonLangMessage.get("form.text.account.legalFirstName")%>',
			'form.text.account.legalLastName': '<%=commonLangMessage.get("form.text.account.legalLastName")%>',
			'form.text.account.address': '<%=commonLangMessage.get("form.text.account.address")%>',
			'form.text.account.occupation': '<%=commonLangMessage.get("form.text.account.occupation")%>',
			'form.text.backOffice.status': '<%=commonLangMessage.get("form.text.backOffice.status")%>',
			'form.text.backOffice.note': '<%=commonLangMessage.get("form.text.backOffice.note")%>',
			'form.text.account.vipLevel': '<%=commonLangMessage.get("form.text.account.vipLevel")%>',
			'form.text.account.email': '<%=commonLangMessage.get("form.text.account.email")%>',
			'form.text.account.phoneNumber': '<%=commonLangMessage.get("form.text.account.phoneNumber")%>',
			'form.text.account.phoneNumber2': '<%=commonLangMessage.get("form.text.account.phoneNumber2")%>',
			'form.text.account.phoneNumber3': '<%=commonLangMessage.get("form.text.account.phoneNumber3")%>',
			'form.text.account.qq_id': '<%=commonLangMessage.get("form.text.account.qq_id")%>',
			'form.text.account.wechat_Id': '<%=commonLangMessage.get("form.text.account.wechat_Id")%>',
			'form.text.accountGroup': '<%=commonLangMessage.get("form.text.accountGroup")%>',
			'form.text.account.balance': '<%=commonLangMessage.get("form.text.account.balance")%>',
			'form.text.account.birthday2': '<%=commonLangMessage.get("form.text.account.birthday2")%>',
			'form.text.account.accountBank': '<%=commonLangMessage.get("form.text.account.accountBank")%>',
			'form.text.account.skype_id': '<%=commonLangMessage.get("form.text.account.skype_id")%>',
			'form.text.account.zalo_id': '<%=commonLangMessage.get("form.text.account.zalo_id")%>',
			'form.text.account.telegram_id': '<%=commonLangMessage.get("form.text.account.telegram_id")%>',
			'form.text.account.whatsapp_id': '<%=commonLangMessage.get("form.text.account.whatsapp_id")%>',
			'form.text.account.updateTransfer': '<%=commonLangMessage.get("form.text.account.updateTransfer")%>',
			'form.text.backOffice.affiliate.firstName': '<%=commonLangMessage.get("form.text.backOffice.affiliate.firstName")%>',
			'form.text.backOffice.affiliate.lastName': '<%=commonLangMessage.get("form.text.backOffice.affiliate.lastName")%>',
			'form.text.affiliate.affiliateBank': '<%=commonLangMessage.get("form.text.affiliate.affiliateBank")%>',
			'ui.text.raf.referrer': '<%=commonLangMessage.get("ui.text.raf.referrer") %>',
			'form.text.bank.bankName': '<%=commonLangMessage.get("form.text.bank.bankName")%>',
			'form.text.bank.bankAccName': '<%=commonLangMessage.get("form.text.bank.bankAccName")%>',
			'form.text.bank.bankBranch': '<%=commonLangMessage.get("form.text.bank.bankBranch")%>',
			'form.text.bank.bankAccNumber': '<%=commonLangMessage.get("form.text.bank.bankAccNumber")%>',
			'form.text.backOffice.status.locked': '<%=commonLangMessage.get("form.text.backOffice.status.locked")%>',
			'form.text.backOffice.transferStatusType.PENDING': '<%=commonLangMessage.get("form.text.backOffice.transferStatusType.PENDING")%>',
			'form.text.backOffice.transferStatusType.SUCCESS': '<%=commonLangMessage.get("form.text.backOffice.transferStatusType.SUCCESS")%>',
			'form.text.backOffice.transferStatusType.FAIL': '<%=commonLangMessage.get("form.text.backOffice.transferStatusType.FAIL")%>',
			'form.text.backOffice.report.allowed': '<%=commonLangMessage.get("form.text.backOffice.report.allowed")%>',
			'form.text.backOffice.report.notAllowed': '<%=commonLangMessage.get("form.text.backOffice.report.notAllowed")%>',
			'form.text.report.viewIcon': '<%=commonLangMessage.get("form.text.report.viewIcon")%>',
			'form.text.backOffice.report.forceServe': '<%=commonLangMessage.get("form.text.backOffice.report.forceServe")%>',
			'form.text.backOffice.action': '<%=commonLangMessage.get("form.text.backOffice.action")%>',
			'form.text.button.search': '<%=commonLangMessage.get("form.text.button.search")%>',
			'form.text.button.check': '<%=commonLangMessage.get("form.text.button.check")%>',
			'form.text.af.btn.update': '<%=commonLangMessage.get("form.text.af.btn.update")%>',
			'global.text.moneyTransactionStatusType.NEW': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.NEW")%>',
			'global.text.moneyTransactionStatusType.PENDING_APPROVAL': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.PENDING_APPROVAL")%>',
			'global.text.moneyTransactionStatusType.REJECTED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.REJECTED")%>',
			'global.text.moneyTransactionStatusType.CONFIRMED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.CONFIRMED")%>',
			'global.text.moneyTransactionStatusType.CLOSE': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.CLOSE")%>',
			'global.text.moneyTransactionStatusType.ON_HOLD': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.ON_HOLD")%>',
			'global.text.moneyTransactionStatusType.PROCESSING': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.PROCESSING")%>',
			'global.text.moneyTransactionStatusType.REVERTED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.REVERTED")%>',
			'global.text.moneyTransactionStatusType.AWAITED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.AWAITED")%>',
			'ui.text.backOffice.same_fingerprint_user': '<%=commonLangMessage.get("ui.text.backOffice.same_fingerprint_user")%>',
			'ui.text.backOffice.same_fingerprint4_user': '<%=commonLangMessage.get("ui.text.backOffice.same_fingerprint4_user")%>',
			'ui.text.backOffice.same_browserHash_user': '<%=commonLangMessage.get("ui.text.backOffice.same_browserHash_user")%>',
			'ui.text.backOffice.same_deviceHash_user': '<%=commonLangMessage.get("ui.text.backOffice.same_deviceHash_user")%>',
			'ui.text.backOffice.same_cookieHash_user': '<%=commonLangMessage.get("ui.text.backOffice.same_cookieHash_user")%>',
			'ui.text.backOffice.same_ip_user': '<%=commonLangMessage.get("ui.text.backOffice.same_ip_user")%>',
			'ui.text.payment_redirect_plz_wait': '<%=commonLangMessage.get("ui.text.payment_redirect_plz_wait")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.complete': '<%=commonLangMessage.get("form.text.backOffice.status.complete")%>',
			'form.text.backOffice.status.returnToMain': '<%=commonLangMessage.get("form.text.backOffice.status.returnToMain")%>',
			'form.text.backOffice.status.suspend': '<%=commonLangMessage.get("form.text.backOffice.status.suspend")%>',
			'form.text.backOffice.status.expired': '<%=commonLangMessage.get("form.text.backOffice.status.expired")%>',
			'form.text.backOffice.status.autoForceServe': '<%=commonLangMessage.get("form.text.backOffice.status.autoForceServe")%>',
			'form.text.backOffice.status.manualForceServe': '<%=commonLangMessage.get("form.text.backOffice.status.manualForceServe")%>',
			'form.text.backOffice.checkProviderAPIType.error': '<%=commonLangMessage.get("form.text.backOffice.checkProviderAPIType.error") %>',
			'form.text.backOffice.checkProviderAPIType.isNotSupported': '<%=commonLangMessage.get("form.text.backOffice.checkProviderAPIType.isNotSupported") %>',
			'form.text.backOffice.checkProviderAPIType.FAIL': '<%=commonLangMessage.get("form.text.backOffice.checkProviderAPIType.FAIL") %>',
			'form.text.backOffice.checkProviderAPIType.SUCCESS': '<%=commonLangMessage.get("form.text.backOffice.checkProviderAPIType.SUCCESS") %>',
			'msg.error.account.noMatchRecord': '<%=commonLangMessage.get("msg.error.account.noMatchRecord") %>',
			'form.text.backOffice.reportExportType.todayBet': '<%=commonLangMessage.get("form.text.backOffice.reportExportType.todayBet")%>',
			'msg.info.backOffice.reportIsProduction': '<%=commonLangMessage.get("msg.info.backOffice.reportIsProduction")%>',
			'form.text.backOffice.payment.disapprove': '<%=commonLangMessage.get("form.text.backOffice.payment.disapprove")%>',
			'form.text.backOffice.payment.approve': '<%=commonLangMessage.get("form.text.backOffice.payment.approve")%>',
			'msg.info.backOffice.checkProcess': '<%=commonLangMessage.get("msg.info.backOffice.checkProcess")%>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'form.text.account.updatePassword': '<%=commonLangMessage.get("form.text.account.updatePassword")%>',
			'form.text.account.systemUpdatePassword': '<%=commonLangMessage.get("form.text.account.systemUpdatePassword")%>',
			'form.text.account.playerUpdatePassword': '<%=commonLangMessage.get("form.text.account.playerUpdatePassword")%>',
			'form.text.account.viber_id': '<%=commonLangMessage.get("form.text.account.viber_id")%>',
			'form.text.account.deleteProviderAccount': '<%=commonLangMessage.get("form.text.account.deleteProviderAccount")%>',
			'ui.text.deposit.select_remark': '<%=commonLangMessage.get("ui.text.deposit.select_remark")%>',
			'msg.remark.template.required': '<%=commonLangMessage.get("msg.remark.template.required")%>',
			'msg.remark.required': '<%=commonLangMessage.get("msg.remark.required")%>',
			'form.text.yes': '<%=commonLangMessage.get("form.text.yes")%>',
			'form.text.no': '<%=commonLangMessage.get("form.text.no")%>',
			'form.text.on': '<%=commonLangMessage.get("form.text.on")%>',
			'form.text.off': '<%=commonLangMessage.get("form.text.off")%>',
			'form.text.account.vipExAdjustment': '<%=commonLangMessage.get("form.text.account.vipExAdjustment")%>',
			'form.text.account.riskRemark': '<%=commonLangMessage.get("form.text.account.riskRemark")%>',
			'form.text.backOffice.remark': '<%=commonLangMessage.get("form.text.backOffice.remark")%>',
			'form.text.backOffice.password': '<%=commonLangMessage.get("form.text.backOffice.password")%>',
			'form.text.account.emailStatus': '<%=commonLangMessage.get("form.text.account.emailStatus")%>',
			'form.text.af.kyc.verificationStatus': '<%=commonLangMessage.get("form.text.af.kyc.verificationStatus")%>',
			'form.text.af.kyc.verificationRemark': '<%=commonLangMessage.get("form.text.af.kyc.verificationRemark")%>',
			'form.text.af.kyc.status.documentPhotoFront-Side': '<%=commonLangMessage.get("form.text.af.kyc.status.documentPhotoFront-Side")%>',
			'form.text.af.kyc.status.documentPhotoBack-Side': '<%=commonLangMessage.get("form.text.af.kyc.status.documentPhotoBack-Side")%>',
			'form.text.af.kyc.status.documentPhotoAddress': '<%=commonLangMessage.get("form.text.af.kyc.status.documentPhotoAddress")%>',
			'form.text.backOffice.documentNo': '<%=commonLangMessage.get("form.text.backOffice.documentNo")%>',
			'form.text.backOffice.expiryDate': '<%=commonLangMessage.get("form.text.backOffice.expiryDate")%>',
			'form.text.backOffice.fullName': '<%=commonLangMessage.get("form.text.backOffice.fullName")%>',
			'form.text.account.dob': '<%=commonLangMessage.get("form.text.account.dob")%>',
			'form.text.backOffice.setting.playResponsibly.sessionExpiry': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.sessionExpiry")%>',
			'form.text.backOffice.setting.playerResponsibility.sessionExpiryNote': '<%=commonLangMessage.get("form.text.backOffice.setting.playerResponsibility.sessionExpiryNote")%>',
			'form.text.backOffice.setting.playResponsibly.selfExclusion': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.selfExclusion")%>',
			'form.text.backOffice.setting.playResponsibly.realityCheck': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.realityCheck")%>',
			'form.text.backOffice.setting.playResponsibly.lossLimits.daily': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.lossLimits.daily")%>',
			'form.text.backOffice.setting.playResponsibly.wagerLimits.daily': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.wagerLimits.daily")%>',
			'form.text.account.card.creditCard': '<%=commonLangMessage.get("form.text.account.card.creditCard")%>',
			'form.text.account.card.bank': '<%=commonLangMessage.get("form.text.account.card.bank")%>',
			'form.text.account.card.cardBrand': '<%=commonLangMessage.get("form.text.account.card.cardBrand")%>',
			'form.text.account.card.cardNumber': '<%=commonLangMessage.get("form.text.account.card.cardNumber")%>',
			'form.text.account.card.expiryDate': '<%=commonLangMessage.get("form.text.account.card.expiryDate")%>',
			'form.text.account.card.cardholderName': '<%=commonLangMessage.get("form.text.account.card.cardholderName")%>',
			'form.text.depositStatus.disapproved': '<%=commonLangMessage.get("form.text.depositStatus.disapproved")%>',
			'form.text.depositStatus.approved': '<%=commonLangMessage.get("form.text.depositStatus.approved")%>',
			'form.text.depositStatus.pending': '<%=commonLangMessage.get("form.text.depositStatus.pending")%>',
			'form.text.af.ui.amount': '<%=commonLangMessage.get("form.text.af.ui.amount")%>',
			'form.text.backOffice.report.subTotal': '<%=commonLangMessage.get("form.text.backOffice.report.subTotal")%>',
			'form.text.backOffice.report.grandTotal': '<%=commonLangMessage.get("form.text.backOffice.report.grandTotal")%>',
			'form.text.backOffice.setting.playResponsibly.wagerLimits.monthly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.wagerLimits.monthly")%>',
			'form.text.backOffice.setting.playResponsibly.wagerLimits.weekly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.wagerLimits.weekly")%>',
			'form.text.backOffice.setting.playResponsibly.depositLimits.monthly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.depositLimits.monthly")%>',
			'form.text.backOffice.setting.playResponsibly.depositLimits.weekly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.depositLimits.weekly")%>',
			'form.text.backOffice.setting.playResponsibly.depositLimits.daily': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.depositLimits.daily")%>',
			'form.text.bank.accountNumber': '<%=commonLangMessage.get("form.text.bank.accountNumber")%>',
			'form.text.backOffice.setting.playResponsibly.lossLimits.monthly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.lossLimits.monthly")%>',
			'form.text.backOffice.setting.playResponsibly.lossLimits.weekly': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.lossLimits.weekly")%>',
			'form.text.backOffice.setting.playResponsibly.accountReviewReminder': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.accountReviewReminder")%>',
			'msg.info.profile.noDocumentImage':'<%=commonLangMessage.get("msg.info.profile.noDocumentImage")%>',
			'form.text.backOffice.setting.playResponsibly.timeSpentLimitDaily': '<%=commonLangMessage.get("form.text.backOffice.setting.playResponsibly.timeSpentLimitDaily")%>'
		});

		$(document).ready(function () {

			//加這個才有Breadcrumbs
			App.init(); // Init layout and core pluginsa
			Plugins.init(); // Init all plugins
			FormComponents.init();

			PageConfig.activeTab = '<%=activeTab%>';
			PageConfig.MoneyTransactionStatusType = <%=MoneyTransactionStatusType.toJsonString()%>;

			PageConfig.AccountSummaryReportType = <%=AccountSummaryReportType.toJsonString(languageType)%>;
			PageConfig.MoneyTransactionStatusType = <%=MoneyTransactionStatusType.toJsonString()%>;
			PageConfig.SystemTxnStatusType = <%=SystemTxnStatusType.toJsonString()%>;

			PageConfig.date = {};
			PageConfig.date.today = '<%=todayEndStr%>';
			PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
			PageConfig.date.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>'
			PageConfig.date.todayOneMonthAgoOnlyDate = '<%=todayOneMonthAgoStrOnlyDate%>';
			PageConfig.date.todayOnlyDate = '<%=todayEndStrOnlyDate%>';

			PageConfig.bonusUpdater = '<%=((Manager) session.getAttribute(SessionKeyConstants.ManagerRole)).getUserId()%>';

			ReportHandler.init();
		});
	</script>
	<link href="/css/manager/riskreport.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>

</head>
<body class="theme-dark">
<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<%-- container start --%>
<div id="container">

	<jsp:include page="/page/manager/include/sidebar2.jsp">
		<jsp:param name="functionTitle" value="Report"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">

		<%-- container2 start --%>
		<div class="container">
			<%
				String functionTitle = "Report";
			%>
			<%@include file="/page/manager/include/head2.jsp" %>

			<!-- 				class="form-vertical"  -->
			<form name='searchProfileGlobalForm' action="#" style='display:none'>
				<input type='hidden' name='userId' value='<%=userId%>'/>
			</form>

			<div class="row">
				<div class="col-md-12">
					<div class="widget box">
						<div class="widget-content">
							<div class="tabbable box-tabs">
								<ul class="nav nav-tabs">
									<%--這裡的排列的順序與顯示相反 --%>
									<li id='tab0' <%=enableViewUpdateLog ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(0);"
										data-toggle="tab"><%=commonLangMessage.get(
										"form.text.report.updateLog")%>
									</a></li>
									<li id='tab1' <%=enableViewLoginLog ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(1);"
										data-toggle="tab"><%=commonLangMessage.get(
										"form.text.report.login")%>
									</a></li>
									<li id='tab3' <%=enableViewDeposit ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(3);"
										data-toggle="tab"><%=commonLangMessage.get(
										"form.text.backOffice.payment.deposit")%>
									</a></li>
									<li id='tab4' <%=enableViewWithdrawal ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(4);"
										data-toggle="tab"><%=commonLangMessage.get(
										"form.text.backOffice.breadcrumbs.withdrawal")%>
									</a></li>
									<li id='tab5' <%=enableViewAdjustment ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(5);"
										data-toggle="tab"><%=commonLangMessage.get(
										"form.text.report.adjustment")%>
									</a></li>
									<li id='tab6' <%=enableViewBetUnsettled ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(6);"
										data-toggle="tab"><%=commonLangMessage.get(
										"ui.text.report.unsettled")%>
									</a></li>
									<li id='tab2' <%=enableViewBetSettled ? "" : "style='display:none'" %>><a href="#"
										onclick="ReportHandler.toggleTab(2);"
										data-toggle="tab"><%=commonLangMessage.get(
										"ui.text.report.settled")%>
									</a></li>
								</ul>
								<div class="tab-content">
									<div class="tab-pane" id="box_tab1">
										<form class="form-vertical" name='searchProfileIPForm' action="#">
											<div class="form-group">
												<div class="row">
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.loginDateRange")%>
														</label>
													</div>
												</div>
												<div class="row next-row">
													<div class="col-md-4">
														<input type="text" class="form-control"
															id="searchProfileIPDaterange"
															name="searchProfileIPDaterange" style="width:300px"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='currencyTypeId'
															value='<%=currency%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='reinitPage'/>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search"
															onclick='ReportHandler.searchProfileLoginLog()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row" id='profileIPResultTable' style='display:none'>
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileIPTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.ipWhitelist.ip")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.loginDate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.dashboard.country")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.account.deviceType")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.platformType")%>
																</th>
															</tr>
															</thead>
														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab0">
										<form class="form-vertical" name='searchProfileUpdateLogForm' action="#">
											<div class="form-group">
												<div class="row">
													<div class="col-md-6">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.updatedDateRange")%>
														</label>
													</div>
													<div class="col-md-6">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.type")%>
														</label>
													</div>
												</div>
												<div class="row next-row">
													<div class="col-md-6">
														<input type="text" class="form-control"
															id="searchProfileUpdateLogDaterange"
															name="searchProfileUpdateLogDaterange" style="width:300px"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='currencyTypeId'
															value='<%=currency%>'/>
													</div>
													<div class="col-md-6">
														<select class="form-control" id="updateType" name="updateType"
															style="width:250px">
															<option value="-99" selected><%=commonLangMessage.get(
																"ui.text.report.all")%>
															</option>
															<%
																for (AccountUpdateType status : accessUpdateTypeOptionSortedList) {
															%>
															<option value="<%=status.unique()%>" style="width:250px;">
																<%=status.getFullName(commonLangMessage)%>
															</option>
															<%
																}
															%>
														</select>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search"
															onclick='ReportHandler.searchProfileUpdateLog()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row" id='profileUpdateLogTable' style='display:none'>
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileUpdateLogTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get("form.text.type")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.beforeUpdate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.afterUpdate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.updatedBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.updateTime")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.updaterIP")%>
																</th>
															</tr>
															</thead>
														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab3">
										<form class="form-vertical" name='searchDepositRecordForm' action="#">

											<div class="form-group">
												<div class="row">
													<!-- 日期范围 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.createdDateRange")%>
														</label>
														<input type="text" class="form-control"
															id="searchProfileUpdateLogDaterange"
															name="searchProfileUpdateLogDaterange"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='transactionType'
															value="<%=MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique()%>"/>
													</div>

													<!-- 金额范围 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.amountRange")%>
														</label>
														<div style="display: flex; align-items: center; gap: 8px;">
															<input
																type="text"
																id="minAmount"
																name="minAmount"
																class="form-control"
																placeholder="Min Amount"
																autocomplete="off"
																onchange="ReportHandler.onChangeSeparatorAdvanced(this,{allowNegative: true})"
																onkeydown="ReportHandler.onKeyDown(event)"
															>
															<label style="font-size: 12px; color: #666; margin: 0;">
																- </label>
															<input
																type="text"
																id="maxAmount"
																name="maxAmount"
																class="form-control"
																placeholder="Max Amount"
																autocomplete="off"
																onchange="ReportHandler.onChangeSeparatorAdvanced(this,{
																		allowNegative: true
																})"
																onkeydown="ReportHandler.onKeyDown(event)"
															>
														</div>
														<label
															class="error-msg-amount-validation"></label>
													</div>

													<!-- 状态类型 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.status")%>
														</label>
														<select class="form-control" id="status" name="status">
															<option value="-999"><%=commonLangMessage.get(
																"ui.text.report.all")%>
															</option>

															<option value="<%=2%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.CONFIRMED") %>
															</option>
															<option value="<%=-2%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.CLOSE") %>
															</option>
															<option value="<%=0%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.NEW") %>
															</option>
														</select>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search"
															onclick='ReportHandler.searchProfileDepositRecord()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row">
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileDepositRecordTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.transactionId")%>
																</th>
																<th id='amount_label_id'><%=commonLangMessage.get(
																	"form.text.af.ui.amount")%>
																	(<%=commonCurrencyTypeSymbol%>
																	)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.status")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.createdBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.staff.createdTime")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.updatedBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.updateTime")%>
																</th>
															</tr>
															</thead>
															<tbody></tbody>
															<tfoot>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id='subtotal_id'
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.subTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='subTotal'></th>
															</tr>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id='grandTotal_id'
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.grandTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='grandTotal'></th>
															</tr>
															</tfoot>

														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab4">
										<form class="form-vertical" name='searchWithdrawalRecordForm' action="#">

											<div class="form-group">
												<div class="row">
													<!-- 日期范围 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.createdDateRange")%>
														</label>
														<input type="text" class="form-control"
															id="searchProfileUpdateLogDaterange"
															name="searchProfileUpdateLogDaterange"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='transactionType'
															value="<%=MoneyTransactionType.WITHDRAWALS.unique()%>"/>
													</div>

													<!-- 金额范围 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.amountRange")%>
														</label>
														<div style="display: flex; align-items: center; gap: 8px;">
															<input
																type="text"
																id="minAmount"
																name="minAmount"
																class="form-control"
																placeholder="Min Amount"
																autocomplete="off"
																onchange="ReportHandler.onChangeSeparatorAdvanced(this,{allowNegative: true})"
																onkeydown="ReportHandler.onKeyDown(event)"
															>
															<label style="font-size: 12px; color: #666; margin: 0;">
																- </label>
															<input
																type="text"
																id="maxAmount"
																name="maxAmount"
																class="form-control"
																placeholder="Max Amount"
																autocomplete="off"
																onchange="ReportHandler.onChangeSeparatorAdvanced(this,{
																		allowNegative: true
																})"
																onkeydown="ReportHandler.onKeyDown(event)"
															>
														</div>
														<label
															class="error-msg-amount-validation"></label>
													</div>

													<!-- 状态类型 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.status")%>
														</label>
														<select class="form-control" id="status" name="status">
															<option value="-999"><%=commonLangMessage.get(
																"ui.text.report.all")%>
															</option>

															<option value="<%=2%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.CONFIRMED") %>
															</option>
															<option value="<%=-2%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.CLOSE") %>
															</option>
															<option value="<%=0%>">
																<%=commonLangMessage.get(
																	"global.text.moneyTransactionStatusType.NEW") %>
															</option>
														</select>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search"
															onclick='ReportHandler.searchProfileWithdrawalRecord()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row">
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileWithdrawalRecordTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.transactionId")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.af.ui.amount")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.status")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.createdBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.staff.createdTime")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.updatedBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.updateTime")%>
																</th>
															</tr>
															</thead>
															<tbody></tbody>
															<tfoot>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id="subtotal_id"
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.subTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='subTotal'></th>
															</tr>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id="grandTotal_id"
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.grandTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='grandTotal'></th>

															</tr>
															</tfoot>

														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab5">
										<form class="form-vertical" name='searchAdjustmentRecordForm' action="#">

											<div class="form-group">
												<div class="row">
													<!-- 日期范围 -->
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.createdDateRange")%>
														</label>
														<input type="text" class="form-control"
															name="searchProfileUpdateLogDaterange"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='transactionType'
															value="<%=MoneyTransactionType.ADJUSTMENT.unique()%>"/>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search"
															onclick='ReportHandler.searchProfileAdjustmentRecord()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row">
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileAdjustmentRecordTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.transactionId")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.af.ui.amount")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.createdBy")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.staff.createdTime")%>
																</th>
															</tr>
															</thead>
															<tbody></tbody>
															<tfoot>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id='subtotal_id'
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.subTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)

																</th>
																<th name='subTotal'></th>
															</tr>
															<tr>
																<th colspan="2"
																	style="text-align:right"
																	id='grandTotal_id'
																><%=commonLangMessage.get(
																	"form.text.backOffice.report.grandTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='grandTotal'></th>
															</tr>
															</tfoot>
														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab6">
										<form class="form-vertical" name='searchProfileReportBetUnsettledForm' action="#">
											<div class="form-group">
												<div class="row">
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.transactionDateRange")%>
														</label>
													</div>
												</div>
												<div class="row next-row">
													<div class="col-md-4">
														<input type="text" class="form-control"
															id="searchProfileReportDaterangeForBetUnsettled"
															name="searchProfileReportDaterange" style="width:300px"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='currencyTypeId'
															value='<%=currency%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='reinitPage'/>
														<input type='hidden' name='transactionType'
															value='<%=AccountSummaryReportType.BET.unique()%>'/>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search" onclick='ReportHandler.searchProfileReportBetUnsettled()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row" id='profileReportBetUnsettledResultTable' style='display:none'>
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileReportBetUnsettledTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.transactionDate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.createdDate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.vendor")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.gameType")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.game")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.odds")%>
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.oddsType")%>
																</th>
																<th><%=commonLangMessage.get(
																	"ui.text.report.bet")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.view")%>
																</th>
															</tr>
															</thead>
															<tfoot>
															<tr>
																<th colspan="8"
																	style="text-align:right"><%=commonLangMessage.get(
																	"form.text.backOffice.report.subTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='subTotal'></th>
																<th></th>
															</tr>
															<tr>
																<th colspan="8"
																	style="text-align:right"><%=commonLangMessage.get(
																	"form.text.backOffice.report.grandTotal")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th name='grandTotal'></th>
																<th></th>
															</tr>
															</tfoot>
														</table>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="tab-pane" id="box_tab2">
										<form class="form-vertical" name='searchProfileReportBetSettledForm' action="#">
											<div class="form-group">
												<div class="row">
													<div class="col-md-4">
														<label class="control-label"><%=commonLangMessage.get(
															"form.text.backOffice.report.transactionDateRange")%>
														</label>
													</div>
												</div>
												<div class="row next-row">
													<div class="col-md-4">
														<input type="text" class="form-control"
															id="searchProfileReportDaterangeForBetSettled"
															name="searchProfileReportDaterange" style="width:300px"/>
														<input type='hidden' name='userId' value='<%=userId%>'/>
														<input type='hidden' name='currencyTypeId'
															value='<%=currency%>'/>
														<input type='hidden' name='searchDateRange'/>
														<input type='hidden' name='reinitPage'/>
														<input type='hidden' name='transactionType'
															value='<%=AccountSummaryReportType.BET.unique()%>'/>
													</div>
												</div>
											</div>
											<div class="form-group">
												<div class="row">
													<div class="col-md-12">
														<input type="button"
															value="<%=commonLangMessage.get("form.text.button.search")%>"
															name="search" onclick='ReportHandler.searchProfileReportBetSettled()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row" id='profileReportBetSettledResultTable' style='display:none'>
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProfileReportBetSettledTable"
															class="table table-striped table-bordered" cellspacing="0"
															width="100%">
															<thead>
															<tr>
																<th>#</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.transactionDate")%>
																</th>
																<th><%=commonLangMessage.get(
																	"ui.text.report.bet")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.profit")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.turnover")%>
																	(<%=commonCurrencyTypeSymbol%>)
																</th>
																<th><%=commonLangMessage.get(
																	"form.text.backOffice.report.view")%>
																</th>
															</tr>
															</thead>
														</table>
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

		</div>
		<%-- container2 end --%>

			<jsp:include page="_documentPhoto.jsp">
				<jsp:param name="documentPhotoTitle" value="<%=commonLangMessage.get(\"form.text.documentType.document\")%>" />
			</jsp:include>

		<div class="modal fade" id="betSummaryDetailsModal" role="dialog">
			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							&times;
						</button>
						<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
							"form.text.backOffice.report.betDetails")%>
						</h4>
					</div>

					<form name='searchDetailGlobalForm' action="#" style='display:none'>
						<input type='hidden' name='userId' value='<%=userId%>'/>
						<input type='hidden' name='transactionDate'/>
					</form>
					<div class="modal-body">
						<form name='searchBetDetailForm' action="#"
							style='display:none'>
							<input type='hidden' name='userId' value='<%=userId%>'/>
							<input type='hidden' name='transactionDate'/>
							<input type='hidden' name='vendorId'/>
							<input type='hidden' name='gameType'/>
							<input type='hidden' name='reinitPage'/>
							<input type='hidden' name='turnoverId'/>
						</form>
						<table class="table table-hover table-bordered"
							id='betSummaryDetailsTable' width='100%' style='width:100%'>
							<thead>
							<tr>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.payment.transactionDate")%>
								</th>
								<th><%=commonLangMessage.get(
									"ui.text.slot.vendor_group")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.af.ui.gameType")%>
								</th>
								<th><%=commonLangMessage.get(
									"ui.text.report.txnBet")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.profit")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.turnover")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.view")%>
								</th>
							</tr>
							</thead>
							<tfoot>
							<tr>
								<th colspan="3"
									style="text-align:right"><%=commonLangMessage.get(
									"form.text.backOffice.report.subTotal")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th name='amount'></th>
								<th name='profit'></th>
								<th name='turnover'></th>
								<th></th>
							</tr>
							<tr>
								<th colspan="3"
									style="text-align:right"><%=commonLangMessage.get(
									"form.text.backOffice.report.grandTotal")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th name='totalAmount'></th>
								<th name='totalProfit'></th>
								<th name='totalTurnover'></th>
								<th></th>
							</tr>
							</tfoot>
						</table>
					</div><!-- end modal-body -->
				</div><!-- end modal-content -->
			</div>
		</div>

		<div class="modal fade" id="betDetailsModal" role="dialog">
			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							&times;
						</button>
						<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
							"form.text.backOffice.report.betDetails")%>
						</h4>
					</div>

					<div class="modal-body">
						<table class="table table-hover table-bordered"
							id='betDetailsTable' style='width:100%'>
							<thead>
							<tr>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.payment.transactionDate")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.settledDate")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.bonus.createdDate")%>
								</th>
								<th><%=commonLangMessage.get(
									"ui.text.slot.vendor_group")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.af.ui.gameType")%>
								</th>
								<th><%=commonLangMessage.get(
									"ui.text.report.txnGame")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.odds")%>
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.oddsType")%>
								</th>
								<th name="balanceBeforeCol"><%=commonLangMessage.get(
									"form.text.backOffice.report.balanceBefore")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"ui.text.report.txnBet")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.profit")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th name="balanceAfterCol"><%=commonLangMessage.get(
									"form.text.backOffice.report.balanceAfter")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.turnover")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th><%=commonLangMessage.get(
									"form.text.backOffice.report.view")%>
								</th>
							</tr>
							</thead>
							<tfoot>
							<tr>
								<th colspan="9"
									style="text-align:right"><%=commonLangMessage.get(
									"form.text.backOffice.report.subTotal")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th name='betAmount'></th>
								<th name='profitLoss'></th>
								<th></th>
								<th name='turnover'></th>
								<th></th>
							</tr>
							<tr>
								<th colspan="9" style="text-align:right">
									<%=commonLangMessage.get(
									"form.text.backOffice.report.grandTotal")%>
									(<%=commonCurrencyTypeSymbol%>)
								</th>
								<th name='totalBetAmount'></th>
								<th name='totalProfitLoss'></th>
								<th></th>
								<th name='totalTurnover'></th>
								<th></th>
							</tr>
							</tfoot>
						</table>
					</div><!-- end modal-body -->
				</div><!-- end modal-content -->
			</div>
		</div>
	</div>
	<%-- content end --%>
</div>
<%-- container end --%>
</body>
</html>
