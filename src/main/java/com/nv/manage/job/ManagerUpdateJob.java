package com.nv.manage.job;

import com.nv.commons.cache.ManagerCache;
import com.nv.commons.scheduler.InterruptableBaseJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description: Manager同步更新
 *
 * @author: Rex
 * @version: 1.0
 */
@DisallowConcurrentExecution
public class ManagerUpdateJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg0) throws JobExecutionException {
		ManagerCache.getInstance().update();
	}

}
