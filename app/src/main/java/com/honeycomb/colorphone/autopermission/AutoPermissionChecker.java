package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import com.honeycomb.colorphone.ColorPhoneApplication;
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
import com.superapps.util.rom.VivoUtils;

/**
 * @author sundxing
 */
public class AutoPermissionChecker {
    private static final String PREFS_POST_NOTIFICATION_PERMISSION = "prefs_post_notification_permission";
    private static final String PREFS_ADD_SHORTCUT_PERMISSION = "prefs_app_shortcut_permission";

    public static boolean skipPhonePermission = false;

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
        } else if (RomUtils.checkIsVivoRom()) {
            return VivoUtils.checkAutoStartPermission(HSApplication.getContext());
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
        } else if (Compats.IS_VIVO_DEVICE) {
            return VivoUtils.checkBackgroundPopupPermission(ColorPhoneApplication.getContext());
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
        }
        return true;
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
        } else if (Compats.IS_VIVO_DEVICE && Build.VERSION.SDK_INT > 24) {
            return VivoUtils.checkLockPermission(ColorPhoneApplication.getContext());
        } else {
            // TODO
            return true;
        }
    }

    public static boolean isAccessibilityGranted() {
        return Utils.isAccessibilityGranted();
    }

    public static boolean isNotificationListeningGranted() {
        return Permissions.isNotificationAccessGranted();
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
            int permission_APC = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.ANSWER_PHONE_CALLS);
            grant = permission_APC == RuntimePermissions.PERMISSION_GRANTED;
            if (HSApplication.getFirstLaunchInfo().appVersionCode != HSApplication.getCurrentLaunchInfo().appVersionCode) {
                grant |= permission_APC == RuntimePermissions.PERMISSION_PERMANENTLY_DENIED;
            }

            if (HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode) {
                grant |= permission_APC == RuntimePermissions.PERMISSION_GRANTED_BUT_NEEDS_REQUEST;
            }
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
        if (RomUtils.checkIsVivoRom()) {
            return checkVivoRuntimePermission(permission);
        }
        if (HSRuntimePermissions.isRuntimePermission(permission)) {
            perm = HSRuntimePermissions.getAndroidPermName(permission);
        }
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), perm) == RuntimePermissions.PERMISSION_GRANTED;
    }

    public static boolean checkVivoRuntimePermission(String permission) {
        Context context = HSApplication.getContext();
        if (permission.equals(HSRuntimePermissions.TYPE_RUNTIME_CONTACT_READ) || permission.equals(Manifest.permission.READ_CONTACTS)) {
            return VivoUtils.checkReadContactPermission(context);
        } else if (permission.equals(HSRuntimePermissions.TYPE_RUNTIME_CONTACT_WRITE) || permission.equals(Manifest.permission.WRITE_CONTACTS)) {
            return VivoUtils.checkWriteContactPermission(context);
        } else if (permission.equals(HSRuntimePermissions.TYPE_RUNTIME_CALL_LOG) || permission.equals(Manifest.permission.READ_CALL_LOG)) {
            return VivoUtils.checkReadCallLog(context);
        } else if (permission.equals(HSRuntimePermissions.TYPE_RUNTIME_STORAGE) || permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return VivoUtils.checkStoragePermission();
        } else {
            return false;
        }
    }

    public static boolean isPostNotificationPermissionGrant() {
        if (Compats.IS_OPPO_DEVICE) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(HSApplication.getContext());
            return manager.areNotificationsEnabled();
        }
        return true;
    }

    public static void onPostNotificationPermissionChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean(PREFS_POST_NOTIFICATION_PERMISSION, hasPermission);
    }

    public static boolean isAddShortcutPermissionGrant() {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean(PREFS_ADD_SHORTCUT_PERMISSION, false);
    }

    public static void onAddShortcutPermissionChange(boolean hasPermission) {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean(PREFS_ADD_SHORTCUT_PERMISSION, hasPermission);
    }
}
