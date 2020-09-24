package com.honeycomb.colorphone.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationInfoBean {
    public String packageId;
    public long idInDB;
    public int notificationId;
    public String tag;
    public Notification notification;
    public PendingIntent contentIntent;
    public String title;
    public String text;
    public String key;
    public long postTime;
    public Bitmap icon;
    public NotificationInfoBean() {
    }
    public static NotificationInfoBean valueOf(StatusBarNotification statusBarNotification) {
        NotificationInfoBean notificationInfo = new NotificationInfoBean();
        notificationInfo.packageId = statusBarNotification.getPackageName();
        notificationInfo.postTime = statusBarNotification.getPostTime();
        Notification notification = statusBarNotification.getNotification();
        notificationInfo.notification = notification;
        notificationInfo.contentIntent = notification.contentIntent;
        notificationInfo.notificationId = statusBarNotification.getId();
        notificationInfo.tag = statusBarNotification.getTag();
//        notificationInfo.icon = notification.largeIcon;
        notificationInfo.key = statusBarNotification.getKey();
        Bundle extras;
        extras = notification.extras;
        if (null != extras) {
            notificationInfo.title = getExtrasTitle(extras);
            notificationInfo.text = getExtrasText(extras);
        }
        return notificationInfo;
    }

    private static String getExtrasTitle(final Bundle extras) {
        String result;
        CharSequence title = extras.getCharSequence("android.title");
        CharSequence bigTitle = extras.getCharSequence("android.title.big");
        if (!TextUtils.isEmpty(title)) {
            result = !TextUtils.isEmpty(bigTitle) ? String.valueOf(bigTitle) : String.valueOf(title);
        } else {
            result = !TextUtils.isEmpty(bigTitle) ? String.valueOf(bigTitle) : "";
        }
        return result;
    }
    private static String getExtrasText(final Bundle extras) {
        CharSequence text = extras.getCharSequence("android.text");
        if (!TextUtils.isEmpty(text)) {
            return text.toString();
        }
        CharSequence textLines = extras.getCharSequence("android.textLines");
        if (!TextUtils.isEmpty(textLines)) {
            return textLines.toString();
        }
        CharSequence subText = extras.getCharSequence("android.subText");
        if (!TextUtils.isEmpty(subText)) {
            return subText.toString();
        }
        return null;
    }
}
