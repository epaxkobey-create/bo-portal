<%@page import="com.nv.commons.constants.AccountStatusType" %>
<%@ page import="java.util.List" %>
<%@ page import="com.nv.commons.constants.ReportExportType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionStatusType" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionType" %>
<%@ page import="com.nv.commons.utils.JSONUtils" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="com.nv.commons.constants.RemarkTemplateType" %>
<%@ page import="com.nv.commons.constants.FunctionType" %>
<%@ page import="com.nv.commons.constants.PaymentType" %>


<%
	FrontendUtils.noCache(response);


	Date now = DateTimeBuilder.localDateTime().withMinTime().toDate();

	String todayStr = DateUtils.toString(DateUtils.getSpecifyDate(now, DateUtils.END),
		FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayHalfYearAgo = DateUtils.getNextNMonth(now, -SystemConstants.MAX_LIMIT_MONTH);
	String todayHalfYearAgoStr = DateUtils.toString(todayHalfYearAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayWeekAgo = DateUtils.getNextNDay(now, -7);
	String todayWeekAgoStr = DateUtils.toString(todayWeekAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayOneMonthAgo = DateUtils.getNextNMonth(now, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	List<String> titleList = ReportExportType.DEPOSIT.getTitleList(null);

	String remarkTemplate = "{}";

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
		"form.text.backOffice.menu.15")%>
	</title>


	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--sidebar.jsp在用 --%>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript"
		src="/js/manager/payment/deposit.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<style type="text/css">
      .loading {
        color: transparent !important;
        background: url('/img/ajax-loading-input.gif') no-repeat 50% 50%;
      }

      .modal-xlg {
        width: 90%;
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

	</style>

	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;
		PageConfig.userId = '<%=((Manager) session.getAttribute(SessionKeyConstants.ManagerRole)).getUserId()%>';
		PageConfig.lang = "<%=commonLangMessage%>";
		PageConfig.remark = <%=JSONUtils.toJsonString(remarkTemplate)%>;


		PageConfig.date = {};
		PageConfig.date.today = '<%=todayStr%>';
		PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
		PageConfig.date.todayWeekAgo = '<%=todayWeekAgoStr%>';
		PageConfig.date.todayOneMonthAgo ='<%=todayOneMonthAgoStr%>';
		PageConfig.date.format = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';

		PageConfig.accessRight = {};
		PageConfig.accessRight.createDeposit = <%=FunctionType.No16.unique()%>;
		PageConfig.accessRight.approveDeposit = <%=FunctionType.No18.unique()%>;
		PageConfig.accessRight.rejectDeposit = <%=FunctionType.REJECT_DEPOSIT.unique()%>;
		PageConfig.accessRight.editDeposit = <%=FunctionType.DEPOSIT_ATTACHMENT.unique()%>;
		PageConfig.paymentTypeJson = <%=PaymentType.toJsonString()%>;


		//set I18N
		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.payment': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.payment")%>',
			'form.text.backOffice.breadcrumbs.deposit': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.deposit")%>',
			'form.text.backOffice.payment.approve': '<%=commonLangMessage.get("form.text.backOffice.payment.approve")%>',
			'form.text.backOffice.payment.transferCheckResult': '<%=commonLangMessage.get("form.text.backOffice.payment.TransactionCheckResult")%>',
			'form.text.backOffice.payment.disapprove': '<%=commonLangMessage.get("form.text.backOffice.payment.disapprove")%>',
			'form.text.backOffice.payment.reject': '<%=commonLangMessage.get("form.text.backOffice.payment.reject")%>',
			'form.text.backOffice.payment.edit': '<%=commonLangMessage.get("form.text.backOffice.payment.edit")%>',
			'form.text.backOffice.payment.resubmit': '<%=commonLangMessage.get("form.text.backOffice.payment.resubmit")%>',
			'form.text.backOffice.payment.checkOrder': '<%=commonLangMessage.get("form.text.backOffice.payment.checkOrder")%>',
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
			'form.text.backOffice.show': '<%=commonLangMessage.get("form.text.backOffice.show")%>',
			'form.text.backOffice.hide': '<%=commonLangMessage.get("form.text.backOffice.hide")%>',
			'form.text.backOffice.payment.accountBank': '<%=commonLangMessage.get("form.text.backOffice.payment.accountBank")%>',
			'form.text.backOffice.payment.otherBank': '<%=commonLangMessage.get("form.text.backOffice.payment.otherBank")%>',
			'form.text.backOffice.file.noFileSelected': '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>',
			'msg.error.info.image.sizeIsLarge': '<%=commonLangMessage.get("msg.error.info.image.sizeIsLarge")%>',
			'msg.error.info.distinctExecutor': '<%=commonLangMessage.get("msg.error.info.distinctExecutor")%>',
			'msg.error.info.dataNotFound': '<%=commonLangMessage.get("msg.error.info.dataNotFound")%>',
			'msg.error.info.notAllowReciveBonus': '<%=commonLangMessage.get("msg.error.info.notAllowReciveBonus")%>',
			'global.text.fail': '<%=commonLangMessage.get("global.text.fail")%>',
			'ui.text.report.all': '<%=commonLangMessage.get("ui.text.report.all")%>',
			'ui.text.deposit.select_account_bank': '<%=commonLangMessage.get("ui.text.report.all")%>',
			'ui.text.deposit.select_bank': '<%=commonLangMessage.get("ui.text.deposit.select_account_bank")%>',
			'ui.text.deposit.select_bonus': '<%=commonLangMessage.get("ui.text.deposit.select_bonus")%>',
			'ui.text.deposit.select_other_bank': '<%=commonLangMessage.get("ui.text.deposit.select_other_bank")%>',
			'msg.error.validation.required': '<%=commonLangMessage.get("msg.error.validation.required")%>',
			'msg.error.validation.transactionIdIsInvalid': '<%=commonLangMessage.get("msg.error.validation.transactionIdIsInvalid")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.suspend': '<%=commonLangMessage.get("form.text.backOffice.status.suspend")%>',
			'form.text.backOffice.status.locked': '<%=commonLangMessage.get("form.text.backOffice.status.locked")%>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.info.backOffice.reportIsProduction': '<%=commonLangMessage.get("msg.info.backOffice.reportIsProduction")%>',
			'form.text.backOffice.external.message': '<%=commonLangMessage.get("form.text.backOffice.external.message")%>',
			'ui.text.reset': '<%=commonLangMessage.get("ui.text.reset")%>',
			'form.text.backOffice.needSelectExportType': '<%=commonLangMessage.get("form.text.backOffice.needSelectExportType")%>',
			'form.text.backOffice.exportType.title': '<%=commonLangMessage.get("form.text.backOffice.exportType.title")%>',
			'fe.error.validation.paymentGatewayMoneyInvalid': '<%=commonLangMessage.get("fe.error.validation.paymentGatewayMoneyInvalid")%>',
			'ui.text.deposit.select_remark': '<%=commonLangMessage.get("ui.text.deposit.select_remark")%>',
			'msg.remark.template.required': '<%=commonLangMessage.get("msg.remark.template.required")%>',
			'msg.remark.required': '<%=commonLangMessage.get("msg.remark.required")%>',
			'msg.info.deposit.noDuplicateReferenceTransaction': '<%=commonLangMessage.get("msg.info.deposit.noDuplicateReferenceTransaction")%>',
			'form.text.backOffice.payment.checkOrderTime': '<%=commonLangMessage.get("form.text.backOffice.payment.checkOrderTime")%>',
			'form.text.backOffice.message.confirmUpdateDepositStatus': '<%=commonLangMessage.get("form.text.backOffice.message.confirmUpdateDepositStatus")%>',
			'form.text.backOffice.payment.apiHistoryDetail': '<%=commonLangMessage.get("form.text.backOffice.payment.apiHistoryDetail")%>',
			'fe.text.noData': '<%=commonLangMessage.get("fe.text.noData")%>',
			'form.text.depositStatus.approved': '<%=commonLangMessage.get("form.text.depositStatus.approved")%>',
			'form.text.depositStatus.disapproved': '<%=commonLangMessage.get("form.text.depositStatus.disapproved")%>',
			'form.text.depositStatus.pending': '<%=commonLangMessage.get("form.text.depositStatus.pending")%>',
			'form.text.backoffice.updatedSince': '<%=commonLangMessage.get("form.text.backoffice.updatedSince")%>',
			'form.text.backoffice.createdSince': '<%=commonLangMessage.get("form.text.backoffice.createdSince")%>',
			'form.text.af.ui.amount': '<%=commonLangMessage.get("form.text.af.ui.amount")%>',
			'ui.text.paymentType.creditCard': '<%=commonLangMessage.get("ui.text.paymentType.creditCard")%>',
			'ui.text.paymentType.onlineBanking': '<%=commonLangMessage.get("ui.text.paymentType.onlineBanking")%>'
		});
		$(document).ready(function() {

			//加這個才有Breadcrumbs
			<%-- head.jsp要用 --%>
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins

			{
				//直接用plugins.form-components.js，而不是app.jsp
				// Fix for class_selector in BS3 plugins.form-components.js
				var _base_resetForm = $.validator.prototype.resetForm;
				$.extend($.validator.prototype, {
					resetForm: function() {
						var resetForm_this = this;
						_base_resetForm.call(this);

						var currentForm = $(this.currentForm);
						var class_selector = ".form-group";
						if (currentForm.hasClass('form-vertical')) {
							class_selector = "*[class^=col-]";
						}

						currentForm.find(class_selector).each(function() {
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
			PageConfig.currencyFullName ='<%=commonCurrencyFullName%>';
			PageConfig.currencyType = <%=commonCurrencyType.unique()%>;

			DepositHandler.init();
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
							<button type="button" class="close" data-dismiss="modal"
								onclick="DepositHandler.closeDetail()">&times;
							</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.depositDetails")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row">
								<div class='col-md-12'>
									<%--									<div class="widget box">--%>
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
													"form.text.backOffice.payment.paymentMethod")%>
												</label>
												<div class="control-label" name="paymentMethod" id="paymentMethod">
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
												<div class="control-label" name="cardNumber"
													id="cardNumber">
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

			<!-- batch deposit -->
			<div class="modal fade in" id="batchApproveDepositModal" role="dialog">
				<div class="modal-dialog modal-xlg">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title"><%=commonLangMessage.get(
								"form.text.backOffice.approveDisapproveMultipleDeposits")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row" id='resultBatchTable'>
								<div class='col-md-12'>
									<div class="widget box">
										<div class="widget-content">
											<table class="table table-hover" id="batchDepositTable">
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
														"form.text.backOffice.payment.paymentMethod")%>
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
												<tbody id='batchDepositContainer'>
												</tbody>
												<tbody name='depositContainer'>
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
								name="reject" onclick='DepositHandler.batchApproveReject()' class="btn btn-primary">

							<input type="button"
								value="<%=commonLangMessage.get("form.text.backOffice.payment.approve")%>"
								name="approve"
								class="btn btn-primary" onclick='DepositHandler.batchApproveDeposit()'
							>

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
												<%--													style="width: 250px;"--%>
													placeholder="Min Amount"
													autocomplete="off"
													onchange="DepositHandler.onChangeSeparatorAdvanced(this,{allowNegative: true})"
													onkeydown="DepositHandler.onKeyDown(event)"
												>
												<label style="font-size: 12px; color: #666; margin: 4px;">
													- </label>
												<input
													type="text"
													id="maxAmount"
													name="maxAmount"
													class="form-control"
												<%--													style="width: 250px;"--%>
													placeholder="Max Amount"
													autocomplete="off"
													onchange="DepositHandler.onChangeSeparatorAdvanced(this,{
																		allowNegative: true
																})"
													onkeydown="DepositHandler.onKeyDown(event)"
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
											value="<%=commonLangMessage.get("form.text.button.search")%>"
											name="search"
											onclick='DepositHandler.search()' class="btn btn-primary">
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>


			</div>
			<%-- Statboxes end --%>

			<%-- result start --%>
			<div class="row" id='resultTable' style='display:none'>
				<div class="col-md-12">
					<div class="widget box">
						<div id='batchApproveButton' style='display:none'>
							<input type="button"
								value="<%=commonLangMessage.get("form.text.backOffice.payment.approveDisapprove")%>"
								name="batchApprove" onclick="DepositHandler.goToBatchApprove()" class="btn btn-primary"
								style='display:none'>
						</div>
						<div class="widget-content">
							<table id="depositSearchTable" class="table table-striped table-bordered" cellspacing="0"
								width="100%">
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
									<th id="<%=thid%>_label_id"><%=title%>
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
