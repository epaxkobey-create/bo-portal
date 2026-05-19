package com.nv.commons.constants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/**
 此類別主要負責單向加密，特色是沒有Key
 author: Alan
 */
public enum MessageDigestType {

	SHA1("SHA1") {
	},
	MD5("MD5") {
	},
	;

	protected String transformation;

	MessageDigestType(String transformation) {
		this.transformation = transformation;
	}

	public String encrypt(String content, EncodeType encodeType) {
		return encrypt(content.getBytes(StandardCharsets.UTF_8), encodeType);
	}

	public String encrypt(byte[] content, EncodeType encodeType) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(this.transformation);
			messageDigest.update(content);

			byte[] encryptedBytes = messageDigest.digest();
			if (encodeType != null) {
				return encodeType.encodeToString(encryptedBytes);
			}
			return new String(encryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String encrypt(String content) {
		return encrypt(content, null);
	}

}
