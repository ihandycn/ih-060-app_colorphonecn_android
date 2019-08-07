package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.os.Build;

import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.permission.HSPermissionRequestMgr;
import com.superapps.util.Compats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sundxing
 */
public class AutoLogger {
    public static void logAutomaticPermissionResult(String typeName, boolean isSucceed, String reason) {
        Analytics.logEvent("Automatic_" + (isSucceed ? "Success_" : "Failed_") + getBrand() + "_" + formatPermissionName(typeName),
                "Reason", reason == null ? "None" : reason,
                "Os", getOSVersion(), "Version", RomUtils.getRomVersion());

    }

    public static void logEvent(String EventID, String... values) {
        Analytics.logEvent(EventID, values);
    }

    public static void logEventWithBrandAndOS(String EventID) {
        Analytics.logEvent(EventID,
                "Brand", getBrand(), "Os", getOSVersion());
    }

    public static String formatPermissionName(String type) {
        switch (type) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                return "AutoStart";
            case HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING:
                return "NA";
            case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                return "Lock";
            case HSPermissionRequestMgr.TYPE_DRAW_OVERLAY:
                return "Float";
            case AutoRequestManager.TYPE_CUSTOM_BACKGROUND_POPUP:
                return "BgPop";
            default:
                return type;
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
            stringBuilder.append(formatPermissionName(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY)).append("_");
        }

        if (AutoPermissionChecker.hasAutoStartPermission()) {
            stringBuilder.append(formatPermissionName(HSPermissionRequestMgr.TYPE_AUTO_START)).append("_");
        }

        if (!isHuawei && AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            stringBuilder.append(formatPermissionName(HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK)).append("_");
        }

        if (AutoPermissionChecker.isNotificationListeningGranted()) {
            stringBuilder.append(formatPermissionName(HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING)).append("_");
        }

        if (AutoPermissionChecker.hasBgPopupPermission()) {
            stringBuilder.append(formatPermissionName("TYPE_PERMISSION_TYPE_BG_POP")).append("_");
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        } else {
            stringBuilder.append("null");
        }

        return stringBuilder.toString();
    }

    public static String getGrantRuntimePermissions() {
        return getRuntimePermissionString(getGrantRuntimePermissions(AutoRequestManager.getInstance().getAllRuntimePermission()));
    }

    public static List<String> getGrantRuntimePermissions(List<String> permissions) {
        List<String> grantPermissions = new ArrayList<>();
        if (permissions != null && permissions.size() > 0) {
            for (String p : permissions) {
                if (AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                    grantPermissions.add(p);
                }
            }
        }
        return grantPermissions;
    }

    public static String getRuntimePermissionString(List<String> permissions) {
        StringBuilder sb = new StringBuilder();
        if (permissions != null && permissions.size() > 0) {
            for (String p : permissions) {
                switch (p) {
                    case Manifest.permission.READ_PHONE_STATE:
                        sb.append("ReadPhoneState+");
                        break;
                    case Manifest.permission.CALL_PHONE:
                        sb.append("CallPhone+");
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        sb.append("readContact+");
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        sb.append("writeContact+");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        sb.append("storage+");
                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        sb.append("callLog+");
                        break;
                }
            }
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }

}
