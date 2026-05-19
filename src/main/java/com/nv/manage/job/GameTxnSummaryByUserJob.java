package com.nv.manage.job;

import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.task.GameTxnSummaryByUserTask;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//@DisallowConcurrentExecution
public class GameTxnSummaryByUserJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext context) throws JobExecutionException {
		GameTxnSummaryByUserTask.getInstance().sync();
	}
}
