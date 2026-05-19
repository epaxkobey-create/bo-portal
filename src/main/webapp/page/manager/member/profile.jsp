<%@page import="com.nv.commons.bo.AccountBO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="com.nv.commons.constants.AccountStatusType" %>
<%@ page import="com.nv.commons.constants.AccountUpdateType" %>
<%@ page import="com.nv.commons.constants.ContactType" %>
<%@ page import="com.nv.commons.constants.CurrencyType" %>
<%@ page import="com.nv.commons.dto.Account" %>
<%@ page import="com.nv.commons.utils.JSONUtils" %>
<%@ page import="com.nv.commons.utils.RequestParser" %>
<%@ page import="com.nv.commons.constants.RemarkTemplateType" %>
<%@ page import="com.nv.commons.constants.KycDocumentStatusType" %>
<%@ page import="com.nv.commons.dto.Bank" %>
<%@ page import="java.util.List" %>
<%@ page import="com.nv.commons.bo.BankBO" %>
<%@ page import="java.util.Collections" %>
<%

	FrontendUtils.noCache(response);

	String userId = RequestParser.getStringParameter(request, 50, "userId");
	int currencyId = RequestParser.getIntParameter(request, "currency");
	CurrencyType currencyType = CurrencyType.getInstance(currencyId);

	Manager managerBO = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);

	Account memberForProfile = AccountBO.getAccountByUserId(userId, managerBO.getWebsiteTypeObj());
	List<Bank> bankList;
	try {
		bankList = BankBO.getBanks();
	} catch (Exception e) {
		bankList = Collections.emptyList();
	}

	int accountStatus = memberForProfile.getStatus();


%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="/page/manager/include/htmlHead.jsp" %>
	<%@include file="/page/manager/include/datatable.jsp" %>
	<%@include file="/page/manager/include/daterangepicker.jsp" %>
	<%@include file="/page/manager/include/common.jsp" %>

	<meta charset="utf-8">
	<meta name="viewport"
		content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0"/>

	<title><%=commonLangMessage.get("form.text.backOffice.menu.2")%> | <%=commonLangMessage.get(
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

	<script type="text/javascript" src="/js/manager/member/profile.js?v=<%=FrontendUtils.getJsFileVersion()%>"></script>

	<script type="text/javascript" src="/js/plugins/uniform/jquery.uniform.min.js"></script>

	<style>
      .btn-unified {
        line-height: 20px;
      }

      .password-container {
        position: relative;
        flex-grow: 1;
      }

      .password-container input {
        width: 100%;
        padding: 5px 30px 5px 5px; /* 留空间给 icon */
        font-size: 14px;
        border: 1px solid #ccc;
        box-sizing: border-box;
      }

      .password-container i {
        position: absolute;
        top: 50%;
        right: 20px;
        transform: translateY(-50%);
        cursor: pointer;
        color: #555;
        font-size: 16px;
      }

      .input-container input {
        width: 100%;
        padding: 5px 30px 5px 5px;
        border: 1px solid #ccc;
        box-sizing: border-box;
      }

      .input-container {
        position: relative;
        flex-grow: 1;
      }

      .input-container i {
        position: absolute;
        top: 50%;
        right: 10px;
        transform: translateY(-50%);
        cursor: pointer;
        color: #555;
        font-size: 16px;
      }

      .upload-box {
        width: 100%;
        height: 300px;
        border: 2px solid #aaa;
        background-color: #f9f9f9;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        margin-bottom: 20px;
        position: relative;
        /*display: inline-block;*/
      }

      .image-preview img {
        max-width: 100%;
        max-height: 100%;
        position: relative;
        text-align: center;
      }

      .image-preview-wrapper {
        position: relative;
        display: inline-block;
        border: 1px solid #ccc;
        border-radius: 6px;
        padding: 4px;
      }


      .clear-btn {
        position: absolute;
        top: -8px;
        right: -8px;
        width: 24px;
        height: 24px;
        background-color: #3366cc;
        color: #fff;
        border-radius: 50%;
        text-align: center;
        line-height: 24px;
        cursor: pointer;
        font-weight: bold;
        box-shadow: 0 0 4px rgba(0, 0, 0, 0.3);
      }

      #verificationRemark {
        display: inline-block;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      #userRemarkDisplay {
        display: inline-block;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      #streetDisplay {
        display: inline-block;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      #cityDisplay {
        display: inline-block;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .remark-cell {
        position: relative;
        overflow: visible;
        cursor: pointer;
      }

      .userRemark-cell {
        position: relative;
        overflow: visible;
        cursor: pointer;
      }

      .userRemark-cell:hover::after {
        content: attr(data-fulltext);
        position: absolute;
        left: 0;
        top: calc(100% + 4px);
        max-width: 480px;
        padding: 6px 10px;
        border: 1px solid #ccc;
        background: #fffacd;
        line-height: 1.4;
        white-space: pre-wrap;
        word-break: break-all;
        box-shadow: 0 2px 6px rgba(0, 0, 0, .15);
        z-index: 9999;
      }


      .remark-cell:hover::after {
        content: attr(data-fulltext);
        position: absolute;
        left: 0;
        top: calc(100% + 4px);
        max-width: 480px;
        padding: 6px 10px;
        border: 1px solid #ccc;
        background: #fffacd;
        line-height: 1.4;
        white-space: pre-wrap;
        word-break: break-all;
        box-shadow: 0 2px 6px rgba(0, 0, 0, .15);
        z-index: 9999;
      }

      .modal-xlg {
        height: 100%;
        width: 90%;
      }

      .password-requirements-box {
        position: absolute;
        left: 0;
        right: 0;
        padding: 15px;
        background: #fff;
        border: 1px solid #ddd;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        z-index: 1000;
      }

      .password-requirements-box h5 {
        margin: 0 0 12px 0;
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .password-requirements-box ul {
        list-style: none;
        padding: 0;
        margin: 0;
      }

      .password-requirements-box li {
        padding: 6px;
        font-size: 13px;
        color: #666;
        display: flex;
        align-items: center;
        line-height: 1.4;

      }

      .password-requirements-box li i {
        margin-right: 5px; /* 调整数值来控制间距 */
      }

	</style>
	<script type="text/javascript">
		if (typeof (PageConfig) == 'undefined') {
			PageConfig = {};
		}
		PageConfig.userId = '<%=userId%>';
		PageConfig.currency = '<%=currencyId%>';
		PageConfig.DateHourMinuteSecondPattern = '<%=FormatUtils.DATE_PATTERN_DASH_yyyyMMdd_HHmmss%>'

		PageConfig.AccountUpdateType = <%=AccountUpdateType.toJsonString()%>;

		PageConfig.UpdateType = {};
		PageConfig.UpdateType.Email = <%=AccountUpdateType.EMAIL.unique()%>;

		PageConfig.ContactType = <%=ContactType.toJsonString()%>;

		PageConfig.lang = "<%=commonLanguageResourceKey%>";
		PageConfig.currencyFullName = {};

		PageConfig.remark = <%=JSONUtils.toJsonString("{}")%>;
		PageConfig.remarkSetting = <%=JSONUtils.toJsonString("{}")%>;
		PageConfig.accessRight = {};
		PageConfig.remarkType = <%=RemarkTemplateType.RISK_REMARK.unique()%>;

		//set I18N
		I18N.setResource({
			'form.text.backOffice.breadcrumbs.profile': '<%=commonLangMessage.get("form.text.backOffice.breadcrumbs.profile")%>',
			'form.text.backOffice.status.inactive': '<%=commonLangMessage.get("form.text.backOffice.status.inactive")%>',
			'form.text.backOffice.status.active': '<%=commonLangMessage.get("form.text.backOffice.status.active")%>',
			'form.text.backOffice.status.suspend': '<%=commonLangMessage.get("form.text.backOffice.status.suspend")%>',
			'form.text.backOffice.status.locked': '<%=commonLangMessage.get("form.text.backOffice.status.locked")%>',
			'msg.error.validation.requirdForm': '<%=commonLangMessage.get("msg.error.validation.requirdForm")%>',
			'msg.info.profile.confirmVIPLevelUpgrade': '<%=commonLangMessage.get("msg.info.profile.confirmVIPLevelUpgrade")%>',
			'msg.info.profile.confirmVIPLevelDowngrade': '<%=commonLangMessage.get("msg.info.profile.confirmVIPLevelDowngrade")%>',
			'msg.error.account.phone.isNotValidated': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated") %>',
			'msg.error.account.phone.isNotValidated.1': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.1") %>',
			'msg.error.account.phone.isNotValidated.2': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.2") %>',
			'msg.error.account.phone.isNotValidated.3': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.3") %>',
			'msg.error.account.phone.isNotValidated.4': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.4") %>',
			'msg.error.account.phone.isNotValidated.5': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.5") %>',
			'msg.error.account.phone.isNotValidated.6': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.6") %>',
			'msg.error.account.phone.isNotValidated.7': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.7") %>',
			'msg.error.account.phone.isNotValidated.8': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.8") %>',
			'msg.error.account.phone.isNotValidated.9': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.9") %>',
			'msg.error.account.phone.isNotValidated.13': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.13") %>',
			'msg.error.account.phone.isNotValidated.15': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.15") %>',
			'msg.error.account.phone.isNotValidated.16': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.16") %>',
			'msg.error.account.phone.isNotValidated.18': '<%=commonLangMessage.get("msg.error.account.phone.isNotValidated.18") %>',
			'msg.error.password.isNotValidated.v2': '<%=commonLangMessage.get("msg.error.password.isNotValidated.v2") %>',
			'form.text.backOffice.updatedBy': '<%=commonLangMessage.get("form.text.backOffice.updatedBy") %>',
			'form.text.backOffice.verifiedBy': '<%=commonLangMessage.get("form.text.backOffice.verifiedBy") %>',
			'ui.text.confirm': '<%=commonLangMessage.get("ui.text.confirm")%>',
			'ui.text.cancel': '<%=commonLangMessage.get("ui.text.cancel")%>',
			'form.text.backOffice.remark': '<%=commonLangMessage.get("form.text.backOffice.remark")%>',
			'ui.text.deposit.select_remark': '<%=commonLangMessage.get("ui.text.deposit.select_remark")%>',
			'msg.remark.template.required': '<%=commonLangMessage.get("msg.remark.template.required")%>',
			'msg.remark.required': '<%=commonLangMessage.get("msg.remark.required")%>',

			'fe.text.profile.maritalType.single': '<%=commonLangMessage.get("fe.text.profile.maritalType.single") %>',
			'fe.text.profile.maritalType.married': '<%=commonLangMessage.get("fe.text.profile.maritalType.married") %>',

			'fe.text.profile.genderType.female': '<%=commonLangMessage.get("fe.text.profile.genderType.female") %>',
			'fe.text.profile.genderType.male': '<%=commonLangMessage.get("fe.text.profile.genderType.male") %>',
			'fe.text.profile.maritalType.widowed': '<%=commonLangMessage.get("fe.text.profile.maritalType.widowed")%>',
			'fe.text.profile.maritalType.divorced': '<%=commonLangMessage.get("fe.text.profile.maritalType.divorced")%>',

			'form.text.profile.providerAccount.delete': '<%=commonLangMessage.get("form.text.profile.providerAccount.delete")%>',
			'form.text.providerAccount.confirm_delete_?': '<%=commonLangMessage.get("form.text.providerAccount.confirm_delete_?")%>',
			'form.text.backOffice.message.confirmRemoveThe3Part': '<%=commonLangMessage.get("form.text.backOffice.message.confirmRemoveThe3Part")%>',
			'msg.error.validation.passwordNotMatch': '<%=commonLangMessage.get("msg.error.validation.passwordNotMatch")%>',

			'global.text.documentStatusType.APPROVED': '<%=commonLangMessage.get("global.text.documentStatusType.APPROVED")%>',

			'global.text.documentStatusType.FILL_IN': '<%=commonLangMessage.get("global.text.documentStatusType.FILL_IN")%>',
			'global.text.documentStatusType.ON_HOLD': '<%=commonLangMessage.get("global.text.documentStatusType.ON_HOLD")%>',
			'global.text.documentStatusType.OTP_PENDING': '<%=commonLangMessage.get("global.text.documentStatusType.OTP_PENDING")%>',
			'global.text.documentStatusType.PENDING': '<%=commonLangMessage.get("global.text.documentStatusType.PENDING")%>',
			'global.text.documentStatusType.REJECTED': '<%=commonLangMessage.get("global.text.documentStatusType.REJECTED")%>',
			'global.text.documentStatusType.REMOVED': '<%=commonLangMessage.get("global.text.documentStatusType.REMOVED")%>',

			'msg.info.profile.noDocumentImage': '<%=commonLangMessage.get("msg.info.profile.noDocumentImage")%>',

			'form.text.account.accountBank.remove_?': '<%=commonLangMessage.get("form.text.account.accountBank.remove_?")%>',
			'form.text.account.card.remove_?': '<%=commonLangMessage.get("form.text.account.card.remove_?")%>',
		});
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


			PageConfig.KycDocumentStatusType = <%=KycDocumentStatusType.toJsonString()%>;
			PageConfig.AccountStatusType = <%=AccountStatusType.toJsonString()%>;
			$(':radio.uniform, :checkbox.uniform').uniform();

			ProfileHandler.init();
		});
	</script>
	<link href="/css/manager/riskreport.css?v=<%=FrontendUtils.getJsFileVersion()%>" rel="stylesheet" type="text/css"/>
	<link rel="stylesheet" href="/css/fontawesome/font-awesome-ie7.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
	<link rel="stylesheet" href="/css/fontawesome/font-awesome.min.css?v=<%=FrontendUtils.getJsFileVersion()%>">
</head>
<body class="theme-dark">
<!-- Header -->
<%@include file="/page/manager/include/top.jsp" %>
<!-- /.header -->

<%-- container start --%>
<div id="container">

	<jsp:include page="/page/manager/include/sidebar2.jsp">
		<jsp:param name="functionTitle" value="Profile"/>
	</jsp:include>

	<%-- content start --%>
	<div id="content">


		<%-- container2 start --%>
		<div class="container">
			<%
				String functionTitle = "Profile";

			%>
			<%@include file="/page/manager/include/head2.jsp" %>


			<%--這些Modal應該不會被共用吧= = --%>


			<div class="modal fade" id="updateUserRemarkModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<div class="modal-content">

						<form class="form-horizontal" name="updateUserRemarkForm" id="updateUserRemarkForm"
							action="#">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" id="modalTitle">
									<%= commonLangMessage.get("form.text.account.editMember") %>
								</h4>
							</div>

							<div class="modal-body">
								<input type="hidden" name="userId" value="<%=userId%>">
								<input type="hidden" name="currencyType" value="<%=currencyId%>">

								<!-- 标题与 Collapse 按钮 -->


								<div class="row">
									<div class="col-md-12">
										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.account.miscellaneousInformation")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<span class="btn btn-xs widget-collapse"><i
															class="icon-angle-down"></i></span>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="form-group">
													<label class="col-md-3 control-label"><%=commonLangMessage.get(
														"form.text.account.remark")%>
													</label>
													<div class="col-md-7">
														<input type="text" id="userRemark" name="userRemark"
															maxlength="3000"
															class="form-control"
															placeholder="<%=commonLangMessage.get("form.text.account.remark")%>">
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%= commonLangMessage.get("ui.text.reset") %>"
									name="resetButton" onclick="ProfileHandler.resetUserRemark()"
									class="btn btn-primary">
								<input type="button" value="<%= commonLangMessage.get("form.text.button.save") %>"
									name="save" onclick="ProfileHandler.updateUserRemark()"
									class="btn btn-primary">
							</div>
						</form>

					</div>
				</div>
			</div>


			<div class="modal fade" id="updateStatusModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<form class="form-horizontal" name="updateStatusForm" action="#">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" id="title">
									<%=commonLangMessage.get("form.text.account.editStatus")%>
								</h4>
							</div>
							<div class="modal-body">
								<div class="form-group">
									<label class="col-md-3 control-label">
										<%=commonLangMessage.get("form.text.backOffice.status")%>
									</label>
									<div class="col-md-7">
										<select class="form-control" id="status" name="status">
											<% for (AccountStatusType status : AccountStatusType.getSortedAccountStatusTypes()) {%>
											<option value="<%=status.unique()%>">
												<%=status.getFullName(commonLangMessage)%>
											</option>

											<% } %>
										</select>
									</div>
									<div class="col-md-7">
										<input type='hidden' name='userId' value='<%=userId%>'>
									</div>
								</div>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
									name="resetButton" onclick='ProfileHandler.resetStatus(<%=accountStatus%>)'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
									name="save" onclick='ProfileHandler.updateStatus()' class="btn btn-primary">
							</div>
						</div>
					</form>
				</div>
			</div>


			<div class="modal fade" id="updatePasswordModal" role="dialog">
				<div class="modal-dialog modal-lg">

					<!-- Modal content-->
					<div class="modal-content">
						<form class="form-horizontal" id="updatePasswordForm" name="updatePasswordForm" action="#">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal"
									onclick="ProfileHandler.resetPassword()">&times;
								</button>
								<h4 class="modal-title" id="modalTitle"><%=commonLangMessage
									.get("form.text.account.editPassword")%>
								</h4>
							</div>
							<div class="modal-body">
								<div class="row">
									<div class="row"
										style="display: flex; gap: 20px; align-content: center; height: 200px">
										<div class="col-md-6">
											<div class="form-group">
												<label class="control-label">
													<%=commonLangMessage.get("form.text.backOffice.staff.password")%>
													<span
														class="required">*</span>
												</label>

												<div class="password-container">
													<input type="password" id="editPassword" name="password"
														class="form-control"
														autocomplete="off"
														placeholder="<%=commonLangMessage.get("form.text.backOffice.staff.password")%>"
													>
													<i class="icon-eye-close" id="togglePassword"
														onclick="ProfileHandler.toggleVisibility('editPassword', this)"></i>
												</div>
												<div id="passwordRequirements" class="password-requirements-box"
													style="display: none"
												>
													<h5>Password Requirements</h5>
													<ul>
														<li id="req-length">
															<i class="icon-circle-blank"></i>
															<span>Must be between 6 to 20 characters.</span>
														</li>
														<li id="req-uppercase">
															<i class="icon-circle-blank"></i>
															<span>Must contain 1 uppercase (A-Z).</span>
														</li>
														<li id="req-lowercase">
															<i class="icon-circle-blank"></i>
															<span>Must contain 1 lowercase (a-z).</span>
														</li>
														<li id="req-digit">
															<i class="icon-circle-blank"></i>
															<span>Must contain 1 digit (0-9).</span>
														</li>
														<li id="req-symbol">
															<i class="icon-circle-blank"></i>
															<span>Must contain 1 symbol (@$!%*#).</span>
														</li>
													</ul>
												</div>
												<label class="error-msg-block"></label>

											</div>
										</div>
										<div class="col-md-6">
											<div class="form-group">
												<label class=" control-label">
													<%=commonLangMessage.get(
														"form.text.backOffice.staff.confirmPassword")%>
													<span
														class="required">*</span>
												</label>

												<div class="password-container">
													<input type="password" id="confirmPassword"
														name="confirmPassword"
														class="form-control"
														autocomplete="off"
														placeholder="<%=commonLangMessage.get("form.text.backOffice.staff.confirmPassword")%>"
													/>
													<i class="icon-eye-close" id="toggleConfirmPassword"
														onclick="ProfileHandler.toggleVisibility('confirmPassword', this)"></i>
												</div>
												<label class="error-msg-block"></label>

											</div>
										</div>

									</div>
								</div>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
									name="resetButton" onclick='ProfileHandler.resetPassword()'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
									name="save" onclick='ProfileHandler.updatePasswordValidation()'
									class="btn btn-primary">
							</div>
						</form>
					</div>

				</div>
			</div>

			<jsp:include page="_documentPhoto.jsp">
				<jsp:param name="documentPhotoTitle"
					value="<%=commonLangMessage.get(\"form.text.documentType.document\")%>"/>
			</jsp:include>

			<%-- profileContainer start --%>
			<div id='profileContainer'>
				<%-- General start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->
								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.general")%>
								</h4>

								<!-- 右侧按钮组 -->
								<div class="toolbar no-padding">
									<button type="button"
										name="gotoEditGeneral"
										onclick="ProfileHandler.goToEditUserRemark()"
										class="btn btn-primary btn-sm"
										style="line-height: 15px"
									>
										<%=commonLangMessage.get("ui.text.profile.edit")%>
									</button>

									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>


							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<tbody>
									<tr id='<%=ContactType.Email.getAccountUpdateType()%>flag'>
										<th><%=commonLangMessage.get(ContactType.Email.getDisplayName(),
											new String[] {""})%>
										</th>
										<td>
											<span id='<%=ContactType.Email.getName()%>'></span>
										</td>
										<td colspan="2">
											<input type="button"
												value="<%=commonLangMessage.get("ui.text.profile.verify")%>"
												onclick='ProfileHandler.gotoVerifyContact(<%=ContactType.Email.unique()%>, 1)'
												class="btn btn-primary btn-xs btn-unified"
												id="<%=ContactType.Email.unique() + "-1_verifyButton"%>"
												style="display: none; float: right; margin-right: 5px">
										</td>

									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.password")%>
										</th>
										<td>
											<span id="password">••••••</span>
										</td>

										<td colspan="2">
											<input type="button"
												value="<%=commonLangMessage.get("ui.text.profile.edit")%>"
												name="gotoEditGeneral" onclick="ProfileHandler.updatePassword()"
												class="btn btn-primary btn-xs btn-unified"
												style="float: right; margin-right: 15px"
											>
										</td>

									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.currency")%>
										</th>
										<td><%=CurrencyType.getInstance(currencyId).getFullName(commonLangMessage)%>
										</td>
										<td></td>
										<td></td>
									</tr>

									<tr id='<%=AccountUpdateType.STATUS%>flag'>
										<th><%=commonLangMessage.get("form.text.backOffice.status")%>
										</th>
										<td>
											<span class="label" id='status'></span>
										</td>
										<td colspan="2">
											<input type="button"
												value="<%=commonLangMessage.get("ui.text.profile.edit")%>"
												name="gotoEditGeneral" onclick='ProfileHandler.getEditStatus()'
												class="btn btn-primary btn-xs btn-unified"
												style="float: right; margin-right: 15px"
											>
										</td>


									</tr>
									<tr id='userRemarkflag'>
										<th><%=commonLangMessage.get("form.text.account.remark")%>
										</th>
										<td class="userRemark-cell" data-fulltext="">
											<span id='userRemarkDisplay'></span>
										</td>
										<td></td>
										<td></td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<%-- General end--%>
				<%-- Document start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->
								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.breadcrumbs.document")%>
								</h4>
								<!-- 右侧按钮组 -->
								<div class="toolbar no-padding">
									<button type="button"
										name="gotoEditGeneral"
										onclick="ProfileHandler.goToEditDocument()"
										class="btn btn-primary btn-sm"
										style="line-height: 15px">
										<%=commonLangMessage.get("ui.text.profile.edit")%>
									</button>
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>

							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<input type="hidden" name="documentId" value="">
									<tbody>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.documentType")%>
										</th>
										<td id='documentType'></td>
										<td></td>
										<td></td>
									</tr>
									<tr id='form.text.backOffice.documentNo'>
										<th><%=commonLangMessage.get("form.text.backOffice.documentNo")%>
										</th>
										<td style="display: flex; justify-content: space-between; align-items: center;">
											<span id="documentNo"></span>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.report.fraudTool.userName")%>
										</th>
										<td style="display: flex; justify-content: space-between; align-items: center;">
											<span id="fullName"></span>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.dob")%>
										</th>
										<td id="dob"></td>
										<td></td>
										<td></td>
									</tr>

									<tr>
										<th><%=commonLangMessage.get("form.text.af.kyc.expiryDate")%>
										</th>
										<td id="expiryDate"></td>
										<td></td>
										<td></td>

									</tr>

									<tr id='STREETflag'>
										<th><%=commonLangMessage.get("form.text.account.address.line1")%>

										</th>

										<td class="remark-cell" data-fulltext="">
											<span id='streetDisplay'></span>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr id='CITYflag'>
										<th><%=commonLangMessage.get("form.text.account.address.city")%>
										</th>
										<td class="remark-cell" data-fulltext="">
											<span id='cityDisplay'></span>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr id='POSTAL_CODEflag'>
										<th><%=commonLangMessage.get("form.text.account.address.postalCode")%>
										</th>
										<td id='postalCode' style="overflow-wrap: anywhere;"></td>
										<td></td>
										<td></td>
									</tr>
									<tr id='<%=commonLangMessage.get("form.text.af.kyc.frontPhoto")%>flag'>
										<th><%=commonLangMessage.get("form.text.af.kyc.frontPhoto")%>
										</th>
										<td>
											<button id="frontPhoto" class="btn btn-xs"
												onclick="ProfileHandler.viewSumsubDocument(this, 'front')">
												<i class="icon-search">
												</i>
											</button>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr id=<%=commonLangMessage.get("form.text.af.kyc.backPhoto")%>>
										<th><%=commonLangMessage.get("form.text.af.kyc.backPhoto")%>
										</th>
										<td>
											<button id="backPhoto" class="btn btn-xs"
												onclick="ProfileHandler.viewSumsubDocument(this, 'back')">
												<i class="icon-search">
												</i>
											</button>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr id='<%=commonLangMessage.get("form.text.af.kyc.documentAddress")%>flag'>
										<th><%=commonLangMessage.get("form.text.af.kyc.documentAddress")%>
										</th>
										<td>
											<button id="documentAddress" class="btn btn-xs"
												onclick="ProfileHandler.viewSumsubDocument(this, 'residence')">
												<i class="icon-search">
												</i>
											</button>
										</td>
										<td></td>
										<td></td>
									</tr>
									<tr id='<%=commonLangMessage.get("form.text.af.kyc.verificationStatus")%>flag'>
										<th><%=commonLangMessage.get("form.text.af.kyc.verificationStatus")%>
										</th>
										<td>
											<span class="label" id='verificationStatus'></span>


										</td>
										<td colspan="2">
											<input type="button"
												value="<%=commonLangMessage.get("ui.text.profile.edit")%>"
												name="gotoEditKycStatus"
												onclick='ProfileHandler.openUpdateKycStatusModal()'
												class="btn btn-primary btn-xs btn-unified"
												style="float: right; margin-right: 15px"
											>
										</td>
									</tr>
									<tr id='verificationRemarkflag'>
										<th><%=commonLangMessage.get("form.text.af.kyc.verificationRemark")%>
										</th>

										<!-- 直接對 <td> 放 data-fulltext，裡面再包一個 span 來做截斷 -->
										<td class="remark-cell" data-fulltext="">
											<span id='verificationRemark'></span>
										</td>

										<td></td>
										<td></td>
									</tr>
									</tbody>

								</table>
							</div>
						</div>
					</div>
				</div>

				<%-- Document end--%>
				<%-- Edit Document start--%>
				<div class="modal fade" id="updateDocumentModal" role="dialog">
					<div class="modal-dialog modal-xlg">
						<div class="modal-content">
							<form class="form-horizontal" name="updateDocumentForm" id="updateDocumentForm"
								enctype="multipart/form-data"
								action="#">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal">&times;</button>
									<h4 class="modal-title" id="editDocumentTitle">
										<%= commonLangMessage.get("form.text.af.kyc.editDocument") %>
									</h4>
								</div>

								<div class="modal-body">
									<input type="hidden" name="userId" value="<%=userId%>">
									<input type="hidden" name="currencyTypeId" value="<%=currencyType.unique()%>">

									<!-- 标题与 Collapse 按钮 -->
									<div class="row">
										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.backOffice.general")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<span class="btn btn-xs widget-collapse"><i
															class="icon-angle-down"></i></span>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="row" style="display: flex; gap: 20px; ">
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.backOffice.documentNo")%><span
																class="required">*</span></label>
															<input type="text" id="editDocumentNo" name="documentNo"
																maxlength="100"
																class="form-control">
														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.report.member.userName")%><span
																class="required">*</span></label>

															<input type="text" id="editFullName" name="fullName"
																maxlength="100"
																class="form-control">
														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.account.dob")%><span
																class="required">*</span></label>
															<div class='input-container'>
																<input type="text"
																	class="form-control singleDatePicker"
																	id="dob"
																	name='dob'
																	placeholder="<%=commonLangMessage.get("form.text.account.dob")%>">
																<i class="icon-calendar"></i>

															</div>
															<label class="error-msg-calander-block"></label>
														</div>
													</div>
												</div>
												<div class="row" style="display: flex; gap: 20px; ">
													<div class="col-md-4">
														<div class="form-group" style="margin-right: 10px">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.af.kyc.expiryDate")%><span
																class="required">*</span></label>
															<div class='input-container'>
																<input type="text" class="form-control singleDatePicker"
																	id="expiryDate"
																	name='expiryDate'
																	placeholder="<%=commonLangMessage.get("form.text.af.kyc.expiryDate")%>">
																<i class="icon-calendar"></i>
															</div>
															<label class="error-msg-calander-block"></label>
														</div>
													</div>
												</div>
											</div>
										</div>
									</div>
									<div class="row">

										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.account.addressInformation")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<span class="btn btn-xs widget-collapse"><i
															class="icon-angle-down"></i></span>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="row" style="display: flex; gap: 20px; ">
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.account.address.line1")%><span
																class="required">*</span></label>

															<input type="text" id="street" name="street"
																maxlength="500"
																class="form-control">

														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.account.address.city")%><span
																class="required">*</span></label>

															<input type="text" id="city" name="city"
																maxlength="50"
																class="form-control">
														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.account.address.postalCode")%><span
																class="required">*</span></label>

															<input type="text" id="postalCode" name="postalCode"
																maxlength="20"
																class="form-control">
														</div>
													</div>
												</div>
											</div>
										</div>
									</div>

									<div class="row">

										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.af.kyc.documentPhoto")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<span class="btn btn-xs widget-collapse"><i
															class="icon-angle-down"></i></span>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="row" style="display: flex; gap: 20px; ">
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label"><%=commonLangMessage.get(
																"form.text.af.kyc.frontSide")%><span
																class="required">*</span></label>

															<div class="upload-box"
																onclick="document.getElementById('frontPhotoFile').click()"
																ondragover="event.preventDefault()"
																ondrop="ProfileHandler.handleDrop(event, 'frontPhotoFile', 'frontPreview')">
																<div id="frontPreview" class="image-preview">

																	<div style="text-align: center">
																		<i class="icon-picture"
																			style="font-size: 50px;"></i>
																		<p style="font-weight: bold">Drop image here or
																			click to upload</p>
																		<br/>
																		<p>File Type: JPG, PNG, HEIC, WEBP or PDF</p>
																		<p>Max:50MB</p>
																	</div>
																</div>
																<input type="file" id="frontPhotoFile"
																	name="frontPhotoFile"
																	accept=".jpg,.jpeg,.png,.heic,.webp,.pdf"
																	onchange="ProfileHandler.previewImage(this, 'frontPreview')"
																	style="display: none;">
															</div>
															<label
																class="error-msg-pictureUpload-block-frontPhotoFile"></label>
														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label">
																<%=commonLangMessage.get("form.text.af.kyc.backSide")%>
															</label>

															<div class="upload-box"
																onclick="document.getElementById('backPhotoFile').click()"
																ondragover="event.preventDefault()"
																ondrop="ProfileHandler.handleDrop(event, 'backPhotoFile', 'backPreview')">

																<div id="backPreview" class="image-preview">
																	<div style="text-align: center">
																		<i class="icon-picture"
																			style="font-size: 50px;"></i>
																		<p style="font-weight: bold">Drop image here or
																			click to upload</p>
																		<br/>
																		<p>File Type: JPG, PNG, HEIC, WEBP or PDF</p>
																		<p>Max:50MB</p>
																	</div>
																</div>

																<input type="file" id="backPhotoFile"
																	name="backPhotoFile"
																	accept=".jpg,.jpeg,.png,.heic,.webp,.pdf"
																	onchange="ProfileHandler.previewImage(this, 'backPreview')"
																	style="display: none;">
															</div>
															<label
																class="error-msg-pictureUpload-block-backPhotoFile"></label>

														</div>
													</div>
													<div class="col-md-4">
														<div class="form-group">
															<label class="control-label">
																<%=commonLangMessage.get(
																	"form.text.backOffice.address")%><span
																class="required">*</span>
															</label>

															<div class="upload-box"
																onclick="document.getElementById('addressPhotoFile').click()"
																ondragover="event.preventDefault()"
																ondrop="ProfileHandler.handleDrop(event, 'addressPhotoFile', 'addressPreview')">

																<div id="addressPreview" class="image-preview">
																	<div style="text-align: center">
																		<i class="icon-picture"
																			style="font-size: 50px;"></i>
																		<p style="font-weight: bold">Drop image here or
																			click to upload</p>
																		<br/>
																		<p>File Type: JPG, PNG, HEIC, WEBP or PDF</p>
																		<p>Max:50MB</p>
																	</div>
																</div>

																<input
																	type="file" id="addressPhotoFile"
																	name="addressPhotoFile"
																	accept=".jpg,.jpeg,.png,.heic,.webp,.pdf"
																	onchange="ProfileHandler.previewImage(this, 'addressPreview')"
																	style="display: none;">
															</div>
															<label
																class="error-msg-pictureUpload-block-addressPhotoFile"></label>
														</div>
													</div>
												</div>
											</div>
										</div>
									</div>
									<div class="row">
										<div class="widget box">
											<div class="widget-header">
												<h4>
													<i class="icon-reorder"></i> <%=commonLangMessage.get(
													"form.text.account.miscellaneousInformation")%>
												</h4>
												<div class="toolbar no-padding">
													<div class="btn-group">
														<span class="btn btn-xs widget-collapse"><i
															class="icon-angle-down"></i></span>
													</div>
												</div>
											</div>
											<div class="widget-content">
												<div class="form-group">
													<label class="col-md-3 control-label"><%=commonLangMessage.get(
														"form.text.af.kyc.verificationRemark")%>
													</label>
													<div class="col-md-7">
														<input type="text" id="verificationRemarkInput"
															name="verificationRemark"
															maxlength="3000"
															class="form-control">
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
								<div class="modal-footer">
									<input type="button" value="<%= commonLangMessage.get("ui.text.reset") %>"
										name="resetButton" onclick="ProfileHandler.resetEditDocument()"
										class="btn btn-primary">
									<input type="button" value="<%= commonLangMessage.get("form.text.button.save") %>"
										name="save" onclick="ProfileHandler.updateDocument()"
										class="btn btn-primary">
								</div>
							</form>

						</div>
					</div>
				</div>
				<%-- Edit Document end--%>

				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->

								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.account.card.creditCard")%>
								</h4>


								<div class="toolbar no-padding">
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>
							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<thead>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.card.bank")%>
										</th>
										<th><%=commonLangMessage.get("form.text.account.card.cardBrand")%>
										</th>
										<th><%=commonLangMessage.get("form.text.account.card.cardNumber")%>
										</th>
										<th><%=commonLangMessage.get("form.text.account.card.expiryDate")%>
										</th>
										<th><%=commonLangMessage.get("form.text.account.card.cardholderName")%>
										</th>
										<th><%=commonLangMessage.get("form.text.backOffice.action")%>
										</th>
									</tr>
									</thead>
									<tbody id="accountCardContainer">
									</tbody>
									<tbody id="accountCardTemplate" style="display:none">
									<tr>
										<td id="cardBank"></td>
										<td id="cardBrand"></td>
										<td id="cardNumber"></td>
										<td id="cardExpiryDate"></td>
										<td id="cardholderName"></td>
										<td id="cardAction">
											<button id="btnRemoveAccountCard" class="btn">
												<i class="icon-trash"></i>
											</button>
										</td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<%-- Account Card end--%>

				<%-- Action start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->
								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.action")%>
								</h4>


								<div class="toolbar no-padding">
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>

							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<tbody>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.createDate")%>
										</th>
										<td id='createTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.signUpIp")%>
										</th>
										<td id='signUpIp'></td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.staff.lastLoginTime")%>
										</th>
										<td id='loginTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.account.loginIp")%>
										</th>
										<td id='loginIp'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.firstDepositTime")%>
										</th>
										<td id='firstDepositTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.lastDepositTime")%>
										</th>
										<td id='lastDepositTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.firstWithdrawalTime")%>
										</th>
										<td id='firstWithdrawalTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.lastWithdrawalTime")%>
										</th>
										<td id='lastWithdrawalTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.firstAdjustmentTime")%>
										</th>
										<td id='firstAdjustmentTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.lastAdjustmentTime")%>
										</th>
										<td id='lastAdjustmentTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.firstBetDate")%>
										</th>
										<td id='firstBetTime'></td>
										<td></td>
										<td></td>
									</tr>
									<tr style="display: none">
										<th><%=commonLangMessage.get("form.text.account.lastBetDate")%>
										</th>
										<td id='lastBetTime'></td>
										<td></td>
										<td></td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<%-- Action end--%>
				<%-- Summary start--%>
				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">
								<!-- 左侧标题 -->
								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.account.summary")%>
								</h4>

								<div class="toolbar no-padding">
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>
							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<thead>
									<tr>
										<th></th>
										<%-- 											<th><%=commonLangMessage.get("ui.text.backOffice.dashboard.count")%></th> --%>
										<th><%=commonLangMessage.get("ui.text.backOffice.dashboard.amount")%>
										</th>
										<th></th>
									</tr>
									</thead>
									<tbody>
									<tr>
										<th><%=commonLangMessage.get("global.text.moneyTransactionType.ADJUSTMENT")%>
										</th>
										<!-- 											<td id='adjustmentCount'></td> -->
										<td id='adjustmentAmount'></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("global.text.deposit")%>
										</th>
										<!-- 											<td id='depositCount'></td> -->
										<td id='depositAmount'></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("global.text.moneyTransactionType.WITHDRAWALS")%>
										</th>
										<!-- 											<td id='withdrawalCount'></td> -->
										<td id='withdrawalAmount'></td>
										<td></td>
									</tr>
									<%--<tr>
										<th><%=commonLangMessage.get("global.text.netDeposit")%>
										</th>
										<!-- 											<td></td> -->
										<td id='netDepositAmount'></td>
										<td></td>
									</tr>--%>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.turnover")%>
										</th>
										<!-- 											<td></td> -->
										<td id='turnover'></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("form.text.backOffice.dashboard.profit")%>
										</th>
										<!-- 											<td></td> -->
										<td id='profitLoss'></td>
										<td></td>
									</tr>

									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<%-- Summary end--%>
				<%-- Balance start--%>

				<div class="row">
					<div class="col-md-12">
						<div class="widget box">
							<div class="widget-header">

								<!-- 左侧标题 -->
								<h4>
									<i class="icon-reorder"></i> <%=commonLangMessage.get(
									"form.text.backOffice.general")%>
								</h4>
								<div class="toolbar no-padding">
									<div class="btn-group">
											<span class="btn btn-xs widget-collapse">
												<i class="icon-angle-down"></i>
											</span>
									</div>
								</div>
							</div>
							<div class="widget-content">
								<table class="table table-hover">
									<tbody>
									<tr>
										<th><%=commonLangMessage.get("form.text.profile.main")%>
										</th>
										<td id='mainBalance'></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
									</tr>
									<tr>
										<th><%=commonLangMessage.get("ui.text.provider")%>
										</th>
										<th><%=commonLangMessage.get("form.text.account.userId")%>
										</th>
										<th><%=commonLangMessage.get("form.text.backOffice.affiliate.registeredTime")%>
										</th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
										<th></th>
									</tr>
									</tbody>
									<tbody id='accountProviderContainer'>
									</tbody>
									<tbody id='accountProviderTemplate' style='display:none'>
									<tr>
										<th id='providerName'></th>
										<td id='providerAccount'></td>
										<td id='providerCreateTime'></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<%-- Balance end--%>
			</div>

			<div class="modal fade" id="updateKycStatusModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<form class="form-horizontal" name="updateKycStatusForm" action="#">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title" id="title">
									<%=commonLangMessage.get("form.text.account.editVerificationStatus")%>
								</h4>
							</div>
							<div class="modal-body">
								<div class="form-group">
									<label class="col-md-3 control-label">
										<%=commonLangMessage.get("form.text.backOffice.status")%>
									</label>
									<div class="col-md-7">
										<select class="form-control" id="kycStatus" name="kycStatus">
												<% for (KycDocumentStatusType status : KycDocumentStatusType.getSortedValues()) {%>
											<option value="<%=status.unique()%>">
												<%=status.getName()%>
											</option>
												<% } %>
									</div>
									<div class="col-md-7">
										<input type='hidden' name='kycUserId' value="<%=userId%>">
										<input type='hidden' name='currencyTypeId' value="<%=currencyType.unique()%>">
									</div>
								</div>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
									name="resetButton" onclick='ProfileHandler.resetKycStatus()'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
									name="save" onclick='ProfileHandler.updateKycStatus()' class="btn btn-primary">
							</div>
						</div>
					</form>
				</div>
			</div>

			<div class="modal fade" id="removeAccountBankModal" role="dialog">
				<div class="modal-dialog modal-lg">

					<form class="form-horizontal" name="removeAccountBankModalForm" action="#">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header" style="display: none;">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
							</div>
							<div class="modal-body" style="justify-items: center;">
								<h4 class="modal-title" id="title"></h4>
							</div>
							<div class="modal-footer">
								<input type="hidden" name="bankId">
								<input type="button" value="<%=commonLangMessage.get("ui.text.confirm")%>"
									name="confirmRemoveAccountBank" onclick='ProfileHandler.removeAccountBank()'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("ui.text.cancel")%>"
									name="cancelRemoveAccountCard" class="btn btn-danger" data-dismiss="modal">
							</div>
						</div>
					</form>
				</div>
			</div>


			<%-- profileContainer end --%>
			<div class="modal fade" id="createBankModal" role="dialog">
				<div class="modal-dialog-lg">
					<form class="form-horizontal" id="createBankModalForm" name="createBankModalForm" action="#">
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
								<h4 class="modal-title">
									<%=commonLangMessage.get("form.text.account.card.createBank")%>
								</h4>
							</div>
							<div class="modal-body">
								<input type="hidden" name="userId" value="<%=userId%>">
								<div class="row" style="display: flex; gap: 20px; ">
									<div class="col-md-6">
										<div class="form-group">
											<label>
												<%=commonLangMessage.get("form.text.account.card.bank")%>
												<span
													class="required">*</span>
											</label>
											<select class="form-control" id="bankId" name="bankId" required>
												<% for (Bank bank : bankList) { %>
												<option value="<%= bank.getId() %>"><%= bank.getBankName() %>
												</option>
												<% } %>
											</select>


										</div>
									</div>
									<div class="col-md-6">
										<div class="form-group">
											<label class="control-label">
												<%=commonLangMessage.get("form.text.bank.accountNumber")%>
												<span
													class="required">*</span>
											</label>
											<input type="text" class="form-control" id="accountNumber"
												name="accountNumber"
											<%--												maxlength="32"--%>
												oninput="ProfileHandler.onInputAccountNumberFormat(event)"

											>
										</div>
									</div>
								</div>

							</div>
							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
									name="resetButton" onclick="ProfileHandler.reset()" class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
									name="save" onclick="ProfileHandler.save()" class="btn btn-primary">
							</div>
						</div>
					</form>
				</div>
			</div>
			<div class="modal fade" id="removeAccountCardModal" role="dialog">
				<div class="modal-dialog modal-lg">
					<form class="form-horizontal" name="removeAccountCardModalForm" action="#">
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header" style="display: none;">
								<button type="button" class="close" data-dismiss="modal">&times;</button>
							</div>
							<div class="modal-body" style="justify-items: center;">
								<h4 class="modal-title" id="title"></h4>
							</div>
							<div class="modal-footer">
								<input type="button" value="<%=commonLangMessage.get("ui.text.confirm")%>"
									name="confirmRemoveAccountCard" onclick='ProfileHandler.removeAccountCard()'
									class="btn btn-primary">
								<input type="button" value="<%=commonLangMessage.get("ui.text.cancel")%>"
									name="cancelRemoveAccountCard" class="btn btn-danger" data-dismiss="modal">
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
		<%-- container2 end --%>

	</div>
	<%-- content end --%>

</div>
<%-- container end --%>

</body>
</html>
