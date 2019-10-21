/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.acb.libwallpaper.live.desktop;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.acb.libwallpaper.live.customize.WallpaperMgr;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.Preferences;

/**
 * We render wallpaper ourselves to get control over how wallpaper scrolls.
 */
public class WallpaperChangedReceiver implements BroadcastListener {

    public static final String RESET_WALLPAPER_LOCATION = "reset_wallpaper_location";

    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent data) {
        HSLog.i("ACTION_WALLPAPER_CHANGED");
        if (Intent.ACTION_WALLPAPER_CHANGED.equals(data.getAction())) {

            HSGlobalNotificationCenter.sendNotification(RESET_WALLPAPER_LOCATION);
            long setWPTime = System.currentTimeMillis() - Preferences.get(LauncherFiles.DESKTOP_PREFS).getLong(WallpaperMgr.PREF_KEY_SET_RECOMMEND_WALLPAPER, 0);
            boolean usesRecommend = Preferences.get(LauncherFiles.DESKTOP_PREFS).getBoolean(WallpaperMgr.PREF_KEY_USES_RECOMMEND_WALLPAPER, false);
            HSLog.i("HWallpaper", "NOTIFICATION_SET_WALLPAPER u == " + usesRecommend + "  t == " + setWPTime);
            if (usesRecommend && setWPTime > 5 * DateUtils.SECOND_IN_MILLIS) {
                HSLog.i("HWallpaper", "PREF_KEY_USES_RECOMMEND_WALLPAPER set false ");
                Preferences.get(LauncherFiles.DESKTOP_PREFS).putBoolean(WallpaperMgr.PREF_KEY_USES_RECOMMEND_WALLPAPER, false);
            }
        }
    }
}
