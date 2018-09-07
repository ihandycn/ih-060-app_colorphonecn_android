package com.honeycomb.colorphone.boost;

import android.text.format.DateUtils;

import com.honeycomb.colorphone.notification.NotificationCondition;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by zhewang on 12/01/2018.
 */

public class BoostAutoPilotUtils {
    private static final String BOOST_TEST_TOPIC_ID = "topic-1514209960668-50";

    public static boolean isBoostPushEnable() {
        return ModuleUtils.isShowModulesDueToConfig() || HSConfig.optBoolean(false, "Application", "Boost", "Enable");
    }

    public static double getBoostPushInterval() {
        double interval = AutopilotConfig.getDoubleToTestNow(BOOST_TEST_TOPIC_ID, "boost_push_interval", 2);
        HSLog.i(NotificationCondition.TAG, "Debug getBoostPushInterval == " + interval);
        if (NotificationCondition.DEBUG_BOOST_PLUS_NOTIFICATION) {
            return 0;
        }
        return DateUtils.HOUR_IN_MILLIS * interval;
    }

    public static int getBoostPushMaxCount() {
        double max = AutopilotConfig.getDoubleToTestNow(BOOST_TEST_TOPIC_ID, "boost_push_maxcount", 4);
        HSLog.i(NotificationCondition.TAG, "Debug getBoostPushMaxCount == " + max);
        if (NotificationCondition.DEBUG_BOOST_PLUS_NOTIFICATION) {
            return 100;
        }
        return (int) max;
    }

    public static void logBoostPushShow() {
        HSLog.i(NotificationCondition.TAG, "boost_push_show");
        AutopilotEvent.logTopicEvent(BOOST_TEST_TOPIC_ID, "boost_push_show");
    }

    public static void logBoostPushClicked() {
        HSLog.i(NotificationCondition.TAG, "boost_push_clicked");
        AutopilotEvent.logTopicEvent(BOOST_TEST_TOPIC_ID, "boost_push_clicked");
    }
    public static void logBoostPushAdShow() {
        HSLog.i(NotificationCondition.TAG, "boost_ad_show");
        AutopilotEvent.logTopicEvent(BOOST_TEST_TOPIC_ID, "boost_ad_show");
    }

    public static void dump() {
        isBoostPushEnable();
        getBoostPushInterval();
        getBoostPushMaxCount();
    }

}
