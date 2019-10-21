package com.honeycomb.colorphone.wallpaper.ad;

import com.honeycomb.colorphone.LauncherConstants;
 import com.honeycomb.colorphone.BuildConfig;

/**
 * Constants for all ad placements.
 */
public class AdPlacements {

    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_RESULT_PAGE = "SevenInOneAds_Clicked_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_RESULT_PAGE = "SevenInOneAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_THREE_IN_ONE = "ThreeInOneAds_Clicked_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_WALLPAPER_THEME = "WallpaperThemeAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_WALLPAPER_THEME = "WallpaperThemeAds_Clicked_In_App";

    //Old Users keep after upgrading. New Users are no longer in use. New launchers no longer have these ad placements.
    public static String CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "Charging";
    public static String LOCKER_EXPRESS_AD_PLACEMENT_NAME = "LockScreen";
    public static String DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "Widget";
    public static String EVENT_NAME_LUCKY = "Lucky";

    //Interstitial Ad placements
    public static String EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "Weel";
    public static final String RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME = "ResultPageInterstitial";
    public static final String SCREEN_GREETING_INTERSTITIAL_AD_PLACEMENT_NAME = "Greeting";
    public static final String GAME_CENTER_INTERSTITIAL_AD_PLACEMENT_NAME = "Game";
    public static final String DESKTOP_TIPS_INTERSTITIAL_AD_PLACEMENT_NAME = "ExitPageCardInterstitial";
    public static final String AD_PLACEMENT_NANE_LOCKER_GIF_INTERSTITIAL = "LockerGifInterstitial";
    public static final String HOROSCOPE_MAIN_AD_PLACEMENT_NAME = "Horoscope";
    public static final String CASH_CENTER_INTERSTITIAL_AD_PLACEMENT_NAME = "Cash";

    //Express Ad placements
    public static String APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLock";
    public static String CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReport";
    public static final String HUB_EXPRESS_AD_PLACEMENT_NAME = "SixInOne";
    public static final String HUB_EXPRESS_NEW_AD_PLACEMENT_NAME = "SixInOneNew";
    public static final String MESSAGE_FLOATBALL_AD_PLACEMENT_NAME = "MessageBox";
    public static final String AD_PLACEMENT_NAME_LOCKER_GIF_EXPRESS = "LockerGifExpress";

    //Native Ad placements
    public static String FOLDER_ALL_APPS_NATIVE_AD_PLACEMENT_NAME = "AppDrawerFolderPlus"; //Facebook 4.99.1 Need A New Adplacement
    public static String APP_MANAGER_NATIVE_AD_PLACEMENT_NAME = "AppManagerPlus"; //Facebook 4.99.1 Need A New Adplacement
    public static String RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "SevenInOne";
    public static String WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "Wallpaper";

    //Reward Ad placements
    public static String GAME_CENTER_REWARD_AD_PLACEMENT_NAME = "Reward";

    public static String[] MAIN_PROCESS_NATIVE_PLACEMENTS;
    public static String[] MAIN_PROCESS_EXPRESS_PLACEMENTS;

    public static void init(){
        switch (BuildConfig.FLAVOR) {
            case LauncherConstants.BUILD_VARIANT_DEFAULT:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "500_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "500_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_SP:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "HomePlus_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "HomePlus_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "HomePlus_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "HomePlus_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "HomePlus_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_EMOJI:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "EmojiPhone_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "EmojiPhone_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "EmojiPhone_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "EmojiPhone_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "EmojiPhone_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_LIVE:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "LiveWallpaper_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "LiveWallpaper_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "LiveWallpaper_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "LiveWallpaper_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "LiveWallpaper_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_POWERFUL:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)SevenInOne";
                FOLDER_ALL_APPS_NATIVE_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)AppDrawerFolder";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "3dTheme_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "3dTheme_A(InterstitialAds)Weel";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_SECURITY:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)SevenInOne";
                FOLDER_ALL_APPS_NATIVE_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)AppDrawerFolder";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "FastPhone_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "FastPhone_A(InterstitialAds)Weel";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_FLASH:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "FlashScreen_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "FlashScreen_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "FlashScreen_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "FlashScreen_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "FlashScreen_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_ZMOJI:
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME = "Stickers_A(NativeAds)Charging";
                LOCKER_EXPRESS_AD_PLACEMENT_NAME = "Stickers_A(NativeAds)LockScreen";
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME = "Stickers_A(NativeAds)SevenInOne";
                WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "Stickers_A(NativeAds)Wallpaper";
                DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME = "Stickers_A(NativeAds)Widget";
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPus";
                break;
            case LauncherConstants.BUILD_VARIANT_WALLPAPER:
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPlus";
                GAME_CENTER_REWARD_AD_PLACEMENT_NAME = "RewardPlus";
                break;
            case LauncherConstants.BUILD_VARIANT_HOROSCOPE:
                EXIT_INTERSTITIAL_AD_PLACEMENT_NAME = "WeelFB";
                APP_LOCK_EXPRESS_AD_PLACEMENT_NAME = "AppLockPlus";
                CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME = "ChargingReportPus";
                break;
            case LauncherConstants.BUILD_VARIANT_NUT:
            case LauncherConstants.BUILD_VARIANT_COOKIE:
            case LauncherConstants.BUILD_VARIANT_LOLLIPOP:
            case LauncherConstants.BUILD_VARIANT_JELLY:
            case LauncherConstants.BUILD_VARIANT_CHOCOLATE:
                break;
            default:
                throw new IllegalStateException("launcher " + BuildConfig.FLAVOR + " not found!!!");
        }

        MAIN_PROCESS_EXPRESS_PLACEMENTS = new String[]{
                CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME,
                LOCKER_EXPRESS_AD_PLACEMENT_NAME,
        };

        MAIN_PROCESS_NATIVE_PLACEMENTS = new String[]{
                RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME,
                FOLDER_ALL_APPS_NATIVE_AD_PLACEMENT_NAME,
                APP_MANAGER_NATIVE_AD_PLACEMENT_NAME,
        };
    }
}
