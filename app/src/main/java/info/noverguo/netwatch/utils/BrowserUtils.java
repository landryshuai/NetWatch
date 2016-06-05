package info.noverguo.netwatch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import info.noverguo.netwatch.ui.WebActivity;

/**
 * Created by noverguo on 2016/5/29.
 */

public class BrowserUtils {
    public static void openBrowserInNewTask(Context context, String url) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void openBrowser(Activity context, String url) {
//        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        WebActivity.showUrl(context, url);
    }
}
