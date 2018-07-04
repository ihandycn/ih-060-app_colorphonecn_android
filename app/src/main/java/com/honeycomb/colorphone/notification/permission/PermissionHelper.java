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

import com.acb.utils.Utils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.notification.floatwindow.FloatWindowController;
import com.honeycomb.colorphone.permission.FloatWindowManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 2018/1/5.
 */

public class PermissionHelper {

    public static final String NOTIFY_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_grant";
    public static final String NOTIFY_OVERLAY_PERMISSION_GRANTED = "overlay_permission_grant";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static List<ContentObserver> observers = new ArrayList<>();
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void requestNotificationAccessIfNeeded(@NonNull EventSource eventSource, @Nullable Activity sourceActivity) {
        boolean needGuideNotificationPermisson = true;
        if (eventSource == EventSource.FirstScreen) {
            needGuideNotificationPermisson = HSConfig.optBoolean(false,
                    "Application", "NotificationAccess", "GoToAccessPageFromFirstScreen");
        }
        if (needGuideNotificationPermisson && !PermissionUtils.isNotificationAccessGranted(HSApplication.getContext())) {
            PermissionUtils.requestNotificationPermission(sourceActivity, true, new Handler(), "FirstScreen");
            PermissionHelper.startObservingNotificationPermissionOneTime(ColorPhoneActivity.class);
            LauncherAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", eventSource.getName());
        }
    }

    public static boolean requestDrawOverlayIfNeeded(EventSource eventSource) {
        boolean hasPermission = FloatWindowManager.getInstance().checkPermission(HSApplication.getContext());
        boolean request = !hasPermission;
        if (eventSource == EventSource.FirstScreen) {
            request = request && HSConfig.optBoolean(true, "Application", "DrawOverlay", "RequestOnFirstScreen");
        }

        if (request) {
            // TODO
            boolean needShowTip  = true;
            if (needShowTip) {
                String hintTxt = HSApplication.getContext().getString(R.string.draw_overlay_window_hint);
                FloatWindowController.getInstance().createUsageAccessTip(HSApplication.getContext(), hintTxt);
                LauncherAnalytics.logEvent("Colorphone_SystemFloatWindowAccessView_Show", "from", eventSource.getName());
            }
            FloatWindowManager.getInstance().applyPermission(HSApplication.getContext());
        }

        return request;
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
        startObservingNotificationPermissionOneTime(actClass);
    }

    private static void startObservingNotificationPermissionOneTime(final Class activityClass) {
        ContentObserver observer = startObservingNotificationPermission(new OneTimeRunnable() {
            @Override
            public void oneTimeRun() {
                HSGlobalNotificationCenter.sendNotification(NOTIFY_NOTIFICATION_PERMISSION_GRANTED);
                bringActivityToFront(activityClass, 0);
            }
        });
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

    public static void waitOverlayGranted(final EventSource source, final boolean requestNotification) {
        final TagRunnable checker = new TagRunnable() {
            @Override
            public void run() {
                boolean enable = (Boolean) getTag();
                boolean hasPermission = FloatWindowManager.getInstance().checkPermission(HSApplication.getContext());
                boolean end = !enable || hasPermission;
                if (end) {
                    sHandler.removeCallbacks(this);
                } else {
                    sHandler.postDelayed(this, 400);
                }

                if (hasPermission) {
                    HSGlobalNotificationCenter.sendNotification(NOTIFY_OVERLAY_PERMISSION_GRANTED);
                    LauncherAnalytics.logEvent("Colorphone_FloatWindow_Access_Enabled", "from", source.getName());

                    if (requestNotification) {
                        requestNotificationAccessIfNeeded(source, null);
                    }
                }
            }
        };
        checker.setTag(Boolean.TRUE);

        sHandler.postDelayed(checker, 1000);
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checker.setTag(Boolean.FALSE);
            }
        }, 8000);
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

