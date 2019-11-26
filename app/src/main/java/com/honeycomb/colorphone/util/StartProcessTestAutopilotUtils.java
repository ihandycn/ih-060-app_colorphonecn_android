package com.honeycomb.colorphone.util;

import com.superapps.util.rom.RomUtils;

import net.appcloudbox.autopilot.AutopilotConfig;

public class StartProcessTestAutopilotUtils {
    private static final String TOPIC_ID = "topic-7c00my1zs";

    public static boolean shouldShowSkipOnFixAlert(){
        if (RomUtils.checkIsMiuiRom()
                || RomUtils.checkIsHuaweiRom()) {
            return AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"skip_on_fixalert",true);
        }else {
            return false;
        }
    }

    public static boolean shouldGuideThemeSet(){
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID,"set_theme_guide",true);
    }

    public static boolean shouldShowPermission() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "user_rights", true);
    }
}
