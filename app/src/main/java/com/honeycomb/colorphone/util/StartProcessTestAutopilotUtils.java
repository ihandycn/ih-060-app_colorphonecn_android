package com.honeycomb.colorphone.util;

import com.superapps.util.rom.RomUtils;

import net.appcloudbox.autopilot.AutopilotConfig;

public class StartProcessTestAutopilotUtils {
    private static final String TOPIC_ID = "Start_Process_Test";

    public static boolean shouldShowSkipOnFixAlert(){
        if (RomUtils.checkIsMiuiRom()
                || RomUtils.checkIsHuaweiRom()) {
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"skip_on_fixalert",true);
            return result;
        }else {
            return false;
        }
    }

    public static boolean shouldGuideThemeSet(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"set_theme_guide",true);
        return result;
    }

    public static boolean shouldShowPermission() {
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "user_rights", true);
        return result;
    }
}
