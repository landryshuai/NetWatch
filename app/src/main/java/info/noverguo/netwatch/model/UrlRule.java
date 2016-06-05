package info.noverguo.netwatch.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import info.noverguo.netwatch.utils.MD5Util;

/**
 * Created by noverguo on 2016/6/4.
 */

public class UrlRule implements Parcelable {
    public HostPathsMap blackMap;
    public HostPathsMap whiteMap;
    public String md5;
    public UrlRule(HostPathsMap blackMap, HostPathsMap whiteMap) {
        this.blackMap = blackMap;
        this.whiteMap = whiteMap;
        calMd5();
    }

    private void calMd5() {
        Gson gson = new Gson();
        md5 = MD5Util.encrypt_string(gson.toJson(blackMap));
    }

    public UrlRule(Parcel in) {
        blackMap = new HostPathsMap(in);
        whiteMap = new HostPathsMap(in);
        md5 = in.readString();
    }

    public static final Creator<UrlRule> CREATOR = new Creator<UrlRule>() {
        @Override
        public UrlRule createFromParcel(Parcel in) {
            return new UrlRule(in);
        }

        @Override
        public UrlRule[] newArray(int size) {
            return new UrlRule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        blackMap.writeToParcel(dest, flags);
        whiteMap.writeToParcel(dest, flags);
        dest.writeString(md5);
    }
}
