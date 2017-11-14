package com.honeycomb.colorphone.notification;

import com.ihs.commons.config.HSConfig;

/**
 * Created by ihandysoft on 2017/11/13.
 */

public class NotificationConfig {


    /**
     * inside app notification access alert config
     */

    public static boolean isInsideAppAccessAlertOpen() {
        return HSConfig.optBoolean(false, "Application", "InsideApp", "Show");
    }

    public static int getInsideAppAccessAlertShowMaxTime() {
        return HSConfig.optInteger(3, "Application", "InsideApp", "ShowMaxTime");
    }

    public static long getInsideAppAccessAlertInterval() {
         return (long) HSConfig.optFloat(1f, "Application", "InsideApp", "ShowInterval") * 1000 * 60 * 60;

    }


    /**
     * outside app notification access alert config
     */

    public static boolean isOutsideAppAccessAlertOpen() {
        return HSConfig.optBoolean(false, "Application", "OutsideApp", "Show");
    }

    public static int getOutsideAppAccessAlertShowMaxTime() {
        return HSConfig.optInteger(3, "Application", "OutsideApp", "ShowMaxTime");
    }

    public static long getOutsideAppAccessAlertInterval() {
        return (long) HSConfig.optFloat(0f, "Application", "OutsideApp", "ShowInterval") * 1000 * 60 * 60;
    }
}
