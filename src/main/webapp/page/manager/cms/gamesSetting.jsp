<!DOCTYPE html>
<html lang="en">
<head>
	<%
		FrontendUtils.noCache(response);
	%>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<link rel="stylesheet" href="/css/fontawesome/font-awesome-ie7.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
	<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">

	<title><%=commonLangMessage.get("form.text.backOffice.menu.45")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.10")%>
	</title>


	<script type="text/javascript" src="/js/manager/app.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<%--Sidebar.js與Header.js在用 --%>
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<%--top.jsp、head.jsp在用 --%>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<!-- DataTables -->
	<script type="text/javascript"
		src="/js/manager/cms/gamesSetting.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<script>
		//set I18N
		I18N.setResource({});

		$(document).ready(function () {
			//加這個才有Breadcrumbs
			App.init(); // Init layout and core plugins

			{
				//直接用plugins.form-components.js，而不是app.jsp
				// Fix for class_selector in BS3 plugins.form-components.js
				var _base_resetForm = $.validator.prototype.resetForm;
				$.extend($.validator.prototype, {
					resetForm: function () {
						var resetForm_this = this;
						_base_resetForm.call(this);

						var currentForm = $(this.currentForm);
						var class_selector = ".form-group";
						if (currentForm.hasClass('form-vertical')) {
							class_selector = "*[class^=col-]";
						}

						currentForm.find(class_selector).each(function () {
							$(this).removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);
						});
						currentForm.find('.select2-container').removeClass(resetForm_this.settings.errorClass + ' ' + resetForm_this.settings.validClass);

						currentForm.find('label[generated="true"]').html('');
					}
				});
			}
			FormComponents.init();

			GamesSettingHandler.init();
		})
	</script>
</head>

<body class="theme-dark">
<%@include file="/page/manager/include/top.jsp" %>

<%@include file="/page/manager/include/sidebar.jsp" %>

<div id="container">

	<jsp:include page="/page/manager/cms/sidebar.jsp">
		<jsp:param name="functionTitle" value="Setting"/>
	</jsp:include>

	<div id="content">
		<%-- container2 start --%>
		<div class="container">
			<%
				String functionTitle = "Setting";
				String moduleName = "Game";
			%>
			<%@include file="/page/manager/include/cmsHead.jsp" %>

			<div id="gameSettingContainer">

				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">
								<!-- 左侧标题 -->
								<h4>
									<i style="line-height: 35px" class="icon-reorder"></i>
									<%=commonLangMessage.get("form.text.backOffice.setting")%>
								</h4>

								<div class="toolbar no-padding">
									<div class="btn-group">
                                  		<span class="btn btn-xs widget-collapse">
                                     		<i class="icon-angle-down" style="line-height: 35px"></i>
                                  		</span>
									</div>
								</div>
							</div>

							<div class="widget-content">
								<form class="form-vertical" id="updateGameDisplayOrderForm" name="updateGameDisplayOrderForm" action="#">
									<div class="row">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label" for="displayOrder">
													<%=commonLangMessage.get("form.text.backOffice.games.display")%>
													<span class="required">*</span>
												</label>
												<div class="input-container">
													<input type="text"
														id="displayOrder"
														name="displayOrder"
														class="form-control"
														autocomplete="off"
														placeholder="<%=commonLangMessage.get("form.text.backOffice.games.display")%>">
												</div>
												<label class="error-msg-block"></label>
											</div>
										</div>
									</div>
									<div class="modal-footer">
										<input type="button"
											value="<%=commonLangMessage.get("ui.text.reset")%>"
											name="resetButton"
											onclick='GamesSettingHandler.resetGameDisplayOrder()'
											class="btn btn-primary">
										<input type="button"
											value="<%=commonLangMessage.get("form.text.button.save")%>"
											name="save"
											onclick='GamesSettingHandler.updateGameDisplayOrder()'
											class="btn btn-primary">
									</div>
								</form>

							</div>

						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

</body>
</html>