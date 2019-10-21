package com.acb.libwallpaper.live.customize;

import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeConfig {

    private static final String TAG = CustomizeConfig.class.getSimpleName();
    private static final String PREFIX_FIRST = "Application";
    private static final String PREFIX_SECOND = "Wallpapers";

    private static String[] getFixedPath(String... path) {
        String[] pathFixed = new String[path.length + 2];

        for (int i = 0; i < path.length; i++) {
            pathFixed[i + 2] = path[i];
        }

        pathFixed[0] = PREFIX_FIRST;
        pathFixed[1] = PREFIX_SECOND;

        return pathFixed;
    }

    public static Map<String, ?> getMap(String... path) {
        path = getFixedPath(path);
        Map<String, ?> map = HSConfig.getMap(path);
        if (map == null) {
            map = new HashMap<>();
        }
        return map;
    }

    public static List<?> getList(String... path) {
        path = getFixedPath(path);
        List<?> list = HSConfig.getList(path);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static String getString(String defaultValue, String... path) {
        path = getFixedPath(path);
        return HSConfig.optString(defaultValue, path);
    }

    public static int getInteger(int defaultValue, String... path) {
        path = getFixedPath(path);
        return HSConfig.optInteger( defaultValue, path);
    }

    public static boolean getBoolean(boolean defaultValue, String... path) {
        path = getFixedPath(path);
        return HSConfig.optBoolean(defaultValue, path);
    }

    private CustomizeConfig() {
    }

}
