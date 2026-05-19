package com.nv.commons.constants;

public enum PGCallBackStatusType {
	FAIL(MoneyTransactionStatusType.CLOSE),
	SUCCESS(MoneyTransactionStatusType.CONFIRMED),
	PENDING(MoneyTransactionStatusType.NEW);

	private final MoneyTransactionStatusType statusType;

	PGCallBackStatusType(MoneyTransactionStatusType statusType) {

		this.statusType = statusType;
	}

	public MoneyTransactionStatusType getStatusType() {
		return statusType;
	}

}
