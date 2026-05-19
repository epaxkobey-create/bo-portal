package com.nv.commons.dto;

import java.math.BigDecimal;

import com.nv.commons.utils.BigDecimalUtils;
public class PaymentMethod {

	private int paymentMethodType;
	
	private int bankType;
	
	private String bankGroup;
	
	private BigDecimal fees = BigDecimal.ZERO;

	private BigDecimal fixedFee = BigDecimal.ZERO;

//	public BigDecimal calPaymentFee(BigDecimal paymentAmount) {
//		BigDecimal amountFee = BigDecimal.ZERO;
//
//		if (fees != null && BigDecimalUtils.compareTo(fees, BigDecimal.ZERO) > 0) {
//			amountFee = paymentAmount.multiply(fees);
//		}
//
//		if (fixedFee != null && BigDecimalUtils.compareTo(fixedFee, BigDecimal.ZERO) > 0) {
//			amountFee = amountFee.add(fixedFee);
//		}
//
//		return amountFee;
//	}

	public int getPaymentMethodType() {
		return paymentMethodType;
	}

	public void setPaymentMethodType(int paymentMethodType) {
		this.paymentMethodType = paymentMethodType;
	}

	public int getBankType() {
		return bankType;
	}

	public void setBankType(int bankType) {
		this.bankType = bankType;
	}

	public String getBankGroup() {
		return bankGroup;
	}

	public void setBankGroup(String bankGroup) {
		this.bankGroup = bankGroup;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public void setFees(BigDecimal fees) {
		this.fees = fees;
	}

	public BigDecimal getFixedFee() {
		return fixedFee;
	}

	public void setFixedFee(BigDecimal fixedFee) {
		this.fixedFee = fixedFee;
	}
}
