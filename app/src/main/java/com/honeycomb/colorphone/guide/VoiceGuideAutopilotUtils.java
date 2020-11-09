package com.honeycomb.colorphone.guide;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class VoiceGuideAutopilotUtils {

    private static final String TAG = VoiceGuideAutopilotUtils.class.getSimpleName();

    private static final String TOPIC_ID = "topic-7prjndmw7";

    public static boolean isEnable() {
        final boolean isEnable = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "enable", false);
        HSLog.d(TAG, "isEnable = " + isEnable);
        Preferences.getDefault().doOnce(() ->
                        Analytics.logEvent("Autopilot_test_ringtone", false, "Enable", isEnable + "")
                , "Voice_Guide_Show");
        return isEnable;
    }

    public static boolean isRefreshEnable() {
        isEnable();
        final boolean isRefresh = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "refreshenable", false);
        HSLog.d(TAG, "isRefresh = " + isRefresh);
        return isRefresh;
    }

    public static void logVoiceGuideStart() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "voice_guide_start");
    }

    public static void logVoiceGuideEnd() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "voice_guide_end");
    }

    public static void logCableFeed1AdChance() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "cablefeed1_ad_chance");
    }

    public static void logCableFeed1AdShow() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "cablefeed1_ad_show");
    }

    public static void logAirNewsFeedAdChance() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "airnewsfeedad_chance");
    }

    public static void logAirNewsFeedAdShow() {
        isEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "airnewsfeedad_show");
    }
}
