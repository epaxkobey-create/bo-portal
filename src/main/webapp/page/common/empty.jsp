<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>Empty</title>
	<%@include file="/page/manager/include/common.jsp" %>

	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/formComponent.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>

	<!-- App -->
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>


	<script type="text/javascript">
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};

		}

		$(document).ready(function() {
			"use strict";
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins
//	    FormComponents.init(); // Init all form-specific plugins
// 	    HeadHandler.init();
			MenuHandler.init();
			MenuHandler.updateMenuTask.execute();
		});
	</script>
</head>

<body class="theme-dark">

<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<div id="container">
	<%@include file="/page/manager/include/sidebar.jsp" %>

	<!-- /Sidebar -->

	<div id="content">
		<div class="container">
			<!-- Breadcrumbs line -->
			<%@include file="/page/manager/include/head.jsp" %>
			<!-- /Breadcrumbs line -->
			<!--=== Page Content ===-->
			<div class="col-md-12">
			</div>
		</div>
		<!-- /.container -->

	</div>
</div>
</body>
</html>