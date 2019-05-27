package com.honeycomb.colorphone;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.config.HSConfig;

import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 2018/1/19.
 */

public class Ap {

    public static class Ringtone {

        public static boolean isEnable() {
            return true;
        }

    }

    public static class DetailAd {

        public static boolean enableMainViewDownloadButton() {
            return false;
        }

        public static boolean enableThemeSlide() {
                return false;
        }

        public static boolean enableAdOnApply() {
            return false;
        }

        public static boolean enableAdOnDetailView() {
            return false;
        }


        public static void onThemeView() {
        }

        public static void onThemeChoose() {
        }

        public static void logEvent(String event) {
            Analytics.logEvent(Analytics.upperFirstCh(event));
        }

        public static void onPageScroll(int scrollCount) {
            Analytics.logEvent("ColorPhone_ThemeDetail_Slide", "Count", String.valueOf(scrollCount));
        }

        public static void onPageScrollOnce() {
//            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themedetail_slide");
        }
        public static void onThemeChooseForOne() {
//            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_seletcontactfortheme_success");
        }
    }

    public static void logEvent(String topicId, String eventName) {
        AutopilotEvent.logTopicEvent(topicId, eventName);
        Analytics.logEvent(eventName);
    }

    @Deprecated
    public static class Improver {
        public static boolean enable() {
            return false;
        }

        public static void logEvent(String name) {

        }
    }

    @Deprecated
    public static class RandomTheme {

        public static boolean enable() {
            return HSConfig.optBoolean(false, "Application", "RandomTheme", "Enable");
        }

        public static int intervalHour() {
            return HSConfig.optInteger(24, "Application", "RandomTheme", "TimeIntervalHour");
        }

        public static void logEvent(String name) {

        }
    }

}
