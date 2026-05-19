<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.message.LangMessage" %>
<%@ page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>

<%
	String functionTitle = RequestParser.getStringParameter(request, 100, "functionTitle");
	String gameId = RequestParser.getStringParameter(request, 20, "gameid");

	LanguageType sidebarLanguageType = ManagerUtils.getLanguageType(session, request);
	LangMessage sidebarLangMessage = sidebarLanguageType.getLangMessage();
%>

<div id="sidebar" class="sidebar-fixed">
	<div class="slimScrollDiv" style="position: relative; overflow: hidden; width: auto; height: 100%;">
		<div id="sidebar-content" style="overflow: hidden; width: auto; height: 100%;">
			<!--=== Navigation ===-->
			<ul id="nav">
				<li display-id="2" parent="#" level="1" style="" name="Overview" <%="Profile".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/cms/gamesProfile.jsp?gameid=<%=gameId%>">
						<i class="icon-search"></i><%=sidebarLangMessage.get("form.text.account.overview")%>
					</a>
				</li>
				<li display-id="2" parent="#" level="1" style="" name="Setting" <%="Setting".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/cms/gamesSetting.jsp?gameid=<%=gameId%>">
						<i class="icon-group"></i><%=sidebarLangMessage.get(
						"form.text.backOffice.breadcrumbs.setting")%>
					</a>
				</li>
				<li display-id="2" parent="#" level="1" style="" name="Report" <%="Report".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/cms/gameReport.jsp?gameid=<%=gameId%>">
						<i class="icon-star"></i><%=sidebarLangMessage.get(
						"form.text.backOffice.breadcrumbs.report")%>
					</a>
				</li>
			</ul>
		</div>
	</div>
</div>