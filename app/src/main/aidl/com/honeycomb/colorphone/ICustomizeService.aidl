package com.honeycomb.colorphone;

interface ICustomizeService {

    // WARNING: (1) DO NOT REMOVE any method.
    //          (2) DO NOT MODIFY ORDER of any methods.
    //          (3) DO NOT CHANGE SIGNITURE of any methods (method names can be changed).
    //          (3) Add new methods ONLY TO THE VERY END of this file.
    // To stay compatible with older versions of theme packages.

    String getCurrentTheme();

    void setCurrentTheme(String theme);

    long browseMarketApp(String packageName);

    String getDefaultSharedPreferenceString(String key, String defaultValue);

    void preChangeWallpaperFromLauncher();

    void putDefaultSharedPreferenceString(String key, String value);

    void notifyWallpaperFeatureUsed();

    void notifyWallpaperSetEvent();

    List getOnlineWallpaperConfig();

    Map getOnlineThemeConfig();

    void logWallpaperEvent(String action, String label);

    void killWallpaperProcess();

    void notifyWallpaperPackageClicked();
}
