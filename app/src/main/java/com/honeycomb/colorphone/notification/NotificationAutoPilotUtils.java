package com.honeycomb.colorphone.notification;

import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;
import com.colorphone.lock.util.ActivityUtils;


public class NotificationAutoPilotUtils {

    private static final String NOTIFICATION_ACCESS_TEST_TOPIC_ID = "topic-1510752104742";
    private static final String LOCAL_NOTIFICATION_TEST = "topic-1510751463187";
    private static final String WHATS_APP_ASSISTANT_TEST = "topic-1512815797473-16";


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


    /**
     * local notification new theme autopilot
     */

    public static String getNewThemeNotificationContent() {
        return AutopilotConfig.getStringToTestNow(LOCAL_NOTIFICATION_TEST, "new_theme_push_detail", "");
    }

    public static boolean isNewThemeNotificationEnabled() {
        return AutopilotConfig.getBooleanToTestNow(LOCAL_NOTIFICATION_TEST, "new_theme_push_enabled", false);
    }

    public static String getNewThemeNotificationTitle() {
        return AutopilotConfig.getStringToTestNow(LOCAL_NOTIFICATION_TEST, "new_theme_push_title", "");
    }

    /**
     * local notification old theme autopilot
     */

    public static boolean isOldThemeNotificationEnabled() {
        return AutopilotConfig.getBooleanToTestNow(LOCAL_NOTIFICATION_TEST, "old_theme_push_enabled", false);
    }

    public static String getOldThemeNotificationContent() {
        return AutopilotConfig.getStringToTestNow(LOCAL_NOTIFICATION_TEST, "old_theme_push_detail", "");
    }

    public static String getOldThemeNotificationTitle() {
        return AutopilotConfig.getStringToTestNow(LOCAL_NOTIFICATION_TEST, "old_theme_push_title", "");
    }

    public static double getOldThemeNotificationShowInterval() {
        return AutopilotConfig.getDoubleToTestNow(LOCAL_NOTIFICATION_TEST, "old_theme_push_min_show_interval", 0);
    }

    public static double getOldThemeNotificationShowIntervalByOpenApp() {
        return AutopilotConfig.getDoubleToTestNow(LOCAL_NOTIFICATION_TEST, "old_theme_push_min_show_interval_by_open_app", 0);
    }

    public static String getPushIconType() {
        return AutopilotConfig.getStringToTestNow(LOCAL_NOTIFICATION_TEST, "push_icon_type", "ThemeIcon");
    }

    public static void logNewThemeNotificationShow() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_newtheme_show");
    }

    public static void logNewThemeNotificationClicked() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_newtheme_clicked");
    }

    public static void logNewThemeNotificationApply() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_newtheme_themeapply");
    }

    public static void logOldThemeNotificationShow() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_oldtheme_show");
    }

    public static void logOldThemeNotificationClicked() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_oldtheme_clicked");
    }

    public static void logOldThemeNotificationApply() {
        AutopilotEvent.logTopicEvent(LOCAL_NOTIFICATION_TEST, "localpush_oldtheme_themeapply");
    }

    /**
     * WhatsApp message
     */

    public static boolean isMessageCenterEnabled() {
        return isWhatsAppEnabled() || isFacebookMessengerEnabled();
    }

    public static boolean isWhatsAppEnabled() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "whatsapp_assistant_enable", false);
    }

    public static boolean isWhatsappShowOnLock() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "whatsapp_assistant_show_on_lock", false);
    }

    public static boolean isWhatsAppShowOnUnlock() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "whatsapp_assistant_show_on_unlock", false);
    }

    public static boolean isFacebookMessengerEnabled() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "fbmessenger_assistant_enable", false);
    }

    public static boolean isFacebookMessengerShowOnLock() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "fbmessenger_assistant_show_on_lock", false);
    }

    public static boolean isFacebookMessengerShowOnUnlock() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "fbmessenger_assistant_show_on_unlock", false);
    }

    public static void logMessageAssistantShow() {
        AutopilotEvent.logTopicEvent(WHATS_APP_ASSISTANT_TEST, "message_assistant_show");
    }

    public static void logMessageAssistantAdShow() {
        AutopilotEvent.logTopicEvent(WHATS_APP_ASSISTANT_TEST, "message_assistant_ad_show");
    }
}
