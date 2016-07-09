package info.noverguo.netwatch.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by noverguo on 2016/6/29.
 */

public class ViewUtils {
    public static void removeView(final Context context, final View view) {
        view.setVisibility(View.GONE);
//        float adHeight = convertPixelsToDp(context, view.getHeight());
//        if(adHeight > 0) {
//
//            ViewGroup.LayoutParams params = view.getLayoutParams();
//            if (params == null) {
//                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//            } else {
//                params.height = 0;
//            }
//            view.setLayoutParams(params);
//        }
//
//        // preventing view not ready situation
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                float adHeight = convertPixelsToDp(context, view.getHeight());
//                if(adHeight > 0) {
//                    ViewGroup.LayoutParams params = view.getLayoutParams();
//                    if (params == null) {
//                        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//                    }
//                    else {
//                        params.height = 0;
//                    }
//                    view.setLayoutParams(params);
//                }
//
//            }
//        });
//        if(view.getParent() != null && view.getParent() instanceof ViewGroup) {
//            ViewGroup parent = (ViewGroup) view.getParent();
//            removeView(context, parent);
//        }
    }

    public static float convertPixelsToDp(Context context, float px){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
