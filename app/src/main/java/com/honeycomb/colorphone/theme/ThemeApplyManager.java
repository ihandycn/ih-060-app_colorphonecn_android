package com.honeycomb.colorphone.theme;

import android.text.TextUtils;

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
        List<String> appliedThemeList = getAppliedTheme();
        if (appliedThemeList == null || appliedThemeList.isEmpty()) {
            return null;
        }

        Theme theme = null;
        for (String themeStr : appliedThemeList) {
            if (!TextUtils.isEmpty(themeStr) && themeStr.contains(String.valueOf(themeId))) {
                theme = Theme.valueOfPrefString(themeStr);
                break;
            }
        }
        return theme;
    }

    public void addAppliedTheme(String themeStr) {
        Preferences.getDefault().addStringToList(PREFS_SCREEN_FLASH_APPLiED_THEME_STRING, themeStr);
    }

    private List<String> getAppliedTheme() {
        return Preferences.getDefault().getStringList(PREFS_SCREEN_FLASH_APPLiED_THEME_STRING);
    }
}
