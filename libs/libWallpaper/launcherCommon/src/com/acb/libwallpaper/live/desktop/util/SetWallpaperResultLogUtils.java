package com.acb.libwallpaper.live.desktop.util;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.livewallpaper.guide.GuideHelper;
import com.acb.libwallpaper.live.customize.WallpaperPicCacheUtils;
import com.acb.libwallpaper.live.customize.WallpaperRecommendDialog;
import com.acb.libwallpaper.live.customize.util.CustomizeUtils;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.util.EventUtils;
import com.superapps.util.Preferences;

import java.util.HashMap;
import java.util.Map;

public class SetWallpaperResultLogUtils {

    public static void logLiveWallpaperUseEvents(String newWallpaper, boolean isLive) {
        String previousWallpaper = Preferences.getDefault().getString(LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, "");
        if (TextUtils.equals(previousWallpaper, newWallpaper)) {
            return;
        }
        // Log wallpaper apply event
        GuideHelper.logWallpaperApply(newWallpaper);

        String type = (isLive ? "Live" : "3D");

        // Save current wallpaper apply time
        long now = System.currentTimeMillis();
        Preferences prefs = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong(CustomizeUtils.PREF_WALLPAPER_APPLY_TIME, now);
        prefsEditor.putLong(LiveWallpaperConsts.PREF_KEY_WALLPAPER_SET_TIME_PREFIX + ":" + newWallpaper, now);
        String previousType = prefs.getString(LiveWallpaperConsts.PREF_KEY_PREVIOUS_WALLPAPER_TYPE, "");
        prefsEditor.putString(LiveWallpaperConsts.PREF_KEY_PREVIOUS_WALLPAPER_TYPE, type);
        prefsEditor.apply();

        // Log previous wallpaper use duration event
        String durationDesc;
        long previousSetTime = prefs.getLong(
                LiveWallpaperConsts.PREF_KEY_WALLPAPER_SET_TIME_PREFIX + ":" + previousWallpaper, -1);
        if (previousSetTime != -1 && !TextUtils.isEmpty(previousWallpaper)) {
            durationDesc = EventUtils.getDurationDescription(now - previousSetTime);
            Map<String, String> value = new HashMap<>(4);
            value.put("Duration", durationDesc);
            value.put(previousWallpaper, durationDesc);
            LauncherAnalytics.logEvent("Wallpaper_" + previousType + "_SetAsWallpaper_UseTime", value);
        }
    }

    public static void logFabricSetSuccessByShuffle(String name, int wallpaperType) {
        if (!TextUtils.isEmpty(name)) {
            String type = "";
            switch (wallpaperType) {
                case WallpaperRecommendDialog.TYPE_THREE_D:
                    type = "Alert_Shuffle_3D_SetAsWallpaper";
                    break;
                case WallpaperRecommendDialog.TYPE_LIVE:
                    type = "Alert_Shuffle_Live_SetAsWallpaper";
                default:
                    break;
            }
            if (!TextUtils.isEmpty(type)) {
                LauncherAnalytics.logEvent(type, "Type", name);
            }
        }
        Preferences.get(LauncherFiles.DESKTOP_PREFS).putString(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_NAME, "");
        Preferences.get(LauncherFiles.DESKTOP_PREFS).putInt(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_TYPE, -1);
    }

    public static void logFabricSetSuccessByWallpaperAward(String name, int wallpaperType) {
        if (!TextUtils.isEmpty(name)) {
            String type = "";
            switch (wallpaperType) {
                case WallpaperRecommendDialog.TYPE_THREE_D:
                    type = "wallpaper_Alert_3D_SetAsWallpaper";
                    break;
                case WallpaperRecommendDialog.TYPE_LIVE:
                    type = "wallpaper_Alert_live_SetAsWallpaper";
                default:
                    break;
            }
            if (!TextUtils.isEmpty(type)) {
                LauncherAnalytics.logEvent(type, "Type", name);
            }
        }
        SharedPreferences.Editor prefsEditor = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).edit();
        prefsEditor.putString(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_NAME, "");
        prefsEditor.putInt(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_TYPE, -1);
        prefsEditor.apply();
    }
}
