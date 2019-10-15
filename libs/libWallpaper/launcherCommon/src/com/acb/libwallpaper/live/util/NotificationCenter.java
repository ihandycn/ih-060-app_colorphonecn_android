package com.acb.libwallpaper.live.util;

import android.text.TextUtils;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps over {@link HSGlobalNotificationCenter} and provides a simple "sticky"
 * notification mechanism. Note that sticky notification differs from Android sticky broadcast in that it is
 * only designed for one-to-one scenario.
 * <p>
 * Note: sticky notifications are stateful and hard to maintain.
 * It's NOT recommended to use this class if you don't have to.
 */
@SuppressWarnings("SuspiciousMethodCalls")
public class NotificationCenter {

    private static List<BufferedNotification> sBufferedNotifications = new ArrayList<>(2);

    public static void sendStickyNotification(String notificationName, HSBundle data) {
        HSGlobalNotificationCenter.sendNotification(notificationName, data);
        sBufferedNotifications.remove(notificationName);
        sBufferedNotifications.add(new BufferedNotification(notificationName, data));
    }

    public static void clearNotification(String notificationName) {
        sBufferedNotifications.remove(notificationName);
    }

    public static synchronized void addObserver(String notificationName, INotificationObserver observer) {
        HSGlobalNotificationCenter.addObserver(notificationName, observer);
        if (sBufferedNotifications.contains(notificationName)) {
            for (BufferedNotification notification : sBufferedNotifications) {
                if (TextUtils.equals(notificationName, notification.name)) {
                    observer.onReceive(notification.name, notification.data);
                }
            }
            sBufferedNotifications.remove(notificationName);
        }
    }

    private static class BufferedNotification {
        String name;
        HSBundle data;

        BufferedNotification(String name, HSBundle data) {
            this.name = name;
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                return obj.equals(name);
            }
            return super.equals(obj);
        }
    }
}
