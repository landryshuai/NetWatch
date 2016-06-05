package info.noverguo.netwatch.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.noverguo.netwatch.utils.SizeUtils;
import info.noverguo.netwatch.utils.UrlServiceUtils;

/**
 * Created by noverguo on 2016/5/21.
 */

public class HostPathsMap implements Parcelable {
    public Map<String, Set<String>> hostMap;

    public synchronized boolean containHost(String host) {
        return hostMap.containsKey(host);
    }

    public synchronized boolean isEmptyHost(String host) {
        return SizeUtils.isEmpty(hostMap.get(host));
    }

    public synchronized boolean containPath(String host, String path) {
        return hostMap.get(host).contains(path);
    }

    public synchronized boolean startWithPath(String host, String path) {
        for (String p : hostMap.get(host)) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean contain(String host, String path) {
        return containHost(host) && containPath(host, path);
    }

    public synchronized void put(String host, String path) {
        if (!containHost(host)) {
            hostMap.put(host, new HashSet<String>());
        }
        hostMap.get(host).add(path);
    }

    public synchronized void useEmptyCollections() {
        for (String key : hostMap.keySet()) {
            Set<String> value = hostMap.get(key);
            if (UrlServiceUtils.isMatchAllPath(value)) {
                hostMap.put(key, new HashSet<String>(0));
            }
        }
    }

    public HostPathsMap() {
        hostMap = new HashMap<>(0);
    }

    public synchronized void reset(HostPathsMap src) {
        this.hostMap.clear();
        for (String key : src.hostMap.keySet()) {
            this.hostMap.put(key, new HashSet<>(src.hostMap.get(key)));
        }
    }

    protected HostPathsMap(Parcel in) {
        int size = in.readInt();
        hostMap = new HashMap<>(size);
        for (int i=0;i<size;++i) {
            String key = in.readString();
            int valueSize = in.readInt();
            Set<String> value = new HashSet<>(valueSize);
            hostMap.put(key, value);
            for (int j=0;j<valueSize;++j) {
                value.add(in.readString());
            }
        }
    }

    public static final Creator<HostPathsMap> CREATOR = new Creator<HostPathsMap>() {
        @Override
        public HostPathsMap createFromParcel(Parcel in) {
            return new HostPathsMap(in);
        }

        @Override
        public HostPathsMap[] newArray(int size) {
            return new HostPathsMap[size];
        }
    };

    @Override
    public int describeContents() {
        return SizeUtils.getSize(hostMap);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(SizeUtils.getSize(hostMap));
        for(String key : hostMap.keySet()) {
            dest.writeString(key);
            Set<String> value = hostMap.get(key);
            dest.writeInt(SizeUtils.getSize(value));
            if (value != null) {
                for (String val : value) {
                    dest.writeString(val);
                }
            }
        }
    }

    @Override
    public String toString() {
        return hostMap.toString();
    }
}
