<%@ page import="com.nv.commons.constants.SystemConstants" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title>Manager</title>
	<%@include file="/page/manager/include/htmlHead.jsp" %>

	<script>
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}

		PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;

		$(document).ready(function() {
			"use strict";

			setTimeout(() => {
				window.location.replace('/page/manager/member/search.jsp');
			}, 0);
		});
	</script>
</head>

</html>