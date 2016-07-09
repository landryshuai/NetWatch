package info.noverguo.netwatch.xposed.methodhook;

import de.robv.android.xposed.XC_MethodHook;
import info.noverguo.netwatch.BuildConfig;
import info.noverguo.netwatch.xposed.utils.ClickHideChecker;

public class ClickHideMethodHook extends XC_MethodHook {
	private ClickHideChecker clickHideChecker;
	private static boolean inHook = false;

	public ClickHideMethodHook(ClickHideChecker clickHideChecker) {
		this.clickHideChecker = clickHideChecker;
	}

	@Override
	final public void beforeHookedMethod(MethodHookParam param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			beforeHooked(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
		}
		inHook = false;
	}

	@Override
	final public void afterHookedMethod(MethodHookParam param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			afterHooked(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
		}
		inHook = false;
	}

	@Override
	final public void call(Param param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		try {
			calling(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
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