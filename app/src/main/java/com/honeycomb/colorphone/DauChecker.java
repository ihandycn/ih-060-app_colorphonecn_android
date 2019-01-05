package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;


public class DauChecker {
    private static final String KEY_TIME_LIVE = "time_has_live";
    private static final String KEY_TIME_DIE = "time_may_die";
    private static DauChecker sDauChecker = new DauChecker();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable lifeOnChecker = new Runnable() {
        @Override
        public void run() {
            mLifeDuration = SystemClock.elapsedRealtime() - mLifeStartTime;
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_LIVE, mLifeDuration);
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_DIE, System.currentTimeMillis());
            mHandler.postDelayed(lifeOnChecker, 1000 * 60 * 5);
        }
    };
    private long mLifeStartTime;
    private long mLifeDuration;

    public static DauChecker get() {
        return sDauChecker;
    }

    public boolean isActive() {
        boolean active = HSConfig.optBoolean(false, "Application", "ManualActiveUse");
        HSLog.d("DauCheck", "active = " + active);
        return active;
    }

    public void start() {
//        listenScreenOnOff();
        long lastDiedTime = Preferences
                .get(Constants.PREF_FILE_DEFAULT)
                .getLong(KEY_TIME_DIE, System.currentTimeMillis());
        long diedDuration = System.currentTimeMillis() - lastDiedTime;

        long liveDuation = Preferences
                .get(Constants.PREF_FILE_DEFAULT)
                .getLong(KEY_TIME_LIVE, 0);
        LauncherAnalytics.logEvent("DAU_Application_Live",
                LauncherAnalytics.FLAG_LOG_FABRIC, "Time", formatHourTime(liveDuation / DateUtils.MINUTE_IN_MILLIS));
        LauncherAnalytics.logEvent("DAU_Application_Die",
                LauncherAnalytics.FLAG_LOG_FABRIC, "Time", formatHourTime(diedDuration / DateUtils.MINUTE_IN_MILLIS));

        mLifeStartTime = SystemClock.elapsedRealtime();
        mHandler.postDelayed(lifeOnChecker, 1000 * 60 * 5);
    }

    private String formatHourTime(long min) {
        long hour = min / 60;
        if (min < 10) {
            return "10min";
        } else if (min < 30) {
            return "10-30min";
        } else if (hour > 48) {
            return "48+";
        } else if (hour > 24) {
            return "24-48";
        } else if (hour > 10) {
            return "10-24";
        }
        return String.valueOf(hour);
    }

    BroadcastReceiver screenOnAndOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_ON:
                case Intent.ACTION_USER_PRESENT:
                    if (isActive()) {
                        HSLog.d("DauCheck", "Allow");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void startNobodyActivity() {
//        long lastTime = Preferences.get(Constants.PREF_FILE_DEFAULT).getLong(Constants.PREF_NAME_TIME, 0);
//        boolean sameDay = Calendars.isSameDay(lastTime, System.currentTimeMillis());
//        if (!sameDay) {
//            Navigations.startActivitySafely(MyApplication.getContext(),
//                    new Intent(MyApplication.getContext(), Panel2Activity.class));
//
//         }
    }

    private void listenScreenOnOff() {
        IntentFilter screenOnAndOffIntentFilter = new IntentFilter();
        screenOnAndOffIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenOnAndOffIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        HSApplication.getContext().registerReceiver(screenOnAndOffReceiver, screenOnAndOffIntentFilter);
    }
}
