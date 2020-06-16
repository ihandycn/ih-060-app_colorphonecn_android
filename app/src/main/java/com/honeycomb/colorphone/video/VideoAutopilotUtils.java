package com.honeycomb.colorphone.video;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class VideoAutopilotUtils {
    private static final String TOPIC_ID = "topic-7jz903iwk";

    public static boolean getVideoSwitch() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "enable", false);
    }

    public static void logTabVideoShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "tab_video_show");
    }

    public static void logVideoSlide() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "video_slide");
    }
}
