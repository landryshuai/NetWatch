package info.noverguo.netwatch.xposed.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.model.HostPathsMap;
import info.noverguo.netwatch.model.UrlRule;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalService;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.xposed.HSetting;

/**
 * Created by noverguo on 2016/5/9.
 */
public class UrlChecker {
    boolean needCheck = true;
    boolean hookIp = true;
    Set<String> fixWhiteHosts = new HashSet<>();
    HostPathsMap whiteList = new HostPathsMap();
    HostPathsMap blackList = new HostPathsMap();
    Object md5Lock = new Object();
    String md5 = "";
    LocalService urlService;
    HSetting setting;
    ExecutorService executorService = Executors.newSingleThreadExecutor();;
    public UrlChecker(Context context, LocalService localService) {
        urlService = localService;
        setting = new HSetting(context);
        initFixWhiteList();
        ReloadReceiver.registerReloadBlack(context, new Runnable() {
            @Override
            public void run() {
                reloadRule();
            }
        });
        ReloadReceiver.registerReloadNeedCheck(context, new Runnable() {
            @Override
            public void run() {
                checkNeedCheck();
            }
        });
        load();
    }

    private void load() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HostPathsMap blackMap = setting.getBlackMap();
                synchronized (blackList) {
                    blackList.reset(blackMap);
                }
                HostPathsMap whiteMap = setting.getWhiteMap();
                synchronized (whiteList) {
                    whiteList.reset(whiteMap);
                }
                String m = setting.getMd5();
                synchronized (md5Lock) {
                    md5 = m;
                }
                checkNeedCheck();
            }
        });
    }

    private void checkNeedCheck() {
        urlService.needCheck(new LocalService.BooleanResultCallback() {
            @Override
            public void onResult(boolean res) {
                needCheck = res;
                checkUpdate();
            }
        });
    }

    private void checkUpdate() {
        if (!needCheck) {
            return;
        }
        urlService.checkUpdate(md5, new LocalService.CheckUpdateCallback() {
            @Override
            public void onUpdate() {
                reloadRule();
            }
        });
    }

    private void reloadRule() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                urlService.queryRules(new LocalService.QueryRulesCallback() {
                    @Override
                    public void onRules(UrlRule urlRule) {
                        if (BuildConfig.DEBUG) DLog.i("queryRules.black: " + urlRule.blackMap);
                        if (BuildConfig.DEBUG) DLog.i("queryRules.white: " + urlRule.whiteMap);
                        synchronized (blackList) {
                            blackList.reset(urlRule.blackMap);
                        }
                        synchronized (whiteList) {
                            whiteList.reset(urlRule.whiteMap);
                        }
                        synchronized (md5Lock) {
                            md5 = urlRule.md5;
                        }
                        storeBlack();
                        storeWhite();
                        setting.putMd5(md5);
                    }
                });
            }
        });
    }

    private  void storeBlack() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                setting.putBlackMap(blackList);
            }
        });
    }

    private  void storeWhite() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                setting.putWhiteMap(whiteList);
            }
        });
    }

    private void initFixWhiteList() {
        fixWhiteHosts.add("127.0.0.9");
    }

    public boolean needCheck() {
        return needCheck;
    }

    public boolean isNetworkUri(String uri) {
        return uri.startsWith("http") || uri.startsWith("ftp") || uri.startsWith("gmsg");
    }
    public boolean checkIsInterceptUri(String uri) {
        if (isNetworkUri(uri)) {
            Uri url = Uri.parse(uri);
            return checkIsInterceptSync(uri, url.getHost(), url.getPath());
        }
        return false;
    }

    private boolean justCheckIsIntercept(String host, String path) throws UnknownHostException {
        if (fixWhiteHosts.contains(host)) {
            return false;
        }
        if (BuildConfig.DEBUG) DLog.d("validate: " + host + ", " + path);
        // 白名单的检查速度比较快，因此先检查它
        synchronized (whiteList) {
            if (whiteList.contain(host, path)) {
                return false;
            }
        }
        synchronized (blackList) {
            if (blackList.containHost(host)) {
                // path列表为空代表拦截全部
                if (blackList.isEmptyHost(host) || blackList.containPath(host, path)) {
                    return true;
                }
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
    public synchronized boolean checkIsInterceptSync(String url, String host, String path) {
        try {
            path = cutPath(path);
            return justCheckIsIntercept(host, path);
        } catch (UnknownHostException e) {
            if (urlService.checkIsInterceptUrl(url, host, path)) {
                blackList.put(host, path);
                storeBlack();
                return true;
            } else {
                whiteList.put(host, path);
                storeWhite();
                return false;
            }
        }
    }

    private String cutPath(String path) {
        int idx = path.indexOf('/', 1);
        if (idx > 0) {
            path = path.substring(0, idx);
        }
        return path;
    }

    public boolean checkIsInterceptHost(String url, String host) {
        return checkIsInterceptSync(url, host, "");
    }

    public boolean checkIsInterceptSocket(String host) {
        return checkIsInterceptHost("socket://" + host, host);
    }

    public boolean checkIsInterceptSocket(String host, String address, int port) {
        String val = host;
        if (TextUtils.isEmpty(val)) {
            val = address;
        }
        if (port > 0) {
            if (port == 80) {
                return checkIsInterceptHost("http://" + val, val);
            } else if (port == 443) {
                return checkIsInterceptHost("https://" + val, val);
            }
            return checkIsInterceptSocket(val + ":" + port);
        }
        return checkIsInterceptSocket(val);
    }

    public boolean isHookIp() {
        return hookIp;
    }
}
