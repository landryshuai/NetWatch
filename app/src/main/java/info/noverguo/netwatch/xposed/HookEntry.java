package info.noverguo.netwatch.xposed;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.UrlChecker;

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
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
//        if(!lp.isFirstApplication) {
//            return;
//        }
        // 不注入自己
        if("com.tencent.noverguo.hooktest".equals(lp.packageName)) {
            return;
        }
        final String packageName = lp.packageName;
        DLog.setHead(packageName);
        if (lp.appInfo == null) {
            DLog.d("appinfo is null");
            return;
        }
//        if(!"com.baidu.mobads.demo.main".equals(lp.packageName)) {
//            return;
//        }

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
        DLog.d("app class: " + applicationClass);
        XposedHelpers.findAndHookMethod(applicationClass, lp.classLoader, "onCreate", new MethodHook() {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                urlChecker = new UrlChecker((Context) param.thisObject);
                hookNetwork(packageName);
            }
        });
    }

    private void hookNetwork(final String packageName) {
        DLog.d("hook: " + packageName);
        XposedHelpers.findAndHookMethod(URL.class, "openConnection", new MethodHook() {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                URL url = (URL)param.thisObject;
                if (urlChecker.isNetworkUri(url.getProtocol()) && !urlChecker.validateSync(url.toString(), url.getHost(), url.getPath())) {
                    DLog.i("----URI.openConnection(): intercept: " + url);
                    param.setResult(null);
                } else {
                    DLog.i("----URI.openConnection(): pass: " + url);
                }
            }
        });

        XposedHelpers.findAndHookMethod(URI.class, "parseURI", String.class, boolean.class, new MethodHook() {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (!urlChecker.validateUri((String)param.args[0])) {
                    DLog.i("----URI.parseURI(): intercept: " + param.args[0]);
                    param.args[0] = "";
                } else {
                    DLog.i("----URI.parseURI(): pass: " + param.args[0]);
                }
            }
        });

        XposedHelpers.findAndHookMethod(InetAddress.class, "getAllByNameImpl", String.class, int.class, new MethodHook() {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (!urlChecker.validateSocket((String)param.args[0])) {
                    DLog.i("----InetAddress.getAllByNameImpl(): intercept: " + param.args[0]);
                    param.args[0] = null;
                } else {
                    DLog.i("----InetAddress.getAllByNameImpl(): pass: " + param.args[0]);
                }
            }
        });

        XposedHelpers.findAndHookMethod(Socket.class, "connect", SocketAddress.class, int.class, new MethodHook() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                boolean result = false;
                if(param.args[0] instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) param.args[0];
                    result = urlChecker.validateSocket(isa.getHostName(), isa.getAddress().getHostAddress(), isa.getPort());
                }
                if (!result) {
                    DLog.i("----Socket.onConnect(): intercept: " + param.args[0]);
                    param.args[0] = null;
                } else {
                    DLog.i("----Socket.onConnect(): pass: " + param.args[0]);
                }
            }
        });

        XposedHelpers.findAndHookMethod(WebView.class, "loadUrl", String.class, Map.class, new MethodHook() {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (!urlChecker.validateUri((String)param.args[0])) {
                    DLog.i("----WebView.loadUrl(): intercept: " + param.args[0]);
                    param.args[0] = "";
                } else {
                    DLog.i("----WebView.loadUrl(): pass: " + param.args[0]);
                }
            }
        });
    }
}
