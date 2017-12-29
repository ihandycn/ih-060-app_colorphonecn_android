package com.colorphone.lock.lockscreen.locker;


import com.acb.autopilot.AutopilotConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.util.ConfigUtils;
import com.colorphone.lock.util.PreferenceHelper;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSPreferenceHelper;

import net.appcloudbox.ads.nativeads.AcbNativeAdManager;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;


public class LockerSettings {
    //Multi-process
    public static final String PREF_KEY_LOCKER_ENABLED = "pref_key_locker_enabled";

    //Main-process
    public static final String PREF_KEY_LOCKER_SHOW_COUNT = "pref_key_locker_show_count";
    public static final String PREF_KEY_LOCKER_ADS_SHOW_COUNT = "pref_key_locker_ads_show_count";
    public static final String PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN = "pref_key_locker_toggle_guide_shown";

    public static final String NOTIFY_LOCKER_STATE = "notify_locker_state";

    public static final String[] LOCKER_ENABLE_PATH = {"Application", "LockScreen", "Enable"};

    private static boolean sDefaultEnabled = HSConfig.optBoolean(false, "Application", "Locker", "LockerDefaultEnabled");

    public static boolean isLockerEnabled() {
        return isLockerConfigEnabled() && isLockerUserEnabled();
    }

    public static boolean isLockerConfigEnabled() {
        return AutopilotConfig.getBooleanToTestNow("topic-1505290483207", "colorscreensaver_enable", false)
                && ConfigUtils.isEnabled(LOCKER_ENABLE_PATH);
    }

    public static boolean isLockerUserEnabled() {
        return PreferenceHelper.getDefault().getBoolean(PREF_KEY_LOCKER_ENABLED, sDefaultEnabled);
    }

    public static boolean isLockerEverEnabled() {
        return sDefaultEnabled || HSPreferenceHelper.getDefault().contains(PREF_KEY_LOCKER_ENABLED);
    }

    public static void setLockerEnabled(boolean isEnabled) {
        PreferenceHelper.getDefault().putBoolean(PREF_KEY_LOCKER_ENABLED, isEnabled);
        HSGlobalNotificationCenter.sendNotification(NOTIFY_LOCKER_STATE);

        String adPlacement = LockerCustomConfig.get().getLockerAdName();
        if (isEnabled) {
            AcbNativeAdManager.sharedInstance().activePlacementInProcess(adPlacement);
        } else {
            AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(adPlacement);
        }
    }

    public static void increaseLockerShowCount() {
        PreferenceHelper.get(LOCKER_PREFS).incrementAndGetInt(PREF_KEY_LOCKER_SHOW_COUNT);
    }

    public static int getLockerShowCount() {
        return PreferenceHelper.get(LOCKER_PREFS).getInt(PREF_KEY_LOCKER_SHOW_COUNT, 0);
    }

    public static void setLockerAdsShowCount() {
        PreferenceHelper.get(LOCKER_PREFS).putInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, getLockerShowCount());
    }

    public static int getLockerAdsShowCount() {
        return PreferenceHelper.get(LOCKER_PREFS).getInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, 0);
    }

    public static boolean isLockerToggleGuideShown() {
        return PreferenceHelper.get(LOCKER_PREFS).getBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, false);
    }

    public static void setLockerToggleGuideShown() {
        PreferenceHelper.get(LOCKER_PREFS).putBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, true);
    }
}
