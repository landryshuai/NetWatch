// IUrlService.aidl
package info.noverguo.netwatch.service;
import info.noverguo.netwatch.model.PackageUrlSet;
import info.noverguo.netwatch.model.HostPathsMap;
import info.noverguo.netwatch.model.UrlRule;

// Declare any non-default types here with import statements

interface IUrlService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    PackageUrlSet check(in PackageUrlSet unknownList);
    boolean checkIsInterceptUrl(in String packageName, in String url, in String host, in String path);
    UrlRule queryRules(in String packageName);
    boolean checkUpdate(in String packageName, in String md5);
    boolean needCheck(in String packageName);
//    List<PackageUrlSet> getAccessUrls();
//    List<PackageUrlSet> getBlackUrls();
//    void addBlackUrls(in List<PackageUrlSet> blackUrls);
//    void removeBlackUrls(in List<PackageUrlSet> blackUrls);
}
