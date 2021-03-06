package com.honeycomb.colorphone.ad;

import android.text.format.DateUtils;

import com.colorphone.lock.util.ConfigUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class ConfigSettings {
    public static boolean showAdOnApplyTheme() {
        boolean configEnable = HSConfig.optBoolean(false,"Application", "FullScreen", "ThemeApply")
                || enabledThisVersion();
         HSLog.d("ConfigSettings", "AdOnApplyTheme:" + configEnable);
        return (configEnable ) && !userLimitByInstallTimes();
    }

    public static boolean showAdOnDetailView() {
        boolean configEnable = HSConfig.optBoolean(false,"Application", "FullScreen", "DetailView")
                || enabledThisVersion();
        HSLog.d("ConfigSettings", "AdOnDetailView:" + configEnable );
        return (configEnable) && !userLimitByInstallTimes();
    }

    private static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "FullScreen", "EnableVersionList");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    private static boolean userLimitByInstallTimes() {
        int disableDays = HSConfig.optInteger(-1, "Application", "FullScreen", "DisableAfterInstallDays");
        if (disableDays > 0) {
            return System.currentTimeMillis() - ConfigUtils.getAppFirstInstallTime() > disableDays * DateUtils.DAY_IN_MILLIS;
        }
        return false;
    }
}
