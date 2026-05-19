package com.nv.commons.cache.key;

import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationContext;

public class AccountContactInfoKey {

	private int contactType;

	private int contentNo;

	public AccountContactInfoKey(int contactType, int contentNo) {
		this.contactType = contactType;
		this.contentNo = contentNo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AccountContactInfoKey that = (AccountContactInfoKey) o;
		return contactType == that.contactType &&
			contentNo == that.contentNo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(contactType, contentNo);
	}

	public int getContactType() {
		return contactType;
	}

	public int getContentNo() {
		return contentNo;
	}

	@Override
	public String toString() {
		return contactType + "_" + contentNo;
	}

	/**
	 *
	 */
	public static class KeyDeserializer extends com.fasterxml.jackson.databind.KeyDeserializer {

		@Override
		public AccountContactInfoKey deserializeKey(String key, DeserializationContext deserializationContext) {
			final String[] strings = key.split("_");
			return new AccountContactInfoKey(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
		}
	}
}
