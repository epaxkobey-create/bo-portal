<%@page import="com.nv.commons.message.LangMessage" %>
<%@page import="com.nv.commons.constants.LanguageType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%@ page import="com.nv.commons.constants.WebSiteType" %>
<%@ page import="com.nv.commons.utils.WebSiteTypeUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%
	String userId = RequestParser.getStringParameter(request, 50, "userId");
	String encodedUserId =userId!=null? URLEncoder.encode(userId, StandardCharsets.UTF_8).replace("+", "%20"):"";
	int currency = RequestParser.getIntParameter(request, "currency", -1);
	String parseBonusTurnoverId = RequestParser.getStringParameter(request, 20, "bonusId", null);
	String functionTitle = RequestParser.getStringParameter(request, 100, "functionTitle");

	LanguageType sideBar2languageType = ManagerUtils.getLanguageType(session, request);
	LangMessage sideBar2langMessage = sideBar2languageType.getLangMessage();
	//TODO:這頁會有新的UI

	WebSiteType sideBar2WebSiteHead = WebSiteTypeUtils.getWebSiteByBoDomain(request.getServerName());
	if (sideBar2WebSiteHead == null) {
		sideBar2WebSiteHead = WebSiteTypeUtils.getWebSiteByBoAfDomain(request.getServerName());
	}

//	boolean enableRafCommission = sideBar2WebSiteHead.isEnableReferralCommission();
%>
<div id="sidebar" class="sidebar-fixed">
	<div class="slimScrollDiv"
		style="position: relative; overflow: hidden; width: auto; height: 100%;">
		<div id="sidebar-content"
			style="overflow: hidden; width: auto; height: 100%;">

			<!--=== Navigation ===-->
			<ul id="nav">
				<%--
				<li id="menu58" display-id="2" parent="#" level="1" style="" name="Member" class="open">
					<a href="#"><i class="icon-user"></i>Member <i class="arrow icon-angle-down"></i></a>
					<ul class="sub-menu" style="display: block;" name="Member_sub">
						<li id="menu55" display-id="1" parent="10" level="2" style="display: list-item;" name="Overview"><a href="/page/manager/member/profile.jsp"> <i class="icon-search"></i>Overview</a></li>
						<li id="menu56" display-id="2" parent="10" level="2" style="display: list-item;" name="Setting" class="current"><a href="/page/manager/member/setting.jsp"> <i class="icon-group"></i>Setting</a></li>
						<li id="menu57" display-id="3" parent="10" level="2" style="display: list-item;" name="Report"><a href="/page/manager/member/report.jsp"> <i class="icon-star"></i>Report</a></li>
					</ul>
				</li>
				--%>


				<li id="menu58" display-id="2" parent="#" level="1" style="" name="Overview" <%="Profile".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/member/profile.jsp?userId=<%=encodedUserId %><%=(parseBonusTurnoverId != null ? "&bonusId=" + parseBonusTurnoverId : "") + "&currency=" + currency%>">
						<i class="icon-search"></i><%=sideBar2langMessage.get("form.text.account.overview")%>
					</a>
				</li>
					<li id="menu58" display-id="2" parent="#" level="1" name="Setting"
						<%= "Setting".equals(functionTitle) ? "class='open'" : "" %>>
						<a href="/page/manager/member/PlayerResponsibilitySetting.jsp?userId=<%=encodedUserId%><%= (parseBonusTurnoverId != null ? "&bonusId=" + parseBonusTurnoverId : "")+ "&currency=" + currency %>">
							<i class="icon-group"></i><%= sideBar2langMessage.get("form.text.backOffice.breadcrumbs.setting") %>
						</a>
					</li>
<%--				<%--%>
<%--					}--%>
<%--				%>--%>
				<li id="menu58" display-id="2" parent="#" level="1" style="" name="Report" <%="Report".equals(
					functionTitle) ? "class='open'" : "" %>>
					<a href="/page/manager/member/report.jsp?userId=<%=encodedUserId%><%=(parseBonusTurnoverId != null ? "&bonusId=" + parseBonusTurnoverId : "") + "&currency=" + currency%>">
						<i class="icon-star"></i><%=sideBar2langMessage.get("form.text.backOffice.breadcrumbs.report")%>
					</a>
				</li>

			</ul>

			<div class="fill-nav-space"></div>
			<div class="fill-nav-space"></div>
		</div>
		<div class="slimScrollBar"
			style="background: rgb(0, 0, 0); width: 7px; position: absolute; top: 0px; opacity: 0.4; display: none; border-radius: 7px; z-index: 99; right: 1px; height: 945px;"></div>
		<div class="slimScrollRail"
			style="width: 7px; height: 100%; position: absolute; top: 0px; display: none; border-radius: 7px; background: rgb(51, 51, 51); opacity: 0.2; z-index: 90; right: 1px;"></div>
	</div>
	<div id="divider" class="resizeable"></div>

	<ul id="menuTemplate" class="sub-menu" style="display: none;">
	</ul>
	<li id="subTemplate" display-id="" parent="" level=""
		style="display: none;"><a href=""> <i
		class="icon-angle-right"></i>
	</a></li>
</div>