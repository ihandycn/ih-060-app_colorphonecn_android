package com.colorphone.smartlocker.utils;

public class AutoPilotUtils {
    private static final String TOPIC_ID_LOCKER = "topic-7fysxrksq";

    public static boolean isH5LockerMode() {
        //return AutopilotConfig.getBooleanToTestNow(TOPIC_ID_LOCKER, "switch", false);
        return true;
    }
}
