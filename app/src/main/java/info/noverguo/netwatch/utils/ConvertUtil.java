package info.noverguo.netwatch.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 类型转换工具类
 * @author serenazhou
 *
 */
public class ConvertUtil {

	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	/**
	 * 把字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2) {
				sb.append(0);
			}
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将int值转为从低位到高位排列的字节数组,如1转换后字节数组为{1,0,0,0}
	 * @param src
	 * @return
	 */
	public static byte[] intToBytes(int src) {
		byte[] rst = new byte[4];
		rst[0] = (byte) (src & 0xff);
		rst[1] = (byte) ((src >> 8) & 0xff);
		rst[2] = (byte) ((src >> 16) & 0xff);
		rst[3] = (byte) ((src >> 24) & 0xff);
		return rst;
	}
	
	/**
	 * 将从低位到高位排列的字节数组转为int值,如字节数组{1,0,0,0}转换后为1
	 * @param src
	 * @return
	 */
	public static int bytesToInt(byte[] src) {
		if (src.length != 4) {
			return 0;
		}
		int rst = (src[0] & 0xff) | ((src[1] & 0xff) << 8)
					| ((src[2] & 0xff) << 16) | ((src[3] & 0xff) << 24);
		return rst;
	}
	
	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}
	
	/**
	 * stream转为字节数组
	 * @param is 输入流
	 * @return
	 */
	public static byte[] inputStreamToBytes(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int length = -1;
			while((length = is.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			baos = null;
		}
	}
	
}
