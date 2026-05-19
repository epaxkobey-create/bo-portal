<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true"%>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import = "com.nv.commons.message.LangMessage" %>
<%
LangMessage langMessage = FrontendUtils.getLangMessage(session, request);
%>
<!DOCTYPE html>

<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>403 | Melon - Flat &amp; Responsive Admin Template</title>

	<!--=== CSS ===-->

	<!-- Bootstrap -->
	<link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>

	<!-- Theme -->
	<link href="/css/main.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>

	<!-- Login -->
	<link href="/css/error.css" rel="stylesheet" type="text/css"/>

	<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=">
	<!--[if IE 7]>
	<link rel="stylesheet" href="assets/css/fontawesome/font-awesome-ie7.min.css?v=">
	<![endif]-->

	<!--[if IE 8]>
	<link href="assets/css/ie8.css" rel="stylesheet" type="text/css"/>
	<![endif]-->
</head>

<body class="error">
<!--=== Error Title ===-->
<div class="title">
	<h1>403</h1>
</div>
<!-- /Error Title -->

<div class="actions">
	<div class="list-group">
		<li class="list-group-item list-group-header align-center">
			<%=langMessage.get("msg.login.loginStatusType.9")%>
		</li>
		<a href="/page/manager/dashboard.jsp" class="list-group-item"><i class="icon-home"></i><%=langMessage
			.get("form.text.page.history.dashborad") %>
		</a>
	</div>
</div>

<!-- Footer -->
<div class="footer">
	Melon - Flat &amp; Responsive Admin Template<br>&copy; 2013
</div>
<!-- /Footer -->
</body>
</html>