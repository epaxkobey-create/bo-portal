package com.nv.commons.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.GameTxnSummaryType;

public class GameTransaction {

	@Column(name = "ID")
	private long id;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "VIP_LEVEL")
	private int vipLevel;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "VENDOR_ID")
	private int vendorId;

	@Column(name = "TXN_TIME")
	private Timestamp txnTime;

	@Column(name = "BET_AMOUNT")
	private BigDecimal betAmount;

	@Column(name = "WIN_AMOUNT")
	private BigDecimal winAmount;

	@Column(name = "CURRENCY")
	private String currency;

	@Column(name = "VENDOR_TXN_ID")
	private String vendorTxnId;

	@Column(name = "SETTLE_TIME")
	private Timestamp settleTime;

	@Column(name = "SYNC_START_TIME")
	private Timestamp syncStartTime;

	@Column(name = "SYNC_END_TIME")
	private Timestamp syncEndTime;

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "GAME_TYPE")
	private int gameType;

	@Column(name = "TURNOVER")
	private BigDecimal turnover;

	@Column(name = "TXN_STATUS")
	private String txnStatus;

	@Column(name = "REAL_BET_AMOUNT")
	private BigDecimal realBetAmount;

	@Column(name = "ADJUST_AMOUNT")
	private BigDecimal adjustAmount;

	@Column(name = "VENDOR_NAME")
	private String vendorName;

	@Column(name = "GAME_ID")
	private Integer gameId;

	@Column(name = "BET_COUNT")
	private int betCount;

	@Column(name = "IS_TURNOVER_SUMMARIZED")
	private int isTurnoverSummarized;

	@Column(name = "PROFIT_LOSS")
	private BigDecimal profitLoss;

	@Column(name = "SYSTEM_TXN_STATUS")
	private int systemTxnStatus;
	
	@Column(name = "PROGRESS_BET_AMOUNT")
	private BigDecimal progressBetAmount;
	
	@Column(name = "PROGRESS_PROFIT_LOSS")
	private BigDecimal progressProfitLoss;

	@Column(name = "IS_GAME_TXN_SUMMARIZED")
	private int isGameTxnSummarized;

	@Column(name = "ODDS_TYPE")
	private int oddsType;

	@Column(name = "ODDS")
	private BigDecimal odds;

	@Column(name = "bonus_turnover_id")
	private long bonusTurnoverId;

	private String markId;

	private String gameInfoJson;

	private boolean existDB;

	@Column(name="FC_RECORD_ID")
	private String fcRecordId;

	// ? todo what is this?
//	@Column(name = "IS_CONVERT_POINT")
//	private int isConvertPoint;

	public GameTransaction() {
	}

	public GameTransaction(BigDecimal profitLoss, BigDecimal turnover) {
		this.profitLoss = profitLoss;
		this.turnover = turnover;
	}

	public GameTransaction(AccountProvider accountProvider, Vendor vendor, Game game) {
		this.userId = accountProvider.getUserId();
		this.websiteType = accountProvider.getWebsiteType();
		this.currency = CurrencyType.getInstance(accountProvider.getCurrencyTypeId()).name();
		this.bonusTurnoverId = accountProvider.getBonusTurnoverId();
		this.vendorId = vendor.getId();
		this.vendorName = vendor.getName();
		this.gameId = game.getId();
		this.gameType = game.getGameType();
		this.vipLevel = 1;
		this.isGameTxnSummarized = GameTxnSummaryType.NOT_SUMMARIZED.unique();

//		GameTxnConvertPointType gameTxnConvertPointType = GameTxnConvertPointType.NOT_CONVERT;
//
//		if (VipSystemBO.isAllowVipPointSystem(this.websiteType, accountProvider.getCurrencyTypeId())) {
//			gameTxnConvertPointType = GameTxnConvertPointType.NEED_CONVERT;
//		}
//
//		this.isConvertPoint = gameTxnConvertPointType.unique();
	}

	public String getReferenceKey() {
		return getReferenceKey(String.valueOf(websiteType), String.valueOf(vendorId), vendorTxnId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GameTransaction gameTransaction = (GameTransaction) o;
		return websiteType == gameTransaction.getWebsiteType() &&
			vendorId == gameTransaction.getVendorId() &&
			vendorTxnId.equals(gameTransaction.getVendorTxnId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, vendorId, vendorTxnId);
	}

	public static String getReferenceKey(String websiteTypeId, String vendorId, String vendorTxnId) {
		return String.join("#@", websiteTypeId, vendorId, vendorTxnId);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public Timestamp getTxnTime() {
		return txnTime;
	}

	public void setTxnTime(Timestamp txnTime) {
		this.txnTime = txnTime;
	}

	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	public BigDecimal getWinAmount() {
		return winAmount;
	}

	public void setWinAmount(BigDecimal winAmount) {
		this.winAmount = winAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getVendorTxnId() {
		return vendorTxnId;
	}

	public void setVendorTxnId(String vendorTxnId) {
		this.vendorTxnId = vendorTxnId;
	}

	public Timestamp getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(Timestamp settleTime) {
		this.settleTime = settleTime;
	}

	public Timestamp getSyncStartTime() {
		return syncStartTime;
	}

	public void setSyncStartTime(Timestamp syncStartTime) {
		this.syncStartTime = syncStartTime;
	}

	public Timestamp getSyncEndTime() {
		return syncEndTime;
	}

	public void setSyncEndTime(Timestamp syncEndTime) {
		this.syncEndTime = syncEndTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public BigDecimal getTurnover() {
		return turnover;
	}

	public void setTurnover(BigDecimal turnover) {
		this.turnover = turnover;
	}

	public String getTxnStatus() {
		return txnStatus;
	}

	public void setTxnStatus(String txnStatus) {
		this.txnStatus = txnStatus;
	}

	public BigDecimal getRealBetAmount() {
		return realBetAmount;
	}

	public void setRealBetAmount(BigDecimal realBetAmount) {
		this.realBetAmount = realBetAmount;
	}

	public BigDecimal getAdjustAmount() {
		return adjustAmount;
	}

	public void setAdjustAmount(BigDecimal adjustAmount) {
		this.adjustAmount = adjustAmount;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public Integer getGameId() {
		return gameId;
	}

	public void setGameId(Integer gameId) {
		this.gameId = gameId;
	}

	public int getBetCount() {
		return betCount;
	}

	public void setBetCount(int betCount) {
		this.betCount = betCount;
	}

	public int getIsTurnoverSummarized() {
		return isTurnoverSummarized;
	}

	public void setIsTurnoverSummarized(int isTurnoverSummarized) {
		this.isTurnoverSummarized = isTurnoverSummarized;
	}

	public BigDecimal getProfitLoss() {
		return profitLoss;
	}

	public void setProfitLoss(BigDecimal profitLoss) {
		this.profitLoss = profitLoss;
	}

	public int getSystemTxnStatus() {
		return systemTxnStatus;
	}

	public void setSystemTxnStatus(int systemTxnStatus) {
		this.systemTxnStatus = systemTxnStatus;
	}

	public BigDecimal getProgressBetAmount() {
		return progressBetAmount;
	}

	public void setProgressBetAmount(BigDecimal progressBetAmount) {
		this.progressBetAmount = progressBetAmount;
	}

	public BigDecimal getProgressProfitLoss() {
		return progressProfitLoss;
	}

	public void setProgressProfitLoss(BigDecimal progressProfitLoss) {
		this.progressProfitLoss = progressProfitLoss;
	}

	public int getIsGameTxnSummarized() {
		return isGameTxnSummarized;
	}

	public void setIsGameTxnSummarized(int isGameTxnSummarized) {
		this.isGameTxnSummarized = isGameTxnSummarized;
	}

	public int getOddsType() {
		return oddsType;
	}

	public void setOddsType(int oddsType) {
		this.oddsType = oddsType;
	}

	public BigDecimal getOdds() {
		return odds;
	}

	public void setOdds(BigDecimal odds) {
		this.odds = odds;
	}

	public long getBonusTurnoverId() {
		return bonusTurnoverId;
	}

	public void setBonusTurnoverId(long bonusTurnoverId) {
		this.bonusTurnoverId = bonusTurnoverId;
	}

	public String getMarkId() {
		return markId;
	}

	public void setMarkId(String markId) {
		this.markId = markId;
	}

	public String getGameInfoJson() {
		return gameInfoJson;
	}

	public void setGameInfoJson(String gameInfoJson) {
		this.gameInfoJson = gameInfoJson;
	}

	public boolean isExistDB() {
		return existDB;
	}

	public void setExistDB(boolean existDB) {
		this.existDB = existDB;
	}

	public String getFcRecordId() {
		return fcRecordId;
	}

	public void setFcRecordId(String fcRecordId) {
		this.fcRecordId = fcRecordId;
	}
}
