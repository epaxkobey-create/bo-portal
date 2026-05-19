package com.nv.commons.constants;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;

/**
 * 此類別主要負責單向加密，特色是消息摘要使用公開密鑰和私有密鑰去做雙向加密
 * 私鑰用於簽名，公鑰用於驗證
 * Message Authentication Code
 *
 * author: Alan
 */
public enum SignatureType {

	SHA256_RSA("SHA256withRSA", CipherType.RSA_ECB) {

	},

	;

	protected String algorithm;
	protected CipherType cipherType;

	SignatureType(String algorithm, CipherType cipherType) {
		this.algorithm = algorithm;
		this.cipherType = cipherType;
	}

	public CipherType getCipherType() {
		return cipherType;
	}

	public String sign(String content, byte[] key, EncodeType encodeType)
		throws NoSuchAlgorithmException, InvalidKeySpecException {
		PrivateKey privateKey = this.cipherType.getPrivateKey(key);
		return sign(content.getBytes(StandardCharsets.UTF_8), privateKey, encodeType);
	}

	public String sign(byte[] content, PrivateKey privateKey, EncodeType encodeType) {
		try {
			Signature signature = Signature.getInstance(this.algorithm);
			signature.initSign(privateKey);

			signature.update(content);
			byte[] encryptedBytes = signature.sign();

			if (encodeType != null) {
				return encodeType.encodeToString(encryptedBytes);
			}
			return new String(encryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean verify(String content, String signatureData, byte[] key, EncodeType encodeType) {
		try {
			return verify(content.getBytes(StandardCharsets.UTF_8), signatureData, key, encodeType);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean verify(byte[] content, String signatureData, byte[] key, EncodeType encodeType)
		throws NoSuchAlgorithmException, InvalidKeySpecException {
		PublicKey publicKey = this.cipherType.getPublicKey(key);
		return verify(content, encodeType.decode(signatureData), publicKey);
	}

	public boolean verify(byte[] content, byte[] signatureData, PublicKey publicKey) {
		try {
			Signature signature = Signature.getInstance(this.algorithm);
			signature.initVerify(publicKey);
			signature.update(content);

			return signature.verify(signatureData);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
