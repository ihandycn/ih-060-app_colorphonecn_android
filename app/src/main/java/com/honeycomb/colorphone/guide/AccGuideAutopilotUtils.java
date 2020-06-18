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
            String voiceTest = isEnable ? String.valueOf(getVoiceType()) : "False";
            String enableXiaoMi = String.valueOf(isXiaoMiEnable());
            String guideHuaWei = getGuideHuaWei();

            Analytics.logEvent("Autopilot_Voice_Test", "voice_test", voiceTest, "enable_xiaomi", enableXiaoMi, "guide_huawei", guideHuaWei);
        }, "Accessbility_Guide_Show");
        return isEnable;
    }

    private static String getGuideHuaWei() {
        String result = AutopilotConfig.getStringToTestNow(TOPIC_ID, "guide_huawei", "toast");
        HSLog.d(TAG, "guide_huawei = " + result);
        return result;
    }

    public static boolean isShowActivityGuide() {
        return "activity".equals(getGuideHuaWei());
    }

    public static boolean isXiaoMiEnable() {
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "enable_xiaomi", false);
        HSLog.d(TAG, "enable_xiaomi = " + result);
        return result;
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
