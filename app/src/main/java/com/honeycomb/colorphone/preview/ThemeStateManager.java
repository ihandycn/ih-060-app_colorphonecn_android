package com.honeycomb.colorphone.preview;


public class ThemeStateManager {
    public static final String NOTIFY_ENJOY_MODE = "notify_enjoy_mode";
    public static final String NOTIFY_PREVIEW_MODE = "notify_preview_mode";
    public static final int ENJOY_MODE = 0;
    public static final int PREVIEW_MODE = 1;
    private static ThemeStateManager themeStateManager;
    public int themeMode = ENJOY_MODE;

    public static ThemeStateManager getInstance() {
        if (themeStateManager == null) {
            themeStateManager = new ThemeStateManager();
        }
        return themeStateManager;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }




}
