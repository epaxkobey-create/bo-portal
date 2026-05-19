package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author Luke Chi
 */
public class AccountWithProviderAccount extends MemberListReport {

	private int providerId;

	private String providerAccount;

	private Timestamp providerCreateTime;

	private String providerExtraData;

	public int getProviderId() {
		return providerId;
	}

	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}

	public String getProviderAccount() {
		return providerAccount;
	}

	public void setProviderAccount(String providerAccount) {
		this.providerAccount = providerAccount;
	}

	public Timestamp getProviderCreateTime() {
		return providerCreateTime;
	}

	public void setProviderCreateTime(Timestamp providerCreateTime) {
		this.providerCreateTime = providerCreateTime;
	}

	public String getProviderExtraData() {
		return providerExtraData;
	}

	public void setProviderExtraData(String providerExtraData) {
		this.providerExtraData = providerExtraData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountWithProviderAccount that = (AccountWithProviderAccount) o;
		return providerId == that.providerId &&
				Objects.equals(providerAccount, that.providerAccount) &&
				Objects.equals(providerCreateTime, that.providerCreateTime) &&
				Objects.equals(providerExtraData, that.providerExtraData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(providerId, providerAccount, providerCreateTime, providerExtraData);
	}
}
