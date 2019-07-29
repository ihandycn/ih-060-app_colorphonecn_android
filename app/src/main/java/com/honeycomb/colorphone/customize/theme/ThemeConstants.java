package com.honeycomb.colorphone.customize.theme;

/**
 * Constants for theme module.
 */
public class ThemeConstants {

    public static final String PREF_THEME_SET_TIME = "theme_set_time";
    public static final String PREF_AB_TEST_THEME_SET_TIME = "ab_test_theme_set_time";
    public static final String PRESET_THEME_IDENTIFIER = "com.honeycomb.launcher";
    public static final String INTENT_KEY_THEME_PACKAGE_NAME = "theme_package_name";
    public static final String INTENT_KEY_APPLY_THEME_ON_LAUNCH = "load.theme.on.launch";
    public static final String INTENT_KEY_LAUNCHED_BY_AIR_LAUNCHER = "launched_by_air_launcher";

    /**
     * For CustomizeActivity.
     */
    public static final String INTENT_KEY_FLURRY_FROM = "from";
    public static final String INTENT_KEY_TAB = "tab";
    public static final String INTENT_KEY_WALLPAPER_TAB = "wallpaper_tab";
    public static final String INTENT_KEY_WALLPAPER_TAB_ITEM_NAME = "wallpaper_tab_item_name";
    public static final String INTENT_KEY_THEME_TAB = "theme_tab";
    public static final String PREF_KEY_THEME_LAST_OPEN_TIME = "theme_last_open_time";

    /**
     * IPC.
     */
    public static final String METHOD_PROCESS_ICON = "processIcon";
    public static final String BUNDLE_KEY_BITMAP = "bitmap";
    public static final String ACTION_CUSTOMIZE_SERVICE = "com.themelab.launcher.ACTION_CUSTOMIZE_SERVICE";
    public static final String ACTION_ICON_PROCESS_SERVICE_SUFFIX = ".ACTION_ICON_PROCESS_SERVICE";
    public static final String ICON_PROCESS_PROVIDER_URI_SUFFIX = ".icon";
    public static final String BROADCAST_THEME_CHANGED = "com" + ".honeycomb.launcher.broadcast.THEME_RELOAD_FINISHED";

    public static final String WALLPAPER_SERVICE_CLASS_NAME = "com.themelab.launcher.ThemeWallpaperService";

    /**
     * Minimum version code of launcher app that this theme supports.
     */
    @Deprecated
    public static final String RES_NAME_MINIMUM_LAUNCHER_VERSION_CODE = "minimum_launcher_version_code";

    /**
     * Version code of launcher app that this theme is designed for and tested against. This is usually the
     * latest version of launcher app when this theme is launched onto market.
     */
    @Deprecated
    public static final String RES_NAME_TARGET_LAUNCHER_VERSION_CODE = "target_launcher_version_code";

    /**
     * Resource names.
     */
    public static final String RES_NAME_NAME = "theme_name";
    public static final String RES_NAME_DESC_SHORT = "theme_description_short";
    public static final String RES_NAME_DESC = "theme_description";
    public static final String RES_NAME_THUMBNAIL = "theme_thumbnail";
    public static final String RES_NAME_PREVIEW_COUNT = "theme_preview_count";
    public static final String RES_NAME_PREVIEW_PREFIX = "theme_preview_";
    public static final String RES_NAME_ICON_MAP = "icon_map";
    public static final String RES_NAME_WALLPAPER = "wallpaper";
    public static final String RES_NAME_WALLPAPER_STATIC = "wallpaper_static";
    public static final String RES_NAME_FOLDER = "folder_bg";
    public static final String RES_NAME_ICON = "icon_bg";
    public static final String RES_NAME_ICON_EFFECT = "icon_bg_effect";
    public static final String RES_NAME_FOLDER_PADDING = "folder_bg_padding";
    public static final String RES_NAME_ALL_APPS_ICON = "all_apps_icon";
    public static final String RES_NAME_APPLY_DIALOG_TOP_IMAGE = "apply_dialog_top";
    public static final String RES_NAME_ICON_THEME = "ic_theme";
    public static final String RES_NAME_ICON_THEME_DIALOG = "ic_theme_dialog";
    public static final String RES_NAME_3D_WALLPAPER_LAYER_COUNT = "wallpaper_3d_layer_count";
    public static final String RES_NAME_3D_WALLPAPER_LAYER_PREFIX = "wallpaper_3d_layer_";
    public static final String RES_NAME_IS_LIVE_THEME="is_live_theme";
    public static final String RES_NAME_SHRINK_ICON_PROCESSOR_RATIO_OVERRIDE = "shrink_icon_processor_ratio_override";

    public static final String FEATURE_NAME_FLASHLIGHT = "flashlight";
    public static final String FEATURE_NAME_UPDATE = "update";
}
