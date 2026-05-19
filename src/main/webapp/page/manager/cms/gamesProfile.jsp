<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.constants.GameStatusType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>

<%
	FrontendUtils.noCache(response);

	List<GameStatusType> gameStatusType = Arrays.stream(GameStatusType.getAll()).filter(o -> o.unique() >= 0).toList();
%>

<link rel="stylesheet" href="/css/fontawesome/font-awesome-ie7.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
<script type="text/javascript">

</script>
<!DOCTYPE html>

<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

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
		src="/js/manager/cms/gamesProfile.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<style>
      /* Table styling */
      .table th,
      .table td {
        font-size: 13px;
      }

      /* Form controls and inputs */
      input,
      select,
      textarea,
      .form-control {
        font-size: 14px;
      }

      /* Button styling */
      .btn {
        font-size: 14px;
      }

      /* Icons */
      i {
        font-size: 16px;
      }

      /* Modal title and headings */
      .modal-title,
      h4 {
        font-size: 14px;
      }

      /* Labels */
      label,
      .control-label {
        font-size: 13px;
      }
	</style>

	<script>
		//set I18N
		I18N.setResource({
			'ui.text.profile.edit': '<%=commonLangMessage.get("ui.text.profile.edit")%>',
		});

		PageConfig.GameStatusType = <%=GameStatusType.toJsonString()%>;

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

			GamesProfileHandler.init();
		})
	</script>
</head>

<body class="theme-dark">
<%@include file="/page/manager/include/top.jsp" %>

<%-- container start --%>
<div id="container">
	<jsp:include page="/page/manager/cms/sidebar.jsp">
		<jsp:param name="functionTitle" value="Profile"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">
		<%-- container2 start --%>
		<div class="container">

			<%
				String functionTitle = "Profile";
				String moduleName = "Game";
			%>
			<%@include file="/page/manager/include/cmsHead.jsp" %>

			<div class="modal fade" id="viewGamePhoto" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal">&times;</button>
							<h4 class="modal-title" id="modalTitle"><%=commonLangMessage.get(
								"form.text.backOffice.games.gamePhoto")%>
							</h4>
						</div>
						<div class="modal-body">
							<div class="form-group">
								<div class="row">
									<div class="col-md-12" name="photo"
										Style="display: flex; justify-content: center; align-items: center;">

									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="modal fade" id="updateStatusModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<form class="form-horizontal" name="updateStatusForm" action="#">
						<input type='hidden' name='gameId' value=''>

						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" id="title">
									<%=commonLangMessage.get("form.text.account.editStatus")%>
								</h4>
							</div>

							<div class="modal-body">
								<div class="row" style="margin-left: 5px">
									<div class="col-md-6">
										<div class="form-group">
											<label class="control-label">
												<%=commonLangMessage.get("form.text.backOffice.status")%>
											</label>
											<select class="form-control" id="gameStatusId" name="status">
												<% for (GameStatusType status : gameStatusType) { %>
												<option value="<%=status.unique()%>">
													<%=status.getName()%>
												</option>
												<% } %>
											</select>
										</div>
									</div>
								</div>
							</div>

							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
									name="resetButton" onclick='GamesProfileHandler.resetStatus()'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
									name="save" onclick='GamesProfileHandler.updateStatus()'
									class="btn btn-primary">
							</div>
						</div>
					</form>
				</div>
			</div>
			<%-- profileContainer start --%>
			<div id='gameSettingContainer'>
				<%-- General start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">
								<!-- 左侧标题 -->

								<h4>
									<i style="line-height: 35px" class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.general")%>
								</h4>


								<div class="toolbar no-padding">
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"  style="line-height: 35px"></i>
											</span>
									</div>
								</div>
							</div>

							<div class="widget-content">
								<table class="table table-hover">
									<tbody>
									<tr id='<%=commonLangMessage.get("form.text.backOffice.games.gamePhoto")%>flag'>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gamePhoto")%>
										</th>
										<td>
											<button id="gamePhoto" class="btn"
												onclick="GamesProfileHandler.viewGamePhoto(this)">
												<i class="icon-search">
												</i>
											</button>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gameName")%>
										</th>
										<td id='gameName'></td>
										<td></td>
										<td></td>
									</tr>

									<tr id='<%=commonLangMessage.get("form.text.backOffice.status")%>flag'>
										<th><%=commonLangMessage.get("form.text.backOffice.status")%>
										</th>
										<td style="vertical-align: middle"
											id='status'>
										</td>
										<td>
											<input type="button" style="line-height: 15px"
												value="<%=commonLangMessage.get("ui.text.profile.edit")%>"
												name="gotoEditGeneral"
												onclick='GamesProfileHandler.getEditStatus()'
												class="btn btn-primary btn-sm">

										</td>
										<td></td>
									</tr>

									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gameCode")%>
										</th>
										<td id='gameCode'></td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.games.gameType")%>
										</th>
										<td id='gameType'></td>
										<td></td>
										<td></td>
									</tr>
									</tbody>
								</table>
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