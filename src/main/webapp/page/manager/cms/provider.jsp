<%@ page language="java" contentType="text/html;charset=UTF-8"
	import="com.nv.commons.constants.SystemConstants"
	import="com.nv.commons.constants.VendorStatusType"
	import="com.nv.commons.constants.ProviderStatusType"
	import="com.nv.commons.constants.GameType"

%>
<%@ page import="com.nv.commons.constants.PlatformType" %>
<%
	FrontendUtils.noCache(response);

	boolean editButton = true;
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/formComponent.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/app.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
	<title><%=commonLangMessage.get("form.text.backOffice.menu.50")%> | <%=commonLangMessage.get("form.text.backOffice.menu.71")%>
	</title>



	<!-- App -->
	<script type="text/javaScript" src="/js/util/JSUtil.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/util/ImageUtils.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/Head.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javaScript" src="/js/manager/Sidebar.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/holder.min.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>
	<script type="text/javascript" src="/js/manager/cms/Provider.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript">
		var $ = jQuery.noConflict();

		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.pageSize = <%=SystemConstants.PAGE_SIZE%>;
		PageConfig.editButton = <%=editButton%>;
		PageConfig.providerValidator;
		PageConfig.vendorValidator;

		PageConfig.noFileText = '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>';

		PageConfig.lang = "<%=commonLangMessage.getLang()%>";

		//set I18N
		I18N.setResource({
			'form.text.backOffice.datatable.save': '<%=commonLangMessage.get("form.text.backOffice.datatable.save")%>',
			'form.text.backOffice.datatable.columns': '<%=commonLangMessage.get("form.text.backOffice.datatable.columns")%>',
			'form.text.backOffice.datatable.success': '<%=commonLangMessage.get("form.text.backOffice.datatable.success")%>',
			'form.text.backOffice.breadcrumbs.home': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.home")%>',
			'form.text.backOffice.breadcrumbs.cms': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.cms")%>',
			'form.text.backOffice.breadcrumbs.provider': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.provider")%>',
			'form.text.backOffice.provider.editProvider': '<%=commonLangMessage.get("form.text.backOffice.provider.editProvider")%>',
			'form.text.backOffice.provider.editVendor': '<%=commonLangMessage.get("form.text.backOffice.provider.editVendor")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.maintenance': '<%=commonLangMessage.get("form.text.backOffice.status.maintenance")%>',
			'form.text.backOffice.status.invisible': '<%=commonLangMessage.get("form.text.backOffice.status.invisible")%>',
			'form.text.on': '<%=commonLangMessage.get("form.text.on")%>',
			'form.text.off': '<%=commonLangMessage.get("form.text.off")%>',
			'msg.manager.update.success': '<%=commonLangMessage.get("msg.manager.update.success")%>',
			'msg.error.info.image.sizeIsLarge': '<%=commonLangMessage.get("msg.error.info.image.sizeIsLarge")%>',
			'form.text.backOffice.file.noFileSelected': '<%=commonLangMessage.get("form.text.backOffice.file.noFileSelected")%>',
			'form.text.originalSize': '<%=commonLangMessage.get("form.text.originalSize")%>',
		});

		$(document).ready(function() {
			"use strict";
			App.init(); // Init layout and core plugins
			Plugins.init(); // Init all plugins
			FormComponents.init(); // Init all form-specific plugins

			ProviderHandler.init();
			ProviderHandler.bindEvent();
			// VendorHandler.init();
			HeadHandler.init();
			MenuHandler.init();




		});
	</script>

	<style>
      .input-container {
        position: relative;
        flex-grow: 1;
      }

      .input-container input {
        width: 100%;
        padding: 5px 30px 5px 5px; /* 留空间给 icon */
        font-size: 14px;
        border: 1px solid #ccc;
        border-radius: 4px;
        box-sizing: border-box;
      }

      .input-container i {
        position: absolute;
        top: 50%;
        right: 20px;
        transform: translateY(-50%);
        cursor: pointer;
        color: #555;
        font-size: 16px;
      }


	</style>
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
			<div class="col-md-12">
				<table id="providerTable" class="table table-nowrap table-bordered" width="100%">
					<thead>
					<tr>
						<th>#</th>
						<th><%=commonLangMessage.get("form.text.backOffice.provider.displayName")%>
						</th>
						<th><%=commonLangMessage.get("form.text.backOffice.status")%>
						</th>
					</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				<div class="modal fade" id="updateProviderData" role="dialog">
					<div class="modal-dialog">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal"
									>&times;
								</button>
								<h4 class="modal-title"><%=commonLangMessage.get("form.text.account.editStatus")%>
								</h4>
							</div>
							<div class="modal-body">
								<form class="form-horizontal" id="updateProviderForm" name="updateProviderForm"
								>
									<div class="form-group hide-default">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.games.display")%>
										</label>
										<input type="hidden" id="providerId" name="providerId">
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.status")%>
										</label>
										<div class="col-md-9">
											<select class="form-control" name="providerStatus">
												<% for (ProviderStatusType status : ProviderStatusType.VALUES_FOR_BO) { %>
												<option value="<%=status.unique()%>"><%=status.getName()%></option>
												<% } %>
											</select>
										</div>
									</div>
									<div class="form-group" id="providerDataRangeDiv" style="display:none;">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.provider.maintainDate")%><span
											class="required">*</span>
										</label>
										<div class="col-md-9">
											<input type="text" class="form-control w-100"
												id="maintenanceDaterange"
												name="maintenanceDaterange"/>
										</div>
									</div>
								</form>
							</div>
							<div class="modal-footer">
								<button class="btn btn-primary"
									onclick="ProviderHandler.resetUpdateProviderForm('updateProviderForm')"><%=commonLangMessage.get(
									"ui.text.reset")%>
								</button>
								<button class="btn btn-primary"
									onclick="ProviderHandler.updateProvider('updateProviderForm')"><%=commonLangMessage.get(
									"form.text.button.save")%>
								</button>
							</div>
						</div>
					</div>
				</div>


				<div class="modal fade" id="updateVendorData" role="dialog">
					<div class="modal-dialog">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal"
									onclick="VendorHandler.closeWindow()">&times;
								</button>
								<h4 class="modal-title"></h4>
							</div>
							<div class="modal-body">
								<form class="form-horizontal" id="updateVendorForm" name="updateVendorForm"
									novalidate="novalidate">
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.provider.displayName")%>
										</label>
										<div class="col-md-9">
											<input type="hidden" id="updateVendorProviderID">
											<input type="hidden" id="updateVendorID" name="updateVendorID">
											<input class="form-control" id="updateVendorName" name="updateVendorName"
												maxlength="15">
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.bonusTemplate.gameType")%>
										</label>
										<div class="col-md-9">
											<%
												for (GameType game : GameType.getSortList()) {
											%>
											<label class="checkbox-inline"><input type="checkBox" class="uniform"
												name="updateVendorGameType" value="<%=game.unique()%>"/>
												<%=game.getFullName(commonLangMessage)%>
											</label>
											<%
												}
											%>
											<label for="updateVendorGameType" class="has-error help-block"
												generated="true" style="display:none;"></label>
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.status")%>
										</label>
										<div class="col-md-9">
											<!-- 											<div class="make-switch has-switch" data-on="primary" data-off="default" id="updateVendorStatus" data-on-label="&nbsp;ACTIVE&nbsp;" data-off-label="&nbsp;INACTIVE&nbsp;"> -->
											<!-- 												<input type="checkbox" checked> -->
											<!-- 											</div> -->
											<!-- 											<input type="hidden" id="updateStatus" name="updateStatus"> -->
											<select class="form-control" id="updateVendorStatus"
												name="updateVendorStatus">
												<%
													for (VendorStatusType status : VendorStatusType.values()) {
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
									<div class="form-group" id="vendorDataRangeDiv" style="display:none;">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.provider.maintainDate")%>
										</label>
										<div class="col-md-4">
											<input type="text" class="form-control singleDateTimePicker"
												id="vendorMainDateStart" name="vendorMainDateStart"
												placeholder="<%=commonLangMessage.get("form.text.from")%>">
											<i class="glyphicon glyphicon-calendar field-icon calendar-icon"></i>
										</div>
										<div class="col-md-1">
										</div>
										<div class="col-md-4">
											<input type="text" class="form-control singleDateTimePicker"
												id="vendorMainDateEnd" name="vendorMainDateEnd"
												placeholder="<%=commonLangMessage.get("form.text.to")%>">
											<i class="glyphicon glyphicon-calendar field-icon calendar-icon"></i>
										</div>
									</div>
									<div class="form-group">
										<label class="col-md-3 control-label"><%=commonLangMessage.get(
											"form.text.backOffice.provider.slotCategories")%>
										</label>
										<div class="col-md-9">
											<select class="select2-select-00 full-width-fix" id="updateGameCategory"
												name="updateGameCategory" multiple>
											</select>
										</div>
									</div>
									<%
											String titleLangKey = commonLanguageResourceKey;
									%>
									<div class="form-group">
										<label class="col-md-3 control-label">
											<%=
												commonLangMessage.get("form.text.backOffice.bonusTemplate.title")
												%>
										</label>
										<div class="col-md-9">
											<input type="text" class="form-control"
												id="<%=titleLangKey%>DisplayTitle"
												name="<%=titleLangKey%>DisplayTitle"
												placeholder="<%=titleLangKey.toUpperCase()%>"/>
										</div>
									</div>

									<div class="form-group">
										<label class="col-md-3 control-label">
											<%=
												commonLangMessage.get("form.text.backOffice.bonusTemplate.description")
												%>
										</label>
										<div class="col-md-9">
											<textarea class="form-control"
												id="<%=titleLangKey%>DisplayDesc"
												name="<%=titleLangKey%>DisplayDesc"
												cols="5" rows="4"
												placeholder="<%=titleLangKey.toUpperCase()%>"></textarea>
										</div>
									</div>
									<%--									<div class="form-group">--%>

									<%--										<div class="col-md-9">--%>
									<%--											<input type="text" class="form-control" id="title" name="title" />--%>
									<%--										</div>--%>
									<%--									</div>--%>
									<%--									<div class="form-group">--%>
									<%--										<label class="col-md-3 control-label"><%=commonLangMessage.get("form.text.backOffice.bonusTemplate.description")%></label>--%>
									<%--										<div class="col-md-9">--%>
									<%--											<textarea class="form-control" id="description" name="description" cols="5" rows="4"></textarea>--%>
									<%--										</div>--%>
									<%--									</div>--%>
									<%
										for (GameType gameType : GameType.values()) {
											for (PlatformType platformType : PlatformType.values()) {
												//													if (platformType == PlatformType.APP) {
												//														continue;
												//													}
									%>
									<div class="form-group" style="display: none"
										id="<%=platformType.getName()%>_<%=gameType.getShortName()%>FileDiv">
										<label class="col-md-3 control-label">
											<%--											<%=platformType.getName()%>&nbsp;<%=gameType.getShortName()%> <%=BonusTemplateImageType.ICON.getFullName(commonLangMessage)%>--%>
										</label>
										<div class="col-md-9">
											<input type="file" data-style="fileinput"
												id="<%=platformType.getName()%>_<%=gameType.getShortName()%>File"
												name="<%=platformType.getName()%>_<%=gameType.getShortName()%>File"
												data-type="image">
										</div>
									</div>
									<%
											}
										}
									%>
								</form>
							</div>
							<div class="modal-footer">
								<button class="btn btn-primary"
									onclick="VendorHandler.cancelChange()"><%=commonLangMessage.get("ui.text.reset")%>
								</button>
								<button class="btn btn-primary"
									onclick="VendorHandler.updateVendor()"><%=commonLangMessage.get("ui.text.confirm")%>
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!--=== Page Content ===-->
		</div>
		<!-- /.container -->

	</div>
</div>
<table id="vendorTemplateTable" class="table table-nowrap table-bordered" width="100%" style="display: none">
	<thead>
	<tr>
		<th><%=commonLangMessage.get("form.text.backOffice.provider.seq")%>
		</th>
		<th><%=commonLangMessage.get("form.text.backOffice.provider.displayName")%>
		</th>
		<th><%=commonLangMessage.get("form.text.backOffice.bonusTemplate.gameType")%>
		</th>
		<th><%=commonLangMessage.get("form.text.backOffice.status")%>
		</th>
		<th id="action"><%=commonLangMessage.get("form.text.backOffice.action")%>
		</th>
	</tr>
	</thead>
	<tbody>
	</tbody>
</table>
<table style="display: none">
	<tr class="odd" id="vendorTemplateLayer" style="display: none">
		<td id="seqId"></td>
		<td id="vendorName"></td>
		<td id="vendorGameType"></td>
		<td id="vendorStatus"></td>
		<td id="action"></td>
	</tr>
</table>

<optgroup style="display:none;" id="categoryGroupTemplate"></optgroup>
<option style="display:none;" id="categoryTemplate"></option>
</body>
</html>