package com.themelab.launcher;

import com.honeycomb.colorphone.wallpaper.livewallpaper.BaseWallpaperManager;
import com.honeycomb.colorphone.wallpaper.livewallpaper.BaseWallpaperService;
import com.ihs.app.framework.HSApplication;

/**
 * A {@link android.service.wallpaper.WallpaperService} used in launchers.
 */
public class ThemeWallpaperService extends BaseWallpaperService {
    private ThemeLiveWallpaperManager mLiveWallpaperManager;

    @Override
    protected BaseWallpaperManager getManager() {
        if (mLiveWallpaperManager ==  null) {
            String appId = HSApplication.getContext().getPackageName();
            int start = appId.lastIndexOf(".");
            String name = appId.substring(start + 1);
            mLiveWallpaperManager = new ThemeLiveWallpaperManager(name);
        }

        return mLiveWallpaperManager;
    }
}
