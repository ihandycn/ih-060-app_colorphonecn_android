package com.acb.libwallpaper.live.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.Display;

import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.LauncherApplication;
import com.acb.libwallpaper.live.download.Downloader;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.util.EventUtils;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

public class ScreenStatus {

    private static final String TAG = ScreenStatus.class.getSimpleName();

    public static final String NOTIFICATION_SCREEN_ON = "screen_on";
    public static final String NOTIFICATION_SCREEN_OFF = "screen_off";

    public static final String PREF_KEY_TODAY_SCREEN_ON_TIMES = "PREF_KEY_TODAY_SCREEN_ON_TIME";
    public static final String PREF_KEY_LAST_SCREEN_ON_TIME = "PREF_KEY_LAST_SCREEN_ON_TIME";

    public static final String PREF_KEY_TODAY_UNLOCK_TIMES = "pref_key_phone_unlock_times_today";
    public static final String PREF_KEY_LAST_UNLOCK_TIME = "pref_key_last_unlock_time";
    public static final String PREF_KEY_LAST_SET_AS_DEFAULT_GUIDE_SHOW_TIME = "pref_key_last_set_as_default_guide_time";

    private static final int EVENT_PRESENT_RUNNABLE_EXPIRE = 0;

    private static boolean sScreenOn = fetchScreenStatus(HSApplication.getContext());
    private static long sScreenOnTime;
    private static long sScreenOffTime;

    private static boolean sShouldKillSelf;
    private static int sMainProcessLifeSpanMin;
    private static int sKillSelfDelaySec;
    private static float sMaxMemoryUsePercentage;
    private static Runnable sKillSelfRunnable = new Runnable() {

        @Override
        public void run() {
            if (!isScreenOn()) {
                HSLog.d(TAG, "Kill self");
                System.exit(0);
            }
        }
    };

    private static Runnable sPresentRunnable = null;
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

    public static boolean isScreenOn() {
        return sScreenOn;
    }

    public static long getScreenOnTime() {
        return sScreenOnTime;
    }

    public static long getScreenOffTime() {
        return sScreenOffTime;
    }

    public static void config() {
        sShouldKillSelf = HSConfig.optBoolean(false, "Application", "ShouldKillSelf");
        sMainProcessLifeSpanMin = HSConfig.optInteger(100, "Application", "MainProcessLifeSpanMin");
        sMaxMemoryUsePercentage = HSConfig.optFloat(0.7f, "Application", "MaxMemoryUsePercentage");
        sKillSelfDelaySec = HSConfig.optInteger(5, "Application", "KillSelfDelaySec");
    }

    public static void onScreenOn(final Context context) {
        HSLog.i(TAG, "Screen on");
        Threads.removeOnMainThread(sKillSelfRunnable);
        sScreenOn = true;
        sScreenOnTime = System.currentTimeMillis();
        if (sScreenOffTime > 0L) {
            long interval = sScreenOnTime - sScreenOffTime;

            // Note that this event is biased as we may have passed out during long screen-off periods.
            LauncherAnalytics.logEvent("Screen_Off_Interval", "Type",
                    EventUtils.getDurationDescription(interval));
        }
        // High-priority to update clock time
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_ON);

        Downloader.getInstance().resume();

        // for gamePromotions.
        markTodayScreenOnTimes();
    }

    public static void markTodayScreenOnTimes() {
        long keyTime = Preferences.get(LauncherFiles.COMMON_PREFS).getLong(PREF_KEY_LAST_SCREEN_ON_TIME, 0);
        if (DateUtils.isToday(keyTime)) {
            Preferences.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(PREF_KEY_TODAY_SCREEN_ON_TIMES);
        } else {
            Preferences.get(LauncherFiles.COMMON_PREFS).putLong(PREF_KEY_LAST_SCREEN_ON_TIME, System.currentTimeMillis());
            Preferences.get(LauncherFiles.COMMON_PREFS).putInt(PREF_KEY_TODAY_SCREEN_ON_TIMES, 1);
        }
    }

    public static void onScreenOff(Context context) {
        HSLog.i(TAG, "Screen off");
        sScreenOn = false;
        sScreenOffTime = System.currentTimeMillis();

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_OFF);
        Downloader.getInstance().pause();
        killSelfIfNeed();
        Threads.removeOnMainThread(analysisEventRunnable);
    }

    public static void markTodayUnlockTimes() {
        long keyTime = Preferences.get(LauncherFiles.COMMON_PREFS).getLong(PREF_KEY_LAST_UNLOCK_TIME, 0);
        if (DateUtils.isToday(keyTime)) {
            Preferences.get(LauncherFiles.COMMON_PREFS).incrementAndGetInt(PREF_KEY_TODAY_UNLOCK_TIMES);
        } else {
            Preferences.get(LauncherFiles.COMMON_PREFS).putLong(PREF_KEY_LAST_UNLOCK_TIME, System.currentTimeMillis());
            Preferences.get(LauncherFiles.COMMON_PREFS).putInt(PREF_KEY_TODAY_UNLOCK_TIMES, 1);
        }
    }

    private static Runnable analysisEventRunnable = () -> {
        if (sScreenOn) {
            LauncherAnalytics.logEvent("DefaultLauncher_Analysis", true, "Default Launcher", String.valueOf(Utils.isDefaultLauncher()));
            LauncherAnalytics.logEvent("Access_Analysis",
                    "Phone Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) >= 0),
                    "Storage Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) >= 0),
                    "Location Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) >= 0),
                    "Camera Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.CAMERA) >= 0),
                    "SMS Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_SMS) >= 0),
                    "Draw Over Apps", String.valueOf(Permissions.isFloatWindowAllowed(HSApplication.getContext())),
                    "Notification Access", String.valueOf(""),
                    "Usage Access", String.valueOf(Permissions.isUsageAccessGranted()),
                    "Contacts Access", String.valueOf(RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_CONTACTS) >= 0)
            );
        }
    };

    public static void setPresentRunnable(Runnable runnable) {
        sPresentRunnable = runnable;

        sHandler.removeCallbacksAndMessages(null);
        sHandler.sendEmptyMessageDelayed(EVENT_PRESENT_RUNNABLE_EXPIRE, 30 * DateUtils.SECOND_IN_MILLIS);
    }


    private static void killSelfIfNeed() {
        if (sShouldKillSelf) {
            long currentRealTime = SystemClock.elapsedRealtime();
            long mainProcessLifespan = (currentRealTime - LauncherApplication.sMainProcessCreatedTime) / 1000 / 60;
            HSLog.d(TAG, "mainProcessLifespan: " + mainProcessLifespan + " maxMainProcessLifespan: " + sMainProcessLifeSpanMin);
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            float percentage = (totalMemory - freeMemory) * 1.0f / maxMemory;
            HSLog.d(TAG, "percentage: " + percentage + " maxMemoryUsePercentage: " + sMaxMemoryUsePercentage);
            if (mainProcessLifespan >= sMainProcessLifeSpanMin && percentage >= sMaxMemoryUsePercentage) {
                Threads.postOnMainThreadDelayed(sKillSelfRunnable, sKillSelfDelaySec * 1000);
            }
        }
    }

    /**
     * Is the screen of the device on.
     *
     * @param context the context.
     * @return {@code true} when (at least one) screen is on.
     */
    public static boolean fetchScreenStatus(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }
}
