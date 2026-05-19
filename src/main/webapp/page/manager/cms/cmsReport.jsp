<%@page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.constants.WebSiteType" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.utils.DateTimeBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.nv.commons.constants.ProviderUpdateType" %>
<%@ page import="com.nv.commons.constants.ProviderStatusType" %>


<%


	FrontendUtils.noCache(response);
	List<ProviderUpdateType> accessUpdateTypeOptionList = new ArrayList<>();
	accessUpdateTypeOptionList.add(ProviderUpdateType.DISPLAY_NAME);
	accessUpdateTypeOptionList.add(ProviderUpdateType.MAINTAIN_DATE);
	accessUpdateTypeOptionList.add(ProviderUpdateType.STATUS);
	List<ProviderUpdateType> accessUpdateTypeOptionSortedList = accessUpdateTypeOptionList.stream()
		.sorted(Comparator.comparing((t) -> (t.getFullName(LanguageType.ENGLISH.getLangMessage()))))
		.toList();

	String providerIdLocal = RequestParser.getStringParameter(request, 3, "providerId");
	String activeTab = "1";//20?



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
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp"%>
	<%@include file ="/page/manager/include/daterangepicker.jsp"%>
	<%@include file="/page/manager/include/common.jsp" %>

	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.71")%> | <%=commonLangMessage.get("form.text.backOffice.menu.38")%>
	</title>

	

	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/plugins.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%-- <script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script> --%>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/cms/CmsReport.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

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
		PageConfig.providerId = '<%=providerIdLocal%>';



		PageConfig.ProviderUpdateType = <%=ProviderUpdateType.toJsonString()%>;
		PageConfig.ProviderStatusType = <%=ProviderStatusType.toJsonString()%>;
		PageConfig.lang = "<%=commonLangMessage%>";
		//set I18N
		I18N.setResource({
			"form.text.backOffice.status":"<%=commonLangMessage.get("form.text.backOffice.status")%>",
			"form.text.backOffice.provider.displayName":"<%=commonLangMessage.get("form.text.backOffice.displayName")%>",
			"form.text.backOffice.provider.maintainDate": "<%=commonLangMessage.get("form.text.backOffice.provider.maintainDate")%>",
			"form.text.backOffice.status.maintenance":"<%=commonLangMessage.get("form.text.backOffice.status.maintenance")%>",
			"form.text.backOffice.status.inactive": "<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>",
			"form.text.backOffice.status.active":"<%=commonLangMessage.get("form.text.backOffice.status.active")%>"
		});

		$(document).ready(function() {

			//加這個才有Breadcrumbs
			App.init(); // Init layout and core pluginsa
			Plugins.init(); // Init all plugins
			FormComponents.init();

			PageConfig.activeTab = '<%=activeTab%>';

			PageConfig.date = {};
			PageConfig.date.today = '<%=todayEndStr%>';
			PageConfig.date.todayHalfYearAgo = '<%=todayHalfYearAgoStr%>';
			PageConfig.date.todayOneMonthAgo = '<%=todayOneMonthAgoStr%>';

			CMSReportHandler.init();
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

	<jsp:include page="/page/manager/include/cmsSideBar.jsp">
		<jsp:param name="functionTitle" value="Report"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">

		<%-- container2 start --%>
		<div class="container">

			<%
				String functionTitle = "Report";
				String moduleName = "Provider";
			%>
			<%@include file="/page/manager/include/cmsHead.jsp" %>

			<!-- 				class="form-vertical"  -->

			<div class="row">
				<div class="col-md-12">
					<div class="widget box">
						<div class="widget-content">
							<div class="tabbable box-tabs">
								<ul class="nav nav-tabs">
									<%--這裡的排列的順序與顯示相反 --%>


									<li id='tab0'><a href="#"
										onclick="ReportHandler.toggleTab(0);" data-toggle="tab"><%=commonLangMessage.get(
										"form.text.report.updateLog")%>
									</a></li>

								</ul>
								<div class="tab-content">
									<div class="tab-pane" id="box_tab0">
										<form class="form-vertical" name='searchProviderUpdateLogForm' action="#">
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
															id="searchProviderUpdateLogDaterange"
															name="searchProviderUpdateLogDaterange"
															style="width:300px"/>
														<input type='hidden' name='providerId' value='<%=providerId%>'/>
<%--														<input type='hidden' name='searchDateRange'/>--%>
													</div>
													<div class="col-md-6">
														<select class="form-control" id="updateType" name="updateType"
															style="width:250px">
															<option value="-99" selected><%=commonLangMessage.get(
																"ui.text.report.all")%>
															</option>
															<%
																for (ProviderUpdateType updateType : accessUpdateTypeOptionSortedList) {
															%>
															<option value="<%=updateType.unique()%>" style="width:250px;">
																<%=updateType.getFullName(commonLangMessage)%>
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
															onclick='CMSReportHandler.searchProviderUpdateLog()'
															class="btn btn-primary">
													</div>
												</div>
											</div>
										</form>
										<div class="row" id='profileUpdateLogTable' style='display:none'>
											<div class="col-md-12">
												<div class="widget box">
													<div class="widget-content">
														<table id="searchProviderUpdateLogTable"
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
