package info.noverguo.netwatch.xposed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.service.LocalService;
import info.noverguo.netwatch.utils.ClassUtils;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.xposed.methodhook.ClickHideMethodHook;
import info.noverguo.netwatch.xposed.utils.ClickHideChecker;

/**
 * Created by noverguo on 2016/6/30.
 */

public class ClickHideHook {
    Context context;
    LocalService localService;
    ClickHideChecker clickHideChecker;
    public ClickHideHook(Context context, LocalService localService) {
        this.context = context;
        this.localService = localService;
        this.clickHideChecker = new ClickHideChecker(context, localService);
    }
    boolean runningCheck = false;
    public boolean performClick(final View view, final MotionEvent event) {
        if (!clickHideChecker.isClickHook() || runningCheck) {
            return false;
        }
        runningCheck = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(AppContext.get().getCurrentActivity()).setTitle("隐藏提示").setMessage("是否隐藏此视图?")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clickHideChecker.addHideView(view);
                        view.setVisibility(View.GONE);
                        runningCheck = false;
                    }
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        view.callOnClick();
                        runningCheck = false;
                    }
                });
        builder.create().show();
        return true;
    }

    public void hookClick() {
        if (BuildConfig.DEBUG) DLog.d("ClickHideHook.hookClick");
        XposedBridge.hookAllConstructors(View.class, new ClickHideMethodHook(clickHideChecker) {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
//                if (BuildConfig.DEBUG) DLog.d("ClickHideHook.View.hookAllConstructors: " + param.thisObject.getClass());
                View view = (View) param.thisObject;
                XposedHelpers.findAndHookMethod(ClassUtils.getDeclaredMethodClass(view.getClass(), "dispatchTouchEvent", MotionEvent.class),
                        "dispatchTouchEvent", MotionEvent.class, new ClickHideMethodHook(clickHideChecker) {
                    @Override
                    public void afterHooked(final MethodHookParam param) throws Throwable {
                        // TODO 由于是after，因此无法停止点击事件传递
                        MotionEvent event = (MotionEvent) param.args[0];
                        if (event == null) {
                            return;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (performClick((View) param.thisObject, event)) {
                                if (BuildConfig.DEBUG) DLog.i("ClickHideHook.View.dispatchTouchEvent: " + param.thisObject.getClass());
                                param.setResult(true);
                            }
                        }
                    }
                });
            }
        });

        XposedHelpers.findAndHookMethod(View.class, "measure", int.class, int.class, new ClickHideMethodHook(clickHideChecker) {
            @Override
            public void afterHooked(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                if (!clickHideChecker.isClickHook()) {
                    return;
                }
                if (clickHideChecker.isHideView(view)) {
                    view.setVisibility(View.GONE);
                    return;
                }
            }
        });
    }
}
