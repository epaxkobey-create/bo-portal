package com.nv.manage.job;

import com.nv.commons.bo.MoneyTransactionBO;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MoneyTransactionCreateJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {
		long startTime = System.currentTimeMillis();

		LogUtils.SYS.info("Auto create withdrawal job start");

		try {
			MoneyTransactionBO.autoGenerateWithdrawalRequest(WebSiteType.RSG.unique());
		} catch (Exception e) {
			LogUtils.SYS.error("failed to auto generate withdrawal request, reason: {}",e.getMessage());
		}

		LogUtils.SYS.info("Finishing MoneyTransactionCreateJob takes {} sec ", DateUtils.secondsElapsedSince(startTime));
	}
}
