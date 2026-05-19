<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.AccountUpdateType" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.constants.SessionExpiryType" %>
<%@ page import="com.nv.commons.constants.SelfExclusionType" %>
<%@ page import="com.nv.commons.constants.RealityCheckType" %>
<%@ page import="com.nv.commons.constants.AccountReviewReminderType" %>
<%@ page import="com.nv.commons.constants.TimeSpentLimitType" %>

<%
	FrontendUtils.noCache(response);

	String userId = RequestParser.getStringParameter(request, 50, "userId");
	int currency = RequestParser.getIntParameter(request, "currency");
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.2")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.44")%>
	</title>


	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<!-- DataTables -->
	<script type="text/javascript" src="/js/manager/member/setting.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.userId = '<%=userId%>';
		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';
		PageConfig.AccountUpdateType = <%=AccountUpdateType.toJsonString()%>;
		PageConfig.SessionExpiryTypes = <%=SessionExpiryType.toJsonString()%>;
		PageConfig.selfExclusionType = <%=SelfExclusionType.toJsonString()%>;
		PageConfig.currencyTypeId = <%=currency%>;
		PageConfig.realityCheckTypes = <%=RealityCheckType.toJsonString()%>;
		PageConfig.accountReviewReminderTypes = <%=AccountReviewReminderType.toJsonString()%>;
		PageConfig.timeSpentLimitTypes =<%=TimeSpentLimitType.toJsonString()%>

			$(document).ready(function () {

				//加這個才有Breadcrumbs
				App.init(); // Init layout and core plugins
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

				$(':radio.uniform, :checkbox.uniform').uniform();

				SettingHandler.init();
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

      .note-style {
        font-size: 13px;
        font-style: italic;
      }

      /*.input-container {*/
      /*  position: relative;*/
      /*}*/

      .currency-input-wrapper {
        position: relative;
        display: flex;
        align-items: center;
        background: white;
        border: 1px solid #ccc;
        border-radius: 4px;
        padding: 0;
      }

      .currency-symbol {
        padding: 0 10px;
        background-color: #f8f9fa;
        border-right: 1px solid #dee2e6;
        font-weight: bold;
        color: #495057;
        height: 100%;
        display: flex;
        align-items: center;
        font-size: 16px;
      }

      .input-container input {
        width: 100%;
        padding: 5px 30px 5px 5px;
        border: 1px solid #ccc;
        box-sizing: border-box;
      }

      .input-container {
        position: relative;
        flex-grow: 1;
      }

      .input-container i {
        position: absolute;
        top: 50%;
        right: 10px;
        transform: translateY(-50%);
        cursor: pointer;
        color: #555;
        font-size: 16px;
      }

	</style>

</head>
<body class="theme-dark">
<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<%-- container start --%>
<div id="container">
	<jsp:include page="/page/manager/include/sidebar2.jsp">
		<jsp:param name="functionTitle" value="Setting"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">

		<%-- container2 start --%>
		<div class="container">

			<%
				String functionTitle = "Setting";
			%>
			<%@include file="/page/manager/include/head2.jsp" %>

			<div class="row">
				<div class="col-md-12">
					<div class="widget box" id='updateSettingDiv'>
						<div class="widget-header">
							<h4>
								<i class="icon-reorder"></i> <%=commonLangMessage.get(
								"form.text.backOffice.setting.playResponsibly")%>
							</h4>
							<div class="toolbar no-padding">
								<div class="btn-group">
									<span class="btn btn-xs widget-collapse"><i class="icon-angle-down"></i></span>
								</div>
							</div>
						</div>

						<div class="widget-content">
							<div class="widget box">
								<div class="widget-header">
									<h4>
										<i class="icon-reorder"></i> <%=commonLangMessage.get(
										"form.text.backOffice.setting.playerResponsibility.wagerLimits")%>
									</h4>
									<div class="toolbar no-padding">
										<div class="btn-group">
											<span class="btn btn-xs widget-collapse"><i
												class="icon-angle-down"></i></span>
										</div>
									</div>
								</div>
								<div class="widget-content">
									<form class="form-horizontal row-border" name='updateWagerLimitForm' action="#">

										<table style="margin: 0">
											<thead>
											<tr>
												<th style="width: 150px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.period")%>
												</th>
												<th style="width: 300px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.currentLimit")%>
												</th>
												<th style="width: 450px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.newLimit")%>
												</th>
											</tr>
											</thead>
											<tbody>
											<tr>
												<td>Daily</td>
												<td id='currentDailyWagerLimits'>-</td>
												<td style="align-content: flex-start">
													<div class="form-group">
														<div class="row"
															style="display: flex; align-items: center; gap: 20px;">
															<div><%=commonCurrencyTypeSymbol%>
															</div>
															<input type="text" id="newDailyWagerLimit"
																name="newDailyWagerLimit"
																maxlength="1000"
																value="0"
																class="form-control flex-grow-1"
																style="min-width: 0; text-align: right;"
																placeholder="<%=commonLangMessage.get("form.text.backOffice.setting.playerResponsibility.wagerLimits")%>"
																onchange="SettingHandler.onChangeSeparator(this)"
															>


															<div class="btn-group">
																<button class="btn btn-secondary" type="button"
																	style="border-radius: 5px; background-color: red"
																	onclick="SettingHandler.clearText('newDailyWagerLimit')"
																><span
																	style="color: white; font-size: 14px; font-weight: bold"> X </span>
																</button>
															</div>
														</div>
														<div class="info-text mt-1" id="dailyWagerLimitErrorMessage"
															style="text-align: start; padding-left: 30px">

														</div>
														<div class="info-text mt-1" id="dailyWagerLimitMessage"
															style="text-align: start; padding-left: 30px">
														</div>
													</div>
												</td>
											</tr>
											</tbody>
										</table>
										<div
											style="display: flex; justify-content: space-between; align-items: center; padding: 8px 5px 8px 5px; margin-left: 0; margin-right: 0;">
											<p class="note-style">
												<%=commonLangMessage.get(
													"form.text.backOffice.setting.playerResponsibility.limitNote")%>
											</p>
											<input type="button"
												value="<%=commonLangMessage.get("form.text.button.save")%>"
												name="save"
												onclick='SettingHandler.saveAction("updateWagerLimitForm")'
												class="btn btn-primary btn-sm">
										</div>
									</form>
								</div>
							</div>


							<div class="widget box">
								<div class="widget-header">
									<h4>
										<i class="icon-reorder"></i> <%=commonLangMessage.get(
										"form.text.backOffice.setting.playerResponsibility.lossLimits")%>
									</h4>
									<div class="toolbar no-padding">
										<div class="btn-group">
											<span class="btn btn-xs widget-collapse"><i
												class="icon-angle-down"></i></span>
										</div>
									</div>
								</div>
								<div class="widget-content">
									<form class="form-horizontal row-border" name='updateLossLimitForm' action="#">

										<table style="margin: 0">
											<thead>
											<tr>
												<th style="width: 150px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.period")%>
												</th>
												<th style="width: 300px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.currentLimit")%>
												</th>
												<th style="width: 450px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.newLimit")%>
												</th>
											</tr>
											</thead>
											<tbody>
											<tr>
												<td>
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.daily")%>
												</td>
												<td id="currentDailyLossLimit">-</td>
												<td style="align-content: flex-start">
													<div class="form-group">
														<div class="row"
															style="display: flex; align-items: center; gap: 20px;">
															<div><%=commonCurrencyTypeSymbol%>
															</div>
															<input type="text" id="newDailyLossLimit"
																name="newDailyLossLimit"
																maxlength="1000"
																value="0"
																class="form-control flex-grow-1"
																style="min-width: 0;text-align: right"
																placeholder="<%=commonLangMessage.get("form.text.backOffice.setting.playerResponsibility.lossLimits")%>"
																onchange="SettingHandler.onChangeSeparator(this)"
															>
															<div class="btn-group">
																<button class="btn btn-secondary" type="button"
																	style="border-radius: 5px; background-color: red"
																	onclick="SettingHandler.clearText('newDailyLossLimit')"
																><span
																	style="color: white; font-size: 14px; font-weight: bold"> X
														</span></button>
															</div>
														</div>
														<div id="dailyLossLimitErrorMessage" class="info-text mt-1"
															style="text-align: start; padding-left: 30px">
														</div>
														<div id="dailyLossLimitMessage" class="info-text mt-1"
															style="text-align: start; padding-left: 30px">
														</div>
													</div>

												</td>
											</tr>

											</tbody>
										</table>
										<div
											style="display: flex; justify-content: space-between; align-items: center; padding: 8px 5px 8px 5px; margin-left: 0; margin-right: 0; ">
											<p class="note-style">
												<%=commonLangMessage.get(
													"form.text.backOffice.setting.playerResponsibility.limitNote")%>
											</p>
											<input type="button"
												value="<%=commonLangMessage.get("form.text.button.save")%>"
												name="save"
												onclick='SettingHandler.saveAction("updateLossLimitForm")'
												class="btn btn-primary btn-sm">
										</div>
									</form>
								</div>
							</div>


							<div class="widget box">
								<div class="widget-header">
									<h4>
										<i class="icon-reorder"></i> <%=commonLangMessage.get(
										"form.text.backOffice.setting.playResponsibly.timeSpentLimit")%>
									</h4>
									<div class="toolbar no-padding">
										<div class="btn-group">
											<span class="btn btn-xs widget-collapse"><i
												class="icon-angle-down"></i></span>
										</div>
									</div>
								</div>
								<div class="widget-content">
									<form class="form-horizontal row-border" name='updateTimeSpentLimitForm' action="#">

										<table style="margin: 0">
											<thead>
											<tr>
												<th style="width: 150px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.period")%>
												</th>
												<th style="width: 300px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.currentLimit")%>
												</th>
												<th style="width: 450px">
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.newLimit")%>
												</th>
											</tr>
											</thead>
											<tbody>
											<tr>
												<td>
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.daily")%>
												</td>
												<td id="currentTimeSpentLimit">-</td>
												<td style="align-content: flex-end">
													<div class="row"
														style="display: flex; align-items: center; gap: 20px;">

														<select class="form-control flex-grow-1" id="timeSpentLimitType"
															name="timeSpentLimitType"
														>
															<%
																for (TimeSpentLimitType type : TimeSpentLimitType.values()) {

															%>
															<option value="<%=type.unique()%>" style="width:250px;"
																id="dropdownValue">
																<%=type.getName()%>
															</option>
															<%
																}
															%>
														</select>

														<div class="btn-group">
															<button class="btn btn-secondary" type="button"
																style="border-radius: 5px; background-color: red"
																onclick="SettingHandler.clearText('timeSpentLimitType')"
															><span
																style="color: white; font-size: 14px; font-weight: bold"> X
														</span></button>
														</div>
													</div>
													<div class="info-text mt-1" style="text-align: start"
														id="timeSpentLimitMessage">

													</div>
												</td>
											</tr>
											</tbody>
										</table>
										<div
											style="display: flex; justify-content: space-between; align-items: center; padding: 8px 5px 8px 5px; margin-left: 0; margin-right: 0;">
											<p class="note-style">
												<%=commonLangMessage.get(
													"form.text.backOffice.setting.playerResponsibility.limitNote")%>
											</p>
											<input type="button"
												value="<%=commonLangMessage.get("form.text.button.save")%>"
												name="save"
												onclick='SettingHandler.saveAction("updateTimeSpentLimitForm")'
												class="btn btn-primary btn-sm">
										</div>
									</form>
								</div>
							</div>


							<div class="widget box">
								<div class="widget-header">
									<h4>
										<i class="icon-reorder"></i> <%=commonLangMessage.get(
										"form.text.backOffice.setting.playResponsibly.selfExclusion")%>
									</h4>
									<div class="toolbar no-padding">
										<div class="btn-group">
											<span class="btn btn-xs widget-collapse"><i
												class="icon-angle-down"></i></span>
										</div>
									</div>
								</div>
								<div class="widget-content">
									<form class="form-horizontal row-border" name='updateSelfExclusionForm' action="#">

										<table style="margin: 0">
											<thead>
											<tr>
												<th>
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.currentExclusion")%>
												</th>
												<th>
													<%=commonLangMessage.get(
														"form.text.backOffice.setting.playerResponsibility.newExclusion")%>
												</th>
											</tr>
											</thead>
											<tbody>
											<tr>
												<td id="currentSelfExclusion">-</td>
												<td style="align-content: flex-end">
													<div class="row"
														style="display: flex; align-items: center; gap: 20px;">

														<select class="form-control flex-grow-1" id="selfExclusionType"
															name="selfExclusionType"
														>
															<%
																for (SelfExclusionType type : SelfExclusionType.values()) {

															%>
															<option value="<%=type.unique()%>" style="width:250px;"
																id="dropdownValue">
																<%=type.getFullName()%>
															</option>
															<%
																}
															%>
														</select>

														<div class="btn-group">
															<button class="btn btn-secondary" type="button"
																style="border-radius: 5px; background-color: red"
																onclick="SettingHandler.clearText('selfExclusionType')"
															><span
																style="color: white; font-size: 14px; font-weight: bold"> X
														</span></button>
														</div>
													</div>
													<div class="info-text mt-1" style="text-align: start"
														id="exclusionMessage">

													</div>
												</td>
											</tr>
											</tbody>
										</table>
										<div
											style="display: flex; justify-content: space-between; align-items: center; padding: 8px 5px 8px 5px; margin-left: 0; margin-right: 0;">
											<p class="note-style">
												<%=commonLangMessage.get(
													"form.text.backOffice.setting.playerResponsibility.selfExclusionNote2")%>
											</p>
											<input type="button"
												value="<%=commonLangMessage.get("form.text.button.save")%>"
												name="save"
												onclick='SettingHandler.saveAction("updateSelfExclusionForm")'
												class="btn btn-primary btn-sm">
										</div>
									</form>
								</div>
							</div>
						</div>

					</div>
				</div>
			</div>

		</div>
		<%-- container2 end --%>

	</div>
	<%-- content end --%>

</div>
<%-- container end --%>

</body>
</html>
