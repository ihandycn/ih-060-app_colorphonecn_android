package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

public class ShareAlertAutoPilotUtils {

    public static boolean isInsideAppEnable() {
        return false;
    }

    public static long getInsideAppShareAlertShowInterval() {
        return 4 * DateUtils.DAY_IN_MILLIS;
    }

    public static int getInsideAppShareAlertShowMaxTime() {
        return 2;
    }

    public static String getInsideAppShareText() {
        return "My latest call screen theme from Color Phone. \nhttps://app.appsflyer.com/com.colorphone.smooth.dialer?pid=Userinvite1\n";
    }

    public static boolean isOutsideAppEnable() {
        return false;
    }


    public static long getOutsideAppShareAlertShowInterval() {
        return 4 * DateUtils.DAY_IN_MILLIS;
    }

    public static int getOutsideAppShareAlerShowMaxTime() {
        return 2;
    }

    public static void logInsideAppShareAlertShow() {
    }

    public static void logInsideAppShareAlertClicked() {
    }

    public static void logOutsideAppShareAlertShow() {
    }

    public static void logOutsideAppShareAlertClicked() {
    }

    public static String getOutsideAppShareText() {
        return "My latest call screen theme from Color Phone. \n https://app.appsflyer.com/com.colorphone.smooth.dialer?pid=Userinvite2";
    }
}
