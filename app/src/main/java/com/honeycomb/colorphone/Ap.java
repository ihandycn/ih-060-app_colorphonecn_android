package com.honeycomb.colorphone;

import android.widget.Toast;

import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;
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
                ringtoneBtnShowBoolean = true;
            }
            if (BuildConfig.DEBUG) {
                Toast.makeText(HSApplication.getContext(), "【Autopilot】铃声功能：" + ringtoneBtnShowBoolean, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(HSApplication.getContext(), "【Autopilot】铃声自动播放：" + ringtoneAutoPlayBoolean, Toast.LENGTH_SHORT).show();
            }
            return ringtoneAutoPlayBoolean;
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题详情页展示
         */
        public static void onShow(Theme theme) {
            String formatString = String.format(Locale.ENGLISH, "theme_%s_detail_page_show", theme.getIdName());
            AutopilotEvent.logTopicEvent("topic-1516083421924-90", formatString);
        }

        /**
         * 上传日志: topic-1516083421924-90 - 主题在详情页被应用
         */
        public static void onApply(Theme theme) {
            String formatString = String.format(Locale.ENGLISH, "theme_%s_detail_page_apply", theme.getIdName());
            AutopilotEvent.logTopicEvent("topic-1516083421924-90", formatString);
        }
    }
}
