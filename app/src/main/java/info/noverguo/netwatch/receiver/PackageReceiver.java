package info.noverguo.netwatch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.tools.UrlsManager;
import info.noverguo.netwatch.utils.DLog;

public class PackageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 安装了新应用
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            if (BuildConfig.DEBUG) DLog.d("android.intent.action.PACKAGE_ADDED: " + intent.getDataString());
        }
        // 重装了应用r
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            if (BuildConfig.DEBUG) DLog.d("android.intent.action.PACKAGE_REPLACED: " + intent.getDataString());
        }
        // 卸载了应用
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            if (BuildConfig.DEBUG) DLog.d("android.intent.action.PACKAGE_REMOVED: " + intent.getDataString());
            String dataString = intent.getDataString();
            if (dataString.startsWith("package:")) {
                UrlsManager.get(context).removePackage(dataString.substring("package:".length()).trim());
            }
        }
    }
}
