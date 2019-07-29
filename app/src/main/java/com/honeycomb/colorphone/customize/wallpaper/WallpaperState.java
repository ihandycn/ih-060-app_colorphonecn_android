package com.honeycomb.colorphone.customize.wallpaper;

import android.graphics.Rect;

public class WallpaperState {

    static Rect CurrentRect;
    static boolean IsScrollable;
    static int ShowWidth;
    static float PerPageScrollOffset;

    public static Rect getCurrentRect() {
        return CurrentRect;
    }

    public static boolean isScrollable() {
        return IsScrollable;
    }

    public static int getShowWidth() {
        return ShowWidth;
    }

    public static float getPerPageScrollOffset() {
        return PerPageScrollOffset;
    }
}
