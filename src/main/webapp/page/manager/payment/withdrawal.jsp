<%@page import="com.nv.commons.constants.AccountStatusType" %>
<%@page import="java.util.List" %>
<%@page import="com.nv.commons.constants.ReportExportType" %>
<%@page import="com.nv.commons.constants.MoneyTransactionStatusType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionType" %>

<%@ page import="com.nv.commons.constants.PaymentType" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>

<%@ page import="com.nv.commons.constants.RemarkTemplateType" %>
<%@ page import="com.nv.commons.constants.WithdrawalSearchType" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="com.nv.commons.constants.FunctionType" %>

<%
	FrontendUtils.noCache(response);

	Date now = DateTimeBuilder.localDateTime().withMinTime().toCalendar().getTime();

	String todayStr = DateUtils
		.toString(DateUtils.getSpecifyDate(now, DateUtils.END), FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayHalfYearAgo = DateUtils.getNextNMonth(now, -SystemConstants.MAX_LIMIT_MONTH);
	String todayHalfYearAgoStr = DateUtils.toString(todayHalfYearAgo,
		FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayWeekAgo = DateUtils.getNextNDay(now, -7);
	String todayWeekAgoStr = DateUtils.toString(todayWeekAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayOneMonthAgo = DateUtils.getNextNMonth(now, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);
	List<String> titleList = ReportExportType.WITHDRAWAL.getTitleList(null);

	int minutes = RequestParser.getIntParameter(request, "minutes", -99);


%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/formComponent.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.14")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.20")%>
	</title>


	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--sidebar.jsp在用 --%>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/ExcelUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript"
		src="/js/manager/payment/withdrawal.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;
		PageConfig.userId = '<%=((Manager) session.getAttribute(SessionKeyConstants.ManagerRole)).getUserId()%>';

		PageConfig.lang = "<%=commonLangMessage%>";

		PageConfig.date = {};
		PageConfig.date.today = '<%=todayStr%>';
		PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
		PageConfig.date.todayWeekAgo = '<%=todayWeekAgoStr%>';
		PageConfig.date.format = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';

		PageConfig.paymentType = {};
		PageConfig.paymentType.LOCAL_BANKING = <%=PaymentType.LOCAL_BANKING.unique()%>;


		PageConfig.accessRight = {};
		PageConfig.accessRight.createWithdrawal = <%=FunctionType.No21.unique()%>;
		PageConfig.accessRight.approveWithdrawal = <%=FunctionType.No23.unique()%>;
		PageConfig.accessRight.verifyWithdrawal = <%=FunctionType.VERIFY_WITHDRAWAL.unique()%>;
		PageConfig.accessRight.rejectWithdrawal = <%=FunctionType.REJECT_WITHDRAWAL.unique()%>;
		PageConfig.accessRight.holdWithdrawal = <%=FunctionType.HOLD_WITHDRAWAL.unique()%>;
		PageConfig.remarkType = <%=RemarkTemplateType.WITHDRAWAL.unique()%>;
		PageConfig.paymentTypeJson = <%=PaymentType.toJsonString()%>;
		PageConfig.date.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>';
		//set I18N
		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.payment': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.payment")%>',
			'form.text.backOffice.breadcrumbs.withdrawal': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.withdrawal")%>',
			'form.text.backOffice.payment.approve': '<%=commonLangMessage.get("form.text.backOffice.payment.approve")%>',
			'form.text.backOffice.payment.disapprove': '<%=commonLangMessage.get("form.text.backOffice.payment.disapprove")%>',
			'form.text.backOffice.payment.reject': '<%=commonLangMessage.get("form.text.backOffice.payment.reject")%>',
			'form.text.backOffice.payment.edit': '<%=commonLangMessage.get("form.text.backOffice.payment.edit")%>',
			'global.text.moneyTransactionType.DEPOSIT': '<%=commonLangMessage.get("global.text.moneyTransactionType.DEPOSIT")%>',
			'global.text.moneyTransactionType.WITHDRAWALS': '<%=commonLangMessage.get("global.text.moneyTransactionType.WITHDRAWALS")%>',
			'global.text.moneyTransactionType.DEPOSIT_PAYMENT_GATEWAY': '<%=commonLangMessage.get("global.text.moneyTransactionType.DEPOSIT_PAYMENT_GATEWAY")%>',
			'global.text.moneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY': '<%=commonLangMessage.get("global.text.moneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY")%>',
			'global.text.moneyTransactionType.ADJUSTMENT': '<%=commonLangMessage.get("global.text.moneyTransactionType.ADJUSTMENT")%>',
			'global.text.moneyTransactionStatusType.NEW': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.NEW")%>',
			'global.text.moneyTransactionStatusType.PENDING_APPROVAL': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.PENDING_APPROVAL")%>',
			'global.text.moneyTransactionStatusType.REJECTED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.REJECTED")%>',
			'global.text.moneyTransactionStatusType.CONFIRMED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.CONFIRMED")%>',
			'global.text.moneyTransactionStatusType.CLOSE': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.CLOSE")%>',
			'global.text.moneyTransactionStatusType.ON_HOLD': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.ON_HOLD")%>',
			'global.text.moneyTransactionStatusType.PROCESSING': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.PROCESSING")%>',
			'global.text.moneyTransactionStatusType.REVERTED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.REVERTED")%>',
			'global.text.moneyTransactionStatusType.AWAITED': '<%=commonLangMessage.get("global.text.moneyTransactionStatusType.AWAITED")%>',
			'global.text.fail': '<%=commonLangMessage.get("global.text.fail")%>',
			'ui.text.report.all': '<%=commonLangMessage.get("ui.text.report.all")%>',
			'ui.text.deposit.select_bank': '<%=commonLangMessage.get("ui.text.deposit.select_account_bank")%>',
			'msg.error.validation.required': '<%=commonLangMessage.get("msg.error.validation.required")%>',
			'msg.error.validation.transactionIdIsInvalid': '<%=commonLangMessage.get("msg.error.validation.transactionIdIsInvalid")%>',
			'msg.error.info.bankNameNotValid': '<%=commonLangMessage.get("msg.error.info.bankNameNotValid")%>',
			'msg.error.info.distinctExecutor': '<%=commonLangMessage.get("msg.error.info.distinctExecutor")%>',
			'ui.text.plz_select': '<%=commonLangMessage.get("ui.text.plz_select")%>',
			'ui.text.withdraw.min_max_amount': '<%=commonLangMessage.get("ui.text.withdraw.min_max_amount")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.suspend': '<%=commonLangMessage.get("form.text.backOffice.status.suspend")%>',
			'form.text.backOffice.status.locked': '<%=commonLangMessage.get("form.text.backOffice.status.locked")%>',
			'msg.info.backOffice.checkTransactionProcess': '<%=commonLangMessage.get("msg.info.backOffice.checkTransactionProcess")%>',
			'msg.info.backOffice.checkProcess': '<%=commonLangMessage.get("msg.info.backOffice.checkProcess")%>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.info.backOffice.reportIsProduction': '<%=commonLangMessage.get("msg.info.backOffice.reportIsProduction")%>',
			'ui.text.wallet.deposit.to_bank': '<%=commonLangMessage.get("ui.text.wallet.deposit.to_bank")%>',
			'form.text.button.save': '<%=commonLangMessage.get("form.text.button.save")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.deposit.needSelectBank': '<%=commonLangMessage.get("msg.deposit.needSelectBank")%>',
			'form.text.backOffice.customerFee.feeType.Overdue': '<%=commonLangMessage.get("form.text.backOffice.customerFee.feeType.Overdue")%>',
			'ui.text.reset': '<%=commonLangMessage.get("ui.text.reset")%>',
			'form.text.backOffice.needSelectExportType': '<%=commonLangMessage.get("form.text.backOffice.needSelectExportType")%>',
			'form.text.backOffice.exportType.title': '<%=commonLangMessage.get("form.text.backOffice.exportType.title")%>',
			'ui.text.deposit.select_remark': '<%=commonLangMessage.get("ui.text.deposit.select_remark")%>',
			'form.text.backOffice.file.noFileSelected': '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>',
			'msg.error.info.image.sizeIsLarge': '<%=commonLangMessage.get("msg.error.info.image.sizeIsLarge")%>',
			'form.text.backOffice.show': '<%=commonLangMessage.get("form.text.backOffice.show")%>',
			'form.text.backOffice.hide': '<%=commonLangMessage.get("form.text.backOffice.hide")%>',
			'msg.remark.template.required': '<%=commonLangMessage.get("msg.remark.template.required")%>',
			'msg.remark.required': '<%=commonLangMessage.get("msg.remark.required")%>',
			'form.text.account.email': '<%=commonLangMessage.get("form.text.account.email")%>',
			'form.text.account.phoneNumber': '<%=commonLangMessage.get("form.text.account.phoneNumber")%>',
			'fe.text.documentType.address': '<%=commonLangMessage.get("fe.text.documentType.address")%>',
			'form.text.backOffice.account': '<%=commonLangMessage.get("form.text.backOffice.account")%>',
			'form.text.backOffice.payment.process': '<%=commonLangMessage.get("form.text.backOffice.payment.process")%>',
			'global.text.moneyTransactionProcessingStatusType.manual': '<%=commonLangMessage.get("global.text.moneyTransactionProcessingStatusType.manual")%>',
			'global.text.moneyTransactionProcessingStatusType.pg': '<%=commonLangMessage.get("global.text.moneyTransactionProcessingStatusType.pg")%>',
			'msg.error.validation.cantNotBeAll': '<%=commonLangMessage.get("msg.error.validation.cantNotBeAll")%>',
			'fe.text.paymentMethods': '<%=commonLangMessage.get("fe.text.paymentMethods")%>',
			'fe.error.validation.requireAmount': '<%=commonLangMessage.get("fe.error.validation.requireAmount")%>',
			'fe.error.validation.min': '<%=commonLangMessage.get("fe.error.validation.min")%>',
			'fe.error.validation.max': '<%=commonLangMessage.get("fe.error.validation.max")%>',
			'fe.text.upiCode': '<%=commonLangMessage.get("fe.text.upiCode")%>',
			'fe.text.yourUpiId': '<%=commonLangMessage.get("fe.text.yourUpiId")%>',
			'form.text.bank.bankAccNumber': '<%=commonLangMessage.get("form.text.bank.bankAccNumber")%>',
			'ui.text.withdrawal.select_UPI_code': '<%=commonLangMessage.get("ui.text.withdrawal.select_UPI_code")%>',
			'fe.error.validation.upiCode': '<%=commonLangMessage.get("fe.error.validation.upiCode")%>',
			'fe.text.noData': '<%=commonLangMessage.get("fe.text.noData")%>',
			'form.text.af.ui.amount': '<%=commonLangMessage.get("form.text.af.ui.amount")%>',
			'ui.text.paymentType.creditCard': '<%=commonLangMessage.get("ui.text.paymentType.creditCard")%>',
			'ui.text.paymentType.onlineBanking': '<%=commonLangMessage.get("ui.text.paymentType.onlineBanking")%>'
		});
		$(document).ready(function () {

			//加這個才有Breadcrumbs
			<%-- head.jsp要用 --%>
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins

			{
				//直接用plugins.form-components.js，而不是app.jsp
				// Fix for class_selector in BS3 plugins.form-components.js
				var _base_resetForm = $.validator.prototype.resetForm;
				$.extend($.validator.prototype, {
					resetForm: function () {
						var resetForm_this = this;
						_base_resetForm.call(this);

						var currentForm = $(this.currentForm);
						var class_selector = ".form-group";
						if (currentForm.hasClass('form-vertical')) {
							class_selector = "*[class^=col-]";
						}

						currentForm.find(class_selector).each(function () {
							$(this).removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);
						});
						currentForm.find('.select2-container').removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);

						currentForm.find('label[generated="true"]').html('');
					}
				});
			}
			FormComponents.init();

			HeadHandler.init();
			MenuHandler.init();

			PageConfig.AccountStatusType = <%=AccountStatusType.toJsonString()%>;
			PageConfig.MoneyTransactionStatusType = <%=MoneyTransactionStatusType.toJsonString()%>;
			PageConfig.MoneyTransactionType = <%=MoneyTransactionType.toJsonString()%>;
			PageConfig.WithdrawalSearchType = <%=WithdrawalSearchType.toJsonString()%>;


			WithdrawalHandler.init();

			setTimeout(function () {
				const minutes = <%=minutes%>;
				if (minutes !== -99) {
					$('[name=searchForm]').find('[name=minutes]').val(minutes);
					$('[name=searchForm]').find('[name=searchFromMenu]').val('true');
					WithdrawalHandler.search();
				}
			}, 0);
		});
	</script>
	<style>
      #processWithdrawalForm .input-comment, #batchProcessWithdrawalForm .input-comment, #batchApproveWithdrawalForm .input-comment {
        margin-top: 3px;
        color: #888888;
        font-size: 14px;
      }

      #processWithdrawalForm .input-comment span, #batchProcessWithdrawalForm .input-comment span, #batchApproveWithdrawalForm .input-comment span {
        color: #ff0000;
      }

      .input-container {
        position: relative;
        flex-grow: 1;
      }

      .input-container input {
        width: 100%;
        padding: 5px 30px 5px 5px; /* 留空间给 icon */
        font-size: 14px;
        border: 1px solid #ccc;
        border-radius: 4px;
        box-sizing: border-box;
      }

      .input-container i {
        position: absolute;
        top: 50%;
        right: 20px;
        transform: translateY(-50%);
        cursor: pointer;
        color: #555;
        font-size: 16px;
      }

      .modal-xlg {
        width: 90%;
      }
	</style>
	<link href="/css/manager/riskreport.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>

</head>
<body class="theme-dark">
<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<%-- container start --%>
<div id="container">

	<%@include file="/page/manager/include/sidebar.jsp" %>
	<!-- /Sidebar -->

	<%-- content start --%>
	<div id="content">


		<%-- container2 start --%>
		<div class="container">

			<%@include file="/page/manager/include/head.jsp" %>

			<div class="modal fade" id="detailModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.withdrawalDetails")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row">
								<div class='col-md-12'>
									<div class="widget-content">
										<div class="row">
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.report.member.email")%>
												</label>

												<div class="control-label" name="email" id="email">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.backOffice.report.transactionId")%>
												</label>
												<div class="control-label" name="transactionId"
													id="transactionId">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.report.payment.status")%>
												</label>
												<div class="control-label" name="status" id="status">
												</div>
											</div>
										</div>
										<br/>
										<br/>
										<div class="row">
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.backOffice.payment.createdTime")%>
												</label>

												<div class="control-label" name="createdTime" id="createdTime">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"global.text.updatedTime")%>
												</label>
												<div class="control-label" name="updatedTime"
													id="updatedTime">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.backOffice.payment.withdrawalMethod")%>
												</label>
												<div class="control-label" name="withdrawMethod" id="withdrawMethod">
												</div>
											</div>
										</div>
										<br/>
										<br/>
										<div class="row">
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.bank")%>
												</label>

												<div class="control-label" name="bank" id="bank">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"ui.text.payment.credit.cardNumber")%>
												</label>
												<div class="control-label" name="accountNumber"
													id="accountNumber">
												</div>
											</div>
											<div class="col-md-4">
												<label class="control-label"><%=commonLangMessage.get(
													"form.text.af.ui.amount")%>
												</label>
												<div class="control-label" name="amount" id="amount">
												</div>
											</div>
										</div>
										<br/>
										<br/>
										<div id="buttonsPlaceHolder">
										</div>
									</div>
								</div>
							</div>

						</div><!-- end modal-body -->
					</div><!-- end modal-content -->
				</div>
			</div>


			<div class="modal fade" id="createWithdrawalModal" role="dialog">
				<div class="modal-dialog modal-xlg">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.payment.createWithdrawal")%>
							</h4>
						</div>
						<div class="modal-body">

							<!-- Email 输入框 -->
							<div class="row">
								<form class="form-horizontal" name='searchBalanceAndAccountBankForm' action="#">
									<div class="col-md-4">
										<div class="form-group">
											<label class="control-label">
												<%=commonLangMessage.get("form.text.account.email")%><span
												class="required">*</span>
											</label>
											<input type="text" name="email" id="emailSelect2"
												placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
											<input type='hidden' id='currencySelect2'
												value="<%=commonCurrencyType.unique()%>">
										</div>
									</div>
								</form>
								<form class="form-horizontal" name='createForm' action="#">
									<div id='generalDiv' style='display:none' class="col-md-8">
										<div class="row" style="display: flex; gap: 20px;">
											<div class="col-md-6">
												<div class="form-group">
													<label class=" control-label">
														<%=commonLangMessage.get(
															"ui.text.payment.bank.cardNumber")%><span
														class="required">*</span>
													</label>

													<select class="form-control" id="bankAccountNumberId"
														name="id">
													</select>
												</div>
											</div>

											<div class="col-md-6">
												<div class="form-group">
													<label class="control-label">
														<%=commonLangMessage.get("ui.text.amount")%>
														(<%=commonCurrencyTypeSymbol%>)<span
														class="required">*</span>
													</label>

													<input type="text" id='amountTest' name="amount"
														class="form-control"
														placeholder="<%=commonLangMessage.get("ui.text.amount")%>"
														onchange="WithdrawalHandler.validateMaxAmount(this,PageConfig.availableBalance)"
														onkeydown="WithdrawalHandler.onKeyDown(event)"
													>
													<label id="checkboxAmount">

													</label>

												</div>
											</div>
										</div>
									</div>
								</form>
							</div>


						</div><!-- end modal-body -->
						<div class="modal-footer">
							<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>" name="resetButton"
								onclick='WithdrawalHandler.resetCreateWithdrawal()' class="btn btn-primary">
							<input type="button" value="<%=commonLangMessage.get("ui.text.submit")%>" name="save"
								onclick='WithdrawalHandler.create()' class="btn btn-primary">
						</div>
					</div><!-- end modal-content -->
				</div>
			</div>

			<div class="modal fade in" id="batchApproveWithdrawalModal" role="dialog">
				<div class="modal-dialog modal-xlg">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.approveDisapproveMultipleWithdrawals")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row" id="resultBatchTable">
								<div class='col-md-12'>
									<div class="widget box">
										<div class="widget-content">
											<table class="table table-hover" id="batchWithdrawalTable">
												<tr>
													<th><%=commonLangMessage.get("form.text.report.member.email")%>
													</th>
													<th><%=commonLangMessage.get(
														"form.text.backOffice.report.transactionId")%>
													</th>

													<th><%=commonLangMessage.get(
														"form.text.backOffice.payment.createdTime")%>
													</th>
													<th><%=commonLangMessage.get(
														"form.text.backOffice.payment.withdrawalMethod")%>
													</th>
													<th><%=commonLangMessage.get(
														"form.text.bank")%>
													</th>
													<th><%=commonLangMessage.get(
														"ui.text.payment.credit.cardNumber")%>
													</th>
													<th><%=commonLangMessage.get(
														"form.text.af.ui.amount")%> (<%=commonCurrencyTypeSymbol%>)
													</th>
												</tr>
												<tbody id='batchWithdrawalContainer'>
												</tbody>
												<tbody id='withdrawalContainer'>
												</tbody>
											</table>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="modal-footer">

							<input type="button"
								value="<%=commonLangMessage.get("form.text.backOffice.payment.disapprove")%>"
								name="reject" onclick='WithdrawalHandler.batchReject()' class="btn btn-primary">

							<input type="button"
								value="<%=commonLangMessage.get("form.text.backOffice.payment.approve")%>"
								name="approve"
								onclick='WithdrawalHandler.batchApproveWithdrawal(this)' class="btn btn-primary">
						</div>
					</div>
				</div>
			</div>

			<%-- Statboxes start --%>
			<!-- 				 row-bg -->
			<div class="row">
				<div class="col-md-12">
					<div class="widget box">
						<div class="widget-header">
							<h4>
								<i class="icon-reorder"></i> <%=commonLangMessage.get(
								"form.text.backOffice.conditionPanel")%>
							</h4>
							<div class="toolbar no-padding">
								<div class="btn-group">
									<span class="btn btn-xs widget-collapse"><i class="icon-angle-down"></i></span>
								</div>
							</div>
						</div>
						<div class="widget-content">
							<form class="form-vertical" name='searchForm' action="#">
								<div class="form-group">
									<div class="row">
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.account.email")%>
											</label>
											<input type="text" name="email" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.report.transactionId")%>
											</label>
											<input type="text" name="transactionId" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.backOffice.report.transactionId")%>">
										</div>

										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage
												.get("ui.text.raf.report.status")%>
											</label>
											<select class="form-control" id="status" name="status">
												<option value="-999"><%=commonLangMessage.get("ui.text.report.all")%>
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
										<div class="col-md-8">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.amountRange")%>
											</label>
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.createdBy")%>
											</label>
										</div>
									</div>
									<div class="row next-row">
										<div class="col-md-8">
											<div style="display: flex; align-items: center; gap: 8px;">
												<input
													type="text"
													id="minAmount"
													name="minAmount"
													class="form-control"
													placeholder="Min Amount"
													autocomplete="off"
													onchange="WithdrawalHandler.onChangeSeparatorAdvanced(this,{allowNegative: true})"
													onkeydown="WithdrawalHandler.onKeyDown(event)"
												>
												<label style="font-size: 12px; color: #666; margin: 4px;">
													- </label>
												<input
													type="text"
													id="maxAmount"
													name="maxAmount"
													class="form-control"
													placeholder="Max Amount"
													autocomplete="off"
													onchange="WithdrawalHandler.onChangeSeparatorAdvanced(this,{
																		allowNegative: true
																})"
													onkeydown="WithdrawalHandler.onKeyDown(event)"
												>
											</div>
											<span
												class="error-msg-amount-validation"></span>

										</div>
										<div class="col-md-4">

											<input type="text" id="createdBy" name="createdBy"
												placeholder="Created By"
												class="form-control">
										</div>
									</div>
								</div>
								<div class="form-group">
									<div class="row">
										<div class="col-md-4">
											<%--不含reject --%>
											<label class="control-label"><%=commonLangMessage
												.get("form.text.backoffice.createdSince")%>
											</label>
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage
												.get("form.text.backOffice.updatedBy")%>
											</label>
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage
												.get("form.text.backoffice.updatedSince")%>
											</label>
										</div>
									</div>
									<div class="row next-row">
										<div class="col-md-4">
											<div class='input-container'>
												<input type="text" class="form-control singleDateTimePicker"
													name='createdSince' id="createdSince"
													placeholder="<%=commonLangMessage.get("form.text.backoffice.createdSince")%>">
												<i class="icon-calendar"></i>
											</div>
										</div>
										<div class="col-md-4">
											<input type="text" name="updatedBy" id="updatedBy" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.backOffice.report.updatedBy")%>">
										</div>
										<div class="col-md-4">
											<div class='input-container'>
												<input type="text" class="form-control singleDateTimePicker"
													name='updatedSince' id="updatedSince"
													placeholder="<%=commonLangMessage.get("form.text.backoffice.updatedSince")%>">
												<i class="icon-calendar"></i>
											</div>
										</div>
									</div>
								</div>
							</form>
							<div class="row">
								<div class="col-md-12">
									<div class="widget">
										<input type="button"
											value="<%=commonLangMessage.get("form.text.button.create")%>"
											name="create"
											onclick='WithdrawalHandler.gotoCreateWithdrawal()' class="btn btn-primary">
										<input type="button"
											value="<%=commonLangMessage.get("form.text.button.search")%>"
											name="search"
											onclick='WithdrawalHandler.search()' class="btn btn-primary">
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<%-- result start --%>
			<div class="row" id='resultTable' style='display:none'>
				<div class="col-md-12">
					<div class="widget box">
						<div id='batchApproveButton' style='display:none'>
							<input type="button"
								value="<%=commonLangMessage.get("form.text.backOffice.payment.approveDisapprove")%>"
								name="batchApprove" onclick='WithdrawalHandler.goToBatchApprove()'
								class="btn btn-primary" style='display:none'>
						</div>
						<div class="widget-content">
							<table id="withdrawalSearchTable" class="table table-striped table-bordered"
								cellspacing="0" width="100%">
								<thead>
								<tr>
									<th><label class="checkbox-inline"><input type="checkBox" class="uniform"
										id="checkAll"/></label></th>
									<%
										for (int i = 0; i < titleList.size(); i++) {
											String title = commonLangMessage.get(titleList.get(i));
											String thid = title.toLowerCase().replace(" ", "_");
											if (i == 0) {
												continue;
											}
											if (title.equalsIgnoreCase("amount")) {
												title =
													title + " (" + commonCurrencyTypeSymbol + ")";
											}
									%>
									<th id="<%=thid%>_label_id">
										<%=title%>
									</th>
									<%
										}
									%>
								</tr>
								</thead>
							</table>
						</div>
					</div>
				</div>
			</div>
			<%-- result end --%>

		</div>
		<%-- container2 end --%>

	</div>
	<%-- content end --%>

</div>
<%-- container end --%>

</body>
</html>
