package com.honeycomb.colorphone.notification.permission;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;

import com.acb.utils.Utils;
import com.honeycomb.colorphone.notification.floatwindow.FloatWindowController;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

public class PermissionUtils {
    public static boolean isNotificationAccessGranted(Context context) {
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(flat)) {
            return false;
        }

        for (String name : flat.split(":")) {
            final ComponentName componentName = ComponentName.unflattenFromString(name);

            if (componentName != null
                    && TextUtils.equals(context.getPackageName(), componentName.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    public static void requestNotificationPermission(final Activity activity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        Navigations.startActivitySafely(HSApplication.getContext(), getNotificationPermissionIntent(true));
        if (activity != null) {
            showAccessGuideOnActivityStop(activity);
        } else {
            FloatWindowController.getInstance().createUsageAccessTip(HSApplication.getContext());
        }
    }

    public static void requestNotificationPermissionNoGuide(final Activity activity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        if (Utils.ATLEAST_JB_MR2) {
            Navigations.startActivityForResultSafely(activity, getNotificationPermissionIntent(false), 100);
            if (recordGrantedFlurry && handler != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (PermissionUtils.isNotificationAccessGranted(HSApplication.getContext())) {
                        }

                    }
                }, 30 * 1000);
            }
        }
    }

    private static Intent getNotificationPermissionIntent(boolean isNewTask) {
        // Don't use Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS as this constant is not included before API 22
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }


    public static void showAccessGuideOnActivityStop(Activity activity) {
        final Application application = activity.getApplication();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            boolean showTip = false;
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {}

            @Override
            public void onActivityPaused(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {
                application.unregisterActivityLifecycleCallbacks(this);
                if (!showTip) {
                    showTip = true;
                    FloatWindowController.getInstance().createUsageAccessTip(activity.getApplicationContext());
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });

    }

}

