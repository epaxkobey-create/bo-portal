package com.nv.manage.job;

import java.sql.Connection;
import java.sql.Timestamp;

import com.nv.commons.bo.AccountStatsBO;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//@DisallowConcurrentExecution
public class AccountStatsSummaryJob extends InterruptableBaseJob {

	@Override
	public void run(JobExecutionContext arg) throws JobExecutionException {
		this.summarize();
	}

	private void summarize() {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			long currentTimeMillis = System.currentTimeMillis();
			long _5minsAgoTimeMillis = currentTimeMillis - (5 * 60 * 1000);

			Timestamp startTime = new Timestamp(_5minsAgoTimeMillis);
			Timestamp endTime = new  Timestamp(currentTimeMillis);

			AccountStatsBO.summarize(startTime, endTime);

			conn.commit();
		} catch (Exception ex) {
			if (!DbUtils.isLockedException(ex)) {
				LogUtils.SYS.error("Fail to sync AccountStatsSummaryTask", ex);
			}
			DbUtils.rollback(conn);
		} finally {
			DbUtils.close(conn);
		}
	}
}
