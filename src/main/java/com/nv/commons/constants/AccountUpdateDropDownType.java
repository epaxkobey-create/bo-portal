package com.nv.commons.constants;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum AccountUpdateDropDownType {
	UK(new AccountUpdateType[] {
		AccountUpdateType.USER_REMARK,
		AccountUpdateType.EMAIL_STATUS,
		AccountUpdateType.UPDATE_PASSWORD,
		AccountUpdateType.ADDRESS,
		AccountUpdateType.STATUS,
		AccountUpdateType.DOCUMENT_NO,
		AccountUpdateType.FULL_NAME,
		AccountUpdateType.DOB,
		AccountUpdateType.DOCUMENT_EXPIRY_DATE,
		AccountUpdateType.DOCUMENT_FRONT_PHOTO,
		AccountUpdateType.DOCUMENT_BACK_PHOTO,
		AccountUpdateType.DOCUMENT_ADDRESS_PHOTO,
		AccountUpdateType.KYC_VERIFICATION_STATUS,
		AccountUpdateType.KYC_VERIFICATION_REMARK,
		AccountUpdateType.SESSION_EXPIRY,
		AccountUpdateType.SELF_EXCLUSION,
		AccountUpdateType.WAGER_LIMITS_DAILY,
		AccountUpdateType.WAGER_LIMITS_WEEKLY,
		AccountUpdateType.WAGER_LIMITS_MONTHLY,
		AccountUpdateType.LOSS_LIMITS_DAILY,
		AccountUpdateType.LOSS_LIMITS_WEEKLY,
		AccountUpdateType.LOSS_LIMITS_MONTHLY,
		AccountUpdateType.REALITY_CHECK,
		AccountUpdateType.BANK,
		AccountUpdateType.DEPOSIT_LIMITS_DAILY,
		AccountUpdateType.DEPOSIT_LIMITS_WEEKLY,
		AccountUpdateType.DEPOSIT_LIMITS_MONTHLY,
		AccountUpdateType.ACCOUNT_REVIEW_REMINDER,
	}),

	CY(new AccountUpdateType[] {
		AccountUpdateType.USER_REMARK,
		AccountUpdateType.EMAIL_STATUS,
		AccountUpdateType.UPDATE_PASSWORD,
		AccountUpdateType.ADDRESS,
		AccountUpdateType.STATUS,
		AccountUpdateType.DOCUMENT_NO,
		AccountUpdateType.FULL_NAME,
		AccountUpdateType.DOB,
		AccountUpdateType.DOCUMENT_EXPIRY_DATE,
		AccountUpdateType.DOCUMENT_FRONT_PHOTO,
		AccountUpdateType.DOCUMENT_BACK_PHOTO,
		AccountUpdateType.DOCUMENT_ADDRESS_PHOTO,
		AccountUpdateType.KYC_VERIFICATION_STATUS,
		AccountUpdateType.KYC_VERIFICATION_REMARK,
		AccountUpdateType.SELF_EXCLUSION,
		AccountUpdateType.WAGER_LIMITS_DAILY,
		AccountUpdateType.LOSS_LIMITS_DAILY,
		AccountUpdateType.TIME_SPENT_LIMIT,
		AccountUpdateType.CREDIT_CARD
	});

	private AccountUpdateType[] updateTypes;

	AccountUpdateDropDownType(AccountUpdateType[] updateTypes) {
		this.updateTypes = updateTypes;
	}

	public List<AccountUpdateType> getAccountUpdateTypeList() {
		return Arrays.stream(updateTypes)
			.sorted(Comparator.comparing((t) -> (t.getFullName(LanguageType.ENGLISH.getLangMessage()))))
			.collect(Collectors.toList());
	}
}
