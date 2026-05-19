<%@ page language="java" contentType="text/html;charset=UTF-8"
	import="com.nv.commons.constants.GameStatusType"
	import="com.nv.commons.constants.GameType"
	import="com.nv.commons.constants.PlatformType"
%>
<%
	FrontendUtils.noCache(response);
	boolean enableUpdateStatus = true;

%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/formComponent.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>

	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title><%=commonLangMessage.get("form.text.backOffice.menu.50")%> | <%=commonLangMessage.get(
		"form.text.backOffice.menu.45")%>
	</title>


	<!-- App -->
	<script type="text/javascript" src="/js/util/ImageUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javaScript"
		src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript"
		src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/holder.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/cms/games.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript"
		src="/js/manager/plugins.form-components.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<style>
      /* 讓狀態 icon 能定位在卡片右上角 */
      .thumbnail {
        position: relative;
        cursor: pointer;
      }

      .status-icon {
        position: absolute;
        top: 8px;
        right: 8px;
        font-size: 18px;
        line-height: 1;
        z-index: 2; /* 確保蓋在圖片上 */
      }

      /* 顏色（沿用你頁面既有配色） */
      .text-success {
        color: #28a745;
      }

      .text-danger {
        color: #dc3545;
      }

      .modal-xlg {
        width: 60%;
      }
	</style>
	<script type="text/javascript">
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.creatGame = false;
		PageConfig.pageSize = <%=SystemConstants.DEFAULT_GAMES_PER_PAGE_OF_API_SERVER%>;
		PageConfig.enableUpdateStatus = <%=enableUpdateStatus%>;

		PageConfig.lang = "<%=commonLanguageResourceKey%>";

		PageConfig.showDialog = false;
		PageConfig.noFileText = '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>';
		PageConfig.validator;
		PageConfig.extraDataRequired = [1, 17, 23, 19]; // can change('MG', 'Sunbet', 'JDB', 'PG')
		PageConfig.emptyGame =
			'<div class="thumbnail">'
			+ '<div class="status-icon">'
			+ '<i class="fa fa-eye text-success"></i>'
			+ '</div>'
			+ '<img id="emptyData" style="width: 300px; height: 200px;">'
			+ '<div class="caption">'
			+ '<h3>-</h3>'
			+ '<p id="name">-</p>'
			+ '<p>-</p>'
			+ '<p>-</p>'
			+ '<p>-</p>'
			+ '<p id="gameId" style="display:none;">-</p>'
			+ '</div></div>';


		PageConfig.slotUnique = <%=GameType.SLOT.unique()%>;
		PageConfig.featuredGames = [];
		//set I18N
		I18N.setResource({
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.cms': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.cms")%>',
			'form.text.backOffice.breadcrumbs.games': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.games")%>',
			'ui.text.profile.edit': '<%=commonLangMessage.get("ui.text.profile.edit")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.notification.statusPromptMessage': '<%=commonLangMessage.get("form.text.backOffice.notification.statusPromptMessage")%>',
			'form.text.on': '<%=commonLangMessage.get("form.text.on")%>',
			'form.text.off': '<%=commonLangMessage.get("form.text.off")%>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'msg.manager.create.success': '<%=commonLangMessage.get("msg.manager.create.success")%>',
			'msg.manager.update.success': '<%=commonLangMessage.get("msg.manager.update.success")%>',
			'msg.manager.set.success': '<%=commonLangMessage.get("msg.manager.set.success")%>',
			'msg.info.selectTwoGames': '<%=commonLangMessage.get("msg.info.selectTwoGames")%>',
			'msg.error.info.image.sizeNotMatch': '<%=commonLangMessage.get("msg.error.info.image.sizeNotMatch")%>',
			'form.text.backOffice.file.noFileSelected': '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>',
		});
		$(document).ready(function () {
			"use strict";
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins
			FormComponents.init(); // Init all form-specific plugins
			HeadHandler.init();
			MenuHandler.init();

			// Debug: Check if GamesHandler is loaded
			if (typeof GamesHandler === 'undefined') {
				console.error('GamesHandler is not defined! Check if games.js loaded correctly.');
				console.log('Dependencies check:', {
					HashMap: typeof HashMap,
					JsCache: typeof JsCache,
					I18N: typeof I18N,
					DataTableHandler: typeof DataTableHandler,
					NotifyHandler: typeof NotifyHandler,
					Select2Handler: typeof Select2Handler,
					GameStatusType: typeof GameStatusType,
					GameType: typeof GameType,
					DBOrderType: typeof DBOrderType
				});
				return;
			}

			GamesHandler.init();
		});
	</script>
</head>

<body class="theme-dark">

<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<div id="container">
	<%@include file="/page/manager/include/sidebar.jsp" %>

	<!-- /Sidebar -->

	<div id="content">
		<div class="container">
			<!-- Breadcrumbs line -->
			<%@include file="/page/manager/include/head.jsp" %>
			<div class="row">
				<div class="col-md-12" style="display: none">
					<div class="widget box">
						<div class="widget-header">
							<h4>
								<i class="icon-reorder"></i> <%=commonLangMessage.get(
								"form.text.backOffice.conditionPanel")%>
							</h4>
							<div class="toolbar no-padding">
								<div class="btn-group">
										<span class="btn btn-xs widget-collapse"><i
											class="icon-angle-down"></i></span>
								</div>
							</div>
						</div>
						<div class="widget-content">
							<form class="form-vertical" name="searchForm">
								<div class="form-group">
									<div class="row">
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.games.vendor")%>
											</label>
											<select class="form-control" id="vendor" name="vendor">
												<option value="-1"><%=commonLangMessage.get("ui.text.report.all")%>
												</option>
											</select>
										</div>
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.bonusTemplate.gameType")%>
											</label>
											<select class="form-control" id="gameType" name="gameType">
												<option value="-1"><%=commonLangMessage.get("ui.text.report.all")%>
												</option>
												<%
													for (GameType game : GameType.getSortList()) {
												%>
												<option value="<%=game.unique()%>"><%=game.getFullName(
													commonLangMessage)%>
												</option>
												<%
													}
												%>
											</select>
										</div>
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.status")%>
											</label>
											<select class="form-control" id="gameStatus"
												name="gameStatus">
												<option value="-99"><%=commonLangMessage.get("ui.text.report.all")%>
												</option>
												<%
													for (GameStatusType status : GameStatusType.values()) {
														if (
															status.unique() == GameStatusType.INVISIBLE.unique()
																|| status.unique()
																== GameStatusType.UNKNOWN.unique()) {
															continue;
														}
												%>
												<option value="<%=status.unique()%>"><%=status.getDisplayName(
													commonLangMessage)%>
												</option>
												<%
													}
												%>
											</select>
										</div>
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.platformType")%>
											</label> <select
											class="form-control" id="platformType"
											name="platformType">
											<option value="-99"><%=commonLangMessage.get("ui.text.report.all")%>
											</option>
											<%
												for (PlatformType device : PlatformType.values()) {
													if (device == PlatformType.APP) {
														continue;
													}
											%>
											<option value="<%=device.unique()%>"><%=device.getName()%>
											</option>
											<%
												}
											%>
										</select>
										</div>
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.menu.117")%>
											</label>
											<select class="form-control" id="category" name="category">
												<option value="-99" data-gametype="0"><%=commonLangMessage.get(
													"ui.text.report.all")%>
												</option>
											</select> <input type="hidden" id="categoryGameType"
											name="categoryGameType"/>
										</div>
										<div class="col-md-2">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.games.sort")%>
											</label>
											<select class="form-control" id="sort" name="sort">
												<option value="name"><%=commonLangMessage.get(
													"form.text.backOffice.games.name")%>
												</option>
												<option value="code"><%=commonLangMessage.get(
													"form.text.backOffice.games.code")%>
												</option>
												<option value="gameType"><%=commonLangMessage.get(
													"form.text.backOffice.bonusTemplate.gameType")%>
												</option>
												<option value="displayOrder"><%=commonLangMessage.get(
													"form.text.backOffice.games.display")%>
												</option>
											</select>
										</div>
									</div>
								</div>
								<div class="form-group">
									<div class="row">
										<div class="col-md-10">
											<label class="control-label"><%=commonLangMessage.get(
												"form.text.backOffice.games.name")%>
											</label>
											<input type="hidden" id="games" name="games"
												style="width: 300px" class="input-xlarge"/>
										</div>
										<div class="col-md-2">
											<div class="make-switch"
												data-on-label="<%=commonLangMessage.get("form.text.backOffice.games.desc")%>"
												data-off-label="<%=commonLangMessage.get("form.text.backOffice.games.asc")%>"
												id="sorting" name="sorting">
												<input type="checkbox" checked="checked" class="toggle"/>
											</div>
											<input type="hidden" id="sortingType" name="sortingType">
										</div>
									</div>
								</div>
								<div class="form-group">
									<div class="row">
										<div class="col-md-6"></div>
										<div class="col-md-6 text-right">
											<div>
												<input type="button"
													value="<%=commonLangMessage.get("form.text.button.search")%>"
													name="search"
													onclick='GamesHandler.search()' class="btn btn-primary">
											</div>
										</div>
									</div>
								</div>
							</form>
						</div>
					</div>
				</div>
				<div class="col-md-12">

				</div>
				<div class="modal fade" role="dialog" id="gameUrlModal">
					<div class="modal-dialog modal-xlg">
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;
								</button>
								<h4 class="modal-title">Game URL</h4>
							</div>
							<div class="modal-body">
								<div class='row'>
									<!-- WEB游戏链接 -->
									<div class="form-group col-md-6" id="webGameUrlDiv" style="display: none">
										<label class="control-label">Link path(WEB)</label>
										<div class="input-group">
          									<textarea class="form-control" style="resize: none" id="webGameUrl" rows="1"
												readonly></textarea>
											<div class="input-group-btn">
												<button onclick="javascript:GamesHandler.copy(this)"
													class="btn btn-primary" type="button">
													<i class="icon-copy"></i>
												</button>
											</div>
										</div>
									</div>

									<!-- HTML5游戏链接 -->
									<div class="form-group col-md-6" id="h5GameUrlDiv" style="display: none">
										<label class="control-label">Link path(HTML5)</label>
										<div class="input-group">
          									<textarea class="form-control" style="resize: none" id="h5GameUrl" rows="1"
			  										readonly></textarea>
											<div class="input-group-btn">
												<button onclick="javascript:GamesHandler.copy(this)"
													class="btn btn-primary" type="button">
													<i class="icon-copy"></i>
												</button>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="modal fade" id="updateGameData" role="dialog">
					<div class="modal-dialog">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal"
									onclick="GamesHandler.closeWindow()">&times;
								</button>
								<h4 class="modal-title"></h4>
							</div>
							<div class="modal-body">
								<form class="form-horizontal" id="updateGameForm"
									name="updateGameForm" novalidate="novalidate">
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.name")%>
										</label>
										<div class="col-md-9">
											<input type="hidden" id="updateGameID" name="updateGameID">
											<label class="control-label" id="updateGameName"
												name="updateGameName"></label>
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.code")%>
										</label>
										<div class="col-md-9">
											<label class="control-label" id="updateGameCode"
												name="updateGameCode"></label>
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.bonusTemplate.gameType")%>
										</label>
										<div class="col-md-9">
											<select class="form-control isEditable" id="updateType">
												<%
													for (GameType type : GameType.getSortList()) {
												%>
												<option value="<%=type.unique()%>"><%=type.getFullName(
													commonLangMessage)%>
												</option>
												<%
													}
												%>
											</select>
											<input type="hidden" id="updateTypeHidden" name="updateType"/>

										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.platformType")%>
										</label>
										<div class="col-md-9">
											<select id="updateDeviceType" name="updateDeviceType"
												class="select2-select-00 full-width-fix isEditable" multiple size="5">
												<%
													for (PlatformType device : PlatformType.values()) {
														if (device == PlatformType.APP) {
															continue;
														}
												%>
												<option value="<%=device.unique()%>"><%=device.getName().toUpperCase()%>
												</option>
												<%
													}
												%>
											</select>
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.display")%><span
											class="required">*</span></label>
										<div class="col-md-9">
											<input class="form-control" id="updateDisplayOrder"
												name="updateDisplayOrder" maxlength="11">
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.gameCategory")%>
										</label>
										<div class="col-md-9">
											<select class="select2-select-00 full-width-fix"
												id="updateGameCategory" name="updateGameCategory" multiple>
											</select>
										</div>
									</div>

									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.status")%><span
											class="required">*</span></label>
										<div class="col-md-9">
											<select class="form-control" id="updateStatus"
												name="updateStatus">
												<%
													for (GameStatusType status : GameStatusType.values()) {
														if (
															status.unique() == GameStatusType.INVISIBLE.unique()
																|| status.unique()
																== GameStatusType.UNKNOWN.unique()) {
															continue;
														}
												%>
												<option value="<%=status.unique()%>"><%=status.getDisplayName(
													commonLangMessage)%>
												</option>
												<%
													}
												%>
											</select>
										</div>
									</div>
								</form>
							</div>
							<div class="modal-footer">
								<button class="btn btn-primary" id="gameReset"
									onclick="GamesHandler.cancelChange()"><%=commonLangMessage.get("ui.text.reset")%>
								</button>
								<button class="btn btn-primary" id="gameConfirm"
									onclick="GamesHandler.updateGame()"><%=commonLangMessage.get("ui.text.confirm")%>
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!--=== Page Content ===-->
			<div class="gamesTable" style="display: none;">

				<table id="gamesTable" class="table table-nowrap" width="100%">
					<thead>
					<tr>
						<th></th>
						<th></th>
						<th></th>
						<th></th>
						<th></th>
					</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
		</div>
		<!-- /.container -->

	</div>
</div>
<optgroup style="display: none;" id="categoryGroupTemplate"></optgroup>
<option style="display: none" id="elementTemplate" data-gametype=""></option>
</option>
<ul id="optionAll" style="display: none"></ul>
</body>
</html>