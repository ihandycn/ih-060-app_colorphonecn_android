package com.honeycomb.colorphone.resultpage.data;

public class ResultConstants {

    public static final String PREF_KEY_RESULT_PAGE_SHOWN_COUNT = "PREF_KEY_RESULT_PAGE_SHOWN_COUNT";

    public static final int RESULT_TYPE_BOOST_PLUS = 0;
//    public static final int RESULT_TYPE_BATTERY = 1;
//    public static final int RESULT_TYPE_JUNK_CLEAN = 2;
//    public static final int RESULT_TYPE_CPU_COOLER = 3;
//    public static final int RESULT_TYPE_NOTIFICATION_CLEANER = 4;
    public static final int RESULT_TYPE_BOOST_TOOLBAR = 5;
//    public static final int RESULT_TYPE_VIRUS_SCAN = 6;

    public static final int CARD_VIEW_TYPE_INVALID = 0;

    // Feature promotions
    public static final int CARD_VIEW_TYPE_BATTERY = 1;
    public static final int CARD_VIEW_TYPE_BOOST_PLUS = 2;
    public static final int CARD_VIEW_TYPE_JUNK_CLEANER = 3;
    public static final int CARD_VIEW_TYPE_CPU_COOLER = 4;

    // Urgent Feature promotions
    public static final int CARD_VIEW_TYPE_BATTERY_URGENT = 11;
    public static final int CARD_VIEW_TYPE_BOOST_PLUS_URGENT = 12;
    public static final int CARD_VIEW_TYPE_JUNK_CLEANER_URGENT = 13;
    public static final int CARD_VIEW_TYPE_CPU_COOLER_URGENT = 14;

    // Cross-app promotions
    public static final int CARD_VIEW_TYPE_SECURITY = 5;
    public static final int CARD_VIEW_TYPE_MAX_GAME_BOOSTER = 6;
    public static final int CARD_VIEW_TYPE_MAX_APP_LOCKER = 7;
    public static final int CARD_VIEW_TYPE_MAX_DATA_THIEVES = 8;

    // new function guide cards
    public static final int CARD_VIEW_TYPE_GUIDE_BP = 20;
    public static final int CARD_VIEW_TYPE_GUIDE_NC = 21;
    public static final int CARD_VIEW_TYPE_GUIDE_APPLOCK_1 = 22;
    public static final int CARD_VIEW_TYPE_GUIDE_APPLOCK_2 = 23;
    public static final int CARD_VIEW_TYPE_GUIDE_APPLOCK_PERMISSION = 24;

    // ad card
    public static final int CARD_VIEW_TYPE_AD = 90;

    public static final String PREF_KEY_GUIDE_CARD_SHOW_INDEX = "pref_key_guide_card_show_index";

    // Fallbacks when no promotion card applies
    public static final int CARD_VIEW_TYPE_DEFAULT = 10;

    public static final String CHARGING_SCREEN_FULL = "ChargingScreen_Full";
    public static final String NOTIFICATION_CLEANER = "NotificationCleaner";
    public static final String NOTIFICATION_CLEANER_FULL = "NotificationCleaner_Full";
    public static final String AD = "AD";
    public static final String APPLOCK = "APPLOCK";
    public static final String BATTERY = "Battery";
    public static final String BOOST_PLUS = "BoostPlus";
    public static final String BOOST_TOOLBAR = "Boost";
    public static final String JUNK_CLEANER = "JunkCleaner";
    public static final String UNREAD_MESSGAE = "UnreadMessage";
    public static final String WHATS_APP = "WhatsApp";
    public static final String CPU_COOLER = "CPUCooler";
    public static final String VIRUS_SCAN = "VirusScan";
    public static final String FILE_SCAN = "FileScan";
    public static final String DEFAULT = "DefaultCard";
    public static final String GUIDE_APPLOCK_PERMISSION = "Guide_Applock_Permission";
    public static final String GUIDE_BP = "Guide_BP";
    public static final String GUIDE_NC = "Guide_NC";
    public static final String GUIDE_APPLOCK_1 = "Guide_Applock_1";
    public static final String GUIDE_APPLOCK_2 = "Guide_Applock_2";

    public static final String PREF_KEY_LAST_BATTERY_USED_TIME = "last_battery_used_time";
    public static final String PREF_KEY_LAST_BOOST_PLUS_USED_TIME = "last_boost_plus_used_time";
    public static final String PREF_KEY_LAST_JUNK_CLEAN_USED_TIME = "last_junk_clean_used_time";
    public static final String PREF_KEY_LAST_CPU_COOLER_USED_TIME = "last_cpu_cooler_used_time";
    public static final String PREF_KEY_LAST_NOTIFICATION_CLEANER_USED_TIME = "last_notification_cleaner_used_time";

    public static final String PREF_KEY_GUIDE_BP_SHOW_COUNT = "PREF_KEY_GUIDE_BP_SHOW_COUNT";
    public static final String PREF_KEY_GUIDE_NC_SHOW_COUNT = "PREF_KEY_GUIDE_NC_SHOW_COUNT";
    public static final String PREF_KEY_GUIDE_APP_LOCK_2_SHOW_COUNT = "PREF_KEY_GUIDE_APP_LOCK_2_SHOW_COUNT";
    public static final String PREF_KEY_GUIDE_APP_LOCK_PERMISSION_SHOW_COUNT = "PREF_KEY_GUIDE_APP_LOCK_PERMISSION_SHOW_COUNT";

    public static final String PREF_KEY_INTO_BATTERY_PROTECTION_COUNT = "into_battery_protection_count";
    public static final String PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT = "into_notification_cleaner_count";
    public static final String PREF_KEY_INTO_APP_LOCK_COUNT = "into_app_lock_count";
    public static final String PREF_KEY_INTO_UNREAD_MESSAGE_COUNT = "into_unread_message_count";
    public static final String PREF_KEY_INTO_WHATS_APP_COUNT = "into_whats_app_count";

    public static final String PREF_KEY_INTO_BATTERY_PROTECTION_SHOWN_TIME = "into_battery_protection_shown_time";
    public static final String PREF_KEY_INTO_NOTIFICATION_CLEANER_SHOWN_TIME = "into_notification_cleaner_shown_time";
    public static final String PREF_KEY_INTO_APP_LOCK_SHOWN_TIME = "into_app_lock_shown_time";

    public static final String PREF_KEY_BP_PROMOTION_CARD_SHOWN_TIME = "PREF_KEY_BP_PROMOTION_CARD_SHOWN_TIME";
    public static final String PREF_KEY_NC_PROMOTION_CARD_SHOWN_TIME = "PREF_KEY_NC_PROMOTION_CARD_SHOWN_TIME";
    public static final String PREF_KEY_AL_PROMOTION_CARD_SHOWN_TIME = "PREF_KEY_AL_PROMOTION_CARD_SHOWN_TIME";

    public static final String PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_TOTAL_JUNK_SIZE = "PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_TOTAL_SIZE";
    public static final String PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_MEM_JUNK_SIZE = "PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_MEM_JUNK_SIZE";
    public static final String PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_AD_JUNK_SIZE = "PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_AD_JUNK_SIZE";
    public static final String PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_APP_JUNK_SIZE = "PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_APP_JUNK_SIZE";
}
