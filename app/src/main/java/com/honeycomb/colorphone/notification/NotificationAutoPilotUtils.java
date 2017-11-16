package com.honeycomb.colorphone.notification;

import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;

/**
 * Created by ihandysoft on 2017/11/16.
 */

public class NotificationAutoPilotUtils {

    private static final String NOTIFICATION_ACCESS_TEST_TOPIC_ID = "topic-1510752104742";


    public static boolean isNotificationAccessTipAtBottom() {
        return AutopilotConfig.getStringToTestNow(NOTIFICATION_ACCESS_TEST_TOPIC_ID, "guide_type", "bottom").equals("bottom");
    }

    public static boolean isNotificationAccessTipAnimated() {
        return AutopilotConfig.getBooleanToTestNow(NOTIFICATION_ACCESS_TEST_TOPIC_ID, "show_animation", true);
    }

    public static void logSettingsAlertShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TEST_TOPIC_ID, "colorphone_system_settings_notification_alert_show");
    }

    public static void logSettingsAccessEnabled() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TEST_TOPIC_ID, "colorphone_notification_access_enable");
    }
}
