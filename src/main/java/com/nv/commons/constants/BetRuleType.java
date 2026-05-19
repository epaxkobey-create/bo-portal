package com.nv.commons.constants;

public enum BetRuleType {
	DAILY_WAGER_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.WAGER_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.DAILY;
		}
	},
	WEEKLY_WAGER_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.WAGER_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.WEEKLY;
		}
	},
	MONTHLY_WAGER_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.WAGER_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.MONTHLY;
		}
	},
	DAILY_LOSS_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.LOSS_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.DAILY;
		}
	},
	WEEKLY_LOSS_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.LOSS_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.WEEKLY;
		}
	},
	MONTHLY_LOSS_LIMIT {
		@Override
		public AccountPlayResponsiblyType getAccountPlayResponsiblyType() {
			return AccountPlayResponsiblyType.LOSS_LIMITS;
		}
		@Override
		public AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType() {
			return AccountPlayResponsiblyPeriodType.MONTHLY;
		}
	},
	;

	public abstract AccountPlayResponsiblyType getAccountPlayResponsiblyType();
	public abstract AccountPlayResponsiblyPeriodType getAccountPlayResponsiblyPeriodType();
}
