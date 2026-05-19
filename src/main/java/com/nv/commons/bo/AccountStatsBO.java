package com.nv.commons.bo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountStatsDAO;
import com.nv.commons.dao.AccountSummaryReportDAO;
import com.nv.commons.dao.WebsiteInfoDAO;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.CollectionUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class AccountStatsBO {

	public static void summarize(Timestamp startTime, Timestamp endTime) throws Exception {
		summarize(startTime, endTime, null);
	}

	public static void summarize(Timestamp startTime, Timestamp endTime, WebSiteType website) throws Exception {

		Map<WebSiteType, List<String>> userMap = new EnumMap<>(WebSiteType.class);

		try (Connection conn = DBPool.getStandbyReadConnection()) {

			if (website == null) {

				for (WebsiteInfo websiteInfo : WebsiteInfoDAO.getAll(conn)) {

					WebSiteType webSiteType = WebSiteType.getInstance(websiteInfo.getId());
					List<String> userIdList = AccountSummaryReportDAO.getUserIdForSummary(conn, webSiteType, startTime,
						endTime);

					if (userIdList.isEmpty()) {
						continue;
					}

					userMap.put(webSiteType, userIdList);
				}
			} else {
				userMap.put(website, AccountSummaryReportDAO.getUserIdForSummary(conn, website, startTime, endTime));
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		}


		for (Map.Entry<WebSiteType, List<String>> entry : userMap.entrySet()) {

			WebSiteType webSiteType = entry.getKey();
			List<String> values = entry.getValue();

			List<List<String>> userIdList = values.stream().collect(CollectionUtils.groupingBy(100));
			AtomicInteger count = new AtomicInteger(0);
			List<Runnable> runnables = new ArrayList<>();

			for (List<String> userIdSubList : userIdList) {

				runnables.add(() -> {
					Connection readConn = null;
					Connection writeConn = null;
					String userId = null;
					try {

						readConn = DBPool.getStandbyReadConnection();
						writeConn = DBPool.getWriteConnection();
						//						writeConn.setAutoCommit(false);

						for (String user : userIdSubList) {
							userId = user;

							AccountStatsDAO.lock(writeConn, webSiteType, userId, startTime,
								DBQueryType.LOCK_FOR_UPDATE);

							// MEMO: 這邊不呼叫 setAutoCommit(false) , 讓 writeConn 執行 sql 馬上自動 commit
							AccountStatsDAO.summarize(readConn, writeConn, webSiteType, userId, startTime, endTime);

							writeConn.commit();
						}

					} catch (Exception e) {
						LogUtils.SYS
							.error("website:{}, userId:{}, time:{}", webSiteType.name(), userId, endTime);
						LogUtils.SYS.error(e.getMessage(), e);
						DbUtils.rollback(writeConn);
					} finally {
						DbUtils.close(writeConn);
						DbUtils.close(readConn);
					}
				});
			}

			GlobalThreadPool.await(runnables, 30);
		}
	}
}
