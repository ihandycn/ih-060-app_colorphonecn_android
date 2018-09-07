package com.honeycomb.colorphone.util;

import com.ihs.commons.utils.HSPreferenceHelper;

public class UserSettings {

    public static final String PREFS_CONTENT_FILE = "content.prefs";
    private static final String NOTIFICATION_APPS = "notification_apps";
    private static final String NOTIFICATION_REMINDER_ENABLE = "notification_reminder";

    public static final String PREF_KEY_NOTIFICATION_TOOLBAR_ENABLED = "PREF_KEY_NOTIFICATION_TOOLBAR_ENABLED";
    public static final String PREF_KEY_NOTIFICATION_TOOLBAR_TOGGLE_TOUCHED = "PREF_KEY_NOTIFICATION_TOOLBAR_TOGGLE_TOUCHED";

    public static boolean containsNotification() {
        return HSPreferenceHelper.getDefault().contains(NOTIFICATION_APPS);
    }

    public static String getNotificationApps() {
        return HSPreferenceHelper.getDefault().getString(NOTIFICATION_APPS, "");
    }

    public static void setNotificationApps(String notificationApps) {
        HSPreferenceHelper.getDefault().putString(NOTIFICATION_APPS, notificationApps);
    }

    public static boolean isNotificationReminderEnable() {
        return HSPreferenceHelper.getDefault().getBoolean(NOTIFICATION_REMINDER_ENABLE, false);
    }

    public static void setNotificationReminderEnable(boolean enable) {
        HSPreferenceHelper.getDefault().putBoolean(NOTIFICATION_REMINDER_ENABLE, enable);
    }


    public static void setNotificationToolbarEnabled(boolean enable) {
        HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_NOTIFICATION_TOOLBAR_ENABLED, enable);
    }

    public static boolean isNotificationToolbarEnabled() {
        return HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_NOTIFICATION_TOOLBAR_ENABLED, false);
    }

    public static void checkNotificationToolbarToggleClicked() {
        if (HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_NOTIFICATION_TOOLBAR_ENABLED, false)) {
            setNotificationToolbarToggleClicked();
        }
    }

    public static boolean isNotificationToolbarToggleClicked() {
        return HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_NOTIFICATION_TOOLBAR_TOGGLE_TOUCHED, false);
    }

    public static void setNotificationToolbarToggleClicked() {
        HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_NOTIFICATION_TOOLBAR_TOGGLE_TOUCHED, true);
    }
}
