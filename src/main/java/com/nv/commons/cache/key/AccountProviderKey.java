package com.nv.commons.cache.key;

import java.util.Objects;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class AccountProviderKey implements Comparable<AccountProviderKey> {

	private int websiteType;

	private int providerId;

	private String userId;

	public AccountProviderKey(int websiteType, int providerId, String userId) {
		this.websiteType = websiteType;
		this.providerId = providerId;
		this.userId = userId;

	}

	@Override
	public int compareTo(AccountProviderKey o) {
		return new CompareToBuilder().append(this.websiteType, o.websiteType)
			.append(this.providerId, o.providerId).append(this.userId, o.userId)
			.toComparison();
	}

	@Override
	public int hashCode() {
		return Objects.hash(websiteType, providerId, userId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AccountProviderKey that = (AccountProviderKey) o;
		return websiteType == that.websiteType &&
			providerId == that.providerId &&
			Objects.equals(userId, that.userId);
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public int getProviderId() {
		return providerId;
	}

	public String getUserId() {
		return userId;
	}


}
