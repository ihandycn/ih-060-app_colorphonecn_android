package com.colorphone.lock.lockscreen.locker;

import com.honeycomb.colorphone.customize.util.CustomizeUtils;

class LockerEventUtils {

    public static String getWallpaperType(int type) {
        if (type == 0) {
            return "Default";
        }
        if (type == LockerWallpaperView.TYPE_IMAGE) {
            return "Image";
        }
        if (type == LockerWallpaperView.TYPE_VIDEO) {
            if (CustomizeUtils.getVideoAudioStatus() == CustomizeUtils.VIDEO_NO_AUDIO) {
                return "Live";
            } else {
                return "Video";
            }
        }
        return null;
    }
}
