package com.nv.manage.job;

import com.nv.commons.bo.GameTransactionBO;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description: Summary Update
 */
@DisallowConcurrentExecution
public class HourlySummaryJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg0) throws JobExecutionException {

		long startTime = System.currentTimeMillis();

		GameTransactionBO.insertGameTxnSummaryHourly();

		long duration = DateUtils.secondsBetween(startTime, System.currentTimeMillis());

		if (duration >= 5) {
			LogUtils.SYS.warn("Finishing HourlySummaryJob takes {} sec", duration);
		}
	}
}
