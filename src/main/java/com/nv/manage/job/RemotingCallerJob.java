package com.nv.manage.job;

import com.nv.commons.cache.RemotingCaller;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.LogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Neutec
 */
@DisallowConcurrentExecution
public class RemotingCallerJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext ctx) throws JobExecutionException {
		try {
			RemotingCaller.getInstance().update();
		} catch (Exception e) {
			LogUtils.SYS.error("Executing job error :", e);
		}
	}

}
