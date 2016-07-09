package info.noverguo.netwatch;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import info.noverguo.netwatch.model.PackageUrlSet;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by noverguo on 2016/5/10.
 */
public class PrefSetting {
    private final static String PREF_NAME = "htsetting";
    private final static String KEY_PACKAGE_BLACK = "kpb";
    private final static String KEY_PACKAGE_URL = "kpu";
    private final static String KEY_MD5_MAP = "kmm";
    private final static String KEY_UNCHECK_PACKAGE = "kup";
    private final static String KEY_CLICK_HIDE_PACKAGE = "kup";
    private Context context;
    public PrefSetting(Context context) {
        this.context = context;
    }
    private SharedPreferences getPref() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void putPackageBlackList(Map<String, PackageUrlSet> packageBlackUrls) {
        putMap(KEY_PACKAGE_BLACK, packageBlackUrls);
    }

    public Observable<Map<String, PackageUrlSet>> getPackageBlackList() {
        return getPackageUrlMap(KEY_PACKAGE_BLACK);
    }

    public void putPackageUrlList(Map<String, PackageUrlSet> packageBlackUrls) {
        putMap(KEY_PACKAGE_URL, packageBlackUrls);
    }

    public Observable<Map<String, PackageUrlSet>> getPackageUrlList() {
        return getPackageUrlMap(KEY_PACKAGE_URL);
    }

    public void putMd5Map(Map<String, String> md5Map) {
        putMap(KEY_MD5_MAP, md5Map);
    }

    public Observable<Map<String, String>> getMd5Map() {
        return getStringMap(KEY_MD5_MAP);
    }

    public void putCheckPackage(Set<String> uncheckPackage) {
        putSet(KEY_UNCHECK_PACKAGE, uncheckPackage);
    }

    public Observable<Set<String>> getCheckPackage() {
        return getStringSet(KEY_UNCHECK_PACKAGE);
    }

    public void putClickHidePackage(Set<String> uncheckPackage) {
        putSet(KEY_CLICK_HIDE_PACKAGE, uncheckPackage);
    }

    public Observable<Set<String>> getClickHidePackage() {
        return getStringSet(KEY_CLICK_HIDE_PACKAGE);
    }


    private <T> void putSet(final String key, final Set<T> set) {
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                synchronized (set) {
                    getPref().edit().putString(key, new Gson().toJson(set)).apply();
                }
            }
        });
    }

    private Observable<Set<String>> getStringSet(final String key) {
        return Observable.just(key).map(new Func1<String, Set<String>>() {
            @Override
            public Set<String> call(String s) {
                String string = getPref().getString(key, null);
                Set<String> val;
                if (string == null) {
                    val = Collections.emptySet();
                } else {
                    val = new Gson().fromJson(string, new TypeToken<Set<String>>() {}.getType());
                }
                return val;
            }
        });
    }

    private <T> void putMap(final String key, final Map<String, T> packageBlackUrls) {
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                synchronized (packageBlackUrls) {
                    getPref().edit().putString(key, new Gson().toJson(packageBlackUrls)).apply();
                }
            }
        });
    }

    private Observable<Map<String, String>> getStringMap(final String key) {
        return Observable.just(key).map(new Func1<String, Map<String, String>>() {
            @Override
            public Map<String, String> call(String s) {
                String string = getPref().getString(key, null);
                Map<String, String> val;
                if (string == null) {
                    val = Collections.emptyMap();
                } else {
                    val = new Gson().fromJson(string, new TypeToken<Map<String, String>>() {}.getType());
                }
                return val;
            }
        });
    }

    private Observable<Map<String, PackageUrlSet>> getPackageUrlMap(final String key) {
        return Observable.just(key).map(new Func1<String, Map<String, PackageUrlSet>>() {
            @Override
            public Map<String, PackageUrlSet> call(String s) {
                String string = getPref().getString(key, null);
                Map<String, PackageUrlSet> val;
                if (string == null) {
                    val = Collections.emptyMap();
                } else {
                    val = new Gson().fromJson(string, new TypeToken<Map<String, PackageUrlSet>>() {}.getType());
                }
                return val;
            }
        });
    }
}
