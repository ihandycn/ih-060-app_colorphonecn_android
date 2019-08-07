package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.util.PermissionsTarget22;
import com.ihs.app.framework.HSApplication;
import com.ihs.permission.HSRuntimePermissions;
import com.ihs.permission.Utils;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.rom.RomUtils;

/**
 * @author sundxing
 */
public class AutoPermissionChecker {

    public static boolean hasFloatWindowPermission() {
        boolean systemResult = Permissions.isFloatWindowAllowed(HSApplication.getContext());
        if (!Compats.IS_XIAOMI_DEVICE || !Compats.IS_HUAWEI_DEVICE) {
            return systemResult;
        }
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean("prefs_float_permission",
                systemResult);
    }

    public static void onFloatPermissionChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean("prefs_float_permission", hasPermission);
    }

    public static void onAutoStartChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean("prefs_auto_start_permission", hasPermission);
    }

    public static boolean hasAutoStartPermission() {
        boolean ret = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && RomUtils.checkIsMiuiRom()) {
            ret = PermissionsTarget22.getInstance().checkPerm(PermissionsTarget22.AUTO_START) == PermissionsTarget22.GRANTED;
        }
        return ret || Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean("prefs_auto_start_permission", false);
    }
    public static void onBgPopupChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean("prefs_bg_popup_permission", hasPermission);
    }

    public static boolean hasBgPopupPermission() {
        if (Compats.IS_XIAOMI_DEVICE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return PermissionsTarget22.getInstance().checkPerm(PermissionsTarget22.BACKGROUND_START_ACTIVITY) == PermissionsTarget22.GRANTED;
            }
            return Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean("prefs_bg_popup_permission", false);
        } else {
            // TODO
            return true;
        }
    }

    public static boolean hasIgnoreBatteryPermission() {
        if (Compats.IS_HUAWEI_DEVICE) {
            PowerManager powerManager = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                return powerManager.isIgnoringBatteryOptimizations(HSApplication.getContext().getPackageName());
            }
            return true;
        } else {
            return true;
        }
    }

    public static void onShowOnLockScreenChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean("prefs_show_on_lockscreen_permission", hasPermission);
    }

    public static boolean hasShowOnLockScreenPermission() {
        if (Compats.IS_XIAOMI_DEVICE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return PermissionsTarget22.getInstance().checkPerm(PermissionsTarget22.SHOW_WHEN_LOCKED) == PermissionsTarget22.GRANTED;
            }
            return Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean("prefs_show_on_lockscreen_permission",
                    false);
        } else {
            // TODO
            return true;
        }
    }

    public static boolean isAccessibilityGranted() {
        return Utils.isAccessibilityGranted();
    }

    public static boolean isNotificationListeningGranted() {
        return Utils.isNotificationListeningGranted();
    }

    public static int getAutoRequestCount() {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getInt("prefs_auto_request_count", 0);
    }

    public static void incrementAutoRequestCount() {
        Preferences.get(Constants.PREF_FILE_DEFAULT).incrementAndGetInt("prefs_auto_request_count");
    }

    public static boolean isPhonePermissionGranted() {
        boolean grant = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            grant = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.ANSWER_PHONE_CALLS) == RuntimePermissions.PERMISSION_GRANTED;
        }
        return grant && RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) == RuntimePermissions.PERMISSION_GRANTED
                    && RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.CALL_PHONE) == RuntimePermissions.PERMISSION_GRANTED;
    }

    public static boolean isWriteSettingsPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(HSApplication.getContext());
        } else {
            return true;
        }
    }

    public static boolean isPermissionPermanentlyDenied(String permission) {
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), permission) == RuntimePermissions.PERMISSION_PERMANENTLY_DENIED;
    }

    public static boolean isRuntimePermissionGrant(String permission) {
        String perm = permission;
        if (HSRuntimePermissions.isRuntimePermission(permission)) {
            perm = HSRuntimePermissions.getAndroidPermName(permission);
        }
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), perm) == RuntimePermissions.PERMISSION_GRANTED;
    }

}
