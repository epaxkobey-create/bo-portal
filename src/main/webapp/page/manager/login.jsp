<%@page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.constants.SessionKeyConstants"
	import="com.nv.commons.message.LangMessage"
	import="com.nv.commons.utils.FrontendUtils"
	import="com.nv.commons.utils.ManagerUtils"
	import="java.util.UUID"
%>
<%@ page import="com.nv.commons.dto.Manager" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<%
	if (session != null) {
		Manager userInSession = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
		if (userInSession != null) {
			response.sendRedirect(FrontendUtils.getBackOffice());
			return;
		}

		String randomCode = (String) (session.getAttribute(SessionKeyConstants.MANAGER_RANDOM_CODE));
		if (randomCode == null || randomCode.length() == 0) {
			session.setAttribute(SessionKeyConstants.MANAGER_RANDOM_CODE, UUID.randomUUID().toString());
		}
	}

	LanguageType languageType = ManagerUtils.getLanguageType(session, request);
	LangMessage langMessage = languageType.getLangMessage();
	String lang = languageType.getLanguageResourceKey();

%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>Login | Melon - Flat &amp; Responsive Admin Template</title>

	<!--=== CSS ===-->
	<link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
	<link href="/css/main.css" rel="stylesheet" type="text/css"/>
	<link href="/css/login.css" rel="stylesheet" type="text/css"/>
	<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=">

	<!--=== JavaScript ===-->
	<script type="text/javascript" src="<%=FrontendUtils.getJQueryPath()%>"></script>
	<script type="text/javascript"
		src="/js/plugins/bootstrap/bootstrap.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/lodash.compat.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<!-- Validate -->
	<script type="text/javascript"
		src="/js/plugins/validation/jquery.validate.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<!-- <script type="text/javascript" src="/js/util/Validate.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script> --->

	<!-- App -->
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/util/EncryptUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/login/manager/login.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script language="javascript" type="text/javascript">
		PageConfig = {};

		PageConfig.path = "/pages/login.jsp";
		PageConfig.lang = "<%=lang%>";

		I18N.setResource({
			'msg.manager.login.inputName': '<%=langMessage.get("msg.manager.login.inputName")%>',
			'msg.manager.login.inputPassword': '<%=langMessage.get("msg.manager.login.inputPassword")%>',
		});


		$(document).ready(function() {
			"use strict";
			ManagerLoginHandler.init();
			document.onkeydown = function(event) {
				if (KeyEventUtils.isEnterKey(event.keyCode)) {
					ManagerLoginHandler.login();
				}
			}
		});
	</script>
</head>

<body class="login">
<!-- Logo -->
<div class="logo">
	<img src="/img/logo.png" alt="logo"/>
	<strong>ME</strong>LON
</div>
<!-- /Logo -->

<!-- Login Box -->
<div class="box">
	<div class="content">
		<!-- Login Formular -->
		<form id="loginForm" action="" method="post" class="login-form">
			<input id="randomCode" name="randomCode" type="hidden"
				value="<%=session.getAttribute(SessionKeyConstants.MANAGER_RANDOM_CODE)%>"/>
			<!-- Title -->
			<h3 class="form-title"><%=langMessage.get("ui.text.manager.adminLogin")%>
			</h3>

			<!-- Error Message -->
			<div id="errorMsg" class="alert fade in alert-danger" style="display: none;">
				<!--             <i class="icon-remove close" data-dismiss="alert"></i> -->
			</div>

			<!-- Input Fields -->
			<div class="form-group">
				<!--<label for="username">Username:</label>-->
				<div class="input-icon">
					<i class="icon-user"></i>
					<input type="text" id="username" name="username" class="form-control"
						placeholder="<%=langMessage.get("form.text.login.userName")%>"/>
				</div>
			</div>
			<div class="form-group">
				<!--<label for="password">Password:</label>-->
				<div class="input-icon">
					<i class="icon-lock"></i>
					<input type="password" id="password" name="password" class="form-control"
						placeholder="<%=langMessage.get("form.text.password")%>"/>
				</div>
			</div>
			<div class="form-group">
				<!--<label for="password">Password:</label>-->
				<div class="input-icon" id="language">
					<select name="language" class="form-control" id="selectLang"
						onchange="ManagerLoginHandler.changeLang()">
						<%
							for (LanguageType langType : LanguageType.BACKOFFICELANGUAGE) {
						%>
						<option value="<%=langType.getLanguageResourceKey()%>">
							<%=langMessage.get("global.text.language." + langType.getLanguageResourceKey())%>
						</option>
						<%
							}
						%>
					</select>
				</div>
			</div>

			<div class="form-group">
				<h6 class="form-title" name="form_title" id="form_title"><%=langMessage.get("ui.text.manager.memo")%>
				</h6>
			</div>

			<div class="form-actions">
				<a class="submit btn btn-primary pull-right" id="login">
					<%=langMessage.get("form.button.login")%>
				</a>
			</div>
		</form>

	</div>
	<!-- /.content -->
</div>
<!-- /Login Box -->
</body>
</html>
