package colorphone.acb.com.libweather.model;

import com.ihs.app.framework.HSApplication;

/**
 * Central list of files the Launcher writes to the application data directory.
 * <p>
 * To add a new Launcher file, create a String constant referring to the filename, and add it to
 * ALL_FILES, as shown below.
 */
public class LauncherFiles {

//    private static final boolean SHOULD_USE_OLD_FILE_NAMES =
//            LauncherApplication.isLauncherFlashVariant()
//                    || LauncherApplication.isLauncherLiveVariant();

    // TODO: 2019/4/3 无法获取到LauncherApplication.isLauncherFlashVariant()
    private static final boolean SHOULD_USE_OLD_FILE_NAMES = false;

    private static final String OLD_PREFIX = "com" + ".honeycomb.launcher"; // Magic, don't touch
    private static final String NEW_PREFIX = HSApplication.getContext().getPackageName();

    private static final String PREFIX = (SHOULD_USE_OLD_FILE_NAMES ? OLD_PREFIX : NEW_PREFIX);

    /**
     * File name format for new files: "com.honeycomb.launcher_module".
     */
    public static final String DESKTOP_PREFS = PREFIX + "_desktop"; // Main process
    public static final String PERFORMANCE_PREFS = PREFIX + "_evaluation"; // Main process
    public static final String NEWS_PREFS = PREFIX + "_news"; // Main process
    public static final String SEARCH_PREFS = PREFIX + ".search.prefs"; // Main process
    public static final String SEARCH_TRENDING_WORDS_PREFS = PREFIX + ".search.trending.words.prefs"; // Main process
    public static final String LUCKY_PREFS = PREFIX + ".lucky.prefs"; // Main process
    public static final String MENU_PREFS = PREFIX + ".desktop.menu.prefs"; // Main process
    public static final String CUSTOMIZE_PREFS = PREFIX + ".customize.prefs"; // Process ":customize"
    public static final String BOOST_PREFS = PREFIX + "_boost"; // Main process
    public static final String MOMENT_PREFS = PREFIX + "_moment"; // Main process
    public static final String BATTERY_PREFS = PREFIX + ".battery.prefs"; // Main process
    public static final String LOCKER_PREFS = PREFIX + ".locker.prefs"; // Main process
    public static final String JUNK_CLEAN_PREFS = PREFIX + ".junk.clean.prefs"; // Process ":clean"
    public static final String CPU_COOLER_PREFS = PREFIX + ".cpu.cooler.prefs"; // Main process
    public static final String NOTIFICATION_PREFS = PREFIX + ".notification.prefs"; // Main process
    public static final String NOTIFICATION_CLEANER_PREFS = PREFIX + ".notification.cleaner.prefs"; // Main process
    public static final String WEATHER_PREFS = PREFIX + "_weather"; // Main process
    public static final String EMOJI_PREFS = PREFIX + "_emoji"; // Process "emoji"
    public static final String APP_LOCK_PREFS = PREFIX + ".applock.prefs"; // Main process
    public static final String APP_PROMOTE_PREFS = PREFIX + ".locker.promote.prefs"; // Main process
    public static final String COMMON_PREFS = PREFIX + ".common.prefs"; // One file for each process
    public static final String WELCOME_PREFS = PREFIX + "_welcome"; // Main process
    public static final String RECORD_USE_TIME_PREFS = PREFIX + ".record.use.time.prefs"; // One file for record use time
    public static final String ANTIVIRUS_PRES = PREFIX + ".antivirus.prefs"; // Main process
    public static final String SECURITY_PROTECTION_PREFS = PREFIX + ".security.protection.prefs"; // Main process
    public static final String SCHEDULE_MANAGER_PREFS = PREFIX + ".worker.manager.prefs";
    public static final String HOROSCOPE_PREFS = PREFIX + "_horoscope";

    /**
     * File name format for new files: "module_name.db".
     */
    public static final String LAUNCHER_DB = "launcher.db";
    public static final String APP_ICONS_DB = "app_icons.db";
    public static final String WALLPAPER_DB = "wallpaper.db";
    public static final String WIDGET_PREVIEWS_DB = "widget_previews.db";
    public static final String WEATHER_DB = "weather.db";
    public static final String NEWS_DB = "news.db";
    public static final String SEARCH_DB = "search.db";

}
