package com.honeycomb.colorphone.recentapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.boost.AppInfo;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author sundxing
 */
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

    private static List<RecentAppInfo> sRecentAppsCache = null;

   // TODO cache

    public static void tryShowSmartAssistant() {
        HSLog.d(TAG, "tryShowSmartAssistant");
        Threads.postOnThreadPoolExecutor(new Runnable() {
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
                if (!SmartAssistantUtils.couldShowByCountAndCache()) {
                    SmartAssistantUtils.clearRecentAppsCache();
                    debugLog("app count is less than 5");
                    return;
                }
                HSLog.d(TAG, "do ShowSmartAssistant");

                Threads.postOnMainThread(new Runnable() {
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

        if (BuildConfig.DEBUG) {
            Threads.postOnMainThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(HSApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static boolean isOnDesktop() {
       return false;
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

    public static void setUserEnable(boolean enable) {
        Preferences.get(PREF_FILE_NAME).putBoolean(PREF_KEY_SMART_ASSISTANT_USER_ENABLED, enable);
    }

    public static boolean isEnabled() {
        return isUserEnabled() && isConfigEnabled();
    }

    public static boolean isUserEnabled() {
        return Preferences.get(PREF_FILE_NAME).getBoolean(PREF_KEY_SMART_ASSISTANT_USER_ENABLED, true);
    }

    public static boolean isConfigEnabled() {
        return ModuleUtils.isShowModulesDueToConfig() || HSConfig.optBoolean(DEFAULT_CONFIG_ENABLED, "Application", "RecentApps", "Recent_Apps_Enable");
    }

    public static boolean isShowOnlyOnDesktop() {
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Recent_Apps_Show_Only_On_Desktop");
    }

    public static boolean gainUsageAccessOnFirstLaunch() {
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Usage_Access_Gain_From_First_Screen");
    }

    public static boolean showUsageAccessTip() {
        return HSConfig.optBoolean(true, "Application", "RecentApps", "Usage_Access_Alert_Show");
    }

    public static String getUsageAccessTipText() {
        return HSConfig.optString("", "Application", "RecentApps", "Usage_Access_Alert_Text");
    }

    private static long getConfigIntervalTimeMills() {
        int seconds = HSConfig.optInteger(DEFAULT_CONFIG_INTERVAL_TIME_SECONDS, "Application", "RecentApps", "IntervalTimeSeconds");
        return seconds * 1000;
    }

    public static void recordSmartAssistantShowTime() {
        Preferences.get(PREF_FILE_NAME).putLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, System.currentTimeMillis());
    }

    public static boolean couldShowSmartAssistantByIntervalTime() {
        long lastShowTime = Preferences.get(PREF_FILE_NAME).getLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, 0);
        if (System.currentTimeMillis() - lastShowTime > getConfigIntervalTimeMills()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFirstShowSmartAssistant() {
        long lastShowTime = Preferences.get(PREF_FILE_NAME).getLong(PREF_KEY_LAST_SHOW_SMART_ASSISTANT_TIME, 0);
        if (lastShowTime == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean couldShowByCountAndCache() {
        return getSmartAssistantApps().size() > SMART_ASSISTANT_AT_LEAST_COUNT;
    }


    public static List<AppInfo> getRecentlyInstallApps() {
        List<AppInfo> resultList = new ArrayList<>();
        List<AppInfo> allApps = SystemAppsManager.getInstance().getAllAppInfos();


        Collections.sort(allApps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                if (o1.getPackageInfo().firstInstallTime > o2.getPackageInfo().firstInstallTime) {
                    return -1;
                } else if (o1.getPackageInfo().firstInstallTime < o2.getPackageInfo().firstInstallTime) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        long timeMills = System.currentTimeMillis() - RECENTLY_INSTALL_APP_TIME_MILLS;
        for (int i = 0; i < allApps.size(); i++) {
            AppInfo AppInfo = allApps.get(i);

            if (AppInfo.getPackageInfo().firstInstallTime > timeMills) {
                resultList.add(AppInfo);
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

    @Deprecated
    public static List<RecentAppInfo> getSmartAssistantApps() {

        if (sRecentAppsCache != null) {
            return sRecentAppsCache;
        }

        List<RecentAppInfo> resultList = new ArrayList<>();

        sRecentAppsCache = resultList;
        return resultList;
    }

    public static void clearRecentAppsCache() {
        sRecentAppsCache = null;
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
