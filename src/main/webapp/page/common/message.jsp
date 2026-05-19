<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.nv.commons.constants.LanguageType" %>
<%@ page import="com.nv.commons.message.LangMessage" %>
<%@ page import="com.nv.commons.utils.FrontendUtils" %>
<%@ page import="com.nv.commons.utils.ManagerUtils" %>
<%@ page import="com.nv.commons.utils.WebSiteTypeUtils" %>
<%

	LanguageType languageType = FrontendUtils.getLanguageType(session, request);

	if (WebSiteTypeUtils.getWebSiteByBoDomain(request.getServerName()) != null) {
		languageType = ManagerUtils.getLanguageType(session, request);
	}

	LangMessage langMessage = languageType.getLangMessage();

%>
<script>

	(function($) {
		$.extend($.validator.messages, {
			required: '<%=langMessage.get("msg.error.validation.required")%>',
			remote: '<%=langMessage.get("msg.error.validation.remote")%>',
			email: '<%=langMessage.get("msg.error.validation.email")%>',
			url: '<%=langMessage.get("msg.error.validation.url")%>',
			date: '<%=langMessage.get("msg.error.validation.date")%>',
			dateISO: '<%=langMessage.get("msg.error.validation.dateISO")%>',
			number: '<%=langMessage.get("msg.error.validation.number")%>',
			digits: '<%=langMessage.get("msg.error.validation.digits")%>',
			creditcard: '<%=langMessage.get("msg.error.validation.creditcard")%>',
			equalTo: '<%=langMessage.get("msg.error.validation.equalTo")%>',
			maxlength: $.validator.format('<%=langMessage.get("msg.error.validation.maxlength")%>'),
			maxDigitslength: $.validator.format('<%=langMessage.get("msg.error.validation.maxDigitslength")%>'),
			minlength: $.validator.format('<%=langMessage.get("msg.error.validation.minlength")%>'),
			minlength: $.validator.format('<%=langMessage.get("msg.error.validation.minlength")%>'),
			rangelength: $.validator.format('<%=langMessage.get("msg.error.validation.rangelength")%>'),
			range: $.validator.format('<%=langMessage.get("msg.error.validation.range")%>'),
			max: $.validator.format('<%=langMessage.get("msg.error.validation.max")%>'),
			min: $.validator.format('<%=langMessage.get("msg.error.validation.min")%>'),
			endDate: '<%=langMessage.get("msg.error.validation.endDate")%>',
			startDate: '<%=langMessage.get("msg.error.validation.startDate")%>',
			require_from_group: $.validator.format('<%=langMessage.get("msg.error.validation.require_from_group")%>'),
			alphanumeric2: '<%=langMessage.get("msg.error.validation.alphanumeric2")%>',
			alphanumeric2WithSpace: '<%=langMessage.get("msg.error.validation.alphanumeric2")%>',
			date2: '<%=langMessage.get("msg.error.validation.date")%>',
			checkAgeOver18: '<%=langMessage.get("msg.error.account.birthday.isNotValidated")%>',
			dateTime: '<%=langMessage.get("msg.error.validation.dateTime")%>',
			qqId: '<%=langMessage.get("msg.error.account.qqId.isNotValidated")%>',
			weChatId: '<%=langMessage.get("msg.error.account.wechatId.isNotValidated")%>',
			skypeId: '<%=langMessage.get("msg.error.account.skypeId.isNotValidated") %>',
			telegramId: '<%=langMessage.get("msg.error.account.telegramId.isNotValidated") %>',
			lineId: '<%=langMessage.get("msg.error.account.lineId.isNotValidated") %>',
			moneyAmount: '<%=langMessage.get("msg.error.validation.moneyAmountInvalid")%>',
			cnBankAccNumber: '<%=langMessage.get("msg.error.account.bank.accountNumber.isNotValidated")%>',
			userId: '<%=langMessage.get("msg.error.account.userId.isNotValidated")%>',
			password2: '<%=langMessage.get("msg.error.password.isNotValidated")%>',
			playerLoginPassword: '<%=langMessage.get("msg.error.password.isNotValidated")%>',
			playerPasswordEqualToAccount: '<%=langMessage.get("msg.error.password.player.passwordEqualsAccount")%>',
			password918Kiss: '<%=langMessage.get("msg.error.password918Kiss.isNotValidated")%>',
			missHighlightedFields: '<%=langMessage.get("msg.error.validation.missHighlightedFields")%>',
			rangeNumber: $.validator.format('<%=langMessage.get("msg.error.validation.rangeNumber")%>'),
			maxNumber: $.validator.format('<%=langMessage.get("msg.error.validation.maxNumber")%>'),
			minNumber: $.validator.format('<%=langMessage.get("msg.error.validation.minNumber")%>'),
			biggerThanNumber: $.validator.format('<%=langMessage.get("msg.error.validation.biggerThanNumber")%>'),
			invalidFormat: '<%=langMessage.get("msg.error.validation.invalidFormat")%>',
			invalidUserIdFormat: '<%=langMessage.get("msg.error.validation.invalidUserIdFormat")%>',
			minStrict: $.validator.format('<%=langMessage.get("msg.error.validation.minStrict")%>'),
			notEqual: $.validator.format('<%=langMessage.get("msg.error.validation.notEqual")%>'),
			maxByteLength: $.validator.format('<%=langMessage.get("msg.error.validation.maxByteLength")%>'),
			gender: '<%=langMessage.get("msg.error.validation.number")%>',
			marital: '<%=langMessage.get("msg.error.validation.number")%>',
			exactLength: '<%=langMessage.get("msg.error.validation.exactlyLength")%>',
			referenceNo: '<%=langMessage.get("msg.error.account.bank.referenceNo.isNotValidated")%>'
		});
	}(jQuery));

</script>
