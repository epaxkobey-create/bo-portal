package com.nv.commons.dto;

import java.math.BigDecimal;
import java.util.List;

// AccountContactInfo 未搬
public class AccountAttach extends Account {

	private List<AccountProvider> accountBonusProviderList;

	private BigDecimal walletBalance = BigDecimal.ZERO;

	private String bonusCode;

	private List<AccountDocument> accountDocumentList;

	private List<AccountRemark> accountRemarkList;

	private AccountCard accountCard;

	private List<AccountBank> accountBank;

	private AccountStats accountStats;

	public List<AccountProvider> getAccountBonusProviderList() {
		return accountBonusProviderList;
	}

	public void setAccountBonusProviderList(List<AccountProvider> accountBonusProviderList) {
		this.accountBonusProviderList = accountBonusProviderList;
	}

	public BigDecimal getWalletBalance() {
		return walletBalance;
	}

	public void setWalletBalance(BigDecimal walletBalance) {
		this.walletBalance = walletBalance;
	}

	public String getBonusCode() {
		return bonusCode;
	}

	public void setBonusCode(String bonusCode) {
		this.bonusCode = bonusCode;
	}

	public List<AccountDocument> getAccountDocumentList() {
		return accountDocumentList;
	}

	public void setAccountDocumentList(List<AccountDocument> accountDocumentList) {
		this.accountDocumentList = accountDocumentList;
	}

	public List<AccountRemark> getAccountRemarkList() {
		return accountRemarkList;
	}

	public void setAccountRemarkList(List<AccountRemark> accountRemarkList) {
		this.accountRemarkList = accountRemarkList;
	}

	public AccountCard getAccountCard() {
		return accountCard;
	}

	public void setAccountCard(AccountCard accountCard) {
		this.accountCard = accountCard;
	}

	public AccountStats getAccountStats() {
		return accountStats;
	}

	public void setAccountStats(AccountStats accountStats) {
		this.accountStats = accountStats;
	}

	public List<AccountBank> getAccountBank() {
		return accountBank;
	}

	public void setAccountBank(List<AccountBank> accountBank) {
		this.accountBank = accountBank;
	}
}
