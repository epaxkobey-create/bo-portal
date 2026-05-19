<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.nv.commons.constants.KycDocumentStatusType" %>
<%@ page import="com.nv.commons.constants.ReportExportType" %>
<%@page import="com.nv.commons.constants.AccountStatusType" %>

<%

	boolean enableExportExcelAccount = true;

	boolean enableCreateAccount = true;

	boolean enableUpdateStatus = true;
	List<String> titleList = ReportExportType.MEMBER.getTitleList(null);

%>

<style>
  .input-container {
    position: relative;

  }

  .input-container input {
    width: 100%;
    padding: 5px 30px 5px 5px; /* 留空间给 icon */
    font-size: 14px;
    border: 1px solid #ccc;
    /*border-radius: 4px;*/
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

  .modal-xlg {
    width: 70%;
  }

  .input-container {
    position: relative;
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
<div class="tab-pane active" id="box_tab0">
	<div class="row">
		<div class="col-md-12">

			<form class="form-vertical" id="searchForm" name='searchForm' action="#">
				<div class="form-group">
					<div class="row">
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage.get("form.text.account.email")%>
							</label>
							<input type="text" name="email" class="form-control"
								placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
						</div>
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage.get("form.text.account.name")%>
							</label>
							<input type="text" name="fullName" class="form-control"
								placeholder="<%=commonLangMessage.get("form.text.account.name")%>">
						</div>
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage
								.get("ui.text.raf.report.status")%>
							</label>
							<select class="form-control" id="status" name="status">
								<option value="-1"><%=commonLangMessage.get("ui.text.report.all")%>
								</option>
								<% for (AccountStatusType accountStatusType : AccountStatusType.getSortedAccountStatusTypes()) { %>
								<option value="<%=accountStatusType.unique() %>">
									<%=commonLangMessage.get(
										"form.text.backOffice.status." + accountStatusType
											.name().toLowerCase()) %>
								</option>
								<% } %>
							</select>
						</div>

					</div>
				</div>

				<div class="form-group">
					<div class="row">
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage
								.get("form.text.account.verification")%>
							</label>
							<select class="form-control" id="verificationStatus" name="verification">
								<option value="-2"><%=commonLangMessage.get("ui.text.report.all")%>
								</option>
								<% for (KycDocumentStatusType kycDocumentStatusType : KycDocumentStatusType.getSortedValues()) { %>
								<option value="<%=kycDocumentStatusType.unique() %>">
									<%=commonLangMessage.get(
										kycDocumentStatusType.getName()) %>
								</option>
								<% } %>
							</select>
						</div>
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage.get(
								"form.text.report.fraudTool.loginIp")%>
							</label>
							<input type="text" name="lastLoginIp" class="form-control"
								placeholder="<%=commonLangMessage.get("form.text.report.fraudTool.loginIp")%>">
						</div>

						<div class="col-md-4">
							<label class="control-label">
								<%=commonLangMessage.get("form.text.member.search.lastLoginSince")%>
							</label>
							<div class="input-container">
								<input type="text" name="lastLoginSince" id="lastLoginSince"
									class="form-control singleDateTimePicker"
									placeholder="<%=commonLangMessage.get("form.text.member.search.lastLoginSince")%>">
								<i class="icon-calendar"></i>
							</div>

						</div>
					</div>
				</div>
				<div class="form-group">
					<div class="row">

						<div class="col-md-4">
							<label class="control-label">
								<%=commonLangMessage.get("form.text.member.search.registeredSince")%>
							</label>
						</div>
						<div class="col-md-4" style="display: none">
							<label class="control-label"><%=commonLangMessage
								.get("form.text.member.search.lastDepositSince")%>
							</label>
						</div>
						<div class="col-md-4" style="display: none">
							<label class="control-label"><%=commonLangMessage
								.get("form.text.member.search.lastWithdrawalSince")%>
							</label>

						</div>
					</div>
					<div class="row next-row">
						<div class="col-md-4">
							<div class="input-container">
								<input type="text" class="form-control singleDateTimePicker"
									name="lastRegister"
									placeholder="<%=commonLangMessage.get("form.text.member.search.registeredSince")%>">
								<i class="icon-calendar"></i>
							</div>
						</div>
						<div class="col-md-4" style="display: none">
							<div class="input-container">
								<div class="input-container">
									<input type="text" class="form-control singleDateTimePicker"
										name='lastDepositSince'
										placeholder="<%=commonLangMessage.get("form.text.member.search.lastDepositSince")%>">
									<i class="icon-calendar"></i>
								</div>
							</div>
						</div>
						<div class="col-md-4" style="display: none">
							<div class="input-container">
								<input type="text" class="form-control singleDateTimePicker"
									name='lastWithdrawSince'
									placeholder="<%=commonLangMessage.get("form.text.member.search.lastWithdrawalSince")%>">
								<i class="icon-calendar"></i>
							</div>
						</div>
					</div>
				</div>
				<div class="form-group" style="display: none">
					<div class="row">
						<div class="col-md-4">
							<label class="control-label"><%=commonLangMessage
								.get("form.text.member.search.lastBetSince")%>
							</label>

						</div>
					</div>
					<div class="row next-row">
						<div class="col-md-4">
							<div class='input-container'>
								<input type="text" class="form-control singleDateTimePicker"
									name='lastBetTimeSince'
									placeholder="<%=commonLangMessage.get("form.text.member.search.lastBetSince")%>"
								>
								<i class="icon-calendar"></i>
							</div>
						</div>
					</div>
				</div>

				<div class="form-group">
					<div class="row">
						<div class="col-md-12">
							<input type="hidden" id="defaultConditionFlag" name="defaultConditionFlag"
								value="false">
							<%if (enableCreateAccount) {%>
							<input type="button"
								value="<%=commonLangMessage.get("form.text.button.create")%>"
								name="search" onclick='SearchHandler.create()'
								class="btn btn-primary">
							<%}%>
							<input type="button"
								value="<%=commonLangMessage.get("form.text.button.search")%>"
								name="search" onclick='SearchHandler.search()'
								class="btn btn-primary">
						</div>
					</div>
				</div>
			</form>


		</div>
	</div>
</div>

<%-- result start --%>
<div class="row" id='resultTable' style='display:none'>
	<div class="col-md-12">
		<div class="widget box">
			<%
				if (enableExportExcelAccount) {
			%>
			<div id='exportButton' style='display:none'>
				<input type="button" value="<%=commonLangMessage.get("form.text.button.exportExcel")%>"
					name="export" onclick='SearchHandler.exportExcel()' class="btn btn-primary">
			</div>
			<%
				}
			%>
			<div class="widget-content">
				<table id="MemberSearchTable" class="table table-striped table-bordered" cellspacing="0"
					width="100%">
					<thead>
					<tr>
						<%
							for (String title : titleList) {
								String titleContent = commonLangMessage.get(title);
								String thid = title.toLowerCase().replace(" ", "_");

								if (titleContent.equalsIgnoreCase("total balance")) {
									titleContent = titleContent + " (" + commonCurrencyTypeSymbol + ")";
								}
						%>
						<th id="<%=thid%>"><%= titleContent %>
						</th>
						<%
							}
						%>
					</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<%-- result end --%>

<%-- Create Form start --%>
<div class="modal fade" id="createAccountModal" role="dialog">
	<div class="modal-dialog modal-xlg">
		<form class="form-horizontal" name="createAccountForm" action="#">
			<!-- Modal content-->
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h4 class="modal-title" id="modalTitle"><%=commonLangMessage
						.get("form.text.account.createMember")%>
					</h4>
				</div>

				<div class="modal-body">
					<!-- 标题与 Collapse 按钮 -->
					<div class="row">
						<div class="col-md-12">
							<div class="widget box">
								<div class="widget-header">
									<h4>
										<i class="icon-reorder"></i> <%=commonLangMessage.get(
										"form.text.account.accountCredentials")%>
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
												<label class="control-label"
													id="creatEmailLebel"><%=commonLangMessage
													.get("form.text.account.email")%><span class="required">*</span>
												</label>

												<input type="text" id="createEmail" name="createEmail"
													class="form-control"
													placeholder="<%=commonLangMessage.get("form.text.account.email")%>">
											</div>
										</div>
										<div class="col-md-4">
											<div class="form-group">
												<label class="control-label"><%=commonLangMessage
													.get("form.text.backOffice.staff.password")%><span
													class="required">*</span>
												</label>

												<div class="input-container">
													<input type="password" id="createPassword"
														name="createPassword"
														class="form-control"
														autocomplete="new-password"
														placeholder="<%=commonLangMessage.get("form.text.backOffice.staff.password")%>">
													<i class="icon-eye-close" id="togglePassword"
														onclick="SearchHandler.toggleVisibility('createPassword', this)"></i>
													<input type="hidden" name="encodePassword">

												</div>
												<div id="passwordRequirements" class="password-requirements-box"
													style="display: none">
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
										<div class="col-md-4">
											<div class="form-group">
												<label class="control-label"><%=commonLangMessage
													.get("form.text.backOffice.staff.confirmPassword")%><span
													class="required">*</span>
												</label>

												<div class="input-container">
													<input type="password" id="createConfirmPassword"
														name="createConfirmPassword" class="form-control"
														autocomplete="new-password"
														placeholder="<%=commonLangMessage.get("form.text.backOffice.staff.confirmPassword")%>">
													<i class="icon-eye-close" id="toggleConfirmPassword"
														onclick="SearchHandler.toggleVisibility('createConfirmPassword', this)"></i>
													<input type="hidden" name="encodePassword">
												</div>

												<label class="error-msg-block"></label>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>

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
									<div class="row" style="display: flex; padding:0 20px 0 20px">
										<div class="col-md-12">
											<div class="form-group">
												<label class="control-label">
													<%=commonLangMessage.get("form.text.backOffice.remark")%>
												</label>
												<input type="text" id="userRemark" name="userRemark"
													maxlength="3000"
													class="form-control"
													placeholder="<%=commonLangMessage.get("form.text.backOffice.remark")%>">
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
						name="resetButton" onclick='SearchHandler.resetAccount()'
						class="btn btn-primary">
					<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
						name="save" onclick='SearchHandler.createAccount()' class="btn btn-primary">
				</div>
			</div>
		</form>
	</div>
</div>

<%-- Create Form end --%>

<%
	if (enableUpdateStatus) {
%>
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
							<select class="form-control" name="status">
									<% for (AccountStatusType status : AccountStatusType.getSortedAccountStatusTypes()) {%>
								<option value="<%=status.unique()%>">
									<%=status.getFullName(commonLangMessage)%>
								</option>

									<% } %>
						</div>
						<div class="col-md-7">
							<input type='hidden' name='userId'>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
						name="resetButton" onclick='SearchHandler.resetStatus()'
						class="btn btn-primary">
					<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
						name="save" onclick='SearchHandler.updateStatus()' class="btn btn-primary">
				</div>
			</div>
		</form>
	</div>
</div>
<%
	}
%>


<%--	update kyc status model--%>
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
							</select>
						</div>
						<div class="col-md-7">
							<input type='hidden' name='kycUserId'>
							<input type='hidden' name='currencyTypeId'>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<input type="button" value="<%=commonLangMessage.get("ui.text.reset")%>"
						name="resetKycButton" onclick='SearchHandler.resetKycStatus()'
						class="btn btn-primary">
					<input type="button" value="<%=commonLangMessage.get("form.text.button.save")%>"
						name="saveKycButton" onclick='SearchHandler.updateKycStatus()' class="btn btn-primary">
				</div>
			</div>
		</form>
	</div>
</div>

</div>