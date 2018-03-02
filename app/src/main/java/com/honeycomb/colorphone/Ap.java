package com.honeycomb.colorphone;

import android.text.TextUtils;
import android.widget.Toast;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;
import com.acb.utils.ToastUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;

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
                ToastUtils.showToast("【Autopilot】铃声功能：" + ringtoneBtnShowBoolean, Toast.LENGTH_SHORT);
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
                ToastUtils.showToast("【Autopilot】铃声自动播放：" + ringtoneAutoPlayBoolean, Toast.LENGTH_SHORT);
            }
            return ringtoneAutoPlayBoolean;
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题详情页展示
         */
        public static void onShow(Theme theme) {
            if (theme != null && !TextUtils.isEmpty(theme.getRingtoneUrl())) {
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
            if (theme != null && !TextUtils.isEmpty(theme.getRingtoneUrl())) {
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
}
