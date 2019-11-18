package com.honeycomb.colorphone.util;

import net.appcloudbox.autopilot.AutopilotConfig;

public class StartProcessTestAutopilotUtils {
    private static final String TOPIC_ID = "Start_Process_Test";

    public static boolean shouldShowSkipOnFixAlert(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"SkipOnFixAlert",true);
        return result;
    }

    public static boolean shouldGuideThemeSet(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"Set_Theme_Guide",true);
        return result;
    }

    public static boolean shouldShowPermission() {
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "Set_Theme_Guide", true);
        return result;
    }
}
