package info.noverguo.netwatch.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.model.UrlRule;
import info.noverguo.netwatch.utils.DLog;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by noverguo on 2016/5/10.
 */
public class LocalService {
    private Context context;
    private String packageName;
    public LocalService(Context context) {
        this.context = context.getApplicationContext();
        this.packageName = context.getPackageName();
    }
    private IUrlService urlRemoteService;
    private ServiceConnection mRemoteConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (BuildConfig.DEBUG) DLog.i("LocalService.onServiceConnected");
            synchronized (LocalService.this) {
                urlRemoteService = IUrlService.Stub.asInterface(service);
            }
            runIfNeed();
        }

        public void onServiceDisconnected(ComponentName className) {
            if (BuildConfig.DEBUG) DLog.i("LocalService.onServiceDisconnected");
            synchronized (LocalService.this) {
                urlRemoteService = null;
                init = false;
            }
        }
    };

    ExecutorService executorService;
    LinkedList<Runnable> tasks = new LinkedList<>();
    boolean init = false;
    private boolean init() {
        synchronized (this) {
            if (init) {
                return true;
            }
            init = true;
        }
        if (urlRemoteService == null) {
            Intent intent = new Intent(IUrlService.class.getName());
            // 5.0后需要显式指定包名
            intent.setPackage("info.noverguo.netwatch");
            try {
                context.bindService(intent, mRemoteConnection, Context.BIND_AUTO_CREATE);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean checkIsInterceptUrl(final String url, final String host, final String path) {
        if (BuildConfig.DEBUG) DLog.i("LocalService.checkIsInterceptUrl: " + url + ", " + host + ", " + path);
        if (urlRemoteService != null) {
            IUrlService urlService;
            synchronized (this) {
                urlService = urlRemoteService;
            }
            if (urlService != null) {
                try {
                    if (BuildConfig.DEBUG) DLog.i("LocalService.checkIsInterceptUrl.sync");
                    return urlService.checkIsInterceptUrl(packageName, url, host, path);
                } catch (RemoteException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }
        }
        init();
        return false;
    }

    public void queryRules(final QueryRulesCallback callback) {
        if (BuildConfig.DEBUG) DLog.i("LocalService.getUrls");
        if (callback == null) {
            return;
        }
        if (urlRemoteService != null) {
            IUrlService urlService;
            synchronized (this) {
                urlService = urlRemoteService;
            }
            if (urlService != null) {
                try {
                    if (BuildConfig.DEBUG) DLog.i("LocalService.queryRules.sync");
                    callback.onRules(urlService.queryRules(packageName));
                } catch (RemoteException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (BuildConfig.DEBUG) DLog.i("LocalService.queryRules.task");
                        callback.onRules(urlRemoteService.queryRules(packageName));
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void checkUpdate(final String md5, final CheckUpdateCallback callback) {
        if (BuildConfig.DEBUG) DLog.i("LocalService.checkUpdate");
        if (callback == null) {
            return;
        }
        if (urlRemoteService != null) {
            IUrlService urlService;
            synchronized (this) {
                urlService = urlRemoteService;
            }
            if (urlService != null) {
                try {
                    if (BuildConfig.DEBUG) DLog.i("LocalService.checkUpdate.sync");
                    if (urlService.checkUpdate(packageName, md5)) {
                        callback.onUpdate();
                    }
                } catch (RemoteException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (BuildConfig.DEBUG) DLog.i("LocalService.checkUpdate.task");
                        if (urlRemoteService.checkUpdate(packageName, md5)) {
                            callback.onUpdate();
                        }
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void needCheck(final BooleanResultCallback callback) {
        if (BuildConfig.DEBUG) DLog.i("LocalService.needCheck");
        if (callback == null) {
            return;
        }
        if (urlRemoteService != null) {
            IUrlService urlService;
            synchronized (this) {
                urlService = urlRemoteService;
            }
            if (urlService != null) {
                try {
                    if (BuildConfig.DEBUG) DLog.i("LocalService.needCheck.sync");
                    callback.onResult(urlService.needCheck(packageName));
                } catch (RemoteException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (BuildConfig.DEBUG) DLog.i("LocalService.needCheck.task");
                        callback.onResult(urlRemoteService.needCheck(packageName));
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void checkClickHide(final BooleanResultCallback callback) {
        if (BuildConfig.DEBUG) DLog.i("LocalService.checkClickHide");
        if (callback == null) {
            return;
        }
        if (urlRemoteService != null) {
            IUrlService urlService;
            synchronized (this) {
                urlService = urlRemoteService;
            }
            if (urlService != null) {
                try {
                    if (BuildConfig.DEBUG) DLog.i("LocalService.checkClickHide.sync");
                    callback.onResult(urlService.checkClickHide(packageName));
                } catch (RemoteException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (BuildConfig.DEBUG) DLog.i("LocalService.checkClickHide.task");
                        callback.onResult(urlRemoteService.checkClickHide(packageName));
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    private void runIfNeed() {
        if (BuildConfig.DEBUG) DLog.i("LocalService.runIfNeed: " + urlRemoteService + ", " + tasks);
        if (urlRemoteService == null) {
            return;
        }
        synchronized (tasks) {
            if (tasks.isEmpty()) {
                return;
            }
        }
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Runnable task;
                synchronized (tasks) {
                    if (tasks.isEmpty()) {
                        return;
                    }
                    task = tasks.removeFirst();
                }
                task.run();
                runIfNeed();
            }
        });
    }

    public static class ErrorCallback {
        void onError(RemoteException e) {}
    }

    public static abstract class QueryRulesCallback extends ErrorCallback {
        public abstract void onRules(UrlRule urlRule);
    }

    public static abstract class CheckUpdateCallback extends ErrorCallback {
        public abstract void onUpdate();
    }
    public static abstract class BooleanResultCallback extends ErrorCallback {
        public abstract void onResult(boolean res);
    }
}
