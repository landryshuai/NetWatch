package info.noverguo.netwatch.xposed;

import de.robv.android.xposed.XC_MethodHook;

public class MethodHook extends XC_MethodHook {
	public static boolean inHook = false;

	@Override
	public void beforeHookedMethod(MethodHookParam param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			beforeHooked(param);
		} catch(Throwable e) {
//			DLog.d(e);
		}
		inHook = false;
	}

	@Override
	public void afterHookedMethod(MethodHookParam param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			afterHooked(param);
		} catch(Throwable e) {
//			DLog.d(e);
		}
		inHook = false;
	}

	@Override
	public void call(Param param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			calling(param);
		} catch(Throwable e) {
//			DLog.d(e);
		}
		inHook = false;
	}

	public void beforeHooked(MethodHookParam param) throws Throwable {
	}

	public void afterHooked(MethodHookParam param) throws Throwable {
	}

	public void calling(Param param) throws Throwable {
	}
}