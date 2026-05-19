<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionStatusType" %>
<%@ page import="com.nv.commons.constants.MoneyTransactionType" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="com.nv.commons.constants.FunctionType" %>

<%
	FrontendUtils.noCache(response);

	//	String vipNameByCurrency = VipSystemBO.getVIPnamesByCache(manager.getWebsiteType(), manager.getManagerRole().getCurrencyTypeList());

	boolean enableExportExcel = false;
	boolean enableProfile = true;

	Date now = DateTimeBuilder.localDateTime().withMinTime().toCalendar().getTime();

	String todayStr = DateUtils.toString(DateUtils.getSpecifyDate(now, DateUtils.END),
		FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayHalfYearAgo = DateUtils.getNextNMonth(now, -SystemConstants.MAX_LIMIT_MONTH);
	String todayHalfYearAgoStr = DateUtils.toString(todayHalfYearAgo,
		FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayWeekAgo = DateUtils.getNextNDay(now, -7);
	String todayWeekAgoStr = DateUtils.toString(todayWeekAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayOneMonthAgo = DateUtils.getNextNMonth(now, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

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
		"form.text.backOffice.menu.26")%>
	</title>


	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--sidebar.jsp在用 --%>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/ExcelUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/util/EncryptUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/payment/adjustment.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

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
		PageConfig.enableProfile = <%=enableProfile%>;
		PageConfig.enableExportExcel = <%=enableExportExcel%>;
		PageConfig.lang = "<%=commonLangMessage%>";

		PageConfig.date = {};
		PageConfig.date.today = '<%=todayStr%>';
		PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
		PageConfig.date.todayWeekAgo = '<%=todayWeekAgoStr%>';
		PageConfig.date.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>';
		PageConfig.date.format = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';

		PageConfig.accessRight = {};
		PageConfig.accessRight.createAdjustment = <%=FunctionType.No28.unique()%>;

		PageConfig.maxAmount = 1000;
		PageConfig.minAmount = 0;

		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.payment': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.payment")%>',
			'form.text.backOffice.breadcrumbs.adjustment': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.adjustment")%>',
			'form.text.backOffice.payment.mainWallet': '<%=commonLangMessage.get("form.text.backOffice.payment.mainWallet")%>',
			'global.text.moneyTransactionType.DEPOSIT': '<%=commonLangMessage.get("global.text.moneyTransactionType.DEPOSIT")%>',
			'global.text.moneyTransactionType.WITHDRAWALS': '<%=commonLangMessage.get("global.text.moneyTransactionType.WITHDRAWALS")%>',
			'global.text.moneyTransactionType.DEPOSIT_PAYMENT_GATEWAY': '<%=commonLangMessage.get("global.text.moneyTransactionType.DEPOSIT_PAYMENT_GATEWAY")%>',
			'global.text.moneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY': '<%=commonLangMessage.get("global.text.moneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY")%>',
			'global.text.moneyTransactionType.ADJUSTMENT': '<%=commonLangMessage.get("global.text.moneyTransactionType.ADJUSTMENT")%>',
			'global.text.moneyTransactionType.REVENUE_ADJUSTMENT': '<%=commonLangMessage.get("global.text.moneyTransactionType.REVENUE_ADJUSTMENT")%>',
			'msg.error.validation.transactionIdIsInvalid': '<%=commonLangMessage.get("msg.error.validation.transactionIdIsInvalid")%>',
			'msg.error.info.dataNotFound': '<%=commonLangMessage.get("msg.error.info.dataNotFound")%>',
			'ui.text.report.all': '<%=commonLangMessage.get("ui.text.report.all")%>',
			'ui.text.amount': '<%=commonLangMessage.get("ui.text.amount")%>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.info.backOffice.reportIsProduction': '<%=commonLangMessage.get("msg.info.backOffice.reportIsProduction")%>',
			'msg.error.validation.notZero.adjustmentAmount': '<%=commonLangMessage.get("msg.error.validation.notZero.adjustmentAmount")%>',
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
			PageConfig.MoneyTransactionType = <%=MoneyTransactionType.toJsonString()%>;
			PageConfig.MoneyTransactionStatusType = <%=MoneyTransactionStatusType.toJsonString()%>;
			AdjustmentHandler.init();
		});
	</script>
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
							<h4 class="modal-title" id="modalTitle">
								<%=commonLangMessage.get("form.text.backOffice.payment.adjustmentDetails")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row">
								<div class="col-xs-4 col-sm-4 col-md-4 col-lg-4" style="font-weight: bold;">
									<%=commonLangMessage.get("form.text.account.email")%>
								</div>
								<div class="col-xs-3 col-sm-3 col-md-3 col-lg-3" style="font-weight: bold;">
									<%=commonLangMessage.get("form.text.backOffice.report.transactionId")%>
								</div>
								<div class="col-xs-3 col-sm-3 col-md-3 col-lg-3" style="font-weight: bold;">
									<%=commonLangMessage.get("form.text.backOffice.payment.createdTime")%>
								</div>
							</div>
							<div class="row">
								<div class="col-xs-4 col-sm-4 col-md-4 col-lg-4" name="userId"></div>
								<div class="col-xs-3 col-sm-3 col-md-3 col-lg-3" name="transactionId"></div>
								<div class="col-xs-3 col-sm-3 col-md-3 col-lg-3" name="createdTime"></div>
							</div>
							<br/>
							<br/>

							<div class="row">
								<div class="col-xs-2 col-sm-2 col-md-2 col-lg-2" style="font-weight: bold;">
									<%=commonLangMessage.get("ui.text.amount")%>
								</div>
							</div>

							<div class="row">
								<div class="col-xs-2 col-sm-2 col-md-2 col-lg-2" name="amount"></div>
							</div>

						</div><!-- end modal-body -->
					</div><!-- end modal-content -->
				</div>
			</div>

			<div class="modal fade" id="createAdjustmentModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								onclick='AdjustmentHandler.resetCreateAdjustment()'>&times;
							</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.payment.createAdjustment")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="row">
								<div class="col-md-6">
									<form class="row-border" name="searchUserIdForm" action="#">
										<div class="form-group">
											<label class="control-label">
												<%=commonLangMessage.get("form.text.account.email")%>
												<span class="required">*</span>
											</label>
											<input type="hidden" name="searchUserId" id="searchUserId" class=""
												placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
										</div>
									</form>
								</div>
								<div class="col-md-6">
									<form name='createForm' action="#">
										<div class="form-group" id="generalDiv" style="display: block;">
											<label class="control-label">
												<%=commonLangMessage.get("ui.text.amount")%>
												(<%=commonCurrencyTypeSymbol%>)
												<span class="required">*</span>
											</label>
											<input type="text" name="amount" class="form-control"
												placeholder="<%=commonLangMessage.get("ui.text.amount")%>"
												onchange="AdjustmentHandler.validateAmount(this, PageConfig.minAmount, PageConfig.maxAmount)">
											<input type='hidden' name='userId'>
											<div style="margin-top: 10px;">
                   								<span style="font-weight: bold;">
                      								<%=commonLangMessage.get(
														"form.text.backOffice.payment.mainWallet")%>
                      								(<%=commonCurrencyTypeSymbol%>)
                   								</span>
												<span id='balance' style="font-weight: bold;"></span>
											</div>
										</div>
									</form>
								</div>
							</div>
						</div><!-- end modal-body -->
						<div class="modal-footer">
							<input type="reset" value="<%=commonLangMessage.get("ui.text.reset")%>" name="resetButton"
								onclick='AdjustmentHandler.resetCreateAdjustment()' class="btn btn-primary">
							<input type="button" value="<%=commonLangMessage.get("ui.text.submit")%>" name="save"
								onclick='AdjustmentHandler.create()' class="btn btn-primary">
						</div>
					</div><!-- end modal-content -->
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
											<input type="text" name="userId" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.report.transactionId")%>
											</label>
											<input type="text" name="transactionId" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.backOffice.report.transactionId")%>">
											<input type="hidden" id="visibleColumns" name="visibleColumns"/>
										</div>
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.payment.createdBy")%>
											</label>
											<input type="text" name="creator" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.backOffice.payment.createdBy")%>">
										</div>
									</div>
								</div>
								<div class="form-group">
									<div class="row">
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.report.createdSince")%>
											</label>
										</div>
									</div>
									<div class="row next-row">
										<div class="col-md-4">
											<div class="input-container">
												<input type="text" name="createDateStart" id="createDateStart"
													class="form-control singleDateTimePicker"
													placeholder="<%=commonLangMessage.get(
													"form.text.backOffice.report.createdSince")%>">
												<i class="icon-calendar"></i>
											</div>
										</div>
									</div>
								</div>
							</form>
							<%-- additional button block --%>
							<div class="row">
								<div class="col-md-12">
									<div class="widget">
										<input type="button"
											value="<%=commonLangMessage.get("form.text.button.create")%>" name="create"
											onclick='AdjustmentHandler.gotoCreateAdjustment()' class="btn btn-primary">
										<input type="button"
											value="<%=commonLangMessage.get("form.text.button.search")%>" name="search"
											onclick='AdjustmentHandler.search()' class="btn btn-primary">
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
						<div id='exportButton' style='display:none'>
							<input type="button" value="<%=commonLangMessage.get("form.text.button.exportExcel")%>"
								name="export" onclick='AdjustmentHandler.exportExcel()' class="btn btn-primary">
						</div>
						<div class="widget-content">
							<table id="adjustmentSearchTable" class="table table-striped table-bordered" cellspacing="0"
								width="100%">
								<thead>
								<tr>
									<th>#</th>
									<th><%=commonLangMessage.get("form.text.account.email")%>
									</th>
									<th><%=commonLangMessage.get("form.text.backOffice.report.transactionId")%>
									</th>
									<th><%=commonLangMessage.get("ui.text.amount")%> (<%=commonCurrencyTypeSymbol%>)
									</th>
									<th><%=commonLangMessage.get("form.text.backOffice.payment.createdBy")%>
									</th>
									<th><%=commonLangMessage.get("form.text.backOffice.payment.createdTime")%>
									</th>
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
