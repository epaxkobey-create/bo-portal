package com.nv.commons.scheduler;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * Cron Scheduler Setting
 *
 * @author Miles
 */
public class CronScheduler {

	private static final CronScheduler instance = new CronScheduler();
	private Scheduler scheduler;

	private CronScheduler() {
		try {
			// 這邊預設會使用quartz.properties
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();

			this.scheduler.start();

		} catch (SchedulerException e) {
			LogUtils.SYS.error("CronScheduler Error.", e);
		}
	}

	public final static CronScheduler getInstance() {
		return instance;
	}

	private JobKey getJobKey(ScheduleType scheduleType) {
		return JobKey.jobKey(scheduleType.getName(), scheduleType.getGroup());
	}

	// 顯示job資訊
	public ObjectNode getJobInfo() throws SchedulerException {
		try {
			ObjectNode jsonNode = JSONUtils.getObjectMapper().createObjectNode();
			for (String groupName : this.scheduler.getJobGroupNames()) {
				for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					// get job's trigger
					List<? extends Trigger> triggers = this.scheduler.getTriggersOfJob(jobKey);

					for (Trigger trigger : triggers) {
						ObjectNode objectNode = JSONUtils.getObjectMapper().createObjectNode();
						Date nextFireTime = trigger.getNextFireTime();
						objectNode.put("groupName", jobKey.getGroup());
						objectNode.put("nextFireTime", nextFireTime.toString());
						jsonNode.set(jobKey.getName(), objectNode);
					}
				}
			}
			return jsonNode;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public ObjectNode getJobInfo(String jobKeyName) throws SchedulerException {
		try {
			ObjectNode jsonNode = JSONUtils.getObjectMapper().createObjectNode();
			for (String groupName : this.scheduler.getJobGroupNames()) {
				for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					if (!jobKey.getName().equals(jobKeyName)) {
						continue;
					}

					List<? extends Trigger> triggers = this.scheduler.getTriggersOfJob(jobKey);

					for (Trigger trigger : triggers) {
						ObjectNode objectNode = JSONUtils.getObjectMapper().createObjectNode();
						objectNode.put("groupName", jobKey.getGroup());
						objectNode.put("nextFireTimeLong", trigger.getNextFireTime().getTime());
						objectNode.put("nextFireTime", trigger.getNextFireTime().toString());
						jsonNode.set(jobKey.getName(), objectNode);
					}
				}
			}
			return jsonNode;
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 開始執行Cron Schedule Job
	 *
	 * @param scheduleType
	 * @throws SchedulerException
	 */
	public void createCronScheduleJob(ScheduleBase scheduleType) throws SchedulerException {

		Class<? extends Job> jobClass = scheduleType.getJobClass();

		// 建構job信息
		JobDetail job = JobBuilder.newJob(jobClass)
			.withIdentity(scheduleType.getName(), scheduleType.getGroup()).build();

		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
			.cronSchedule(scheduleType.getCronSchedule());

		CronTrigger cronTrigger = TriggerBuilder.newTrigger()
			.withIdentity(scheduleType.getTriggerName(), scheduleType.getGroup())//
			.withSchedule(cronScheduleBuilder).build();

		Date d2 = scheduler.scheduleJob(job, cronTrigger);

		LogUtils.SYS.info(job.getKey() + " has been scheduled to run at: " + d2
			+ " and repeat based on expression: " + cronTrigger.toString());
	}

	public void runOnce(ScheduleType scheduleType) throws SchedulerException {
		scheduler.triggerJob(getJobKey(scheduleType));
	}

	// 暫停任務
	public void pauseJob(ScheduleType scheduleType) throws SchedulerException {
		scheduler.pauseJob(getJobKey(scheduleType));
	}

	// 恢復任務
	public void resumeJob(ScheduleType scheduleType) throws SchedulerException {
		scheduler.resumeJob(getJobKey(scheduleType));
	}

	// 刪除定時任務
	public void deleteScheduleJob(ScheduleType scheduleType) throws SchedulerException {
		scheduler.deleteJob(getJobKey(scheduleType));
	}

	/**
	 * 強制中斷任務
	 * 因為刪除定時任務時，quartz還是會等之前的job完成，才會做移除跟新增job的動作，所以job被鎖死時，可考慮強制中斷
	 * 能被強制中斷的job必須實作InterruptableJob
	 *
	 * @param scheduleType
	 * @throws SchedulerException
	 */
	// 中斷定時任務
	public void interruptScheduleJob(ScheduleType scheduleType) throws SchedulerException {
		scheduler.interrupt(getJobKey(scheduleType));
	}

	public void shutdown() {
		if (this.scheduler != null) {
			try {
				this.scheduler.shutdown(true);
			} catch (SchedulerException ignore) {

			}
		}
	}

}