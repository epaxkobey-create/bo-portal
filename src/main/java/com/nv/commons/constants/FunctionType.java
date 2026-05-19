package com.nv.commons.constants;

// todo @Ben 最後再整理權限部分
public enum FunctionType {
	HOLD_WITHDRAWAL(4),
	MEMBER_PROFILE(10),
	No13(13),
	No15(15),
	No16(16),
	REJECT_DEPOSIT(17),
	No18(18),
	No20(20),
	No21(21),
	REJECT_WITHDRAWAL(22),
	No23(23),
	VERIFY_WITHDRAWAL(24),
	No26(26),
	No28(28),
	MEMBER_REPORT_VIEW_BONUS(99),
	DEPOSIT_ATTACHMENT(132),
	VERIFY_ACCOUNT(174),
	APPROVE_VERIFY_ACCOUNT(175),
	REJECT_VERIFY_ACCOUNT(208),
	HOLD_VERIFY_ACCOUNT(209),
	COMPLETE_BONUS(211),
	MEMBER_PROFILE_EDIT_RISK_REMARK(264),
	;

	private final int level;

	FunctionType(int level) {
		this.level = level;
	}

	public int unique() {
		return level;
	}
}
