package com.acb.libwallpaper.live.livewallpaper;


import com.acb.libwallpaper.BuildConfig;

import java.io.File;

public class LiveWallpaperConsts {

    @SuppressWarnings({"PointlessBooleanExpression"})
    public static final boolean DEBUG_PARTICLE_FLOW_WALLPAPER = false && BuildConfig.DEBUG;

    public static final int TYPE_SHADER_AND_CONFETTI = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_PARTICLE_FLOW = 2;

    public static final int NUMBER_OF_SERVICES = 4;

    public static final String DIRECTORY = "livewallpapers/";

    static final String LIVE_WALLPAPER_BACKGROUND_DIRECTORY = "shader-textures";
    static final String LIVE_WALLPAPER_CONFETTI_DIRECTORY = "particle-images";
    public static final String LIVE_DIRECTORY = "wallpapers" + File.separator + "live";

    public static final String PREF_KEY_IS_PREVIEW_MODE = "live_wallpaper_is_preview_mode";
    public static final String PREF_KEY_WALLPAPER_NAME = "live_wallpaper_name";
    public static final String PREF_KEY_WALLPAPER_SET_TIME_PREFIX = "live_wallpaper_set_time";
    public static final String PREF_KEY_PREVIEW_WALLPAPER_NAME = "preview_live_wallpaper_name";
    public static final String PREF_KEY_SERVICE_INDEX = "live_wallpaper_htc_service_index";
    public static final String PREF_KEY_PREVIOUS_WALLPAPER_TYPE = "live_wallpaper_previous_wallpaper_type";

    public static final String PREF_KEY_WALLPAPER_PREVIEW_TYPE = "live_wallpaper_preview_type";

    public static final String PREF_KEY_TYPE_3D = "live_wallpaper_type_3d";
    public static final String PREF_KEY_TYPE_LIVE_TOUCH = "live_wallpaper_type_touch";
    public static final String PREF_KEY_TYPE_NORMAL = "live_wallpaper_type_normal";

    public static final long TOUCH = 0x0001L;
    public static final long BACKGROUND = 0x0010L;
    public static final long CLICK = 0x0100L;
    public static final long COMMON = TOUCH | BACKGROUND;
    public static final String GUIDE_WINDOW_TAG = "wallpaper_guide";

    public static class Files {
        public static final String LOCAL_DIRECTORY = "wallpapers" + File.separator + "local";
        public static final String LUCKY_DIRECTORY = "wallpapers" + File.separator + "lucky";
        public static final String LIVE_DIRECTORY = "wallpapers" + File.separator + "live";
        /**
         * Key prefix that should be joined with a category identifier (eg. display_list_cartoon).
         * <p/>
         * Value: serialized data containing wallpapers in order that is displayed in a
         */
        public static final String DISPLAY_LIST_PREFIX = "display_list_";
        /**
         * Key prefix that should be joined with a category identifier (eg. shuffled_list_cartoon).
         * <p/>
         * Value: serialized data containing wallpapers in a shuffled order that is used to generate
         * "latest wallpapers" content.
         */
        public static final String SHUFFLED_LIST_PREFIX = "shuffled_list_";
        public static final String LOCAL_WALLPAPER_THUMB_SUFFIX = "-thumb";
        private static final String DIRECTORY = "wallpapers";
    }
}
