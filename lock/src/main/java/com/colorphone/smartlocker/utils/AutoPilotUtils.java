package com.colorphone.smartlocker.utils;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AutoPilotUtils {
    private static final String TOPIC_ID_LOCKER = "topic-7nec8s2as";

    public static boolean isH5LockerMode() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_LOCKER, "h5enable", false);
    }

    public static void logOldAdCpm(double ecpm) {
        AutopilotEvent.logTopicEvent(TOPIC_ID_LOCKER, "cpm_collection_news", ecpm);
    }

    public static void logNewsChance() {
        AutopilotEvent.logTopicEvent(TOPIC_ID_LOCKER, "lockscreen_should_show");
    }

    public static void logNewsShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID_LOCKER, "lockscreen_show");
    }
}
