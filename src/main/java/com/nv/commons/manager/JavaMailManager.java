package com.nv.commons.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.nv.commons.cache.MailTemplateCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MailTemplateType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.MailTemplate;
import com.nv.commons.dto.RecipientInfo;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.StringUtil;
import com.nv.module.engagement.EngageMessageDeliverLogBO;
import com.nv.module.engagement.cache.EngageServiceAccountCache;
import com.nv.module.engagement.cache.WebsiteEngageProviderCache;
import com.nv.module.engagement.constant.EngagementPurpose;
import com.nv.module.engagement.constant.EngagementType;
import com.nv.module.engagement.dto.EngageServiceAccount;
import com.nv.module.engagement.dto.EngageServiceProvider;
import com.nv.module.engagement.email.EmailServiceProviderFactory;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

public class JavaMailManager {

	private static final JavaMailManager instance = new JavaMailManager();

	private Random random = new Random();

	private JavaMailManager() {
		initEmailTemplate();
	}

	/*
	 *
	 */
	public void initEmailTemplate() {
		try {
			MailTemplateCache.getInstance().refresh();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public static JavaMailManager getInstance() {
		return instance;
	}

	/**
	 * 將收件人集合字串轉換成JavaMail所需要的InternetAddress[]格式
	 *
	 * @param recipients
	 * @return
	 * @throws AddressException
	 */
	private InternetAddress[] convertToInternetAddress(String recipients) throws AddressException {
		// 改用標準的函數(用逗號分隔)
		return InternetAddress.parse(recipients);
	}

	private MimeBodyPart convertToHTMLPart(String content) throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent(content, "text/html;charset=UTF-8");
		return bodyPart;
	}

	/**
	 *
	 */
	private void sendMail(int engageServiceAccountId,
		RecipientInfo recipientInfo, MimeBodyPart content,
		// MimeBodyPart[] attachmentFiles,
		// MEMO: https://trello.com/c/Y5Woq3P0/2056 , 於 Sender 右側新增 Purpose 欄位
		EngagementPurpose purpose) throws Exception {

		InternetAddress[] recipients = convertToInternetAddress(recipientInfo.getRecipients());
		String subject = recipientInfo.getSubject();
		WebSiteType websiteType = recipientInfo.getWebSiteType();

		Multipart mimeMultipart = new MimeMultipart();

		if (content != null) {
			mimeMultipart.addBodyPart(content);
		}

		//		if (attachmentFiles != null) {
		//			for (MimeBodyPart attachmentFile : attachmentFiles) {
		//				mimeMultipart.addBodyPart(attachmentFile);
		//			}
		//		}

		final Session session;
		String nick;
		final String sender;
		final String username;
		final String password;

		final int providerId;
		final String providerName;
		final String accountName;

		final EngageServiceAccount engageServiceAccount = EngageServiceAccountCache.getInstance()
			.getAccount(websiteType, EngagementType.EMAIL, engageServiceAccountId);

		final JsonNode accountJsonNode = JSONUtils.getObjectMapper().readTree(
			engageServiceAccount.getAccountInfo());

		nick = Optional.ofNullable(accountJsonNode.get("nickName"))
			.map(JsonNode::asText)
			.orElse(null);

		sender = accountJsonNode.get("sender").asText();
		username = accountJsonNode.get("username").asText();
		password = accountJsonNode.get("password").asText();

		final EngageServiceProvider engageServiceProvider = WebsiteEngageProviderCache.getInstance()
			.getProvider(websiteType, EngagementType.EMAIL, engageServiceAccount.getProviderId());
		String providerInfoJson = convertProviderInfoJson(engageServiceProvider.getProviderInfo());

		session = EmailServiceProviderFactory.getEmailSession(
			username, password, providerInfoJson);

		providerId = engageServiceProvider.getId();
		providerName = engageServiceProvider.getDisplayName();
		accountName = engageServiceAccount.getDisplayName();

		MimeMessage mimeMessage = new MimeMessage(session);
		if (nick != null) {
			nick = MimeUtility.encodeText(nick, "UTF-8", null);
			mimeMessage.setFrom(new InternetAddress(
				nick + " <" + sender + ">"));
		} else {
			mimeMessage.setFrom(
				new InternetAddress(sender));
		}
		//		if (replyTo != null) {
		//			mimeMessage.setReplyTo(new Address[]{new InternetAddress(replyTo)});
		//		}
		mimeMessage.setSubject(subject, "UTF-8");
		mimeMessage.setRecipients(Message.RecipientType.TO, recipients);
		mimeMessage.setContent(mimeMultipart, "UTF-8");
		mimeMessage.setSentDate(new Date());

		boolean isSuccess = false;
		String deliverResult = null;
		try {
			Transport transport = session.getTransport("smtp");

			transport.connect(
				session.getProperty("mail.smtp.host"),
				Integer.parseInt(session.getProperty("mail.smtp.port")),
				username,
				password);

			Transport.send(mimeMessage);
			isSuccess = true;

		} catch (Exception e) {
			isSuccess = false;
			deliverResult = e.getMessage();
			throw e;
		} finally {
			/**
			 * save log
			 */
			//			final CurrencyType currencyType = recipientInfo.getCurrencyType();

			//			CountryType countryType = WebsiteCurrencySettingCache.getInstance()
			//				.getDefaultCountryType(websiteType, currencyType);
			int countryTypeId = -1;

			final String receiver = recipientInfo.getRecipients();

			EngageMessageDeliverLogBO.insert(websiteType,
				EngagementType.EMAIL,
				providerId,
				providerName,
				accountName,
				countryTypeId,
				sender,
				receiver,
				isSuccess,
				deliverResult,
				purpose);
		}
	}

	private String convertProviderInfoJson(String providerInfoJson) {
		String resultJson = providerInfoJson;
		try {
			final JsonNode jsonNode = JSONUtils.getObjectMapper().readTree(providerInfoJson);
			if (jsonNode.get("totalPort") != null) {
				Integer totalPort = Integer.parseInt(jsonNode.get("totalPort").asText());
				if (totalPort > 1) {
					Map<String, String> infoMap = new HashMap<>();
					Integer index = random.nextInt(totalPort) + 1;
					infoMap.put("host", jsonNode.get("host" + index).asText());
					infoMap.put("port", jsonNode.get("port" + index).asText());
					infoMap.put("auth", jsonNode.get("auth" + index).asText());
					infoMap.put("socketClass", jsonNode.get("socketClass" + index).asText());
					infoMap.put("socketPort", jsonNode.get("socketPort" + index).asText());
					JsonNode starttls = jsonNode.get("starttls" + index);
					if (starttls != null) {
						infoMap.put("starttls", starttls.asText());
					}
					resultJson = JSONUtils.toJsonString(infoMap);
				}
			}
		} catch (Exception e) {
			LogUtils.SYS.error("convertProviderInfoJson error, providerInfoJson :{}", providerInfoJson);
		}
		return resultJson;
	}

	/*
	 *
	 */
	public void sendHtmlMail(RecipientInfo recipientInfo,
		// MEMO: https://trello.com/c/Y5Woq3P0/2056 , 於 Sender 右側新增 Purpose 欄位
		EngagementPurpose purpose)
		throws Exception {

		String content = getHtmlEmailBody(recipientInfo);

		MimeBodyPart body = convertToHTMLPart(content);

		final int engageServiceAccountId = -1;
		// no attachmentFiles: null
		//		sendMail(engageServiceAccountId, recipientInfo, body, null, purpose);
		sendMail(engageServiceAccountId, recipientInfo, body, purpose);
	}

	/*
	 *
	 */
	public String getHtmlEmailBody(RecipientInfo recipientInfo) {

		String urlPrefixForImage = recipientInfo.getUrlPrefixForImage();
//		WebSiteType webSiteType = recipientInfo.getWebSiteType();
//		CurrencyType currencyType = recipientInfo.getCurrencyType();
//		LanguageType languageType = recipientInfo.getLanguageType();

		int fileVersion = FrontendUtils.getJsFileVersion();

		String emailLogo = (urlPrefixForImage.contains("/images/"))
			// for BEA
			? urlPrefixForImage + "/email_logo.png?v=" + fileVersion
			// for FE
			: urlPrefixForImage + "/images/web/email_logo.png?v=" + fileVersion;

		String imageUrl = (urlPrefixForImage.contains("/images/"))
			// for BEA
			? urlPrefixForImage
			// for FE
			: urlPrefixForImage + "/images/web";

		Map<String, String> values = new HashMap<>();

		values.put("emailLogo", emailLogo);
		values.put("imageUrl", imageUrl);
		values.put("accountName", recipientInfo.getUserName());
		values.put("userId", recipientInfo.getUserId());

		return getHtmlContentByWebSite(recipientInfo, values);
	}

	/*
	 *
	 */
	private String getHtmlContentByWebSite(RecipientInfo recipientInfo, Map<String, String> values) {

		WebSiteType webSiteType = recipientInfo.getWebSiteType();

		CurrencyType currencyType = recipientInfo.getCurrencyType();
		LanguageType languageType = recipientInfo.getLanguageType();
		MailTemplateType mailTemplateType = recipientInfo.getMailTemplateType();

		MailTemplate mailTemplate = MailTemplateCache.getInstance()
			.get(webSiteType, currencyType, languageType, mailTemplateType);

		String template = mailTemplate.getTemplate();

		// common can support all type
		if ((template == null || StringUtil.isEmpty(template))
			&& mailTemplateType != MailTemplateType.COMMON) {
			mailTemplate = MailTemplateCache.getInstance()
				.get(webSiteType, currencyType, languageType, MailTemplateType.COMMON);
			template = mailTemplate.getTemplate();
			mailTemplateType = MailTemplateType.COMMON;
		}

		if (StringUtils.isEmpty(template)) {
			LogUtils.SYS.error(
				"missing email template for WebSiteType:{}, mailTemplateType:{}, languageType:{}",
				webSiteType, mailTemplateType, languageType);
			throw new RuntimeException("missing email template");
		}

		String[] templateReplaceKeys = mailTemplateType.getTemplateReplaceKeys();
		String[] replaceToStrings = recipientInfo.getReplaceToMessages();

		int forLength = Math.min(replaceToStrings.length, templateReplaceKeys.length);

		for (int i = 0; i < forLength; i++) {
			values.put(templateReplaceKeys[i], replaceToStrings[i]);
		}

		/*
		 * replace ${namedValue} with actual String value
		 */
		StringSubstitutor substitutor = new StringSubstitutor(values);

		return substitutor.replace(template);
	}

	public boolean checkMailSetting(WebSiteType webSiteType, CurrencyType currencyType, LanguageType languageType,
		MailTemplateType mailTemplateType, int engageServiceAccountId) {
		try {
			final EngageServiceAccount engageServiceAccount = EngageServiceAccountCache.getInstance()
				.getAccount(webSiteType, EngagementType.EMAIL, engageServiceAccountId);

			MailTemplate mailTemplate = MailTemplateCache.getInstance()
				.get(webSiteType, currencyType, languageType, mailTemplateType);

			LogUtils.SYS.debug("mailTemplateType:{}", JSONUtils.toJsonString(mailTemplate));

			if ((mailTemplate == null || StringUtil.isEmpty(mailTemplate.getTemplate()))
				&& mailTemplateType != MailTemplateType.COMMON) {
				mailTemplate = MailTemplateCache.getInstance()
					.get(webSiteType, currencyType, languageType, MailTemplateType.COMMON);
			}

			return engageServiceAccount != null && mailTemplate != null && StringUtils.isNotEmpty(mailTemplate.getTemplate());
		} catch (Exception e) {
			LogUtils.SYS.error("checkMailSetting error, " + e);
			throw new Deviation("msg.error.account.email.isNotSupported");
		}
	}

}
