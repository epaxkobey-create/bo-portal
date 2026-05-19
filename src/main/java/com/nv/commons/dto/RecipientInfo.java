package com.nv.commons.dto;

import com.nv.commons.cache.WebsiteCurrencySettingCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MailTemplateType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.WebSiteType;

public class RecipientInfo {

	//收件者列表 可以多筆
	private String recipients;

	private WebSiteType webSiteType;

	private CurrencyType currencyType;

	private LanguageType languageType;

	private MailTemplateType mailTemplateType;

	// 信件主旨
	private String subject;

	private String urlPrefixForImage;

	private String userId;

	private String userName;

	private String[] replaceToMessages;

	public LanguageType getLanguageType() {
		return this.languageType != null ? this.languageType :
			WebsiteCurrencySettingCache.getInstance().getDefaultLanguage(this.webSiteType, this.currencyType);

	}

	private MarketingGroupType marketingGroupType;

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public WebSiteType getWebSiteType() {
		return webSiteType;
	}

	public void setWebSiteType(WebSiteType webSiteType) {
		this.webSiteType = webSiteType;
	}

	public CurrencyType getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(CurrencyType currencyType) {
		this.currencyType = currencyType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public MailTemplateType getMailTemplateType() {
		return mailTemplateType;
	}

	public void setMailTemplateType(MailTemplateType mailTemplateType) {
		this.mailTemplateType = mailTemplateType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getUrlPrefixForImage() {
		return urlPrefixForImage;
	}

	public void setUrlPrefixForImage(String urlPrefixForImage) {
		this.urlPrefixForImage = urlPrefixForImage;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String[] getReplaceToMessages() {
		return replaceToMessages;
	}

	public void setReplaceToMessages(String[] replaceToMessages) {
		this.replaceToMessages = replaceToMessages;
	}

	public MarketingGroupType getMarketingGroup() {
		return marketingGroupType;
	}

	public void setMarketingGroup(MarketingGroupType marketingGroupType) {
		this.marketingGroupType = marketingGroupType;
	}
}
