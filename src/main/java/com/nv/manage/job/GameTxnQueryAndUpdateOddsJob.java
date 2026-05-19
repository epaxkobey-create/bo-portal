package com.nv.manage.job;

import java.util.List;

import com.nv.commons.cache.ProviderCache;
import com.nv.commons.cache.ProviderProxyCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.WebsiteProvider;
import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class GameTxnQueryAndUpdateOddsJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {

		long currentTime = System.currentTimeMillis();

		try {
			List<WebsiteProvider> websiteProviders = ProviderCache.getInstance().getWebsiteProvider(WebSiteType.RSG);
			for (WebsiteProvider websiteProvider : websiteProviders) {
				ProviderProxy proxy = ProviderProxyCache.getInstance()
					.getProviderProxy(WebSiteType.RSG, websiteProvider.getProviderId(), CurrencyType.EUR);
				proxy.updateGameTransactionFromProvider(WebSiteType.RSG);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}

		long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

		if (duration >= 5) {
			LogUtils.SYS.warn("GameTxnQueryAndUpdateOddsJob update use {} secs ", duration);
		}
	}
}
