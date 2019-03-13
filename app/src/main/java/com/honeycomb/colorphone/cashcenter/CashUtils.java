package com.honeycomb.colorphone.cashcenter;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.List;

public class CashUtils {

    public static void startWheelActivity(@Nullable Activity activity, Source source) {

    }

    public static boolean hasShortcut() {
        return Preferences.get("cash_center").getBoolean("shortcut", false);
    }

    public static void markCreateShortcut() {
        Preferences.get("cash_center").putBoolean("shortcut", true);
    }

    public static boolean hasUserEnterCrashCenter() {
        return Preferences.get("cash_center").getBoolean("user_visit", false);
    }

    private static void userEnterCashCenter() {
        Preferences.get("cash_center").putBoolean("user_visit", true);
    }

    public static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "EarnCash", "MasterSwitch");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    private static boolean masterSwitch() {
        boolean masterSwitch = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_master_switch", false);
        return  masterSwitch;
    }

    public static boolean checkGlobalSwitch() {
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
                && HSConfig.optBoolean(false, "Application", "EarnCash", "MainviewFloatButtonShow");
    }


    public static boolean guideShowOnUnlockScreeen() {
        if (!checkGlobalSwitch()) {
            return false;
        }
        boolean earncashAlertShowWhenUnlockscreenBoolean = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_when_unlockscreen", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "UnlockAlertShow")
                && earncashAlertShowWhenUnlockscreenBoolean;
    }

    public static boolean guideShowOnCallAlertClose() {
        if (!checkGlobalSwitch()) {
            return false;
        }
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_when_callassistant_close", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "CloseCallAssistantAlertShow")
                && enable;
    }

    public static boolean showEntranceAtCallAlert() {
        if (!checkGlobalSwitch()) {
            return false;
        }
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_on_callassistant", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "OnCallAssistantEntranceShow")
                && enable;
    }

    public static boolean guideShowOnBacktoMain() {
        if (!checkGlobalSwitch()) {
            return false;
        }
        boolean enable = AutopilotConfig.getBooleanToTestNow("topic-1539675249991-758",
                "earncash_alert_show_when_back_to_mainview_from_detail", false);
        return HSConfig.optBoolean(false, "Application", "EarnCash", "InsideAppAlertShow")
                && enable;
    }

    public static int maxTimeOnUnlockScreen() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                "earncash_alert_show_maxtime_when_unlockscreen", 1);
        return (int) enable;
    }

    public static int maxTimeOnCallAlertClose() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                        "earncash_alert_show_maxtime_when_callassistant_close", 1);
        return (int) enable;
    }

    public static int maxTimeOnBacktoMain() {
        double enable = AutopilotConfig.getDoubleToTestNow("topic-1539675249991-758",
                "earncash_alert_show_maxtime_when_back_to_mainview_from_detail", 1);
        return (int) enable;
    }

    public static void logSwitchStatusToServer() {
        if (!Utils.isNewUser()) {
            return;
        }
        boolean open = masterSwitch();
        int sessionId = HSSessionMgr.getCurrentSessionId();
        Analytics.logEvent("NewUser_Cash_Switch", "Switch", String.valueOf(open),
                "Session", String.valueOf(sessionId), "GdprUser", String.valueOf(GdprUtils.isGdprUser()));
    }


    public static class Event {
        public static final String TOPIC_ID = "topic-1539675249991-758";

        public static void logEvent(String name) {
            // For start autopilot test
            CashUtils.masterSwitch();
            AutopilotEvent.logTopicEvent(TOPIC_ID, name);
            Analytics.logEvent(name);
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


        public static void onShortcutGuideShow(int triggerCount) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_shortcut_alert_show");
            Analytics.logEvent("colorphone_earncash_shortcut_alert_show", "ShowTime", String.valueOf(triggerCount));
        }

        public static void onShortcutGuideClick(int triggerCount) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_shortcut_alert_ok_click");
            Analytics.logEvent("colorphone_earncash_shortcut_alert_ok_click", "ShowTime", String.valueOf(triggerCount));
        }

        public static void onGuideViewShow(Source source) {
            String name = "";
            switch (source) {
                case Inner:
                    name = "colorphone_earncash_alert_show_when_back_to_mainview_from_detail";
                    break;
                case UnlockScreen:
                    name = "colorphone_earncash_alert_show_when_unlockscreen";
                    break;
                case CallAlertClose:
                    name = "colorphone_earncash_alert_show_when_callassistant_close";
                    break;
                case CallAlertFloatBar:
                    name = "colorphone_earncash_alert_show_on_callassistant";
                    break;

            }
            if (!TextUtils.isEmpty(name)) {
                logEvent(name);
            }
        }

        public static void onGuideViewClick(Source source) {
            String name = "";
            switch (source) {
                case Inner:
                    name = "colorphone_earncash_alert_click_when_back_to_mainview_from_detail";
                    break;
                case UnlockScreen:
                    name = "colorphone_earncash_alert_click_when_unlockscreen";
                    break;
                case CallAlertClose:
                    name = "colorphone_earncash_alert_click_when_callassistant_close";
                    break;
                case CallAlertFloatBar:
                    name = "colorphone_earncash_alert_click_on_callassistant";
                    break;

            }
            if (!TextUtils.isEmpty(name)) {
                logEvent(name);
            }
        }

        public static void onSpinClick(Source cashWheelSource) {
            Analytics.logEvent("colorphone_earncash_spin",
                    "From", cashWheelSource == null ? "NULL" : cashWheelSource.getDisplayName());
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_earncash_spin");
        }
    }

    public enum Source {
        FloatIcon("FloatButton"), Inner("BackToMainview"), UnlockScreen("Unlockscreen"),
        CallAlertClose("CallAssistantClose"), Shortcut("Desktop"), CallAlertFloatBar("OnCallAssistant"), Toolbar("Toolbar");

        String displayName;

        Source(String name) {
            displayName = name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
