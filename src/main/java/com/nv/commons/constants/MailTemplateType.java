package com.nv.commons.constants;

import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.RecipientInfo;
import com.nv.commons.manager.JavaMailManager;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.LogUtils;
import com.nv.module.engagement.constant.EngagementPurpose;
import org.apache.commons.lang3.StringUtils;

public enum MailTemplateType implements UniqueValueHolder {

	COMMON(1, "email.html", new String[] {"message"}, "mail.common.title"),
	PASSWORD(2, "password.html", new String[] {"BrandName","domain","password"},
		"msg.account_inbox.updatedPassword.title"),
	VERIFICATION(3, "verification.html", new String[] {"BrandName","domain","verifyLink"},
		"msg.profile.verify.emailSubject"),
	;

	private final int value;
	private final String fileName;
	private final String[] templateReplaceKeys;
	private final String defaultTitleKey;


	MailTemplateType(int value, String fileName, String[] templateReplaceKeys, String defaultTitleKey) {
		this.value = value;
		this.fileName = fileName;
		this.templateReplaceKeys = templateReplaceKeys;
		this.defaultTitleKey = defaultTitleKey;
	}

	@Override
	public int unique() {
		return this.value;
	}

	public static MailTemplateType getInstance(int value) {
		for (MailTemplateType e : MailTemplateType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String[] getTemplateReplaceKeys() {
		return templateReplaceKeys;
	}

	public String getDefaultTitleKey() {
		return defaultTitleKey;
	}

	public String getDefaultTitleString(LangMessage langMessage, WebSiteType webSiteType) {
		if (this == PASSWORD) {
			return String.format("%s %s", langMessage.get(this.getDefaultTitleKey()),
				langMessage.get("ui.text.brand." + webSiteType.name()));
		} else if (this == VERIFICATION) {
			return langMessage.get("msg.profile.verify.emailSubject",
				new String[] {langMessage.get("ui.text.brand." + webSiteType.name())});
		}

		return langMessage.get(this.getDefaultTitleKey());
	}

	public void sendMail(String subject, Account account, String domain, EngagementPurpose purpose,
		LanguageType languageType, String... replaceValues) {

		WebSiteType webSiteType = WebSiteType.getInstance(account.getWebsiteType());

		CurrencyType currencyType = CurrencyType.getInstance(account.getCurrencyTypeId());
		if (languageType == null) {
			languageType = WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(webSiteType, currencyType);
		}

		if (StringUtils.isBlank(domain)) {
			domain = "https://";
		}

		LogUtils.SYS.info(
			"[sendMail] mailTemplateType : " + this.name() + ", webSiteType : " + webSiteType.name() + ", user : "
				+ account.getUserId());

		sendMail(account.getEmail(), subject, webSiteType, currencyType, languageType, domain, purpose,
			account.getUserId(), account.getUserName(), replaceValues);
	}

	public void sendMail(String mail, String subject, WebSiteType webSiteType, CurrencyType currencyType,
		LanguageType languageType, String domain, EngagementPurpose purpose, String userId, String userName,
		String... replaceValues) {

		if (replaceValues.length != this.templateReplaceKeys.length) {
			LogUtils.SYS.error("templateReplaceKeys must equal replaceValues");
			return;
		}

		if (StringUtils.isBlank(subject)) {
			subject = getDefaultTitleString(languageType.getLangMessage(), webSiteType);
		}
		try {

			RecipientInfo recipientInfo = new RecipientInfo();
			recipientInfo.setRecipients(mail);
			recipientInfo.setWebSiteType(webSiteType);
			recipientInfo.setCurrencyType(currencyType);
			recipientInfo.setLanguageType(languageType);
			recipientInfo.setMailTemplateType(this);
			recipientInfo.setSubject(subject);
			recipientInfo.setUrlPrefixForImage(domain);
			recipientInfo.setUserId(userId);
			recipientInfo.setUserName(userName);
			recipientInfo.setReplaceToMessages(replaceValues);
			recipientInfo.setMarketingGroup(WebsiteCurrencySettingCache.getInstance().getMarketGroup(webSiteType, currencyType));


			JavaMailManager.getInstance().sendHtmlMail(recipientInfo, purpose);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}
}
