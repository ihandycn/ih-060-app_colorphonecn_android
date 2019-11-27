package com.honeycomb.colorphone.util;

import android.util.Log;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;

import java.util.concurrent.TimeUnit;

import okhttp3.internal.Util;

public class EventUtils {

    private static final String TAG = "EventUtils";
    private static final String PREF_COLORPHONE_RETENTION_DAY1 = "pref_colorphone_retention_day1";


    public static void tryToLogRetentionEvent() {
        Log.d(TAG, "tryToLogRetentionEvent");
        if (Preferences.getDefault().getBoolean(PREF_COLORPHONE_RETENTION_DAY1, false)) {
            return;
        }

        if (!Utils.isNewUser()) {
            return;
        }
        long installMills = Utils.getAppInstallTimeMillis();

        long oneDayMills = TimeUnit.DAYS.toMillis(1);
        long alreadyDayMills = installMills % oneDayMills;
        long lastDayMills = oneDayMills - alreadyDayMills;
        long duration = System.currentTimeMillis() - installMills;
        if (duration > lastDayMills && duration < (lastDayMills + oneDayMills)) {
            Preferences.getDefault().putBoolean(PREF_COLORPHONE_RETENTION_DAY1, true);
            Analytics.logEvent("ColorPhone_Retention_Day1");
            HSAnalytics.logEventToAppsFlyer("ColorPhone_Retention_Day1");
        }
    }
}
