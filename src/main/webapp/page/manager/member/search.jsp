<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.nv.commons.constants.AccountStatusType" %>
<%@ page import="com.nv.commons.constants.KycDocumentStatusType" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>


<%

	FrontendUtils.noCache(response);
	
	boolean enableExportExcel = true;
	boolean enableProfile = true;
	boolean enableProfileContactView = true;
	boolean enableShowEmail = true;
	boolean enableShowContactPhone = true;
	boolean enableShowPartOfContactPhone = true;
	

	Date today = DateTimeBuilder.localDateTime().withMinTime().toCalendar().getTime();
	Date todayOneMonthAgo = DateUtils.getNextNMonth(today, -1);
	String todayOneMonthAgoStr = DateUtils.toString(todayOneMonthAgo, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss);

%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/formComponent.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp"%>
	<%@include file="/page/manager/include/common.jsp"%>

	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.2")%> | <%=commonLangMessage.get("form.text.backOffice.menu.3")%>
	</title>



	<%-- head.jsp要用 --%>
	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--sidebar.jsp在用 --%>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/util/ExcelUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/manager/member/search.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;
		PageConfig.enableProfile = <%=enableProfile%>;
		PageConfig.enableExportExcel = <%=enableExportExcel%>;
		PageConfig.enableProfileContactView = <%=enableProfileContactView%>;
		PageConfig.enableShowEmail = <%=enableShowEmail%>;
		PageConfig.enableShowContactPhone = <%=enableShowContactPhone%>;
		PageConfig.enableShowPartOfContactPhone = <%=enableShowPartOfContactPhone%>;
		PageConfig.webSiteType = '<%=commonWebsiteType.getShortName()%>';

		PageConfig.lang = "<%=commonLangMessage%>";
		PageConfig.currencyFullName = {};

		PageConfig.excludeMemberCol = [0];
		PageConfig.excludeProviderCol = [0];

		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>>'




		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.member': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.member")%>',
			'form.text.backOffice.breadcrumbs.search': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.search")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.suspend': '<%=commonLangMessage.get("form.text.backOffice.status.suspend")%>',
			'form.text.backOffice.status.locked': '<%=commonLangMessage.get("form.text.backOffice.status.locked")%>',
			'msg.error.account.phone.isNotValidated': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated") %>',
			'msg.error.account.phone.isNotValidated.1': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.1") %>',
			'msg.error.account.phone.isNotValidated.2': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.2") %>',
			'msg.error.account.phone.isNotValidated.3': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.3") %>',
			'msg.error.account.phone.isNotValidated.4': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.4") %>',
			'msg.error.account.phone.isNotValidated.5': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.5") %>',
			'msg.error.account.phone.isNotValidated.6': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.6") %>',
			'msg.error.account.phone.isNotValidated.7': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.7") %>',
			'msg.error.account.phone.isNotValidated.9': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.9") %>',
			'msg.error.account.phone.isNotValidated.13': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.13") %>',
			'msg.error.account.phone.isNotValidated.15': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.15") %>',
			'msg.error.account.phone.isNotValidated.16': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.16") %>',
			'msg.error.account.phone.isNotValidated.18': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.18") %>',
			'ui.text.member.userChannelType.AFFILIATE': '<%=commonLangMessage.get("ui.text.member.userChannelType.AFFILIATE") %>',
			'ui.text.member.userChannelType.DIRECT': '<%=commonLangMessage.get("ui.text.member.userChannelType.DIRECT") %>',
			'ui.text.member.userChannelType.REFER_A_FRIEND': '<%=commonLangMessage.get("ui.text.member.userChannelType.REFER_A_FRIEND") %>',
			'ui.text.report.all': '<%=commonLangMessage.get("ui.text.report.all") %>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.info.backOffice.reportIsProduction': '<%=commonLangMessage.get("msg.info.backOffice.reportIsProduction")%>',
			'msg.error.password.isNotValidated.v2': '<%=commonLangMessage.get("msg.error.password.isNotValidated.v2")%>',
			'msg.error.validation.passwordNotMatch': '<%=commonLangMessage.get("msg.error.validation.passwordNotMatch")%>',
			'form.text.account.totalBalance': '<%=commonLangMessage.get("form.text.account.totalBalance")%>'
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
			PageConfig.DocumentStatusType = <%=KycDocumentStatusType.toJsonString()%>;
			PageConfig.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>';


			SearchHandler.init();

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
							<%@ include file="../include/search_account.jsp" %>

						</div>

					</div>
				</div>


			</div>
			<%-- Statboxes end --%>

		</div>
		<%-- container2 end --%>

	</div>
	<%-- content end --%>

</div>
<%-- container end --%>

<div style="display: none">
	<label>
		<select>
			<option id="affiliateTemplate"></option>
			<option id="referrerIdListTemplate"></option>
			<option id="providerIdListTemplate"></option>
		</select>
	</label>
</div>
</body>
</html>
