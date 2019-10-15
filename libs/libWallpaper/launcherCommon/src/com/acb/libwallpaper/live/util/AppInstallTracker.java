package com.acb.libwallpaper.live.util;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.annimon.stream.Stream;
import com.acb.libwallpaper.live.LauncherAnalytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

/**
 * Google Play install guide (v1.5.1, 103).
 * <p>
 * Tracking installation of themes.
 */

public final class AppInstallTracker {

    private static final String TAG = AppInstallTracker.class.getSimpleName();

    private static final String PREF_KEY_APP_INSTALLATION_TRACKER = "app_installation_tracker";

    private static final long TRACK_DURATION = 30 * DateUtils.MINUTE_IN_MILLIS; // 30 min

    public static void startTracking(String packageName, String extra) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        HSLog.i(TAG, "startTracking: " + packageName + ", " + extra);

        // Performance: 2 IPC calls if not already tracked, 4 IPC calls if already tracked
        // (expiration time will be refreshed)
        addTrackedApp(packageName, extra);
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
        String from = app.extra;
        if (!TextUtils.isEmpty(from)) {
            HSLog.i(TAG, "logInstallEvent: " + app);
            LauncherAnalytics.logEvent("Theme_GooglePlay_From", "type", from);
        }
    }

    public synchronized static void stopTrackingExpiredApps() {
        // Performance: 1 IPC call if no expired apps, 2 more IPC calls per expired app
        getTrackedAppsLocked()
                .filter(TrackedApp::isExpired)
                .forEach(trackedApp -> removeTrackedAppLocked(trackedApp.packageName));
    }

    private static void addTrackedApp(String packageName, String extra) {
        // Performance: 2 IPC calls if not already tracked, 4 IPC calls if already tracked
        Preferences prefs = Preferences.getDefault();
        String trackRecord = new TrackedApp(packageName, extra, System.currentTimeMillis() + TRACK_DURATION).toString();
        synchronized (AppInstallTracker.class) {
            removeTrackedAppLocked(packageName);
            prefs.addStringToList(PREF_KEY_APP_INSTALLATION_TRACKER, trackRecord);
        }
    }

    private static Stream<TrackedApp> getTrackedAppsLocked() {
        // Performance: 1 IPC call
        return Stream.of(Preferences.getDefault().getStringList(PREF_KEY_APP_INSTALLATION_TRACKER))
                .map(TrackedApp::ofValue)
                .filter(trackedApp -> trackedApp != null);
    }

    private static void removeTrackedAppLocked(String packageName) {
        // Performance: 1 IPC call if not tracked, 3 IPC calls if tracked
        final Preferences prefs = Preferences.getDefault();
        Stream.of(prefs.getStringList(PREF_KEY_APP_INSTALLATION_TRACKER))
                .filter(trackRecord -> {
                    String[] sections = trackRecord.split(TrackedApp.SEPARATOR);
                    return sections.length == 3 && TextUtils.equals(sections[0], packageName);
                })
                .forEach(trackRecord -> prefs.removeStringFromList(PREF_KEY_APP_INSTALLATION_TRACKER, trackRecord));
    }

    private static class TrackedApp {
        String packageName;
        String extra;
        long expireTime;

        static final String SEPARATOR = "/";

        TrackedApp(String packageName, String extra, long expireTime) {
            this.packageName = packageName;
            this.extra = extra;
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
            return packageName + SEPARATOR + extra + SEPARATOR + Long.toString(expireTime);
        }
    }
}
