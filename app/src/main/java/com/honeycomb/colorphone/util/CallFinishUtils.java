package com.honeycomb.colorphone.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class CallFinishUtils {

    private static final String TOPIC_ID = "topic-1536215679114-660";

    public static boolean isCallFinishFullScreenAdEnabled() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "call_finish_wire_show", false);
    }

    public static void logCallFinishWiredShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_wire_show");
    }

    public static void logCallFinish() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished");
    }

    public static void logCallFinishCallAssistantShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_call_assistant_show");
    }

    public static void logCallFinishWiredShouldShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_wire_should_show");
    }
}
