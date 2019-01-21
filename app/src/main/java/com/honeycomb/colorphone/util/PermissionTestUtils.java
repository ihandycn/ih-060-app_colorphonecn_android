package com.honeycomb.colorphone.util;

import android.text.TextUtils;

import com.honeycomb.colorphone.BuildConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class PermissionTestUtils {
    private static final String TEST_TOPIC_ID = "topic-6z2dn7nbq";

    public static boolean getAlertOutSideApp() {
        return BuildConfig.DEBUG || AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "alert_outsideapp", false);
    }

    public static int getAlertShowMaxTime() {
        return (int) AutopilotConfig.getDoubleToTestNow(TEST_TOPIC_ID, "alert_show_maxtime", 2);
    }

    public static String getTitleCustomizeAlert() {
//        return AutopilotConfig.getStringToTestNow(TEST_TOPIC_ID, "tittle_customizealert", "text1");
        return "text1";
    }

    public static boolean getButtonBack() {
        return AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "button_back", true);
    }

    public static boolean getAlertStyle() {
        return BuildConfig.DEBUG || AutopilotConfig.getBooleanToTestNow(TEST_TOPIC_ID, "alert_style_new", false);
    }

    public static void logPermissionEvent(String event) {
        if (TextUtils.isEmpty(event)) {
            HSLog.w("PermissionTestUtils", "event is empty");
            return;
        }
        getAlertOutSideApp();
        try {
            LauncherAnalytics.logEvent(event);
            AutopilotEvent.logTopicEvent(TEST_TOPIC_ID, event.toLowerCase());
        } catch (Throwable e) {}
    }
}
