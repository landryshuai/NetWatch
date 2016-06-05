package info.noverguo.netwatch.xposed;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import info.noverguo.netwatch.model.HostPathsMap;

/**
 * Created by noverguo on 2016/5/10.
 */
public class HSetting {
    private final static int VERSION = 1;
    private final static String PREF_NAME = "hsetting_" + VERSION;
    private final static String KEY_BLACK = "kb";
    private final static String KEY_WHITE = "kw";
    private final static String KEY_MD5 = "km";
    private Context context;
    public HSetting(Context context) {
        this.context = context;
    }
    private SharedPreferences getPref() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void putBlackMap(HostPathsMap blackMap) {
        putMap(KEY_BLACK, blackMap);
    }

    public HostPathsMap getBlackMap() {
        return getHostPathsMap(KEY_BLACK);
    }

    public void putWhiteMap(HostPathsMap whiteMap) {
        putMap(KEY_BLACK, whiteMap);
    }

    public HostPathsMap getWhiteMap() {
        return getHostPathsMap(KEY_WHITE);
    }

    public void putMd5(String md5) {
        putString(KEY_MD5, md5);
    }


    public String getMd5() {
        return getString(KEY_MD5);
    }

    private <T> void putMap(final String key, final T t) {
        synchronized (t) {
            getPref().edit().putString(key, new Gson().toJson(t)).apply();
        }
    }

    private void putString(final String key, final String val) {
        getPref().edit().putString(KEY_MD5, val).apply();
    }

    private String getString(final String key) {
        return getPref().getString(key, "");
    }

    private HostPathsMap getHostPathsMap(final String key) {
        String string = getPref().getString(key, null);
        HostPathsMap val;
        if (string == null) {
            val = new HostPathsMap();
        } else {
            val = new Gson().fromJson(string, new TypeToken<HostPathsMap>() {}.getType());
        }
        return val;
    }
}
