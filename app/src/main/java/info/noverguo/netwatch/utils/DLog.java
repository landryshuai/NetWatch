package info.noverguo.netwatch.utils;

import android.util.Log;

import com.tencent.noverguo.hooktest.BuildConfig;

/**
 * Created by noverguo on 2016/5/10.
 */
public class DLog {
    private static final String TAG = "Xposed";
    public static final boolean d = true;
    private static String head = "";
    public static void setHead(String h) {
        head = h + ": ";
    }

    public static void d(Object... info) {
        if (BuildConfig.DEBUG && d) {
            Log.d(TAG, toString(info));
        }
    }
    public static void i(Object... info) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, toString(info));
        }
    }

    private static String toString(Object... info) {
        StringBuilder builder = new StringBuilder(head);
        for (Object i : info) {
            builder.append(i).append(", ");
        }
        return builder.toString();
    }
}
