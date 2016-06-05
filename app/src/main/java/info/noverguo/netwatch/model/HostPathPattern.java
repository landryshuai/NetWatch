package info.noverguo.netwatch.model;

import java.util.regex.Pattern;

import info.noverguo.netwatch.utils.UrlServiceUtils;

public class HostPathPattern {
    String host;
    String path;
    boolean usePattern;
    Pattern pattern;
    public HostPathPattern(String url) {
        this(HostPath.create(url));
    }
    public HostPathPattern(HostPath hostPath) {
        this(hostPath.host, hostPath.path);
    }
    public HostPathPattern(String host, String path) {
        this.host = host;
        this.path = path;
        String url = host + path;
        usePattern = UrlServiceUtils.isPatternUrl(url);
        if (usePattern) {
            pattern = UrlServiceUtils.getUrlPattern(url);
        }
    }

    public boolean isMatch(String url) {
        HostPath hostPath = HostPath.create(url);
        return isMatch(hostPath.host, hostPath.path);
    }
    public boolean isMatch(String host, String path) {
        if (usePattern) {
            return pattern.matcher(host + path).find();
        }
        return host.equals(this.host) && (UrlServiceUtils.isMatchAllPath(this.path) || path.startsWith(this.path));
    }
}