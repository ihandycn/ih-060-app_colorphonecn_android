package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.Calendar;

public class ADAutoPilotUtils {
    private static final String AD_CALLFINISH_AND_THEME_TOPIC_ID = "topic-6y9jgfwg2";
    private static final String PREF_CALLFINISH_SHOW_COUNT = "pref_callfinish_show_count";
    private static final String PREF_CALLFINISH_SHOW_LAST_TIME = "pref_callfinish_show_last_time";
    private static final String PREF_THEMEWIRE_SHOW_COUNT = "pref_themewire_show_count";
    private static final String PREF_THEMEWIRE_SHOW_LAST_TIME = "pref_themewire_show_last_time";

    private static final int DAY_DIV = 10000;
    private static final int MONTH_DAY_VALUE = 100;

    public static int getCallFinishWireShowMaxTime() {
        int sMaxCallFinishShow = (int) AutopilotConfig.getDoubleToTestNow(AD_CALLFINISH_AND_THEME_TOPIC_ID, "callfinishwire_show_maxtime", 100);
        HSLog.d("ADAutoPilotUtils", "getCallFinishWireShowMaxTime == " + sMaxCallFinishShow);
        return Math.min(sMaxCallFinishShow, HSConfig.optInteger(1000, "Application", "CallFinishWire", "Maxtime"));
    }

    public static int getCallFinishWireTimeInterval() {
        int sIntervalCallFinishShow = (int) (AutopilotConfig.getDoubleToTestNow(AD_CALLFINISH_AND_THEME_TOPIC_ID, "callfinishwire_time_interval_minute", 0) * DateUtils.MINUTE_IN_MILLIS);
        HSLog.d("ADAutoPilotUtils", "getCallFinishWireTimeInterval == " + sIntervalCallFinishShow);
        return sIntervalCallFinishShow;
    }

    public static int getThemeWireShowMaxTime() {
        int sMaxThemeShow = (int) AutopilotConfig.getDoubleToTestNow(AD_CALLFINISH_AND_THEME_TOPIC_ID, "themewire_show_maxtime", 100);
        HSLog.d("ADAutoPilotUtils", "getThemeWireShowMaxTime == " + sMaxThemeShow);
        return Math.min(sMaxThemeShow, HSConfig.optInteger(1000, "Application", "FullScreen", "Maxtime"));
    }

    public static int getThemeWireShowInterval() {
        int sIntervalThemeShow = (int) (AutopilotConfig.getDoubleToTestNow(AD_CALLFINISH_AND_THEME_TOPIC_ID, "themewire_show_interval_second", 0) * DateUtils.SECOND_IN_MILLIS);
        HSLog.d("ADAutoPilotUtils", "getThemeWireShowInterval == " + sIntervalThemeShow);
        return sIntervalThemeShow;
    }

    public static void logThemeWireShow() {
        getThemeWireShowMaxTime();
        AutopilotEvent.logTopicEvent(AD_CALLFINISH_AND_THEME_TOPIC_ID, "colorphone_themewire_show");
    }

    public static void logCallFinishWireShow() {
        getCallFinishWireShowMaxTime();
        AutopilotEvent.logTopicEvent(AD_CALLFINISH_AND_THEME_TOPIC_ID, "colorphone_callfinishwire_show");
    }

    private static long getThemeWireLastShowTime() {
        return Preferences.getDefault().getLong(PREF_THEMEWIRE_SHOW_LAST_TIME, 0);
    }

    public static void recordShowThemeWireTime() {
        Preferences.getDefault().putLong(PREF_THEMEWIRE_SHOW_LAST_TIME, System.currentTimeMillis());

    }

    public static void recordShowThemeWireCount() {
        int recordValue = Preferences.getDefault().getInt(PREF_THEMEWIRE_SHOW_COUNT, 0);
        int recordDay = recordValue / DAY_DIV;
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.MONTH) * MONTH_DAY_VALUE + today.get(Calendar.DAY_OF_MONTH);
        if (todayDay != recordDay) {
            recordValue = todayDay * DAY_DIV + 1;
        } else {
            recordValue++;
        }
        HSLog.d("ADAutoPilotUtils", "recordShowThemeWire recordValue = " + recordValue);
        Preferences.getDefault().putInt(PREF_THEMEWIRE_SHOW_COUNT, recordValue);
    }

    private static int getThemeWireShowCount() {
        int recordValue = Preferences.getDefault().getInt(PREF_THEMEWIRE_SHOW_COUNT, 0);
        int count = recordValue % DAY_DIV;
        int recordDay = recordValue / DAY_DIV;
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.MONTH) * MONTH_DAY_VALUE + today.get(Calendar.DAY_OF_MONTH);
        HSLog.d("ADAutoPilotUtils", "getThemeWireShowCount recordV = " + recordValue + "    today == " + todayDay);
        if (todayDay != recordDay) {
            return 0;
        } else {
            return count;
        }
    }

    public static boolean canShowThemeWireADThisTime() {
        boolean showInterval = System.currentTimeMillis() - getThemeWireLastShowTime() > getThemeWireShowInterval();
        boolean showCount = getThemeWireShowCount() < getThemeWireShowMaxTime();
        HSLog.d("ADAutoPilotUtils", "canShowThemeWireADThisTime i,c = " + showInterval + ", " + showCount);
        return showInterval && showCount;
    }

    public static void update() {
    }

    public static void logAutopilotEventToFaric() {


    }
}
