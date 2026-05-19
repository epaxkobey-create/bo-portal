<%@ page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
	String select2Lang = ManagerUtils.getLanguageType(session, request).getLangMessage().getLang();
%>

<!-- Forms -->
<script type="text/javascript" src="/js/plugins/select2/select2.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/util/Select2.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/uniform/jquery.uniform.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/tagsinput/jquery.tagsinput.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>