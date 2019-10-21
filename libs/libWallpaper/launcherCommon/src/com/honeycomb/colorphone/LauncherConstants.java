package com.honeycomb.colorphone;

import android.content.Context;

import com.ihs.commons.config.HSConfig;

public class LauncherConstants {

    /**
     * Note that this constant should only be used as preset theme identifier. Use {@link Context#getPackageName()}
     * if you need package name because other launcher build variants (eg. launcherSP) has different package names.
     */
    public static final String LAUNCHER_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcher");

    public static final String LAUNCHER_SP_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherSP");

    public static final String LAUNCHER_CHOCOLATE_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherChocolate");

    public static final String LAUNCHER_EMOJI_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherEmoji");

    public static final String LAUNCHER_LIVE_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherLive");

    public static final String LAUNCHER_POWERFUL_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherPowerful");

    public static final String LAUNCHER_SECURITY_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherSecurity");

    public static final String LAUNCHER_FLASH_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherFlash");

    public static final String LAUNCHER_ZMOJI_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherZmoji");

    public static final String LAUNCHER_WALLPAPER_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherWallpaper");

    public static final String LAUNCHER_HOROSCOPE_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherHoroscope");

    public static final String LAUNCHER_LOLLIPOP_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherLollipop");

    public static final String LAUNCHER_COOKIE_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherCookie");

    public static final String LAUNCHER_JELLY_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherJelly");

    public static final String LAUNCHER_NUT_PACKAGE_NAME =
            HSConfig.optString("", "Application", "Launchers", "launcherNut");

    public static final String[] LAUNCHERS = {
            LAUNCHER_PACKAGE_NAME,
            LAUNCHER_SP_PACKAGE_NAME,
            LAUNCHER_CHOCOLATE_PACKAGE_NAME,
            LAUNCHER_EMOJI_PACKAGE_NAME,
            LAUNCHER_LIVE_PACKAGE_NAME,
            LAUNCHER_POWERFUL_PACKAGE_NAME,
            LAUNCHER_SECURITY_PACKAGE_NAME,
            LAUNCHER_FLASH_PACKAGE_NAME,
            LAUNCHER_ZMOJI_PACKAGE_NAME,
            LAUNCHER_WALLPAPER_PACKAGE_NAME,
            LAUNCHER_HOROSCOPE_PACKAGE_NAME,
            LAUNCHER_LOLLIPOP_PACKAGE_NAME,
            LAUNCHER_COOKIE_PACKAGE_NAME,
            LAUNCHER_JELLY_PACKAGE_NAME,
            LAUNCHER_NUT_PACKAGE_NAME,
    };

    public static final String BUILD_VARIANT_DEFAULT = "launcher";
    public static final String BUILD_VARIANT_SP = "launcherSP";
    public static final String BUILD_VARIANT_CHOCOLATE = "launcherChocolate";
    public static final String BUILD_VARIANT_EMOJI = "launcherEmoji";
    public static final String BUILD_VARIANT_LIVE = "launcherLive";
    public static final String BUILD_VARIANT_POWERFUL = "launcherPowerful";
    public static final String BUILD_VARIANT_SECURITY = "launcherSecurity";
    public static final String BUILD_VARIANT_FLASH = "launcherFlash";
    public static final String BUILD_VARIANT_ZMOJI = "launcherZmoji";
    public static final String BUILD_VARIANT_WALLPAPER = "launcherWallpaper";
    public static final String BUILD_VARIANT_HOROSCOPE = "launcherHoroscope";
    public static final String BUILD_VARIANT_LOLLIPOP = "launcherLollipop";
    public static final String BUILD_VARIANT_COOKIE = "launcherCookie";
    public static final String BUILD_VARIANT_JELLY = "launcherJelly";
    public static final String BUILD_VARIANT_NUT = "launcherNut";

    public static final String CUSTOM_FEATURE_PREFIX = "feature://";

    /**
     * Use Utils#getAppInstallTimeMillis() to read the value.
     */
    public static final String PREF_KEY_INSTALLED_TIME = "pref_key_installed_time";

    public static final String NOTIFICATION_TIP_DISMISS = "tip_dismiss";
}
