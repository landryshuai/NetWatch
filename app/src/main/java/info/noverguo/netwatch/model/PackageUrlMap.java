package info.noverguo.netwatch.model;

import java.util.List;

/**
 * Created by noverguo on 2016/5/18.
 */
public class PackageUrlMap extends PackageUrl {
    public List<PackageUrl> relativePackageUrls;
    public boolean show = false;
    public PackageUrlMap(PackageUrl packageUrl, List<PackageUrl> relativePackageUrls) {
        super(packageUrl.packageName, packageUrl.url);
        this.relativePackageUrls = relativePackageUrls;
    }
    public int size() {
        return show ? relativePackageUrls.size() : 0;
    }
}
