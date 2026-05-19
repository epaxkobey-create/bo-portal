package com.nv.commons.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.nv.commons.constants.CipherType;
import com.nv.commons.constants.EncodeType;
import com.nv.commons.constants.MacType;
import com.nv.commons.constants.MessageDigestType;
import com.nv.commons.constants.SignatureType;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Neutec
 */
public class EncryptUtil {

	// 128 bit key
//	static final String APP_VERSION_ENC_KEY = "92aa438323c2fb13";
	// 16 bytes IV
//	static final String APP_VERSION_INIT_VECTOR = "4a7059ef47ace19f";

//	private static final String ALGORITHM_AES_CBC = "AES/CBC/PKCS5Padding";
//	//格式為:加密算法的名稱/分組加密的模式/分組的填充方式
//	private static final String ALGORITHM_DES_CBC = "DESede/CBC/PKCS5Padding";
//	private static final String ALGORITHM_DES_CBC_BC = "DESede/CBC/PKCS7Padding";
//	private static final String ALGORITHM_AES = "AES";
//	//等於DESede/ECB/PKCS5Padding
//	private static final String ALGORITHM_DES = "DESede";

//	private static final String ALGORITHM_RSA = "RSA";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private EncryptUtil() {
		throw new AssertionError();
	}

	/**
	 * 將訊息使用 3DES 加密後，再轉為Base64位元字串
	 *
	 * @param plainText 　要加密的訊息
	 * @param key       　私鑰
	 * @return Base64位元的結果字串
	 * @throws Exception exception
	 */
	public static String encrypt3DESToBase64(String plainText, String key) throws Exception {
		return CipherType.DESede_ECB.encrypt(plainText, key, EncodeType.Base64);
	}


	/**
	 * 將訊息使用 3DES 解密
	 *
	 * @param cipherText 加密後的 Base64 字串
	 * @param key        　私鑰
	 * @return 原始文字
	 * @throws Exception exception
	 */
	public static String decrypt3DESFromBase64(String cipherText, String key) throws Exception {
		return CipherType.DESede_ECB.decrypt(cipherText, key, EncodeType.Base64);
	}

	public static String encryptMD5ToBase64(String data) {
		return MessageDigestType.MD5.encrypt(data, EncodeType.Base64);
	}

	/**
	 * 將訊息使用 MD5 加密後，再轉為16位元進制字串
	 *
	 * @param plainText 要加密的訊息
	 * @return 16位元進制的結果字串
	 */
	public static String encryptMD5ToHex(String plainText) {
		return MessageDigestType.MD5.encrypt(plainText, EncodeType.Hex);
	}


	/**
	 * 將訊息使用 SHA1 加密後，再轉為16位元進制字串
	 *
	 * @param strToHash 要加密的訊息
	 * @param upperCase 是否將結果
	 * @return 16位元進制的結果字串
	 */
	public static String encryptSHA1ToHex(String strToHash, boolean upperCase) {
		String result = MessageDigestType.SHA1.encrypt(strToHash, EncodeType.Hex);
		return upperCase ? result.toUpperCase() : result.toLowerCase();
	}

	public static boolean checkSHA256SignByPubkey(String oriInfo, String sign, String pubKey) {
		return SignatureType.SHA256_RSA.verify(oriInfo, sign, Base64.decodeBase64(pubKey), EncodeType.Base64);
	}

	/**
	 * 訊息使用 HMAC-384 加密後，再轉為Hex回傳
	 *
	 * @param key     加密的key
	 * @param plainText 加密的訊息
	 * @return String result
	 */
	public static String encryptHMACSHA384ToHex(String key, String plainText) {
		return MacType.HMAC_SHA384.encrypt(plainText, key, EncodeType.Hex);
	}

//	// 這邊應該搬移到CipherType，但沒有足夠資料測試，所以先保留在這
//	public static String decryptRSA(String data, String key) throws Exception {
//		byte[] decode = java.util.Base64.getDecoder().decode(key);
//		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(decode);
//		KeyFactory kf = KeyFactory.getInstance(ALGORITHM_RSA);
//		PublicKey generatePublic = kf.generatePublic(x509EncodedKeySpec);
//		Cipher ci = Cipher.getInstance(ALGORITHM_RSA);
//		ci.init(Cipher.DECRYPT_MODE, generatePublic);
//
//		byte[] bytes = java.util.Base64.getDecoder().decode(data);
//
//		int inputLen = bytes.length;
//		int offLen = 0;
//		int i = 0;
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		while (inputLen - offLen > 0) {
//			byte[] cache;
//			if (inputLen - offLen > 128) {
//				cache = ci.doFinal(bytes, offLen, 128);
//			} else {
//				cache = ci.doFinal(bytes, offLen, inputLen - offLen);
//			}
//			byteArrayOutputStream.write(cache);
//			i++;
//			offLen = 128 * i;
//
//		}
//		byteArrayOutputStream.close();
//		byte[] byteArray = byteArrayOutputStream.toByteArray();
//		return new String(byteArray);
//	}

	public static String encryptByBase64(String data) {
		return new String(Base64.encodeBase64(data.getBytes()));
	}

//	public static String decryptByBase64(String data) {
//		return new String(Base64.decodeBase64(data.getBytes()));
//	}

	public static String encryptHMACSHA256ToHex(String key, String plainText) {
		return MacType.HMAC_SHA256.encrypt(plainText, key, EncodeType.Hex);
	}

/*
Standard: AES-256
Mode: CBC
Padding: PKCS5Padding
Key: apiKey (UTF-8)
IV (initialization vector): companyKey.toLowerCase() (UTF-8)
Input: md5Hex((apiKey + companyKey).toLowerCase()) + timestamp (current timestamp in seconds)
Output Format: base64
* */



	private static final String ALGORITHM_AES_CBC = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM_AES = "AES";

	public static String encryptAESWithCBC(String plainText, String iv, String key) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CBC, "SunJCE");
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		return encryptAES(plainText,cipher);
	}
	public static String encryptAES(String plainText, Cipher cipher) throws Exception {
		return new String(Base64.encodeBase64(cipher.doFinal(
			plainText.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
	}
	public static String decryptAESWithCBC(String cryptText, String iv, String key) throws Exception {

		Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CBC);
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		return decryptAES(cryptText, cipher);
	}
	public static String decryptAES(String cryptText, Cipher cipher) throws Exception {
		return new String(cipher.doFinal(Base64.decodeBase64(cryptText.getBytes(StandardCharsets.UTF_8))));
	}

}
