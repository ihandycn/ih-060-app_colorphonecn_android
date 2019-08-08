package com.colorphone.lock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

public class ScreenStatusReceiver {

    private static final String TAG = ScreenStatusReceiver.class.getSimpleName();

    public static final String NOTIFICATION_SCREEN_ON = "screen_on";
    public static final String NOTIFICATION_SCREEN_OFF = "screen_off";
    public static final String NOTIFICATION_PRESENT = "user_present";

    private static boolean sScreenOn = true;
    private static long sScreenOnTime;
    private static long sScreenOffTime;
    private static Runnable sPresentRunnable;

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

    public static void onUserPresent(Context context) {
        HSLog.i(TAG, "onUserPresent");
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_PRESENT);
        if (sPresentRunnable != null) {
            sPresentRunnable.run();
        }
    }

    public static void setPresentRunnable(Runnable runnable) {
        sPresentRunnable = runnable;
        sHandler.removeCallbacksAndMessages(null);
        sHandler.sendEmptyMessageDelayed(EVENT_PRESENT_RUNNABLE_EXPIRE, 30 * DateUtils.SECOND_IN_MILLIS);
    }

    public static final int EVENT_PRESENT_RUNNABLE_EXPIRE = 1;
    
    @SuppressLint("HandlerLeak")
    private static Handler sHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PRESENT_RUNNABLE_EXPIRE:
                    sPresentRunnable = null;
                    break;
                default:
                    break;
            }
        }
    };

}
