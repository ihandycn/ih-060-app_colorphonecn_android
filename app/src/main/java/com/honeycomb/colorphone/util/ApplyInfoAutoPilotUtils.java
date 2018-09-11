package com.honeycomb.colorphone.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class ApplyInfoAutoPilotUtils {

    private static final String TOPIC_ID = "topic-1536046257588-648";

    public static boolean showApplyButton() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "apply_button", false);
    }

    public static boolean showThemeInfomation() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "information_display", false);
    }

    public static void logApplyButtonClicked() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "apply_button_clicked");
    }

    public static void logThumbnailClicked() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "thumbnail_clicked");
    }

    public static void logCallFlashSet() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "call flash_set");
    }
}
