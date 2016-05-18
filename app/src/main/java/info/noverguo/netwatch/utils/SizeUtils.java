package info.noverguo.netwatch.utils;

import java.util.Collection;
import java.util.Map;

public class SizeUtils {
	public static int getSize(boolean[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(byte[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(char[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(short[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(int[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(long[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(float[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static int getSize(double[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static <T> int getSize(T[] arr) {
		return arr == null ? 0 : arr.length;
	}
	public static <T> int getSize(Collection<T> list) {
		return list == null ? 0 : list.size();
	}
	public static int getSize(Map<?, ?> map) {
		if (map == null)
			return 0;
		return map.size();
	}
	
	
	public static boolean isEmpty(boolean[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(byte[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(char[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(short[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(int[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(long[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(float[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(double[] arr) {
		return arr == null || arr.length == 0;
	}
	public static <T> boolean isEmpty(T[] arr) {
		return arr == null || arr.length == 0;
	}
	public static boolean isEmpty(Collection<?> coll) {
		return coll == null || coll.isEmpty();
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
}
