package com.honeycomb.colorphone.notification;

public class NotificationAutoPilotUtils {

     /**
     * local notification new theme autopilot
     */

    public static String getNewThemeNotificationContent() {
        return "Newly launched '%1$s' theme for Color Phone";
    }

    public static boolean isNewThemeNotificationEnabled() {
        return false;
    }

    public static String getNewThemeNotificationTitle() {
        return " Brand new theme!";
    }

    /**
     * local notification old theme autopilot
     */

    public static boolean isOldThemeNotificationEnabled() {
        return false;
    }

    public static String getOldThemeNotificationContent() {
        return "'%1$s' theme for Color Phone! Try this most popular feature!";
    }

    public static String getOldThemeNotificationTitle() {
        return "Hot theme recommended!";
    }

    public static double getOldThemeNotificationShowInterval() {
        return 3;
    }

    public static double getOldThemeNotificationShowIntervalByOpenApp() {
        return 7;
    }

    public static String getPushIconType() {
        return "ThemeIcon";
    }

    public static void logNewThemeNotificationShow() {
    }

    public static void logNewThemeNotificationClicked() {
    }

    public static void logNewThemeNotificationApply() {
    }

    public static void logOldThemeNotificationShow() {
    }

    public static void logOldThemeNotificationClicked() {
    }

    public static void logOldThemeNotificationApply() {
    }
}
