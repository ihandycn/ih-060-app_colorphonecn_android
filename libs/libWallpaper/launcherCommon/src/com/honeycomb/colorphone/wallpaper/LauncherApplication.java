package com.honeycomb.colorphone.wallpaper;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.wallpaper.desktop.WallpaperChangedReceiver;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.theme.ThemeConstants;
import com.honeycomb.colorphone.wallpaper.util.PicCache;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class LauncherApplication {

    private static final String TAG = LauncherApplication.class.getSimpleName();
    public static final String PREF_KEY_SELLING_POINT_ID = "selling_point_id";
    public static final String NOTIFICAITON_TRIM_MEMORY_COMPLETE = "trim_memory_complete";
    public static final String NOTIFICATION_TIME_TICK = "time_tick";

    /**
     * Process holding live wallpaper service.
     */
    public static final String PROCESS_LIVE_WALLPAPER = "livewallpaper";

    public static final String PREF_KEY_FIRST_LAUNCHED = "first.launched";
    public static final String PREF_KEY_LOCALE_LOGGED = "locale.logged";
    public static final String PREF_KEY_DAILY_EVENTS_LOGGED_TIME = "default_launcher_logged_epoch";
    public static final String PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ = "default_launcher_log_session_seq";
    public static final String PREF_KEY_DEFAULT_LAUNCHER_STATUS_LOGGED_COUNT = "default_launcher_logged_times";
    private static final String PREF_KEY_LAST_CRASH_TIME = "last_crash_time";
    private static final String PREF_KEY_GOOGLE_AD_ID = "google_ad_id";
    public static final String PREF_KEY_ALLOW_NEW_WELCOME = "use_new_welcome";
    private static final String PREF_KEY_READ_NEW_WELCOME_CONFIG = "read_new_welcome_config";
    public static final String PREF_KEY_AD_CAFFE_ENABLE = "ad_caffe_enable";

    /**
     * We log daily events on start of the 2nd session every day.
     */
    private static final long DAILY_EVENTS_LOG_SESSION_SEQ = 2;

    static final String IMAGE_LOADER_CACHE_DIR = "image_loader";

    private long mGenesis;

    public static long sMainProcessCreatedTime;

    private Thread.UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;


    private WallpaperChangedReceiver mWallpaperChangedReceiver;

    private volatile static boolean isFabricInited;

    private boolean mAppsFlyerResultReceived;

    private BroadcastListener mDownloadCompleteReceiver = new BroadcastListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                LauncherAnalytics.logEvent("Launcher_Action_Download");
            }
        }
    };

    public static boolean isFabricInited() {
        return isFabricInited;
    }


    private String getDurationSinceGenesis() {
        return (SystemClock.elapsedRealtime() - mGenesis) + " ms";
    }

    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case HSNotificationConstant.HS_SESSION_START:

                Preferences prefs = Preferences.get(LauncherFiles.DESKTOP_PREFS);
                prefs.doOnce(new Runnable() {
                    @Override
                    public void run() {
                        // Default theme use time
                        Preferences.get(LauncherFiles.DESKTOP_PREFS).putLong(
                                ThemeConstants.PREF_THEME_SET_TIME + ":" + LauncherConstants.LAUNCHER_PACKAGE_NAME,
                                System.currentTimeMillis());

                        // Write installed time
                        Utils.getAppInstallTimeMillis();
                    }
                }, PREF_KEY_FIRST_LAUNCHED);

                break;
            case HSNotificationConstant.HS_SESSION_END:
                prefs = Preferences.get(LauncherFiles.DESKTOP_PREFS);
                long lastLoggedTime = prefs.getLong(PREF_KEY_DAILY_EVENTS_LOGGED_TIME, 0);
                long now = System.currentTimeMillis();
                int dayDifference = Calendars.getDayDifference(now, lastLoggedTime);
                if (dayDifference > 0) {
                    int sessionSeq = prefs.incrementAndGetInt(PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ);
                    if (sessionSeq == DAILY_EVENTS_LOG_SESSION_SEQ) {
                        prefs.putInt(PREF_KEY_DAILY_EVENTS_LOG_SESSION_SEQ, 0);
                        prefs.putLong(PREF_KEY_DAILY_EVENTS_LOGGED_TIME, now);
                    }
                }

                break;
            case HSNotificationConstant.HS_CONFIG_LOAD_FINISHED:
                HSLog.d(TAG, "hs config loadFinished!");
                break;
            case HSNotificationConstant.HS_CONFIG_CHANGED:
                HSLog.d(TAG, "hs config changed!");
                Threads.postOnThreadPoolExecutor(() -> PicCache.getInstance().clearUnnecessaryCachedPics());
                break;
            default:
                break;
        }
    }


    String getUseDays() {
        long days = (System.currentTimeMillis() - HSSessionMgr.getFirstSessionStartTime()) / 1000 / 3600 / 24;
        if (days <= 1) {
            return "leq 1 day";
        } else if (days <= 5) {
            return "leq 5 days";
        } else if (days <= 10) {
            return "leq 10 days";
        } else if (days > 70) {
            return "greater than 70 days";
        } else {
            return "leq " + ((days / 10) + 1) * 10 + " days";
        }
    }
}
