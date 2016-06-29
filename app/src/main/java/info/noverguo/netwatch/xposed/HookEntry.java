package info.noverguo.netwatch.xposed;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.utils.ClassUtils;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.NetworkUtils;
import info.noverguo.netwatch.xposed.methodhook.UrlCheckMethodHook;
import info.noverguo.netwatch.xposed.utils.UrlChecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by noverguo on 2016/2/17.
 */
public class HookEntry implements IXposedHookLoadPackage {
    private static final String LOCAL_HTTP = "http://127.0.0.9";
    private UrlChecker urlChecker;
    static int count = 0;
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lp) throws Throwable {
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
                hookNetwork(packageName, lp.classLoader);
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
    private UrlCheckMethodHook openConnectionMethodHook;
    private UrlCheckMethodHook webViewMethodHook;
    private UrlCheckMethodHook webViewMethodHook2;
    private UrlCheckMethodHook webViewMethodHook3;
    private void hookNetwork(final String packageName, final ClassLoader classLoader) {
        if (BuildConfig.DEBUG) DLog.d("hook: " + packageName);
        webViewMethodHook = new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String) param.args[0];
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                boolean res = urlChecker.checkIsInterceptUri(url);
                if (BuildConfig.DEBUG) DLog.i("====hook.WebView." + param.method.getName() + ": " + ClassUtils.getClassName(param.thisObject) + ", " + (res ? "intercept" : "pass") + ": " + url);
                if (res) {
                    param.args[0] = LOCAL_HTTP;
                }
            }
        };
        webViewMethodHook2 = new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String) param.args[0];
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                boolean res = urlChecker.checkIsInterceptUri(url);
                if (BuildConfig.DEBUG) DLog.i("====hook.WebView." + param.method.getName() + "2: " + ClassUtils.getClassName(param.thisObject) + ", " + (res ? "intercept" : "pass") + ": " + url);
                if (res) {
                    param.args[0] = LOCAL_HTTP;
                }
            }
        };
        webViewMethodHook3 = new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String) param.args[0];
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                boolean res = urlChecker.checkIsInterceptUri(url);
                if (BuildConfig.DEBUG) DLog.i("====hook.WebView." + param.method.getName() + "2: " + ClassUtils.getClassName(param.thisObject) + ", " + (res ? "intercept" : "pass") + ": " + url);
                if (res) {
                    param.args[0] = LOCAL_HTTP;
                }
            }
        };
        XposedBridge.hookAllMethods(WebView.class, "loadUrl", webViewMethodHook);
        XposedBridge.hookAllMethods(WebView.class, "loadUrl", webViewMethodHook);
        XposedBridge.hookAllMethods(WebView.class, "loadData", webViewMethodHook);
        XposedBridge.hookAllMethods(WebView.class, "loadDataWithBaseURL", webViewMethodHook);

        XposedBridge.hookAllConstructors(WebView.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                Class<?> clazz = param.thisObject.getClass();
                XposedBridge.hookAllMethods(clazz, "loadUrl", webViewMethodHook2);
                XposedBridge.hookAllMethods(clazz, "loadUrl", webViewMethodHook2);
                XposedBridge.hookAllMethods(clazz, "loadData", webViewMethodHook2);
                XposedBridge.hookAllMethods(clazz, "loadDataWithBaseURL", webViewMethodHook2);
            }
        });

        XposedHelpers.findAndHookMethod(WebView.class, "setWebViewClient", WebViewClient.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                if (BuildConfig.DEBUG) DLog.i("====hook.WebView.setWebViewClient: " + ClassUtils.getClassName(param.args[0]));
                Object client = param.args[0];
                if (client != null) {
                    Class clazz = ClassUtils.getDeclaredMethodClass(client.getClass(), "shouldOverrideUrlLoading", WebView.class, String.class);
                    if (clazz == null) {
                        if (BuildConfig.DEBUG) DLog.i("====hook.WebView.setWebViewClient.NoSuchMethod " );
                        return;
                    }
                    if (BuildConfig.DEBUG) DLog.i("====hook.WebView.setWebViewClient: " + clazz.getName());
                    XposedHelpers.findAndHookMethod(clazz, "shouldOverrideUrlLoading", WebView.class, String.class, new UrlCheckMethodHook(urlChecker) {
                        @Override
                        public void beforeHooked(MethodHookParam param) throws Throwable {
                            boolean res = urlChecker.checkIsInterceptUri((String)param.args[1]);
                            if (BuildConfig.DEBUG) DLog.i("----WebViewClient.shouldOverrideUrlLoading(): " + (res ? "intercept" : "pass") + ": " + param.args[1]);
                            if (res) {
                                param.args[1] = LOCAL_HTTP;
                            }
                            setIsCheck(!res);
                        }
                    });
                }
            }
        });


        openConnectionMethodHook = new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                URL url = (URL) param.thisObject;
                boolean res = urlChecker.isNetworkUri(url.getProtocol()) && urlChecker.checkIsInterceptSync(url.toString(), url.getHost(), url.getPath());
                if (BuildConfig.DEBUG) DLog.i("----URL.openConnection(): " + (res ? "intercept" : "pass") + ": " + url);
                if (res) {
//                    param.setThrowable(new IOException());
                    if (url.getProtocol().startsWith("https")) {
                        param.setResult(new HttpsURLConnection(url) {
                            @Override
                            public String getCipherSuite() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.getCipherSuite");
                                return null;
                            }

                            @Override
                            public Certificate[] getLocalCertificates() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.getLocalCertificates");
                                return new Certificate[0];
                            }

                            @Override
                            public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.getServerCertificates");
                                return new Certificate[0];
                            }

                            @Override
                            public void disconnect() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.disconnect");
                            }

                            @Override
                            public boolean usingProxy() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.usingProxy");
                                return false;
                            }

                            @Override
                            public void connect() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.connect");
                                throwException();
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.getInputStream");
                                throwException();
                                return null;
                            }

                            @Override
                            public OutputStream getOutputStream() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpsURLConnection.getOutputStream");
                                throwException();
                                return null;
                            }

                            private void throwException() throws IOException {
                                new IOException("your https has be hook").printStackTrace();
                                throw new IOException("your https has be hook");
                            }
                        });
                    } else {
                        param.setResult(new HttpURLConnection(url) {
                            @Override
                            public void disconnect() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpURLConnection.disconnect");
                            }

                            @Override
                            public boolean usingProxy() {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpURLConnection.usingProxy");
                                return false;
                            }

                            @Override
                            public void connect() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpURLConnection.connect");
                                throwException();
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpURLConnection.getInputStream");
                                throwException();
                                return null;
                            }

                            @Override
                            public OutputStream getOutputStream() throws IOException {
                                if (BuildConfig.DEBUG) XposedBridge.log("=====HttpURLConnection.getOutputStream");
                                throwException();
                                return null;
                            }

                            private void throwException() throws IOException {
                                new IOException("your http has be hook").printStackTrace();
                                throw new IOException("your http has be hook");
                            }
                        });
                    }
                }
                setIsCheck(!res);
            }
        };
        XposedHelpers.findAndHookMethod(URL.class, "openConnection", openConnectionMethodHook);
        XposedHelpers.findAndHookMethod(URL.class, "openConnection", Proxy.class, openConnectionMethodHook);


        XposedHelpers.findAndHookMethod(URI.class, "parseURI", String.class, boolean.class, new UrlCheckMethodHook(urlChecker) {
            @Override
            public void beforeHooked(MethodHookParam param) throws Throwable {
                String url = (String)param.args[0];
                boolean res = urlChecker.checkIsInterceptUri(url);
                if (res) {
                    param.args[0] = LOCAL_HTTP;
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
                if (!isHookIp() || isCheck()) {
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
                if (!isHookIp() || isCheck()) {
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

    }
}
