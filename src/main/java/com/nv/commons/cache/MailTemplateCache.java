package com.nv.commons.cache;

import com.nv.commons.bo.MailTemplateBO;
import com.nv.commons.cache.key.MailTemplateKey;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;
import com.nv.commons.constants.MailTemplateType;
import com.nv.commons.constants.MarketingGroupType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.MailTemplateDAO;
import com.nv.commons.dto.MailTemplate;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

public class MailTemplateCache extends AbstractCache {

	private static final MailTemplateCache instance = new MailTemplateCache();

	private final ConcurrentHashMap<MailTemplateKey, MailTemplate> cache = new ConcurrentHashMap<>();

	public static MailTemplateCache getInstance() {
		return instance;
	}

	private Timestamp latestUpdateTime = new Timestamp(0);

	@Override
	protected void init() {

		long currentTime = System.currentTimeMillis();

		try (Connection conn = DBPool.getReadConnection()) {
			MailTemplateDAO.getAll(conn).forEach(template -> {
					MailTemplateKey mailTemplateKey = new MailTemplateKey(template.getWebsiteType(),
						template.getMarketingGroup(), template.getCurrencyType(), template.getLanguageType(),
						template.getTemplateType());

					setMailTemplate(template);

					cache.put(mailTemplateKey, template);
				}
			);

		} catch (Exception e) {
			LogUtils.mailTemplate.error(e.getMessage(), e);
		} finally {

			long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

			if (duration > 1) {
				LogUtils.mailTemplate.info("MailTemplateCache init Use {} secs ", duration);
			}
		}
	}

	public void setMailTemplate(MailTemplate mailTemplate) {
		CurrencyType currencyType = CurrencyType.getInstance(mailTemplate.getCurrencyType());
		LanguageType languageType = LanguageType.getInstance(mailTemplate.getLanguageType());
		MailTemplateType mailTemplateType = MailTemplateType.getInstance(mailTemplate.getTemplateType());

		if (mailTemplateType == null) {
			throw new IllegalArgumentException("MailTemplate is null");
		}

		String template = setMailTemplate(currencyType, languageType, mailTemplateType);

		mailTemplate.setTemplate(template);

	}

	protected String setMailTemplate(CurrencyType currencyType,
		LanguageType languageType, MailTemplateType mailTemplateType) {

		String templateFolder = MailTemplateBO.getFolderPath(currencyType, languageType);

		String filePath = templateFolder + File.separator + mailTemplateType.getFileName();

		try (InputStream in = ResourceUtils.getResourceAsStream(filePath)) {

			String template = IOUtils.toString(in, StandardCharsets.UTF_8);

			if (StringUtils.isNotEmpty(template)) {
				return template;
			}

		} catch (Exception e) {
			LogUtils.mailTemplate.error(e.getMessage(), e);
		}

		return null;
	}

	public MailTemplate get(WebSiteType webSiteType, CurrencyType currencyType, LanguageType languageType,
		MailTemplateType templateType) {

		MarketingGroupType marketingGroupType = WebsiteCurrencySettingCache.getInstance()
			.getMarketGroup(webSiteType, currencyType);

		MailTemplateKey mailTemplateKey = new MailTemplateKey(webSiteType.unique(), marketingGroupType.getMarketingName(),
			currencyType.unique(), languageType.unique(), templateType.unique());

		MailTemplate mailTemplate = cache.get(mailTemplateKey);

		if (mailTemplate != null) {
			return mailTemplate;
		}

		String title = templateType.getDefaultTitleString(languageType.getLangMessage(), webSiteType);

		String template = setMailTemplate(currencyType, languageType, templateType);

		mailTemplate = new MailTemplate();
		mailTemplate.setWebsiteType(webSiteType.unique());
		mailTemplate.setMarketingGroup(marketingGroupType.getMarketingName());
		mailTemplate.setCurrencyType(currencyType.unique());
		mailTemplate.setLanguageType(languageType.unique());
		mailTemplate.setTemplateType(templateType.unique());
		mailTemplate.setTitle(title);
		mailTemplate.setTemplate(template);

		cache.put(mailTemplateKey, mailTemplate);

		return mailTemplate;
	}

	@Override
	public void update() {
		long aMinuteAgo = System.currentTimeMillis() - 60 * 1000;

		try (Connection conn = DBPool.getReadConnection()) {
			Timestamp tempLatestUpdateTime = latestUpdateTime;

			for (MailTemplate templateInDb : MailTemplateDAO.findLatestUpdate(conn, latestUpdateTime)) {

				WebSiteType webSiteType = WebSiteType.getInstance(templateInDb.getWebsiteType());
				CurrencyType currencyType = CurrencyType.getInstance(templateInDb.getCurrencyType());
				MarketingGroupType marketingGroupType = WebsiteCurrencySettingCache.getInstance()
					.getMarketGroup(webSiteType, currencyType);

				assert webSiteType != null;
				MailTemplateKey mailTemplateKey = new MailTemplateKey(webSiteType.unique(), marketingGroupType.getMarketingName(),
					currencyType.unique(), templateInDb.getLanguageType(), templateInDb.getTemplateType());

				MailTemplate templateInCache = cache.get(mailTemplateKey);

				if (templateInCache == null) {
					setMailTemplate(templateInDb);
					cache.put(mailTemplateKey, templateInDb);
				} else {
					templateInCache.setTitle(templateInDb.getTitle());
					templateInCache.setUpdater(templateInDb.getUpdater());
					templateInCache.setUpdateTime(templateInDb.getUpdateTime());

					if (templateInDb.getUpdateFileTime() != null
						&& templateInDb.getUpdateFileTime().after(tempLatestUpdateTime)) {

						templateInCache.setUpdateFileName(templateInDb.getUpdateFileName());
						templateInCache.setUpdateFileTime(templateInDb.getUpdateFileTime());
						setMailTemplate(templateInCache);
					}

					if (templateInDb.getUpdateTime().after(tempLatestUpdateTime)) {

						tempLatestUpdateTime = templateInDb.getUpdateTime();
					}
				}
			}

			//MEMO 若無資料更新 則將下次查詢的時間推進到這次開始做Update時間的前一分鐘
			if (latestUpdateTime.compareTo(tempLatestUpdateTime) == 0) {
				latestUpdateTime = new Timestamp(aMinuteAgo);
			} else {
				latestUpdateTime = tempLatestUpdateTime;
			}

		} catch (Exception ex) {
			LogUtils.mailTemplate.error("error while update MailTemplate cache", ex);
		}
	}

	@Override
	public void refresh() {
		init();
	}

	@Override
	public String getCacheInfo() {
		return "{}";
	}

}
