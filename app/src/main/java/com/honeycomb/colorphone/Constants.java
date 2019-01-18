package com.honeycomb.colorphone;

import com.ihs.commons.config.HSConfig;

public class Constants {

    public static final int DEFAULT_THEME_ID = 14; // Shining
    public static final String URL_PRIVACY = "https://colorphoneflash.weebly.com/";
    public static final String URL_TERM_SERVICES = "http://colorphone.weebly.com/terms-of-service.html";
    public static final String NOTIFICATION_PREFS = "notification.prefs";
    public static final String DESKTOP_PREFS = "desktop.prefs";

    public static final String PREFS_LED_FLASH_ENABLE = "led_flash_enable";
    public static final String PREFS_LED_SMS_ENABLE = "led_flash_sms_enable";
    public static final String PREFS_CHECK_DEFAULT_PHONE = "PREFS_CHECK_DEFAULT_PHONE";
    public static final String PREF_FILE_DEFAULT = "default_main";

    public static String getFeedBackAddress() {
        return HSConfig.optString("", "Application", "FeedbackEmailAddress");
    }
}
