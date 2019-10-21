package com.honeycomb.colorphone.wallpaper.customize;

import android.text.TextUtils;

import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.wallpaper.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.util.PicCache;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallpaperPicCacheUtils {

    public static final String KEY_READY_TO_SET_WALLPAPER_TYPE = "ready_to_set_wallpaper_type";
    public static final String KEY_READY_TO_SET_WALLPAPER_NAME = "ready_to_set_wallpaper_name";

    public static final int TYPE_LIVE_WALLPAPER = 1;
    public static final int TYPE_3D_WALLPAPER = 2;

    private static final String KEY_CACHE_WALLPAPER_URL = "shuffle_cache_wallpaper_url";
    private static final String KEY_CACHE_WALLPAPER_NAME = "shuffle_cache_wallpaper_name";
    private static Preferences sPreferenceHelper = Preferences.get(LauncherFiles.DESKTOP_PREFS);

    public static String getCacheWallpaperUrl() {
        String url = sPreferenceHelper.getString(KEY_CACHE_WALLPAPER_URL, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (PicCache.getInstance().isCachedPic(url)) {
            return url;
        } else {
            return null;
        }
    }

    private static List<String> getAll3DWallpaperPicUrls() {
        List<String> allPicList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, ?>> configs = (List<Map<String, ?>>) CustomizeConfig.getList("3DWallpapers", "Items");
        for (Map<String, ?> map : configs) {
            String name = String.valueOf(map.get("Name"));
            allPicList.add(CustomizeUtils.generate3DWallpaperThumbnailUrl(name));
        }
        return allPicList;
    }

    private static List<String> getAllLiveWallpaperPicUrls() {
        List<String> allPicList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<String> names = getLiveWallpaperNames();
        for (String name : names) {
            allPicList.add(CustomizeUtils.generateLiveWallpaperThumbnailUrl(name));
        }
        return allPicList;
    }

    public static List<String> getAllWallpaperPics() {
        List<String> allPic = getAll3DWallpaperPicUrls();
        allPic.addAll(getAllLiveWallpaperPicUrls());
        return allPic;
    }

    public static String getCacheWallpaperName() {
        return sPreferenceHelper.getString(KEY_CACHE_WALLPAPER_NAME, "");
    }

    public static void downloadPic(int type) {
        String[] info;
        switch (type) {
            case TYPE_LIVE_WALLPAPER:
                info = getLiveWallpaperNameAndPicUrl();
                break;
            case TYPE_3D_WALLPAPER:
            default:
                info = get3DWallpaperNameAndPicUrl();
                break;
        }

        String name = info[0];
        String url = info[1];
        if (TextUtils.isEmpty(url) || PicCache.getInstance().isCachedPic(url)) {
            return;
        }
        sPreferenceHelper.putString(KEY_CACHE_WALLPAPER_NAME, name);
        sPreferenceHelper.putString(KEY_CACHE_WALLPAPER_URL, url);
        PicCache.getInstance().downloadAndCachePic(url);
    }

    public static String[] get3DWallpaperNameAndPicUrl() {
        String[] strings = new String[2];

        @SuppressWarnings("unchecked")
        List<Map<String, ?>> configs = (List<Map<String, ?>>) CustomizeConfig.getList("3DWallpapers", "Items");
        if (configs.size() == 0) {
            return strings;
        }
        int index = com.honeycomb.colorphone.wallpaper.util.Utils.getRandomInt(configs.size());
        Map<String, ?> map = configs.get(index);
        String name = String.valueOf(map.get("Name"));
        if (!isUseful(name)) {
            if (index == 0) {
                index++;
            } else {
                index--;
            }
            name = String.valueOf(configs.get(index).get("Name"));
        }
        strings[0] = name;
        strings[1] = CustomizeUtils.generate3DWallpaperThumbnailUrl(name);
        return strings;

    }

    public static String[] getLiveWallpaperNameAndPicUrl() {
        String[] strings = new String[2];

        @SuppressWarnings("unchecked")
        List<String> names = getLiveWallpaperNames();
        if (names.size() == 0) {
            return strings;
        }
        int index = com.honeycomb.colorphone.wallpaper.util.Utils.getRandomInt(names.size());
        String name = names.get(index);
        if (!isUseful(name)) {
            if (index == 0) {
                index++;
            } else {
                index--;
            }
            name = names.get(index);
        }
        strings[0] = name;
        strings[1] = CustomizeUtils.generateLiveWallpaperThumbnailUrl(name);
        return strings;
    }

    private static List<String> getLiveWallpaperNames() {
        List<String> nameList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<?> names = CustomizeConfig.getList("LiveWallpapers", "Items");
        for (Object object : names) {
            String name;
            if (object instanceof HashMap) {
                name = ((HashMap<String, String>) object).get("Name");
            } else {
                name = String.valueOf(object);
            }
            nameList.add(name);
        }
        return nameList;
    }

    public static boolean isUseful(String name) {
        return !name.equals(CustomizeUtils.getCurrentLiveWallpaperName());
    }
}
