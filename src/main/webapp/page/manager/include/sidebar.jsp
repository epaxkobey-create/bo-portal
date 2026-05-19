<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<link href="/css/manager/sidebar.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
<div id="sidebar" class="sidebar-fixed">
	<div id="sidebar-content">

		<!--=== Navigation ===-->
		<ul id="nav">
		</ul>

	</div>
	<div id="divider" class="resizeable"></div>

	<ul id="menuTemplate" class="sub-menu" style="display:none;">
		<li id="subTemplate" display-id="" parent="" level="" style="display: none;">
			<a href="">
				<i class="icon-angle-right"></i>
			</a>
		</li>
	</ul>
</div>
