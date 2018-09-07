package com.honeycomb.colorphone.dialer;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AP {

    public static boolean dialerEnable() {
        boolean dialerEnableBoolean = AutopilotConfig.getBooleanToTestNow("topic-1536215679114-660", "dialer enable", false);
        return dialerEnableBoolean;
    }

    public static boolean setDefaultGuideShow() {
        return false;
    }

    public static void guideShow() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_show");
    }

    public static void guideConfirmed() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_set_clicked");
    }

    public static void successSetAsDefault() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_success");
    }

    public static void dilerShow() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "dailer_page_show");
    }



}
