package com.colorphone.lock.lockscreen.chargingscreen;


import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;
import com.colorphone.lock.util.ConfigUtils;
import com.colorphone.lock.util.PreferenceHelper;
import com.ihs.libcharging.ChargingPreferenceUtil;

public class SmartChargingSettings {

    private static final String TOPIC_ID_SMART_CHARGING = "topic-1512822231846-17";
    private static final String PREFS_CHARGING_REPORT_ENABLE = "charging_report_enable_in_color_phone";

    public static boolean isSmartChargingConfigEnabled() {
        return AutopilotConfig.getBooleanToTestNow("topic-1505290483207", "smart_charging_enable", false)
                && (isChargingReportConfigEnabled() || isChargingScreenConfigEnabled());
    }

    public static boolean isSmartChargingUserEnabled() {
        return ChargingScreenSettings.isChargingScreenUserEnabled() || isChargingReportUserEnabled();
    }

    public static void setModuleEnabled(boolean enable) {
        ChargingScreenSettings.setChargingScreenEnabled(enable);
        setChargingReportUserEnabled(enable);
        ChargingPreferenceUtil.setChargingReportSettingEnabled(SmartChargingSettings.isChargingReportEnabled());
    }

    /**
     * charging screen
     */
    public static boolean isChargingScreenEnabled() {
        return isSmartChargingConfigEnabled()
                && isChargingScreenConfigEnabled()
                && ChargingScreenSettings.isChargingScreenUserEnabled();
    }

    private static boolean isChargingScreenConfigEnabled() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_SMART_CHARGING, "charging_lockscreen_enable", false)
                && isChargingScreenEnabledWithGooglePolicy();
    }

    public static boolean isChargingScreenEnabledWithGooglePolicy() {
        return ConfigUtils.isEnabled("Application", "Charging", "ChargingLockScreen", "Enable");
    }

    /**
     * charging report
     */
    public static boolean isChargingReportEnabled() {
        return isSmartChargingConfigEnabled()
                && isChargingReportUserEnabled()
                && isChargingReportConfigEnabled()
                && !ConfigUtils.isAnyLockerAppInstalled("Application", "Charging", "ChargingReport", "AppConflictList");
    }

    private static boolean isChargingReportUserEnabled() {
        return PreferenceHelper.getDefault().getBoolean(PREFS_CHARGING_REPORT_ENABLE, false);
    }

    public static void setChargingReportUserEnabled(boolean enabled) {
        PreferenceHelper.getDefault().putBoolean(PREFS_CHARGING_REPORT_ENABLE, enabled);
    }

    private static boolean isChargingReportConfigEnabled() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_SMART_CHARGING, "charging_report_enable", false)
                && isChargingReportEnabledWithGooglePolicy();
    }

    public static boolean isChargingReportOffChargerEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_SMART_CHARGING, "chargingreport_offcharger_enable", false);
    }

    public static boolean isChargingReportOnChargerEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_SMART_CHARGING, "chargingreport_oncharger_enable", false);
    }

    public static boolean isChargingReportEnabledWithGooglePolicy() {
        return ConfigUtils.isEnabled("Application", "Charging", "ChargingReport", "Enable");
    }

    public static void logChargingReportEnabled() {
        AutopilotEvent.logTopicEvent(TOPIC_ID_SMART_CHARGING, "chargingreport_view_show");
    }

}
