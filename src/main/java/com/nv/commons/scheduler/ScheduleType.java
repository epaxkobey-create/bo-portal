package com.nv.commons.scheduler;

import com.nv.api.job.AccountProviderCacheUpdateJob;
import com.nv.api.job.OTPRecordCacheCleanJob;
import com.nv.api.job.OTPRecordCacheUpdateJob;
import com.nv.commons.cache.RemotingCaller;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.manage.job.AccountStatsSummaryJob;
import com.nv.manage.job.DailySummaryJob;
import com.nv.manage.job.GameSessionUsageResetJob;
import com.nv.manage.job.GameTxnPreSummaryJob;
import com.nv.manage.job.GameTxnSummaryByUserJob;
import com.nv.manage.job.HourlySummaryJob;
import com.nv.manage.job.ManagerUpdateJob;
import com.nv.manage.job.MoneyTransactionCreateJob;
import com.nv.manage.job.MoneyTxnRejectPendingJob;
import com.nv.manage.job.RealityCheckReminderJob;
import com.nv.manage.job.RemotingCallerJob;
import com.nv.module.backendapi.job.TokenRecordCacheUpdateJob;
import com.nv.manage.job.GameTxnQueryAndUpdateOddsJob;
import com.nv.player.job.AccountCacheUpdateJob;
import com.nv.player.job.AccountPlayResponsiblyAnnualReminderYearlyResetJob;
import com.nv.player.job.AccountPlayResponsiblyHandleEffectiveTimeJob;
import com.nv.player.job.AccountPlayResponsiblySettingCacheUpdateJob;
import com.nv.player.job.CleanCacheJob;
import com.nv.manage.job.MoneyTxnCacheUpdateJob;
import com.nv.player.job.PlayerCacheUpdateJob;
import org.quartz.Job;

/**
 * @author Luke Chi
 * <p>
 * cron-expression refer to
 * <a href="https://www.freeformatter.com/cron-expression-generator-quartz.html">...</a>
 */
public enum ScheduleType implements ScheduleBase {

	//=============
	//	Player
	//=============

	/********************* PlayerCacheUpdateJob 排程 ***********************/
	PlayerCacheUpdateJob(PlayerCacheUpdateJob.class, "* * * * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isPlayerServer()) {
				super.execute();
			}
		}
	},

	/********************* OTPRecordCacheUpdateJob 排程 ***********************/
	OTPRecordCacheUpdateJob(OTPRecordCacheUpdateJob.class, "0/10 * * * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isPlayerServer() || ServerInfoUtils.isBackendApiServer()) {
				super.execute();
			}
		}
	},
	/********************* OTPRecordCacheUpdateJob 排程 ***********************/
	OTPRecordCacheCleanJob(OTPRecordCacheCleanJob.class, "* * 0/1 * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isPlayerServer() || ServerInfoUtils.isBackendApiServer()) {
				super.execute();
			}
		}
	},

	//=============
	//	Manager
	//=============

	/********************* ManagerUpdateJob 排程 ***********************/
	ManagerUpdateJob(ManagerUpdateJob.class, "* * * * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	/********************* Auto Reject pending MoneyTransaction 排程 ***********************/
	MoneyTxnRejectPendingJob(MoneyTxnRejectPendingJob.class, "0 */1 * * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	//=============
	//	Multi-server or ALL
	//=============

	/********************* RemotingCallerJob 排程 ***********************/
	RemotingCallerJob(RemotingCallerJob.class, "0/5 * * * * ?") {
		@Override
		public void execute() {
			/** AP系統通知資訊初始化 */
			RemotingCaller.getInstance().init();

			/** 定期更新暨有AP系統資訊 */
			if (ServerInfoUtils.isPlayerServer() || ServerInfoUtils.isBackendApiServer()
				|| ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	/********************* TokenRecordCacheUpdateJob 排程 ***********************/
	TokenRecordCacheUpdateJob(TokenRecordCacheUpdateJob.class, "* * * * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isBackendApiServer()) {
				super.execute();
			}
		}
	},

	/********************* All Server AccountProviderCache update 排程 ***********************/
	AccountProviderCacheUpdateJob(AccountProviderCacheUpdateJob.class, "* * * * * ?") {
		@Override
		public void execute() {
			super.execute();
		}
	},

	/********************* CacheRemoveJob 排程 ***********************/
	CleanCacheJob(CleanCacheJob.class, "0 0 */1 * * ?") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isPlayerServer() || ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	/********************* AccountPlayResponsiblySettingCacheUpdateJob 排程 ***********************/
	AccountPlayResponsiblySettingCacheUpdateJob(AccountPlayResponsiblySettingCacheUpdateJob.class, "* * * * * ?") {
		@Override
		public void execute() {
			super.execute();
		}
	},
	/********************* AccountPlayResponsiblyHandleEffectiveTimeJob 排程 ***********************/
	AccountPlayResponsiblyHandleEffectiveTimeJob(AccountPlayResponsiblyHandleEffectiveTimeJob.class, "0 * * ? * *") {
		@Override
		public void execute() {
			super.execute();
		}
	},
	/********************* AccountPlayResponsiblyAnnualReminderYearlyResetJob 排程 ***********************/
	AccountPlayResponsiblyAnnualReminderYearlyResetJob(AccountPlayResponsiblyAnnualReminderYearlyResetJob.class,
		"0 0 0 1 1 ? *") { // every 01 Jan 00:00:00

		@Override
		public void execute() {
			super.execute();
		}
	},

	GameSessionUsageResetJob(GameSessionUsageResetJob.class, "0 * * ? * *") {
		@Override
		public void execute() {
			super.execute();
		}
	},

	/********************* HourlySummaryJob 排程 ***********************/
	// TODO: confirm the interval
	HourlySummaryJob(HourlySummaryJob.class, "0 * * ? * *") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	/********************* DailySummaryJob 排程 ***********************/
	DailySummaryJob(DailySummaryJob.class, "0 0 3 * * ?") {
		@Override
		public void execute() {

			//			if (Features.DAILYSUMMARYJOB_SETTLEMENT_SERVER.isActive()) {
			//			if (SystemSettingUtil.isActive(name())) {
			//
			//				if (ServerInfoUtils.isSettlementServer()) {
			//					super.execute();
			//				}
			//			} else {

			if (ServerInfoUtils.isManagerServer()) {
				//				super.execute();
			}
			//			}
		}
	},

	/********************* GameTxnPreSummaryJob 排程 ***********************/
	GameTxnPreSummaryJob(GameTxnPreSummaryJob.class, "5 */5 * * * ?") {
		@Override
		public void execute() {

			//			if (SystemSettingUtil.isActive(name())) {
			//
			//				if (ServerInfoUtils.isSettlementServer()) {
			//					super.execute();
			//				}
			//			} else {

			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
			//			}
		}
	},

	/********************* GameTxnSummaryByUserJob 排程 ***********************/
	GameTxnSummaryByUserJob(GameTxnSummaryByUserJob.class, "5 */5 * * * ?") {
		@Override
		public void execute() {

			//			if (SystemSettingUtil.isActive(name())) {
			//
			//				if (ServerInfoUtils.isSettlementServer()) {
			//					super.execute();
			//				}
			//			} else {

			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
			//			}
		}
	},

	/********************* AccountTurnoverSummary 排程 ***********************/
	AccountStatsSummaryJob(AccountStatsSummaryJob.class, "10 */5 * * * ?") {
		@Override
		public void execute() {
			//			if (Features.ACCOUNTSTATSSUMMARYJOB_SETTLEMENT_SERVER.isActive()) {
			//			if (SystemSettingUtil.isActive(name())) {
			//
			//				if (ServerInfoUtils.isSettlementServer()) {
			//					super.execute();
			//				}
			//			} else {
			if (ServerInfoUtils.isManagerServer()) {
				//				super.execute();
			}
			//			}
		}
	},

	/********************* AutoCreateWithdrawal 排程 ***********************/

	// 0 */5 * ? * * * ----- testing use
	// "0 0 0 ? * * *" -----
	WithdrawalJobForInactiveAccount(MoneyTransactionCreateJob.class, "0 0 0 ? * * *") {
		@Override
		public void execute() {
			if (ServerInfoUtils.isManagerServer()) {
				super.execute();
			}
		}
	},

	/********************* AccountCacheUpdateJob 排程 ***********************/
	AccountCacheUpdateJob(AccountCacheUpdateJob.class, "0/10 * * ? * * *") { // every 10 seconds
		@Override
		public void execute() {
			super.execute();
		}
	},

	/********************* MoneyTxnCacheUpdateJob 排程 ***********************/
	MoneyTxnCacheUpdateJob(MoneyTxnCacheUpdateJob.class, "0/10 * * ? * * *") { // every 10 seconds
		@Override
		public void execute() {
			super.execute();
		}
	},

	/********************* RealityCheckReminderJob 排程 ***********************/
	RealityCheckReminderJob(RealityCheckReminderJob.class, "0 * * ? * *") { // every 1 minute
		@Override
		public void execute() {
			if (ServerInfoUtils.isBackendApiServer()) {
				super.execute();
			}
		}
	},

	/********************* GameTxnQueryAndUpdateOddsJob 排程 ***********************/
	GameTxnQueryAndUpdateOddsJob(GameTxnQueryAndUpdateOddsJob.class, "0/30 * * ? * * *") { // every 30 seconds
		@Override
		public void execute() {
			super.execute();
		}
	};

	private final Class<? extends Job> jobClass;
	private final String name;
	// MEMO: YB 沒有臨時更改排程的需要 所以設為 private
	private final String cronSchedule;

	ScheduleType(Class<? extends Job> jobClass, String cronSchedule) {
		this.jobClass = jobClass;
		this.name = jobClass.getSimpleName();
		this.cronSchedule = cronSchedule;
	}

	@Override
	public Class<? extends Job> getJobClass() {
		return jobClass;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTriggerName() {
		return name + "Trigger";
	}

	@Override
	public String getGroup() {
		return "DEFAULT";
	}

	@Override
	public String getCronSchedule() {
		return cronSchedule;
	}

	public void execute() {
		try {
			CronScheduler.getInstance().createCronScheduleJob(this);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}
}
