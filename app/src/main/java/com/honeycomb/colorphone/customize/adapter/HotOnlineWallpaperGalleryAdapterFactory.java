package com.honeycomb.colorphone.customize.adapter;

import android.content.Context;

public class HotOnlineWallpaperGalleryAdapterFactory {
    public static AbstractOnlineWallpaperAdapter createHotOnlineWallpaperGalleryAdapter(int type, Context context) {
        switch (type) {
            default:
            case 1:
                return new OnlineWallpaperGalleryAdapter(context);
        }
    }
}
