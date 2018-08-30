package com.honeycomb.colorphone.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AutoPilotUtils {
    private static final String NOTIFICATION_TOOLBAR_TOPIC = "topic-1535427355191-617";

    public static boolean getNotificationToolbarEnable() {
        return AutopilotConfig.getBooleanToTestNow(NOTIFICATION_TOOLBAR_TOPIC, "notificationtoolbar_enable", false);
    }

    public static void logNotificationToolbarBoostClick() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_boost_clicked");
        logNotificationToolbarClicked();
    }

    public static void logNotificationToolbarSettingClick() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_settings_clicked");
    }

    public static void logNotificationToolbarFlashlightClick() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_flashlight_clicked");
    }

    public static void logNotificationToolbarBatteryClick() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_battery_clicked");
        logNotificationToolbarClicked();
    }

    public static void logNotificationToolbarCpuClick() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_cpu_clicked");
        logNotificationToolbarClicked();
    }

    public static void logBoostwireAdShowFromToolbar() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_boostwire_ad_shown_fromtoolbar");
    }

    public static void logBoostdoneAdShowFromToolbar() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_boostdone_ad_shown_fromtoolbar");
    }

    public static void logCpuwireAdShow() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_cpuwire_ad_shown");
    }

    public static void logCpudoneAdShow() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_cpudone_ad_shown");
    }

    public static void logBatterywireAdShow() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_batterywire_ad_shown");
    }

    public static void logBatterydoneAdShow() {
        getNotificationToolbarEnable();
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_batterydone_ad_shown");
    }

    public static void logNotificationToolbarClicked() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_TOOLBAR_TOPIC, "colorphone_notification_toolbar_clicked");
    }
}
