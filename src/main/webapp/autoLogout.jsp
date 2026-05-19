<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.nv.commons.constants.SessionKeyConstants" %>
<%@ page import="com.nv.commons.constants.SystemConstants" %>
<%@ page import="com.nv.commons.message.LangMessage" %>
<%@ page import="com.nv.commons.utils.AccountUtils" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<%
	//Note:
	//request.getAttribute是從Filter forward過來的
	//request.getParameter是從自己reload傳過來的

	String accountFilterStatus = (String) request.getAttribute("accountFilterStatus");
	if (accountFilterStatus == null) {
		accountFilterStatus = RequestParser.getStringParameter(request, 2, "accountFilterStatus", null);
	}
	if (accountFilterStatus != null) {

		//針對Ajax而設定的機制(使用者自己點擊頁面, 或頁面reload而進來的)
		//System.out.println("[autoLogout.jsp]redirectUrl:"+redirectUrl);
		String logoutDestPage = null;
		LangMessage langMessage = FrontendUtils.getLangMessage(session, request);
		if (session != null) {

			String roleType = (String) request.getAttribute("roleType");
			if (roleType == null) {
				roleType = RequestParser.getStringParameter(request, 20, "roleType", null);
			}

			switch (roleType) {
				case SessionKeyConstants.PlayerRoleType: //player
					//logoutDestPage = (String) session.getAttribute(SessionKeyConstants.UserLoginPage);
					//MEMO 目前SessionKeyConstants.UserLoginPage沒在用, player一律是導到 "/"
					logoutDestPage = "/";
					AccountUtils.logoutPlayer(session);
					break;
				case SessionKeyConstants.ManagerRole: //manager
					logoutDestPage = SystemConstants.MANAGER_LOGIN_PAGE;
					AccountUtils.logoutManager(session);
					break;
			}
		}

		if (logoutDestPage == null) {
			logoutDestPage = "/";
		}

		String alertMsg = "alert('" + langMessage.get("msg.login.loginStatusType." + accountFilterStatus) + "');";
		String redirectUrl = "top.location.href= '" + logoutDestPage + "';";

		out.println("<script>" + alertMsg + redirectUrl + "</script>");
	}
%>

