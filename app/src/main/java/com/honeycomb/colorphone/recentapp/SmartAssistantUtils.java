package com.honeycomb.colorphone.recentapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.acb.utils.ConcurrentUtils;
import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.boost.AppInfo;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.monitor.topapp.TopAppManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SmartAssistantUtils {

    public static final String PREF_FILE_NAME = "colorphone.recentapps";
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


    public static void tryShowSmartAssistant() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {

                if (!SmartAssistantUtils.isUserEnabled()) {
                    debugLog("user disable");
                    return;
                }

                if (!SmartAssistantUtils.isConfigEnabled()) {
                    debugLog("config disable");

                    return;
                }

                if (!SmartAssistantUtils.couldShowSmartAssistantByIntervalTime()) {
                    debugLog("config interval time");
                    return;
                }

                if (isShowOnlyOnDesktop() && !isOnDesktop()) {
                    debugLog("Not on Desktop");
                    return;
                }

                // Huge works to do, so we do it at last.
                if (!SmartAssistantUtils.couldShowByCount()) {
                    debugLog("app count is less than 5");
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

    private static void debugLog(final String msg) {
        HSLog.d(TAG, msg);

        ConcurrentUtils.postOnMainThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(HSApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private static boolean isOnDesktop() {
        String topPkg = TopAppManager.getInstance().getTopApp();
        if (TextUtils.isEmpty(topPkg)) {
            return false;
        }

        String defaultLauncher = getDefaultLauncher();
        return TextUtils.equals(topPkg, defaultLauncher);
    }

    /**
     * @return Package name of current default launcher.
     */
    public @NonNull
    static String getDefaultLauncher() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo;
        try {
            resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        } catch (Exception e) {
            return "";
        }
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            String packageName = resolveInfo.activityInfo.packageName;
            return packageName == null ? "" : packageName;
        }
        return "";
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
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Usage_Access_Gain_From_First_Screen");
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
                if (o1.firstInstallTime > o2.firstInstallTime) {
                    return -1;
                } else if (o1.firstInstallTime < o2.firstInstallTime) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        long timeMills = System.currentTimeMillis() - RECENTLY_INSTALL_APP_TIME_MILLS;
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo packageInfo = packageInfoList.get(i);

            if (packageInfo.firstInstallTime > timeMills) {
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

    private static void transPackageIntoAppList(List<RecentAppInfo> results, String packageName, int type) {
        AppInfo appInfo = SystemAppsManager.getInstance().getAppInfoByPkgName(packageName);
        if (appInfo != null) {
            results.add(buildRecentApp(appInfo, type));
        }
    }

    public static List<RecentAppInfo> getSmartAssistantApps() {
        List<RecentAppInfo> resultList = new ArrayList<>();

        // 4 recent apps
        List<AppInfo> recentlyInstallApps = getRecentlyInstallApps();
        List<String> frequentlyAppsByTime = RecentAppManager.getInstance().getAppUsageListRecently(SMART_ASSISTANT_AT_MOST_COUNT);
        List<String> frequentlyAppsByUsed = RecentAppManager.getInstance().getAppUsageListFrequently(SMART_ASSISTANT_AT_MOST_COUNT);

        int firstMax = frequentlyAppsByTime.size();
        for (int i = 0; i < firstMax; i++) {
            String packageName = frequentlyAppsByTime.get(i);
            transPackageIntoAppList(resultList, packageName, RecentAppInfo.TYPE_RECENTLY_USED);
            HSLog.d(TAG, "Recently used app: " + packageName);

            if (resultList.size() >= COUNT_APP_RECENTLY_OPEN) {
                break;
            }
        }

        for (int i = 0; i < recentlyInstallApps.size(); i++) {
            String packageName = recentlyInstallApps.get(i).getPackageName();

            if (!isExistApplicationInfo(resultList, packageName)) {
                resultList.add(buildRecentApp(recentlyInstallApps.get(i), RecentAppInfo.TYPE_NEW_INSTALL));
                HSLog.d(TAG, "Recently install app: " + packageName);
            }
        }

        for (int i = 0; i < frequentlyAppsByUsed.size(); i++) {
            String pkgName = frequentlyAppsByUsed.get(i);

            if (!isExistApplicationInfo(resultList, pkgName)) {
                transPackageIntoAppList(resultList, pkgName, RecentAppInfo.TYPE_MOSTLY_USED);
                HSLog.d(TAG, "Frequently used app: " + pkgName);

                if (resultList.size() >= SMART_ASSISTANT_AT_MOST_COUNT) {
                    break;
                }
            }
        }
        return resultList;
    }

    private static RecentAppInfo buildRecentApp(AppInfo appInfo, int type) {
        return new RecentAppInfo(appInfo, type);
    }

    private static boolean isOurSelf(String packageName) {
        return TextUtils.equals(HSApplication.getContext().getPackageName(), packageName);
    }

    private static boolean isExistApplicationInfo(List<RecentAppInfo> list, String packageName) {
        boolean isExist = false;
        for (RecentAppInfo item : list) {
            if (TextUtils.equals(packageName, item.getPackageName())) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
}
