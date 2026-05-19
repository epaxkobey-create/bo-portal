package com.nv.manage.job;

import com.nv.commons.cache.MoneyTransactionCache;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class MoneyTxnCacheUpdateJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {

		long currentTime = System.currentTimeMillis();
		
		// all Server will perform refresh
		MoneyTransactionCache.getInstance().update();

		long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

		if (duration >= 5) {
			LogUtils.SYS.warn("MoneyTxnCacheUpdateJob update use {} secs ", duration);
		}
	}
}
