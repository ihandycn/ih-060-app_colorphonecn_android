package com.honeycomb.colorphone.customize.theme;

import android.support.annotation.NonNull;

import com.honeycomb.colorphone.customize.CustomizeConfig;

public class ThemeUtils {

    public static boolean is3DOrLiveTheme(String themePackage) {
        ThemeInfo themeInfo = makeThemeInfo(themePackage);
        return themeInfo.is3D() || themeInfo.isLive();
    }

    public static @NonNull ThemeInfo makeThemeInfo(String themePackage) {
        return ThemeInfo.ofConfig(themePackage, CustomizeConfig.getMap("Themes", "OnlineDescriptions"));
    }
}
