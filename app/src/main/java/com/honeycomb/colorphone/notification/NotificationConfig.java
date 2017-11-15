package com.honeycomb.colorphone.notification;

import com.ihs.commons.config.HSConfig;

public class NotificationConfig {


    /**
     * inside app notification access alert config
     */

    public static boolean isInsideAppAccessAlertOpen() {
        return HSConfig.optBoolean(true, "Application", "NotificationAccess", "InsideApp", "Show");
    }

    public static int getInsideAppAccessAlertShowMaxTime() {
        return HSConfig.optInteger(3, "Application", "NotificationAccess",  "InsideApp", "ShowMaxTime");
    }

    public static long getInsideAppAccessAlertInterval() {
         return (long) HSConfig.optFloat(1f, "Application", "NotificationAccess", "InsideApp", "ShowInterval") * 1000 * 60 * 60;

    }


    /**
     * outside app notification access alert config
     */

    public static boolean isOutsideAppAccessAlertOpen() {
        return HSConfig.optBoolean(true, "Application", "NotificationAccess", "OutsideApp", "Show");
    }

    public static int getOutsideAppAccessAlertShowMaxTime() {
        return HSConfig.optInteger(3, "Application", "NotificationAccess", "OutsideApp", "ShowMaxTime");
    }

    public static long getOutsideAppAccessAlertInterval() {
        return (long) HSConfig.optFloat(0f, "Application", "NotificationAccess", "OutsideApp", "ShowInterval") * 1000 * 60 * 60;
    }
}
