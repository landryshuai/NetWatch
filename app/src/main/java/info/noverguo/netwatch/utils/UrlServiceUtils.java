package info.noverguo.netwatch.utils;

import android.util.Log;

import info.noverguo.netwatch.model.HostPath;
import info.noverguo.netwatch.model.PackageUrl;
import info.noverguo.netwatch.model.PackageUrlSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by noverguo on 2016/5/11.
 */
public class UrlServiceUtils {
    private static List<String> nullList;
    private static Set<String> nullSet;
    private static final String NULL_STRING = "usu_null";
    public static final String USER_ADD_PACKAGE = "usu_user_edit";
    static {
        nullList = new ArrayList<>();
        nullList.add("null");
        nullSet = new HashSet<>(nullList);
    }
    public static List<String> getNullList() {
        return nullList;
    }
    public static Set<String> getNullSet() {
        return nullSet;
    }
    public static boolean isNull(Collection<String> collection) {
        return SizeUtils.getSize(collection) == 1 && NULL_STRING.equals(collection.iterator().next());
    }
    public static boolean isUserAddPackage(String packageName) {
        return USER_ADD_PACKAGE.equals(packageName);
    }
    public static boolean isPatternUrl(String url) {
        return url.contains("*");
    }

    public static Pattern getUrlPattern(String regex) {
        return Pattern.compile(regex.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".+"));
    }

    public static boolean isMatchUrl(String regex, String url) {
        return getUrlPattern(regex).matcher(url).find();
    }

    public static boolean isMatchBlack(String regex, String url) {

        if (isPatternUrl(regex)) {
            return getUrlPattern(regex).matcher(url).find();
        }
        return url.startsWith(regex);
    }

    public static boolean isMatchBlack(List<PackageUrlSet> blackUrlList, String url) {
        url = HostPath.create(url).toString();
        for (PackageUrlSet pus : blackUrlList) {
            for (String regex : pus.relativeUrls) {
                regex = HostPath.create(regex).toString();
                Log.i("Match", "isMatchBlack: " + regex + " , " + url + ", " + isMatchUrl(regex, url));
                if (isMatchBlack(regex, url)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<PackageUrl> getMatchPackageUrls(String regex, List<PackageUrlSet> packageUrlList) {
        List<PackageUrl> result = new ArrayList<>();
        Pattern pattern = null;
        if (UrlServiceUtils.isPatternUrl(regex)) {
            pattern = UrlServiceUtils.getUrlPattern(regex);
        }
        for (PackageUrlSet pus : packageUrlList) {
            String p = pus.packageName;
            for(String u : pus.relativeUrls) {
                u = HostPath.create(u).toString();
                Log.i("Match", "getMatchPackageUrls: " + regex + " , " + u + ", " + u.startsWith(regex));
                if (pattern != null){
                    if (pattern.matcher(u).find()) {
                        result.add(new PackageUrl(p, u));
                    }
                } else {
                    if (u.startsWith(regex)) {
                        result.add(new PackageUrl(p, u));
                    }
                }
            }
        }
        return result;
    }
}
