<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>

<%

	String providerId = RequestParser.getStringParameter(request, 6, "providerId", null);
	String gameId = RequestParser.getStringParameter(request,6,"gameid", null);
	String i18nKey = functionTitle.toLowerCase();

%>
<div class="crumbs">
	<ul id="breadcrumbs" class="breadcrumb">
		<li id="headTemplate" style="display: list-item;">
			<i class="icon-home"></i>
			<a href="../dashboard.jsp"><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>
			</a>
		</li>
		<li id="headTemplate" style="display: list-item;">
			<%
				if (moduleName.equalsIgnoreCase("provider")) {
			%>
			<a href="#"><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.provider")%>
			</a>
			<%} else {%>
			<a href="#"><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.games")%>
			</a>
			<%}%>
		</li>

		<li id="headTemplate" class="current" style="display: list-item;">
			<% if (moduleName.equalsIgnoreCase("provider")) { %>
			<% if ("Profile".equalsIgnoreCase(functionTitle)) { %>
			<a href="providerProfile.jsp?providerId=<%=providerId%>">
				<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.profile")%>
			</a>
			<% } else if ("Report".equalsIgnoreCase(functionTitle)) { %>
			<a href="cmsReport.jsp?providerId=<%=providerId%>">
				<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.report")%>
			</a>
			<% }  %>
			<% } else { %>
			<% if ("Profile".equalsIgnoreCase(functionTitle)) { %>
			<a href="gamesProfile.jsp?gameId=<%=gameId%>">
				<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.profile")%>
			</a>
			<% } else if ("Report".equalsIgnoreCase(functionTitle)) { %>
			<a href="gameReport.jsp?gameId=<%=gameId%>">
				<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.report")%>
			</a>
			<% } else if ("Setting".equalsIgnoreCase(functionTitle)) { %>
			<a href="gamesSetting.jsp?gameId=<%=gameId%>">
				<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.setting")%>
			</a>
			<% } %>
			<% } %>
		</li>
	</ul>
</div>
<div class="page-header">
	<div class="page-title">
		<h3><%=commonLangMessage.get("form.text.backOffice.breadcrumbs." + i18nKey)%>
		</h3>
		<span></span>
	</div>
</div>