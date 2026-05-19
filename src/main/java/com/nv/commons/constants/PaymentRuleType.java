package com.nv.commons.constants;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.JsonGenerateProcessor;

public enum PaymentRuleType {
	DAILY_DEPOSIT_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.DEPOSIT_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.DAILY;
		}
	},
	WEEKLY_DEPOSIT_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.DEPOSIT_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.WEEKLY;
		}
	},
	MONTHLY_DEPOSIT_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.DEPOSIT_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.MONTHLY;
		}
	},
	;

	public static final PaymentRuleType[] VALUES = PaymentRuleType.values();
	public static final PaymentRuleType[] DEPOSIT_RULE_VALUES = {
		DAILY_DEPOSIT_LIMIT, WEEKLY_DEPOSIT_LIMIT, MONTHLY_DEPOSIT_LIMIT
	};

	public abstract AccountPlayResponsiblyType getAccountPlayResponsiblyType();
	public abstract AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType();

	public static Map<String, String> getPaymentRuleViolationData(PaymentRuleType ruleType,
		BigDecimal limit, BigDecimal usage, BigDecimal remain) {

		String status = "";
		String message = "Deposit Limit Exceeded";
		String messageKey = "";
		String data;

		switch (ruleType) {
			case DAILY_DEPOSIT_LIMIT -> {
				status = APIResponseType.DAILY_DEPOSIT_LIMIT_ERROR.getCode();
				messageKey = "msg.error.account.playResponsibly.limit.daily.deposit.exceed";
			}
			case WEEKLY_DEPOSIT_LIMIT -> {
				status = APIResponseType.WEEKLY_DEPOSIT_LIMIT_ERROR.getCode();
				messageKey = "msg.error.account.playResponsibly.limit.weekly.deposit.exceed";
			}
			case MONTHLY_DEPOSIT_LIMIT -> {
				status = APIResponseType.MONTHLY_DEPOSIT_LIMIT_ERROR.getCode();
				messageKey = "msg.error.account.playResponsibly.limit.monthly.deposit.exceed";
			}
		}

		JsonGenerateProcessor processor = jGenerator -> {

			jGenerator.writeNumberField("maxLimitAmount", limit);
			jGenerator.writeNumberField("currentLimitAmount", usage);
			jGenerator.writeNumberField("remainLimitAmount", remain);
		};
		data = JSONUtils.getJSONString(processor);

		Map<String, String> result = new HashMap<>();
		result.put("status", status);
		result.put("message", message);
		result.put("messageKey", messageKey);
		result.put("data", data);

		return result;
	}
}
