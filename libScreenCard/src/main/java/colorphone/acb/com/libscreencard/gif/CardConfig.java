package colorphone.acb.com.libscreencard.gif;

/**
 * Central list of files the Security writes to the application data directory.
 * <p>
 * To add a new Security file, create a String constant referring to the filename, and add it to
 * ALL_FILES, as shown below.
 */
public class CardConfig {
    public static final String CARD_MODULE_PREFS = "com.fasttrack.security.security.protection.prefs"; // Main process

    public static final String PREF_KEY_CONTENT_TYPE_CURSOR = "content_type_cursor";
    public static final String PREF_KEY_START_SHOW_TIME = "start_show_time";
    public static final String PREF_KEY_CONTENT_CLICKED = "content_clicked";
    public static final String PREF_KEY_GAME_SHOW_TIME = "game_show_time_mills";
    public static final String PREF_KEY_FM_GAME_SHOW_TIME = "game_fm_show_time_mills";
    //TODO use right interval
    public static final long GAME_SHOW_INTERVAL_MIN_HOUR = 6;
    public static final long GAME_FM_SHOW_INTERVAL_MIN_HOUR = 1;
    public static final String AD_GIF_INTERS = "GifInterstitial";
    public static final String AD_GIF_EXPR = "GifExpress";
}
