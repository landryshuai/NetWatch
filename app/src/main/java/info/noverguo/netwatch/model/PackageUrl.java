package info.noverguo.netwatch.model;

public class PackageUrl {
    public String packageName;
    public String url;
    public PackageUrl(String packageName, String url) {
        this.packageName = packageName;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackageUrl that = (PackageUrl) o;

        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
        return url != null ? url.equals(that.url) : that.url == null;

    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

//    public static List<PackageUrl>
}