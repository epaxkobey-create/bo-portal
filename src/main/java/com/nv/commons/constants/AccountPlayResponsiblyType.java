package com.nv.commons.constants;

import com.nv.commons.model.I18nKeyHolder;
import com.nv.commons.model.UniqueValueHolder;

public enum AccountPlayResponsiblyType implements UniqueValueHolder, I18nKeyHolder {
	WAGER_LIMITS(1, "form.text.backOffice.setting.playResponsibly.wagerLimits", SystemConstants.NO_LIMIT_SETTING) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> AccountUpdateType.WAGER_LIMITS_DAILY;
				case WEEKLY -> AccountUpdateType.WAGER_LIMITS_WEEKLY;
				case MONTHLY -> AccountUpdateType.WAGER_LIMITS_MONTHLY;
			};
		}

		@Override
		public BetRuleType getBetRuleType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> BetRuleType.DAILY_WAGER_LIMIT;
				case WEEKLY -> BetRuleType.WEEKLY_WAGER_LIMIT;
				case MONTHLY -> BetRuleType.MONTHLY_WAGER_LIMIT;
			};
		}
	},
	LOSS_LIMITS(2, "form.text.backOffice.setting.playResponsibly.lossLimits", SystemConstants.NO_LIMIT_SETTING) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> AccountUpdateType.LOSS_LIMITS_DAILY;
				case WEEKLY -> AccountUpdateType.LOSS_LIMITS_WEEKLY;
				case MONTHLY -> AccountUpdateType.LOSS_LIMITS_MONTHLY;
			};
		}

		@Override
		public BetRuleType getBetRuleType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> BetRuleType.DAILY_LOSS_LIMIT;
				case WEEKLY -> BetRuleType.WEEKLY_LOSS_LIMIT;
				case MONTHLY -> BetRuleType.MONTHLY_LOSS_LIMIT;
			};
		}
	},
	SESSION_EXPIRY(3, "form.text.backOffice.setting.playResponsibly.sessionExpiry", SessionExpiryType.NONE.unique()) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return AccountUpdateType.SESSION_EXPIRY;
		}
	},
	SELF_EXCLUSION(4, "form.text.backOffice.setting.playResponsibly.selfExclusion",
		SelfExclusionType.NO_EXCLUSION.unique()) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return AccountUpdateType.SELF_EXCLUSION;
		}
	},
	REALITY_CHECK(5, "form.text.backOffice.setting.playResponsibly.realityCheck", RealityCheckType.CHECK_15M.unique()) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return AccountUpdateType.REALITY_CHECK;
		}
	},
	DEPOSIT_LIMITS(6, "form.text.backOffice.setting.playResponsibly.depositLimits", SystemConstants.NO_LIMIT_SETTING) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> AccountUpdateType.DEPOSIT_LIMITS_DAILY;
				case WEEKLY -> AccountUpdateType.DEPOSIT_LIMITS_WEEKLY;
				case MONTHLY -> AccountUpdateType.DEPOSIT_LIMITS_MONTHLY;
			};
		}

		@Override
		public PaymentRuleType getPaymentRuleType(AccountPlayResponsiblyPeriodType periodType) {
			return switch (periodType) {
				case DAILY -> PaymentRuleType.DAILY_DEPOSIT_LIMIT;
				case WEEKLY -> PaymentRuleType.WEEKLY_DEPOSIT_LIMIT;
				case MONTHLY -> PaymentRuleType.MONTHLY_DEPOSIT_LIMIT;
			};
		}
	},
	ANNUAL_REMINDER(7, "form.text.backOffice.setting.playResponsibly.annualReminder",
		BinaryStatusType.INACTIVE.unique()),
	ACCOUNT_REVIEW_REMINDER(8, "form.text.backOffice.setting.playResponsibly.accountReviewReminder",
		AccountReviewReminderType.CHECK_6M.unique()) {
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return AccountUpdateType.ACCOUNT_REVIEW_REMINDER;
		}
	},
	TIME_SPENT_LIMIT(9, "form.text.backOffice.setting.playResponsibly.timeSpentLimit",
		TimeSpentLimitType.NO_TIME_SPENT_LIMIT.unique()){
		@Override
		public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
			return AccountUpdateType.TIME_SPENT_LIMIT;
		}
	};

	private final int value;
	private final String name;
	private final int defaultValue;

	public static AccountPlayResponsiblyType getInstanceOf(int value) {
		for (AccountPlayResponsiblyType e : AccountPlayResponsiblyType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	AccountPlayResponsiblyType(int value, String name, int defaultValue) {
		this.value = value;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public int unique() {
		return value;
	}

	public String getI18nKey() {
		return name;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public AccountUpdateType getAccountUpdateType(AccountPlayResponsiblyPeriodType periodType) {
		throw new UnsupportedOperationException();
	}

	public BetRuleType getBetRuleType(AccountPlayResponsiblyPeriodType periodType) {
		throw new UnsupportedOperationException();
	}

	public PaymentRuleType getPaymentRuleType(AccountPlayResponsiblyPeriodType periodType) {
		throw new UnsupportedOperationException();
	}
}
