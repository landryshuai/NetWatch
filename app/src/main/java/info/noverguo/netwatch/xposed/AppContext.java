package info.noverguo.netwatch.xposed;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.LinkedList;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.utils.DLog;

/**
 * Created by noverguo on 2016/6/30.
 */

public class AppContext {
    private static AppContext sInst;
    private AppContext(){}
    public static AppContext get() {
        if (sInst == null) {
            synchronized (AppContext.class) {
                if (sInst == null) {
                    sInst = new AppContext();
                }
            }
        }
        return sInst;
    }
    Context appContext;
    LinkedList<Activity> activitys = new LinkedList<Activity>();
    public void init(final XC_LoadPackage.LoadPackageParam lp, final Callback callback) {
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
        if (BuildConfig.DEBUG) DLog.d("hook app class: " + applicationClass);
        XposedHelpers.findAndHookMethod(applicationClass, lp.classLoader, "onCreate", new MethodHook() {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                appContext = (Context) param.thisObject;
                callback.appOnCreate(appContext);
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new MethodHook() {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                super.afterHooked(param);
                activitys.addLast((Activity) param.thisObject);
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onPause", new MethodHook() {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                super.afterHooked(param);
                activitys.removeLast();
            }
        });
    }

    public Context getAppContext() {
        return appContext;
    }

    public Activity getCurrentActivity() {
        if (activitys.isEmpty()) {
            return null;
        }
        return activitys.getLast();
    }

    public interface Callback {
        void appOnCreate(Context context);
    }
}
