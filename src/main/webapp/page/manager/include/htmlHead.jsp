<%@page import="com.nv.commons.system.Setting" %>
<%@page import="com.nv.commons.constants.WebSiteType" %>
<%@page import="com.nv.commons.utils.FrontendUtils" %>
<%@page import="com.nv.commons.utils.WebSiteTypeUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
	FrontendUtils.noCache(response);
	WebSiteType webSiteHead = WebSiteTypeUtils.getWebSiteByBoDomain(request.getServerName());
	if (webSiteHead == null) {
		webSiteHead = WebSiteTypeUtils.getWebSiteByBoAfDomain(request.getServerName());
	}
	String websiteTypeShortName = webSiteHead.getShortName();

	String customizeCss = "main";

	String customizeDatatableBootstrap = "datatables_bootstrap";

%>
<%--<link rel="SHORTCUT ICON"--%>
<%--	href="/images/<%= webSiteHead.getTemplateVersion()%>/web/<%=websiteTypeShortName%>/<%=websiteTypeShortName%>-favicon.ico"/>--%>
<%--<link rel="bookmark"--%>
<%--	href="/images/<%= webSiteHead.getTemplateVersion()%>/web/<%=websiteTypeShortName%>/<%=websiteTypeShortName%>-favicon.ico"/>--%>
<!--=== CSS ===-->

<!-- Bootstrap -->
<link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css?v=<%=FrontendUtils.getJsFileVersion()%>"/>

<!-- jQuery UI -->
<!--<link href="plugins/jquery-ui/jquery-ui-1.10.2.custom.css" rel="stylesheet" type="text/css" />-->
<!--[if lt IE 9]>
<link rel="stylesheet" type="text/css" href="/js/plugins/jquery-ui/jquery.ui.1.10.2.ie.css?v=<%=FrontendUtils.getJsFileVersion()%>"/>
<![endif]-->

<!-- Theme -->
<link href="/css/<%=customizeCss%>.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
<link href="/css/plugins/<%=customizeDatatableBootstrap%>.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet"
	type="text/css"/>

<link href="/css/plugins.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
<link href="/css/responsive.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
<link href="/css/icons.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>

<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
<!--[if IE 7]>
<link rel="stylesheet" href="/css/fontawesome/font-awesome-ie7.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
<![endif]-->

<!--[if IE 8]>
<link href="/css/ie8.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css" />
<![endif]-->
<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,600,700' rel='stylesheet' type='text/css'>

<!--=== JavaScript ===-->

<script type="text/javascript" src="<%=FrontendUtils.getJQueryPath()%>"></script>
<script type="text/javascript" src="<%=FrontendUtils.getJQueryUIPath()%>"></script>

<script type="text/javascript"
	src="/js/plugins/bootstrap/bootstrap.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/util/lodash.compat.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
<script src="/js/util/html5shiv.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<![endif]-->


<!-- Switch -->
<script type="text/javascript"
	src="/js/plugins/bootstrap-switch/bootstrap-switch.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- Smartphone Touch Events -->
<script type="text/javascript"
	src="/js/plugins/touchpunch/jquery.ui.touch-punch.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/event.swipe/jquery.event.move.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/event.swipe/jquery.event.swipe.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- General -->
<script type="text/javascript" src="/js/util/breakpoints.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/respond/respond.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<!-- Polyfill for min/max-width CSS3 Media Queries (only for IE8) -->
<script type="text/javascript"
	src="/js/plugins/cookie/jquery.cookie.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/slimscroll/jquery.slimscroll.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/slimscroll/jquery.slimscroll.horizontal.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- Encrypt -->
<script type="text/javaScript" src="/js/util/EncryptUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- Noty -->
<script type="text/javascript" src="/js/plugins/noty/jquery.noty.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/plugins/noty/layouts/top.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript"
	src="/js/plugins/noty/themes/default.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/util/NotifyUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- Validate -->
<script type="text/javascript"
	src="/js/plugins/validation/jquery.validate.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript" src="/js/util/Validate.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<jsp:include page="/page/common/message.jsp"/>

<!-- Const -->
<script type="text/javascript" src="/js/common/const.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

<!-- Block -->
<script type="text/javascript"
	src="/js/plugins/blockui/jquery.blockUI.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
<script type="text/javascript">
	if (typeof (PageConfig) == 'undefined') {
		PageConfig = {};
	}

	PageConfig.version = '<%=Setting.FILE_VERSION%>';
</script>