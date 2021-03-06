package com.colorphone.lock.lockscreen;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.chargingscreen.CharingScreenChangeObserver;
import com.colorphone.lock.lockscreen.locker.TimeTextSizeChangeObserver;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@TargetApi (Build.VERSION_CODES.KITKAT)
public class LockNotificationManager {

    private static LockNotificationManager lockNotificationManager;
    private final String mDefaultPhone;
    private final String mDefaultSMS;
    private final List<String> eventPkgList;
    private AppNotificationInfo info;
    private AppNotificationInfo clickedNotification;
    private List<NotificationObserver> list = new ArrayList<>();
    private List<CharingScreenChangeObserver> list1 = new ArrayList<>();
    private List<TimeTextSizeChangeObserver> timeSizeChangeObserverList = new ArrayList<>();
    private List<String> mWantedAppList = new ArrayList<>();

    public static Drawable getAppIcon(String packageName){
        try {
            PackageManager pm = HSApplication.getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return null;
    }

    public static String getAppName(String packageName){
        try {
            PackageManager pm = HSApplication.getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return null;
    }

    public AppNotificationInfo getClickedNotification() {
        return clickedNotification;
    }

    public void setClickedNotification(AppNotificationInfo clickedNotification) {
        this.clickedNotification = clickedNotification;
    }

    public AppNotificationInfo getInfo() {
        return info;
    }

    private LockNotificationManager() {
        List<String> whiteList = (List<String>) HSConfig.getList("Application", "Locker", "Notification", "WhiteList");
        eventPkgList = (List<String>) HSConfig.getList("Application", "Locker", "NotificationAll");
        mDefaultSMS = findDefaultSMSPkg();
        mDefaultPhone = findDefaultDialerPkg();

        for (String appName : whiteList) {
            String pkgName = appName;
            if ("Phone".equalsIgnoreCase(appName)) {
                pkgName = mDefaultPhone;
            } else if ("Text".equalsIgnoreCase(appName)) {
                pkgName = mDefaultSMS;
            }
            if (!TextUtils.isEmpty(pkgName)) {
                mWantedAppList.add(pkgName);
            }
        }
    }

    public static LockNotificationManager getInstance() {
        if (lockNotificationManager == null) {
            synchronized (LockNotificationManager.class) {
                if (lockNotificationManager == null) {
                    lockNotificationManager = new LockNotificationManager();
                }
            }
        }
        return lockNotificationManager;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @WorkerThread
    public void onNotificationPosted(final StatusBarNotification statusBarNotification) {
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification" );
        }
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                onNotificationPostedMainThread(statusBarNotification);
            }
        });
    }

    private void onNotificationPostedMainThread(StatusBarNotification statusBarNotification) {

        if (eventPkgList.contains(statusBarNotification.getPackageName())) {
            LockerCustomConfig.getLogger().logEvent("ColorPhone_Notification_Receive_All",
                    "Source",  statusBarNotification.getPackageName());
        }

        if (isNotificationWanted(statusBarNotification)) {
            boolean isScreenOn = ScreenStatusReceiver.isScreenOn();

            if (!isScreenOn && list.isEmpty()) {
                LockerCustomConfig.getLogger().logEvent("ColorPhone_Notification_Missed_ScreenOff",
                        "Source", getEventSourceName(statusBarNotification.getPackageName()));
            }
            AppNotificationInfo appNotificationInfo = loadNotificationInfo(statusBarNotification);
            boolean isValid = !TextUtils.isEmpty(appNotificationInfo.content) && !TextUtils.isEmpty(appNotificationInfo.title);
            if (isValid) {
                info = appNotificationInfo;
                sendNotification();
            }
        }
    }

    private boolean isNotificationWanted(StatusBarNotification statusBarNotification) {
        String pkgName = statusBarNotification.getPackageName();
        return mWantedAppList.contains(pkgName);
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
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (NotificationObserver observer : list) {
                    observer.onReceive(info);
                }
            }
        });
    }

    public void registerForThemeStateChange(NotificationObserver observer) {
        list.add(observer);
    }

    public void unregisterForThemeStateChange(NotificationObserver observer) {
        list.remove(observer);
    }

    public void registerForChargingScreenChange(CharingScreenChangeObserver observer) {
        list1.add(observer);
    }

    public void unregisterForChargingScreenChange(CharingScreenChangeObserver observer) {
        list1.remove(observer);
    }

    public void notifyForUpdatingChargingNumberSize(final int showNumber) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (CharingScreenChangeObserver observer : list1) {
                    observer.onReceive(showNumber);
                }
            }
        });
    }



    public void registerForLockerSpaceNotEnough(TimeTextSizeChangeObserver observer) {
        timeSizeChangeObserverList.add(observer);
    }

    public void unregisterForLockerSpaceNotEnough(TimeTextSizeChangeObserver observer) {
        timeSizeChangeObserverList.remove(observer);
    }

    public void notifyForUpdatingLockerTimeSize(final int showNumber, final int yCoordinateOfAboveNotification) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (TimeTextSizeChangeObserver observer : timeSizeChangeObserverList) {
                    observer.update(showNumber, yCoordinateOfAboveNotification);
                }
            }
        });
    }

    private String findDefaultDialerPkg() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + "911");
        intent.setData(data);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        final String myPkg = HSApplication.getContext().getPackageName();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!TextUtils.equals(resolveInfo.activityInfo.packageName, myPkg)) {
                return resolveInfo.activityInfo.packageName;
            }
        }
        return null;
    }

    private String findDefaultSMSPkg() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        Uri data = Uri.parse("smsto:" + "911");
        intent.setData(data);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        final String myPkg = HSApplication.getContext().getPackageName();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!TextUtils.equals(resolveInfo.activityInfo.packageName, myPkg)) {
                return resolveInfo.activityInfo.packageName;
            }
        }
        return null;
    }

    private String getEventSourceName(String pkg) {
        if (pkg.equalsIgnoreCase(mDefaultPhone)) {
            return "Phone";
        } else if (pkg.equalsIgnoreCase(mDefaultSMS)) {
            return "Text";
        }
        return pkg;
    }

    public void logEvent(String event, String pkg, String receiveNumber, String showNumber) {
        LockerCustomConfig.getLogger().logEvent(event,
                "Source", getEventSourceName(pkg), "ReceiveNumber", receiveNumber, "ShowNumber", showNumber);
    }

    public void logEvent(String event, String pkg) {
        LockerCustomConfig.getLogger().logEvent(event,
                "Source", getEventSourceName(pkg));
    }
    public void logEvent(String event) {
        LockerCustomConfig.getLogger().logEvent(event);
    }
}

