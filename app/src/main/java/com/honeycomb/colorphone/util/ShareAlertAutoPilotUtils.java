package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;

public class ShareAlertAutoPilotUtils {

    public static final String SHARE_ALERT_TOPIC_ID = "topic-1511875453515";

    public static String getInsideAppShareBtnText() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_btn_text", "error");
    }

    public static String getInsideAppShareDetail() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_detail", "error");
    }

    public static String getInsideAppShareAlertTitle() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_title", "error");
    }

    public static long getInsideAppShareAlertShowInterval() {
        return (long) AutopilotConfig.getDoubleToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_show_interval", 0) * DateUtils.DAY_IN_MILLIS;
    }

    public static int getInsideAppShareAlertShowMaxTime() {
        return (int) AutopilotConfig.getDoubleToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_show_max_time", 0);
    }

    public static String getInsideAppShareText() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "inapp_share_text", "error").replace("\\n", "\n");
    }

    public static String getOutsideAppShareBtnText() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_btn_text", "error");
    }

    public static String getOutsideAppShareDetail() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_detail", "error");
    }

    public static String getOutsideAppShareAlertTitle() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_title", "error");
    }

    public static long getOutsideAppShareAlertShowInterval() {
        return (long) AutopilotConfig.getDoubleToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_show_interval", 0) * DateUtils.DAY_IN_MILLIS;
    }

    public static int getOutsideAppShareAlerShowMaxTime() {
        return (int) AutopilotConfig.getDoubleToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_show_max_time", 0);
    }

    public static void logInsideAppShareAlertShow() {
        AutopilotEvent.logTopicEvent(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_show");
    }

    public static void logInsideAppShareAlertClicked() {
        AutopilotEvent.logTopicEvent(SHARE_ALERT_TOPIC_ID, "inapp_share_alert_clicked");
    }

    public static void logOutsideAppShareAlertShow() {
        AutopilotEvent.logTopicEvent(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_show");
    }

    public static void logOutsideAppShareAlertClicked() {
        AutopilotEvent.logTopicEvent(SHARE_ALERT_TOPIC_ID, "outapp_share_alert_clicked");
    }

    public static String getOutsideAppShareText() {
        return AutopilotConfig.getStringToTestNow(SHARE_ALERT_TOPIC_ID, "outapp_share_text", "error").replace("\\n", "\n");
    }
}
