<%@ page language="java" contentType="text/html;charset=UTF-8"
	import="com.nv.commons.utils.RequestParser"
	import="com.nv.commons.constants.LengthType"
	import="com.nv.commons.constants.SystemTxnStatusType"
%>
<%
	FrontendUtils.noCache(response);
	long gameTxnId = RequestParser.getLongParameter(request, "gameTxnId");
	String userId = RequestParser.getStringParameter(request, LengthType.AccountUserId.getLength(), "userId");
	String txnTime = RequestParser.getStringParameter(request, 10, "txnTime");
	int txnStatus = RequestParser.getIntParameter(request, "txnStatus", -1);
	String settleDate = RequestParser.getStringParameter(request, 10, "settleTime", "");
	int currency = RequestParser.getIntParameter(request, "currency", -1);
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>Game Transaction Detail</title>


	<!-- App -->
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/plugins/json-bigint/json-bigint.min.js"></script>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/member/subGameTransaction.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript">
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.accountCurrency = "<%=currency%>";
		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';
		PageConfig.SystemTxnStatusType = <%=SystemTxnStatusType.toJsonString()%>;

		$(document).ready(function () {
			"use strict";
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins
			FormComponents.init(); // Init all form-specific plugins
			SubGameTransactionHandler.init();
		});
	</script>

	<style>
      table {
        width: 100%;
        border-collapse: collapse;
      }

      /*tr:nth-child(3) {*/
      /*  width: 55%;*/
      /*}*/

      table th,
      table td {
        padding: 15px 20px;
        text-align: left;
        border: 2px solid #eee;
      }

      table th {
        font-weight: bold;
        color: #555;
        text-align: center;
      }

      /*table thead{*/
      /*  justify-content: center;*/
      /*  align-content: center;*/
      /*  align-items: center;*/
      /*}*/
      table td {
        background-color: #ffffff;
        color: #333;
        text-align: center;
      }

      table thead th {
        background-color: #f9f9f9;
      }

      #tableBetInfo1 thead th,
      #tableBetInfo1 tbody tr,
      #tableBetInfo2 thead th,
      #tableBetInfo2 tbody tr {
        width: 25%;
      }
	</style>
</head>

<body>

<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<div id="container" class="sidebar-closed">

	<div id="content">
		<div class="container">
			<div class="col-md-12">

				<div class="row">
					<div class="col-md-12">
						<form class="form-vertical" name="searchForm" style="display:none;">
							<input type="text" name="userId" class="form-control" value="<%=userId%>">
							<input type="text" name="gameTxnId" class="form-control" value="<%=gameTxnId%>">
							<input type="text" name="txnDate" class="form-control" value="<%=txnTime%>">
							<input type="text" name="settleDate" class="form-control" value="<%=settleDate%>">
							<input type="text" name="txnStatus" class="form-control" value="<%=txnStatus%>">
						</form>
						<div class="modal-body">
							<form class="form-horizontal" name="gameForm">
								<div class="row form-group">
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.account.email")%>
										</label>
										<div>
											<label class="control-label" id="userID"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.transactionId")%>
										</label>
										<div>
											<label class="control-label" id="txnID"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.vendorTransactionId")%>
										</label>
										<div>
											<label class="control-label" id="vendorTxnID"></label>
										</div>
									</div>
								</div>

								<div class="row form-group">
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.payment.transactionDate")%>
										</label>
										<div>
											<label class="control-label" id="txnTime"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.settledDate")%>
										</label>
										<div>
											<label class="control-label" id="settleTime"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.bonus.createdDate")%>
										</label>
										<div>
											<label class="control-label" id="createTime"></label>
										</div>
									</div>
								</div>

								<div class="row form-group">
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.vendor")%>
										</label>
										<div>
											<label class="control-label" id="vendor"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.bonusTemplate.gameType")%>
										</label>
										<div>
											<label class="control-label" id="gameType"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"ui.text.report.txnGame")%>
										</label>
										<div>
											<label class="control-label" id="gameName"></label>
										</div>
									</div>
								</div>

								<div class="row form-group">
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.status")%>
										</label>
										<div>
											<label class="control-label" id="status"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.odds")%>
										</label>
										<div>
											<label class="control-label" id="odds"></label>
										</div>
									</div>
									<div class="col-md-4">
										<label class="control-label"><%=commonLangMessage.get(
											"form.text.backOffice.report.oddsType")%>
										</label>
										<div>
											<label class="control-label" id="oddsType"></label>
										</div>
									</div>
								</div>

								<%--Table--%>
								<table id="tableBetInfo1" style="margin: 25px 0 0;">
									<thead>
									<tr>
										<th>
											<%=commonLangMessage.get(
												"ui.text.report.bet")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.report.winAmount")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.report.profit")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.turnover")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
									</tr>
									</thead>
									<tbody>
									<tr>
										<td><label class="control-label" id="betAmount"></label></td>
										<td><label class="control-label" id="winAmount"></label></td>
										<td><label class="control-label" id="profit"></label></td>
										<td><label class="control-label" id="turnover"></label></td>
									</tr>
									</tbody>
								</table>

								<table id="tableBetInfo2" style="margin: 25px 0 0;">
									<thead>
									<tr>
										<th>
											<%=commonLangMessage.get(
												"ui.text.realBetAmount")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.report.adjustAmount")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.report.progressBetAmount")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
										<th>
											<%=commonLangMessage.get(
												"form.text.backOffice.report.progressProfitLoss")%>
											(<%=commonCurrencyTypeSymbol%>)
										</th>
									</tr>
									</thead>
									<tbody>
									<tr>
										<td><label class="control-label" id="realBetAmount"></label></td>
										<td><label class="control-label" id="adjustAmount"></label></td>
										<td><label class="control-label" id="progressBetAmount"></label></td>
										<td><label class="control-label" id="progressProfitLoss"></label></td>
									</tr>
									</tbody>
								</table>

								<div style="border: 1px solid #CCC; width: 100%; margin: 30px 0 20px 0;"></div>

								<div id="originDataDiv">
									<label class="control-label"><%=commonLangMessage.get(
										"form.text.backOffice.report.originalData")%>
									</label>
									<div>
										<table id="originalData" class="table table-striped table-bordered"
											style="table-layout:fixed; word-break:break-all; width: 100%; margin-top: 5px;">
											<tbody>
											<tr style="display:none;" id="templateTimeZone">
												<th><%=commonLangMessage.get(
													"form.text.backOffice.report.timeZone")%>
												</th>
												<th id="timeZone">GMT+8</th>
											</tr>
											<tr style="display:none;" id="templateTr">
												<td id="key" style="text-align: end; font-weight: bold;"></td>
												<td id="value" style="text-align: start;"></td>
											</tr>
											</tbody>
										</table>
									</div>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- /.container -->
	</div>
</div>
<a id="tempHyperlink" style="display:none;" target="_blank">view screenshot</a>
</body>
</html>
