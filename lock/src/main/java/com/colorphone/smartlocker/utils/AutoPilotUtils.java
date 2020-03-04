package com.colorphone.smartlocker.utils;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AutoPilotUtils {
    private static final String TOPIC_ID_LOCKER = "topic-7fysxrksq";

    public static String getLockerMode() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID_LOCKER, "cable_mode", "normal");
    }

    public static void logLockerModeAutopilotEvent(String eventName) {
        AutopilotEvent.logTopicEvent(TOPIC_ID_LOCKER, eventName);
    }
}
