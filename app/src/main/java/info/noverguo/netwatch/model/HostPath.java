package info.noverguo.netwatch.model;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by noverguo on 2016/5/11.
 */
public class HostPath {
    public String host;
    public String path;
    public HostPath(String host, String path) {
        this.host = host;
        this.path = path;
    }
    public static HostPath create(String u) {
        u = u.trim();
        if (u.startsWith("#") || TextUtils.isEmpty(u)) {
            return null;
        }
        URL url;
        String host;
        String path;
        try {
            url = new URL(u);
            host = url.getHost();
            path = url.getPath();
        } catch (MalformedURLException e) {
            int start = u.indexOf("://");
            if (start > -1) {
                start = start + 3;
            } else {
                start = 0;
            }
            int end = u.indexOf("/", start + 3);
            if (end > 0) {
                path = u.substring(end);
            } else {
                end = u.length();
                path = "/";
            }
            host = u.substring(start, end);
        }
        int idx = path.lastIndexOf("/");
        if (idx > -1) {
            path = path.substring(0, idx + 1);
        }
        return new HostPath(host, path);
    }

    public static String toSimpleUrl(String url) {
        return create(url).toString();
    }

    @Override
    public String toString() {
        return host + path;
    }
}
