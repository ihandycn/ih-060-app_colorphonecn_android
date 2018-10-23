package com.honeycomb.colorphone.cashcenter;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class Utils {

    public static boolean hasUserEnterCrashCenter() {
        return Preferences.get("cash_center").getBoolean("user_visit", false);
    }
    public static void userEnterCashCenter() {
        Preferences.get("cash_center").putBoolean("user_visit", true);
    }

    public static boolean mainFloatButtonShow() {
        boolean mainviewFloatButtonShowBoolean = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "mainview_float_button_show", false);
        return mainviewFloatButtonShowBoolean
                || HSConfig.optBoolean(false, "Application", "EarnCash", "MainviewFloatButtonShow");
    }

    public static boolean masterSwitch() {
        boolean masterSwitch = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_master_switch", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "MasterSwitch")
                || masterSwitch;
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

        public static void onDesktopShortcutClick() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_desktop_icon_click");
            LauncherAnalytics.logEvent("colorphone_earncash_desktop_icon_click");

        }

        public static void onAdShouldShow() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_ad_should_show");
            LauncherAnalytics.logEvent("colorphone_earncash_ad_should_show");

        }

        public static void onAdShow() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_ad_show");
            LauncherAnalytics.logEvent("colorphone_earncash_ad_show");

        }
    }

}
