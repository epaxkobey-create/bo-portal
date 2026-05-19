package com.nv.commons.constants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.UniqueValueHolder;
import com.nv.commons.utils.JSONUtils;

public enum AccountUpdateType implements UniqueValueHolder {
	ALLOW_GAME_TYPE(5, "allowGameType", "Allow Game Type",
		"form.text.account.allowGameType"),
	STATUS(7, "status", "Status", "form.text.backOffice.status"),
	USER_REMARK(8, "remark", "User Remark", "form.text.backOffice.remark"),
	EMAIL(10, "email", "Email", "form.text.account.email"),
	AUTO_VERIFICATION(31, "autoVerification", "Auto Verification",
		"form.text.account.autoVerification"),
	UPDATE_PASSWORD(32, "password", "Update Password",
		"form.text.backOffice.password"),
	ADDRESS(49, "address", "Address", "form.text.account.address"),
	EMAIL_STATUS(54, "emailStatus", "Email Status", "form.text.account.emailStatus"),
	DOCUMENT_FRONT_PHOTO(55, "documentPhotoFront", "Document Front Photo",
		"form.text.af.kyc.status.documentPhotoFront-Side"),
	DOCUMENT_BACK_PHOTO(56, "documentPhotoBack", "Document Back Photo",
		"form.text.af.kyc.status.documentPhotoBack-Side"),
	DOCUMENT_ADDRESS_PHOTO(57, "documentPhotoAddress", "Document Address Photo",
		"form.text.af.kyc.status.documentPhotoAddress"),
	KYC_VERIFICATION_STATUS(58, "verificationStatus", "Verification Status", "form.text.af.kyc.verificationStatus"),
	KYC_VERIFICATION_REMARK(59, "verificationRemark", "Verification Remark", "form.text.af.kyc.verificationRemark"),
	DOCUMENT_NO(60, "documentNo", "Document No", "form.text.backOffice.documentNo"),
	DOCUMENT_EXPIRY_DATE(61, "expiryDate", "Expiry Date", "form.text.backOffice.expiryDate"),
	FULL_NAME(62, "fullName", "Full Name", "form.text.backOffice.fullName"),
	DOB(63, "dob", "DOB", "form.text.account.dob"),
	WAGER_LIMITS_DAILY(64, "wagerLimitsDaily", "Wager Limit (Daily)",
		"form.text.backOffice.setting.playResponsibly.wagerLimits.daily"),
	LOSS_LIMITS_DAILY(65, "lossLimitsDaily", "Loss Limit (Daily)",
		"form.text.backOffice.setting.playResponsibly.lossLimits.daily"),
	SESSION_EXPIRY(66, "sessionExpiry", "Session Expiry",
		"form.text.backOffice.setting.playResponsibly.sessionExpiry"),
	SELF_EXCLUSION(67, "selfExclusion", "Self Exclusion",
		"form.text.backOffice.setting.playResponsibly.selfExclusion"),
	REALITY_CHECK(68, "realityCheck", "Reality Check",
		"form.text.backOffice.setting.playResponsibly.realityCheck"),
	CREDIT_CARD(69, "creditCard", "Credit Card",
		"form.text.account.card.creditCard"),
	WAGER_LIMITS_WEEKLY(70, "wagerLimitsWeekly", "Wager Limit (Weekly)",
		"form.text.backOffice.setting.playResponsibly.wagerLimits.weekly"),
	WAGER_LIMITS_MONTHLY(71, "wagerLimitsMonthly", "Wager Limit (Monthly)",
		"form.text.backOffice.setting.playResponsibly.wagerLimits.monthly"),
	LOSS_LIMITS_WEEKLY(72, "lossLimitsWeekly", "Loss Limit (Weekly)",
		"form.text.backOffice.setting.playResponsibly.lossLimits.weekly"),
	LOSS_LIMITS_MONTHLY(73, "lossLimitsMonthly", "Loss Limit (Monthly)",
		"form.text.backOffice.setting.playResponsibly.lossLimits.monthly"),
	DEPOSIT_LIMITS_DAILY(74, "depositLimitsDaily", "Deposit Limit (Daily)",
		"form.text.backOffice.setting.playResponsibly.depositLimits.daily"),
	DEPOSIT_LIMITS_WEEKLY(75, "depositLimitsWeekly", "Deposit Limit (Weekly)",
		"form.text.backOffice.setting.playResponsibly.depositLimits.weekly"),
	DEPOSIT_LIMITS_MONTHLY(76, "depositLimitsMonthly", "Deposit Limit (Monthly)",
		"form.text.backOffice.setting.playResponsibly.depositLimits.monthly"),
	ACCOUNT_REVIEW_REMINDER(77, "accountReviewReminder", "Account Review Reminder",
		"form.text.backOffice.setting.playResponsibly.accountReviewReminder"),
	BANK(78, "bank", "Bank", "form.text.account.card.bank"),
	TIME_SPENT_LIMIT(79, "timeSpentLimit", "Time Spent Limit (Daily)",
		"form.text.backOffice.setting.playResponsibly.timeSpentLimitDaily");

	private static final String json;

	static {
		JsonGenerator jGenerator = null;
		StringWriter out = new StringWriter();
		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();

			for (AccountUpdateType accountUpdateType : AccountUpdateType.values()) {
				jGenerator.writeObjectFieldStart(String.valueOf(accountUpdateType.unique()));
				jGenerator.writeStringField("name", accountUpdateType.name());
				jGenerator.writeStringField("getName", accountUpdateType.getName());
				jGenerator.writeStringField("getDisplayName", accountUpdateType.getDisplayName());
				jGenerator.writeStringField("getFullName", accountUpdateType.getFullName());
				jGenerator.writeEndObject();
			}

			jGenerator.writeEndObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		json = out.toString();
	}

	public static String toJsonString() {
		return json;
	}

	private final int value;
	private final String name;
	private final String displayName;
	private final String fullName;

	public static final AccountUpdateType[] VALUES = AccountUpdateType.values();

	public static AccountUpdateType getInstanceOf(int value) {
		for (AccountUpdateType e : AccountUpdateType.values()) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	public String getFullName(LangMessage langMessage) {
		if (langMessage == null) {
			langMessage = LanguageType.ENGLISH.getLangMessage();
		}

		return langMessage.get(fullName);
	}

	AccountUpdateType(int value, String name, String displayName, String fullName) {
		this.value = value;
		this.name = name;
		this.displayName = displayName;
		this.fullName = fullName;
	}

	public int unique() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFullName() {
		return fullName;
	}

	public static List<AccountUpdateType> getAll() {
		return List.of(AccountUpdateType.VALUES);
	}
}
