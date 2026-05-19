package com.nv.commons.constants;

import java.util.ArrayList;
import java.util.List;

import com.nv.commons.message.LangMessage;

/**
 * @author Rex Chou
 */
public enum ReportExportType {

	MEMBER(new String[] {
		"#",
		"form.text.account.email",
		"form.text.report.member.userName",
		"form.text.account.dob",
		"form.text.backOffice.status",
		"form.text.account.verification",
		"ui.text.profile.last_login_date",
		"form.text.account.totalBalance",

	}),

	MEMBER_WITH_PROVIDER_ACCOUNT(new String[] {
		"#",
		"form.text.backOffice.report.createTime",
		"ui.text.provider",
		"ui.text.providerAccount",
		"form.text.account.userId",
		"form.text.backOffice.staff.name",
		"form.text.account.email",
		"form.text.af.profile.phoneNumber",
		"form.text.account.birthday",
		"form.text.member.search.affiliateUrl",
		"form.text.backOffice.status",
		"form.text.account.totalBalance",
		"form.text.account.signUp",
		"form.text.account.loginIp",
		"form.text.backOffice.staff.lastLoginTime",
		"form.text.account.lastDepositDate",
		"form.text.account.lastBetDate",
		"form.text.backOffice.currencyType",
		"ui.text.member.userChannelType",
		"ui.text.member.userChannelName",
	}),

	DEPOSIT(new String[] {
		"#",
		"form.text.report.member.email",
		"form.text.backOffice.report.transactionId",
		"form.text.af.ui.amount",
		"form.text.backOffice.status",

		"form.text.backOffice.createdBy",
		"form.text.backOffice.payment.createdTime",
		"form.text.backOffice.report.updatedBy",
		"form.text.backOffice.updateTime"
	}),

	WITHDRAWAL(new String[] {
		"#",
		"form.text.report.member.email",
		"form.text.backOffice.report.transactionId",
		"form.text.af.ui.amount",
		"form.text.backOffice.status",

		"form.text.backOffice.createdBy",
		"form.text.backOffice.payment.createdTime",
		"form.text.backOffice.report.updatedBy",
		"form.text.backOffice.updateTime"}),

	DOCUMENT(new String[] {
		"#",
		"form.text.account.userId",
		"form.text.backOffice.currencyType",
		"form.text.backOffice.documentType",
		"form.text.backOffice.staff.createdTime",
		"form.text.backOffice.status",
		"form.text.backOffice.payment.approvedOrRejectedTime",
		"form.text.backOffice.payment.approvedOrRejectedBy",
		"form.text.backOffice.payment.executor",
		"form.text.backOffice.action"}),

	;

	private final String[] title;

	ReportExportType(String[] title) {
		this.title = title;
	}

	public List<String> getTitleList(LangMessage langMessage) {
		if (langMessage == null)
			return List.of(title);
		List<String> list = new ArrayList<>();
		for (String titleElement : title) {
			list.add(langMessage.get(titleElement));
		}
		return list;
	}

}
