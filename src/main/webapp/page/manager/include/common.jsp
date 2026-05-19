<%@ page import="com.nv.commons.dto.Manager" %>
<%@ page import="com.nv.commons.constants.SessionKeyConstants" %>
<%@ page import="com.nv.commons.constants.WebSiteType" %>
<%@ page import="com.nv.commons.message.LangMessage" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%@ page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.constants.CurrencyType" %><%--
  Created by IntelliJ IDEA.
  User: Wong Seng Foong
  Date: 07/10/2025
  Time: 11:52 am
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
	Manager manager = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);
	WebSiteType commonWebsiteType = WebSiteType.getInstance(manager.getWebsiteType());
	LanguageType languageType = ManagerUtils.getLanguageType(session, request);
	LangMessage commonLangMessage = languageType.getLangMessage();
	CurrencyType commonCurrencyType = manager.getWebsiteTypeObj().getDefaultCurrencyType();
	String commonCurrencyTypeSymbol =  commonCurrencyType.getCurrencySymbol();
	String commonCurrencyFullName = commonCurrencyType.getFullName(commonLangMessage);
	String commonMangerUserId = manager.getUserId();
	String commonLanguageResourceKey =  languageType.getLanguageResourceKey();
%>

<script>
	if (typeof (PageConfig) == 'undefined') {
		PageConfig = {};
	}
	PageConfig.currencyFullName ='<%=commonCurrencyFullName%>';
	PageConfig.currencyType = <%=commonCurrencyType.unique()%>;
	PageConfig.DateFormatPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy%>';
	PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss%>';
	PageConfig.managerCurrencySymbol = '<%=commonCurrencyTypeSymbol%>'
</script>
