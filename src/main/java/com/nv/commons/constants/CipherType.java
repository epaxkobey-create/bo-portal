package com.nv.commons.constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 處理雙向加密演算法類別，包含對稱加密跟非對稱加密
 * author: Alan
 */
public enum CipherType {

	DESede_ECB("DESede", "DESede/ECB/PKCS5Padding") {
		/**
		 * 將key轉為3des要求規格
		 * @param keyStr key
		 */
		private byte[] build3DesKey(String keyStr) {
			byte[] key = new byte[24];
			byte[] temp = keyStr.getBytes(StandardCharsets.UTF_8);
			final int minLength = Math.min(key.length, temp.length);
			System.arraycopy(temp, 0, key, 0, minLength);
			return key;
		}

		@Override
		public String encrypt(String content, String key, String iv, EncodeType encodeType) {
			return encrypt(content, build3DesKey(key), iv, encodeType);
		}

		@Override
		public String decrypt(String content, String key, String iv, EncodeType encodeType) {
			return decrypt(content, build3DesKey(key), iv, encodeType);
		}

	},

	RSA_ECB("RSA", "RSA/ECB/PKCS1Padding") {

	},
	;

	protected final String transformation;
	protected final String algorithm;

	CipherType(String algorithm, String transformation) {
		this.algorithm = algorithm;
		this.transformation = transformation;
	}

	// 只有非對稱加密需要
	public KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithm);
		keyPairGenerator.initialize(keySize, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	// for decrypt
	public PublicKey getPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
		return keyFactory.generatePublic(keySpec);
	}

	// for decrypt
	public PrivateKey getPrivateKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
		KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
		return keyFactory.generatePrivate(keySpec);
	}

	public String encrypt(String content, String key) {
		return encrypt(content, key, null, null);
	}

	public String encrypt(String content, String key, EncodeType encodeType) {
		return encrypt(content, key, null, encodeType);
	}

	public String encrypt(String content, String key, String iv, EncodeType encodeType) {
		return encrypt(content, key.getBytes(StandardCharsets.UTF_8), iv, encodeType);
	}

	public String encrypt(String content, byte[] key, String iv, EncodeType encodeType) {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, this.algorithm);
		return encrypt(content, secretKeySpec, iv, encodeType);
	}

	public String encrypt(String content, Key key, String iv, EncodeType encodeType) {
		try {

			Cipher cipher = Cipher.getInstance(this.transformation, "SunJCE");
			if (iv != null) {
				cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, key);
			}

			byte[] encryptedBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
			if (encodeType != null) {
				return encodeType.encodeToString(encryptedBytes);
			}
			return new String(encryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String decrypt(String content, String key) {
		return decrypt(content, key, null, null);
	}

	public String decrypt(String content, String key, EncodeType encodeType) {
		return decrypt(content, key, null, encodeType);
	}

	public String decrypt(String content, String key, String iv, EncodeType encodeType) {
		return decrypt(content, key.getBytes(StandardCharsets.UTF_8), iv, encodeType);
	}

	public String decrypt(String content, byte[] key, String iv, EncodeType encodeType) {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, this.algorithm);
		return decrypt(content, secretKeySpec, iv, encodeType);
	}

	public String decrypt(String content, Key key, String iv, EncodeType encodeType) {
		try {
			Cipher cipher = Cipher.getInstance(this.transformation, "SunJCE");

			if (iv != null) {
				cipher.init(Cipher.DECRYPT_MODE, key,
					new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key, new SecureRandom());
			}

			byte[] decodeBytes;
			if (encodeType != null) {
				decodeBytes = encodeType.decode(content);
			} else {
				decodeBytes = content.getBytes(StandardCharsets.UTF_8);
			}
			byte[] decryptedBytes = cipher.doFinal(decodeBytes);
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 當資料龐大時，分段去解密
	public byte[] decryptByBuffer(Cipher cipher, byte[] bytes)
		throws IllegalBlockSizeException, BadPaddingException, IOException {
		int inputLen = bytes.length;
		int offLen = 0;
		int i = 0;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		while (inputLen - offLen > 0) {
			byte[] cache;
			if (inputLen - offLen > 128) {
				cache = cipher.doFinal(bytes, offLen, 128);
			} else {
				cache = cipher.doFinal(bytes, offLen, inputLen - offLen);
			}
			byteArrayOutputStream.write(cache);
			i++;
			offLen = 128 * i;
		}
		byteArrayOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}
}
