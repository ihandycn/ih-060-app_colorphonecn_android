package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.honeycomb.colorphone.activity.PromoteLockerActivity;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class PromoteLockerAutoPilotUtils {

    public static final String TOPIC_ID = "topic-1512825907890-18";

    public static boolean isPromoteAlertEnable(int alertType) {
        if (alertType == PromoteLockerActivity.AFTER_APPLY_FINISH) {
            return isApplyFinishEnable();
        } else {
            return isAppLaunchAlertEnable();
        }
    }

    public static boolean isApplyFinishEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "applyfinish_promote_enable", false);
    }
    public static boolean isAppLaunchAlertEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "startapp_promote_enable", false);
    }

    public static String getPromoteAlertBtnText() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "promote_alert_btn", "error");
    }

    public static String getPromoteAlertDetailText() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "promote_alert_detail_text", "error");
    }

    public static int getPromoteAlertMaxShowCount() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "promote_alert_max_showtime", 0);
    }

    public static long getPromoteAlertShowInterval() {
        return (long) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "promote_alert_show_interval", 0) * DateUtils.DAY_IN_MILLIS;
    }

    public static String getPromoteAlertTitle() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "promote_alert_title", "error");
    }

    public static String getPromoteLockerApp() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "promote_app", "error");
    }

    public static void logPromoteAlertViewed() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "promote_alert_viewed");
    }

    public static void logPromoteAlertBtnClicked() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "promote_alert_btn_clicked");
    }

    public static void logPromoteLockerDownloaded() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "promote_app_downloaded");
    }
}
