package com.honeycomb.colorphone.news;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class NewsTest {
    private static final String TOPIC_ID = "topic-73gcd98v9";
    private static final String PREF_FILE = "news";
    private static final String PREF_KEY_LAST_SHOW_NEWS_ALERT_TIME = "pref_key_last_show_news_alert_time";

    public static boolean isNewsAlertAllowBack() {
//        return true;
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "news_alert_allow_back", false);
    }

    public static boolean isNewsAlertEnable() {
//        return true;
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "news_alert_enable", false);
    }

    private static int getNewsAlertShowFirstTimeAfterInstall() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "news_alert_show_first_time_after_install", 2);
    }

    private static int getNewsAlertShowIntervalMinutes() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "news_alert_show_interval_minutes", 30);
    }

    public static boolean isNewsAlertShowOnlyWhenGetAd() {
//        return true;
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "news_alert_show_only_when_get_ad", false);
    }

    private static String getNewsAlertType() {
//        return "others";
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "news_alert_type", "image");
    }

    public static boolean isNewsAlertWithBigPic() {
        return TextUtils.equals(getNewsAlertType(), "image");
    }

    public static int getNewsWireShowIntervalSecond() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "news_wire_show_interval_seconds", 300);
    }

    public static boolean canShowNewsAlert() {
        if (!isNewsAlertEnable()) {
            HSLog.i(NewsManager.TAG, "isNewsAlertEnable false");
            return false;
        }

        if ((System.currentTimeMillis() - Utils.getAppInstallTimeMillis())
                < getNewsAlertShowFirstTimeAfterInstall() * DateUtils.HOUR_IN_MILLIS) {
            HSLog.i(NewsManager.TAG, "canShowNewsAlert  install time too short");
            return false;
        }

        if ((System.currentTimeMillis() - getLastShowNewsAlertTime())
                < getNewsAlertShowIntervalMinutes() * DateUtils.MINUTE_IN_MILLIS) {
            HSLog.i(NewsManager.TAG, "canShowNewsAlert  last show time too short");
            return false;
        }

        logNewsEvent("news_alert_should_show");

        return true;
    }

    private static long getLastShowNewsAlertTime() {
        return Preferences.get(PREF_FILE).getLong(PREF_KEY_LAST_SHOW_NEWS_ALERT_TIME, 0);
    }

    public static void recordShowNewsAlertTime() {
        Preferences.get(PREF_FILE).putLong(PREF_KEY_LAST_SHOW_NEWS_ALERT_TIME, System.currentTimeMillis());
    }

    public static void logNewsEvent(String eventID) {
        isNewsAlertEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, eventID);

        LauncherAnalytics.logEvent(eventID);
    }

}
