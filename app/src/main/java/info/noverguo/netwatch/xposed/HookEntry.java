package info.noverguo.netwatch.xposed;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.service.LocalService;
import info.noverguo.netwatch.utils.DLog;

/**
 * Created by noverguo on 2016/2/17.
 */
public class HookEntry implements IXposedHookLoadPackage {
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

        AppContext.get().init(lp, new AppContext.Callback() {
            @Override
            public void appOnCreate(Context context) {
                if (BuildConfig.DEBUG) DLog.d(context.getClass().getName() + ".onCreate");
                LocalService localService = new LocalService(context);
                new NetworkHook(context, localService).hookNetwork(packageName, lp.classLoader);
                new ClickHideHook(context, localService).hookClick();
            }
        });
        ++count;
    }


}
