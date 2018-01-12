package com.honeycomb.colorphone.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.acb.utils.PermissionUtils;
import com.colorphone.lock.util.CommonUtils;
import com.colorphone.lock.util.NavUtils;
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

    public static ContentObserver startObservingNotificationPermission(final Runnable grantPermissionRunnable) {

//        final WeakReference<Runnable> grantPermissionRunnableRefer = new WeakReference<Runnable>(grantPermissionRunnable);
        ContentObserver observer = new ContentObserver(new Handler()) {
            @SuppressLint("InflateParams")
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (PermissionUtils.isNotificationAccessGranted(HSApplication.getContext())) {
                    if (grantPermissionRunnable != null) {
                        grantPermissionRunnable.run();
                        LauncherAnalytics.logEvent("Authority_NotificationAccess_Granted");
                    }
                }
            }
        };

        if (CommonUtils.ATLEAST_JB_MR2) {
            HSApplication.getContext().getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(ENABLED_NOTIFICATION_LISTENERS), false, observer);

        }
        return observer;
    }

    public static void requestNotificationPermission(Class actClass, Activity activity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        PermissionUtils.requestNotificationPermission(activity, recordGrantedFlurry, handler, fromType);
        final Class activityClass = actClass;
        ContentObserver observer = startObservingNotificationPermission(new Runnable() {
            private boolean mPermissionAcquired;

            @Override
            public void run() {
                if (!mPermissionAcquired) {
                    mPermissionAcquired = true;
                    HSGlobalNotificationCenter.sendNotification(NOTIFY_NOTIFICATION_PERMISSION_GRANTED);
                    NavUtils.bringActivityToFront(activityClass, 0);
                }
            }
        });
        observers.add(observer);
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


}
