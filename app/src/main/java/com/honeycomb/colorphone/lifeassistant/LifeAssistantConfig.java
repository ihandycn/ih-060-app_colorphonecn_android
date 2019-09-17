package com.honeycomb.colorphone.lifeassistant;

import android.text.format.DateUtils;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superappscommon.util.Strings;

import java.util.List;
import java.util.Random;

public class LifeAssistantConfig {
    private static final String PREF_KEY_LIFE_ASSISTANT_SETTING_ENABLE = "life_assistant_setting_enable";
    private static final String PREF_KEY_LIFE_ASSISTANT_MORNING_STRING_UNUSED = "life_assistant_morning_string_unused";
    private static final String PREF_KEY_LIFE_ASSISTANT_NIGHT_STRING_UNUSED = "life_assistant_night_string_unused";

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

    public static String getWelcomeStr(boolean morning) {
        List<String> welcomeStrings = (List<String>) HSConfig.getList("Application", "LifeAssistant", "Text", morning ? "GoodMorning" : "GoodEvening");
        String prefKey = morning ? PREF_KEY_LIFE_ASSISTANT_MORNING_STRING_UNUSED : PREF_KEY_LIFE_ASSISTANT_NIGHT_STRING_UNUSED;
        String usedString = Preferences.get(Constants.DESKTOP_PREFS).getString(prefKey, "");
        List<String> unusedIndexes = Strings.csvToStringList(usedString);

        HSLog.d(NewsManager.TAG, "getWelcomeStr wSize: " + welcomeStrings.size() + "   unSize: " + unusedIndexes.size() + "   usedString: " + usedString);

        if (unusedIndexes.size() == 0) {
            for (int i = 0; i < welcomeStrings.size(); i++) {
                unusedIndexes.add(String.valueOf(i));
            }
        }

        Random random = new Random();
        int index = Integer.valueOf(unusedIndexes.remove(random.nextInt(unusedIndexes.size())));
        Preferences.get(Constants.DESKTOP_PREFS).putString(prefKey, Strings.stringListToCsv(unusedIndexes));

        return welcomeStrings.get(index);
    }
}
