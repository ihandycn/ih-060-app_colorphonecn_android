package com.acb.libwallpaper.live.util;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Dimensions;

import java.util.Locale;
import java.util.Map;

/**
 * A wrapper over {@link HSConfig} to provide multilingual and multi-APK support.
 */
@SuppressWarnings("unchecked")
public class LauncherConfig {

    public static String getDefaultString(Map<String, ?> map, String key) {
        String configString;
        Object objectFromKey = map.get(key);
        if (objectFromKey instanceof String) {
            configString = (String) objectFromKey;
        } else {
            Map<String, String> stringMap = (Map<String, String>) objectFromKey;
            if (stringMap == null) {
                return null;
            }
            configString = stringMap.get("Default");
        }
        return configString;
    }


        /**
         * Wraps {@link HSConfig#getString(String...)} and added multilingual support.
         *
         * @param path Config path
         */
    public static String getMultilingualString(String... path) {
        String configString;
        Map<String, String> stringMap = (Map<String, String>) HSConfig.getMap(path);
        if (stringMap.isEmpty()) {
            configString = HSConfig.getString(path);
        } else {
            configString = getStringForCurrentLanguage(stringMap);
        }
        return configString;
    }

    public static String getMultilingualString(Map<String, ?> map, String key) {
        String configString;
        Object objectFromKey = map.get(key);
        if (objectFromKey instanceof String) {
            configString = (String) objectFromKey;
        } else {
            Map<String, String> stringMap = (Map<String, String>) objectFromKey;
            if (stringMap == null) {
                return null;
            }
            configString = getStringForCurrentLanguage(stringMap);
        }
        return configString;
    }

    public static String getStringForCurrentLanguage(Map<String, String> stringMap) {
        String key = getLanguageString();
        String localeString = stringMap.get(key);
        if (localeString == null) {
            localeString = stringMap.get("Default");
        }
        return localeString;
    }

    public static String getLanguageString() {
        Locale locale = Dimensions.getLocale(HSApplication.getContext());
        String localeString = locale.getLanguage();
        if ("zh".equals(localeString)) {
            String country = locale.getCountry();
            if ("CN".equals(country)) {
                // Simplified Chinese
                localeString = "zh-rCN";
            }
        }
        return localeString;
    }
}
