package com.nv.commons.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Map;

import com.nv.commons.constants.SystemTxnStatusType;
import com.nv.commons.dto.GameTransaction;

public class GameTransactionUtils {

	private final static int RESERVED_DAYS = 7;
//	private final static int EXPIRED_TO_HISTORY = 30;

//	private final static Timestamp LAST_BET_TIME = DateTimeBuilder.localDateTime("2022/09/01", "yyyy/MM/dd")
//		.withMinTime().toTimestamp();




//	public static boolean isAmountChange(GameTransaction dbTxn, GameTransaction apiTxn) {
//
//		return CompareUtils.different(dbTxn.getBetAmount(), apiTxn.getBetAmount()) ||
//			CompareUtils.different(dbTxn.getWinAmount(), apiTxn.getWinAmount()) ||
//			CompareUtils.different(dbTxn.getTurnover(), apiTxn.getTurnover()) ||
//			CompareUtils.different(dbTxn.getProfitLoss(), apiTxn.getProfitLoss()) ||
//			CompareUtils.different(dbTxn.getRealBetAmount(), apiTxn.getRealBetAmount()) ||
//			CompareUtils.different(dbTxn.getAdjustAmount(), apiTxn.getAdjustAmount()) ||
//			CompareUtils.different(dbTxn.getProgressBetAmount(), apiTxn.getProgressBetAmount()) ||
//			CompareUtils.different(dbTxn.getProgressProfitLoss(), apiTxn.getProgressProfitLoss());
//	}


	public static Timestamp getReservedTime() {
		return DateTimeBuilder.localDateTime().withMinTime().minusDays(RESERVED_DAYS).toTimestamp();
	}


//	public static Timestamp getLastBetTimeOfHistory() {
//		return LAST_BET_TIME;
//	}


//	public static BigDecimal modifyAmount(BigDecimal amount, BigDecimal providerConversion,
//		BigDecimal systemConversion) {
//		return amount.multiply(providerConversion).divide(systemConversion, 2, RoundingMode.HALF_UP);
//	}

//	public static String getDBSource(Timestamp searchTime, String defaultSource) {
//		if (checkInHistory(searchTime)) {
//			return " gametransaction_hist ";
//		}
//
//		if (checkOverLastBetTime(searchTime)) {
//			return " gametransactionhistory ";
//		}
//
//		return defaultSource;
//	}


//	public static boolean checkInHistory(Timestamp searchTime) {
//		return searchTime.getTime() < GameTransactionUtils.getReservedTime().getTime()
//			&& searchTime.getTime() >= GameTransactionUtils.getLastBetTimeOfHistory().getTime();
//	}
//
//	public static boolean checkOverLastBetTime(Timestamp searchTime) {
//		return searchTime.getTime() < GameTransactionUtils.getLastBetTimeOfHistory().getTime();
//	}

}
