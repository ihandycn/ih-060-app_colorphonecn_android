package com.colorphone.lock.lockscreen;

import android.app.Notification;
import android.os.Bundle;
import android.text.TextUtils;



public class AppNotificationInfo extends Information {
    private static final long serialVersionUID = -1095503244134979974L;
    public String packageName;
    public Notification notification;
    public String tag;
    public String content;
    public int notificationId;

    public AppNotificationInfo(String packageName, Notification notification) {
        updateNotificationInfo(packageName, notification);
    }

    public AppNotificationInfo(String packageName, String tag, int notificationId, Notification notification) {
        this.tag = tag;
        this.notificationId = notificationId;

        updateNotificationInfo(packageName, notification);
    }

    protected void updateNotificationInfo(String packageName, Notification notification) {

        this.packageName = packageName;
        this.notification = notification;
        when = notification.when;
    }

    @Override
    public String toString() {
        return packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AppNotificationInfo) {
            AppNotificationInfo info = (AppNotificationInfo) o;
            return TextUtils.equals(info.packageName, packageName)
                    && TextUtils.equals(info.title, title)
                    && TextUtils.equals(info.text, text);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (packageName + title + text).hashCode();
    }

}
