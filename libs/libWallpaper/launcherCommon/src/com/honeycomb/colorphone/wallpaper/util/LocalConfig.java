package com.honeycomb.colorphone.wallpaper.util;

import android.support.v4.os.TraceCompat;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import net.appcloudbox.common.utils.AcbMapUtils;
import net.appcloudbox.common.utils.AcbParser;

import java.util.List;
import java.util.Map;

public class LocalConfig {

    private static final String TAG = LocalConfig.class.getSimpleName();

    private static final String LOCAL_CONFIG_NAME = "config-local.lw";

    private static Map<String, ?> sConfigMap;

    private static boolean sInit;

    private LocalConfig() {
    }

    public static String getString(String defaultValue, String... path) {
        initIfNeed();
        return HSMapUtils.optString(sConfigMap, defaultValue, path);
    }

    public static boolean getBoolean(boolean defaultValue, String... path) {
        initIfNeed();
        return HSMapUtils.optBoolean(sConfigMap, defaultValue, path);
    }

    public static int getInteger(int defaultValue, String... path) {
        initIfNeed();
        return HSMapUtils.optInteger(sConfigMap, defaultValue, path);
    }

    public static List<?> getList(String... path) {
        initIfNeed();
        return AcbMapUtils.getList(sConfigMap, path);
    }

    public static Map<String, ?> getMap(String... path) {
        initIfNeed();
        return AcbMapUtils.getMap(sConfigMap, path);
    }

    /**
     * Performs a synchronous local load and starts an async remote fetch.
     */
    public synchronized static void initIfNeed() {
        if (sInit) {
            return;
        }
        TraceCompat.beginSection("LocalConfig Init");
        try {
            sConfigMap = loadConfig();
            sInit = true;
        } finally {
            TraceCompat.endSection();
        }
    }

    private static Map<String, ?> loadConfig() {
        Map<String, ?> rootData = parseConfigFile();
        ConfigRegionsSupport.mergeRegions(rootData);
        return AcbMapUtils.getMap(rootData, ConfigRegionsSupport.KEY_DATA);
    }

    private static Map<String, ?> parseConfigFile() {
        HSLog.d(TAG, "Load local config");
        return AcbParser.parse(HSApplication.getContext().getAssets(), LOCAL_CONFIG_NAME);
    }
}
