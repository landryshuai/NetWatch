package info.noverguo.netwatch.xposed;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.NetworkUtils;
import info.noverguo.netwatch.xposed.utils.UrlChecker;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by noverguo on 2016/2/17.
 */
public class HookEntry implements IXposedHookLoadPackage {
    private UrlChecker urlChecker;
    static int count = 0;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
//        if(!lp.isFirstApplication) {
//            return;
//        }
        if (BuildConfig.DEBUG) DLog.i("handleLoadPackage: " + lp.packageName);
        // 不注入自己
        if("info.noverguo.netwatch".equals(lp.packageName) || count > 0) {
            return;
        }
        if (lp.appInfo == null) {
            if (BuildConfig.DEBUG) DLog.d("appinfo is null");
            return;
        }
        final String packageName = lp.packageName;
        DLog.setHead(packageName);

        String applicationClass = lp.appInfo.className;
        if (applicationClass == null) {
            applicationClass = Application.class.getName();
        } else {
            try {
                XposedHelpers.findMethodExact(applicationClass, lp.classLoader, "onCreate");
            } catch (NoSuchMethodError e) {
                applicationClass = Application.class.getName();
            }
        }
        if (BuildConfig.DEBUG) DLog.d("app class: " + applicationClass + ", " + count++);
        XposedHelpers.findAndHookMethod(applicationClass, lp.classLoader, "onCreate", new MethodHook() {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                urlChecker = new UrlChecker((Context) param.thisObject);
                hookNetwork(packageName);
            }
        });
    }
    // 1秒内同一线程的hook不作拦截处理
    //
    final int TIMEOUT = 1000;
    ThreadLocal<Long> threadLocals = new ThreadLocal<>();
    private boolean isCheck() {
        Long val = threadLocals.get();
        if (val == null || val == -1) {
            return false;
        }
        return System.currentTimeMillis() - val < TIMEOUT;
    }
    private void setIsCheck(boolean isCheck) {
        if (isCheck) {
            threadLocals.set(System.currentTimeMillis());
        } else {
            threadLocals.set(-1L);
        }
    }
    private void hookNetwork(final String packageName) {
        if (BuildConfig.DEBUG) DLog.d("hook: " + packageName);
        XposedHelpers.findAndHookMethod(URL.class, "openConnection", new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                URL url = (URL)param.thisObject;
                boolean res = urlChecker.isNetworkUri(url.getProtocol()) && urlChecker.checkIsInterceptSync(url.toString(), url.getHost(), url.getPath());
                if (res) {
                    param.setResult(null);
                }
                if (BuildConfig.DEBUG) DLog.i("----URL.openConnection(): " + (res ? "intercept" : "pass") + ": " + url);
                setIsCheck(!res);
            }
        });

        XposedHelpers.findAndHookMethod(URI.class, "parseURI", String.class, boolean.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String)param.args[0];
                boolean res = urlChecker.checkIsInterceptUri(url);
                if (res) {
                    param.args[0] = "http://127.0.0.9";
                }
                if (BuildConfig.DEBUG) DLog.i("----URI.parseURI(): " + (res ? "intercept" : "pass") + ": " + url);
                setIsCheck(!res);
            }
        });

        XposedHelpers.findAndHookMethod(InetAddress.class, "getAllByNameImpl", String.class, int.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String)param.args[0];
                if (BuildConfig.DEBUG) DLog.i("----InetAddress.getAllByNameImpl().isCheck(): " + url + ", " + NetworkUtils.isIp(url) + ", " + isCheck());
                if (isCheck()) {
                    return;
                }
                boolean res = urlChecker.checkIsInterceptSocket(url);
                if (res) {
                    param.args[0] = null;
                }
                if (BuildConfig.DEBUG) DLog.i("----InetAddress.getAllByNameImpl(): " + (res ? "intercept" : "pass") + ": " + url);
                setIsCheck(!res);
            }
        });

        XposedHelpers.findAndHookMethod(Socket.class, "connect", SocketAddress.class, int.class, new UrlCheckMethodHook(urlChecker) {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (BuildConfig.DEBUG) DLog.i("----Socket.onConnect().isCheck(): " + param.args[0] + ", " + isCheck());
                if (isCheck()) {
                    return;
                }
                boolean res = false;
                if(param.args[0] instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) param.args[0];
                    res = urlChecker.checkIsInterceptSocket(isa.getHostName(), isa.getAddress().getHostAddress(), isa.getPort());
                }
                if (res) {
                    param.args[0] = null;
                }
                if (BuildConfig.DEBUG) DLog.i("----Socket.Socket(): " + (res ? "intercept" : "pass") + ": " + param.args[0]);
                setIsCheck(!res);
            }
        });

        XposedHelpers.findAndHookMethod(WebView.class, "loadUrl", String.class, Map.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (BuildConfig.DEBUG) DLog.i("----WebView.loadUrl().isCheck(): " + param.args[0] + ", " + isCheck());
                boolean res = urlChecker.checkIsInterceptUri((String)param.args[0]);
                if (res) {
                    param.args[0] = "";
                }
                if (BuildConfig.DEBUG) DLog.i("----WebView.loadUrl(): " + (res ? "intercept" : "pass") + ": " + param.args[0]);
                setIsCheck(!res);
            }
        });
    }
}
