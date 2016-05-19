package info.noverguo.netwatch;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.noverguo.netwatch.model.PackageUrlSet;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by noverguo on 2016/5/10.
 */
public class PrefSetting {
    private final static String PREF_NAME = "htsetting";
    private final static String KEY_BLACK = "kb";
    private final static String KEY_WHITE = "kw";
    private final static String KEY_PACKAGE_BLACK = "kpb";
    private final static String KEY_PACKAGE_URL = "kpu";
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
    public Observable<Set<String>> getBlackList() {
        return getSet(KEY_BLACK);
    }
    public void putWhiteList(Set<String> whiteUrls) {
        putSet(KEY_WHITE, whiteUrls);
    }
    public Observable<Set<String>> getWhiteList() {
        return getSet(KEY_WHITE);
    }

    public void putPackageBlackList(Map<String, PackageUrlSet> packageBlackUrls) {
        putPackageUrlMap(KEY_PACKAGE_BLACK, packageBlackUrls);
    }

    public Observable<Map<String, PackageUrlSet>> getPackageBlackList() {
        return getPackageUrlMap(KEY_PACKAGE_BLACK);
    }

    public void putPackageUrlList(Map<String, PackageUrlSet> packageBlackUrls) {
        putPackageUrlMap(KEY_PACKAGE_URL, packageBlackUrls);
    }

    public Observable<Map<String, PackageUrlSet>> getPackageUrlList() {
        return getPackageUrlMap(KEY_PACKAGE_URL);
    }

    private void putPackageUrlMap(final String key, final Map<String, PackageUrlSet> packageBlackUrls) {
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                getPref().edit().putString(key, new Gson().toJson(packageBlackUrls)).apply();
            }
        });
    }

    private Observable<Map<String, PackageUrlSet>> getPackageUrlMap(final String key) {
        return Observable.create(new Observable.OnSubscribe<Map<String, PackageUrlSet>>() {
            @Override
            public void call(Subscriber<? super Map<String, PackageUrlSet>> subscriber) {
                String string = getPref().getString(key, null);
                Map<String, PackageUrlSet> val;
                if (string == null) {
                    val = Collections.emptyMap();
                } else {
                    val = new Gson().fromJson(string, new TypeToken<Map<String, PackageUrlSet>>() {}.getType());
                }
                subscriber.onNext(val);
                subscriber.onCompleted();
            }
        });

    }

    private void putSet(final String name, final Set<String> set) {
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                getPref().edit().putStringSet(name, set).apply();
            }
        });
    }
    private Observable<Set<String>> getSet(final String name) {
        return Observable.create(new Observable.OnSubscribe<Set<String>>() {

            @Override
            public void call(Subscriber<? super Set<String>> subscriber) {
                subscriber.onNext(getPref().getStringSet(name, new HashSet<String>()));
                subscriber.onCompleted();
            }
        });
    }
}
