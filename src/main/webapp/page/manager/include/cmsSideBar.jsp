<%@page import="com.nv.commons.message.LangMessage" %>
<%@page import="com.nv.commons.constants.LanguageType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%
	String providerId = RequestParser.getStringParameter(request, 3, "providerId");

	String functionTitle = RequestParser.getStringParameter(request, 100, "functionTitle");

	LanguageType sideBar2languageType = ManagerUtils.getLanguageType(session, request);
	LangMessage sideBar2langMessage = sideBar2languageType.getLangMessage();
%>
<div id="sidebar" class="sidebar-fixed">
	<div class="slimScrollDiv"
		style="position: relative; overflow: hidden; width: auto; height: 100%;">
		<div id="sidebar-content"
			style="overflow: hidden; width: auto; height: 100%;">
			<!--=== Navigation ===-->
			<ul id="nav">
				<li  display-id="2" parent="#" level="1" style="" name="Overview" <%="Profile".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/cms/providerProfile.jsp?providerId=<%=providerId %>">
						<i class="icon-search"></i><%=sideBar2langMessage.get("form.text.account.overview")%>
					</a>
				</li>
				<li  display-id="2" parent="#" level="1" style="" name="Report"  <%="Report".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/cms/cmsReport.jsp?providerId=<%=providerId%>">
						<i class="icon-star"></i><%=sideBar2langMessage.get("form.text.backOffice.breadcrumbs.report")%>
					</a>
				</li>
			</ul>
		</div>
	</div>
</div>