package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.ihs.commons.config.HSConfig;

public class ShareAlertAutoPilotUtils {

    public static boolean isInsideAppEnable() {
        return HSConfig.optBoolean(false, "Application", "Share", "Enable");
    }

    public static long getInsideAppShareAlertShowInterval() {
        return HSConfig.optInteger(4, "Application", "Share", "TimeInterval") * DateUtils.HOUR_IN_MILLIS;
    }

    public static int getInsideAppShareAlertShowMaxTime() {
        return HSConfig.optInteger(2, "Application", "Share", "MaxTime");
    }

    public static String getInsideAppShareText() {
        return "My latest call screen theme from Color Phone. \nhttps://app.appsflyer.com/com.colorphone.smooth.dialer?pid=Userinvite1\n";
    }

    public static boolean isOutsideAppEnable() {
        return false;
    }


    public static long getOutsideAppShareAlertShowInterval() {
        return HSConfig.optInteger(4, "Application", "Share", "TimeInterval") * DateUtils.HOUR_IN_MILLIS;
    }

    public static int getOutsideAppShareAlerShowMaxTime() {
        return HSConfig.optInteger(2, "Application", "Share", "MaxTime");

    }

    public static String getOutsideAppShareText() {
        return "My latest call screen theme from Color Phone. \n https://app.appsflyer.com/com.colorphone.smooth.dialer?pid=Userinvite2";
    }
}
