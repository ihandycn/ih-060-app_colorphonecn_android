package com.honeycomb.colorphone.util;

import android.os.Build;

import com.superapps.util.rom.RomUtils;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class StartProcessTestAutopilotUtils {
    private static final String TOPIC_ID = "topic-7c00my1zs";

    public static boolean shouldShowSkipOnFixAlert() {
        if (RomUtils.checkIsMiuiRom()
                || RomUtils.checkIsHuaweiRom()) {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "skip_on_fixalert", true);
        } else {
            return false;
        }
    }

    public static boolean shouldGuideThemeSet() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "set_theme_guide", true);
    }

    public static boolean shouldShowPermission() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "user_rights", true);
    }

    public static void logEventWithSdkVersion(String event) {
        event = event.toLowerCase();
        String sdkString;
        switch (Build.VERSION.SDK_INT) {
            case 24:
            case 25:
                sdkString = "7";
                break;
            case 26:
            case 27:
                sdkString = "8";
                break;
            case 28:
                sdkString = "9";
                break;
            default:
                return;
        }
        AutopilotEvent.logTopicEvent(TOPIC_ID, event + "_" + sdkString);
    }
}
