package com.honeycomb.colorphone.wallpaper.util;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.annimon.stream.Stream;
import com.honeycomb.colorphone.LauncherAnalytics;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

/**
 * Tracking installation of promoted ihs apps and live wallpapers.
 */

public final class PromotionTracker {

    private static final String PREF_KEY_TRACKED_APP_INSTALLATIONS = "tracked_app_installations";

    public static final String EVENT_LOG_APP_NAME_SECURITY = "Security";
    public static final String EVENT_LOG_APP_NAME_MAX = "Max";
    public static final String EVENT_LOG_APP_NAME_FLASHLIGHT = "Flashlight";
    public static final String EVENT_LOG_APP_NAME_CAMERA = "Camera";
    public static final String EVENT_LOG_APP_NAME_KEYBOARD = "Keyboard";
    public static final String EVENT_LOG_APP_NAME_KEYBOARD_THEME = "KeyboardTheme";
    public static final String EVENT_LOG_APP_NAME_LOCKER = "Locker";
    public static final String EVENT_LOG_APP_NAME_LOCKER_THEME = "LockerTheme";

    public static final String EVENT_LOG_APP_NAME_LIVE_WALLPAPER_PREFIX = "LiveWallpaper_";

    private static final long TRACK_DURATION = DateUtils.WEEK_IN_MILLIS; // A week to expire

    public static void startTracking(String packageName, String eventLogAppName) {
        startTracking(packageName, eventLogAppName, false);
    }

    public static void startTracking(String packageName, String eventLogAppName, boolean browseApp) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        // Performance: 2 IPC calls if not already tracked, 4 IPC calls if already tracked
        // (expiration time will be refreshed)
        if (browseApp) {
            Navigations.browseApp(packageName);
        }
        addTrackedApp(packageName, eventLogAppName);
    }

    public synchronized static void onAppInstalled(String packageName) {
        // Performance: 1 IPC call if no tracked app installed, 2 more IPC calls per tracked app installed
        getTrackedAppsLocked()
                .filter(trackedApp -> !trackedApp.isExpired() && TextUtils.equals(trackedApp.packageName, packageName))
                .forEach(trackedApp -> {
                    logInstallEvent(trackedApp);
                    removeTrackedAppLocked(trackedApp.packageName);
                });
    }

    private static void logInstallEvent(TrackedApp app) {
        String eventLogName = app.eventLogAppName;
        if (eventLogName.startsWith(EVENT_LOG_APP_NAME_LIVE_WALLPAPER_PREFIX)) {
            LauncherAnalytics.logEvent("Wallpaper_live_Downloaded", "PackageName", app.packageName);
        } else {
            LauncherAnalytics.logEvent("Promotion_Installed", "Type", eventLogName);
        }
    }

    public synchronized static void stopTrackingExpiredApps() {
        // Performance: 1 IPC call if no expired apps, 2 more IPC calls per expired app
        getTrackedAppsLocked()
                .filter(TrackedApp::isExpired)
                .forEach(trackedApp -> removeTrackedAppLocked(trackedApp.packageName));
    }

    private static void addTrackedApp(String packageName, String eventLogAppName) {
        // Performance: 2 IPC calls if not already tracked, 4 IPC calls if already tracked
        Preferences prefs = Preferences.getDefault();
        String trackRecord = new TrackedApp(packageName, eventLogAppName, System.currentTimeMillis() + TRACK_DURATION).toString();
        synchronized (PromotionTracker.class) {
            removeTrackedAppLocked(packageName);
            prefs.addStringToList(PREF_KEY_TRACKED_APP_INSTALLATIONS, trackRecord);
        }
    }

    private static Stream<TrackedApp> getTrackedAppsLocked() {
        // Performance: 1 IPC call
        return Stream.of(Preferences.getDefault().getStringList(PREF_KEY_TRACKED_APP_INSTALLATIONS))
                .map(TrackedApp::ofValue)
                .filter(trackedApp -> trackedApp != null);
    }

    private static void removeTrackedAppLocked(String packageName) {
        // Performance: 1 IPC call if not tracked, 3 IPC calls if tracked
        final Preferences prefs = Preferences.getDefault();
        Stream.of(prefs.getStringList(PREF_KEY_TRACKED_APP_INSTALLATIONS))
                .filter(trackRecord -> {
                    String[] sections = trackRecord.split(TrackedApp.SEPARATOR);
                    return sections.length == 3 && TextUtils.equals(sections[0], packageName);
                })
                .forEach(trackRecord -> prefs.removeStringFromList(PREF_KEY_TRACKED_APP_INSTALLATIONS, trackRecord));
    }

    private static class TrackedApp {
        String packageName;
        String eventLogAppName;
        long expireTime;

        static final String SEPARATOR = "/";

        TrackedApp(String packageName, String eventLogAppName, long expireTime) {
            this.packageName = packageName;
            this.eventLogAppName = eventLogAppName;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        static TrackedApp ofValue(String serializedString) {
            String[] sections = serializedString.split(SEPARATOR);
            if (sections.length == 3) {
                try {
                    return new TrackedApp(sections[0], sections[1], Long.valueOf(sections[2]));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return packageName + SEPARATOR + eventLogAppName + SEPARATOR + Long.toString(expireTime);
        }
    }
}
