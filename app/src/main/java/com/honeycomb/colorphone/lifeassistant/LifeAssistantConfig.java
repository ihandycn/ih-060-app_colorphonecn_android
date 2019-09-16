package com.honeycomb.colorphone.lifeassistant;

import android.text.format.DateUtils;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

public class LifeAssistantConfig {
    private static final String PREF_KEY_LIFE_ASSISTANT_SETTING_ENABLE = "life_assistant_setting_enable";

    public static boolean canShowLifeAssistant() {
        if (isLifeAssistantConfigEnable() && isLifeAssistantSettingEnable()) {
            long installTime = Utils.getAppInstallTimeMillis();
            return (System.currentTimeMillis() - installTime) > getActiveAfterInstall() * DateUtils.MINUTE_IN_MILLIS;
        }
        return false;
    }

    public static boolean isLifeAssistantConfigEnable() {
        return HSConfig.optBoolean(true, "Application", "LifeAssistant", "Enable");
    }

    public static boolean isLifeAssistantSettingEnable() {
        return Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_LIFE_ASSISTANT_SETTING_ENABLE, true);
    }

    public static void setLifeAssistantSettingEnable(boolean enable) {
        Preferences.get(Constants.DESKTOP_PREFS).putBoolean(PREF_KEY_LIFE_ASSISTANT_SETTING_ENABLE, enable);
    }

    public static boolean isLifeAssistantAdEnable() {
        return HSConfig.optBoolean(true, "Application", "LifeAssistant", "NewsAdEnable");
    }

    public static int getActiveAfterInstall() {
        return HSConfig.optInteger(360, "Application", "LifeAssistant", "ActiveAfterInstallMinutes");
    }
}
