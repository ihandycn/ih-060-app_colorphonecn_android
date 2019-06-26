package com.colorphone.lock.lockscreen;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.colorphone.lock.BuildConfig;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LockNotificationManager {

    private static LockNotificationManager lockNotificationManager;
    private AppNotificationInfo info;
    private List<ViewChangeObserver> list = new ArrayList<>();

    public AppNotificationInfo getInfo() {
        return info;
    }

    public static LockNotificationManager getInstance() {
        if (lockNotificationManager == null) {
            lockNotificationManager = new LockNotificationManager();
        }
        return lockNotificationManager;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void getNotificationInfo(StatusBarNotification statusBarNotification) {
        if (BuildConfig.DEBUG) {
            HSLog.e("Lock", "New notification:" + 11);
        }
        info = loadNotificationInfo(statusBarNotification);
        sendNotification();
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public AppNotificationInfo loadNotificationInfo(StatusBarNotification statusBarNotification) {
        if (BuildConfig.DEBUG) {
            HSLog.d("Lock", "New notification:" + 12);
        }

        AppNotificationInfo notificationInfo;
        notificationInfo = new AppNotificationInfo(statusBarNotification.getPackageName(),
                statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getNotification());

        notificationInfo.packageName = statusBarNotification.getPackageName();
        notificationInfo.notificationId = statusBarNotification.getId();
        notificationInfo.tag = statusBarNotification.getTag();

        Bundle extras = getExtras(notificationInfo.notification);

        if (null != extras) {
            notificationInfo.title = getExtrasTitle(extras);
            notificationInfo.content = getExtrasContent(extras);
            notificationInfo.text = getExtrasText(extras);
        }

        long time = System.currentTimeMillis() - statusBarNotification.getPostTime();

        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("HH:mm");
        notificationInfo.when = statusBarNotification.getPostTime();


        if (BuildConfig.DEBUG) {
            HSLog.d("Lock", "New notification:" + notificationInfo.content + " " + notificationInfo.title
                    + " " + notificationInfo.packageName + " " + notificationInfo.text + " " + notificationInfo.when + " "
                    + notificationInfo.tag + " " + notificationInfo.notificationId + " " + sdf.format(statusBarNotification.getPostTime()));
        }

        return notificationInfo;

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Nullable
    private static Bundle getExtras(@NonNull final Notification notification) {
        try {
            Field extrasField = com.superapps.util.ReflectionHelper.getField(notification.getClass(), "extras");
            return (Bundle) extrasField.get(notification);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static String getExtrasTitle(@NonNull final Bundle extras) {
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

    protected static String getExtrasContent(@NonNull final Bundle extras) {
        CharSequence text = extras.getCharSequence("android.text");
        if (!TextUtils.isEmpty(text)) {
            return text.toString();
        }

        CharSequence subText = extras.getCharSequence("android.subText");
        if (!TextUtils.isEmpty(subText)) {
            return subText.toString();
        }

        CharSequence[] textLines = extras.getCharSequenceArray("android.textLines");
        if (textLines != null && textLines.length > 0) {
            return textLines[textLines.length - 1].toString();
        }

        return null;
    }

    protected static String getExtrasText(@NonNull final Bundle extras) {
        CharSequence[] textLines = extras.getCharSequenceArray("android.textLines");
        if (textLines != null && textLines.length > 0) {
            return textLines[textLines.length - 1].toString();
        }

        CharSequence text = extras.getCharSequence("android.text");
        if (!TextUtils.isEmpty(text)) {
            return text.toString();
        }

        CharSequence subText = extras.getCharSequence("android.subText");
        if (!TextUtils.isEmpty(subText)) {
            return subText.toString();
        }

        return null;
    }


    public void sendNotification() {
        for (ViewChangeObserver observer : list) {
            observer.onReceive(info);
        }
    }

    public void registerForThemeStateChange(ViewChangeObserver observer) {
        list.add(observer);
    }

    public void unregisterForThemeStateChange(ViewChangeObserver observer) {
        list.remove(observer);
    }
}

