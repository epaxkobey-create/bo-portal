<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.message.LangMessage" %>
<%@ page import="com.nv.commons.utils.Validator" %>
<%
	LangMessage langMessage = FrontendUtils.getLangMessage(session, request);
	String message = (String) request.getAttribute("message");
	if (message == null) {
		// unknown length
		message = Validator.stripXSS(request.getParameter("message"));
	}
%>
<!DOCTYPE html>

<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>ERROR | Melon - Flat &amp; Responsive Admin Template</title>

	<!--=== CSS ===-->

	<!-- Bootstrap -->
	<link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>

	<!-- Theme -->
	<link href="/css/main.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
	<link href="/css/plugins.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
	<link href="/css/responsive.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
	<link href="/css/icons.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>

	<!-- Login -->
	<link href="/css/error.css" rel="stylesheet" type="text/css"/>

	<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=">
	<!--[if IE 7]>
	<link rel="stylesheet" href="assets/css/fontawesome/font-awesome-ie7.min.css?v=">
	<![endif]-->

	<!--[if IE 8]>
	<link href="assets/css/ie8.css" rel="stylesheet" type="text/css"/>
	<![endif]-->
	<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,600,700' rel='stylesheet' type='text/css'>

	<!--=== JavaScript ===-->

	<script type="text/javascript" src="<%=FrontendUtils.getJQueryPath() %>"></script>

	<script type="text/javascript"
		src="/js/plugins/bootstrap/bootstrap.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/lodash.compat.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
	<!--[if lt IE 9]>
		<script src="/js/util/html5shiv.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<![endif]-->
</head>

<body class="error">
<!--=== Error Title ===-->
<div class="title">
	<h1>Sorry !! Find Error</h1>
</div>
<!-- /Error Title -->

<div class="actions">
	<div class="list-group">
		<li class="list-group-item list-group-header align-center"><%=message%>
		</li>
		<a href="javascript: history.back();" class="list-group-item"><i class="icon-arrow-left"></i><%=langMessage.get(
			"form.text.page.history.back") %>
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