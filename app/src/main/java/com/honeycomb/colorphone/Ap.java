package com.honeycomb.colorphone;

import android.text.TextUtils;

import com.honeycomb.colorphone.util.Analytics;

import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 2018/1/19.
 */

public class Ap {

    public static class Ringtone {

        /**
         * 使用 topic-1516083421924-90 - ringtone_btn_show 远程配置
         * ---------------------------------------------
         * Topic 名称:           Ringtone Test
         * Topic 描述:           铃声功能试验
         * Topic.x 可能值:       [true, false]
         * Topic.x 描述:         铃声功能及按钮是否显示
         */
        public static boolean isEnable() {
            return false;
        }

        /**
         * 使用 topic-1516083421924-90 - ringtone_auto_play 远程配置
         * ---------------------------------------------
         * Topic 名称:           Ringtone Test
         * Topic 描述:           铃声功能试验
         * Topic.x 可能值:       [true, false]
         * Topic.x 描述:         铃声是否自动播放
         */
        public static boolean isAutoPlay() {
            return false;
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题详情页展示
         */
        public static void onShow(Theme theme) {
            if (theme != null && !TextUtils.isEmpty(theme.getRingtoneUrl()) && isEnable()) {

            }
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题在详情页被应用
         */
        public static void onApply(Theme theme) {

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
            return false;
        }

        public static int intervalHour() {
            return 24;
        }

        public static void logEvent(String name) {

        }
    }

}
