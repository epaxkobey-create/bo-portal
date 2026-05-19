package com.nv.commons.scheduler;

import org.quartz.Job;
interface ScheduleBase {

	Class<? extends Job> getJobClass();

	String getName();

	String getTriggerName();

	String getGroup();

	String getCronSchedule();
}

