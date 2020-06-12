package com.honeycomb.colorphone.guide;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AccGuideAutopilotUtils {

    private static final String TAG = "AccGuideAutopilotUtils";

    private static final String TOPIC_ID = "topic-7jwfh16wh";

    public static boolean isEnable() {
        final boolean isEnable = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "enable", false);
        HSLog.d(TAG, "isEnable = " + isEnable);
        Preferences.getDefault().doOnce(() -> {
            if (isEnable) {
                int type = getVoiceType();
                Analytics.logEvent("Accessbility_Guide_Show", "voice_test", String.valueOf(type));
            } else {
                Analytics.logEvent("Accessbility_Guide_Show", "voice_test", "False");
            }
        }, "Accessbility_Guide_Show");
        return isEnable;
    }

    public static int getVoiceType() {
        int type = (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "type", 1);
        HSLog.d(TAG, "type = " + type);
        return type;
    }

    public static void logStartGuideShow() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "startguide_show");
    }

    public static void logAccGuideShow() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "acc_guide_show");
    }

    public static void logAccGuideBtnClick() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "acc_guide_btn_click");
    }

    public static void logAccGranted() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "acc_granted");
    }

    public static void logCongratulationPageShown() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "congratulation_page_shown");
    }

    public static void logVoiceGuideStart() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "voice_guide_start");
    }

    public static void logVoiceGuideEnd() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "voice_guide_end");
    }

}
