package com.honeycomb.colorphone.notification.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.acb.colorphone.permissions.NotificationGuideActivity;
import com.acb.utils.Utils;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 2018/1/5.
 */

// TODO use lib class
public class PermissionHelper {

    public static final String NOTIFY_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_grant";
    public static final String NOTIFY_OVERLAY_PERMISSION_GRANTED = "overlay_permission_grant";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static List<ContentObserver> observers = new ArrayList<>();
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static boolean requestNotificationAccessIfNeeded(@NonNull EventSource eventSource, @Nullable Activity sourceActivity) {
        boolean needGuideNotificationPermisson = true;
        if (eventSource == EventSource.FirstScreen) {
            needGuideNotificationPermisson = HSConfig.optBoolean(false,
                    "Application", "NotificationAccess", "GoToAccessPageFromFirstScreen");
        }
        if (needGuideNotificationPermisson && !PermissionUtils.isNotificationAccessGranted(HSApplication.getContext())) {
            PermissionUtils.requestNotificationPermission(sourceActivity, true, new Handler(), "FirstScreen");
            PermissionHelper.startObservingNotificationPermissionOneTime(ColorPhoneActivity.class, eventSource.getName());
            LauncherAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", eventSource.getName());
            Threads.postOnMainThreadDelayed(() -> {
                Navigations.startActivity(HSApplication.getContext(), NotificationGuideActivity.class);
            }, 1000);
            return true;
        }
        return false;
    }


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

    public static void requestNotificationPermission(Class actClass, Activity lifeObserverActivity, boolean recordGrantedFlurry, Handler handler, final String fromType) {
        PermissionUtils.requestNotificationPermission(lifeObserverActivity, recordGrantedFlurry, handler, fromType);
        startObservingNotificationPermissionOneTime(actClass, fromType);
    }

    private static void startObservingNotificationPermissionOneTime(final Class activityClass, final String fromType) {
        ContentObserver observer = startObservingNotificationPermission(new OneTimeRunnable() {
            @Override
            public void oneTimeRun() {
                HSGlobalNotificationCenter.sendNotification(NOTIFY_NOTIFICATION_PERMISSION_GRANTED);
                onNotificationAccessGranted(fromType);
                bringActivityToFront(activityClass, 0);
            }
        });
        observers.add(observer);
    }

    private static void onNotificationAccessGranted(String fromType) {
        HSAnalytics.logEvent("Colorphone_Notification_Access_Enabled", "from", fromType);
        NotificationAutoPilotUtils.logSettingsAccessEnabled();
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

    public abstract static class TagRunnable implements Runnable {
        private Object mTag;

        public Object getTag() {
            return mTag;
        }

        public void setTag(Object tag) {
            mTag = tag;
        }
    }

}

