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

package com.honeycomb.colorphone.customize.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.CustomizeConstants;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.activity.VideoWallpaperPreviewActivity;
import com.honeycomb.colorphone.customize.livewallpaper.BaseWallpaperService;
import com.honeycomb.colorphone.customize.livewallpaper.GLWallpaperService;
import com.honeycomb.colorphone.customize.livewallpaper.GLWallpaperService2;
import com.honeycomb.colorphone.customize.livewallpaper.GLWallpaperService3;
import com.honeycomb.colorphone.customize.livewallpaper.GLWallpaperService4;
import com.honeycomb.colorphone.customize.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.customize.livewallpaper.LiveWallpaperLoader;
import com.honeycomb.colorphone.customize.livewallpaper.VideoWallpaperLoader;
import com.honeycomb.colorphone.customize.livewallpaper.WallpaperLoader;
import com.honeycomb.colorphone.customize.theme.ThemeInfo;
import com.honeycomb.colorphone.customize.theme.data.ThemeDataProvider;
import com.honeycomb.colorphone.customize.view.PercentageProgressDialog;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.superapps.util.Calendars;
import com.superapps.util.Compats;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.honeycomb.colorphone.customize.CustomizeConstants.DATE_LINE_HOUR;
import static com.honeycomb.colorphone.customize.CustomizeConstants.PREFS_TOP_THEME_ID;
import static com.honeycomb.colorphone.customize.CustomizeConstants.PREFS_TOP_THEME_UPDATE_TIME;
import static com.honeycomb.colorphone.customize.CustomizeConstants.THEME_PUBLISH_INTERVAL_DAYS;

/**
 * Utility methods for theme (wallpapers / icon sets) management and event logs.
 */
public final class CustomizeUtils {

    public static final String THREE_D_WALLPAPERS = "3DWallpapers";
    public static final String BASE_URL = "BaseUrl";
    public static final String THUMBNAIL_EXTENSION = "ThumbnailExtension";

    private static final String PREF_KEY_THEME_PACKAGE = "theme.package";
    public static final String PREF_KEY_LIVE_WALLPAPER_BADGE_ALLOWED = "live_wallpaper_badge_allowed";

    public static final String PREF_LIVE_WALLPAPER_TURN_COUNT = "pref_live_wallpaper_turn_count";
    public static final String PREF_3D_WALLPAPER_TURN_COUNT = "pref_3d_wallpaper_turn_count";
    public static final String PREF_HOT_WALLPAPER_TURN_COUNT = "pref_hot_wallpaper_turn_count";
    public static final String PREF_WALLPAPER_APPLY_TIME = "pref_hot_wallpaper_apply_time";
    public static final String PREF_WALLPAPER_LOCKER = "pref_wallpaper_locker_apply";
    public static final String PREF_WALLPAPER_LOCKER_MUTE = "pref_wallpaper_locker_video_mute";
    public static final String WEATHER_CLOCK_AB_TEST_THEME = "com.themelab.launcher.clash";

    private static final long WALLPAPER_PROGRESS_BAR_TIMEOUT = 20 * DateUtils.SECOND_IN_MILLIS;

    public static final int VIDEO_NO_AUDIO = 0;
    public static final int VIDEO_AUDIO_ON = 1;
    public static final int VIDEO_AUDIO_OFF = 2;

    private static boolean sLoadingWallpaper;

    public static void setLockerWallpaperPath(String path) {
        Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).putString(PREF_WALLPAPER_LOCKER, path);
    }

    public static String getLockerWallpaperPath() {
        return Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).getString(PREF_WALLPAPER_LOCKER, "");
    }

    public static void setVideoAudioStatus(int audioStatus) {
        Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).putInt(PREF_WALLPAPER_LOCKER_MUTE, audioStatus);
    }

    public static int getVideoAudioStatus() {
        return Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).getInt(PREF_WALLPAPER_LOCKER_MUTE, VIDEO_NO_AUDIO);
    }

    public static boolean isVideoMute() {
        return Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS)
                .getInt(PREF_WALLPAPER_LOCKER_MUTE, VIDEO_NO_AUDIO) == VIDEO_AUDIO_OFF;
    }

    public static void setWallpaperWindowFlags(Activity activity) {
        // Show on lock
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        // Status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    public interface WallpaperApplyCallback {
        void onWallpaperApplied(Bitmap wallpaper, Intent data);
    }

    /**
     * Pattern string (supports *, ? only) -> Regex
     */
    private static HashMap<String, String> sRegexMap = new HashMap<>(2);



    public static boolean needPublishNewThemes() {
        long lastShowTime = Preferences.getDefault().getLong(PREFS_TOP_THEME_UPDATE_TIME, 0);
        return Calendars.getDayDifference(lastShowTime, System.currentTimeMillis(), DATE_LINE_HOUR) >= THEME_PUBLISH_INTERVAL_DAYS;
    }

    public static String generate3DWallpaperThumbnailUrl(String name) {
        Map<String, ?> configMap = CustomizeConfig.getMap(THREE_D_WALLPAPERS);
        return generateThumbnailUrl(configMap, name);
    }

    public static String generateLiveWallpaperThumbnailUrl(String name) {
        Map<String, ?> configMap = CustomizeConfig.getMap("Application", "Wallpaper", "LiveWallpapers");
        return generateThumbnailUrl(configMap, name);
    }

    private static String generateThumbnailUrl(Map<String, ?> configMap, String name) {
        return HSMapUtils.optString(configMap, "", BASE_URL)
                + name + File.separator
                + "thumb." + HSMapUtils.optString(configMap, "png", THUMBNAIL_EXTENSION);
    }

    public static boolean hasNewThemesToShow() {
        int localId = Preferences.getDefault().getInt(PREFS_TOP_THEME_ID, -1);
        List<ThemeInfo> allThemes = ThemeDataProvider.getAllThemes(true);
        int maxId = allThemes.size() > 0 ? allThemes.get(0).updateId : 0;
        return localId > 0 && localId < maxId && needPublishNewThemes();
    }

    public static void updateWallpaperTurn() {
        long applyTime = Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).getLong(PREF_WALLPAPER_APPLY_TIME, 0);
        if (System.currentTimeMillis() - applyTime > 10 * DateUtils.MINUTE_IN_MILLIS) {
            Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).incrementAndGetInt(CustomizeUtils.PREF_HOT_WALLPAPER_TURN_COUNT);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isThemePackage(String themePackage) {
        if (TextUtils.isEmpty(themePackage)) {
            return false;
        }
        List<String> matchPatterns = ThemeDataProvider.getPackagePatterns();
        if (matchPatterns != null) {
            for (String pattern : matchPatterns) {
                String regex = getPatternRegex(pattern);
                if (themePackage.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
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

    public static void configTabLayoutText(final TabLayout tabLayout, final Typeface typeface, final float textSize) {
        setTypefaceRecursive(tabLayout, typeface);

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildrenCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildrenCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTextSize(textSize);
                }
            }
        }
    }


    public static void setTypefaceRecursive(View root, Typeface typeface) {
        if (!(root instanceof ViewGroup)) {
            if (root instanceof TextView) {
                ((TextView) root).setTypeface(typeface);
            }
            return;
        }
        int childCount = ((ViewGroup) root).getChildCount();
        for (int i = 0; i < childCount; i++) {
            setTypefaceRecursive(((ViewGroup) root).getChildAt(i), typeface);
        }
    }

    public static int mirrorIndexIfRtl(boolean isRtl, int total, int ltrIndex) {
        if (isRtl) {
            return total - ltrIndex - 1;
        } else {
            return ltrIndex;
        }
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
        final boolean isColorPhoneVideo =
                info.getType() == WallpaperInfo.WALLPAPER_TYPE_VIDEO;

        WallpaperLoader wallpaperLoader = isColorPhoneVideo
                ? new VideoWallpaperLoader()
                : new LiveWallpaperLoader();

        final String wallpaperName = info.getSource();
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
//                    Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                            new ComponentName(activity, getWallpaperService(prefs)));
//                    Navigations.startActivityForResultSafely(activity, intent, CustomizeActivity.REQUEST_CODE_APPLY_3D_WALLPAPER);
                    String path = isColorPhoneVideo ? TasksManager.getVideoWallpaperPath(info.getSource())
                            : getLiveVideoPath(wallpaperName);
                    VideoWallpaperPreviewActivity.start(activity, path, isColorPhoneVideo);

//                    WallpaperPreloadService.prepareLiveWallpaper(activity);
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
            }
        });
        return wallpaperLoader;
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
            Analytics.logEvent("Wallpaper_" + getWallpaperTypeDescription() + "_Loading_Shown", true,
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
                    Analytics.logEvent("Wallpaper_" + getWallpaperTypeDescription() + "_Loading_Closed", true,
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

    public static String getLiveVideoPath(String wallpaperName) {
        File baseDirectory = Utils.getDirectory(
                LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
        File videoFile = new File(baseDirectory, "video.mp4");
        return videoFile.getAbsolutePath();
    }
}
