package info.noverguo.netwatch.xposed;

import info.noverguo.netwatch.BuildConfig;

import de.robv.android.xposed.XC_MethodHook;
import info.noverguo.netwatch.xposed.utils.UrlChecker;

public class UrlCheckMethodHook extends XC_MethodHook {
	private UrlChecker urlChecker;

	public UrlCheckMethodHook(UrlChecker urlChecker) {
		this.urlChecker = urlChecker;
	}

	@Override
	final public void beforeHookedMethod(MethodHookParam param) throws Throwable {
		if (!urlChecker.needCheck()) {
			return;
		}
		try {
			beforeHooked(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
		}
	}

	@Override
	final public void afterHookedMethod(MethodHookParam param) throws Throwable {
		if (!urlChecker.needCheck()) {
			return;
		}
		try {
			afterHooked(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
		}
	}

	@Override
	final public void call(Param param) throws Throwable {
		if (!urlChecker.needCheck()) {
			return;
		}
		try {
			calling(param);
		} catch(Throwable e) {
			if (BuildConfig.DEBUG) e.printStackTrace();
		}
	}

	public void beforeHooked(MethodHookParam param) throws Throwable {
	}

	public void afterHooked(MethodHookParam param) throws Throwable {
	}

	public void calling(Param param) throws Throwable {
	}
}