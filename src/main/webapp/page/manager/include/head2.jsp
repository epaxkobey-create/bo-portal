
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%

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

			<a href="#"><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.member")%>
			</a>
		</li>
		<li id="headTemplate" class="current" style="display: list-item;">
			<%
				if ("Profile".equals(functionTitle)) {
			%>
			<a href="profile.jsp?userId=<%=userId + "&currency=" + commonCurrencyType.unique()%>"><%=commonLangMessage.get(
				"form.text.backOffice.breadcrumbs.profile")%>
			</a>
			<%
			} else if ("Setting".equals(functionTitle)) {
			%>
			<a href="setting.jsp?userId=<%=userId%>"><%=commonLangMessage.get(
				"form.text.backOffice.breadcrumbs.setting")%>
			</a>
			<%
			} else if ("Report".equals(functionTitle)) {
			%>
			<a href="report.jsp?userId=<%=userId%>"><%=commonLangMessage.get("form.text.backOffice.breadcrumbs.report")%>
			</a>
<%--			<%--%>
<%--			} else if ("Commission".equals(functionTitle) && enableRafCommission) {--%>
<%--			%>--%>
<%--			<a href="commission.jsp?userId=<%=userId%>"><%=commonLangMessage.get(--%>
<%--				"form.text.backOffice.breadcrumbs.commission")%>--%>
<%--			</a>--%>
			<%
				}
			%>
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