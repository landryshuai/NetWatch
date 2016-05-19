package info.noverguo.netwatch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.UrlServiceUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by noverguo on 2016/5/10.
 */
public class RemoteUrlService extends Service {

    Map<String, PackageUrlSet> packageBlackList;
    Map<String, Set<String>> blackList;
    Map<String, Pattern> regexHostBlackList;
    Map<String, Set<String>> regexBlackList;
    Map<String, PackageUrlSet> packageUrlList;

    @Override
    public void onCreate() {
        super.onCreate();
        initBlackList();
        reloadBlackList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IUrlService.Stub() {
            @Override
            public List<String> checkHost(String packageName, String url, String host, String path) throws RemoteException {
                DLog.i("onBind.checkHost: " + packageName + ", " + host + path);
                PackageUrlSet.put(packageUrlList, packageName, url);
                ReloadReceiver.sendReloadPackage(getApplicationContext());
                synchronized (RemoteUrlService.class) {
                    // 缓存访问列表
                    if (blackList.containsKey(host)) {
                        return new ArrayList<>(blackList.get(host));
                    }
                    Set<String> paths = getRegexHostBlackPathList(host);
                    if (paths != null) {
                        return new ArrayList<>(paths);
                    }
                }
                return UrlServiceUtils.getNullList();
            }

            @Override
            public List<PackageUrlSet> getAccessUrls() throws RemoteException {
                DLog.d("RemoteUrlService.getAccessUrls: " + packageUrlList);
                return new ArrayList<>(packageUrlList.values());
            }

            @Override
            public List<PackageUrlSet> getBlackUrls() throws RemoteException {
                synchronized (RemoteUrlService.class) {
                    DLog.d("RemoteUrlService.getBlackUrls: " + packageBlackList);
                    return new ArrayList<>(packageBlackList.values());
                }
            }

            @Override
            public void addBlackUrls(List<PackageUrlSet> blackUrls) throws RemoteException {
                DLog.i("addBlackUrls: " + blackUrls);
                PackageUrlSet.put(packageBlackList, blackUrls);
                reloadBlackList();
            }

            @Override
            public void removeBlackUrls(List<PackageUrlSet> blackUrls) throws RemoteException {
                PackageUrlSet.remove(packageBlackList, blackUrls);
                reloadBlackList();
            }
        };
    }

    private Set<String> getRegexHostBlackPathList(String host) {
        synchronized (RemoteUrlService.class) {
            for (Map.Entry<String, Pattern> entry : regexHostBlackList.entrySet()) {
                if (entry.getValue().matcher(host).find()) {
                    return regexBlackList.get(entry.getKey());
                }
            }
        }
        return null;
    }

    private void initBlackList() {
        synchronized (RemoteUrlService.class) {
            packageUrlList = new HashMap<>();
            packageBlackList = new HashMap<>();
            blackList = new HashMap<>();
            regexHostBlackList = new HashMap<>();
            regexBlackList = new HashMap<>();
        }
        File file = new File("/data/local/tmp/black_list.txt");
        List<String> urls = null;
        try {
            urls = IOUtils.readLines(new FileInputStream(file));
        } catch (IOException e) {
        }
        if (urls == null) {
            urls = new ArrayList<>();
            urls.add("http://mobads.baidu.com/cpro/ui/mads.php?code2=UM");
            urls.add("http://mobads.baidu.com/cpro/ui/123456789/123456789/123456789/123456789/123456789/mads.php?code2=UM");
            urls.add("http://mobads-logs.baidu.com/");
        }
        synchronized (RemoteUrlService.class) {
            PackageUrlSet packageUrlSet = new PackageUrlSet(UrlServiceUtils.USER_ADD_PACKAGE);
            for (String url : urls) {
                url = url.trim();
                if (url.startsWith("#")) {
                    continue;
                }
                packageUrlSet.relativeUrls.add(url);
            }
            packageBlackList.put(UrlServiceUtils.USER_ADD_PACKAGE, packageUrlSet);
        }
    }

    private void reloadBlackList() {
        synchronized (RemoteUrlService.class) {
            for(PackageUrlSet pus : packageBlackList.values()) {
                for (String u : pus.relativeUrls) {
                    HostPath hostPath = HostPath.create(u);
                    if (hostPath == null) {
                        continue;
                    }
                    DLog.d("add black packageName : " + u + ", " + hostPath.host + ", " + hostPath.path);
                    String host = hostPath.host;
                    String path = hostPath.path;
                    if (UrlServiceUtils.isPatternUrl(host)) {
                        regexHostBlackList.put(host, UrlServiceUtils.getUrlPattern(host));
                        putMap(regexBlackList, host, path);
                    } else {
                        putMap(blackList, host, path);
                    }
                }
                useEmptyCollections(blackList);
            }
        }
        ReloadReceiver.sendReloadBlack(getApplicationContext());
    }


    private void useEmptyCollections(Map<String, Set<String>> map) {
        for (String key : map.keySet()) {
            Set<String> value = map.get(key);
            if (value.contains("") || value.contains("/")) {
                map.put(key, Collections.<String>emptySet());
            }
        }
    }

    public static void putMap(Map<String, Set<String>> map, String key, String val) {
        if (!map.containsKey(key) || map.get(key).isEmpty()) {
            map.put(key, new HashSet<String>());
        }
        map.get(key).add(val);
    }

}
