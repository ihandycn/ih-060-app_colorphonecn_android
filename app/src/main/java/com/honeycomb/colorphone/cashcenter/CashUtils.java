package com.honeycomb.colorphone.cashcenter;

import android.os.Build;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.List;

public class CashUtils {

    public static boolean hasUserEnterCrashCenter() {
        return Preferences.get("cash_center").getBoolean("user_visit", false);
    }
    public static void userEnterCashCenter() {
        Preferences.get("cash_center").putBoolean("user_visit", true);
    }

    public static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "EarnCash", "EarnCashEnableVersioncode");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    public static boolean masterSwitch() {
        boolean masterSwitch = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_master_switch", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "MasterSwitch")
                || masterSwitch;
    }

    private static boolean checkGlobalSwitch() {
        return masterSwitch()
                && enabledThisVersion()
                // Lottie support
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean needShowMainFloatButton() {
        if (!checkGlobalSwitch()) {
            return false;
        }
        boolean mainviewFloatButtonShowBoolean = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "mainview_float_button_show", false);
        return mainviewFloatButtonShowBoolean
                || HSConfig.optBoolean(false, "Application", "EarnCash", "MainviewFloatButtonShow");
    }


    public static boolean guideShowOnUnlockScreeen() {
        boolean earncashAlertShowWhenUnlockscreenBoolean = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_when_unlockscreen", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "UnlockAlertShow")
                || earncashAlertShowWhenUnlockscreenBoolean;
    }

    public static boolean guideShowOnCallAlertClose() {
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_maxtime_when_callassistant_closed", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "CloseCallAssistantAlertShow")
                || enable;
    }

    public static boolean showEntranceAtCallAlert() {
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_on_callassistant", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "OnCallAssistantEntranceShow")
                || enable;
    }

    public static boolean guideShowOnBacktoMain() {
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_when_back_to_mianview_from_detail", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "InsideAppAlertShow")
                || enable;
    }

    public static int maxTimeOnUnlockScreen() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                "earncash_alert_show_maxtime_when_unlockscreen", 1);
        return (int) enable;
    }

    public static int maxTimeOnCallAlertClose() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                        "earncash_alert_show_maxtime_when_callassistant_closed", 1);
        return (int) enable;
    }

    public static int maxTimeOnBacktoMain() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                "earncash_alert_show_maxtime_when_back_to_mianview_from_detail", 1);
        return (int) enable;
    }

    public static class Event {
        public static final String TOPIC_ID = "topic-1539675249991-758";

        private static void logEvent(String name) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, name);
            LauncherAnalytics.logEvent(name);
        }

        public static void onDesktopShortcutClick() {
            logEvent("colorphone_earncash_desktop_icon_click");
        }

        public static void onAdShouldShow() {
            logEvent("colorphone_earncash_ad_should_show");
        }

        public static void onAdShow() {
            logEvent("colorphone_earncash_ad_show");
        }

        public static void onMainviewFloatButtonShow() {
            logEvent("colorphone_mainview_float_button_show");
        }

        public static void onMainviewFloatButtonClick() {
            logEvent("colorphone_mainview_float_button_click");
        }
    }

}
