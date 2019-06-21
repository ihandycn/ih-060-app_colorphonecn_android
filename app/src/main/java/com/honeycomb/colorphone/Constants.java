package com.honeycomb.colorphone;

import com.ihs.commons.config.HSConfig;

public class Constants {
    public static final int DEFAULT_THEME_ID = 14; // Shining
    public static final String NOTIFICATION_PREFS = "notification.prefs";
    public static final String DESKTOP_PREFS = "desktop.prefs";
    public static final String PREFS_LED_FLASH_ENABLE = "led_flash_enable";
    public static final String PREFS_LED_SMS_ENABLE = "led_flash_sms_enable";
    public static final String PREFS_CHECK_DEFAULT_PHONE = "PREFS_CHECK_DEFAULT_PHONE";
    public static final String PREF_FILE_DEFAULT = "default_main";
    public static final String KEY_TAB_POSITION = "tab_position";
    public static final String INTENT_KEY_TAB_POSITION = "intent_tab_position";
    public static final String KEY_TAB_LEAVE_NEWS = "tab_leave_news";

    public static final String NOTIFY_KEY_LIST_SCROLLED = "content_list_scrolled";
    public static final String NOTIFY_KEY_LIST_SCROLLED_TOP = "content_list_scrolled_TOP";

    public static String getFeedBackAddress() {
        return HSConfig.optString("", "Application", "FeedbackEmailAddress");
    }

    public static String getUrlPrivacy() {
        return HSConfig.optString("", "Application", "PrivacyPolicyURL");
    }

    public static String getUrlTermServices() {
        return HSConfig.optString("", "Application", "TermsOfServiceURL");
    }

}
