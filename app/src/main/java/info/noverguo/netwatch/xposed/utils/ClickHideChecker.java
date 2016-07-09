package info.noverguo.netwatch.xposed.utils;

import android.content.Context;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import info.noverguo.netwatch.receiver.ReloadReceiver;
import info.noverguo.netwatch.service.LocalService;

/**
 * Created by noverguo on 2016/6/30.
 */

public class ClickHideChecker {
    boolean isClickHook = false;
    Context context;
    LocalService localService;
    Set<ViewId> hideViews = new HashSet<>();
    public ClickHideChecker(Context context, LocalService localService) {
        this.context = context;
        this.localService = localService;
        initData();
        ReloadReceiver.registerReloadClickHide(context, new Runnable() {
            @Override
            public void run() {
                XposedBridge.log("ClickHideChecker.receiverReloadClickHide");
                initData();
            }
        });
    }
    private void initData() {
        localService.checkClickHide(new LocalService.BooleanResultCallback() {
            @Override
            public void onResult(boolean res) {
                XposedBridge.log("ClickHideChecker.checkClickHide: " + res);
                isClickHook = res;
            }
        });
    }

    public boolean isClickHook() {
        return isClickHook;
    }

    public void addHideView(View hideView) {
        hideViews.add(new ViewId(hideView));
    }

    public boolean isHideView(View view) {
        return hideViews.contains(new ViewId(view));
    }

    public Set<ViewId> getHideViews() {
        return new HashSet<>(hideViews);
    }

    public static class ViewId {
        public String className;
        public int width;
        public int height;
        public int left;
        public int top;
        public int right;
        public int bottom;
        public ViewId(View view) {
            className = view.getClass().getName();
            width = view.getWidth();
            height = view.getHeight();
            left = view.getLeft();
            top = view.getTop();
            right = view.getRight();
            bottom = view.getBottom();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewId viewId = (ViewId) o;

            if (width != viewId.width) return false;
            if (height != viewId.height) return false;
            if (left != viewId.left) return false;
            if (top != viewId.top) return false;
            if (right != viewId.right) return false;
            if (bottom != viewId.bottom) return false;
            return className != null ? className.equals(viewId.className) : viewId.className == null;

        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + width;
            result = 31 * result + height;
            result = 31 * result + left;
            result = 31 * result + top;
            result = 31 * result + right;
            result = 31 * result + bottom;
            return result;
        }
    }

}
