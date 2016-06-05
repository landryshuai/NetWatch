package info.noverguo.netwatch.utils;

import android.util.Log;

import com.tencent.noverguo.hooktest.BuildConfig;

import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.HostPathPattern;
import info.noverguo.netwatch.model.HostPathsMap;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.model.UrlsMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by noverguo on 2016/5/11.
 */
public class UrlServiceUtils {
    public static final String USER_ADD_PACKAGE = "usu_user_edit";
    public static boolean isUserAddPackage(String packageName) {
        return USER_ADD_PACKAGE.equals(packageName);
    }
    public static boolean isMatchAllPath(Set<String> value) {
        return value.contains("") || value.contains("/");
    }
    public static boolean isMatchAllPath(String value) {
        return value.equals("") || value.equals("/");
    }
    public static boolean isPatternUrl(String url) {
        return url.contains("*");
    }

    public static Pattern getUrlPattern(String regex) {
        return Pattern.compile(regex.replaceAll("\\.", "\\\\.").replaceAll("\\?", "\\\\?").replaceAll("\\&", "\\\\&").replaceAll("\\#", "\\\\#").replaceAll("\\*", ".+"));
    }

    public static List<PackageUrl> getMatchPackageUrls(String url, PackageUrlSet pus) {
        return _getMatchPackageUrls(new HostPathPattern(url), pus);
    }

    public static List<PackageUrl> getMatchPackageUrls(String url, List<PackageUrlSet> packageUrlList) {
        HostPathPattern hostPathPattern = new HostPathPattern(url);
        List<PackageUrl> result = new ArrayList<>();
        for (PackageUrlSet pus : packageUrlList) {
            result.addAll(_getMatchPackageUrls(hostPathPattern, pus));
        }
        return result;
    }

    private static List<PackageUrl> _getMatchPackageUrls(HostPathPattern hostPathPattern, PackageUrlSet pus) {
        List<PackageUrl> result = new ArrayList<>();
        if (pus != null) {
            String p = pus.packageName;
            for (String u : pus.relativeUrls) {
                HostPath hostPath = HostPath.create(u);
                if (hostPathPattern.isMatch(hostPath.host, hostPath.path)) {
                    result.add(new PackageUrl(p, u));
                }
            }
        }
        return result;
    }

    public static Map<String, UrlsMatcher> createUrlsMatcher(Map<String, PackageUrlSet> packageBlackUrls) {
        Map<String, UrlsMatcher> urlsMatcherMap = new HashMap<>();
        synchronized (packageBlackUrls) {
            for(String key : packageBlackUrls.keySet()) {
                PackageUrlSet pus = packageBlackUrls.get(key);
                UrlsMatcher urlsMatcher = new UrlsMatcher();
                synchronized (pus.relativeUrls) {
                    for (String u : pus.relativeUrls) {
                        HostPath hostPath = HostPath.create(u);
                        if (hostPath == null) {
                            continue;
                        }
                        if (BuildConfig.DEBUG) DLog.d("add black packageName : " + u + ", " + hostPath.host + ", " + hostPath.path);
                        String host = hostPath.host;
                        String path = hostPath.path;
                        urlsMatcher.add(host, path);
                    }
                }
                urlsMatcher.useEmptyCollections();
                urlsMatcherMap.put(pus.packageName, urlsMatcher);
            }
        }
        return urlsMatcherMap;
    }
}
