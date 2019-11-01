package com.honeycomb.colorphone.theme;

import android.text.TextUtils;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.Theme;
import com.superapps.util.Preferences;

import java.util.List;

public class ThemeApplyManager {

    private static final String PREFS_SCREEN_FLASH_APPLiED_THEME_STRING = "screen_flash_applied_theme_string";

    private ThemeApplyManager() {
    }

    public static ThemeApplyManager getInstance() {
        return ThemeApplyManager.ClassHolder.INSTANCE;
    }

    private static class ClassHolder {
        private static final ThemeApplyManager INSTANCE = new ThemeApplyManager();
    }

    public Theme getAppliedThemeByThemeId(int themeId) {
        String themeIdStr = String.valueOf(themeId);
        if (TextUtils.isEmpty(themeIdStr)) {
            return null;
        }

        List<String> appliedThemeList = getAppliedTheme();
        if (appliedThemeList == null || appliedThemeList.isEmpty()) {
            return null;
        }

        Theme theme = null;
        for (String themeStr : appliedThemeList) {
            if (themeIdStr.equals(getThemeId(themeStr))) {
                theme = Theme.valueOfPrefString(themeStr);
                break;
            }
        }
        return theme;
    }

    public void addAppliedTheme(String themeStr) {

        String currentThemeId = getThemeId(themeStr);
        if (TextUtils.isEmpty(currentThemeId)) {
            return;
        }

        List<String> themeList = getAppliedTheme();
        for (String theme : themeList) {
            if (currentThemeId.equals(getThemeId(theme))) {
                themeList.remove(theme);
                break;
            }
        }
        themeList.add(themeStr);
        Preferences.getDefault().putStringList(PREFS_SCREEN_FLASH_APPLiED_THEME_STRING, themeList);
    }

    private String getThemeId(String themeStr) {
        if (TextUtils.isEmpty(themeStr)) {
            return "";
        }
        String[] array = themeStr.split(Type.SEPARATOR);
        if (array.length != Theme.THEME_DEFAULT_LENGTH && array.length != Type.TYPE_DEFAULT_LENGTH) {
            return "";
        }
        return array[1];
    }

    private List<String> getAppliedTheme() {
        return Preferences.getDefault().getStringList(PREFS_SCREEN_FLASH_APPLiED_THEME_STRING);
    }
}
