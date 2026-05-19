<%@page import="com.nv.commons.message.LangMessage" %>
<%@page import="com.nv.commons.constants.LanguageType" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.nv.commons.dto.Manager" %>
<%@ page import="com.nv.commons.utils.CookieUtils" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.utils.DateUtils" %>
<%@ page import="com.nv.commons.utils.FormatUtils" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%@ page import="com.nv.commons.constants.BinaryStatusType" %>
<%@ page import="com.nv.commons.constants.CurrencyType" %>
<%@ page import="com.nv.commons.constants.WebSiteType" %>
<%@ page import="com.nv.commons.constants.SessionKeyConstants" %>
<%@ page import="com.nv.commons.constants.SystemConstants" %>
<%@ page import="com.nv.commons.cache.WebsiteCurrencySettingCache" %>
<%@ page import="com.nv.commons.exceptions.Deviation" %>
<%
	FrontendUtils.noCache(response);



//	String showName = managerWebsite.getShortName().toUpperCase();
	String showName = "TS";

	Manager loginManager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
	String forwardPage = FrontendUtils.getBackOffice();

	LanguageType topLanType = ManagerUtils.getLanguageType(session, request);
	LangMessage topLangMsg = topLanType.getLangMessage();
	String topLang = topLanType.getLanguageResourceKey();

//	boolean enableNotification = role.getEnableNotification() == BinaryStatusType.ACTIVE.unique();

	Date queryToday = new Date();
	String[] dateRange = new String[7];
	dateRange[0] = FormatUtils.dateFormat(queryToday, FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd);

	for (int i = 1; i <= 6; i++) {
		dateRange[i] = FormatUtils.dateFormat(DateUtils.getNextNDay(queryToday, -i),
			FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd);
	}

	String logoSrc = "/images/manager/" + commonWebsiteType.getTempName() + "/favicon.ico";

	String topCookieCurrency = CookieUtils.getCookieValue(request, SystemConstants.CURRENCY);




	int systemTime = 480;
	int currencyTime =
		WebsiteCurrencySettingCache.getInstance().getDefaultTimeZone(commonWebsiteType, commonCurrencyType).getRawOffset()
			/ (1000 * 60);

%>

<style type="text/css">
  a {
    cursor: pointer;
  }

  .label.label-await {
    background-color: #A77E88;
  }
</style>
<script type="text/javascript" src="/js/manager/moment.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/manager/Top.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript">
	var $ = jQuery.noConflict();

	if (typeof (PageConfig) == 'undefined') {
		PageConfig = {};
	}
	PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;
	PageConfig.lang = "<%=topLang%>";
	PageConfig.managerCurrency = "<%=topCookieCurrency%>";

	PageConfig.systemTime = "<%=systemTime%>";
	PageConfig.currencyTime = "<%=currencyTime%>";

	$(document).ready(function() {
		"use strict";
		LogUtil.init();
		TopHandler.init();
	});
</script>
<header class="header navbar navbar-fixed-top" role="banner">
	<!-- Top Navigation Bar -->
	<div class="container">

		<!-- Only visible on smartphones, menu toggle -->
		<ul class="nav navbar-nav">
			<li class="nav-toggle"><a href="javascript:void(0);" title=""><i class="icon-reorder"></i></a></li>
		</ul>

		<!-- Logo -->
		<a id="logoLink" class="navbar-brand" href="<%=forwardPage%>">
			<img src="<%=logoSrc%>" alt="logo" style="display: inline-block; height:32px; width:32px"/>
			<strong><%=showName%></strong>
		</a>
		<!-- /logo -->

		<!-- Sidebar Toggler -->
		<a href="#" class="toggle-sidebar bs-tooltip" data-placement="bottom" data-original-title="Toggle navigation">
			<i class="icon-reorder"></i>
		</a>
		<!-- /Sidebar Toggler -->

		<!-- Top Right Menu -->
		<ul class="nav navbar-nav navbar-right">
			<!-- System time -->

			<!-- User Login Dropdown -->
			<li class="dropdown user">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown">
					<!--<img alt="" src="assets/img/avatar1_small.jpg" />-->
					<i class="icon-male"></i>
					<span class="username"><%=loginManager.getUserId()%></span>
					<i class="icon-caret-down small"></i>
				</a>
				<ul class="dropdown-menu">
					<!--           <li><a href="#"><i class="icon-key"></i> Change Password</a></li> -->
					<!--           <li class="divider"></li> -->
					<li>
						<a href="#" data-toggle="modal" data-target="#updatePassword" data-backdrop="static"
							data-keyboard="false">
							<i class="icon-key"></i> <%=topLangMsg.get("ui.text.backOffice.top.changePassword")%>
						</a>
					</li>
					<li><a href="JavaScript:LogUtil.logout();"><i class="icon-signout"></i> <%=topLangMsg.get(
						"ui.text.backOffice.top.logOut")%>
					</a></li>
				</ul>
			</li>
			<!-- /user login dropdown -->
		</ul>
		<!-- /Top Right Menu -->
	</div>
	<!-- /top navigation bar -->

</header>

<!-- move from head.jsp -->
<!--=== Change Password ===-->
<div class="modal fade" id="updatePassword" role="dialog">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					onclick="LogUtil.clearData()">&times;
				</button>
				<h4 class="modal-title"><%=topLangMsg.get("form.text.backOffice.staff.updatePassword")%>
				</h4>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" id="updateForm" name="updateForm"
					novalidate="novalidate">
					<div class="form-group">
						<label class="col-md-3 control-label"><%=topLangMsg.get(
							"form.text.backOffice.staff.oldPassword")%><span
							class="required">*</span></label>
						<div class="col-md-9">
							<input type="password" class="form-control"
								id="oldPassword" name="oldPassword"
								maxlength="15" class="form-control">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-3 control-label"><%=topLangMsg.get(
							"form.text.backOffice.staff.newPassword")%><span
							class="required">*</span></label>
						<div class="col-md-9">
							<input type="password" class="form-control"
								id="roleNewPwd" name="roleNewPwd"
								maxlength="15" class="form-control">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-3 control-label"><%=topLangMsg.get(
							"form.text.backOffice.staff.confirmPassword")%><span
							class="required">*</span></label>
						<div class="col-md-9">
							<input type="password" class="form-control"
								id="roleConfirmPwd" name="roleConfirmPwd"
								maxlength="15" class="form-control">
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer">
				<button class="btn btn-primary" onclick="LogUtil.clearData()"><%=topLangMsg.get("ui.text.reset")%>
				</button>
				<button class="btn btn-primary" id="changePassword"
					onclick="LogUtil.changePassword()"><%=topLangMsg.get("ui.text.confirm")%>
				</button>
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="reportListModal" role="dialog">
	<div class="modal-dialog modal-lg">
		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title"><%=topLangMsg.get("form.text.backOffice.reportList")%>
				</h4>
			</div>
			<div class="modal-body">
				<table id="reportListTable" class="table table-striped table-bordered" width="100%">
					<thead>
					<tr>
						<th><%=topLangMsg.get("form.text.type")%>
						</th>
						<th><%=topLangMsg.get("form.text.backOffice.bonusTemplate.condition")%>
						</th>
						<th><%=topLangMsg.get("form.text.backOffice.status")%>
						</th>
						<th><%=topLangMsg.get("form.text.fileName")%>
						</th>
						<th><%=topLangMsg.get("form.text.backOffice.report.createTime")%>
						</th>
						<th><%=topLangMsg.get("form.text.backOffice.report.completedTime")%>
						</th>
						<th><%=topLangMsg.get("form.text.backOffice.affiliateApplication.processedTime")%>(s)</th>
					</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<div class="modal fade" id="reportConditionModal" role="dialog">
	<div class="modal-dialog">
		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h4 class="modal-title"><%=topLangMsg.get("form.text.backOffice.reportCondition")%>
				</h4>
			</div>
			<div class="modal-body" id="conditionBody">
			</div>
		</div>
	</div>
</div>
<audio id="depositAudio" controls preload="auto" style="display:none;">
	<source src="/audio/deposit_noti.mp3" type="audio/mpeg">
</audio>
<audio id="withdrawalAudio" controls preload="auto" style="display:none;">
	<source src="/audio/withdrawal_noti.mp3" type="audio/mpeg">
</audio>
<span style="display:none;" id="notifyTemplate" class="label label-danger pull-right"></span>
<li style="display:none;" id="registerTemplate">
	<a href="javascript:void(0);">
		<span class="label label-info"><i class="icon-user"></i></span>
		<span class="message"></span><br>&nbsp;
		<span class="time"></span>
	</a>
</li>
<li style="display:none;" id="depositTemplate">
	<a href="javascript:void(0);">
		<span class="label label-danger"><i class="icon-inbox"></i></span>
		<span class="message"></span><br>&nbsp;
		<span class="time"></span>
	</a>
</li>
<li style="display:none;" id="withdrawalTemplate">
	<a href="javascript:void(0);">
		<span class="label label-warning"><i class="icon-upload-alt"></i></span>
		<span class="message"></span><br>&nbsp;
		<span class="time"></span>
	</a>
</li>