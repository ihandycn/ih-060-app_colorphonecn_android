package com.honeycomb.colorphone.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.utils.PermissionHelper;
import com.call.assistant.customize.CallAssistantSettings;
import com.call.assistant.util.CommonUtils;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.autopermission.RomUtils;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Calendars;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;

public class DailyLogger {
    public static final String PREF_KEY_DAILY_EVENTS_LOGGED_TIME = "default_launcher_logged_epoch";
    public static final String PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ = "default_launcher_log_session_seq";

    /**
     * We log daily events on start of the 2nd session every day.
     */
    private static final long DAILY_EVENTS_LOG_SESSION_SEQ = 1;

    public void checkAndLog() {
        Preferences prefs = Preferences.get(Constants.DESKTOP_PREFS);
        long lastLoggedTime = prefs.getLong(PREF_KEY_DAILY_EVENTS_LOGGED_TIME, 0);
        long now = System.currentTimeMillis();
        int dayDifference = Calendars.getDayDifference(now, lastLoggedTime);
        if (dayDifference > 0) {
            int sessionSeq = prefs.incrementAndGetInt(PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ);
            if (sessionSeq == DAILY_EVENTS_LOG_SESSION_SEQ) {
                prefs.putInt(PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ, 0);
                prefs.putLong(PREF_KEY_DAILY_EVENTS_LOGGED_TIME, now);
                long installTime = Utils.getAppInstallTimeMillis();
                int daysSinceInstall = Calendars.getDayDifference(now, installTime);
                performDailyWork();
                logDailyStatus(daysSinceInstall);
            }
        }
    }

    private void logDailyStatus(int daysSinceInstall) {
        String eventID = null;
        if (Compats.IS_XIAOMI_DEVICE) {
            eventID = "Rom_Active_Xiaomi";
        } else if (Compats.IS_HUAWEI_DEVICE) {
            eventID = "Rom_Active_Huawei";
        } else if (Compats.IS_OPPO_DEVICE) {
            eventID = "Rom_Active_Oppo";
        }

        if (!TextUtils.isEmpty(eventID)) {
            Analytics.logEvent(eventID,
                    "Model", Build.MODEL, "bluetooth_name", Settings.Secure.getString(HSApplication.getContext().getContentResolver(), "bluetooth_name"),
                    "Version", RomUtils.getRomVersion(),
                    "SDK", String.valueOf(Build.VERSION.SDK_INT),
                    "InDays", String.valueOf(daysSinceInstall));
        }


        boolean isCallAssistantEnable = ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_CALL_ASSISTANT)
                && CallAssistantSettings.isCallAssistantModuleEnabled();

        if (isCallAssistantEnable) {
            Analytics.logEvent("ColorPhone_Daily_CallAssistant_Open");
        }

        if (ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled()) {
            Analytics.logEvent("ColorPhone_Daily_ScreenFlash_Open");
        }

        Context context = HSApplication.getContext();
        boolean phoneAccessGranted = PermissionChecker.hasPhonePermission();

        boolean contactsAccessGranted = PermissionChecker.hasCotactPermission();

        boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);


        logPermissionStatusEvent("ColorPhone_Permission_Check",
                phoneAccessGranted, contactsAccessGranted,
                notificationAccessGranted);

        Analytics.logEvent("ColorPhone_VersionCode_Check", "versioncode", String.valueOf(HSApplication.getFirstLaunchInfo().appVersionCode));
        Analytics.logEvent("Agency_Info_Dau", "userlevel", HSConfig.optString("not_configured", "UserLevel"));
        Analytics.logEvent("ColorPhone_ScreenFlash_Enabled", "type", String.valueOf(ScreenFlashSettings.isScreenFlashModuleEnabled()));

    }

    public void logOnceFirstSessionEndStatus() {
        // Theme id valid.

        Context context = HSApplication.getContext();
        boolean phoneAccessGranted = PermissionChecker.hasPhonePermission();
        boolean contactsAccessGranted = PermissionChecker.hasCotactPermission();
        boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);

        if (CommonUtils.ATLEAST_MARSHMALLOW && Utils.isNewUser()) {
            String permission = logPermissionStatusEvent("Permission_Check_Above23_FirstSessionEnd",
                    phoneAccessGranted, contactsAccessGranted,
                    notificationAccessGranted);
            Analytics.logEvent("Permission_Check_above23_" + permission);
        }


        if (Build.VERSION.SDK_INT >= 28) {
            StringBuilder sb = new StringBuilder();
            if (Compats.IS_XIAOMI_DEVICE) {
                sb.append("Xiaomi");
            } else if (Compats.IS_HUAWEI_DEVICE) {
                sb.append("Huawei");
            }
            if (sb.length() > 0) {
                sb.append(Permissions.hasPermission(Manifest.permission.READ_CALL_LOG) ? "Yes" : "No");
                Analytics.logEvent("Permission_Check_CallLog", "Device", sb.toString());
            }
        }
    }

    private String logPermissionStatusEvent(String eventName,
                                          boolean phoneAccessGranted,
                                          boolean contactsAccessGranted,
                                          boolean notificationAccessGranted) {
        StringBuilder permission = new StringBuilder();

        if (phoneAccessGranted) {
            permission.append("Phone");
        }

        if (contactsAccessGranted) {
            permission.append("Contact");
        }

        if (notificationAccessGranted) {
            permission.append("NA");
        }

        if (TextUtils.isEmpty(permission.toString())) {
            permission.append("None");
        }

        Analytics.logEvent(eventName,
                "type", permission.toString()
        );

        return permission.toString();
    }
    private boolean isTargetingOreo() {
        int targetSdkVersion = 0;
        PackageManager pm = HSApplication.getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(HSApplication.getContext().getPackageName(), 0);
            if (applicationInfo != null) {
                targetSdkVersion = applicationInfo.targetSdkVersion;
            }
        } catch (Exception e) {
            return false;
        }
        return targetSdkVersion == Build.VERSION_CODES.O;
    }


    private void performDailyWork() {

    }


}
