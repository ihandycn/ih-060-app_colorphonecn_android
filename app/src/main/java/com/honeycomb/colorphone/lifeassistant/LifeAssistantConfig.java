package com.honeycomb.colorphone.lifeassistant;

import android.text.format.DateUtils;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superappscommon.util.Strings;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class LifeAssistantConfig {
    private static final String PREF_KEY_LIFE_ASSISTANT_SETTING_ENABLE = "life_assistant_setting_enable";
    private static final String PREF_KEY_LIFE_ASSISTANT_MORNING_STRING_UNUSED = "life_assistant_morning_string_unused";
    private static final String PREF_KEY_LIFE_ASSISTANT_NIGHT_STRING_UNUSED = "life_assistant_night_string_unused";
    private static final String PREF_KEY_LIFE_ASSISTANT_MORNING_SHOWN_TIME = "life_assistant_morning_shown_time";
    private static final String PREF_KEY_LIFE_ASSISTANT_MORNING_CHECK_TIME = "life_assistant_morning_check_time";
    private static final String PREF_KEY_LIFE_ASSISTANT_NIGHT_SHOWN_TIME = "life_assistant_night_shown_time";

    public static boolean canShowLifeAssistant() {
        if (isLifeAssistantConfigEnable() && isLifeAssistantSettingEnable()) {
            long installTime = Utils.getAppInstallTimeMillis();
            boolean timeInterval = (System.currentTimeMillis() - installTime) > getActiveAfterInstall() * DateUtils.MINUTE_IN_MILLIS;
            if (timeInterval) {
                return isShowHour();
            }

        }
        return false;
    }

    private static boolean isShowHour() {
        if (BuildConfig.DEBUG)
            return true;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 9) {
            int lastShow = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_LIFE_ASSISTANT_MORNING_SHOWN_TIME, 0);
            int showDate = lastShow / 100;
            int showCount = lastShow % 100;

            int lastCheck = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_LIFE_ASSISTANT_MORNING_CHECK_TIME, 0);
            int checkDate = lastCheck / 100;
            int checkCount = lastCheck % 100;

            int todayInt = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);

            if ((showDate != todayInt || showCount < 1) && (checkCount > 1)) {
                return true;
            }
        }

        if (hour >= 17 && hour < 23) {
            int lastShowDate = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_LIFE_ASSISTANT_NIGHT_SHOWN_TIME, 0);
            int showDate = lastShowDate / 100;
            int todayInt = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
            return todayInt != showDate;
        }

        return false;
    }

    public static void recordLifeAssistantCheck() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 9) {
            int lastCheck = Preferences.get(Constants.DESKTOP_PREFS).getInt(PREF_KEY_LIFE_ASSISTANT_MORNING_CHECK_TIME, 0);
            int checkDate = lastCheck / 100;

            int todayInt = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);

            if (checkDate == todayInt) {
                Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_LIFE_ASSISTANT_MORNING_CHECK_TIME);
            } else {
                Preferences.get(Constants.DESKTOP_PREFS).putInt(PREF_KEY_LIFE_ASSISTANT_MORNING_CHECK_TIME, todayInt * 100 + 1);
            }
        }

    }

    public static void recordLifeAssistantShow() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String prefKey;
        if (hour >= 5 && hour <= 9) {
            prefKey = PREF_KEY_LIFE_ASSISTANT_MORNING_SHOWN_TIME;
        } else if (hour >= 17 && hour <= 23) {
            prefKey = PREF_KEY_LIFE_ASSISTANT_NIGHT_SHOWN_TIME;
        } else {
            return;
        }

        int lastShow = Preferences.get(Constants.DESKTOP_PREFS).getInt(prefKey, 0);
        int showDate = lastShow / 100;
        int showCount = lastShow % 100;

        int todayInt = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);

        if (todayInt == showDate) {
            showCount++;
        } else {
            showCount = 1;
        }
        int shown = todayInt * 100 + showCount;

        Preferences.get(Constants.DESKTOP_PREFS).putInt(prefKey, shown);
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
