package com.honeycomb.colorphone.wallpaper.livewallpaper;



public class GLWallpaperService extends BaseWallpaperService {
    @Override
    protected BaseWallpaperManager getManager() {
        return LiveWallpaperManager.getInstance();
    }
}
