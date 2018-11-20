package com.honeycomb.colorphone;

import android.text.TextUtils;
import android.widget.Toast;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.superapps.util.Toasts;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.Locale;

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
            boolean ringtoneBtnShowBoolean = AutopilotConfig.getBooleanToTestNow("topic-1516083421924-90", "ringtone_btn_show", false);
            if (BuildConfig.DEBUG) {
                Toasts.showToast("【Autopilot】铃声功能：" + ringtoneBtnShowBoolean, Toast.LENGTH_SHORT);
            }
            return ringtoneBtnShowBoolean;
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
            boolean ringtoneAutoPlayBoolean = AutopilotConfig.getBooleanToTestNow("topic-1516083421924-90", "ringtone_auto_play", false);
            if (BuildConfig.DEBUG) {
                Toasts.showToast("【Autopilot】铃声自动播放：" + ringtoneAutoPlayBoolean, Toast.LENGTH_SHORT);
            }
            return ringtoneAutoPlayBoolean;
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题详情页展示
         */
        public static void onShow(Theme theme) {
            if (theme != null && !TextUtils.isEmpty(theme.getRingtoneUrl()) && isEnable()) {
                try {
                    String formatString = String.format(Locale.ENGLISH, "theme_%s_detail_page_show", theme.getIdName().toLowerCase());
                    AutopilotEvent.logTopicEvent("topic-1516083421924-90", formatString);
                } catch (Exception ignore) {}
            }
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题在详情页被应用
         */
        public static void onApply(Theme theme) {
            if (theme != null && !TextUtils.isEmpty(theme.getRingtoneUrl()) && isEnable()) {
                String formatString = String.format(Locale.ENGLISH, "theme_%s_detail_page_apply", theme.getIdName().toLowerCase());
                AutopilotEvent.logTopicEvent("topic-1516083421924-90", formatString);
            }
        }

    }

    public static class Avatar {

        /**
         * 使用 topic-1516620266175-105 - avatar_title 远程配置
         * ---------------------------------------------
         * Topic 名称:           Avatar Test
         * Topic 描述:           Avatar Test
         * Topic.x 可能值:       ["Display your avatar in your friends\u2019 phone right away", "Create funny avatars for your friends!", "Create your avatar right away"]
         * Topic.x 描述:         avatar标题文案
         */
        public static String getAvatarTitleString() {
            String avatarTitleString = AutopilotConfig.getStringToTestNow("topic-1516620266175-105", "avatar_title",
                    "Display your avatar in your friends’ phone right away");
            return avatarTitleString;
        }

        /**
         * 使用 topic-1516620266175-105 - avatar_button_text 远程配置
         * ---------------------------------------------
         * Topic 名称:           Avatar Test
         * Topic 描述:           Avatar Test
         * Topic.x 可能值:       ["Start Now"]
         * Topic.x 描述:         avatar按钮上的文案
         */
        public static String getButtonTextString() {
            String avatarButtonTextString = AutopilotConfig.getStringToTestNow("topic-1516620266175-105", "avatar_button_text", "Start Now");
            return avatarButtonTextString;
        }
    }

    public static class MsgBall {
        public static boolean enable() {
            boolean ringtoneBtnShowBoolean = AutopilotConfig.getBooleanToTestNow("topic-1531210959452-409", "message_floatingball_enable", false);
            if (BuildConfig.DEBUG) {
                Toasts.showToast("【Autopilot】MsgBall：" + ringtoneBtnShowBoolean, Toast.LENGTH_SHORT);
            }
            return ringtoneBtnShowBoolean;
        }

        public static void onShow() {
            AutopilotEvent.logTopicEvent("topic-1531210959452-409", "message_floatingball_view_show");
        }

        public static void onClick() {
            AutopilotEvent.logTopicEvent("topic-1531210959452-409", "message_floatingball_view_click");
        }

        public static void onCancel() {
            AutopilotEvent.logTopicEvent("topic-1531210959452-409", "message_floatingball_cancel");
        }

        public static void onAdShow() {
            AutopilotEvent.logTopicEvent("topic-1531210959452-409", "message_floatingball_ad_show");
        }
    }

    public static class DetailAd {

        public static String TOPIC_ID = "topic-6wzryt9bs";
        public static boolean enableMainViewDownloadButton() {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "mainviewapplyicon", false);
        }

        public static boolean enableThemeSlide() {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "detailslide", false);
        }

        public static boolean enableAdOnApply() {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "applywire", false);
        }

        public static boolean enableAdOnDetailView() {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "detailviewwire", false);
        }


        public static void onThemeView() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themedetail_view");
        }

        public static void onThemeChoose() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themedetail_choosetheme");
        }

        public static void logEvent(String event) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, event);
            LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh(event));
        }

        public static void onPageScroll(int scrollCount) {
            LauncherAnalytics.logEvent("ColorPhone_ThemeDetail_Slide", "Count", String.valueOf(scrollCount));
        }

        public static void onPageScrollOnce() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themedetail_slide");
        }
        public static void onThemeChooseForOne() {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_seletcontactfortheme_success");
        }
    }
}
