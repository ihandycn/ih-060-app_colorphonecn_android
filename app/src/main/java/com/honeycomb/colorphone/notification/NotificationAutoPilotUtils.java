package com.honeycomb.colorphone.notification;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;


public class NotificationAutoPilotUtils {

    private static final String LOCAL_NOTIFICATION_TEST = "topic-1510751463187";
    private static final String WHATS_APP_ASSISTANT_TEST = "topic-1512815797473-16";

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
        return isWhatsAppEnabled() || isFacebookMessengerEnabled() || isGmailEnabled();
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
        AutopilotEvent.logTopicEvent(WHATS_APP_ASSISTANT_TEST, "message_assistant_ad_should_show");
    }

    public static void logMessageAssistantAdShow() {
        AutopilotEvent.logTopicEvent(WHATS_APP_ASSISTANT_TEST, "message_assistant_ad_show");
    }

    public static void logMessageAssistantShowOnLockScreen() {
        AutopilotEvent.logTopicEvent(WHATS_APP_ASSISTANT_TEST, "message_assistant_show_onlockscreen");
    }


    /**
     * Upgrade users get default value, return true
     * @return
     */
    public static boolean isMessageAssistantEnabled() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "message_assistant_enable", false);
    }

    public static boolean isSmsEnabled() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "sms_assistant_enable", false);
    }

    public static boolean isSmsEnabledWhenScreenOn() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "sms_assistant_show_on_unlock", false);

    }

    public static boolean isSmsEnabledWhenScreenOff() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "sms_assistant_show_on_lock", false);

    }

    /**
     * Gmail
     */
    public static boolean isGmailEnabled() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "gmail_assistant_enable", false);

    }

    public static boolean isGmailEnabledWhenScreenOn() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "gmail_assistant_show_on_unlock", false);

    }

    public static boolean isGmailEnabledWhenScreenOff() {
        return AutopilotConfig.getBooleanToTestNow(WHATS_APP_ASSISTANT_TEST, "gmail_assistant_show_on_lock", false);

    }

}
