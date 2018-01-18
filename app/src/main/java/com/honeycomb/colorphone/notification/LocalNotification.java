package com.honeycomb.colorphone.notification;

import android.app.PendingIntent;
import android.support.annotation.ColorInt;

import java.io.Serializable;

public class LocalNotification implements Serializable {

    static final int PICTORIAL_CONTENT_TYPE_NONE = 0;
    static final int PICTORIAL_CONTENT_TYPE_ICONS = 1;
    static final int PICTORIAL_CONTENT_TYPE_BAR = 2;

    public int notificationId = hashCode();
    long autoCleanTimeMills; // Notification remove itself when timeout.
    public CharSequence title;
    public CharSequence description;
    CharSequence buttonText;
    @ColorInt int primaryColor;
    int iconDrawableId;
    int iconDrawableIdRealStyle;
    int buttonBgDrawableId;
    int smallIconDrawableId;
    int pictorialContentType = PICTORIAL_CONTENT_TYPE_ICONS;
    PendingIntent pendingIntent;
    PendingIntent deletePendingIntent;
    boolean isHeadsUp;
}
