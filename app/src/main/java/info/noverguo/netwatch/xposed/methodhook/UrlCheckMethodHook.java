package info.noverguo.netwatch.xposed.methodhook;

import info.noverguo.netwatch.BuildConfig;

import de.robv.android.xposed.XC_MethodHook;
import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.xposed.utils.UrlChecker;

public class UrlCheckMethodHook extends XC_MethodHook {
	private UrlChecker urlChecker;
	private static boolean inHook = false;

	public boolean isHookIp() {
		return urlChecker.isHookIp();
	}

	public UrlCheckMethodHook(UrlChecker urlChecker) {
		this.urlChecker = urlChecker;
	}

	@Override
	final public void beforeHookedMethod(MethodHookParam param) throws Throwable {
		if (inHook) {
			return;
		}
		inHook = true;
		if (BuildConfig.DEBUG) DLog.i("====----: " + urlChecker.isHookIp() +"" + urlChecker.needCheck());
		if (!urlChecker.needCheck()) {
			return;
		}
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
		if (!urlChecker.needCheck()) {
			return;
		}
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
		if (!urlChecker.needCheck()) {
			return;
		}
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