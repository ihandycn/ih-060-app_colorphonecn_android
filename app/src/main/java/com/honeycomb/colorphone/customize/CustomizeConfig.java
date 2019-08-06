package com.honeycomb.colorphone.customize;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.libraryconfig.HSLibraryConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import net.appcloudbox.common.utils.AcbParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CustomizeConfig implements HSLibraryConfig.ILibraryListener {

    private static final String TAG = CustomizeConfig.class.getSimpleName();

    /**
     * This notification is sent when a remote fetch succeeded AND data fetched is NOT identical to local data.
     */
    public static final String NOTIFICATION_CUSTOMIZE_CONFIG_CHANGED = "customize_config_changed";

    private static final String COMMON_CONFIG_NAME = "launcher-customize-common";
    private static final String VARIANT_CONFIG_NAME = "launcher-customize-variant";

    private static final int CONFIG_VERSION = HSApplication.getCurrentLaunchInfo().appVersionCode;

    private static final String LOCAL_COMMON_CONFIG_NAME = "customize-common.sp";
    private static final String LOCAL_VARIANT_CONFIG_NAME = "customize-variant.sp";

    private static CustomizeConfig sInstance;

    private static HSLibraryConfig.ILibraryProvider sCommonProvider = new ConfigProvider() {
        @Override
        public String getLibraryName() {
            return COMMON_CONFIG_NAME;
        }
    };

    private static HSLibraryConfig.ILibraryProvider sVariantProvider = new ConfigProvider() {
        @Override
        public String getLibraryName() {
            return VARIANT_CONFIG_NAME;
        }
    };

    private abstract static class ConfigProvider implements HSLibraryConfig.ILibraryProvider {
        @Override
        public int getLibraryVersionNumber() {
            return CONFIG_VERSION;
        }

        @Override
        public int getUpdateIntervalInSeconds() {
            return HSConfig.optInteger((int) TimeUnit.DAYS.toSeconds(1),
                    "Application", "LibraryConfig", "CustomizeUpdateInterval");
        }
    }

    private static Map<String, ?> sConfigMap;

    public static Map<String, ?> getMap(String... path) {
        Map<String, ?> map = HSMapUtils.getMap(getConfigMap(), path);
        if (map == null) {
            map = new HashMap<>();
        }
        return map;
    }

    public static List<?> getList(String... path) {
        List<?> list = HSMapUtils.getList(getConfigMap(), path);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static String getString(String defaultValue, String... path) {
        return HSConfig.optString(defaultValue, path);
    }

    public static int getInteger(int defaultValue, String... path) {
        return HSConfig.optInteger(defaultValue, path);
    }

    public static boolean getBoolean(boolean defaultValue, String... path) {
        return HSConfig.optBoolean(defaultValue, path);
    }

    public static Map<String, ?> getConfigMap() {
        return  HSConfig.getConfigMap();
    }

    private CustomizeConfig() {
    }

    /**
     * Performs a synchronous local load and starts an async remote fetch.
     */
    public synchronized static void init() {
        if (sInstance == null) {
            sInstance = new CustomizeConfig();
        }
        sInstance.doInit();
    }

    private void doInit() {
//        TraceCompat.beginSection("CustomizeConfig Init");
//        try {
//            HSLibrarySessionManager librarySessionManager = HSLibrarySessionManager.getInstance();
//
//            librarySessionManager.startSessionForLibrary(sCommonProvider);
//            Map<String, ?> localCommonData = parseLocalConfig(LOCAL_COMMON_CONFIG_NAME);
//            ConfigRegionsSupport.mergeRegions(localCommonData);
//
//            librarySessionManager.startSessionForLibrary(sVariantProvider);
//            Map<String, ?> localVariantData = parseLocalConfig(LOCAL_VARIANT_CONFIG_NAME);
//            ConfigRegionsSupport.mergeRegions(localVariantData);
//
//            HSLog.d(TAG, "Load customize config, fire a remote fetch");
//            HSLibraryConfig libraryConfig = HSLibraryConfig.getInstance();
//            libraryConfig.startForLibrary("TODO",
//                    localCommonData, sCommonProvider, this);
//            libraryConfig.startForLibrary("TODO",
//                    localVariantData, sVariantProvider, this);
//
//            mergeAndPublish();
//        } finally {
//            TraceCompat.endSection();
//        }
    }

    private Map<String, ?> parseLocalConfig(String localConfigName) {
        HSLog.d(TAG, "Load local customize config");
        return AcbParser.parse(HSApplication.getContext().getAssets(), localConfigName);
    }

    @Override
    public void onRemoteConfigDataChanged() {
        HSLog.i(TAG, "Customize config data changed");
        mergeAndPublish();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_CONFIG_CHANGED);
    }

    private void mergeAndPublish() {
        Map<String, ?> common = HSLibraryConfig.getInstance()
                .getDataForLibrary(sCommonProvider);
        Map<String, ?> variant = HSLibraryConfig.getInstance()
                .getDataForLibrary(sVariantProvider);

        HashMap<String, Object> mergedConfig = new HashMap<>(common);
        HSMapUtils.mergeMaps(mergedConfig, variant);

        sConfigMap = mergedConfig;
    }
}
