package com.nv.manage.job;

import com.nv.commons.bo.AccountPlayResponsiblySettingBO;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class RealityCheckReminderJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {

		long currentTime = System.currentTimeMillis();

		AccountPlayResponsiblySettingBO.checkAndSendRealityCheckReminderToPlayer();

		long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

		if (duration >= 5) {
			LogUtils.SYS.warn("RealityCheckReminderJob use {} secs ", duration);
		}
	}
}
