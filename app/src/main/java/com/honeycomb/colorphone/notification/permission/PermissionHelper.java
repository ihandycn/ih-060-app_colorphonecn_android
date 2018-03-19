package com.honeycomb.colorphone.notification.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.acb.utils.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 2018/1/5.
 */

public class PermissionHelper {

    public static final String NOTIFY_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_grant";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static List<ContentObserver> observers = new ArrayList<>();

    private static ContentObserver startObservingNotificationPermission(final Runnable grantPermissionRunnable) {

        ContentObserver observer = new ContentObserver(new Handler()) {
            @SuppressLint("InflateParams")
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (PermissionUtils.isNotificationAccessGranted(HSApplication.getContext())) {
                    if (grantPermissionRunnable != null) {
                        grantPermissionRunnable.run();
                    }
                }
            }
        };

        if (Utils.ATLEAST_JB_MR2) {
            HSApplication.getContext().getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(ENABLED_NOTIFICATION_LISTENERS), false, observer);

        }
        return observer;
    }

    public static void requestNotificationPermission(Class actClass, Activity activity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        PermissionUtils.requestNotificationPermission(activity, recordGrantedFlurry, handler, fromType);
        final Class activityClass = actClass;
        ContentObserver observer = startObservingNotificationPermission(new OneTimeRunnable() {
            @Override
            public void oneTimeRun() {
                HSGlobalNotificationCenter.sendNotification(NOTIFY_NOTIFICATION_PERMISSION_GRANTED);
                bringActivityToFront(activityClass, 0);
            }
        });
        observers.add(observer);
    }

    public static void startObservingNotificationPermissionOneTime(Runnable runnable) {
        ContentObserver observer = startObservingNotificationPermission(runnable);
        observers.add(observer);
    }

    public static void bringActivityToFront(Class activity, int launchParam) {
        Intent intent = new Intent(HSApplication.getContext(), activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        HSApplication.getContext().startActivity(intent);
    }

    public static void stopObservingPermission() {
        try {
            for (ContentObserver observer : observers) {
                HSApplication.getContext().getContentResolver().unregisterContentObserver(observer);
            }
        } catch (Exception ignore) {}
    }

    public static void requestNotificationPermission(Activity activity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        requestNotificationPermission(activity.getClass(), activity, recordGrantedFlurry, handler, fromType);
    }

    public static void requestNotificationPermission(Activity activity) {
        requestNotificationPermission(activity, false, null, null);
    }

    public abstract static class OneTimeRunnable implements Runnable {
        private boolean mPermissionAcquired;

        @Override
        public final void run() {
            if (!mPermissionAcquired) {
                mPermissionAcquired = true;
                oneTimeRun();
            }
        }

        public abstract void oneTimeRun();
    }
}

