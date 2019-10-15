package com.acb.libwallpaper.live.customize.wallpaper;

import com.acb.libwallpaper.BuildConfig;

@SuppressWarnings("ALL")
class DebugMode {
    static boolean IS_DEBUG = false;
    static boolean DRAW_DEBUG = true && BuildConfig.DEBUG && IS_DEBUG;
    static boolean HELPER_DEBUG = false && BuildConfig.DEBUG && IS_DEBUG;
    static boolean MANAGER_PROXY_DEBUG = true && BuildConfig.DEBUG && IS_DEBUG;
    static boolean BACKGROUND_DEBUG = true && BuildConfig.DEBUG && IS_DEBUG;
    static boolean WALLPAPER_SCROLL_DEBUG = true && BuildConfig.DEBUG;
}
