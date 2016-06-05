package info.noverguo.netwatch;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by noverguo on 2016/5/21.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        LeakCanary.install(this);
        ApplicationInfo applicationInfo = getApplicationInfo();
        if ((applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().penaltyDeath().build());
        }
    }
}
