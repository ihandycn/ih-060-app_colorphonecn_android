package com.honeycomb.colorphone.notification;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.acb.notification.NotificationServiceListenerManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.commons.utils.HSLog;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationServiceV18 extends NotificationListenerService {

    public static final String TAG = NotificationServiceV18.class.getSimpleName();

    public NotificationServiceV18() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HSLog.i(TAG, "NotificationListenerService created");
    }

    @Override
    public void onListenerConnected() {
        HSLog.i(TAG, "NotificationListenerService onListenerConnected");
        NotificationServiceListenerManager.getInstance().onListenerConnected();
    }

    @SuppressLint("NewApi")
    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        NotificationServiceListenerManager.getInstance().onNotificationPosted(statusBarNotification);
        HSLog.e(TAG, "New notification: " + statusBarNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        NotificationServiceListenerManager.getInstance().onNotificationRemoved(sbn);
        HSLog.d(TAG, "Removed notification: " + sbn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HSLog.i(TAG, "NotificationListenerService destroyed");
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        StatusBarNotification[] notifications = null;
        try {
            notifications = super.getActiveNotifications();
        } catch (SecurityException e) {
            // The user has revoked our permission. Turn off the feature.
//            BadgeSettings.setBadgeEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notifications == null) {
            notifications = new StatusBarNotification[0];
        }
        return notifications;
    }
}
