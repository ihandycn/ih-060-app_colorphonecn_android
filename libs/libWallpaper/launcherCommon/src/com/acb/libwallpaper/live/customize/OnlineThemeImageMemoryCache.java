package com.acb.libwallpaper.live.customize;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.SparseArray;

public class OnlineThemeImageMemoryCache {

    private static SparseArray<Bitmap> sBitmapList = new SparseArray<>(3);

    public static @Nullable Bitmap getBitmap(int key) {
        return sBitmapList.get(key);
    }

    public static void putBitmap(int key, Bitmap bitmap) {
        sBitmapList.put(key, bitmap);
    }

    public static void clear() {
        sBitmapList.clear();
    }

}
