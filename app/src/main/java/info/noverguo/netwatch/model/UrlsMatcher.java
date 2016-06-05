package info.noverguo.netwatch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import info.noverguo.netwatch.utils.UrlServiceUtils;

/**
 * Created by noverguo on 2016/5/28.
 */

public class UrlsMatcher {
    HostPathsMap hostPathsMap = new HostPathsMap();
    List<Pattern> regexList = new ArrayList<>();
    public UrlsMatcher() {
    }

    public void add(String url) {
        HostPath hostPath = HostPath.create(url);
        add(hostPath.host, hostPath.path);
    }

    public void add(String host, String path) {
        String url = host + path;
        if (UrlServiceUtils.isPatternUrl(url)) {
            regexList.add(UrlServiceUtils.getUrlPattern(url));
        } else {
            hostPathsMap.put(host, path);
        }
    }

    public boolean isMatch(String url) {
        HostPath hostPath = HostPath.create(url);
        return isMatch(hostPath.host, hostPath.path);
    }

    public boolean isMatch(String host, String path) {
        if (hostPathsMap.containHost(host)) {
            if (hostPathsMap.isEmptyHost(host) || hostPathsMap.containPath(host, path) || hostPathsMap.startWithPath(host, path)) {
                return true;
            }
        }
        if(!regexList.isEmpty()) {
            String url = host + path;
            for (Pattern pattern : regexList) {
                if (pattern.matcher(url).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void useEmptyCollections() {
        hostPathsMap.useEmptyCollections();
    }
}
