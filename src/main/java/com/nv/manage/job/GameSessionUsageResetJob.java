package com.nv.manage.job;

import com.nv.commons.bo.GameSessionUsageResetJobBO;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GameSessionUsageResetJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {
		long currentTime = System.currentTimeMillis();
		if (ServerInfoUtils.isManagerServer()) {
			GameSessionUsageResetJobBO.run();
		}

		long duration = DateUtils.secondsBetween(currentTime, System.currentTimeMillis());

		if (duration >= 5) {
			LogUtils.SYS.warn("GameSessionUsageResetJob update use {} secs ", duration);
		}
	}

}
