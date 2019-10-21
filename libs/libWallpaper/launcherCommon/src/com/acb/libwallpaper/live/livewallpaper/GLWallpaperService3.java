package com.acb.libwallpaper.live.livewallpaper;

/**
 * Alternative of {@link GLWallpaperService}.
 *
 * This service and {@link GLWallpaperService} are used in turns to support switching between video
 * wallpapers and non-video wallpapers. Also, on some HTC devices, the "APPLY" button on system
 * wallpaper preview page is greyed out when the wallpaper service for preview is set as current
 * live wallpaper. This makes it impossible for the user to switch between different instances of
 * our live wallpapers. We solve this by introducing this second wallpaper service and switch
 * between the two services.
 */
public class GLWallpaperService3 extends BaseWallpaperService {
    @Override
    protected BaseWallpaperManager getManager() {
        return LiveWallpaperManager.getInstance();
    }
}
