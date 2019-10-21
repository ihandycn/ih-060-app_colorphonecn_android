package com.honeycomb.colorphone.wallpaper.update;

import android.content.Context;

/**
 * Indicates where user check update
 */

public enum  Entrance {
    ToolKit,
    Settings;

    public static Entrance get(Context c) {
        return false ? ToolKit : Settings;
    }
}
