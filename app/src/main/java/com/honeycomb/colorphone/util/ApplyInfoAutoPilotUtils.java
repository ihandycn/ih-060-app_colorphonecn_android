package com.honeycomb.colorphone.util;

import com.honeycomb.colorphone.BuildConfig;

import net.appcloudbox.autopilot.AutopilotEvent;

public class ApplyInfoAutoPilotUtils {

    private static final String TOPIC_ID = "topic-1536046257588-648";

    private static void callGetMethod() {
        if (BuildConfig.DEBUG) {
            showApplyButton();
        }
    }

    public static boolean showApplyButton() {
        return true;
    }

    public static boolean showThemeInformation() {
        return true;
    }

    public static void logApplyButtonClicked() {
        callGetMethod();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "apply_button_clicked");
    }

    public static void logThumbnailClicked() {
        callGetMethod();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "thumbnail_clicked");
    }

    public static void logCallFlashSet() {
        callGetMethod();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "call flash_set");
    }
}
