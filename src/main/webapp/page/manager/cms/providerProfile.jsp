<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.constants.ProviderStatusType" %>
<%
	FrontendUtils.noCache(response);
	String providerIdLocal = RequestParser.getStringParameter(request, 3, "providerId");
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

	<title><%=commonLangMessage.get("form.text.backOffice.menu.71")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.10")%>
	</title>

	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<!-- DataTables -->

	<script type="text/javascript" src="/js/manager/cms/Provider.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<style>
      .btn-unified {
        line-height: 20px;
      }

	</style>
	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.providerId = '<%=providerIdLocal%>';
		PageConfig.DateFormatPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy%>'
		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>>'
		PageConfig.DatePattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy%>';

		//set I18N
		I18N.setResource({
			'form.text.backOffice.breadcrumbs.profile': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.profile")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.maintenance': '<%=commonLangMessage.get("form.text.backOffice.status.maintenance")%>',
			'form.text.backOffice.provider.editProvider': '<%=commonLangMessage.get("form.text.backOffice.provider.editProvider")%>',
			'msg.manager.update.success': '<%=commonLangMessage.get("msg.manager.update.success")%>',
		});
		$(document).ready(function () {
			App.init();
			ProviderHandler.init();
			ProviderHandler.loadProviderProfile();
			ProviderHandler.bindProviderProfileEvent();
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
		});
	</script>
</head>
<body class="theme-dark">
<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<%-- container start --%>
<div id="container">

	<jsp:include page="/page/manager/include/cmsSideBar.jsp">
		<jsp:param name="functionTitle" value="Profile"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">


		<%-- container2 start --%>
		<div class="container">
			<%
				String functionTitle = "Profile";
				String moduleName = "Provider";
			%>
			<%@include file="/page/manager/include/cmsHead.jsp" %>
			<div class="modal fade" id="updateProviderNameModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<div class="modal-content">

						<form class="form-horizontal" name="updateProviderNameForm" id="updateProviderNameForm"
							action="#">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" id="modalTitle">
									<%= commonLangMessage.get("form.text.backOffice.provider.editProvider") %>
								</h4>
							</div>

							<div class="modal-body">
								<input type="hidden" name="providerId" value="<%=providerId%>">

								<div class="row">
									<div class="col-md-12">
										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i style="line-height: 35px" class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.account.generalInformation")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<button type="button" class="btn btn-xs widget-collapse">
															<i style="line-height: 35px" class="icon-angle-down"></i>
														</button>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="form-group">
													<label class="col-md-3 control-label"><%=commonLangMessage.get(
														"form.text.backOffice.provider.displayName")%><span
														class="required">*</span>
													</label>
													<div class="col-md-7">
														<input type="text" id="providerName" name="providerName"
															maxlength="50"
															class="form-control"
															placeholder="<%=commonLangMessage.get("form.text.backOffice.provider.displayName")%>">
													</div>

													<input type="hidden" id="providerStatus" name="providerStatus">
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%= commonLangMessage.get("ui.text.reset") %>"
									name="resetButton" onclick="ProviderHandler.resetProviderName()"
									class="btn btn-primary">
								<input type="button" value="<%= commonLangMessage.get("form.text.button.save") %>"
									name="save" onclick="ProviderHandler.updateProvider('updateProviderNameForm')"
									class="btn btn-primary">
							</div>
						</form>

					</div>
				</div>
			</div>

			<%-- profileContainer start --%>
			<div id='profileContainer'>
				<%-- General start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->

								<h4>
									<i style="line-height: 35px" class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.general")%>
								</h4>


								<!-- 右侧按钮组 -->
								<div class="toolbar no-padding">
									<button type="button"
										name="gotoEditGeneral"
										onclick="ProviderHandler.gotoEditProviderName()"
										class="btn btn-primary btn-sm"
										style="line-height: 15px;">
										<%=commonLangMessage.get("ui.text.profile.edit")%>
									</button>


									<div class="btn-group">
										<button type="button" class="btn btn-xs widget-collapse">
											<i class="icon-angle-down"  style="line-height: 35px"></i>
										</button>
									</div>

								</div>

							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<input type="hidden" name="providerId" value=<%=providerId%>>
									<tbody>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.displayName")%>
										</th>
										<td id='displayName'></td>
										<td></td>
										<td></td>
									</tr>

									<tr id='<%=commonLangMessage.get("form.text.backOffice.status")%>flag'>
										<th><%=commonLangMessage.get("form.text.backOffice.status")%>
										</th>
										<td style="align-content: center;" id="status"></td>
										<td colspan="2" id="editStatus">
											<span id="editStatusButton"></span>
										</td>
									</tr>

									</tbody>

								</table>
							</div>
						</div>
					</div>
				</div>

			</div>
			<%-- container2 end --%>

			<div class="modal fade" id="updateProviderData" role="dialog">
				<div class="modal-dialog">
					<!-- Modal content-->
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
							>&times;
							</button>
							<h4 class="modal-title"><%=commonLangMessage.get("form.text.account.editStatus")%>
							</h4>
						</div>
						<div class="modal-body">
							<form class="form-horizontal" id="updateProviderProfileForm"
								name="updateProviderProfileForm"
							>
								<div class="form-group hide-default">
									<label class="col-md-3 control-label"><%=commonLangMessage.get(
										"form.text.backOffice.games.display")%>
									</label>
									<input type="hidden" id="providerId" name="providerId">
								</div>
								<div class="form-group">
									<label class="col-md-3 control-label"><%=commonLangMessage.get(
										"form.text.backOffice.status")%>
									</label>
									<div class="col-md-9">
										<select class="form-control" name="providerStatus">
											<% for (ProviderStatusType status : ProviderStatusType.VALUES_FOR_BO) { %>
											<option value="<%=status.unique()%>"><%=status.getName()%>
											</option>
											<% } %>
										</select>
									</div>
								</div>
								<div class="form-group" id="providerDataRangeDiv" style="display:none;">
									<label class="col-md-3 control-label"><%=commonLangMessage.get(
										"form.text.backOffice.provider.maintainDate")%><span
										class="required">*</span>
									</label>
									<div class="col-md-9">
										<input type="text" class="form-control w-100"
											id="maintenanceDaterange"
											name="maintenanceDaterange"/>
									</div>
								</div>
							</form>
						</div>
						<div class="modal-footer">
							<button class="btn btn-primary"
								onclick="ProviderHandler.resetUpdateProviderForm('updateProviderProfileForm')"><%=commonLangMessage.get(
								"ui.text.reset")%>
							</button>
							<button class="btn btn-primary"
								onclick="ProviderHandler.updateProvider('updateProviderProfileForm')"><%=commonLangMessage.get(
								"form.text.button.save")%>
							</button>
						</div>
					</div>
				</div>
			</div>

		</div>
		<%-- content end --%>

	</div>
	<%-- container end --%>

</body>
</html>
