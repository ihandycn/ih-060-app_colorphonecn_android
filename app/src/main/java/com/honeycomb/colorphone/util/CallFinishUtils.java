package com.honeycomb.colorphone.util;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class CallFinishUtils {

    private static final String TOPIC_ID = "topic-1536215679114-660";

    public static boolean isCallFinishFullScreenAdEnabled() {
        boolean autopilotEnable = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "call_finish_wire_show", false);
        boolean configEnable = HSConfig.optBoolean(false, "Application", "CallFinishWire", "Enable");
        HSLog.d("CallFinish", "config enable : " + configEnable +", autopilot : " + autopilotEnable);
        return autopilotEnable && configEnable;
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
