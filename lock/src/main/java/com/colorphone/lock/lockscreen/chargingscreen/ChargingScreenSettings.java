package com.colorphone.lock.lockscreen.chargingscreen;


import com.colorphone.lock.util.PreferenceHelper;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.libcharging.ChargingPreferenceUtil;

/**
 * Charging Screen upgrade to SmartCharging, Use SmartChargingSettings instead;
 */

public class ChargingScreenSettings {

    // Multi-process
    public static final String PREF_KEY_CHARGING_SCREEN_ENABLED = "pref_key_charging_screen_enabled";

    // Main-process
    public static final String PREF_KEY_CHARGING_COUNT = "pref_key_charging_count";
    public static final String PREF_KEY_CHARGING_SCREEN_EVER_ENABLED = "pref_key_charging_screen_ever_enabled";
    public static final String PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME = "pref_key_charging_screen_guide_last_show_time";
    public static final String PREF_KEY_CHARGING_SCREEN_SHOW_COUNT = "pref_key_charging_screen_show_count";
    public static final String PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN = "pref_key_charging_screen_battery_menu_tip_shown";
    public static final String PREF_KEY_CHARGING_SCREEN_SETTINGS_TIP_SHOWN = "pref_key_charging_screen_settings_tip_shown";
    public static final String PREF_KEY_CHARGING_SCREEN_DIALOG_GUIDE_SHOWN = "pref_key_charging_screen_dialog_guide_shown";
    public static final String LOCKER_PREFS = "locker.prefs";

    public static final String NOTIFY_CHARGING_SCREEN_STATE = "notify_charging_screen_state";

    private static boolean sDefaultEnabled = HSConfig.optBoolean(false, "Application", "Locker", "ChargeDefaultEnabled");

    public static boolean isChargingScreenUserEnabled() {
        return PreferenceHelper.getDefault().getBoolean(PREF_KEY_CHARGING_SCREEN_ENABLED, sDefaultEnabled);
    }

    public static void setChargingScreenEnabled(boolean isEnabled) {
        PreferenceHelper.getDefault().putBoolean(PREF_KEY_CHARGING_SCREEN_ENABLED, isEnabled);
        if (isEnabled) {
            PreferenceHelper.getDefault().putBoolean(PREF_KEY_CHARGING_SCREEN_EVER_ENABLED, true);
        }
        ChargingPreferenceUtil.setChargingModulePreferenceEnabled(isEnabled);
        HSGlobalNotificationCenter.sendNotification(NOTIFY_CHARGING_SCREEN_STATE);
    }

    public static void increaseChargingCount() {
        PreferenceHelper.get(LOCKER_PREFS
        ).incrementAndGetInt(PREF_KEY_CHARGING_COUNT);
    }

    public static int getChargingCount() {
        return PreferenceHelper.get(LOCKER_PREFS
        ).getInt(PREF_KEY_CHARGING_COUNT, 0);
    }

    public static boolean isChargingScreenEverEnabled() {
        return sDefaultEnabled || PreferenceHelper.getDefault().getBoolean(PREF_KEY_CHARGING_SCREEN_EVER_ENABLED, false);
    }

    public static void increaseChargingScreenGuideShowCount() {
        PreferenceHelper.get(LOCKER_PREFS
        ).incrementAndGetInt(PREF_KEY_CHARGING_SCREEN_SHOW_COUNT);
    }

    public static int getChargingScreenGuideShowCount() {
        return PreferenceHelper.get(LOCKER_PREFS
        ).getInt(PREF_KEY_CHARGING_SCREEN_SHOW_COUNT, 0);
    }

    public static boolean isBatteryTipShown() {
        return PreferenceHelper.getDefault().getBoolean(PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN, false);
    }

    public static void setBatteryTipShown() {
        PreferenceHelper.getDefault().putBoolean(PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN, true);
    }

    public static boolean isLauncherSettingsTipShown() {
        return PreferenceHelper.get(LOCKER_PREFS
        ).getBoolean(PREF_KEY_CHARGING_SCREEN_SETTINGS_TIP_SHOWN, false);
    }

    public static void setLauncherSettingsTipShown() {
        PreferenceHelper.get(LOCKER_PREFS
        ).putBoolean(PREF_KEY_CHARGING_SCREEN_SETTINGS_TIP_SHOWN, true);
    }

    public static boolean isChargingScreenDialogGuideShown() {
        return PreferenceHelper.get(LOCKER_PREFS
        ).getBoolean(PREF_KEY_CHARGING_SCREEN_DIALOG_GUIDE_SHOWN, false);
    }

    public static void setChargingScreenDialogGuideShown() {
        PreferenceHelper.get(LOCKER_PREFS
        ).putBoolean(PREF_KEY_CHARGING_SCREEN_DIALOG_GUIDE_SHOWN, true);
    }
}
