package com.acb.libwallpaper.live.livewallpaper;


public class GLWallpaperService extends BaseWallpaperService {
    @Override
    protected BaseWallpaperManager getManager() {
        return LiveWallpaperManager.getInstance();
    }
}
