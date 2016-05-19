package info.noverguo.netwatch.utils;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.noverguo.hooktest.BuildConfig;
import info.noverguo.netwatch.PrefSetting;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalUrlService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by noverguo on 2016/5/9.
 */
public class UrlChecker {
    Map<String, Set<String>> whiteList = new HashMap<>();
    Map<String, Set<String>> blackList = new HashMap<>();
    LocalUrlService urlService;
    PrefSetting setting;
    public UrlChecker(Context context) {
        urlService = new LocalUrlService(context);
        setting = new PrefSetting(context);
        ReloadReceiver.registerReloadBlack(context, new Runnable() {
            @Override
            public void run() {
                clear();
            }
        });
        load();
    }

    private void clear() {
        clearSync(blackList);
        clearSync(whiteList);
        store();
    }

    private void clearSync(Map<?, ?> map) {
        synchronized (map) {
            map.clear();
        }
    }

    public boolean isNetworkUri(String uri) {
        return uri.startsWith("http") || uri.startsWith("ftp");
    }
    public boolean validateUri(String uri) {
        try {
            if (isNetworkUri(uri)) {
                URL url = new URL(uri);
                return validateSync(uri, url.getHost(), url.getPath());
            }
        } catch (MalformedURLException e) {
        }
        return true;
    }

    private boolean justValidate(String host, String path) throws UnknownHostException {
        DLog.i("validate: " + host + ", " + path);
        // 白名单的检查速度比较快，因此先检查它
        synchronized (whiteList) {
            if (whiteList.containsKey(host)) {
                Set<String> whiteSet = whiteList.get(host);
                if (SizeUtils.isEmpty(whiteSet) || whiteSet.contains(path)) {
                    return true;
                }
            }

        }
        synchronized (blackList) {
            if (blackList.containsKey(host)) {
                Set<String> blackSet = blackList.get(host);
                if (SizeUtils.isEmpty(blackSet) || blackSet.contains(path)) {
                    return false;
                }
                if (path != null) {
                    for (String blackPath : blackSet) {
                        if (UrlServiceUtils.isPatternUrl(blackPath)) {
                            if(UrlServiceUtils.isMatchUrl(blackPath, path)) {
                                // 便于快速匹配
                                put(blackList, host, path);
                                return false;
                            }
                        } else if (path.startsWith(blackPath)) {
                            put(blackList, host, path);
                            return false;
                        }
                    }
                }
                // 有发现在黑名单,却没找到,则应该需要加入到白名单中
                put(whiteList, host, path);
                return true;
            }
        }
        // 黑白名单都没找到的话，则报错
        throw new UnknownHostException();
    }


    /**
     * 同步检查
     * @param host
     * @param path
     * @return host对应的所有黑名单
     */
    public synchronized boolean validateSync(String url, String host, String path) {
        try {
            return justValidate(host, path);
        } catch (UnknownHostException e) {
            Set<String> blackUrls = urlService.checkHost(url, host, path);
            if (blackUrls == null) {
                put(whiteList, host, path);
            } else {
                blackList.put(host, blackUrls);
            }
            try {
                return justValidate(host, path);
            } catch (UnknownHostException e1) {
                if (BuildConfig.DEBUG) {
                    // 不应该到这步啊
                    throw new RuntimeException(e1);
                } else {
                    e1.printStackTrace();
                    return true;
                }
            }
        }
    }

    public boolean validateHost(String url, String host) {
        return validateSync(url, host, "");
    }

    public boolean validateSocket(String host) {
        return validateHost("socket://" + host, host);
    }

    public boolean validateSocket(String host, String address, int port) {
        String val = host;
        if (TextUtils.isEmpty(val)) {
            val = address;
        }
        if (port > 0) {
            if (port == 80) {
                return validateHost("http://" + val, val);
            }
            return validateSocket(val + ":" + port);
        }
        return validateSocket(val);
    }

    private void put(Map<String, Set<String>> map, String key, String val) {
        if (!map.containsKey(key) || map.get(key).isEmpty()) {
            map.put(key, new HashSet<String>());
        }
        map.get(key).add(val);
    }

    private void store() {
        setting.putBlackList(toSet(blackList));
        setting.putWhiteList(toSet(whiteList));
    }

    private void load() {
        RxJavaUtils.io2AndroidMain(setting.getBlackList()).subscribe(new Action1<Set<String>>() {
            @Override
            public void call(Set<String> res) {
                blackList.putAll(toMap(res));
            }
        });

        RxJavaUtils.io2AndroidMain(setting.getWhiteList()).subscribe(new Action1<Set<String>>() {
            @Override
            public void call(Set<String> res) {
                whiteList.putAll(toMap(res));
            }
        });
    }

    private Map<String, Set<String>> toMap(Set<String> urls) {
        Map<String, Set<String>> result = new HashMap<>();
        for(String url : urls) {
            int end = url.indexOf("/");
            if (end > -1) {
                String host = url.substring(0, end);
                String path = url.substring(end);
                if (!result.containsKey(host)) {
                    result.put(host, new HashSet<String>());
                }
                result.get(host).add(path);
            } else {
                result.put(url, new HashSet<String>());
            }
        }
        // 如果为空,则共用对象,以节省内存
        for (String key : result.keySet()) {
            if (result.get(key).isEmpty()) {
                result.put(key, Collections.<String>emptySet());
            }
        }
        return result;
    }

    private Set<String> toSet(Map<String, Set<String>> map) {
        Set<String> result = new HashSet<>();
        for (String host : map.keySet()) {
            Set<String> paths = map.get(host);
            if (!paths.isEmpty()) {
                for (String path : paths) {
                    result.add(host + path);
                }
            } else {
                result.add(host);
            }
        }
        return result;
    }
}
