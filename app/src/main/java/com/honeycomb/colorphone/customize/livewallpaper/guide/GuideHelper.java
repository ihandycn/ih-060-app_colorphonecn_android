package com.honeycomb.colorphone.customize.livewallpaper.guide;

import android.os.SystemClock;

import com.honeycomb.colorphone.customize.livewallpaper.BaseWallpaperManager;
import com.honeycomb.colorphone.customize.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by sundxing on 2018/5/28.
 */

public class GuideHelper {
    public static final int TYPE_LIVE_TOUCH = 1;
    public static final int TYPE_3D = 2;
    public static final int TYPE_NORMAL = 3;
    public static final long TOUCH_GUIDE_DELAY = 250;

    public static int getWallpaperType(BaseWallpaperManager manager, String name) {
        boolean is3D = manager.is3D(name);
        if (is3D) {
            return TYPE_3D;
        }

        boolean isTouchAlive = manager.touchable(name);
        return isTouchAlive ? TYPE_LIVE_TOUCH : TYPE_NORMAL;
    }

    public static void logWallpaperPreview(int type) {
//        boolean isLive = true;
//        String name = "Wallpaper_Live_NoGesture_Preview_Shown";
//        if (type == TYPE_3D) {
//            name = "Wallpaper_3D_Preview_Shown";
//            isLive = false;
//        } else if (type == TYPE_LIVE_TOUCH) {
//            name = "Wallpaper_Live_Gesture_Preview_Shown";
//        }
//
//        Analytics.logEvent(name, true);
//        if (isLive) {
//            Analytics.logEvent("Wallpaper_Live_Preview_Shown", true);
//        }

        HSPreferenceHelper.getDefault().putInt(LiveWallpaperConsts.PREF_KEY_WALLPAPER_PREVIEW_TYPE, type);
    }

    public static void logWallpaperApply(String wallpaperName) {
        int type = HSPreferenceHelper.getDefault().getInt(LiveWallpaperConsts.PREF_KEY_WALLPAPER_PREVIEW_TYPE, 0);

        String name = "UNKOWN";
        boolean isLive = true;
        if (type == TYPE_3D) {
            name = "Wallpaper_3D_SetAsWallpaper_Btn_Clicked";
            isLive = false;
        } else if (type == TYPE_LIVE_TOUCH) {
            name = "Wallpaper_Live_Gesture_SetAsWallpaper_Btn_Clicked";
        } else if (type == TYPE_NORMAL) {
            name = "Wallpaper_Live_NoGesture_SetAsWallpaper_Btn_Clicked";
        }
        Analytics.logEvent(name, true, "type", wallpaperName);
        if (isLive) {
            Analytics.logEvent("Wallpaper_Live_SetAsWallpaper_Btn_Clicked",
                    true, "type", wallpaperName);
        }
    }

    public static void logTipShowEvent(int type) {
        String name = "Alert_Wallpaper_live_NoGesture_preview_guide_showed";
        if (type == TYPE_3D) {
            name = "Alert_Wallpaper_3D_preview_guide_showed";
        } else if (type == TYPE_LIVE_TOUCH) {
            name = "Alert_Wallpaper_live_Gesture_preview_guide_showed";
        }
        Analytics.logEvent(name);
    }

    public static void logTipCloseEvent(int type) {
        String name = "Alert_Wallpaper_live_NoGesture_preview_guide_closed";
        if (type == TYPE_3D) {
            name = "Alert_Wallpaper_3D_preview_guide_closed";
        } else if (type == TYPE_LIVE_TOUCH) {
            name = "Alert_Wallpaper_live_Gesture_preview_guide_closed";
        }
        Analytics.logEvent(name);
    }

    public static long startTimeWaitForConsume;
    public static void logLiveGestureCancelStartOrEnd(boolean start) {
        if (start) {
            startTimeWaitForConsume = SystemClock.uptimeMillis();
        } else {
            if (startTimeWaitForConsume > 0) {
                logLiveGestureCancel(0);
                startTimeWaitForConsume = 0;
            }
        }
    }

    public static void logLiveGestureCancel() {
        if (startTimeWaitForConsume > 0) {
            logLiveGestureCancel(SystemClock.uptimeMillis() - startTimeWaitForConsume - TOUCH_GUIDE_DELAY);
            startTimeWaitForConsume = 0;
        }
    }

    private static void logLiveGestureCancel(long timeInterval) {
        String paramValue = "0";
        if (timeInterval > 1200) {
            paramValue = "1.2s";
        } else if (timeInterval > 0) {
            paramValue = "0-1.2s";
        } else {
        }
        Analytics.logEvent("Wallpaper_Live_Gesture_Preview_Slided", "GestureShowedTime", paramValue);
    }
}
