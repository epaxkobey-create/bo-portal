<%@ page language="java" contentType="text/html;charset=UTF-8"
%>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="com.nv.commons.constants.SystemTxnStatusType" %>
<%
	FrontendUtils.noCache(response);


	boolean enableExport = true;

	Date today = DateTimeBuilder.localDateTime().withMinTime().toCalendar().getTime();
	Date todayEnd = DateUtils.getSpecifyDate(today, DateUtils.END);

	String todayEndStr = DateUtils.toString(todayEnd, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayHalfYearAgo = DateUtils.getNextNMonth(today, -SystemConstants.MAX_LIMIT_MONTH);
	String todayHalfYearAgoStr = DateUtils.toString(todayHalfYearAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

	Date todayOneMonthAgo = DateUtils.getNextNMonth(today, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

%>
<!DOCTYPE html>
<html lang="en">
<head>

	<%@include file="../include/htmlHead.jsp" %>
	<%@include file="../include/formComponent.jsp" %>
	<%@include file="../include/datatable.jsp" %>
	<%@include file="../include/daterangepicker.jsp" %>
	<%@include file="../include/app.jsp" %>
	<%@include file="../include/common.jsp" %>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title><%=commonLangMessage.get("form.text.backOffice.menu.38")%> | <%=commonLangMessage.get("form.text.backOffice.menu.42")%>
	</title>


	<!-- App -->

	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/ExcelUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/report/Bet.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<!-- DataTables -->

	<script type="text/javascript">
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.enableExport = <%=enableExport%>;

		PageConfig.date = {};
		PageConfig.date.today = '<%=todayEndStr%>';
		PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
		PageConfig.date.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>';
		PageConfig.currency = '<%=commonCurrencyType.unique()%>'

		PageConfig.SystemTxnStatusType = <%=SystemTxnStatusType.toJsonString()%>;

		//set I18N
		I18N.setResource({
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.report': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.report")%>',
			'form.text.backOffice.breadcrumbs.bet': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.bet")%>',
			'ui.text.report.winLoss' :'<%=commonLangMessage.get("ui.text.report.winLoss")%>',
			'ui.text.report.bet':'<%=commonLangMessage.get("ui.text.report.bet")%>',
			'ui.text.report.turnOver':'<%=commonLangMessage.get("ui.text.report.turnOver")%>'

		});
		$(document).ready(function() {
			"use strict";
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins
			FormComponents.init(); // Init all form-specific plugins

			BetReportHandler.init();
			HeadHandler.init();
			MenuHandler.init();
			BetReportHandler.initValidator();

		});
	</script>
</head>

<body class="theme-dark">

<!-- Header -->
<%@include file="../include/top.jsp" %>
<!-- /.header -->

<div id="container">
	<%@include file="../include/sidebar.jsp" %>

	<!-- /Sidebar -->

	<div id="content">
		<div class="container">
			<!-- Breadcrumbs line -->
			<%@include file="../include/head.jsp" %>
			<div class="text-right">
				<ul class="nav nav-tabs" id="betStatusTabs" style="display: inline-flex; border-bottom: none;">
					<li id="betTab-SETTLED" class="active">
						<a href="#" onclick="BetReportHandler.switchTab('SETTLED'); return false;">
							<%=commonLangMessage.get("ui.text.report.settled")%>
						</a>
					</li>
					<li id="betTab-UNSETTLED">
						<a href="#" onclick="BetReportHandler.switchTab('UNSETTLED'); return false;">
							<%=commonLangMessage.get("ui.text.report.unsettled")%>
						</a>
					</li>
				</ul>
			</div>
			<!--=== Page Content ===-->
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
							<form class="form-vertical" id="searchForm" name='searchForm' action="#">
								<div class="form-group">
									<div class="row">
										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get("form.text.account.email")%>
											</label>
											<input type="text" name="email" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
										</div>

										<div class="col-md-4">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.report.transactionDateRange")%>
											</label>
											<input type="text" class="form-control"
												placeholder="<%=commonLangMessage.get("form.text.backOffice.report.transactionDateRange")%>"
												id="transactionDaterange"
												name="transactionDaterange"
											/>
										</div>

										<div class="col-md-4" id="winLossAmountRangeContainer">
											<label class="control-label"><%=commonLangMessage
												.get("form.text.backOffice.report.winLossAmountRange")%>
											</label>
											<div style="display: flex; align-items: center; gap: 8px;">
												<input
													type="text"
													id="minAmount"
													name="minAmount"
													class="form-control"
													style="width: 250px;"
													placeholder="Min Amount"
													autocomplete="off"
													onchange="BetReportHandler.onChangeSeparatorAdvanced(this,{allowNegative: true})"
												>
												<label style="font-size: 12px; color: #666; margin: 0;"> - </label>
												<input
													type="text"
													id="maxAmount"
													name="maxAmount"
													class="form-control"
													style="width: 250px;"
													placeholder="Max Amount"
													autocomplete="off"
													onchange="BetReportHandler.onChangeSeparatorAdvanced(this,{
														allowNegative: true
													})"
												>
											</div>

											<label class="error-msg-amount-validation" style="display: none;"></label>
										</div>
									</div>
								</div>

								<div class="form-group">
									<div class="row">
										<div class="col-md-12">
											<input type="hidden" id="defaultConditionFlag" name="defaultConditionFlag"
												value="false">
											<input type="hidden" id="txnStatus" name="txnStatus" value="SETTLED">
											<input type="button"
												value="<%=commonLangMessage.get("form.text.button.search")%>"
												name="search" onclick='BetReportHandler.searchReport()'
												class="btn btn-primary">
										</div>
									</div>
								</div>
							</form>
						</div>
					</div>

					<div class="widget box" id='resultContainer' style='display:none'>
						<div class="widget-content">
							<div id="settledTableWrapper">
								<table id="betReportTableSettled" class="table table-striped table-bordered">
									<thead>
									<tr>
										<th>#</th>
										<th><%=commonLangMessage.get("form.text.report.member.email")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.payment.transactionDate")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.settledDate")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.bonus.createdDate")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.vendor")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gameType")%></th>
										<th><%=commonLangMessage.get("ui.text.report.txnGame")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.odds")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.oddsType")%></th>
										<th><%=commonLangMessage.get("ui.text.report.bet")%> (<%=commonCurrencyTypeSymbol%>)</th>
										<th><%=commonLangMessage.get("ui.text.report.winLoss")%> (<%=commonCurrencyTypeSymbol%>)</th>
										<th><%=commonLangMessage.get("ui.text.report.turnOver")%> (<%=commonCurrencyTypeSymbol%>)</th>
										<th><%=commonLangMessage.get("form.text.af.btn.view")%></th>
									</tr>
									</thead>
									<tbody></tbody>
									<tfoot>
										<tr>
											<th colspan="10" style="text-align: right;"><%=commonLangMessage.get(
												"form.text.backOffice.report.subTotal")%>
												(<%=commonCurrencyTypeSymbol%>)
											</th>
											<th name='subTotalBetAmount'></th>
											<th name='subTotalProfitLoss'></th>
											<th name='subTotalTurnover'></th>
											<th></th>
										</tr>
										<tr>
											<th colspan="10" style="text-align: right;"><%=commonLangMessage.get(
												"form.text.backOffice.report.grandTotal")%>
												(<%=commonCurrencyTypeSymbol%>)
											</th>
											<th name='grandTotalBetAmount'></th>
											<th name='grandTotalProfitLoss'></th>
											<th name='grandTotalTurnover'></th>
											<th></th>
										</tr>
									</tfoot>
								</table>
							</div>
							<div id="unsettledTableWrapper" style="display:none">
								<table id="betReportTableUnsettled" class="table table-striped table-bordered">
									<thead>
									<tr>
										<th>#</th>
										<th><%=commonLangMessage.get("form.text.report.member.email")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.payment.transactionDate")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.bonus.createdDate")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.vendor")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gameType")%></th>
										<th><%=commonLangMessage.get("ui.text.report.txnGame")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.odds")%></th>
										<th><%=commonLangMessage.get("form.text.backOffice.report.oddsType")%></th>
										<th><%=commonLangMessage.get("ui.text.report.bet")%> (<%=commonCurrencyTypeSymbol%>)</th>
										<th><%=commonLangMessage.get("form.text.af.btn.view")%></th>
									</tr>
									</thead>
									<tbody></tbody>
									<tfoot>
										<tr>
											<th colspan="9" style="text-align: right;"><%=commonLangMessage.get(
												"form.text.backOffice.report.subTotal")%>
												(<%=commonCurrencyTypeSymbol%>)
											</th>
											<th name='subTotalBetAmount'></th>
											<th></th>
										</tr>
										<tr>
											<th colspan="9" style="text-align: right;"><%=commonLangMessage.get(
												"form.text.backOffice.report.grandTotal")%>
												(<%=commonCurrencyTypeSymbol%>)
											</th>
											<th name='grandTotalBetAmount'></th>
											<th></th>
										</tr>
									</tfoot>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
</body>
</html>
