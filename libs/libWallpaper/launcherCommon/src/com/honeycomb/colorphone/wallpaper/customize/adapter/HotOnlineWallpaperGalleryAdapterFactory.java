package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.content.Context;


public class HotOnlineWallpaperGalleryAdapterFactory {
    public static AbstractOnlineWallpaperAdapter createHotOnlineWallpaperGalleryAdapter(int type, Context context) {
        switch (type) {
            default:
            case 1:
                return new OnlineWallpaperGalleryAdapter(context);
            case 2:
                return new HotOnlineWallpaperGalleryAdapter2(context);
            case 3:
                return new HotOnlineWallpaperGalleryAdapter3(context);
            case 4:
                return new HotOnlineWallpaperGalleryAdapter4(context);
        }
    }
}
