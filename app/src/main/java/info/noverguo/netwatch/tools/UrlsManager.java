package info.noverguo.netwatch.tools;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.tencent.noverguo.hooktest.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.noverguo.netwatch.PrefSetting;
import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.HostPathsMap;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.model.UrlRule;
import info.noverguo.netwatch.model.UrlsMatcher;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalUrlService;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.RxJavaUtils;
import info.noverguo.netwatch.utils.UrlServiceUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by noverguo on 2016/5/28.
 */

public class UrlsManager {
    LocalUrlService urlService;
    Map<String, PackageUrlSet> packageBlackUrls = new HashMap<>();
    Map<String, PackageUrlSet> packageUrls = new HashMap<>();
    Map<String, String> md5Map = new HashMap<>();
    Map<String, UrlsMatcher> urlsMatcherMap = new HashMap<>();
    Context context;
    PrefSetting setting;
    Handler bgHandler;
    HandlerThread bgThread;

    private static UrlsManager sInst;
    public static UrlsManager get(Context context) {
        if (sInst == null) {
            synchronized (UrlsManager.class) {
                if (sInst == null) {
                    sInst = new UrlsManager(context);
                }
            }
        }
        return sInst;
    }

    private UrlsManager(Context context) {
        this.context = context.getApplicationContext();
        this.urlService = new LocalUrlService(context);
        setting = new PrefSetting(context);
        bgThread = new HandlerThread("urls-manager");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
        load();
    }

    public void load() {
        initMd5();
    }

    private void initMd5() {
        RxJavaUtils.io2io(setting.getMd5Map()).subscribe(new Action1<Map<String, String>>() {
            @Override
            public void call(Map<String, String> res) {
                synchronized (md5Map) {
                    md5Map.clear();
                    md5Map.putAll(res);
                }
                initBlackUrls();
            }
        });
    }

    private void initBlackUrls() {
        RxJavaUtils.io2io(setting.getPackageBlackList()).subscribe(new Action1<Map<String, PackageUrlSet>>() {
            @Override
            public void call(Map<String, PackageUrlSet> res) {
                synchronized (packageBlackUrls) {
                    packageBlackUrls.clear();
                    packageBlackUrls.putAll(res);
                }
                initPackageUrls();
            }
        });
    }

    private void initPackageUrls() {
        RxJavaUtils.io2io(setting.getPackageUrlList()).subscribe(new Action1<Map<String, PackageUrlSet>>() {
            @Override
            public void call(Map<String, PackageUrlSet> res) {
                synchronized (packageUrls) {
                    packageUrls.clear();
                    packageUrls.putAll(res);
                }
                reloadBlackUrlMatcherList();
                ReloadReceiver.sendReloadBlack(context);
            }
        });
    }

    public void addPackageUrl(final String packageName, final String url) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (packageUrls) {
                    PackageUrlSet.put(packageUrls, packageName, url);
                }
                setting.putPackageUrlList(packageUrls);
                ReloadReceiver.sendReloadPackage(context);
            }
        });
    }

    public void addBlackUrls(final List<PackageUrlSet> blackUrls) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (packageBlackUrls) {
                    PackageUrlSet.put(packageBlackUrls, blackUrls);
                }
                onReloadBlack(blackUrls);
            }
        });
    }

    public void replaceBlackUrl(final String packageName, final String originUrl, final String newUrl) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (packageBlackUrls) {
                    PackageUrlSet.remove(packageBlackUrls, packageName, originUrl);
                    PackageUrlSet.put(packageBlackUrls, packageName, newUrl);
                }
                ReloadReceiver.sendReloadBlack(context);
                ReloadReceiver.sendReloadBlack(context, packageName);
            }
        });
    }

    public void removeBlackUrls(final List<PackageUrlSet> blackUrls) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (packageBlackUrls) {
                    PackageUrlSet.remove(packageBlackUrls, blackUrls);
                }
                onReloadBlack(blackUrls);
            }
        });
    }

    public PackageUrlSet getPackageUrl(String packageName) {
        synchronized (packageUrls) {
            return packageUrls.get(packageName);
        }
    }

    public void removePackage(String packageName) {
        synchronized (packageUrls) {
            if (packageUrls.containsKey(packageName)) {
                packageUrls.remove(packageName);
                ReloadReceiver.sendReloadPackage(context);
            }
        }
        synchronized (packageBlackUrls) {
            if (packageBlackUrls.containsKey(packageName)) {
                packageBlackUrls.remove(packageName);
                ReloadReceiver.sendReloadBlack(context);
            }
        }
    }

    public boolean checkIsIntercept(String packageName, String host, String path) {
        boolean res = false;
        UrlsMatcher urlsMatcher = null;
        synchronized (urlsMatcherMap) {
            // 检测局部黑名单
            if (urlsMatcherMap.containsKey(packageName)) {
                urlsMatcher = urlsMatcherMap.get(packageName);
            }
        }
        if (urlsMatcher != null) {
            res = urlsMatcher.isMatch(host, path);
        }
        if (BuildConfig.DEBUG) DLog.i("onBind.checkIsIntercept: " + ", " + host + path + ", " + res);
        if (!res) {
            res = checkGlobalIntercept(host, path);
        }
        return res;
    }

    public boolean checkGlobalIntercept(String host, String path) {
        boolean res = false;
        UrlsMatcher urlsMatcher = null;
        synchronized (urlsMatcherMap) {
            if (urlsMatcherMap.containsKey(UrlServiceUtils.USER_ADD_PACKAGE)) {
                urlsMatcher = urlsMatcherMap.get(UrlServiceUtils.USER_ADD_PACKAGE);
            }
        }
        if (urlsMatcher != null) {
            res = urlsMatcher.isMatch(host, path);
        }
        return res;
    }


    public List<PackageUrlSet> getBlackList() {
        synchronized (packageBlackUrls) {
            return PackageUrlSet.copy(packageBlackUrls.values());
        }
    }

    public List<PackageUrlSet> getUrlList() {
        synchronized (packageUrls) {
            return PackageUrlSet.copy(packageUrls.values());
        }
    }

    private void reloadBlackUrlMatcherList() {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                Map<String, UrlsMatcher> res = UrlServiceUtils.createUrlsMatcher(packageBlackUrls);
                synchronized (urlsMatcherMap) {
                    urlsMatcherMap.clear();
                    urlsMatcherMap.putAll(res);
                }
            }
        });
    }

    private void onReloadBlack(final List<PackageUrlSet> blackUrls) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                reloadBlackUrlMatcherList();
                ReloadReceiver.sendReloadBlack(context);
                // 发送广播告知需要重新加载黑名单
                for (PackageUrlSet packageUrlSet : blackUrls) {
                    removeMd5(packageUrlSet.packageName);
                    ReloadReceiver.sendReloadBlack(context, packageUrlSet.packageName);
                }
                setting.putPackageBlackList(packageBlackUrls);
            }
        });
    }

    private void removeMd5(final String packageName) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                md5Map.remove(packageName);
                setting.putMd5Map(md5Map);
            }
        });
    }

    private void updateMd5(final String packageName, final String md5) {
        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                md5Map.put(packageName, md5);
                setting.putMd5Map(md5Map);
            }
        });
    }

    public boolean chechUpdate(String packageName, String md5) {
        return !md5Map.containsKey(packageName) || !md5.equals(md5Map.get(packageName));
    }

    public UrlRule queryRules(String packageName) {
        HostPathsMap blackMap = new HostPathsMap();
        HostPathsMap whiteMap = new HostPathsMap();
        PackageUrlSet packageUrlSet = getPackageUrl(packageName);
        if (packageUrlSet != null) {
            synchronized (packageUrlSet.relativeUrls) {
                for(String url : packageUrlSet.relativeUrls) {
                    HostPath hostPath = HostPath.create(url);
                    if (checkIsIntercept(packageName, hostPath.host, hostPath.path)) {
                        blackMap.put(hostPath.host, hostPath.path);
                    } else {
                        whiteMap.put(hostPath.host, hostPath.path);
                    }
                }
            }
        }
        UrlRule urlRule = new UrlRule(blackMap, whiteMap);
        updateMd5(packageName, urlRule.md5);
        return urlRule;
    }
}
