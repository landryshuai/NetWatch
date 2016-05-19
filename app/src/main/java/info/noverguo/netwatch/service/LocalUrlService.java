package info.noverguo.netwatch.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.UrlServiceUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by noverguo on 2016/5/10.
 */
public class LocalUrlService {
    private Context context;
    private String packageName;
    public LocalUrlService(Context context) {
        this.context = context.getApplicationContext();
        packageName = context.getPackageName();
    }
    private IUrlService urlRemoteService;
    private ServiceConnection mRemoteConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DLog.d("LocalUrlService.onServiceConnected");
            urlRemoteService = IUrlService.Stub.asInterface(service);
            runIfNeed();
        }

        public void onServiceDisconnected(ComponentName className) {
            DLog.d("LocalUrlService.onServiceDisconnected");
            urlRemoteService = null;
        }
    };

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    LinkedList<Runnable> tasks = new LinkedList<>();
    private boolean init() {
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
//    public void check(final PackageUrlSet unknownList, final CheckCallback callback) {
//        DLog.d("LocalUrlService.check");
//        init();
//        unknownList.packageName = packageName;
//        tasks.add(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    DLog.d("LocalUrlService.urlRemoteService.check: " + unknownList.relativeUrls);
//                    callback.onCheck(unknownList, urlRemoteService.check(unknownList));
//                } catch (RemoteException e) {
//                    callback.onError(e);
//                }
//                runIfNeed();
//            }
//        });
//        runIfNeed();
//    }

    public Set<String> checkHost(final String url, final String host, final String path) {
        DLog.d("LocalUrlService.checkHost: " + url + ", " + host + ", " + path);
        if (!init()) {
            return null;
        }
        final AtomicReference<List<String>> result = new AtomicReference<>();
        final AtomicBoolean isRun = new AtomicBoolean(false);
        tasks.addFirst(new Runnable() {
            @Override
            public void run() {
                try {
                    result.set(urlRemoteService.checkHost(packageName, url, host, path));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                synchronized (host) {
                    host.notify();
                }
                isRun.set(true);
                runIfNeed();
            }
        });
        runIfNeed();
        while(!isRun.get()) {
            try {
                synchronized (host) {
                    host.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List<String> blackUrls = result.get();
        DLog.d("LocalUrlService.checkHost result: " + blackUrls);
        if (blackUrls == null || UrlServiceUtils.isNull(blackUrls)) {
            return null;
        }
        if (blackUrls.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(blackUrls);
    }

    public void getAccessUrls(final GetUrlsCallback callback) {
        DLog.d("LocalUrlService.getUrls");
        if (!init()) {
            return;
        }
        tasks.add(new Runnable() {
            @Override
            public void run() {
                try {
                    DLog.d("LocalUrlService.urlRemoteService.getAccessUrls");
                    if (callback != null) {
                        callback.onGet(urlRemoteService.getAccessUrls());
                    }
                } catch (RemoteException e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
                runIfNeed();
            }
        });
        runIfNeed();
    }

    public void getBlackUrls(final GetUrlsCallback callback) {
        DLog.d("LocalUrlService.getUrls");
        if (!init()) {
            return;
        }
        tasks.add(new Runnable() {
            @Override
            public void run() {
                try {
                    DLog.d("LocalUrlService.urlRemoteService.getBlackUrls");
                    if (callback != null) {
                        callback.onGet(urlRemoteService.getBlackUrls());
                    }
                } catch (RemoteException e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
                runIfNeed();
            }
        });
        runIfNeed();
    }

    public void addBlackUrls(final List<PackageUrlSet> blackUrls, final ErrorCallback callback) {
        DLog.d("LocalUrlService.addBlackUrls: " + blackUrls);
        if (!init()) {
            return;
        }
        tasks.add(new Runnable() {
            @Override
            public void run() {
                try {
                    DLog.d("LocalUrlService.urlRemoteService.addBlackUrls");
                    urlRemoteService.addBlackUrls(blackUrls);
                } catch (RemoteException e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
                runIfNeed();
            }
        });
        runIfNeed();
    }

    public void removeBlackUrls(final List<PackageUrlSet> blackUrls, final ErrorCallback callback) {
        DLog.d("LocalUrlService.removeBlackUrls: " + blackUrls);
        if (!init()) {
            return;
        }
        tasks.add(new Runnable() {
            @Override
            public void run() {
                try {
                    DLog.d("LocalUrlService.urlRemoteService.addBlackUrls");
                    urlRemoteService.removeBlackUrls(blackUrls);
                } catch (RemoteException e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
                runIfNeed();
            }
        });
        runIfNeed();
    }

    private void runIfNeed() {
        DLog.d("LocalUrlService.runIfNeed: " + urlRemoteService + ", " + tasks);
        if (urlRemoteService == null || tasks.isEmpty()) {
            return;
        }
        executorService.execute(tasks.removeFirst());
    }

    public interface ErrorCallback {
        void onError(RemoteException e);
    }

    public static abstract class GetUrlsCallback implements ErrorCallback {
        public abstract void onGet(List<PackageUrlSet> result);
        public void onError(RemoteException e) {
        }
    }
}
