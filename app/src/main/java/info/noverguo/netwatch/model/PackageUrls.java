package info.noverguo.netwatch.model;

import java.util.List;

/**
 * Created by noverguo on 2016/5/18.
 */
public class PackageUrls extends PackageUrl {
    public List<String> relativeUrls;
    public PackageUrls(String packageName, List<String> relativeUrls) {
        super(packageName, packageName);
        this.relativeUrls = relativeUrls;
    }
}
