package com.honeycomb.colorphone.autopermission;

import android.os.Build;

import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.permission.HSPermissionType;
import com.superapps.util.Compats;

/**
 * @author sundxing
 */
public class AutoLogger {
    public static void logAutomaticPermissionFailed(HSPermissionType type, String reason) {
        Analytics.logEvent("Automatic_Failed_" + getBrand() + "_" + formatPermissionName(type),
                "Reason", reason == null ? "Null" : reason,
                "Os", getOSVersion());
    }

    public static void logEventWithBrandAndOS(String EventID) {
        Analytics.logEvent(EventID,
                "Brand", getBrand(), "Os", getOSVersion());
    }

    public static String formatPermissionName(HSPermissionType type) {
        switch (type) {
            case TYPE_AUTO_START:
                return "AutoStart";
            case TYPE_NOTIFICATION_LISTENING:
                return "NA";
            case TYPE_SHOW_ON_LOCK:
                return "Lock";
            case TYPE_DRAW_OVERLAY:
                return "Float";
            default:
                return "Unknown";
        }
    }

    public static String getOSVersion(){
        return Utils.getDeviceInfo();
    }

    public static String getBrand() {
        String brand = "";
        if (Compats.IS_HUAWEI_DEVICE) {
            brand = "Huawei";
        } else if (Compats.IS_XIAOMI_DEVICE) {
            brand = "Xiaomi";
        } else {
            brand = Build.BRAND;
        }
        return brand.toLowerCase();
    }

    public static String getPermissionString(boolean isHuawei) {
        StringBuilder stringBuilder = new StringBuilder();
        if (AutoPermissionChecker.hasFloatWindowPermission()) {
            stringBuilder.append(formatPermissionName(HSPermissionType.TYPE_DRAW_OVERLAY)).append("_");
        }

        if (AutoPermissionChecker.hasAutoStartPermission()) {
            stringBuilder.append(formatPermissionName(HSPermissionType.TYPE_AUTO_START)).append("_");
        }

        if (!isHuawei && AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            stringBuilder.append(formatPermissionName(HSPermissionType.TYPE_SHOW_ON_LOCK)).append("_");
        }

        if (AutoPermissionChecker.isNotificationListeningGranted()) {
            stringBuilder.append(formatPermissionName(HSPermissionType.TYPE_NOTIFICATION_LISTENING)).append("_");
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        } else {
            stringBuilder.append("null");
        }

        return stringBuilder.toString();
    }

}
