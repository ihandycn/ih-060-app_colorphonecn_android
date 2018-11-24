package com.honeycomb.colorphone.notification.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

// TODO remove, use lib
public class PermissionUtils {

    public static void requestNotificationPermission(final Activity lifeObserverActivity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        Navigations.startActivitySafely(HSApplication.getContext(), getNotificationPermissionIntent(true));
    }

    private static Intent getNotificationPermissionIntent(boolean isNewTask) {
        // Don't use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS as this constant is not included before API 22
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }
}

