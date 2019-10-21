package com.acb.libwallpaper.live.animation;

import android.graphics.RectF;

public class RectFPool {

    private static final int MAX_POOL_SIZE = 4;

    private static final RectF[] sPool = new RectF[MAX_POOL_SIZE];
    private static int sPoolSize;

    public static RectF obtain() {
        if (sPoolSize > 0) {
            return sPool[--sPoolSize];
        }
        return new RectF();
    }

    public static void recycle(RectF rect) {
        if (sPoolSize < MAX_POOL_SIZE) {
            sPool[sPoolSize++] = rect;
        }
    }
}
