package com.nv.manage.job;

import com.nv.commons.scheduler.InterruptableBaseJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class MoneyTxnRejectPendingJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext context) throws JobExecutionException {
		// Malta不應該會有reject pending的情況
//		MoneyTxnRejectPendingSyncTask.getInstance().sync();
	}

}
