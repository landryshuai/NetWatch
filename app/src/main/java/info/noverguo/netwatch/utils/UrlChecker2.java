package info.noverguo.netwatch.utils;

import android.content.Context;

import info.noverguo.netwatch.PrefSetting;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalUrlService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by noverguo on 2016/5/9.
 */
public class UrlChecker2 {
    Map<String, Set<String>> whiteList = new HashMap<>();
    Map<String, Set<String>> blackList = new HashMap<>();
//    Map<String, Set<String>> unknownList = new HashMap<>();
//    Map<String, Set<String>> checkingList = new HashMap<>();
    LocalUrlService urlService;
    PrefSetting setting;
//    private boolean needClear = false;
    public UrlChecker2(Context context) {
        urlService = new LocalUrlService(context);
        setting = new PrefSetting(context);
        ReloadReceiver.registerReloadBlack(context, new Runnable() {
            @Override
            public void run() {
//                needClear = true;
//                if (checkingList.isEmpty()) {
                    clear();
//                }
            }
        });
        load();
    }

    private void clear() {
        clearSync(blackList);
        clearSync(whiteList);
//        clearSync(unknownList);
//        clearSync(checkingList);
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
                return validateSync(url.getHost(), url.getPath());
            }
        } catch (MalformedURLException e) {
        }
        return true;
    }

    private boolean justValidate(String host, String path) {
        DLog.i("validate: " + host + ", " + path);
        synchronized (blackList) {
            if (blackList.containsKey(host)) {
                Set<String> blackSet = blackList.get(host);
                if (SizeUtils.isEmpty(blackSet) || blackSet.contains(path)) {
                    return false;
                }
                if (path != null) {
                    for (String blackPath : blackSet) {
                        if (path.startsWith(blackPath)) {
                            return false;
                        }
                    }
                }
            }
        }
        synchronized (whiteList) {
            if (whiteList.containsKey(host)) {
                Set<String> whiteSet = whiteList.get(host);
                if (SizeUtils.isEmpty(whiteSet) || whiteSet.contains(path)) {
                    return true;
                }
            }
        }
        throw new UnknownError();
    }

    /**
     * 异步检查,未知的先通过
     * @param host
     * @param path
     * @return
     */
//    public boolean validate(String host, String path) {
//        DLog.i("validate: " + host + ", " + path);
//        try {
//            return justValidate(host, path);
//        } catch (UnknownError e) {
//            addUnknown(host, path);
//            postCheck();
//            return true;
//        }
//    }

    /**
     * 同步检查
     * @param host
     * @param path
     * @return host对应的所有黑名单
     */
    public synchronized boolean validateSync(String host, String path) {
        try {
            return justValidate(host, path);
        } catch (UnknownError e) {
            Set<String> blackUrls = urlService.checkHost(host + path, host, path);
            if (blackUrls == null) {
                put(whiteList, host, path);
            } else {
                blackList.put(host, blackUrls);
            }
            return justValidate(host, path);
        }
    }

    public boolean validateHost(String host) {
        return validateSync(host, "");
    }

    public boolean validateSocket(String host, String address, int port) {
        if (port > 0) {
            return validateHost(host + (port == 80 ? "" : ":" + port)) || validateHost(address + ":" + port);
        }
        return validateHost(host) || validateHost(address);
    }

    private void put(Map<String, Set<String>> map, String key, String val) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<String>());
        }
        map.get(key).add(val);
    }

//    private boolean inCache(String host, String path) {
//        synchronized (blackList) {
//            if (blackList.containsKey(host)) {
//                Set<String> blackSet = blackList.get(host);
//                if (SizeUtils.isEmpty(blackSet) || blackSet.contains(path)) {
//                    return true;
//                }
//            }
//        }
//        synchronized (whiteList) {
//            if (whiteList.containsKey(host)) {
//                Set<String> whiteSet = whiteList.get(host);
//                if (SizeUtils.isEmpty(whiteSet) || whiteSet.contains(path)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


//    private void addUnknown(String host, String path) {
//        DLog.d("addUnknown: " + host + ", " + path);
//        synchronized (unknownList) {
//            if (!unknownList.containsKey(host)) {
//                unknownList.put(host, new HashSet<String>());
//            }
//            unknownList.get(host).add(path);
//        }
//    }
//
//    boolean checking = false;
//    private Runnable checkCallback = new Runnable() {
//        @Override
//        public void run() {
//            synchronized (checkCallback) {
//                if (checking) {
//                    return;
//                }
//                checking = true;
//            }
//            try {
//                synchronized (checkingList) {
//                    if (!checkingList.isEmpty()) {
//                        return;
//                    }
//                    synchronized (unknownList) {
//                        if (unknownList.isEmpty()) {
//                            return;
//                        }
//                        checkingList.putAll(unknownList);
//                        unknownList.clear();
//                    }
//                }
//
//                urlService.check(new PackageUrlSet(checkingList), new LocalUrlService.CheckCallback() {
//                    @Override
//                    public void onCheck(PackageUrlSet origin, PackageUrlSet result) {
//                        DLog.d("UrlChecker.onCheck: " + result.relativeUrls + ", " + origin.relativeUrls);
//                        synchronized (unknownList) {
//                            Map<String, Set<String>> blackResult = result.relativeUrls;
//                            synchronized (checkingList) {
//                                for (String key : checkingList.keySet()) {
//                                    Set<String> value = checkingList.get(key);
//                                    if (blackResult.containsKey(key)) {
//                                        Set<String> set = blackResult.get(key);
//                                        // 不存在path黑名单,则整个host为黑名单
//                                        if (!set.isEmpty()) {
//                                            for (String checkValue : value) {
//                                                // 存在pach黑名单，则不在黑名单的加入到白名单中
//                                                if (!set.contains(checkValue)) {
//                                                    if (!whiteList.containsKey(key)) {
//                                                        whiteList.put(key, new HashSet<String>());
//                                                    }
//                                                    whiteList.get(key).add(checkValue);
//                                                }
//                                            }
//                                        }
//                                        blackList.put(key, set);
//                                    } else {
//                                        // 由于黑名单是直接根据host查的，因此如果没发现黑名单，则该host都可作为白名单
//                                        if (!whiteList.containsKey(key)) {
//                                            whiteList.put(key, Collections.<String>emptySet());
//                                        }
//                                    }
//                                }
//                                checkingList.clear();
//                            }
//                            store();
//                            DLog.d("blackList: " + blackList);
//                            DLog.d("whiteList: " + whiteList);
//                            if (needClear) {
//                                needClear = false;
//                                clear();
//                            }
//                            if (unknownList.isEmpty()) {
//                                return;
//                            }
//                            Map<String, Set<String>> checkingUnknowList = new HashMap<String, Set<String>>(unknownList);
//                            for (String host : checkingUnknowList.keySet()) {
//                                if (blackList.containsKey(host) || whiteList.containsKey(host)) {
//                                    for (String path : checkingUnknowList.get(host)) {
//                                        if (!inCache(host, path)) {
//                                            whiteList.get(host).add(path);
//                                        }
//                                    }
//                                    unknownList.remove(host);
//                                }
//                            }
//
//                            if (unknownList.isEmpty()) {
//                                return;
//                            }
//                        }
//                        postCheck();
//                    }
//
//                    @Override
//                    public void onError(RemoteException e) {
//                        e.printStackTrace();
//                        postCheck();
//                    }
//                });
//            } finally {
//                synchronized (checkCallback) {
//                    checking = false;
//                }
//            }
//        }
//    };
//    private static final int DELAY_TIME = 1000 * 8;
//    private void postCheck() {
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.removeCallbacks(checkCallback);
//        handler.postDelayed(checkCallback, DELAY_TIME);
//    }

    private void store() {
        setting.putBlackList(toSet(blackList));
        setting.putWhiteList(toSet(whiteList));
    }

    private void load() {
        blackList.putAll(toMap(setting.getBlackList()));
        whiteList.putAll(toMap(setting.getWhiteList()));
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
