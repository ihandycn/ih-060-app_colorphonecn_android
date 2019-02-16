package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.trigger.DailyTrigger;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;



public class DauChecker {
    private static final String KEY_TIME_LIVE = "time_has_live";
    private static final String KEY_TIME_DIE = "time_may_die";
    private static final String KEY_TIME_LIVE_TOTAL = "time_total_live";
    private static final String KEY_COUNT_LIVE_TOTAL = "count_total_live";

    private static DauChecker sDauChecker = new DauChecker();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable lifeOnChecker = new Runnable() {
        @Override
        public void run() {
            // app life add up
            long lastCheckDuration = (SystemClock.elapsedRealtime() - mLastCheckTime);
            mLifeDuration += lastCheckDuration;
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_LIVE, mLifeDuration);
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_DIE, System.currentTimeMillis());

            // daily add up
            long savedDuration = Preferences.get(Constants.PREF_FILE_DEFAULT).getLong(KEY_TIME_LIVE_TOTAL, 0);
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_LIVE_TOTAL, savedDuration + lastCheckDuration);

            mHandler.postDelayed(lifeOnChecker, 1000 * 60 * 5);
            mLastCheckTime = SystemClock.elapsedRealtime();

            if (mLifeDuration >= DateUtils.DAY_IN_MILLIS) {
                checkDailyRecordAndLog();
            }
        }
    };
    private long mLifeStartTime;
    private long mLastCheckTime;
    private long mLifeDuration;
    private DailyTrigger mDailyTrigger = new DailyTrigger();

    public static DauChecker get() {
        return sDauChecker;
    }

    public boolean isActive() {
        boolean active = HSConfig.optBoolean(false, "Application", "ManualActiveUse");
        HSLog.d("DauCheck", "active = " + active);
        return active;
    }

    public void start() {
        listenScreenOnOff();
        mLifeStartTime = SystemClock.elapsedRealtime();
        mLastCheckTime = mLifeStartTime;

        checkDailyRecordAndLog();

        logApplicationCreate();

        Preferences.get(Constants.PREF_FILE_DEFAULT).incrementAndGetInt(KEY_COUNT_LIVE_TOTAL);

        mHandler.postDelayed(lifeOnChecker, 1000 * 60 * 2);
    }

    private void logApplicationCreate() {
        long liveDuration = Preferences
                .get(Constants.PREF_FILE_DEFAULT)
                .getLong(KEY_TIME_LIVE, 0);
        if (liveDuration != 0) {
            Analytics.logEvent("DAU_Application_Live",
                    Analytics.FLAG_LOG_FABRIC, "Time", formatHourTime(liveDuration / DateUtils.MINUTE_IN_MILLIS));

            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_LIVE, 0);
        }
    }

    private void checkDailyRecordAndLog() {
        boolean hasDailyChance = mDailyTrigger.onChance();
        if (hasDailyChance) {
            long totalTime = Preferences.get(Constants.PREF_FILE_DEFAULT)
                    .getLong(KEY_TIME_LIVE_TOTAL,0);

            int count = Preferences.get(Constants.PREF_FILE_DEFAULT)
                    .getInt(KEY_COUNT_LIVE_TOTAL, 0);

            // No record
            if (totalTime == 0 && count == 0 ) {
                return;
            }
            // Log
            Analytics.logEvent("DAU_Application_Check_" + getDeviceInfo(),
                    "Time", formatTotalTime(totalTime / DateUtils.MINUTE_IN_MILLIS),
                    "Count", String.valueOf(count));
            // Reset
            Preferences.get(Constants.PREF_FILE_DEFAULT).putInt(KEY_COUNT_LIVE_TOTAL, 0);
            Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(KEY_TIME_LIVE_TOTAL, 0);
            mDailyTrigger.onConsumeChance();
        }

    }

    private String formatTotalTime(long min) {
        long hour = min / 60;
        if (min < 10) {
            return "0-10min";
        } else if (min < 60) {
            return "10-60min";
        } else if (hour > 24) {
            return "24+";
        } else if (hour > 18) {
            return "18-24";
        } else if (hour < 18 && hour >= 12) {
            return "12-18";
        } else if (hour < 12 && hour >= 8) {
            return "8-12";
        } else if (hour < 8 && hour >= 5) {
            return "5-8";
        }
        return String.valueOf(hour);
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

    private String getDeviceInfo() {
        if (Build.VERSION.SDK_INT >= 26) {
            return "8";
        } else if (Build.VERSION.SDK_INT >= 24) {
            return "7";
        } else if (Build.VERSION.SDK_INT >= 23) {
            return "6";
        } else if (Build.VERSION.SDK_INT >= 21) {
            return "5";
        } else {
            return "4";
        }
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


    private void listenScreenOnOff() {
        IntentFilter screenOnAndOffIntentFilter = new IntentFilter();
        screenOnAndOffIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenOnAndOffIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        HSApplication.getContext().registerReceiver(screenOnAndOffReceiver, screenOnAndOffIntentFilter);
    }
}
