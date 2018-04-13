package com.colorphone.lock.lockscreen.chargingscreen;


import android.app.Application;
import android.text.TextUtils;

import com.colorphone.lock.util.ConfigUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.libcharging.ChargingPreferenceUtil;
import com.superapps.util.Preferences;

public class SmartChargingSettings {

    private static final String PREFS_CHARGING_REPORT_ENABLE = "charging_report_enable_in_color_phone";

    public static boolean isSmartChargingConfigEnabled() {
        return isChargingReportConfigEnabled() || isChargingScreenConfigEnabled();
    }

    public static boolean isSmartChargingUserEnabled() {
        return ChargingScreenSettings.isChargingScreenUserEnabled() || isChargingReportUserEnabled();
    }

    public static void setModuleEnabled(boolean enable) {
        ChargingScreenSettings.setChargingScreenEnabled(enable);
        setChargingReportUserEnabled(enable);
        ChargingPreferenceUtil.setChargingReportSettingEnabled(enable);
    }

    /**
     * charging screen
     */
    public static boolean isChargingScreenEnabled() {
        return isSmartChargingConfigEnabled()
                && isChargingScreenConfigEnabled()
                && ChargingScreenSettings.isChargingScreenUserEnabled();
    }

    public static boolean isChargingScreenConfigEnabled() {
        return ConfigUtils.isShowModulesDueToConfig()
                || isChargingScreenEnabledWithGooglePolicy();
    }

    public static boolean isChargingScreenEnabledWithGooglePolicy() {
        return ConfigUtils.isEnabled("Application", "Charging", "ChargingLockScreen", "Enable")
                && ConfigUtils.isScreenAdEnabledThisVersion();
    }

    /**
     * charging report
     */
    public static boolean isChargingReportEnabled() {
        return isChargingReportUserEnabled()
                && isChargingReportConfigEnabled()
                && !ConfigUtils.isAnyLockerAppInstalled("Application", "Charging", "ChargingReport", "AppConflictList");
    }

    private static boolean isChargingReportUserEnabled() {
        return Preferences.getDefault().getBoolean(PREFS_CHARGING_REPORT_ENABLE, false);
    }

    public static void setChargingReportUserEnabled(boolean enabled) {
        Preferences.getDefault().putBoolean(PREFS_CHARGING_REPORT_ENABLE, enabled);
    }

    private static boolean isChargingReportConfigEnabled() {

        return ConfigUtils.isShowModulesDueToConfig()
                || isChargingReportEnabledWithGooglePolicy();
    }

    public static boolean isChargingReportEnabledWithGooglePolicy() {
        return ConfigUtils.isEnabled("Application", "Charging", "ChargingReport", "Enable");
    }

}
