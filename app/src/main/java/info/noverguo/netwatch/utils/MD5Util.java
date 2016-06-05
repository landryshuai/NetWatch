package info.noverguo.netwatch.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密帮助类
 * @author noverguo
 *
 */
public class MD5Util {

	public static String encrypt_string(String plainText) {
		byte[] bytes = encrypt(plainText);
		return ConvertUtil.bytesToHexString(bytes);
	}

	public static String encrypt_bytes(byte[] bytes) {
		byte[] result_bytes = encrypt(bytes);
		return ConvertUtil.bytesToHexString(result_bytes);
	}

	/**
	 * @param plainText 明文
	 * @return	16位的加密加密结果
	 */
	public static byte[] encrypt(String plainText) {
		return encrypt(plainText.getBytes());
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static byte[] encrypt(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			byte b[] = md.digest();
			return b;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
