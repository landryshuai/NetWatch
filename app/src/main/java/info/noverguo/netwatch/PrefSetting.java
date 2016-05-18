package info.noverguo.netwatch;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by noverguo on 2016/5/10.
 */
public class PrefSetting {
    private final static String PREF_NAME = "htsetting";
    private final static String KEY_BLACK = "kb";
    private final static String KEY_WHITE = "kw";
    private Context context;
    public PrefSetting(Context context) {
        this.context = context;
    }
    private SharedPreferences getPref() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public void putBlackList(Set<String> blackUrls) {
        putSet(KEY_BLACK, blackUrls);
    }
    public Set<String> getBlackList() {
        return getSet(KEY_BLACK);
    }
    public void putWhiteList(Set<String> whiteUrls) {
        putSet(KEY_WHITE, whiteUrls);
    }
    public Set<String> getWhiteList() {
        return getSet(KEY_WHITE);
    }

    private void putSet(String name, Set<String> set) {
        getPref().edit().putStringSet(name, set).apply();
    }
    private Set<String> getSet(String name) {
        return getPref().getStringSet(name, new HashSet<String>());
    }
}
