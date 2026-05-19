package com.nv.manage.job;

import java.sql.Connection;
import java.util.Map;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.GameTransactionDAO;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.scheduler.InterruptableBaseJob;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.LogUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//@DisallowConcurrentExecution
public class GameTxnPreSummaryJob extends InterruptableBaseJob {

	private final static int UNSETTLE_COUNT = 100000;

	private final static String PREFIX = "[" + HostAddressUtils.getLocalIPAddress() + "][GameTxnSummaryByUserJob]\r\n";

	@Override
	public void run(JobExecutionContext context) throws JobExecutionException {
		checkSummary();
	}

	private void checkSummary(){
		LogUtils.backOfficeMonitor.info("GameTxnSummaryByUserJob, checking start");
		try (Connection conn = DBPool.getReadConnection()) {
			Map<WebSiteType, Integer> resultMap = GameTransactionDAO.getGameTxnNotSummarizedCount(conn);

			StringBuilder message = new StringBuilder(PREFIX);
			for (Map.Entry<WebSiteType, Integer> entry : resultMap.entrySet()) {

				int count = entry.getValue();

				if (count > UNSETTLE_COUNT) {
					WebSiteType webSiteType = entry.getKey();
					message.append("Website:").append(webSiteType.name()).append("\r\n");
					message.append("Count:").append(count).append("\r\n");

					String msg = message.toString();
					LogUtils.SYS.info(msg);
				}
			}

		} catch (Exception e) {
			LogUtils.backOfficeMonitor.error(e.getMessage(), e);
		}

		LogUtils.backOfficeMonitor.info("GameTxnSummaryByUserJob, checking end");
	}

}
