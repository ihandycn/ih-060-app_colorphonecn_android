package com.acb.libwallpaper.live.livewallpaper;

import android.net.Uri;

public abstract class WallpaperLoader {

    public interface Callbacks {
        void onProgress(float progress);

        void onLiveWallpaperLoaded(Uri[] layerUris);

        void onLiveWallpaperLoadFailed(String message);
    }

    public abstract void load(Callbacks callbacks);

    public abstract void cancel();

    public abstract void setWallpaperName(String wallpaperName);

    public abstract String getWallpaperName();
}
