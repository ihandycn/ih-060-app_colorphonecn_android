package com.honeycomb.colorphone.dialer;

import android.os.Build;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Commons;

import java.util.List;

public class ConfigEvent {

    public static boolean dialerEnable() {
        return enabledThisVersion();
    }

    private static boolean enabledThisVersion() {
        // Check os version
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "Dialer", "EnableVersionList");
        int versionCode = Build.VERSION.SDK_INT;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }

        // Check brand
        List<String> enableBrandList = (List<String>) HSConfig.getList("Application", "Dialer", "EnableAllOSVersionForBrand");
        for (String brand : enableBrandList) {
            if (Build.BRAND.equalsIgnoreCase(brand)){
                return true;
            }
        }

        return false;
    }

    public static boolean setDefaultGuideShow() {
        return false;
    }

    public static void guideShow() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ConfigEvent.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_guide_show"));
    }

    public static void guideConfirmed() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ConfigEvent.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_guide_set_clicked"));
    }

    public static void successSetAsDefault() {
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_success"));
    }

    public static void dialerShow() {
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("dialer_page_show_above9"), "Type",
                Commons.isKeyguardLocked(HSApplication.getContext(), false) ?
                        "Withlock" : "Withoutlock");
    }

}
