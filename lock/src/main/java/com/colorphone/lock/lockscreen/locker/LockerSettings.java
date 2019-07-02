package com.colorphone.lock.lockscreen.locker;


import com.colorphone.lock.util.ConfigUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Preferences;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;


public class LockerSettings {
    //Multi-process
    public static final String PREF_KEY_LOCKER_ENABLED = "pref_key_locker_enabled";

    //Main-process
    public static final String PREF_KEY_LOCKER_SHOW_COUNT = "pref_key_locker_show_count";
    public static final String PREF_KEY_LOCKER_ADS_SHOW_COUNT = "pref_key_locker_ads_show_count";
    public static final String PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN = "pref_key_locker_toggle_guide_shown";
    public static final String PREF_KEY_NOTIFICATION_CHARGING = "pref_key_notification_open_charging";
    public static final String PREF_KEY_NOTIFICATION_LOCKER = "pref_key_notification_open_locker";

    public static final String NOTIFY_LOCKER_STATE = "notify_locker_state";

    public static final String[] LOCKER_ENABLE_PATH = {"Application", "LockScreen", "Enable"};

    private static boolean sDefaultEnabled = HSConfig.optBoolean(false, "Application", "Locker", "LockerDefaultEnabled");

    public static boolean isLockerEnabled() {
        return isLockerConfigEnabled()
                && isLockerUserEnabled()
                && !ConfigUtils.isNewUserInAdBlockStatus();
    }

    public static boolean isLockerConfigEnabled() {
        return (ConfigUtils.isEnabled(LOCKER_ENABLE_PATH) && ConfigUtils.isScreenAdEnabledThisVersion());
    }

    public static boolean isLockerUserEnabled() {
        return Preferences.getDefault().getBoolean(PREF_KEY_LOCKER_ENABLED, sDefaultEnabled);
    }

    public static boolean isLockerEverEnabled() {
        return sDefaultEnabled || HSPreferenceHelper.getDefault().contains(PREF_KEY_LOCKER_ENABLED);
    }

    public static void setLockerEnabled(boolean isEnabled) {
        Preferences.getDefault().putBoolean(PREF_KEY_LOCKER_ENABLED, isEnabled);
        HSGlobalNotificationCenter.sendNotification(NOTIFY_LOCKER_STATE);
    }

    public static void increaseLockerShowCount() {
        Preferences.get(LOCKER_PREFS).incrementAndGetInt(PREF_KEY_LOCKER_SHOW_COUNT);
    }

    public static int getLockerShowCount() {
        return Preferences.get(LOCKER_PREFS).getInt(PREF_KEY_LOCKER_SHOW_COUNT, 0);
    }

    public static void setLockerAdsShowCount() {
        Preferences.get(LOCKER_PREFS).putInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, getLockerShowCount());
    }

    public static int getLockerAdsShowCount() {
        return Preferences.get(LOCKER_PREFS).getInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, 0);
    }

    public static boolean isLockerToggleGuideShown() {
        return Preferences.get(LOCKER_PREFS).getBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, false);
    }

    public static void setLockerToggleGuideShown() {
        Preferences.get(LOCKER_PREFS).putBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, true);
    }

    public static boolean needShowNotificationLocker() {
        return Preferences.get(LOCKER_PREFS).getBoolean(PREF_KEY_NOTIFICATION_CHARGING, true);
    }

    public static boolean needShowNotificationCharging() {
        return Preferences.get(LOCKER_PREFS).getBoolean(PREF_KEY_NOTIFICATION_CHARGING, true);
    }
}
