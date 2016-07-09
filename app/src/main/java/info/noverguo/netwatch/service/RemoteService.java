package info.noverguo.netwatch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.model.UrlRule;
import info.noverguo.netwatch.tools.AppDataManager;
import info.noverguo.netwatch.utils.DLog;

/**
 * Created by noverguo on 2016/5/10.
 */
public class RemoteService extends Service {


    AppDataManager appDataManager;
    @Override
    public void onCreate() {
        super.onCreate();
        appDataManager = AppDataManager.get(getApplicationContext());
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
            public boolean checkIsInterceptUrl(final String packageName, final String url, String host, String path) throws RemoteException {
                appDataManager.addPackageUrl(packageName, url);
                boolean res = appDataManager.checkIsIntercept(packageName, host, path);
                if (BuildConfig.DEBUG) DLog.i("onBind.checkIsInterceptUrl: " + ", " + host + path + ", " + res);
                return res;
            }

            @Override
            public UrlRule queryRules(String packageName) throws RemoteException {
                if (BuildConfig.DEBUG) DLog.i("onBind.queryRule: " + packageName);
                return appDataManager.queryRules(packageName);
            }

            @Override
            public boolean checkUpdate(String packageName, String md5) throws RemoteException {
                boolean needUpdate = appDataManager.chechUpdate(packageName, md5);
                if (BuildConfig.DEBUG) DLog.i("onBind.checkUpdate: " + packageName, md5, needUpdate);
                return needUpdate;
            }

            @Override
            public boolean needCheck(String packageName) throws RemoteException {
                return appDataManager.needCheck(packageName);
            }

            @Override
            public boolean checkClickHide(String packageName) throws RemoteException {
                return appDataManager.checkClickHide(packageName);
            }
        };
    }
}
