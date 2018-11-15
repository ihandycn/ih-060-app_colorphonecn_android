package com.honeycomb.colorphone.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.acb.call.utils.PermissionHelper;
import com.call.assistant.util.CommonUtils;
import com.honeycomb.colorphone.Constants;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

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
        Context context = HSApplication.getContext();
        boolean phoneAccessGranted = RuntimePermissions.checkSelfPermission(
                context, Manifest.permission.READ_PHONE_STATE) >= 0;
        boolean contactsAccessGranted = RuntimePermissions.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS) >= 0;
//                RuntimePermissions.checkSelfPermission(
//                context, Manifest.permission.WRITE_EXTERNAL_STORAGE) >= 0;

        boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);


        logPermissionStatusEvent("ColorPhone_Permission_Check",
                phoneAccessGranted, contactsAccessGranted,
                notificationAccessGranted);

        if (CommonUtils.ATLEAST_MARSHMALLOW && Utils.isNewUser()) {
            logPermissionStatusEvent("ColorPhone_Permission_Check_Above23",
                    phoneAccessGranted, contactsAccessGranted,
                    notificationAccessGranted);
        }

    }

    public void logOnceFirstSessionEndStatus() {
        Context context = HSApplication.getContext();
        boolean phoneAccessGranted = RuntimePermissions.checkSelfPermission(
                context, Manifest.permission.READ_PHONE_STATE) >= 0;
        boolean contactsAccessGranted = RuntimePermissions.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS) >= 0;

        boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);

        if (CommonUtils.ATLEAST_MARSHMALLOW && Utils.isNewUser()) {
            logPermissionStatusEvent("ColorPhone_Permission_Check_Above23_FirstSessionEnd",
                    phoneAccessGranted, contactsAccessGranted,
                    notificationAccessGranted);
        }

    }

    private void logPermissionStatusEvent(String eventName,
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

        LauncherAnalytics.logEvent(eventName,
                "type", permission.toString()
        );
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
