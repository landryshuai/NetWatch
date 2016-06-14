package info.noverguo.netwatch.model;

import android.os.Parcel;
import android.os.Parcelable;

import info.noverguo.netwatch.BuildConfig;

import info.noverguo.netwatch.utils.DLog;
import info.noverguo.netwatch.utils.SizeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by noverguo on 2016/5/10.
 */
public class PackageUrlSet implements Parcelable {
    public String packageName = "";
    public List<String> relativeUrls;

    public PackageUrlSet(String packageName) {
        this(packageName, new ArrayList<String>());
    }

    public PackageUrlSet(String packageName, List<String> relativeUrls) {
        this.packageName = packageName;
        this.relativeUrls = relativeUrls;
    }

    public PackageUrlSet(PackageUrlSet src) {
        this.packageName = src.packageName;
        this.relativeUrls = new ArrayList<>(src.relativeUrls);
    }

    @Override
    public String toString() {
        return "PackageUrlSet{" +
                "packageName='" + packageName + '\'' +
                ", relativeUrls=" + relativeUrls +
                '}';
    }

    public void add(String url) {
        if (!relativeUrls.contains(url)) {
            relativeUrls.add(url);
        }
    }

    public void addAll(Collection<String> urls) {
        HashSet<String> newUrls = new HashSet<>(relativeUrls);
        newUrls.addAll(urls);
        relativeUrls = new ArrayList<>(newUrls);
    }

    protected PackageUrlSet(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<PackageUrlSet> CREATOR = new Creator<PackageUrlSet>() {
        @Override
        public PackageUrlSet createFromParcel(Parcel in) {
            return new PackageUrlSet(in);
        }

        @Override
        public PackageUrlSet[] newArray(int size) {
            return new PackageUrlSet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel parcel) {
        packageName = parcel.readString();
        int size = parcel.readInt();
        relativeUrls = new ArrayList<>(size);
        for(int i=0;i<size;++i) {
            Set<String> value = new HashSet<>();
            int valueSize = parcel.readInt();
            for(int j=0;j<valueSize;++j) {
                value.add(parcel.readString());
            }
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(packageName);
        int size = SizeUtils.getSize(relativeUrls);
        parcel.writeInt(size);
        if (size > 0) {
            for (String key : relativeUrls) {
                parcel.writeString(key);
            }
        }
    }

    public static void put(Map<String, PackageUrlSet> dist, List<PackageUrlSet> src) {
        for(PackageUrlSet pus : src) {
            put(dist, pus);
        }
    }

    public static void put(Map<String, PackageUrlSet> map, PackageUrlSet pus) {
        put(map, pus.packageName, pus.relativeUrls);
    }

    public static void put(Map<String, PackageUrlSet> map, String packageName, List<String> urls) {
        if (!map.containsKey(packageName)) {
            map.put(packageName, new PackageUrlSet(packageName));
        }
        map.get(packageName).addAll(urls);
    }

    public static void put(Map<String, PackageUrlSet> map, String packageName, String url) {
        if (!map.containsKey(packageName)) {
            map.put(packageName, new PackageUrlSet(packageName));
        }
        map.get(packageName).add(url);
    }

    public static void remove(Map<String, PackageUrlSet> dist, List<PackageUrlSet> src) {
        for(PackageUrlSet pus : src) {
            remove(dist, pus);
        }
    }

    public static void remove(Map<String, PackageUrlSet> map, PackageUrlSet pus) {
        remove(map, pus.packageName, pus.relativeUrls);
    }

    public static void remove(Map<String, PackageUrlSet> map, String key, List<String> vals) {
        for (String val : vals) {
            remove(map, key, val);
        }
    }

    public static void remove(Map<String, PackageUrlSet> map, String key, String val) {
        if (map.containsKey(key)) {
            List<String> paths = map.get(key).relativeUrls;
            if (!paths.isEmpty()) {
                paths.remove(val);
            }
            if (paths.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public static List<PackageUrlSet> copy(Collection<PackageUrlSet> src) {
        if (BuildConfig.DEBUG) DLog.i("PackageUrlSet.copy: " + src);
        List<PackageUrlSet> dist = new ArrayList<>();
        for (PackageUrlSet pus : src) {
            dist.add(pus);
        }
        return dist;
    }

    public static List<PackageUrlSet> copy(Collection<PackageUrlSet> src, Set<String> checkPackages) {
        if (BuildConfig.DEBUG) DLog.i("PackageUrlSet.copy: " + src);
        List<PackageUrlSet> dist = new ArrayList<>();
        for (PackageUrlSet pus : src) {
            if (checkPackages.contains(pus.packageName)) {
                dist.add(pus);
            }
        }
        return dist;
    }
}
