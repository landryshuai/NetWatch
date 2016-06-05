package info.noverguo.netwatch.utils;

import java.util.regex.Pattern;

/**
 * Created by noverguo on 2016/5/21.
 */

public class NetworkUtils {
    public static boolean isIp(String val) {
        return Pattern.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$", val);
    }
}
