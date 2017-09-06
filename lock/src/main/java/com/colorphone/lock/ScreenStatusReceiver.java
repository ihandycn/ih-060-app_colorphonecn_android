package com.colorphone.lock;

import android.content.Context;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

public class ScreenStatusReceiver {

    private static final String TAG = ScreenStatusReceiver.class.getSimpleName();

    public static final String NOTIFICATION_SCREEN_ON = "screen_on";
    public static final String NOTIFICATION_SCREEN_OFF = "screen_off";

    private static boolean sScreenOn = true;
    private static long sScreenOnTime;
    private static long sScreenOffTime;

    public static boolean isScreenOn() {
        return sScreenOn;
    }

    public static long getScreenOnTime() {
        return sScreenOnTime;
    }

    public static long getScreenOffTime() {
        return sScreenOffTime;
    }

    public static void onScreenOn(final Context context) {
        HSLog.i(TAG, "Screen on");
        sScreenOn = true;
        sScreenOnTime = System.currentTimeMillis();
        long interval = (sScreenOnTime - sScreenOffTime) / 1000 / 10 * 10;

        // High-priority to update clock time

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_ON);


    }

    public static void onScreenOff(Context context) {
        HSLog.i(TAG, "Screen off");
        sScreenOn = false;
        sScreenOffTime = System.currentTimeMillis();

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_OFF);

    }
}
