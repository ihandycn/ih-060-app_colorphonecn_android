package com.honeycomb.colorphone.customize.livewallpaper;


public class GLWallpaperService extends BaseWallpaperService {
    @Override
    protected BaseWallpaperManager getManager() {
        return LiveWallpaperManager.getInstance();
    }
}
