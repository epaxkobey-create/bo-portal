package com.nv.commons.task;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.constants.AccountSummaryReportType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.GameTxnSummaryType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.constants.WebsiteStatusType;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dao.AccountSummaryReportDAO;
import com.nv.commons.dao.GameTransactionSummaryDAO;
import com.nv.commons.dao.GameTransactionSummaryHourlyDAO;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountSummaryReport;
import com.nv.commons.dto.GameTransactionSummary;
import com.nv.commons.dto.GameTransactionSummaryHourly;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.manager.GlobalThreadPool;
import com.nv.commons.utils.BigDecimalUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbExecutor;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

/**
 * @author Dean Hsiao
 */
public class GameTxnSummaryByUserTask {

	private final static GameTxnSummaryByUserTask instance = new GameTxnSummaryByUserTask();

	private final AccountSummaryReportType[] accountSummaryReportTypes = {AccountSummaryReportType.BET,
		AccountSummaryReportType.TURNOVER};

	public static GameTxnSummaryByUserTask getInstance() {
		return instance;
	}

	public void sync() {
		long startTime = System.currentTimeMillis();
		summarizeGameTransactionByUser();

		double duration = DateUtils.secondsElapsedSince(startTime);
		if (duration > 10) {
			LogUtils.backOfficeMonitor.warn("Finishing GameTransactionSummarize takes {} sec ", duration);
		}
//		Connection connUpdateNoWait = null;
//		try {
//			connUpdateNoWait = DBPool.getWriteConnection();
//			connUpdateNoWait.setAutoCommit(false);
//
//			SystemSettingDAO.findByKey(connUpdateNoWait, SystemSettingKeyConstants.SUMMARY_GAME_TXN_UPDATE_NOWAIT,
//				DBQueryType.LOCK_FOR_UPDATE_NO_WAIT);
//
//			long startTime = System.currentTimeMillis();
//
//			summarizeGameTransactionByUser();
//			double duration = DateUtils.secondsElapsedSince(startTime);
//			if (duration > 10) {
//				LogUtils.opAction.warn("Finishing GameTransactionSummarize takes {} sec ", duration);
//			}
//
//			connUpdateNoWait.commit();
//
//		} catch (Exception ex) {
//			if (!DbUtils.isLockedException(ex)) {
//				LogUtils.opAction.error("Fail to sync accountTurnoverSummary", ex);
//			}
//			DbUtils.rollback(connUpdateNoWait);
//		} finally {
//			DbUtils.close(connUpdateNoWait);
//		}
	}

	private void summarizeGameTransactionByUser() {
		List<Runnable> taskList = new ArrayList<>();

		for (WebsiteInfo websiteInfo : WebsiteInfoCache.getInstance().getAll()) {

			WebSiteType webSiteType = WebSiteType.getInstance(websiteInfo.getId());
			if (WebsiteStatusType.isAllInactive(webSiteType)) {
				continue;
			}

			taskList.add(() -> {

				try {

					Map<Timestamp, List<String>> notSummarizedMap = DbExecutor.query(conn -> {
						return GameTransactionSummaryHourlyDAO.findNotSummarized(conn, webSiteType);
					});

					int successCount = 0;

					Set<String> currencySet = new HashSet<>();

					Set<Long> updateAffiliateSet = new HashSet<>();

					for (Entry<Timestamp, List<String>> notSummarizedEntry : notSummarizedMap.entrySet()) {
						Timestamp settleDate = notSummarizedEntry.getKey();

						for (String userId : notSummarizedEntry.getValue()) {

							Account account = DbExecutor.query(conn -> {
								return AccountDAO.getAccountByUserId(conn, userId, webSiteType, DBQueryType.UNLOCK);
							});

							CurrencyType accountCurrencyType = CurrencyType.getInstance(account.getCurrencyTypeId());
							currencySet.add(accountCurrencyType.name());

							updateAffiliateSet.add(account.getAffiliateId());

							Timestamp startTime = DateTimeBuilder.localDateTime(settleDate)
								// .withMinTime() // TODO: confirm the interval
								.toTimestamp();
							Timestamp endTime = DateTimeBuilder.localDateTime(settleDate)
								// .withMaxTime() // TODO: confirm the interval
								.toTimestamp();

							List<GameTransactionSummaryHourly> txnSummaryListForUpdate = new ArrayList<>();

							// key: vendorId-gameType
							Map<String, List<GameTransactionSummaryHourly>> gameTxnSummaryHourlyMap = DbExecutor.query(conn -> {
								return GameTransactionSummaryHourlyDAO.find(conn, webSiteType, userId, startTime, endTime);
							});

							List<GameTransactionSummary> gameTxnSummaryList = new ArrayList<>();

							for (List<GameTransactionSummaryHourly> gameTxnSummaryHourlyList : gameTxnSummaryHourlyMap.values()) {
								BigDecimal betAmount = BigDecimal.ZERO;
								BigDecimal profit = BigDecimal.ZERO;
								BigDecimal turnover = BigDecimal.ZERO;
								BigDecimal progressBetAmount = BigDecimal.ZERO;
								BigDecimal progressProfitLoss = BigDecimal.ZERO;
								int betCount = 0;
								int maxVipLv = 0;
								for (GameTransactionSummaryHourly gameTxnSummary : gameTxnSummaryHourlyList) {
									maxVipLv = Math.max(maxVipLv, gameTxnSummary.getVipLevel());
									betCount += gameTxnSummary.getBetCount();
									betAmount = BigDecimalUtils.add(betAmount, gameTxnSummary.getSumBetAmount());
									turnover = BigDecimalUtils.add(turnover, gameTxnSummary.getTurnover());
									profit = BigDecimalUtils.add(profit, gameTxnSummary.getProfit());

									BigDecimal pBetAmount = gameTxnSummary.getProgressBetAmount() == null
										? BigDecimal.ZERO
										: gameTxnSummary.getProgressBetAmount();
									progressBetAmount = BigDecimalUtils.add(progressBetAmount, pBetAmount);

									BigDecimal pProfitLoss = gameTxnSummary.getProgressProfitLoss() == null
										? BigDecimal.ZERO
										: gameTxnSummary.getProgressProfitLoss();
									progressProfitLoss = BigDecimalUtils.add(progressProfitLoss, pProfitLoss);

									if (gameTxnSummary.getIsSummarized() == GameTxnSummaryType.NOT_SUMMARIZED
										.unique()) {
										txnSummaryListForUpdate.add(gameTxnSummary);
									}
								}

								GameTransactionSummaryHourly tempTxnSummary = gameTxnSummaryHourlyList.get(0);

								GameTransactionSummary gameTxnSummary = new GameTransactionSummary();
								gameTxnSummary.setUserId(userId);
								gameTxnSummary.setVendorId(tempTxnSummary.getVendorId());
								gameTxnSummary.setWebsiteType(webSiteType.unique());
								gameTxnSummary.setGameType(tempTxnSummary.getGameType());
								gameTxnSummary.setSummaryDate(settleDate);
								gameTxnSummary.setSumBetAmount(betAmount);
								gameTxnSummary.setTurnover(turnover);
								gameTxnSummary.setBetCount(betCount);
								gameTxnSummary.setProfit(profit);
								//GameTxnSummary, AccountSummaryReport, Dashboard的幣別以Account上的為主
								gameTxnSummary.setCurrency(accountCurrencyType.name());
								gameTxnSummary.setProgressBetAmount(progressBetAmount);
								gameTxnSummary.setProgressProfitLoss(progressProfitLoss);
								gameTxnSummary.setAffiliateId(account.getAffiliateId());
								gameTxnSummary.setVipLevel(maxVipLv);

								GameTransactionSummary existingBean = DbExecutor.query(conn -> {
									return GameTransactionSummaryDAO.find(conn, gameTxnSummary);
								});
								if (existingBean != null) {
									gameTxnSummary.setBetCount(gameTxnSummary.getBetCount() + existingBean.getBetCount());
									gameTxnSummary.setSumBetAmount(gameTxnSummary.getSumBetAmount().add(existingBean.getSumBetAmount()));
									gameTxnSummary.setProfit(gameTxnSummary.getProfit().add(existingBean.getProfit()));
									gameTxnSummary.setTurnover(gameTxnSummary.getTurnover().add(existingBean.getTurnover()));
									gameTxnSummary.setProgressBetAmount(gameTxnSummary.getProgressBetAmount().add(existingBean.getProgressBetAmount()));
									gameTxnSummary.setProgressProfitLoss(gameTxnSummary.getProgressProfitLoss().add(existingBean.getProgressProfitLoss()));

									DbExecutor.update(conn ->
										GameTransactionSummaryDAO.update(conn, gameTxnSummary)
									);
								} else {
									DbExecutor.update(conn ->
										GameTransactionSummaryDAO.insert(conn, gameTxnSummary)
									);
								}

								gameTxnSummaryList.add(gameTxnSummary);
								currencySet.add(accountCurrencyType.name());
							}
							// 結算AccountSummaryReport
							if (!gameTxnSummaryList.isEmpty()) {
								processAccountSummaryReport(gameTxnSummaryList);
							}

//							Timestamp summaryStartTime = DateTimeBuilder.localDateTime(settleDate).firstDayOfMonth().withMinTime().toTimestamp();
//							Timestamp summaryEndTime = DateTimeBuilder.localDateTime(settleDate).lastDayOfMonth().withMaxTime().toTimestamp();
//
//							conn.commit();

							DbExecutor.update(conn -> {
								return GameTransactionSummaryHourlyDAO.updateIsSummarized(conn, txnSummaryListForUpdate);
							});

							successCount += txnSummaryListForUpdate.size();
						}

						// 結算Dashboard
						for (AccountSummaryReportType accountSummaryReportType : accountSummaryReportTypes) {
							if (accountSummaryReportType == AccountSummaryReportType.BET) {
//								DashboardDAO
//									.summarizeFromAccountSummaryReport(conn, webSiteType, settleDate, currencySet,
//										accountSummaryReportType, DashboardType.BET);
//								conn.commit();
//
//								DashboardDAO
//									.summarizeFromAccountSummaryReport(conn, webSiteType, settleDate, currencySet,
//										accountSummaryReportType, DashboardType.PROFITLOSS);
//								conn.commit();
							} else if (accountSummaryReportType == AccountSummaryReportType.TURNOVER) {
//								DashboardDAO
//									.summarizeFromAccountSummaryReport(conn, webSiteType, settleDate, currencySet,
//										accountSummaryReportType, DashboardType.TURNOVER);
//								conn.commit();

							}
						}
					}

//					for (Long affiliateId : updateAffiliateSet) {
//
//						AffiliateDAO.modifyUpdatedAttribute(conn, affiliateId, AfUpdatedAttributeType.TXN_DASHBOARD);
//						conn.commit();
//					}

					LogUtils.backOfficeMonitor.info(
						"websiteType: {}, Finishing summarizeGameTransaction. Number of {} gameTransaction is summarized",
						webSiteType.name(), successCount);

				} catch (Exception ex) {
					LogUtils.backOfficeMonitor.error("Fail to sync summarizeGameTransaction, websiteType:{}", webSiteType.name());
					LogUtils.backOfficeMonitor.error(ex.getMessage(), ex);
				}
			});
		}
		GlobalThreadPool.await(taskList);
	}

	private void processAccountSummaryReport(List<GameTransactionSummary> gameTxnSummaryList) {
		try {
			BigDecimal amount = BigDecimal.ZERO;
			BigDecimal profit = BigDecimal.ZERO;
			BigDecimal turnover = BigDecimal.ZERO;

			for (GameTransactionSummary summary : gameTxnSummaryList) {
				amount = amount.add(summary.getSumBetAmount());
				profit = profit.add(summary.getProfit());
				turnover = turnover.add(summary.getTurnover());
			}

			for (AccountSummaryReportType accountSummaryReportType : accountSummaryReportTypes) {
				GameTransactionSummary summary = gameTxnSummaryList.get(0);

				AccountSummaryReport bean = new AccountSummaryReport();
				bean.setUserId(summary.getUserId());
				bean.setWebsiteType(summary.getWebsiteType());
				bean.setPaymentType(accountSummaryReportType.unique());
				bean.setTransactionTime(summary.getSummaryDate());
				if (AccountSummaryReportType.BET == accountSummaryReportType) {
					bean.setAmount(amount);
					bean.setBonus(BigDecimal.ZERO);
					bean.setProfit(profit);
				} else if (AccountSummaryReportType.TURNOVER == accountSummaryReportType) {
					bean.setAmount(turnover);
					bean.setBonus(BigDecimal.ZERO);
					bean.setProfit(BigDecimal.ZERO);
				}
				bean.setAffiliateId(summary.getAffiliateId());
				bean.setCurrency(summary.getCurrency());

				AccountSummaryReport existingBean = DbExecutor.query(conn -> {
					return AccountSummaryReportDAO.find(conn, bean);
				});
				if (existingBean != null) {
					bean.setAmount(bean.getAmount().add(existingBean.getAmount()));
					bean.setBonus(bean.getBonus().add(existingBean.getBonus()));
					bean.setProfit(bean.getProfit().add(existingBean.getProfit()));

					DbExecutor.update(conn -> {
						return AccountSummaryReportDAO.update(conn, bean);
					});
				} else {
					DbExecutor.update(conn -> {
						return AccountSummaryReportDAO.insert(conn, bean);
					});
				}
			}
		} catch (Exception e) {
			LogUtils.backOfficeMonitor.error(e.getMessage(), e);
			GameTransactionSummary summary = gameTxnSummaryList.get(0);
			LogUtils.backOfficeMonitor.error("processAccountSummaryReport Error, websiteType:{}, userId:{}, summaryDate:{}",
					summary.getWebsiteType(),
					summary.getUserId(), summary.getSummaryDate());
		}
	}
}
