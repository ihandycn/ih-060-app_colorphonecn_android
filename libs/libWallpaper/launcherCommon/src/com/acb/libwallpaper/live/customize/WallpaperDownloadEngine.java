package com.acb.libwallpaper.live.customize;

import android.os.Build;
import android.util.Log;

import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.live.customize.util.CustomizeUtils;
import com.acb.libwallpaper.live.download.Downloader;
import com.acb.libwallpaper.live.download.SingleDownloadTask;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSServerAPIConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.MultiHashMap;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WallpaperDownloadEngine {

    public interface OnLoadWallpaperListener {
        void onLoadFinished(List<WallpaperInfo> wallpaperInfoList);

        void onLoadFailed();
    }

    private static WallpaperDownloadEngine sInstance;

    private static final String TAG = "WallpaperDownloadEngine";

    private static final int TURN_SIZE = 6;

    private static final HSPreferenceHelper sPrefs = HSPreferenceHelper.create(HSApplication.getContext(),
            LauncherFiles.CUSTOMIZE_PREFS);

    private static final String PREF_WALLPAPER_HOT_NEXT_PAGE = "pref_wallpaper_hot_next_page";
    private static final String PREF_WALLPAPER_CATEGORY_NEXT_PAGE = "pref_wallpaper_category_next_page_";

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final String WALLPAPER_HD_URL_PREFIX = "http://cdn.appcloudbox.net/launcherapps/apps/launcher/Wallpaper_New/High_resolution/";
    private static final String WALLPAPER_THUMBNAIL_URL_PREFIX = "http://cdn.appcloudbox.net/launcherapps/apps/launcher/Wallpaper_New/Low_resolution/";

    private static final String WALLPAPER_URL = HSConfig.optString("", "Application", "WallpaperAPIURL");
    private static final String SUFFIX_HOT = "tags/hottest";
    private static final String SUFFIX_CATEGORY = "categories/";
    private static final String DOWNLOAD_DIRECTORY = "live_3d_wallpaper";

    private static final String HD_SUFFIX = "High_revolution/";
    private static final String THUMB_SUFFIX = "Low_revolution/";

    private MultiHashMap<String, Callback> mCallbacks = new MultiHashMap<>();

    public static WallpaperDownloadEngine getInstance() {
        if (sInstance == null) {
            synchronized (WallpaperDownloadEngine.class) {
                if (sInstance == null) {
                    sInstance = new WallpaperDownloadEngine();
                }
            }
        }
        return sInstance;
    }

    @SuppressWarnings("unchecked")
    public static void getNextCategoryWallpaperList(int categoryIndex, final OnLoadWallpaperListener listener) {
        List<Map<String, ?>> wallpaperConfig = (List<Map<String, ?>>) CustomizeConfig.getList("Wallpapers");
        String baseUrl = CustomizeConfig.getString("", "NewWallpaper", "BaseUrl");

        final List<WallpaperInfo> wallpaperInfoList = new ArrayList<>(120);
        String categoryName = wallpaperConfig.get(categoryIndex).get(("Identifier")).toString();
        String hdUrl;
        String thumbUrl;

        String countRange = (wallpaperConfig.get(categoryIndex).get("Count")).toString();
        List<Range> fileRanges = new ArrayList<>(1);
        parseFileRanges(fileRanges, countRange);

        for (Range range : fileRanges) {
            for (int i = range.start; i <= range.end; i++) {
                hdUrl = baseUrl + HD_SUFFIX + categoryName + "/" + categoryName + "_H_" + i + ".jpeg";
                thumbUrl = baseUrl + THUMB_SUFFIX + categoryName + "/" +categoryName + "_L_" + i + ".jpeg";
                WallpaperInfo wallpaperInfo = WallpaperInfo.newOnlineWallpaper(
                        hdUrl,
                        thumbUrl);
                wallpaperInfoList.add(wallpaperInfo);
            }
        }

        if (listener != null) {
            Threads.postOnMainThread(() -> listener.onLoadFinished(wallpaperInfoList));
        }
    }

    private static void parseFileRanges(List<Range> fileRanges, String rangesStr) {
        String trimmed = rangesStr.replaceAll(" ", "");
        String[] split = trimmed.split(",");
        for (String rangeStr : split) {
            String[] bounds = rangeStr.split("-");
            try {
                Range range;
                switch (bounds.length) {
                    case 1:
                        int singlePoint = Integer.valueOf(bounds[0]);
                        range = new Range(singlePoint, singlePoint);
                        break;
                    case 2:
                        range = new Range(Integer.valueOf(bounds[0]), Integer.valueOf(bounds[1]));
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal format: " + rangesStr);
                }
                fileRanges.add(range);
            } catch (Exception e) {
                HSLog.w(TAG, "Illegal format for ranges: " + rangesStr);
                e.printStackTrace();
            }
        }
    }


    /**
     * Second page.
     * Default page.
     * 3D & Live.
     */
    @SuppressWarnings("unchecked")
    public static void getHotWallpaperList(OnLoadWallpaperListener listener) {
        List<WallpaperInfo> wallpapers;
        List<Object> liveConfigs = new ArrayList<>(CustomizeConfig.getList("LiveWallpapers", "Items"));
        List<Map<String, ?>> threeDConfigs = new ArrayList<>((List<Map<String, ?>>) CustomizeConfig.getList("3DWallpapers", "Items"));

        List<String> promotedLiveNames = new ArrayList<>((List<String>) CustomizeConfig.getList("LiveWallpapers", "VariantPromotion"));
        List<String> promoted3DNames = new ArrayList<>((List<String>) CustomizeConfig.getList("3DWallpapers", "VariantPromotion"));

        List<Object> promotedLiveConfigs = new ArrayList<>(promotedLiveNames.size());
        for (Iterator<Object> iterator = liveConfigs.iterator(); iterator.hasNext(); ) {
            Object liveConfig = iterator.next();
            String name;
            name = getLiveWallpaperName(liveConfig);
            boolean removedBySdkVersionCheck = false;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && name.equals("particleflow")){
                removedBySdkVersionCheck = true;
                iterator.remove();
            }
            if (promotedLiveNames.contains(name) && !removedBySdkVersionCheck) {
                iterator.remove();
                promotedLiveConfigs.add(liveConfig);
            }
        }
        liveConfigs.addAll(0, promotedLiveConfigs);

        List<Map<String, ?>> promoted3DConfigs = new ArrayList<>(promoted3DNames.size());
        for (Iterator<Map<String, ?>> iterator = threeDConfigs.iterator(); iterator.hasNext(); ) {
            Map<String, ?> threeDConfig = iterator.next();
            String name = (String) threeDConfig.get("Name");
            if (promoted3DNames.contains(name)) {
                iterator.remove();
                promoted3DConfigs.add(threeDConfig);
            }
        }
        threeDConfigs.addAll(0, promoted3DConfigs);

        int count = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getInt(CustomizeUtils.PREF_HOT_WALLPAPER_TURN_COUNT, 0);
        Log.i(TAG, "getHotWallpaperList: count is " + count);
        int turnStart = TURN_SIZE * count;

        //left shift make list disorder
        //this is for change order
        if (liveConfigs.size() > TURN_SIZE) {
            turnStart %= liveConfigs.size();
            leftShift(liveConfigs, turnStart);
        }
        if (threeDConfigs.size() > TURN_SIZE) {
            turnStart %= threeDConfigs.size();
            leftShift(threeDConfigs, turnStart);
        }

        wallpapers = new ArrayList<>(liveConfigs.size() + threeDConfigs.size());
        int minSize = Math.min(liveConfigs.size(), threeDConfigs.size());
        int maxSize = Math.max(liveConfigs.size(), threeDConfigs.size());
        for (int i = 0; i < maxSize; i++) {
            String name;
            int index;
            if (i < liveConfigs.size()) {
                Object liveConfig = liveConfigs.get(i);
                String videoUrl = null;
                name = getLiveWallpaperName(liveConfig);
                if (liveConfig instanceof HashMap) {
                    videoUrl = (String) ((HashMap) liveConfig).get("VideoUrl");
                }
                index = i < minSize ? 2 * i : i + minSize;
                wallpapers.add(index, WallpaperInfo.newLiveWallpaper(name, videoUrl));
            }

            if (i < threeDConfigs.size()) {
                name = (String) threeDConfigs.get(i).get("Name");
                String videoUrl = (String) threeDConfigs.get(i).get("VideoUrl");
                index = i < minSize ? 2 * i + 1 : i + minSize;
                wallpapers.add(index, WallpaperInfo.new3DWallpaper(name, videoUrl));
            }
        }

        if (listener != null) {
            listener.onLoadFinished(wallpapers);
        }
        CustomizeUtils.updateWallpaperTurn();
    }

    private static String getLiveWallpaperName(Object liveConfig) {
        String name;
        if (liveConfig instanceof HashMap) {
            name = (String) ((HashMap) liveConfig).get("Name");
        } else {
            name = String.valueOf(liveConfig);
        }
        return name;
    }

    /**
     * First page
     */
    public static void getNextNewWallpaperList(final OnLoadWallpaperListener listener) {
        final List<WallpaperInfo> wallpaperInfoList = new ArrayList<>();

        final int nextPage = sPrefs.getInt(PREF_WALLPAPER_HOT_NEXT_PAGE, 1);
        sPrefs.putInt(PREF_WALLPAPER_HOT_NEXT_PAGE, nextPage + 1);
        String url = WALLPAPER_URL + SUFFIX_HOT;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("page", nextPage);
            jsonObject.put("page_size", DEFAULT_PAGE_SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HSLog.d(TAG, url);
        final HSServerAPIConnection connection = new HSServerAPIConnection(url, HttpRequest.Method.POST, jsonObject);
        connection.setConnectTimeout(30 * 1000);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                HSLog.d(TAG, "load success : " + hsHttpConnection.getURL());
                try {
                    JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                    JSONArray wallpaperArray = bodyJSON.getJSONObject("data").getJSONArray("medias");

                    for (int i = 0; i < wallpaperArray.length(); i++) {
                        JSONObject wallpaper = wallpaperArray.getJSONObject(i);
                        WallpaperInfo wallpaperInfo = WallpaperInfo.newOnlineWallpaper(
                                wallpaper.getJSONObject("images").getString("hdurl"),
                                wallpaper.getJSONObject("images").getString("thumburl"), "",
                                wallpaper.getInt("downloads"));
                        wallpaperInfoList.add(wallpaperInfo);
                    }

                    int nextPage = sPrefs.getInt(PREF_WALLPAPER_HOT_NEXT_PAGE, 1);
                    int totalPage = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("page_count");
                    if (totalPage < nextPage) {
                        nextPage = 1;
                        sPrefs.putInt(PREF_WALLPAPER_HOT_NEXT_PAGE, nextPage);
                    }

                    if (listener != null) {
                        listener.onLoadFinished(wallpaperInfoList);
                    }
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onLoadFailed();
                    }
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.e(TAG, "load failed : " + hsHttpConnection.getURL() + " : " + hsError.getMessage());
                int nextPage = sPrefs.getInt(PREF_WALLPAPER_HOT_NEXT_PAGE, 1);
                sPrefs.putInt(PREF_WALLPAPER_HOT_NEXT_PAGE, nextPage - 1);
                if (listener != null) {
                    listener.onLoadFailed();
                }
            }
        });
        connection.startAsync();
    }

    private static void leftShift(List list, int start) {
        List toStart = new ArrayList<>();
        for (int index = 0; index < start; index++) {
            toStart.add(list.get(index));
        }
        list.removeAll(toStart);
        list.addAll(toStart);
    }

    public void getPreviewFile(String url, Callback callback) {
        String path = getDownloadPath(url);
        if (Utils.checkFileValid(new File(path))) {
            callback.onFileAvailable(path);
        } else {
            boolean isDownloading = (mCallbacks.putToList(url, callback) > 1);
            if (!isDownloading) {
                SingleDownloadTask task = new SingleDownloadTask(new Downloader.DownloadItem(url, path), new SingleDownloadTask.SingleTaskListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(Downloader.DownloadItem item) {
                        List<Callback> remove = mCallbacks.remove(url);
                        if (remove != null) {
                            for (Callback callback1 : remove) {
                                callback1.onFileAvailable(item.getPath());
                            }
                        }
                    }

                    @Override
                    public void onFailed(Downloader.DownloadItem item) {
                        mCallbacks.remove(url);
                    }
                });
                Downloader.getInstance().download(task, null);
            }
        }
    }

    private String getDownloadPath(String url) {
        return new File(CommonUtils.getDirectory(DOWNLOAD_DIRECTORY),
                Utils.md5(url) + "." + Utils.getRemoteFileExtension(url)).getAbsolutePath();
    }

    /**
     * Inclusive range of file names (eg. 1-100).
     */
    private static class Range {
        int start;
        int end;

        Range(int start, int end) {
            if (start > end) {
                throw new IllegalArgumentException("Range with negative length: " + this);
            }
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    public interface Callback {

        void onFileAvailable(String path);
    }
}
