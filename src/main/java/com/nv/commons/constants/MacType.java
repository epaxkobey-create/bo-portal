package com.nv.commons.constants;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 此類別主要負責單向加密，但消息摘要使用單一密鑰的雙向加密，通信雙方必須持有相同的秘鑰
 * Message Authentication Code
 * <p>
 * author: Alan
 */
public enum MacType {

	HMAC_SHA256("HmacSHA256") {
	},
	HMAC_SHA384("HmacSHA384") {
	},
	;

	protected String algorithm;

	MacType(String algorithm) {
		this.algorithm = algorithm;
	}

	public String encrypt(String key, String content) {
		return encrypt(key, content, null);
	}

	public String encrypt(String content, String key, EncodeType encodeType) {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), this.algorithm);
		return encrypt(content, secretKeySpec, encodeType);
	}

	public String encrypt(String content, Key key, EncodeType encodeType) {
		try {
			Mac mac = Mac.getInstance(this.algorithm);
			mac.init(key);

			byte[] encryptedBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));

			if (encodeType != null) {
				return encodeType.encodeToString(encryptedBytes);
			}
			return new String(encryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
