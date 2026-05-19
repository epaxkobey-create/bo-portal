package com.nv.manage.job;

import com.nv.commons.scheduler.InterruptableBaseJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description: NotificationSetting同步更新
 *
 * @author: Rex
 * @version: 1.0
 */
@Deprecated
@DisallowConcurrentExecution
public class NotificationSettingUpdateJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg0) throws JobExecutionException {
//		if ("15".equals(SystemInfo.getInstance().getServerID())) {
//			return;
//		}
//		NotificationCache.getInstance().update();
	}

}
