package com.honeycomb.colorphone.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class PermissionTestUtils {
    private static final String TEST_TOPIC_ID = "";

    public static boolean getAlertOutSideApp() {
        return AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "alert_outsideapp", false);
    }

    public static int getAlertShowMaxTime() {
        return (int) AutopilotConfig.getDoubleToTestNow(TEST_TOPIC_ID, "alert_show_maxtime", 2);
    }

    public static String getTitleCustomizeAlert() {
        return AutopilotConfig.getStringToTestNow(TEST_TOPIC_ID, "tittle_customizealert", "text1");
    }

    public static boolean getButtonBack() {
        return AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "button_back", true);
    }

    public static boolean getAlertStyle() {
        return AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "alert_style", false);
    }

    public static void logPermissionEvent(String event) {
        getAlertOutSideApp();
        try {
            LauncherAnalytics.logEvent(event);
            AutopilotEvent.logTopicEvent(TEST_TOPIC_ID, event);
        } catch (Exception e) {}
    }
}
