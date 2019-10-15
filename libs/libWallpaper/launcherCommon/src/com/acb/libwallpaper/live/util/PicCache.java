package com.acb.libwallpaper.live.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.acb.libwallpaper.live.customize.WallpaperPicCacheUtils;
import com.acb.libwallpaper.live.download.Downloader;
import com.acb.libwallpaper.live.download.SingleDownloadTask;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PicCache {

    private static PicCache sPicCache;
    public static final String PIC_CACHE_DIRECTORY = "pic_cache_directory";

    private PicCache() {
    }

    public static PicCache getInstance() {
        if (sPicCache == null) {
            synchronized (PicCache.class) {
                if (sPicCache == null) {
                    sPicCache = new PicCache();
                }
            }
        }
        return sPicCache;
    }

    public boolean isCachedPic(String url) {
        if (Downloader.isCachedSuccess(PIC_CACHE_DIRECTORY, url)) {
            return true;
        } else {
            return false;
        }
    }

    public void downloadAndCachePic(@NonNull String url) {
        downloadAndCachePic(url, null);
    }

    public void downloadAndCachePic(@NonNull String url, OnCacheSuccessListener listener) {
        Downloader.getInstance().download(new SingleDownloadTask(new Downloader.DownloadItem(url, Downloader.getDownloadPath(PIC_CACHE_DIRECTORY, url)),
                new SingleDownloadTask.SingleTaskListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(Downloader.DownloadItem item) {
                        if (listener != null) {
                            listener.onSuccess(item);
                        }
                    }

                    @Override
                    public void onFailed(Downloader.DownloadItem item) {
                        if (listener != null) {
                            listener.onFailure(item);
                        }
                    }
                }), null);
    }

    public File getCacheFile(String url) {
        File file = Downloader.getDownloadFile(PIC_CACHE_DIRECTORY, url);
        if (file.exists() && file.length() > 0) {
            return file;
        } else {
            return null;
        }
    }

    public @Nullable File getCacheDirectory() {
        return CommonUtils.getDirectory(PIC_CACHE_DIRECTORY);
    }

    public void clearCachedPic(String url) {
        File file = getCacheFile(url);
        if (file != null) {
            Utils.deleteRecursive(file);
        }
    }

    public Bitmap getCacheBitmap(String url) {
        File file = getCacheFile(url);
        if (file != null) {
            return BitmapFactory.decodeFile(file.getPath());
        } else {
            return null;
        }
    }

    public void clear() {
        File dir = getCacheDirectory();
        if (dir != null) {
            Utils.deleteRecursive(dir);
        }
    }

    @WorkerThread
    public void clearUnnecessaryCachedPics() {
        File directory = getCacheDirectory();
        if (directory == null) {
            HSLog.w("PicCache", "Error making cache directory, return");
            return;
        }
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            Set<String> allPic = getAllPicFileNames();
            for (File child : children) {
                String fileName = child.getName();
                if (!isNecessary(allPic, fileName)) {
                    Utils.deleteRecursive(child);
                }
            }
        }
    }

    private boolean isNecessary(Set<String> allNecessaryPics, String fileName) {
        if (allNecessaryPics == null || allNecessaryPics.size() == 0) {
            return false;
        }
        return allNecessaryPics.contains(fileName);
    }

    private Set<String> getAllPicFileNames() {
        List<String> allUrls = WallpaperPicCacheUtils.getAllWallpaperPics();

        Set<String> allFileNames = new HashSet<>(allUrls.size());
        for (String aUrl : allUrls) {
            allFileNames.add(getFileNameFromUrl(aUrl));
        }
        return allFileNames;
    }

    private String getFileNameFromUrl(String url) {
        return Utils.md5(url) + "." + Utils.getRemoteFileExtension(url);
    }

    public interface OnCacheSuccessListener {
        void onSuccess(Downloader.DownloadItem item);

        void onFailure(Downloader.DownloadItem item);
    }
}
