/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acb.libwallpaper.live.customize.util;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.KeyEvent;

import com.annimon.stream.Stream;
import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.LauncherConstants;
import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.WallpaperAnalytics;
import com.acb.libwallpaper.live.customize.CustomizeConfig;
import com.acb.libwallpaper.live.customize.WallpaperInfo;
import com.acb.libwallpaper.live.customize.WallpaperMgr;
import com.acb.libwallpaper.live.customize.WallpaperProvider;
import com.acb.libwallpaper.live.customize.activity.CustomizeActivity;
import com.acb.libwallpaper.live.customize.view.PercentageProgressDialog;
import com.acb.libwallpaper.live.customize.view.ProgressDialog;
import com.acb.libwallpaper.live.customize.wallpaper.WallpaperManagerProxy;
import com.acb.libwallpaper.live.livewallpaper.BaseWallpaperService;
import com.acb.libwallpaper.live.livewallpaper.GLWallpaperService;
import com.acb.libwallpaper.live.livewallpaper.GLWallpaperService2;
import com.acb.libwallpaper.live.livewallpaper.GLWallpaperService3;
import com.acb.libwallpaper.live.livewallpaper.GLWallpaperService4;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperLoader;
import com.acb.libwallpaper.live.livewallpaper.WallpaperLoader;
import com.acb.libwallpaper.live.livewallpaper.WallpaperPreloadService;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.receiver.ScreenStatus;
import com.acb.libwallpaper.live.theme.ThemeConstants;
import com.acb.libwallpaper.live.util.ActivityUtils;
import com.acb.libwallpaper.live.util.Alarm;
import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.Calendars;
import com.superapps.util.Compats;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;
import com.themelab.launcher.ThemeWallpaperService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.acb.libwallpaper.live.customize.CustomizeConstants.DATE_LINE_HOUR;
import static com.acb.libwallpaper.live.customize.CustomizeConstants.PREFS_TOP_THEME_UPDATE_TIME;
import static com.acb.libwallpaper.live.customize.CustomizeConstants.THEME_PUBLISH_INTERVAL_DAYS;

/**
 * Utility methods for theme (wallpapers / icon sets) management and event logs.
 */
public final class CustomizeUtils {

    public static final String THREE_D_WALLPAPERS = "3DWallpapers";
    public static final String LIVE_WALLPAPERS = "LiveWallpapers";
    public static final String BASE_URL = "BaseUrl";
    public static final String THUMBNAIL_EXTENSION = "ThumbnailExtension";

    private static final String PREF_KEY_THEME_PACKAGE = "theme.package";
    public static final String PREF_KEY_LIVE_WALLPAPER_BADGE_ALLOWED = "live_wallpaper_badge_allowed";

    public static final String PREF_LIVE_WALLPAPER_TURN_COUNT = "pref_live_wallpaper_turn_count";
    public static final String PREF_3D_WALLPAPER_TURN_COUNT = "pref_3d_wallpaper_turn_count";
    public static final String PREF_HOT_WALLPAPER_TURN_COUNT = "pref_hot_wallpaper_turn_count";
    public static final String PREF_WALLPAPER_APPLY_TIME = "pref_hot_wallpaper_apply_time";

    public static final String WEATHER_CLOCK_AB_TEST_THEME = "com.themelab.launcher.clash";

    private static final long WALLPAPER_PROGRESS_BAR_TIMEOUT = 20 * DateUtils.SECOND_IN_MILLIS;

    private static boolean sLoadingWallpaper;

    public interface WallpaperApplyCallback {
        void onWallpaperApplied(Bitmap wallpaper, Intent data);
    }

    /**
     * Pattern string (supports *, ? only) -> Regex
     */
    private static HashMap<String, String> sRegexMap = new HashMap<>(2);

    public static void setThemePackage(String themePackage) {
        HSPreferenceHelper.getDefault().putString(PREF_KEY_THEME_PACKAGE, themePackage);
        Preferences.get(LauncherFiles.DESKTOP_PREFS).putLong(
                ThemeConstants.PREF_THEME_SET_TIME + ":" + themePackage, System.currentTimeMillis());
    }

    public static String getCurrentThemePackageWithDefault() {
        String themePackage = HSPreferenceHelper.getDefault().getString(PREF_KEY_THEME_PACKAGE,
                LauncherConstants.LAUNCHER_PACKAGE_NAME);
        if ("com.android.launcher".equals(themePackage)) { // Upgrade: v1.5.1 (104) -> v1.5.2 (105)
            themePackage = LauncherConstants.LAUNCHER_PACKAGE_NAME;
            HSPreferenceHelper.getDefault().putString(PREF_KEY_THEME_PACKAGE, themePackage);
        }
        return themePackage;
    }

    public static boolean shouldAddHotBadgeOnThemes() {
        return !Preferences.get(LauncherFiles.DESKTOP_PREFS)
                .getBoolean(CustomizeActivity.PREF_KEY_THEME_LAUNCHED_FROM_SHORTCUT, false)
                && Utils.isNewUser();
    }

    public static boolean needPublishNewThemes() {
        long lastShowTime = Preferences.getDefault().getLong(PREFS_TOP_THEME_UPDATE_TIME, 0);
        return Calendars.getDayDifference(lastShowTime, System.currentTimeMillis(), DATE_LINE_HOUR) >= THEME_PUBLISH_INTERVAL_DAYS;
    }

    public static String generate3DWallpaperThumbnailUrl(String name) {
        Map<String, ?> configMap = CustomizeConfig.getMap(THREE_D_WALLPAPERS);
        return generateThumbnailUrl(configMap, name);
    }

    public static String generateLiveWallpaperThumbnailUrl(String name) {
        Map<String, ?> configMap = CustomizeConfig.getMap(LIVE_WALLPAPERS);
        return generateThumbnailUrl(configMap, name);
    }

    private static String generateThumbnailUrl(Map<String, ?> configMap, String name) {
        return HSMapUtils.optString(configMap, "", BASE_URL)
                + name + File.separator
                + "thumb." + HSMapUtils.optString(configMap, "", THUMBNAIL_EXTENSION);
    }


    public static boolean isUserInstallInOneDay() {
        return (System.currentTimeMillis() - Utils.getAppInstallTimeMillis()) < DateUtils.DAY_IN_MILLIS;
    }

    public static void updateWallpaperTurn() {
        long applyTime = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getLong(PREF_WALLPAPER_APPLY_TIME, 0);
        if (System.currentTimeMillis() - applyTime > 10 * DateUtils.MINUTE_IN_MILLIS) {
            Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).incrementAndGetInt(CustomizeUtils.PREF_HOT_WALLPAPER_TURN_COUNT);
        }
    }

    private static String getPatternRegex(String pattern) {
        String regex = sRegexMap.get(pattern);
        if (regex == null) {
            regex = pattern
                    .replaceAll("\\.", "\\\\.") // Escapes "." in package names
                    .replaceAll("\\*", ".*") // "*" support
                    .replaceAll("\\?", "."); // "?" support
            sRegexMap.put(pattern, regex);
        }
        return regex;
    }

    public static int getTimeTickInterval() {
        return (System.currentTimeMillis() - ScreenStatus.getScreenOnTime() < 3 * 60 * 1000)
                ? 1 : 10;
    }

    public static Intent getThemeChangedBroadcastIntent(String themePackage) {
        Intent finishBroadcast = new Intent(ThemeConstants.BROADCAST_THEME_CHANGED);
        finishBroadcast.putExtra(ThemeConstants.INTENT_KEY_THEME_PACKAGE_NAME, themePackage);
        return finishBroadcast;
    }

    public static void applyWallpaper(final Context context,
                                      final WallpaperInfo info,
                                      final Bitmap wallpaper,
                                      final WallpaperApplyCallback callback) {
        // Apply from main process
        if (!CommonUtils.isMainProcess(context)) {
            return;
        }
        if (wallpaper == null) {
            Toasts.showToast(R.string.wallpaper_toast_set_failed);
            return;
        }
        final ProgressDialog dialog = ProgressDialog.createDialog(context,
                context.getString(R.string.wallpaper_setting_progress_dialog_text));
        dialog.show();

        // Register to broadcast first and then apply the wallpaper
        BroadcastCenter.register(context, new BroadcastListener() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                HSLog.d("WallpaperReload", "Received ACTION_WALLPAPER_CHANGED broadcast");
                BroadcastCenter.unregister(context, this);

                if (dialog.isShowing()) {
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (callback != null) {
                                callback.onWallpaperApplied(wallpaper, intent);
                            }
                        }
                    });
                    dialog.dismiss(false);
                } else {
                    if (callback != null) {
                        callback.onWallpaperApplied(wallpaper, intent);
                    }
                }
            }
        }, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));

        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    WallpaperManagerProxy.getInstance().setSystemBitmap(context, wallpaper);
                    info.setApplied(true);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(WallpaperProvider.BUNDLE_KEY_WALLPAPER, info);

                    // Upgrade from v1.2.8 (33), if necessary
                    String jsonArrayString = HSPreferenceHelper.getDefault().getString(
                            WallpaperMgr.Prefs.LOCAL_WALLPAPERS, "");
                    bundle.putString(WallpaperProvider.BUNDLE_KEY_LEGACY_LOCAL_WALLPAPERS, jsonArrayString);

                    // Consume the legacy local wallpapers to avoid any duplicated upgrade
                    // when CustomizedActivity brings up WallpaperMgr.
                    HSPreferenceHelper.getDefault().putString(WallpaperMgr.Prefs.LOCAL_WALLPAPERS, "");

                    ContentResolver contentResolver = HSApplication.getContext().getContentResolver();
                    contentResolver.call(WallpaperProvider.CONTENT_URI,
                            WallpaperProvider.METHOD_APPLY_WALLPAPER, "", bundle);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return The {@link WallpaperLoader} instance used to load local wallpaper (if needed).
     * Can be used to cancel the load later on.
     */
    public static WallpaperLoader preview3DWallpaper(final Activity activity, final WallpaperInfo info) {
        return previewWallpaper(activity, info, false, true);
    }

    /**
     * @return The {@link WallpaperLoader} instance used to load local wallpaper (if needed).
     * Can be used to cancel the load later on.
     */
    public static WallpaperLoader previewLiveWallpaper(final Activity activity, final WallpaperInfo info) {
        return previewLiveWallpaper(activity, info, true);
    }

    public static WallpaperLoader previewLiveWallpaper(final Activity activity, final WallpaperInfo info,
                                                       boolean showProgress) {
        return previewWallpaper(activity, info, true, showProgress);
    }

    private static WallpaperLoader previewWallpaper(final Activity activity, final WallpaperInfo info,
                                                    boolean isLive, boolean showProgress) {
        if (sLoadingWallpaper || ActivityUtils.isDestroyed(activity)) {
            return null;
        }
        sLoadingWallpaper = true;
        LiveWallpaperLoader wallpaperLoader = new LiveWallpaperLoader();
        String wallpaperName = info.getSource();
        wallpaperLoader.setWallpaperName(wallpaperName);
        PercentageProgressDialog dialog = null;
        if (showProgress) {
            dialog = PercentageProgressDialog.createDialog(activity,
                    activity.getString(R.string.live_wallpaper_loading_text));
        }
        PercentageProgressDialog finalDialog = dialog;
        WallpaperLoadEventLogger eventLogger = new WallpaperLoadEventLogger(wallpaperName, isLive);
        Alarm watchdog = new Alarm();
        watchdog.setOnAlarmListener(alarm -> {
            sLoadingWallpaper = false;
            wallpaperLoader.cancel();
            eventLogger.onLoadFailed();
            if (finalDialog != null) {
                finalDialog.cancel();
            }
            Toasts.showToast(R.string.wallpaper_network_error);
        });
        eventLogger.onShow();
        if (dialog != null) {
            dialog.show();
            dialog.setProgress(0f);
            dialog.setCancelable(true);
            dialog.setOnKeyListener(eventLogger);
            dialog.setOnCancelListener(cancelledDialog -> {
                sLoadingWallpaper = false;
                watchdog.cancelAlarm();
                wallpaperLoader.cancel();
                eventLogger.onCancel();
                WallpaperAnalytics.logEvent("Wallpaper_Live_Detail_Click", "success", "false");
            });
        }
        watchdog.setAlarm(WALLPAPER_PROGRESS_BAR_TIMEOUT);
        wallpaperLoader.load(new LiveWallpaperLoader.Callbacks() {
            @Override
            public void onProgress(float progress) {
                HSLog.d(LiveWallpaperLoader.TAG, "Wallpaper loader progress: " + progress);
                watchdog.setAlarm(WALLPAPER_PROGRESS_BAR_TIMEOUT);
                if (finalDialog != null) {
                    finalDialog.setProgress(progress);
                }
            }

            @Override
            public void onLiveWallpaperLoaded(Uri[] layerUris) {
                sLoadingWallpaper = false;
                watchdog.cancelAlarm();
                boolean shouldShowPreview = !eventLogger.onLoadSucceeded();
                if (!ActivityUtils.isDestroyed(activity) && finalDialog != null) {
                    finalDialog.dismiss();
                }

                if (shouldShowPreview) {
                    HSLog.d(LiveWallpaperLoader.TAG, "Wallpaper loader loaded, start preview");
                    Preferences prefs = Preferences.getDefault();
                    prefs.putString(LiveWallpaperConsts.PREF_KEY_PREVIEW_WALLPAPER_NAME, info.getSource());
                    prefs.putBoolean(LiveWallpaperConsts.PREF_KEY_IS_PREVIEW_MODE, true);
                    Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(activity, getWallpaperService(prefs)));
                    Navigations.startActivityForResultSafely(activity, intent, CustomizeActivity.REQUEST_CODE_APPLY_3D_WALLPAPER);
                    WallpaperPreloadService.prepareLiveWallpaper(activity);

                    if (Compats.IS_OPPO_DEVICE) {
                        Threads.postOnMainThreadDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.finish();
                            }
                        }, 300);
                    }

                    WallpaperAnalytics.logEvent("Wallpaper_Live_Detail_Click", "success", "true");
                }
            }

            @SuppressWarnings("unchecked")
            private Class<? extends BaseWallpaperService> getWallpaperService(Preferences prefs) {
                int serviceIdx = prefs.incrementAndGetInt(
                        LiveWallpaperConsts.PREF_KEY_SERVICE_INDEX);
                final Class[] services = {
                        GLWallpaperService.class,
                        GLWallpaperService2.class,
                        GLWallpaperService3.class,
                        GLWallpaperService4.class,
                };
                return services[serviceIdx % LiveWallpaperConsts.NUMBER_OF_SERVICES];
            }

            @Override
            public void onLiveWallpaperLoadFailed(String message) {
                sLoadingWallpaper = false;
                watchdog.cancelAlarm();
                eventLogger.onLoadFailed();
                if (finalDialog != null) {
                    finalDialog.cancel();
                }
                Toasts.showToast(R.string.wallpaper_network_error);
                WallpaperAnalytics.logEvent("Wallpaper_Live_Detail_Click", "success", "false");
            }
        });
        return wallpaperLoader;
    }

    public static void logThemeDurationEvent() {
        String previousTheme = HSPreferenceHelper.getDefault().getString(PREF_KEY_THEME_PACKAGE, "");
        if (TextUtils.isEmpty(previousTheme)) {
            return;
        }
        String durationDesc = "";
        long previousSetTime = Preferences.get(LauncherFiles.DESKTOP_PREFS)
                .getLong(ThemeConstants.PREF_THEME_SET_TIME + ":" + previousTheme, -1);
        if (previousSetTime != -1) {
            long duration = System.currentTimeMillis() - previousSetTime;
            if (duration >= 0 && duration < 10 * DateUtils.MINUTE_IN_MILLIS) {
                durationDesc = "less than 10 minutes";
            } else if (duration >= 10 * DateUtils.MINUTE_IN_MILLIS && duration < DateUtils.HOUR_IN_MILLIS) {
                durationDesc = "10 to 60 minutes";
            } else if (duration >= DateUtils.HOUR_IN_MILLIS && duration < DateUtils.DAY_IN_MILLIS) {
                durationDesc = "1 hour to 24 hours";
            } else if (duration >= DateUtils.DAY_IN_MILLIS && duration < 3 * DateUtils.DAY_IN_MILLIS) {
                durationDesc = "1 day to 3 days";
            } else {
                durationDesc = "more than 3 days";
            }
        }
        Map<String, String> value = new HashMap<>(4);
        value.put("Duration", durationDesc);
        value.put(previousTheme, durationDesc);
        LauncherAnalytics.logEvent("Theme_Name_UseTime", value);
    }

    public static String getCurrentLiveWallpaperName() {
        return HSPreferenceHelper.getDefault().getString(LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, "");
    }

    public static boolean isApplySuccessful(Context context, int resultCode) {
        if (isDeviceAlwaysReturnCancel()) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            android.app.WallpaperInfo info = wallpaperManager.getWallpaperInfo();
            return info != null && isWallpaperService(info.getComponent().getClassName());
        } else {
            return resultCode == Activity.RESULT_OK;
        }
    }

    private static final Class[] WALLPAPER_SERVICES = {
            ThemeWallpaperService.class,
            GLWallpaperService.class,
            GLWallpaperService2.class,
            GLWallpaperService3.class,
            GLWallpaperService4.class,
    };

    public static boolean isWallpaperService(String className) {
        return Stream.of(WALLPAPER_SERVICES).anyMatch(klass -> className.equals(klass.getName()));
    }

    public static boolean isDeviceAlwaysReturnCancel() {
        int sdkInt = Build.VERSION.SDK_INT;
        return sdkInt == Build.VERSION_CODES.JELLY_BEAN_MR2
                || (Compats.IS_SONY_DEVICE && sdkInt == Build.VERSION_CODES.JELLY_BEAN)
                || (Compats.IS_HUAWEI_DEVICE && sdkInt == Build.VERSION_CODES.KITKAT)
                || (Compats.IS_VIVO_DEVICE && sdkInt >= Build.VERSION_CODES.M)
                || (Compats.IS_XIAOMI_DEVICE && sdkInt >= Build.VERSION_CODES.M)
                || (Compats.IS_GOOGLE_DEVICE && sdkInt == Build.VERSION_CODES.KITKAT);
    }

    private static class WallpaperLoadEventLogger
            implements DialogInterface.OnKeyListener, HomeKeyWatcher.OnHomePressedListener {
        private static final int EVENT_STATE_UNCLEAR = 1;
        private static final int EVENT_STATE_LOAD_SUCCEEDED = 0;
        private static final int EVENT_STATE_LOAD_FAILED = -1;
        private static final int EVENT_STATE_BACK_PRESSED = -2;

        private String mWallpaperName;
        private boolean mIsLive;
        private int mEventState = EVENT_STATE_UNCLEAR;
        private boolean mHomeOrRecentsPressed;

        private boolean mEventLogged;

        private HomeKeyWatcher mHomeKeyWatcher = new HomeKeyWatcher(HSApplication.getContext());

        WallpaperLoadEventLogger(String wallpaperName, boolean isLive) {
            mWallpaperName = wallpaperName;
            mIsLive = isLive;
            mHomeKeyWatcher.setOnHomePressedListener(this);
        }

        /**
         * @return Whether home or recents button has been pressed during loading.
         */
        boolean onLoadSucceeded() {
            mEventState = EVENT_STATE_LOAD_SUCCEEDED;
            logEvent();
            return mHomeOrRecentsPressed;
        }

        void onLoadFailed() {
            mEventState = EVENT_STATE_LOAD_FAILED;
        }

        void onShow() {
            LauncherAnalytics.logEvent("Wallpaper_" + getWallpaperTypeDescription() + "_Loading_Shown",
                    "Name", mWallpaperName);
            mHomeKeyWatcher.startWatch();
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mEventState = EVENT_STATE_BACK_PRESSED;
            }
            return false;
        }

        @Override
        public void onHomePressed() {
            mHomeOrRecentsPressed = true;
            logEvent();
        }

        @Override
        public void onRecentsPressed() {
            mHomeOrRecentsPressed = true;
            logEvent();
        }

        void onCancel() {
            logEvent();
        }

        private void logEvent() {
            if (!mEventLogged) {
                mHomeKeyWatcher.stopWatch();
                mEventLogged = true;
                if (mEventState != EVENT_STATE_LOAD_SUCCEEDED) {
                    LauncherAnalytics.logEvent("Wallpaper_" + getWallpaperTypeDescription() + "_Loading_Closed",
                            "Reason", getDialogCloseReasonDescription(),
                            "Name", mWallpaperName);
                }
            }
        }

        private String getWallpaperTypeDescription() {
            return mIsLive ? "Live" : "3D";
        }

        private String getDialogCloseReasonDescription() {
            switch (mEventState) {
                case EVENT_STATE_LOAD_FAILED:
                    return "LoadingFailed";
                case EVENT_STATE_BACK_PRESSED:
                    return "Back";
                case EVENT_STATE_UNCLEAR:
                    return "Other";
                case EVENT_STATE_LOAD_SUCCEEDED:
                    throw new IllegalStateException("Should not log dialog close reason on success.");
            }
            return "";
        }
    }
}
