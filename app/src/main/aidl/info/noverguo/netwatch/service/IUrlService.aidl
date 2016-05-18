// IUrlService.aidl
package info.noverguo.netwatch.service;
import info.noverguo.netwatch.model.PackageUrlSet;

// Declare any non-default types here with import statements

interface IUrlService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    PackageUrlSet check(in PackageUrlSet unknownList);
    List<String> checkHost(in String packageName, in String url, in String host, in String path);
    List<PackageUrlSet> getAccessUrls();
    List<PackageUrlSet> getBlackUrls();
    void addBlackUrls(in List<PackageUrlSet> blackUrls);
    void removeBlackUrls(in List<PackageUrlSet> blackUrls);
}
