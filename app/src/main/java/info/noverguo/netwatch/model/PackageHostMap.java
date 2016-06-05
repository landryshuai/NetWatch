package info.noverguo.netwatch.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noverguo on 2016/5/18.
 */
public class PackageHostMap extends PackageUrl {
    public List<String> relativePackageUrls;
    public PackageHostMap(String packageName, String host) {
        super(packageName, host);
        this.relativePackageUrls = new ArrayList<>();
    }
    public void add(String url) {
        relativePackageUrls.add(url);
    }
}
