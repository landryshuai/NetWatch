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
    public static void d(String info) {
        if (BuildConfig.DEBUG && d) {
            Log.d(TAG, head + info);
        }
    }
    public static void i(String info) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, head + info);
        }
    }
}
