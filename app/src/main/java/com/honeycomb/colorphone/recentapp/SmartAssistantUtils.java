package com.honeycomb.colorphone.recentapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.acb.utils.ConcurrentUtils;
import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.boost.AppInfo;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SmartAssistantUtils {

    private static final String PREF_FILE_NAME = "colorphone.recentapps";
    public static final int SMART_ASSISTANT_AT_LEAST_COUNT = 4;
    public static final int SMART_ASSISTANT_AT_MOST_COUNT = 8;

    private static final int COUNT_APP_RECENTLY_INSTALL = 2;
    private static final int COUNT_APP_RECENTLY_OPEN = 4;

    private static final long RECENTLY_INSTALL_APP_TIME_MILLS = 3 * DateUtils.DAY_IN_MILLIS;

    public static final String PREF_KEY_SMART_ASSISTANT_USER_ENABLED = "smart_assistant_user_enabled";
    private static final int DEFAULT_CONFIG_INTERVAL_TIME_SECONDS = 2; //2min
    private static final boolean DEFAULT_CONFIG_ENABLED = true;
    private static final String PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME = "pref_key_last_show_smart_assistant_time";
    public static final String PREF_KEY_COULD_SHOW_SMART_ASSISTANT = "pref_key_could_show_smart_assistant";
    private static final String TAG = "SmartAssistantUtils";


    public static void showSmartAssistant() {
        if (!isOnDesktop()) {
            HSLog.d(TAG, "Not on Desktop");
            return;
        }
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                if (!SmartAssistantUtils.isUserEnabled()) {
                    HSLog.d(TAG, "user disable");
                    return;
                }

                if (!SmartAssistantUtils.isConfigEnabled()) {
                    HSLog.d(TAG, "config disable");

                    return;
                }

                if (!SmartAssistantUtils.couldShowSmartAssistantByIntervalTime()) {
                    HSLog.d(TAG, "config interval time");
                    return;
                }

                if (!SmartAssistantUtils.couldShowByCount()) {
                    HSLog.d(TAG, "app count is less than 5");
                    return;
                }
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent recentApp = new Intent(HSApplication.getContext(), SmartAssistantActivity.class);
                        Utils.startActivitySafely(HSApplication.getContext(), recentApp);
                    }
                });

            }
        });
    }

    private static boolean isOnDesktop() {
        // TODO
        return true;
    }

    static void disableByUser() {
        PreferenceHelper.get(PREF_FILE_NAME).putBoolean(PREF_KEY_SMART_ASSISTANT_USER_ENABLED, false);
    }

    public static boolean isEnabled() {
        return isUserEnabled() && isConfigEnabled();
    }

    public static boolean isUserEnabled() {
        return PreferenceHelper.get(PREF_FILE_NAME).getBoolean(PREF_KEY_SMART_ASSISTANT_USER_ENABLED, true);
    }

    public static boolean isConfigEnabled() {
        return HSConfig.optBoolean(DEFAULT_CONFIG_ENABLED, "Application", "RecentApps", "Enabled");
    }

    public static boolean isShowOnlyOnDesktop() {
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Recent_Apps_Show_Only_On_Desktop");
    }

    public static boolean gainUsageAccessOnFirstLaunch() {
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Ussage_Access_Gain_From_First_Screen");
    }

    private static long getConfigIntervalTimeMills() {
        int seconds = HSConfig.optInteger(DEFAULT_CONFIG_INTERVAL_TIME_SECONDS, "Application", "RecentApps", "IntervalTimeSeconds");
        return seconds * 1000;
    }

    public static void recordSmartAssistantShowTime() {
        PreferenceHelper.get(PREF_FILE_NAME).putLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, System.currentTimeMillis());
    }

    public static boolean couldShowSmartAssistantByIntervalTime() {
        long lastShowTime = PreferenceHelper.get(PREF_FILE_NAME).getLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, 0);
        if (System.currentTimeMillis() - lastShowTime > getConfigIntervalTimeMills()) {
            return true;
        } else {
            return false;
        }
    }
//
//    public static void recordCouldShowSmartAssistantByPresent(boolean isCouldShow) {
//        PreferenceHelper.get(PREF_FILE_NAME).putBoolean(PREF_KEY_COULD_SHOW_SMART_ASSISTANT, isCouldShow);
//    }
//
//    public static boolean couldShowSmartAssistantByPresent() {
//        return PreferenceHelper.get(PREF_FILE_NAME).getBoolean(PREF_KEY_COULD_SHOW_SMART_ASSISTANT, false);
//    }

    public static boolean isFirstShowSmartAssistant() {
        long lastShowTime = PreferenceHelper.get(PREF_FILE_NAME).getLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, 0);
        if (lastShowTime == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean couldShowByCount() {
        return getSmartAssistantApps().size() > SMART_ASSISTANT_AT_LEAST_COUNT;
    }


    public static List<AppInfo> getRecentlyInstallApps() {
        List<AppInfo> resultList = new ArrayList<>();
        List<PackageInfo> packageInfoList = SystemAppsManager.getInstance().getAllPackageInfos();

        Collections.sort(packageInfoList, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                if (o1.lastUpdateTime > o2.lastUpdateTime) {
                    return -1;
                } else if (o1.lastUpdateTime < o2.lastUpdateTime) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        long timeMills = System.currentTimeMillis() - RECENTLY_INSTALL_APP_TIME_MILLS;
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo packageInfo = packageInfoList.get(i);

            if (packageInfo.lastUpdateTime > timeMills) {
                AppInfo appInfo = SystemAppsManager.getInstance().getAppInfoByPkgName(packageInfo.packageName);
                if (appInfo != null) {
                    resultList.add(appInfo);
                }
            } else {
                break;
            }
            if (resultList.size() >= COUNT_APP_RECENTLY_INSTALL) {
                break;
            }
        }
        return resultList;
    }

    private static void transPackageIntoAppList(List<AppInfo> results, String packageName) {
        AppInfo appInfo = SystemAppsManager.getInstance().getAppInfoByPkgName(packageName);
        if (appInfo != null) {
            results.add(appInfo);
        } else {
            if (BuildConfig.DEBUG) {
//                Log.e()
//                throw new IllegalStateException("Pkg name = " + packageName + " not exists in SystemAppsManager!");
            }
        }
    }

    public static List<AppInfo> getSmartAssistantApps() {
        List<AppInfo> resultList = new ArrayList<>();

        // 4 recent apps
        List<AppInfo> recentlyInstallApps = getRecentlyInstallApps();
        List<String> frequentlyAppsByTime = RecentAppManager.getInstance().getAppUsageListRecently(COUNT_APP_RECENTLY_OPEN);
        List<String> frequentlyAppsByUsed = RecentAppManager.getInstance().getAppUsageListFrequently(SMART_ASSISTANT_AT_MOST_COUNT);

        int firstMax = frequentlyAppsByTime.size();
        for (int i = 0; i < firstMax; i++) {
            String packageName = frequentlyAppsByTime.get(i);
            transPackageIntoAppList(resultList, packageName);
            if (resultList.size() >= COUNT_APP_RECENTLY_OPEN) {
                break;
            }
        }

        for (int i = 0; i < recentlyInstallApps.size(); i++) {
            String key = recentlyInstallApps.get(i).getPackageName();
            if (!isExistApplicationInfo(resultList, key)) {
                resultList.add(recentlyInstallApps.get(i));
            }
        }

        for (int i = 0; i < frequentlyAppsByUsed.size(); i++) {
            String pkgName = frequentlyAppsByUsed.get(i);
            if (!isExistApplicationInfo(resultList, pkgName)) {
                transPackageIntoAppList(resultList, pkgName);
                if (resultList.size() >= SMART_ASSISTANT_AT_MOST_COUNT) {
                    break;
                }
            }
        }
        return resultList;
    }

    private static boolean isExistApplicationInfo(List<AppInfo> list, String packageName) {
        boolean isExist = false;
        for (AppInfo item : list) {
            if (TextUtils.equals(packageName, item.getPackageName())) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
}
